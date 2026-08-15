package com.classification.domain_system.repository;

import com.classification.domain_system.entity.BulkImportJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BulkImportJobRepository extends JpaRepository<BulkImportJob, UUID> {
    List<BulkImportJob> findByDomainIdOrderByCreatedAtDesc(UUID domainId);
}
