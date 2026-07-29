package com.classification.domain_system.service;

import com.classification.domain_system.entity.DqScoreSnapshot;
import com.classification.domain_system.repository.DqScoreSnapshotRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DqScoreSnapshotServiceTest {

    @Mock
    private DqScoreSnapshotRepository snapshotRepository;

    @InjectMocks
    private DqScoreSnapshotService dqScoreSnapshotService;

    private UUID domainId;

    @BeforeEach
    void setUp() {
        domainId = UUID.randomUUID();
    }

    @Test
    @DisplayName("스냅샷 저장 시 scoreData에서 올바른 값을 추출하여 엔티티에 저장한다")
    void recordSnapshot_ExtractsCorrectValues() {
        Map<String, Object> scoreData = Map.of(
                "score", 92.5,
                "totalRecords", 1000L,
                "totalViolations", 75L
        );

        DqScoreSnapshot savedEntity = new DqScoreSnapshot();
        savedEntity.setId(UUID.randomUUID());
        savedEntity.setScore(92.5);
        when(snapshotRepository.save(any(DqScoreSnapshot.class))).thenReturn(savedEntity);

        DqScoreSnapshot result = dqScoreSnapshotService.recordSnapshot(domainId, scoreData, "MANUAL");

        ArgumentCaptor<DqScoreSnapshot> captor = ArgumentCaptor.forClass(DqScoreSnapshot.class);
        verify(snapshotRepository).save(captor.capture());

        DqScoreSnapshot captured = captor.getValue();
        assertThat(captured.getDomainId()).isEqualTo(domainId);
        assertThat(captured.getScore()).isCloseTo(92.5, within(0.01));
        assertThat(captured.getTotalRecords()).isEqualTo(1000L);
        assertThat(captured.getTotalViolations()).isEqualTo(75L);
        assertThat(captured.getScanType()).isEqualTo("MANUAL");
        assertThat(captured.getRecordedAt()).isNotNull();
    }

    @Test
    @DisplayName("scoreData가 null이면 기본값 0으로 저장한다")
    void recordSnapshot_NullScoreData_UsesDefaults() {
        DqScoreSnapshot savedEntity = new DqScoreSnapshot();
        savedEntity.setId(UUID.randomUUID());
        when(snapshotRepository.save(any(DqScoreSnapshot.class))).thenReturn(savedEntity);

        dqScoreSnapshotService.recordSnapshot(domainId, null, "SCHEDULED");

        ArgumentCaptor<DqScoreSnapshot> captor = ArgumentCaptor.forClass(DqScoreSnapshot.class);
        verify(snapshotRepository).save(captor.capture());

        DqScoreSnapshot captured = captor.getValue();
        assertThat(captured.getScore()).isEqualTo(0.0);
        assertThat(captured.getTotalRecords()).isEqualTo(0L);
        assertThat(captured.getTotalViolations()).isEqualTo(0L);
        assertThat(captured.getScanType()).isEqualTo("SCHEDULED");
    }

    @Test
    @DisplayName("트렌드 조회 시 from/to가 null이면 기본값 30일 범위를 적용한다")
    void getTrend_NullDates_DefaultsTo30Days() {
        when(snapshotRepository.findByDomainIdAndRecordedAtBetweenOrderByRecordedAtAsc(
                any(), any(), any())).thenReturn(List.of());

        dqScoreSnapshotService.getTrend(domainId, null, null);

        ArgumentCaptor<LocalDateTime> fromCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<LocalDateTime> toCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(snapshotRepository).findByDomainIdAndRecordedAtBetweenOrderByRecordedAtAsc(
                eq(domainId), fromCaptor.capture(), toCaptor.capture());

        LocalDateTime from = fromCaptor.getValue();
        LocalDateTime to = toCaptor.getValue();
        assertThat(to).isAfter(from);
        // from은 대략 30일 전
        assertThat(java.time.Duration.between(from, to).toDays()).isBetween(29L, 31L);
    }

    @Test
    @DisplayName("최근 스냅샷 조회 시 시간순 오름차순으로 반환한다")
    void getRecentSnapshots_ReturnsChronologicalOrder() {
        DqScoreSnapshot newer = new DqScoreSnapshot();
        newer.setRecordedAt(LocalDateTime.now());
        newer.setScore(95.0);

        DqScoreSnapshot older = new DqScoreSnapshot();
        older.setRecordedAt(LocalDateTime.now().minusDays(1));
        older.setScore(90.0);

        // Repository는 최신순 내림차순 반환
        when(snapshotRepository.findTop30ByDomainIdOrderByRecordedAtDesc(domainId))
                .thenReturn(new java.util.ArrayList<>(List.of(newer, older)));

        List<DqScoreSnapshot> result = dqScoreSnapshotService.getRecentSnapshots(domainId);

        // Service는 시간순 오름차순으로 reverse하여 반환
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getScore()).isEqualTo(90.0); // older first
        assertThat(result.get(1).getScore()).isEqualTo(95.0); // newer second
    }
}
