package com.classification.domain_system.repository;

import com.classification.domain_system.entity.MatchCandidate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.List;

@Repository
public interface MatchCandidateRepository extends JpaRepository<MatchCandidate, UUID> {
    Page<MatchCandidate> findByNodeIdAndStatus(UUID nodeId, String status, Pageable pageable);
    Page<MatchCandidate> findByStatus(String status, Pageable pageable);

    Page<MatchCandidate> findByDomainIdAndStatus(UUID domainId, String status, Pageable pageable);

    Page<MatchCandidate> findByNodeIdInAndStatus(List<UUID> nodeIds, String status, Pageable pageable);

    // --- 피드백 통계 집계 쿼리 ---

    long countByMatchedRuleIdAndStatus(UUID matchedRuleId, String status);

    @Query("SELECT AVG(mc.score) FROM MatchCandidate mc WHERE mc.matchedRuleId = :ruleId AND mc.status = :status")
    Double findAvgScoreByMatchedRuleIdAndStatus(@Param("ruleId") UUID ruleId, @Param("status") String status);

    @Query("SELECT MAX(mc.score) FROM MatchCandidate mc WHERE mc.matchedRuleId = :ruleId AND mc.status = :status")
    Double findMaxScoreByMatchedRuleIdAndStatus(@Param("ruleId") UUID ruleId, @Param("status") String status);

    @Query("SELECT MIN(mc.score) FROM MatchCandidate mc WHERE mc.matchedRuleId = :ruleId AND mc.status = :status")
    Double findMinScoreByMatchedRuleIdAndStatus(@Param("ruleId") UUID ruleId, @Param("status") String status);
}
