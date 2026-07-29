package com.classification.domain_system.service;

import com.classification.domain_system.entity.DqScoreSnapshot;
import com.classification.domain_system.repository.DqScoreSnapshotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * DQ 스코어 스냅샷 기록 및 트렌드 조회 서비스.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DqScoreSnapshotService {

    private final DqScoreSnapshotRepository snapshotRepository;

    /**
     * DQ 스캔 결과를 스냅샷으로 기록합니다.
     *
     * @param domainId  도메인 ID
     * @param scoreData DqRuleEngine.getDomainDqScore() 반환 데이터
     * @param scanType  스캔 유형 ("SCHEDULED" | "MANUAL")
     * @return 저장된 스냅샷
     */
    public DqScoreSnapshot recordSnapshot(UUID domainId, Map<String, Object> scoreData, String scanType) {
        DqScoreSnapshot snapshot = new DqScoreSnapshot();
        snapshot.setDomainId(domainId);
        snapshot.setScore(extractDouble(scoreData, "score", 0.0));
        snapshot.setTotalRecords(extractLong(scoreData, "totalRecords", 0L));
        snapshot.setTotalViolations(extractLong(scoreData, "totalViolations", 0L));
        snapshot.setScanType(scanType != null ? scanType : "MANUAL");
        snapshot.setRecordedAt(LocalDateTime.now());

        DqScoreSnapshot saved = snapshotRepository.save(snapshot);
        log.info("[DQ Snapshot] Recorded score={} for domain={} type={}",
                saved.getScore(), domainId, scanType);
        return saved;
    }

    /**
     * 기간 내 DQ 스코어 트렌드를 조회합니다.
     *
     * @param domainId 도메인 ID
     * @param from     시작 시각 (null이면 30일 전)
     * @param to       종료 시각 (null이면 현재)
     * @return 시간순 정렬된 스냅샷 리스트
     */
    public List<DqScoreSnapshot> getTrend(UUID domainId, LocalDateTime from, LocalDateTime to) {
        if (from == null) {
            from = LocalDateTime.now().minusDays(30);
        }
        if (to == null) {
            to = LocalDateTime.now();
        }
        return snapshotRepository.findByDomainIdAndRecordedAtBetweenOrderByRecordedAtAsc(domainId, from, to);
    }

    /**
     * 최근 30건의 DQ 스코어 스냅샷을 조회합니다 (최신순).
     */
    public List<DqScoreSnapshot> getRecentSnapshots(UUID domainId) {
        List<DqScoreSnapshot> snapshots = snapshotRepository.findTop30ByDomainIdOrderByRecordedAtDesc(domainId);
        // 트렌드 차트 용도로 시간순 정렬하여 반환
        Collections.reverse(snapshots);
        return snapshots;
    }

    private double extractDouble(Map<String, Object> map, String key, double defaultValue) {
        if (map == null || !map.containsKey(key)) return defaultValue;
        Object val = map.get(key);
        if (val instanceof Number) return ((Number) val).doubleValue();
        try { return Double.parseDouble(String.valueOf(val)); } catch (Exception e) { return defaultValue; }
    }

    private long extractLong(Map<String, Object> map, String key, long defaultValue) {
        if (map == null || !map.containsKey(key)) return defaultValue;
        Object val = map.get(key);
        if (val instanceof Number) return ((Number) val).longValue();
        try { return Long.parseLong(String.valueOf(val)); } catch (Exception e) { return defaultValue; }
    }
}
