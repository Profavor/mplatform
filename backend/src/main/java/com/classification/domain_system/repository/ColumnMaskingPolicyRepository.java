package com.classification.domain_system.repository;

import com.classification.domain_system.entity.ColumnMaskingPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ColumnMaskingPolicyRepository extends JpaRepository<ColumnMaskingPolicy, UUID> {

    List<ColumnMaskingPolicy> findByIsActiveTrue();

    List<ColumnMaskingPolicy> findByDomainIdAndIsActiveTrue(UUID domainId);

    List<ColumnMaskingPolicy> findByTargetTypeAndTargetIdAndIsActiveTrue(String targetType, String targetId);

    Optional<ColumnMaskingPolicy> findByDomainIdAndFieldKeyAndTargetTypeAndTargetId(
            UUID domainId, String fieldKey, String targetType, String targetId);
}
