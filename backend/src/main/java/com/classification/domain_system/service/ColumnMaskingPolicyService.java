package com.classification.domain_system.service;

import com.classification.domain_system.dto.ColumnMaskingPolicyDto;
import com.classification.domain_system.entity.ColumnMaskingPolicy;
import com.classification.domain_system.entity.Department;
import com.classification.domain_system.exception.ResourceNotFoundException;
import com.classification.domain_system.repository.ColumnMaskingPolicyRepository;
import com.classification.domain_system.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ColumnMaskingPolicyService {

    private final ColumnMaskingPolicyRepository policyRepository;
    private final DepartmentRepository departmentRepository;
    private final PermissionAuditService auditService;

    @Transactional(readOnly = true)
    public List<ColumnMaskingPolicyDto.Response> getPolicies(UUID domainId) {
        List<ColumnMaskingPolicy> list = domainId != null
                ? policyRepository.findByDomainIdAndIsActiveTrue(domainId)
                : policyRepository.findByIsActiveTrue();

        Map<UUID, String> deptNameMap = loadDepartmentNames();

        return list.stream().map(p -> toResponse(p, deptNameMap)).toList();
    }

    @Transactional
    public ColumnMaskingPolicyDto.Response createPolicy(ColumnMaskingPolicyDto.Request request, String operator) {
        if (request == null || request.getFieldKey() == null || request.getTargetType() == null || request.getTargetId() == null) {
            throw new IllegalArgumentException("fieldKey, targetType, and targetId are required");
        }

        String fieldKey = request.getFieldKey().trim().toLowerCase();
        String targetType = request.getTargetType().trim().toUpperCase();
        String targetId = request.getTargetId().trim();
        String maskingAction = request.getMaskingAction() != null ? request.getMaskingAction().trim().toUpperCase() : "UNMASK";

        Optional<ColumnMaskingPolicy> existing = policyRepository.findByDomainIdAndFieldKeyAndTargetTypeAndTargetId(
                request.getDomainId(), fieldKey, targetType, targetId);

        ColumnMaskingPolicy policy;
        if (existing.isPresent()) {
            policy = existing.get();
            policy.setMaskingAction(maskingAction);
            policy.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
            policy.setDescription(request.getDescription());
        } else {
            policy = ColumnMaskingPolicy.builder()
                    .domainId(request.getDomainId())
                    .fieldKey(fieldKey)
                    .targetType(targetType)
                    .targetId(targetId)
                    .maskingAction(maskingAction)
                    .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                    .description(request.getDescription())
                    .createdBy(operator)
                    .build();
        }

        ColumnMaskingPolicy saved = policyRepository.save(policy);

        // Audit Log
        auditService.recordLog(
                null, targetId, "CREATE_MASK_POLICY", "MASKING_POLICY",
                saved.getId().toString(), fieldKey + " (" + targetType + ":" + targetId + ")",
                null, "Action=" + maskingAction + ", Active=" + saved.getIsActive(),
                operator, null
        );

        return toResponse(saved, loadDepartmentNames());
    }

    @Transactional
    public void deletePolicy(UUID policyId, String operator) {
        ColumnMaskingPolicy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new ResourceNotFoundException("ColumnMaskingPolicy not found: " + policyId));

        policyRepository.delete(policy);

        // Audit Log
        auditService.recordLog(
                null, policy.getTargetId(), "DELETE_MASK_POLICY", "MASKING_POLICY",
                policy.getId().toString(), policy.getFieldKey() + " (" + policy.getTargetType() + ":" + policy.getTargetId() + ")",
                "Action=" + policy.getMaskingAction(), null,
                operator, null
        );
    }

    /**
     * 특정 사용자에게 특정 필드에 대한 UNMASK 권한이 부여되어 있는지 판정
     */
    @Transactional(readOnly = true)
    public boolean canUserUnmaskField(String role, UUID departmentId, UUID domainId, String fieldKey) {
        if (fieldKey == null || fieldKey.isBlank()) return false;
        String lKey = fieldKey.trim().toLowerCase();

        List<ColumnMaskingPolicy> activePolicies = policyRepository.findByIsActiveTrue();
        for (ColumnMaskingPolicy p : activePolicies) {
            if (!p.getFieldKey().equalsIgnoreCase(lKey)) continue;

            // 도메인 스코프 체크 (null이면 전역 정책, 일치하면 도메인 정책)
            if (p.getDomainId() != null && domainId != null && !p.getDomainId().equals(domainId)) {
                continue;
            }

            // 1. 역할 기반 매칭
            if ("ROLE".equalsIgnoreCase(p.getTargetType()) && role != null) {
                for (String r : role.split(",")) {
                    if (r.trim().equalsIgnoreCase(p.getTargetId().trim())) {
                        return "UNMASK".equalsIgnoreCase(p.getMaskingAction());
                    }
                }
            }

            // 2. 부서 기반 매칭
            if ("DEPARTMENT".equalsIgnoreCase(p.getTargetType()) && departmentId != null) {
                if (departmentId.toString().equalsIgnoreCase(p.getTargetId().trim())) {
                    return "UNMASK".equalsIgnoreCase(p.getMaskingAction());
                }
            }
        }

        return false;
    }

    private Map<UUID, String> loadDepartmentNames() {
        Map<UUID, String> map = new HashMap<>();
        try {
            List<Department> depts = departmentRepository.findAll();
            for (Department d : depts) {
                String name = d.getName() != null ? d.getName() : d.getId().toString();
                map.put(d.getId(), name);
            }
        } catch (Exception e) {
            log.warn("Failed to load department names", e);
        }
        return map;
    }

    public ColumnMaskingPolicyDto.Response toResponse(ColumnMaskingPolicy p, Map<UUID, String> deptMap) {
        String code = "POL-" + (p.getId() != null ? p.getId().toString().substring(0, 8) : "00000000");
        String targetName = p.getTargetId();
        if ("DEPARTMENT".equalsIgnoreCase(p.getTargetType())) {
            try {
                UUID deptId = UUID.fromString(p.getTargetId());
                if (deptMap.containsKey(deptId)) {
                    targetName = deptMap.get(deptId);
                }
            } catch (Exception ignored) {}
        }

        return ColumnMaskingPolicyDto.Response.builder()
                .id(p.getId())
                .policyCode(code)
                .domainId(p.getDomainId())
                .fieldKey(p.getFieldKey())
                .targetType(p.getTargetType())
                .targetId(p.getTargetId())
                .targetName(targetName)
                .maskingAction(p.getMaskingAction())
                .isActive(p.getIsActive())
                .description(p.getDescription())
                .createdAt(p.getCreatedAt())
                .createdBy(p.getCreatedBy())
                .build();
    }
}
