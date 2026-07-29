package com.classification.domain_system.service;

import com.classification.domain_system.entity.MatchingRule;
import com.classification.domain_system.entity.Record;
import com.classification.domain_system.repository.MatchingRuleRepository;
import com.classification.domain_system.repository.RecordRepository;
import com.classification.domain_system.entity.ClassificationNode;
import com.classification.domain_system.repository.ClassificationNodeRepository;
import com.classification.domain_system.repository.FieldDefinitionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.*;
import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchingService {
    
    private final MatchingRuleRepository matchingRuleRepository;
    private final RecordRepository recordRepository;
    private final ClassificationNodeRepository nodeRepository;
    private final FieldDefinitionRepository fieldDefinitionRepository;
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
                        
                        List<Record> duplicates = recordRepository.findDynamicRecords(List.of(nodeId), null, searchParams, Pageable.unpaged()).getContent();
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
                            List<Record> duplicates = recordRepository.findDynamicRecords(List.of(nodeId), null, searchParams, Pageable.unpaged()).getContent();
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
                        List<Record> duplicates = recordRepository.findDynamicRecords(List.of(nodeId), null, searchParams, Pageable.unpaged()).getContent();
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
                    // FUZZY matching (상한 500건으로 페이징 제한)
                    List<Record> candidateRecords = recordRepository.findByNodeId(nodeId, org.springframework.data.domain.PageRequest.of(0, 500)).getContent();
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
                                    String s1 = v1.toString().trim().toLowerCase().replaceAll("[^a-zA-Z0-9가-힣]", "");
                                    String s2 = v2.toString().trim().toLowerCase().replaceAll("[^a-zA-Z0-9가-힣]", "");
                                    double sim = similarityAlgo.apply(s1, s2);
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
                }
            }
        } catch (Exception e) {
            log.error("Failed to check duplicate matching rules", e);
        }

        return result;
    }
}
