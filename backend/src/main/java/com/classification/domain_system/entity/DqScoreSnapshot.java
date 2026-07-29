package com.classification.domain_system.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DQ 스캔 실행 시마다 도메인별 품질 점수를 기록하는 스냅샷 엔티티.
 * 시계열 트렌드 분석에 사용됩니다.
 */
@Entity
@Table(name = "dq_score_snapshot", indexes = {
        @Index(name = "idx_dq_snapshot_domain_recorded", columnList = "domainId, recordedAt")
})
@Getter
@Setter
@NoArgsConstructor
public class DqScoreSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID domainId;

    /** DQ 점수 (0.0 ~ 100.0) */
    @Column(nullable = false)
    private double score;

    @Column(nullable = false)
    private long totalRecords;

    @Column(nullable = false)
    private long totalViolations;

    /** 스캔 유형: SCHEDULED, MANUAL */
    @Column(nullable = false, length = 20)
    private String scanType;

    @Column(nullable = false)
    private LocalDateTime recordedAt;

    @PrePersist
    protected void onPrePersist() {
        if (recordedAt == null) {
            recordedAt = LocalDateTime.now();
        }
    }
}
