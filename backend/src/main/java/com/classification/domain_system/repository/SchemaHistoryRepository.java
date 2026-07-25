package com.classification.domain_system.repository;

import com.classification.domain_system.entity.SchemaHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SchemaHistoryRepository extends JpaRepository<SchemaHistory, UUID>, JpaSpecificationExecutor<SchemaHistory> {
    Page<SchemaHistory> findByDomainIdOrderByChangedAtDesc(UUID domainId, Pageable pageable);
    java.util.List<SchemaHistory> findByDomainIdOrderByChangedAtDesc(UUID domainId);
}

