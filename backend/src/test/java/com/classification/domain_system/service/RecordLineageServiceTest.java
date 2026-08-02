package com.classification.domain_system.service;

import com.classification.domain_system.dto.RecordLineageDto;
import com.classification.domain_system.entity.Record;
import com.classification.domain_system.repository.FieldDefinitionRepository;
import com.classification.domain_system.repository.IntegrationLogRepository;
import com.classification.domain_system.repository.RecordHistoryRepository;
import com.classification.domain_system.repository.RecordRepository;
import com.classification.domain_system.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class RecordLineageServiceTest {

    @Mock
    private RecordRepository recordRepository;

    @Mock
    private RecordHistoryRepository recordHistoryRepository;

    @Mock
    private IntegrationLogRepository integrationLogRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RecordLineageService recordLineageService;

    private Record testRecord;
    private UUID testId;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        testRecord = new Record();
        testRecord.setId(testId);
        testRecord.setCreatedAt(LocalDateTime.now().minusDays(2));
        testRecord.setStatus("ACTIVE");
    }

    @Test
    @DisplayName("레코드 UUID로 Data Lineage 그래프 노드 및 엣지를 추출한다")
    void testGetRecordLineage() {
        // given
        given(recordRepository.findById(testId)).willReturn(Optional.of(testRecord));
        given(recordHistoryRepository.findByRecordIdOrderByVersionAsc(testId)).willReturn(Collections.emptyList());
        given(integrationLogRepository.findByRecordIdOrderByCreatedAtDesc(testId)).willReturn(Collections.emptyList());

        // when
        RecordLineageDto.RecordLineageResponse response = recordLineageService.getRecordLineage(testId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getRecordId()).isEqualTo(testId);
        assertThat(response.getNodes()).isNotEmpty();
    }
}
