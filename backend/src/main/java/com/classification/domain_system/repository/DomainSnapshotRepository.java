package com.classification.domain_system.repository;

import com.classification.domain_system.entity.DomainSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DomainSnapshotRepository extends JpaRepository<DomainSnapshot, UUID> {
    List<DomainSnapshot> findByDomainIdOrderByCreatedAtDesc(UUID domainId);
}
