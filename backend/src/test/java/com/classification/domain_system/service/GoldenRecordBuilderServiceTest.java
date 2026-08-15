package com.classification.domain_system.service;

import com.classification.domain_system.dto.GoldenRecordDto;
import com.classification.domain_system.entity.Record;
import com.classification.domain_system.repository.RecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GoldenRecordBuilderServiceTest {

    @Mock private RecordRepository recordRepository;

    @InjectMocks
    private GoldenRecordBuilderService goldenRecordBuilderService;

    private UUID rec1Id;
    private UUID rec2Id;
    private Record rec1;
    private Record rec2;

    @BeforeEach
    void setUp() {
        rec1Id = UUID.randomUUID();
        rec2Id = UUID.randomUUID();

        rec1 = new Record();
        rec1.setId(rec1Id);
        rec1.setSourceSystem("ERP");
        rec1.setData("{\"name\":\"홍길동\",\"phone\":\"010-1111-2222\",\"email\":\"\"}");

        rec2 = new Record();
        rec2.setId(rec2Id);
        rec2.setSourceSystem("CRM");
        rec2.setData("{\"name\":\"홍길동\",\"phone\":\"\",\"email\":\"hong@crm.com\"}");
    }

    @Test
    @DisplayName("buildGoldenRecord: 후보 레코드들의 유효 필드를 결합하여 골든 레코드 빌드")
    void testBuildGoldenRecord() {
        when(recordRepository.findAllById(List.of(rec1Id, rec2Id))).thenReturn(List.of(rec1, rec2));

        GoldenRecordDto.GoldenRecordBuildRequest req = GoldenRecordDto.GoldenRecordBuildRequest.builder()
                .targetRecordIds(List.of(rec1Id, rec2Id))
                .priorityStrategy("SOURCE_PRIORITY")
                .build();

        GoldenRecordDto.GoldenRecordPreviewResponse res = goldenRecordBuilderService.buildGoldenRecord(req);

        assertThat(res).isNotNull();
        assertThat(res.getCandidateRecordCodes()).hasSize(2);
        assertThat(res.getAssembledData()).containsEntry("name", "홍길동");
        assertThat(res.getAssembledData()).containsEntry("phone", "010-1111-2222");
        assertThat(res.getAssembledData()).containsEntry("email", "hong@crm.com");
        assertThat(res.getConfidenceScore()).isGreaterThan(0);
    }
}
