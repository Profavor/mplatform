package com.classification.domain_system.service;

import com.classification.domain_system.entity.Record;
import com.classification.domain_system.entity.RecordHistory;
import com.classification.domain_system.repository.RecordHistoryRepository;
import com.classification.domain_system.repository.RecordRepository;
import com.classification.domain_system.repository.SurvivorshipRuleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecordMergeServiceTest {

    @Mock
    private RecordRepository recordRepository;
    @Mock
    private RecordHistoryRepository recordHistoryRepository;
    @Mock
    private SurvivorshipRuleRepository survivorshipRuleRepository;

    @InjectMocks
    private RecordMergeService recordMergeService;

    private UUID survivorId;
    private UUID mergedId;
    private Record survivor;
    private Record merged;

    @BeforeEach
    void setUp() {
        survivorId = UUID.randomUUID();
        mergedId = UUID.randomUUID();

        survivor = new Record();
        survivor.setId(survivorId);
        survivor.setStatus("ACTIVE");
        survivor.setData("{\"name\":\"삼성전자\", \"code\":\"005930\"}");
        survivor.setVersion(1);

        merged = new Record();
        merged.setId(mergedId);
        merged.setStatus("ACTIVE");
        merged.setData("{\"name\":\"(주)삼성전자\", \"phone\":\"02-123-4567\"}");
        merged.setVersion(1);
    }

    @Test
    @DisplayName("mergeRecords - survivor와 merged 레코드가 성공적으로 병합되고 merged 상태로 변경됨")
    void mergeRecords_Success() {
        RecordMergeService.MergeRequest req = new RecordMergeService.MergeRequest();
        req.survivorRecordId = survivorId;
        req.mergedRecordIds = List.of(mergedId);
        req.fieldResolutions = Map.of("phone", mergedId);

        when(recordRepository.findById(survivorId)).thenReturn(Optional.of(survivor));
        when(recordRepository.findById(mergedId)).thenReturn(Optional.of(merged));
        when(recordRepository.save(any(Record.class))).thenAnswer(i -> i.getArgument(0));

        Record result = recordMergeService.mergeRecords(req, "admin");

        assertThat(result.getId()).isEqualTo(survivorId);
        assertThat(result.getData()).contains("phone");
        assertThat(merged.getStatus()).isEqualTo("MERGED");
        assertThat(merged.getMergedIntoRecordId()).isEqualTo(survivorId);
        verify(recordHistoryRepository, times(2)).save(any(RecordHistory.class));
    }

    @Test
    @DisplayName("unmergeRecord - MERGED 상태의 레코드를 다시 ACTIVE로 복원하고 history 남김")
    void unmergeRecord_Success() {
        merged.setStatus("MERGED");
        merged.setMergedIntoRecordId(survivorId);

        when(recordRepository.findById(mergedId)).thenReturn(Optional.of(merged));
        when(recordRepository.save(any(Record.class))).thenAnswer(i -> i.getArgument(0));

        Record restored = recordMergeService.unmergeRecord(mergedId, "admin");

        assertThat(restored.getStatus()).isEqualTo("ACTIVE");
        assertThat(restored.getMergedIntoRecordId()).isNull();
        verify(recordHistoryRepository, times(1)).save(any(RecordHistory.class));
    }
}
