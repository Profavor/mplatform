package com.classification.domain_system.repository;

import com.classification.domain_system.entity.ClassificationAxis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClassificationAxisRepository extends JpaRepository<ClassificationAxis, UUID> {

    List<ClassificationAxis> findByDomainIdOrderBySortOrderAsc(UUID domainId);

    Optional<ClassificationAxis> findByDomainIdAndIsDefaultTrue(UUID domainId);

    Optional<ClassificationAxis> findByDomainIdAndAxisCode(UUID domainId, String axisCode);

    boolean existsByDomainIdAndAxisCode(UUID domainId, String axisCode);
}
