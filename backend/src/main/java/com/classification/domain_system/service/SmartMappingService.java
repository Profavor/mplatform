package com.classification.domain_system.service;

import com.classification.domain_system.dto.SmartMappingRecommendationDto;
import com.classification.domain_system.entity.ClassificationNode;
import com.classification.domain_system.entity.FieldDefinition;
import com.classification.domain_system.repository.ClassificationNodeRepository;
import com.classification.domain_system.repository.FieldDefinitionRepository;
import com.classification.domain_system.utils.KoreanTextMatcher;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmartMappingService {

    private final FieldDefinitionRepository fieldDefinitionRepository;
    private final ClassificationNodeRepository nodeRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional(readOnly = true)
    public List<SmartMappingRecommendationDto> recommendMappings(UUID domainId, String samplePayloadJson) {
        if (domainId == null || samplePayloadJson == null || samplePayloadJson.isBlank()) {
            return Collections.emptyList();
        }

        List<ClassificationNode> nodes = nodeRepository.findByDomain_Id(domainId);
        List<FieldDefinition> domainFields = fieldDefinitionRepository.findDomainFieldsWithSort(domainId);
        List<UUID> nodeIds = nodes.stream().map(ClassificationNode::getId).collect(Collectors.toList());
        List<FieldDefinition> nodeFields = !nodeIds.isEmpty() ? fieldDefinitionRepository.findByDefinedAtNode_IdIn(nodeIds) : Collections.emptyList();

        Map<String, FieldDefinition> fieldMap = new LinkedHashMap<>();
        domainFields.forEach(f -> fieldMap.put(f.getKey(), f));
        nodeFields.forEach(f -> fieldMap.putIfAbsent(f.getKey(), f));

        if (fieldMap.isEmpty()) {
            return Collections.emptyList();
        }

        Set<String> sourceKeys = extractKeys(samplePayloadJson);
        List<SmartMappingRecommendationDto> recommendations = new ArrayList<>();

        for (String sourceKey : sourceKeys) {
            String bestTargetKey = null;
            String bestTargetName = null;
            int bestScore = 0;
            String bestReason = null;

            String normSource = normalize(sourceKey);

            for (FieldDefinition fd : fieldMap.values()) {
                String targetKey = fd.getKey();
                String targetName = fd.getName() != null ? fd.getName().getOrDefault("ko", fd.getName().getOrDefault("en", targetKey)) : targetKey;
                String normTarget = normalize(targetKey);

                int score = 0;
                String reason = null;

                if (targetKey.equalsIgnoreCase(sourceKey)) {
                    score = 100;
                    reason = "필드 키 완전 일치";
                } else if (targetName.equalsIgnoreCase(sourceKey)) {
                    score = 98;
                    reason = "한글 필드명 완전 일치";
                } else if (normTarget.equals(normSource)) {
                    score = 95;
                    reason = "카멜/스네이크 정규화 일치";
                } else {
                    // Korean Fuzzy similarity
                    double korSim = KoreanTextMatcher.calculateKoreanFuzzySimilarity(targetName, sourceKey);
                    int korScore = (int) (korSim * 90);
                    if (korScore > score && korScore >= 50) {
                        score = korScore;
                        reason = String.format("한글 자모 퍼지 유사도 (신뢰도 %d%%)", korScore);
                    }

                    // Key substring match
                    if (normSource.contains(normTarget) || normTarget.contains(normSource)) {
                        int subScore = 70;
                        if (subScore > score) {
                            score = subScore;
                            reason = "필드 키 부분 일치";
                        }
                    }
                }

                if (score > bestScore) {
                    bestScore = score;
                    bestTargetKey = targetKey;
                    bestTargetName = targetName;
                    bestReason = reason;
                }
            }

            if (bestScore >= 50 && bestTargetKey != null) {
                recommendations.add(SmartMappingRecommendationDto.builder()
                        .sourceField(sourceKey)
                        .targetFieldKey(bestTargetKey)
                        .targetFieldName(bestTargetName)
                        .confidenceScore(bestScore)
                        .matchReason(bestReason)
                        .recommendedSpel(String.format("#payload['%s']", sourceKey))
                        .build());
            }
        }

        return recommendations;
    }

    private Set<String> extractKeys(String jsonStr) {
        Set<String> keys = new LinkedHashSet<>();
        try {
            if (jsonStr.trim().startsWith("[")) {
                List<Map<String, Object>> list = objectMapper.readValue(jsonStr, new TypeReference<List<Map<String, Object>>>() {});
                if (!list.isEmpty()) {
                    keys.addAll(list.get(0).keySet());
                }
            } else {
                Map<String, Object> map = objectMapper.readValue(jsonStr, new TypeReference<Map<String, Object>>() {});
                keys.addAll(map.keySet());
            }
        } catch (Exception e) {
            log.warn("Failed to parse sample payload JSON for keys: {}", e.getMessage());
        }
        return keys;
    }

    private String normalize(String input) {
        if (input == null) return "";
        return input.replaceAll("[^a-zA-Z0-9가-힣]", "").toLowerCase();
    }
}
