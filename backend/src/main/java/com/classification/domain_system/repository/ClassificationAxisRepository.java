package com.classification.domain_system.repository;

import com.classification.domain_system.entity.ClassificationAxis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClassificationAxisRepository extends JpaRepository<ClassificationAxis, UUID> {

    @Query("SELECT a FROM ClassificationAxis a WHERE a.domain.id = :domainId ORDER BY a.sortOrder ASC")
    List<ClassificationAxis> findByDomainIdOrderBySortOrderAsc(@Param("domainId") UUID domainId);

    @Query("SELECT a FROM ClassificationAxis a WHERE a.domain.id = :domainId AND a.isDefault = true")
    Optional<ClassificationAxis> findByDomainIdAndIsDefaultTrue(@Param("domainId") UUID domainId);

    @Query("SELECT a FROM ClassificationAxis a WHERE a.domain.id = :domainId AND a.axisCode = :axisCode")
    Optional<ClassificationAxis> findByDomainIdAndAxisCode(@Param("domainId") UUID domainId, @Param("axisCode") String axisCode);

    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM ClassificationAxis a WHERE a.domain.id = :domainId AND a.axisCode = :axisCode")
    boolean existsByDomainIdAndAxisCode(@Param("domainId") UUID domainId, @Param("axisCode") String axisCode);
}
