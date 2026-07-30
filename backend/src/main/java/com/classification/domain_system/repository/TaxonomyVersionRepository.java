package com.classification.domain_system.repository;

import com.classification.domain_system.entity.TaxonomyVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TaxonomyVersionRepository extends JpaRepository<TaxonomyVersion, UUID> {
    List<TaxonomyVersion> findByDomainIdOrderByCreatedAtDesc(UUID domainId);
}
