package com.classification.domain_system.service;

import com.classification.domain_system.dto.ReferenceIntegrityDto;
import com.classification.domain_system.entity.FieldDefinition;
import com.classification.domain_system.entity.Record;
import com.classification.domain_system.repository.FieldDefinitionRepository;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ReferenceIntegrityServiceTest {

    @Mock private RecordRepository recordRepository;
    @Mock private FieldDefinitionRepository fieldDefinitionRepository;

    @InjectMocks
    private ReferenceIntegrityService referenceIntegrityService;

    private UUID domainId;
    private FieldDefinition refField;
    private Record sourceRecord;

    @BeforeEach
    void setUp() {
        domainId = UUID.randomUUID();

        refField = new FieldDefinition();
        refField.setKey("parentRecordId");
        refField.setType("RECORD_REF");

        sourceRecord = new Record();
        sourceRecord.setId(UUID.randomUUID());
    }

    @Test
    @DisplayName("scanDomainIntegrity: 미존재 대상 참조(고아 데이터) 정상 탐지")
    void testScanOrphanReference() {
        UUID nonExistentTargetId = UUID.randomUUID();
        sourceRecord.setData("{\"parentRecordId\": \"" + nonExistentTargetId + "\"}");

        when(fieldDefinitionRepository.findDomainFieldsWithSort(domainId)).thenReturn(List.of(refField));
        when(recordRepository.findAllByDomainId(domainId)).thenReturn(List.of(sourceRecord));
        when(recordRepository.findById(nonExistentTargetId)).thenReturn(Optional.empty());

        ReferenceIntegrityDto.IntegrityReportResponse report = referenceIntegrityService.scanDomainIntegrity(domainId);

        assertThat(report).isNotNull();
        assertThat(report.getOrphanCount()).isEqualTo(1);
        assertThat(report.getIntegrityScore()).isEqualTo(90);
        assertThat(report.getViolations()).hasSize(1);
        assertThat(report.getViolations().get(0).getIssueType()).isEqualTo("TARGET_NOT_FOUND");
    }

    @Test
    @DisplayName("scanDomainIntegrity: 정상 참조 시 100점 만점 반환")
    void testScanHealthyReference() {
        UUID validTargetId = UUID.randomUUID();
        sourceRecord.setData("{\"parentRecordId\": \"" + validTargetId + "\"}");

        Record validTarget = new Record();
        validTarget.setId(validTargetId);
        validTarget.setStatus("ACTIVE");

        when(fieldDefinitionRepository.findDomainFieldsWithSort(domainId)).thenReturn(List.of(refField));
        when(recordRepository.findAllByDomainId(domainId)).thenReturn(List.of(sourceRecord));
        when(recordRepository.findById(validTargetId)).thenReturn(Optional.of(validTarget));

        ReferenceIntegrityDto.IntegrityReportResponse report = referenceIntegrityService.scanDomainIntegrity(domainId);

        assertThat(report.getOrphanCount()).isEqualTo(0);
        assertThat(report.getIntegrityScore()).isEqualTo(100);
    }
}
