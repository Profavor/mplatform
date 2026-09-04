package com.classification.domain_system.service;

import com.classification.domain_system.entity.MatchingRule;
import com.classification.domain_system.exception.BusinessException;
import com.classification.domain_system.exception.ErrorCode;
import com.classification.domain_system.entity.Record;
import com.classification.domain_system.repository.MatchingRuleRepository;
import com.classification.domain_system.repository.RecordRepository;
import com.classification.domain_system.entity.ClassificationNode;
import com.classification.domain_system.repository.ClassificationNodeRepository;
import com.classification.domain_system.repository.FieldDefinitionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import com.classification.domain_system.config.MdmProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.*;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.query.StringQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import com.classification.domain_system.entity.RecordDocument;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchingService {
    
    private final MatchingRuleRepository matchingRuleRepository;
    private final RecordRepository recordRepository;
    private final ClassificationNodeRepository nodeRepository;
    private final FieldDefinitionRepository fieldDefinitionRepository;
    private final MdmProperties mdmProperties;
    private final ElasticsearchOperations elasticsearchOperations;
    private final ObjectMapper mapper = new ObjectMapper();

    public static class DuplicateResult {
        public boolean hasDuplicates;
        public List<UUID> duplicateRecordIds;
        public String message;
        public Double score = 1.0; // Default 1.0 (EXACT)
        public UUID matchedRuleId;
        public String matchType = "EXACT";
    }

    public DuplicateResult checkDuplicates(UUID nodeId, String dataJson) {
        DuplicateResult result = new DuplicateResult();
        result.hasDuplicates = false;
        result.duplicateRecordIds = new ArrayList<>();

        ClassificationNode node = nodeRepository.findById(nodeId).orElse(null);
        if (node == null) return result;

        List<MatchingRule> rules = matchingRuleRepository.findByDomainIdAndIsActiveTrue(node.getDomain().getId());

        try {
            Map<String, Object> data = mapper.readValue(dataJson, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
            
            // 1. Default Check: Domain Identifier Field
            UUID idFieldId = node.getDomain().getIdentifierFieldId();
            if (idFieldId != null) {
                com.classification.domain_system.entity.FieldDefinition idDef = fieldDefinitionRepository.findById(idFieldId).orElse(null);
                if (idDef != null && data.containsKey(idDef.getKey())) {
                    Object val = data.get(idDef.getKey());
                    if (val != null && !val.toString().isBlank()) {
                        Map<String, String> searchParams = new HashMap<>();
                        searchParams.put(idDef.getKey(), val.toString());
                        searchParams.put("op_" + idDef.getKey(), "EQ");
                        
                        List<Record> duplicates = new ArrayList<>();
                        try {
                            String json = String.format("{\"bool\": {\"must\": [{\"term\": {\"domainId\": \"%s\"}}, {\"match\": {\"dataMap.%s\": \"%s\"}}]}}",
                                    node.getDomain().getId().toString(), idDef.getKey(), val.toString().replace("\"", "\\\""));
                            StringQuery sq = new StringQuery(json);
                            SearchHits<RecordDocument> hits = elasticsearchOperations.search(sq, RecordDocument.class);
                            for (SearchHit<RecordDocument> hit : hits) {
                                Record r = new Record();
                                r.setId(UUID.fromString(hit.getContent().getId()));
                                duplicates.add(r);
                            }
                        } catch (Exception e) {
                            try {
                                List<Record> dbRecords = recordRepository.findActiveRecordsByDomainAndFieldValue(node.getDomain().getId(), idDef.getKey(), val.toString());
                                if (dbRecords != null && !dbRecords.isEmpty()) {
                                    duplicates = dbRecords;
                                }
                            } catch (Exception ex) {
                                // fallback to dynamic search
                            }
                            if (duplicates.isEmpty()) {
                                try {
                                    duplicates = recordRepository.findDynamicRecords(List.of(nodeId), null, searchParams, Pageable.unpaged()).getContent();
                                } catch (Exception ex) {
                                    try {
                                        duplicates = recordRepository.findDynamicRecordsByDomain(node.getDomain().getId(), searchParams, Pageable.unpaged()).getContent();
                                    } catch (Exception ignored) {}
                                }
                            }
                        }

                        if (!duplicates.isEmpty()) {
                            result.hasDuplicates = true;
                            duplicates.forEach(d -> result.duplicateRecordIds.add(d.getId()));
                            result.message = "Duplicate found based on Identifier Field (" + idDef.getKey() + ")";
                            return result;
                        }
                    }
                }
            }

            // 1-1. Candidate Key Check (emp_id, id, code, required fields) if no explicit identifier field matched
            if (!result.hasDuplicates) {
                List<com.classification.domain_system.entity.FieldDefinition> domainFields = fieldDefinitionRepository.findDomainFieldsWithSort(node.getDomain().getId());
                for (com.classification.domain_system.entity.FieldDefinition fd : domainFields) {
                    String k = fd.getKey();
                    if (data.containsKey(k) && data.get(k) != null && !data.get(k).toString().isBlank()) {
                        String lowerK = k.toLowerCase();
                        if (lowerK.endsWith("_id") || lowerK.endsWith("id") || lowerK.endsWith("_code") || lowerK.endsWith("code") || lowerK.endsWith("_no") || lowerK.endsWith("no") || Boolean.TRUE.equals(fd.getRequired())) {
                            Object val = data.get(k);
                            Map<String, String> searchParams = new HashMap<>();
                            searchParams.put(k, val.toString());
                            searchParams.put("op_" + k, "EQ");
                            
                            List<Record> duplicates = new ArrayList<>();
                            try {
                                String json = String.format("{\"bool\": {\"must\": [{\"term\": {\"domainId\": \"%s\"}}, {\"match\": {\"dataMap.%s\": \"%s\"}}]}}",
                                        node.getDomain().getId().toString(), k, val.toString().replace("\"", "\\\""));
                                StringQuery sq = new StringQuery(json);
                                SearchHits<RecordDocument> hits = elasticsearchOperations.search(sq, RecordDocument.class);
                                for (SearchHit<RecordDocument> hit : hits) {
                                    Record r = new Record();
                                    r.setId(UUID.fromString(hit.getContent().getId()));
                                    duplicates.add(r);
                                }
                            } catch (Exception e) {
                                try {
                                    List<Record> dbRecords = recordRepository.findActiveRecordsByDomainAndFieldValue(node.getDomain().getId(), k, val.toString());
                                    if (dbRecords != null && !dbRecords.isEmpty()) {
                                        duplicates = dbRecords;
                                    }
                                } catch (Exception ex) {
                                    // fallback to dynamic search
                                }
                                if (duplicates.isEmpty()) {
                                    try {
                                        duplicates = recordRepository.findDynamicRecords(List.of(nodeId), null, searchParams, Pageable.unpaged()).getContent();
                                    } catch (Exception ex) {
                                        try {
                                            duplicates = recordRepository.findDynamicRecordsByDomain(node.getDomain().getId(), searchParams, Pageable.unpaged()).getContent();
                                        } catch (Exception ignored) {}
                                    }
                                }
                            }
                            
                            if (!duplicates.isEmpty()) {
                                result.hasDuplicates = true;
                                duplicates.forEach(d -> result.duplicateRecordIds.add(d.getId()));
                                result.message = "Duplicate found based on candidate key field (" + k + ")";
                                return result;
                            }
                        }
                    }
                }
            }

            // 2. Additional Custom Rules
            if (rules.isEmpty()) return result;
            
            org.apache.commons.text.similarity.JaroWinklerSimilarity similarityAlgo = new org.apache.commons.text.similarity.JaroWinklerSimilarity();

            for (MatchingRule rule : rules) {
                // Check if rule applies to this node
                if (rule.getNodeId() != null && !rule.getNodeId().equals(nodeId)) {
                    continue;
                }
                
                String[] fields = mapper.readValue(rule.getTargetFieldKeys(), String[].class);
                if (fields.length == 0) continue;

                boolean isFuzzy = "FUZZY".equalsIgnoreCase(rule.getMatchType());
                double threshold = rule.getSimilarityThreshold() != null ? rule.getSimilarityThreshold() : 0.85;

                if (!isFuzzy) {
                    // EXACT matching
                    Map<String, String> searchParams = new HashMap<>();
                    boolean hasAllFields = true;
                    for (String field : fields) {
                        Object val = data.get(field);
                        if (val == null || val.toString().isBlank()) {
                            hasAllFields = false;
                            break;
                        }
                        searchParams.put(field, val.toString());
                        searchParams.put("op_" + field, "EQ");
                    }

                    if (hasAllFields) {
                        List<Record> duplicates = new ArrayList<>();
                        try {
                            StringBuilder musts = new StringBuilder();
                            musts.append(String.format("{\"term\": {\"domainId\": \"%s\"}}", node.getDomain().getId().toString()));
                            for (String field : fields) {
                                musts.append(String.format(",{\"match\": {\"dataMap.%s\": \"%s\"}}", field, data.get(field).toString().replace("\"", "\\\"")));
                            }
                            String json = String.format("{\"bool\": {\"must\": [%s]}}", musts.toString());
                            StringQuery sq = new StringQuery(json);
                            SearchHits<RecordDocument> hits = elasticsearchOperations.search(sq, RecordDocument.class);
                            for (SearchHit<RecordDocument> hit : hits) {
                                Record r = new Record();
                                r.setId(UUID.fromString(hit.getContent().getId()));
                                duplicates.add(r);
                            }
                        } catch (Exception e) {
                            duplicates = recordRepository.findDynamicRecords(List.of(nodeId), null, searchParams, Pageable.unpaged()).getContent();
                        }
                        
                        if (!duplicates.isEmpty()) {
                            result.hasDuplicates = true;
                            duplicates.forEach(d -> result.duplicateRecordIds.add(d.getId()));
                            result.message = "Potential duplicate found based on rule: " + rule.getRuleName() + " (fields: " + Arrays.toString(fields) + ")";
                            result.score = 1.0;
                            result.matchedRuleId = rule.getId();
                            result.matchType = "EXACT";
                            return result;
                        }
                    }
                } else {
                    try {
                        StringBuilder shoulds = new StringBuilder();
                        for (String field : fields) {
                            Object val = data.get(field);
                            if (val != null && !val.toString().isBlank()) {
                                if (shoulds.length() > 0) shoulds.append(",");
                                shoulds.append(String.format("{\"fuzzy\": {\"dataMap.%s\": {\"value\": \"%s\", \"fuzziness\": \"AUTO\"}}}", field, val.toString().replace("\"", "\\\"")));
                            }
                        }
                        String json = String.format("{\"bool\": {\"must\": [{\"term\": {\"domainId\": \"%s\"}}], \"should\": [%s], \"minimum_should_match\": 1}}", node.getDomain().getId().toString(), shoulds.toString());
                        StringQuery sq = new StringQuery(json);
                        sq.setMaxResults(10);
                        SearchHits<RecordDocument> hits = elasticsearchOperations.search(sq, RecordDocument.class);
                        if (hits.hasSearchHits()) {
                            SearchHit<RecordDocument> firstHit = hits.getSearchHit(0);
                            result.hasDuplicates = true;
                            result.duplicateRecordIds.add(UUID.fromString(firstHit.getContent().getId()));
                            result.message = "Fuzzy duplicate candidate found via OpenSearch (rule: " + rule.getRuleName() + ")";
                            result.score = (double) firstHit.getScore();
                            result.matchedRuleId = rule.getId();
                            result.matchType = "FUZZY";
                            return result;
                        }
                    } catch (Exception fallbackE) {
                        // FUZZY matching (JPA Fallback with JaroWinkler)
                        int pageSize = (mdmProperties != null && mdmProperties.getMatching() != null && mdmProperties.getMatching().getFuzzyMaxCandidates() > 0)
                                ? mdmProperties.getMatching().getFuzzyMaxCandidates() : 500;
                        int maxPages = (mdmProperties != null && mdmProperties.getMatching() != null && mdmProperties.getMatching().getFuzzyMaxPages() > 0)
                                ? mdmProperties.getMatching().getFuzzyMaxPages() : 10;
                        for (int page = 0; page < maxPages; page++) {
                            org.springframework.data.domain.Page<Record> candidatePage = recordRepository.findByNodeId(nodeId, org.springframework.data.domain.PageRequest.of(page, pageSize));
                            List<Record> candidateRecords = candidatePage.getContent();
                            for (Record cand : candidateRecords) {
                                if (cand.getData() == null) continue;
                                try {
                                    Map<String, Object> candData = mapper.readValue(cand.getData(), new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
                                    double totalScore = 0.0;
                                    int count = 0;
                                    for (String field : fields) {
                                        Object v1 = data.get(field);
                                        Object v2 = candData.get(field);
                                        if (v1 != null && v2 != null) {
                                            String str1 = v1.toString();
                                            String str2 = v2.toString();
                                            double sim = com.classification.domain_system.utils.KoreanTextMatcher.calculateKoreanFuzzySimilarity(str1, str2);
                                            totalScore += sim;
                                            count++;
                                        }
                                    }
                                    if (count > 0) {
                                        double avgScore = totalScore / count;
                                        if (avgScore >= threshold) {
                                            result.hasDuplicates = true;
                                            result.duplicateRecordIds.add(cand.getId());
                                            result.message = "Fuzzy duplicate candidate found based on rule: " + rule.getRuleName() + " (Score: " + String.format("%.2f", avgScore) + ")";
                                            result.score = avgScore;
                                            result.matchedRuleId = rule.getId();
                                            result.matchType = "FUZZY";
                                            return result;
                                        }
                                    }
                                } catch (Exception ignored) {}
                            }
                            if (!candidatePage.hasNext()) break;
                        }
                    }
                }
            }
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            log.warn("Invalid data JSON provided for duplicate matching check: {}", e.getMessage());
            return result;
        } catch (org.springframework.dao.DataAccessException e) {
            log.error("Database error during duplicate matching check — aborting", e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR,
                "Duplicate matching check failed due to database error: " + e.getMessage());
        } catch (RuntimeException e) {
            log.error("Unexpected error during duplicate matching check — aborting", e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR,
                "Duplicate matching check failed: " + e.getMessage());
        }

        return result;
    }

    public List<Map<String, Object>> fuzzySearch(UUID nodeId, String keyword, int page, int size) {
        if (keyword == null || keyword.isBlank()) {
            return Collections.emptyList();
        }
        int maxPages = mdmProperties.getMatching().getFuzzyMaxPages();
        if (page >= maxPages) {
            return Collections.emptyList();
        }
        
        try {
            String domainId = nodeRepository.findById(nodeId).map(n -> n.getDomain().getId().toString()).orElse("");
            String json = String.format("{\"bool\": {\"must\": [{\"term\": {\"domainId\": \"%s\"}}, {\"match\": {\"searchableData\": \"%s\"}}]}}",
                    domainId, keyword.replace("\"", "\\\""));
            StringQuery sq = new StringQuery(json);
            sq.setPageable(org.springframework.data.domain.PageRequest.of(page, size));
            SearchHits<RecordDocument> hits = elasticsearchOperations.search(sq, RecordDocument.class);
            List<Map<String, Object>> results = new ArrayList<>();
            for (SearchHit<RecordDocument> hit : hits) {
                Map<String, Object> map = new HashMap<>();
                map.put("recordId", UUID.fromString(hit.getContent().getId()));
                map.put("data", hit.getContent().getData() != null ? hit.getContent().getData() : new HashMap<>());
                map.put("score", (double) hit.getScore());
                results.add(map);
            }
            return results;
        } catch (Exception e) {
            org.springframework.data.domain.Page<Record> candidatePage = recordRepository.findByNodeId(nodeId, org.springframework.data.domain.PageRequest.of(page, size));
            List<Map<String, Object>> results = new ArrayList<>();
            org.apache.commons.text.similarity.JaroWinklerSimilarity similarityAlgo = new org.apache.commons.text.similarity.JaroWinklerSimilarity();
            String s2 = keyword.trim().toLowerCase().replaceAll("[^a-zA-Z0-9가-힣]", "");
            
            for (Record cand : candidatePage.getContent()) {
                if (cand.getData() == null) continue;
                try {
                    Map<String, Object> candData = mapper.readValue(cand.getData(), new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
                    double maxScore = 0.0;
                    for (Object value : candData.values()) {
                        if (value == null) continue;
                        String s1 = value.toString().trim().toLowerCase().replaceAll("[^a-zA-Z0-9가-힣]", "");
                        double sim = similarityAlgo.apply(s1, s2);
                        if (sim > maxScore) {
                            maxScore = sim;
                        }
                    }
                    
                    if (maxScore >= 0.5) {
                        Map<String, Object> hit = new HashMap<>();
                        hit.put("recordId", cand.getId());
                        hit.put("data", candData);
                        hit.put("score", maxScore);
                        results.add(hit);
                    }
                } catch (Exception ignored) {}
            }
            
            results.sort((a, b) -> Double.compare((Double) b.get("score"), (Double) a.get("score")));
            return results;
        }
    }
}
