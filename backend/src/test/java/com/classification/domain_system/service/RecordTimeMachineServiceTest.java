package com.classification.domain_system.service;

import com.classification.domain_system.dto.RecordTimeMachineDto;
import com.classification.domain_system.entity.Record;
import com.classification.domain_system.entity.RecordHistory;
import com.classification.domain_system.exception.ResourceNotFoundException;
import com.classification.domain_system.repository.FieldDefinitionRepository;
import com.classification.domain_system.repository.RecordHistoryRepository;
import com.classification.domain_system.repository.RecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RecordTimeMachineServiceTest {

    @Mock private RecordRepository recordRepository;
    @Mock private RecordHistoryRepository recordHistoryRepository;
    @Mock private FieldDefinitionRepository fieldDefinitionRepository;

    @InjectMocks
    private RecordTimeMachineService timeMachineService;

    private UUID recordId;
    private Record record;

    @BeforeEach
    void setUp() {
        recordId = UUID.randomUUID();
        record = new Record();
        record.setId(recordId);
        record.setVersion(2);
        record.setData("{\"name\": \"홍길동\", \"phone\": \"010-9999-8888\", \"dept\": \"IT\"}");
    }

    @Test
    @DisplayName("getTimelineAndDiff: 버전 1과 버전 2 간의 필드 Diff 계산 정상 동작")
    void testGetTimelineAndDiff() {
        when(recordRepository.findById(recordId)).thenReturn(Optional.of(record));

        RecordHistory h1 = new RecordHistory();
        h1.setVersion(1);
        h1.setNewData("{\"name\": \"홍길동\", \"phone\": \"010-1234-5678\"}");
        h1.setChangedAt(LocalDateTime.now().minusDays(2));
        h1.setChangeType("INITIAL");
        h1.setSourceSystem("WEB");

        RecordHistory h2 = new RecordHistory();
        h2.setVersion(2);
        h2.setNewData("{\"name\": \"홍길동\", \"phone\": \"010-9999-8888\", \"dept\": \"IT\"}");
        h2.setChangedAt(LocalDateTime.now());
        h2.setChangeType("UPDATE");
        h2.setSourceSystem("API");

        when(recordHistoryRepository.findByRecordIdOrderByVersionAsc(recordId)).thenReturn(List.of(h1, h2));

        RecordTimeMachineDto.TimeMachineDiffResponse response = timeMachineService.getTimelineAndDiff(recordId, 1, 2);

        assertThat(response).isNotNull();
        assertThat(response.getV1()).isEqualTo(1);
        assertThat(response.getV2()).isEqualTo(2);
        assertThat(response.getFieldDiffs()).hasSize(3);

        // name -> UNCHANGED
        assertThat(response.getFieldDiffs().stream().anyMatch(d -> d.getFieldKey().equals("name") && d.getDiffStatus().equals("UNCHANGED"))).isTrue();
        // phone -> MODIFIED
        assertThat(response.getFieldDiffs().stream().anyMatch(d -> d.getFieldKey().equals("phone") && d.getDiffStatus().equals("MODIFIED"))).isTrue();
        // dept -> ADDED
        assertThat(response.getFieldDiffs().stream().anyMatch(d -> d.getFieldKey().equals("dept") && d.getDiffStatus().equals("ADDED"))).isTrue();
    }

    @Test
    @DisplayName("getTimelineAndDiff: 레코드 미존재 시 ResourceNotFoundException 발생")
    void testRecordNotFound() {
        when(recordRepository.findById(recordId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> timeMachineService.getTimelineAndDiff(recordId, 1, 2))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Record not found");
    }
}
