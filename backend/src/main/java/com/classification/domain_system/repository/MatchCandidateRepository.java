package com.classification.domain_system.repository;

import com.classification.domain_system.entity.MatchCandidate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.List;

@Repository
public interface MatchCandidateRepository extends JpaRepository<MatchCandidate, UUID> {
    Page<MatchCandidate> findByNodeIdAndStatus(UUID nodeId, String status, Pageable pageable);
    Page<MatchCandidate> findByStatus(String status, Pageable pageable);

    Page<MatchCandidate> findByDomainIdAndStatus(UUID domainId, String status, Pageable pageable);

    Page<MatchCandidate> findByNodeIdInAndStatus(List<UUID> nodeIds, String status, Pageable pageable);
}
