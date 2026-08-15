package com.classification.domain_system.service;

import com.classification.domain_system.dto.DomainSnapshotDto;
import com.classification.domain_system.entity.DomainSnapshot;
import com.classification.domain_system.entity.Record;
import com.classification.domain_system.repository.DomainSnapshotRepository;
import com.classification.domain_system.repository.RecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DomainSnapshotServiceTest {

    @Mock private DomainSnapshotRepository domainSnapshotRepository;
    @Mock private RecordRepository recordRepository;

    @InjectMocks
    private DomainSnapshotService domainSnapshotService;

    private UUID domainId;
    private UUID snapshotId;
    private UUID recordId;
    private Record record;

    @BeforeEach
    void setUp() {
        domainId = UUID.randomUUID();
        snapshotId = UUID.randomUUID();
        recordId = UUID.randomUUID();

        record = new Record();
        record.setId(recordId);
        record.setData("{\"name\":\"홍길동\",\"dept\":\"인사팀\"}");
        record.setStatus("ACTIVE");
    }

    @Test
    @DisplayName("createSnapshot: 도메인 전체 레코드 스냅샷 생성 검증")
    void testCreateSnapshot() {
        when(recordRepository.findAllByDomainId(domainId)).thenReturn(List.of(record));

        DomainSnapshot savedSnapshot = DomainSnapshot.builder()
                .id(snapshotId)
                .domainId(domainId)
                .snapshotName("정기 백업")
                .versionTag("v1.0")
                .recordCount(1)
                .snapshotData("[{\"id\":\"" + recordId + "\",\"data\":\"{\\\"name\\\":\\\"홍길동\\\"}\",\"status\":\"ACTIVE\"}]")
                .build();

        when(domainSnapshotRepository.save(any(DomainSnapshot.class))).thenReturn(savedSnapshot);

        DomainSnapshotDto.SnapshotCreateRequest request = DomainSnapshotDto.SnapshotCreateRequest.builder()
                .snapshotName("정기 백업")
                .versionTag("v1.0")
                .build();

        DomainSnapshotDto.SnapshotResponse response = domainSnapshotService.createSnapshot(domainId, request, "ADMIN");

        assertThat(response).isNotNull();
        assertThat(response.getRecordCount()).isEqualTo(1);
        assertThat(response.getVersionTag()).isEqualTo("v1.0");
    }

    @Test
    @DisplayName("restoreSnapshot: 스냅샷 데이터를 기반으로 레코드 복원 수행 검증")
    void testRestoreSnapshot() {
        DomainSnapshot snapshot = DomainSnapshot.builder()
                .id(snapshotId)
                .domainId(domainId)
                .versionTag("v1.0")
                .snapshotData("[{\"id\":\"" + recordId + "\",\"data\":\"{\\\"name\\\":\\\"홍길동_과거\\\"}\",\"status\":\"ACTIVE\"}]")
                .build();

        when(domainSnapshotRepository.findById(snapshotId)).thenReturn(Optional.of(snapshot));
        when(recordRepository.findById(recordId)).thenReturn(Optional.of(record));

        DomainSnapshotDto.SnapshotRestoreResponse response = domainSnapshotService.restoreSnapshot(snapshotId);

        assertThat(response).isNotNull();
        assertThat(response.getRestoredRecords()).isEqualTo(1);
        assertThat(record.getData()).contains("홍길동_과거");
        verify(recordRepository, times(1)).save(record);
    }
}
