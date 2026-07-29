package com.classification.domain_system.repository;

import com.classification.domain_system.entity.DqScoreSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface DqScoreSnapshotRepository extends JpaRepository<DqScoreSnapshot, UUID> {

    /**
     * 기간 내 도메인 DQ 스코어 트렌드 조회 (시간순 오름차순)
     */
    List<DqScoreSnapshot> findByDomainIdAndRecordedAtBetweenOrderByRecordedAtAsc(
            UUID domainId, LocalDateTime from, LocalDateTime to);

    /**
     * 최근 N건의 스냅샷 조회 (최신순 내림차순)
     */
    List<DqScoreSnapshot> findTop30ByDomainIdOrderByRecordedAtDesc(UUID domainId);
}
