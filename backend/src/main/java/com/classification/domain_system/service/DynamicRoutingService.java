package com.classification.domain_system.service;

import com.classification.domain_system.dto.ApprovalRoutingDto;
import com.classification.domain_system.entity.ApprovalRoutingTemplate;
import com.classification.domain_system.repository.ApprovalRoutingTemplateRepository;
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
public class DynamicRoutingService {

    private final ApprovalRoutingTemplateRepository templateRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public ApprovalRoutingDto.TemplateResponse createTemplate(ApprovalRoutingDto.TemplateCreateRequest req) {
        String stepsJson;
        try {
            stepsJson = objectMapper.writeValueAsString(req.getSteps() != null ? req.getSteps() : Collections.emptyList());
        } catch (Exception e) {
            stepsJson = "[]";
        }

        ApprovalRoutingTemplate template = ApprovalRoutingTemplate.builder()
                .templateName(req.getTemplateName())
                .domainId(req.getDomainId())
                .conditionField(req.getConditionField())
                .conditionOperator(req.getConditionOperator() != null ? req.getConditionOperator() : "DEFAULT")
                .conditionValue(req.getConditionValue())
                .stepsJson(stepsJson)
                .build();

        ApprovalRoutingTemplate saved = templateRepository.save(template);

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ApprovalRoutingDto.TemplateResponse> getTemplates(UUID domainId) {
        List<ApprovalRoutingTemplate> list = domainId != null
                ? templateRepository.findByDomainId(domainId)
                : templateRepository.findAll();

        return list.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ApprovalRoutingDto.EvaluateRouteResponse evaluateRoute(ApprovalRoutingDto.EvaluateRouteRequest req) {
        Map<String, Object> data = req.getRecordData() != null ? req.getRecordData() : Collections.emptyMap();
        List<ApprovalRoutingTemplate> templates = templateRepository.findAll();

        for (ApprovalRoutingTemplate t : templates) {
            if (t.getDomainId() != null && req.getDomainId() != null && !t.getDomainId().equals(req.getDomainId())) {
                continue;
            }

            String field = t.getConditionField();
            String op = t.getConditionOperator();
            String expectedVal = t.getConditionValue();

            if ("DEFAULT".equalsIgnoreCase(op) || field == null || field.isBlank()) {
                continue; // check explicit rules first
            }

            Object actualValObj = data.get(field);
            String actualVal = actualValObj != null ? String.valueOf(actualValObj) : "";

            boolean matched = false;
            if ("EQUALS".equalsIgnoreCase(op)) {
                matched = actualVal.equalsIgnoreCase(expectedVal);
            } else if ("CONTAINS".equalsIgnoreCase(op)) {
                matched = actualVal.toLowerCase().contains(expectedVal != null ? expectedVal.toLowerCase() : "");
            } else if ("GTE".equalsIgnoreCase(op)) {
                try {
                    double v1 = Double.parseDouble(actualVal.replaceAll("[^0-9.]", ""));
                    double v2 = Double.parseDouble(expectedVal.replaceAll("[^0-9.]", ""));
                    matched = v1 >= v2;
                } catch (Exception ignored) {}
            }

            if (matched) {
                return ApprovalRoutingDto.EvaluateRouteResponse.builder()
                        .matchedTemplateName(t.getTemplateName())
                        .dynamicSteps(parseSteps(t.getStepsJson()))
                        .matchReason(String.format("조건 일치: [%s] %s '%s' (실제값: '%s')", field, op, expectedVal, actualVal))
                        .build();
            }
        }

        // Fallback default step
        return ApprovalRoutingDto.EvaluateRouteResponse.builder()
                .matchedTemplateName("기본 결재선")
                .dynamicSteps(List.of(
                        ApprovalRoutingDto.ApproverStepDto.builder()
                                .stepOrder(1)
                                .requiredRole("ROLE_ADMIN")
                                .stepName("관리자 승인")
                                .build()
                ))
                .matchReason("조건부 일치 규칙 없음 -> 기본 1단계 관리자 승인 배정")
                .build();
    }

    private ApprovalRoutingDto.TemplateResponse mapToResponse(ApprovalRoutingTemplate t) {
        return ApprovalRoutingDto.TemplateResponse.builder()
                .id(t.getId())
                .templateName(t.getTemplateName())
                .domainId(t.getDomainId())
                .conditionField(t.getConditionField())
                .conditionOperator(t.getConditionOperator())
                .conditionValue(t.getConditionValue())
                .steps(parseSteps(t.getStepsJson()))
                .createdAt(t.getCreatedAt())
                .build();
    }

    private List<ApprovalRoutingDto.ApproverStepDto> parseSteps(String json) {
        if (json == null || json.isBlank()) return Collections.emptyList();
        try {
            return objectMapper.readValue(json, new TypeReference<List<ApprovalRoutingDto.ApproverStepDto>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
