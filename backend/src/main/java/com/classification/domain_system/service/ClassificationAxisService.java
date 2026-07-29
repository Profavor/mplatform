package com.classification.domain_system.service;

import com.classification.domain_system.dto.ClassificationAxisRequest;
import com.classification.domain_system.dto.ClassificationAxisResponse;
import com.classification.domain_system.entity.ClassificationAxis;
import com.classification.domain_system.entity.Domain;
import com.classification.domain_system.exception.BusinessException;
import com.classification.domain_system.exception.ErrorCode;
import com.classification.domain_system.exception.ResourceNotFoundException;
import com.classification.domain_system.repository.ClassificationAxisRepository;
import com.classification.domain_system.repository.DomainRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 도메인별 분류축(Classification Axis) 생성/조회/수정/삭제 서비스.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ClassificationAxisService {

    private final ClassificationAxisRepository axisRepository;
    private final DomainRepository domainRepository;

    @Transactional(readOnly = true)
    public List<ClassificationAxisResponse> getAxesByDomain(UUID domainId) {
        return axisRepository.findByDomainIdOrderBySortOrderAsc(domainId).stream()
                .map(ClassificationAxisResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ClassificationAxisResponse getAxisById(UUID axisId) {
        ClassificationAxis axis = axisRepository.findById(axisId)
                .orElseThrow(() -> new ResourceNotFoundException("ClassificationAxis not found with id: " + axisId));
        return ClassificationAxisResponse.fromEntity(axis);
    }

    @Transactional
    public ClassificationAxisResponse createAxis(UUID domainId, ClassificationAxisRequest request) {
        Domain domain = domainRepository.findById(domainId)
                .orElseThrow(() -> new ResourceNotFoundException("Domain not found with id: " + domainId));

        if (axisRepository.existsByDomainIdAndAxisCode(domainId, request.getAxisCode())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "Duplicate axis code: " + request.getAxisCode());
        }

        ClassificationAxis axis = new ClassificationAxis();
        axis.setDomain(domain);
        axis.setAxisCode(request.getAxisCode());
        axis.setName(request.getName() != null ? request.getName() : Map.of("ko", request.getAxisCode()));
        axis.setDescription(request.getDescription());
        axis.setIsDefault(Boolean.TRUE.equals(request.getIsDefault()));
        axis.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);

        ClassificationAxis saved = axisRepository.save(axis);
        log.info("[Axis] Created new classification axis: code={} domain={}", saved.getAxisCode(), domainId);
        return ClassificationAxisResponse.fromEntity(saved);
    }

    @Transactional
    public ClassificationAxisResponse updateAxis(UUID axisId, ClassificationAxisRequest request) {
        ClassificationAxis axis = axisRepository.findById(axisId)
                .orElseThrow(() -> new ResourceNotFoundException("ClassificationAxis not found with id: " + axisId));

        if (request.getName() != null) axis.setName(request.getName());
        if (request.getDescription() != null) axis.setDescription(request.getDescription());
        if (request.getSortOrder() != null) axis.setSortOrder(request.getSortOrder());
        if (request.getIsDefault() != null) axis.setIsDefault(request.getIsDefault());

        ClassificationAxis updated = axisRepository.save(axis);
        return ClassificationAxisResponse.fromEntity(updated);
    }

    @Transactional
    public void deleteAxis(UUID axisId) {
        ClassificationAxis axis = axisRepository.findById(axisId)
                .orElseThrow(() -> new ResourceNotFoundException("ClassificationAxis not found with id: " + axisId));

        if (Boolean.TRUE.equals(axis.getIsDefault())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "Cannot delete default classification axis.");
        }

        axisRepository.delete(axis);
        log.info("[Axis] Deleted classification axis: id={}", axisId);
    }

    @Transactional
    public ClassificationAxis getOrCreateDefaultAxis(UUID domainId) {
        return axisRepository.findByDomainIdAndIsDefaultTrue(domainId)
                .orElseGet(() -> {
                    Domain domain = domainRepository.findById(domainId)
                            .orElseThrow(() -> new ResourceNotFoundException("Domain not found with id: " + domainId));
                    ClassificationAxis defaultAxis = new ClassificationAxis();
                    defaultAxis.setDomain(domain);
                    defaultAxis.setAxisCode("DEFAULT");
                    defaultAxis.setName(Map.of("ko", "기본 분류축", "en", "Default Axis"));
                    defaultAxis.setDescription("도메인의 기본 분류 트리가 속하는 축");
                    defaultAxis.setIsDefault(true);
                    defaultAxis.setSortOrder(0);
                    return axisRepository.save(defaultAxis);
                });
    }
}
