package com.classification.domain_system.service;

import com.classification.domain_system.dto.DataMaskingDto;
import com.classification.domain_system.entity.FieldDefinition;
import com.classification.domain_system.entity.Record;
import com.classification.domain_system.repository.RecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DataMaskingServiceTest {

    @Mock private RecordRepository recordRepository;
    @Mock private com.classification.domain_system.repository.FieldDefinitionRepository fieldDefinitionRepository;

    @InjectMocks
    private DataMaskingService dataMaskingService;

    private UUID recordId;
    private UUID nodeId;
    private Record record;
    private java.util.List<FieldDefinition> mockFields;

    @BeforeEach
    void setUp() {
        recordId = UUID.randomUUID();
        nodeId = UUID.randomUUID();
        record = new Record();
        record.setId(recordId);
        record.setData("{\"name\":\"홍길동\",\"phone\":\"010-1234-5678\",\"email\":\"hong@company.com\",\"residentNo\":\"900101-1234567\"}");

        com.classification.domain_system.entity.ClassificationNode node = new com.classification.domain_system.entity.ClassificationNode();
        node.setId(nodeId);

        FieldDefinition fPhone = new FieldDefinition();
        fPhone.setKey("phone");
        fPhone.setMaskingPattern("PHONE");

        FieldDefinition fEmail = new FieldDefinition();
        fEmail.setKey("email");
        fEmail.setMaskingPattern("EMAIL");

        FieldDefinition fRrn = new FieldDefinition();
        fRrn.setKey("residentNo");
        fRrn.setMaskingPattern("RRN");

        FieldDefinition fName = new FieldDefinition();
        fName.setKey("name");

        mockFields = java.util.List.of(fPhone, fEmail, fRrn, fName);
        record.setNode(node);
    }

    @Test
    @DisplayName("getMaskedRecord: 권한 없을 때 스키마 정의에 따른 동적 마스킹 처리 검증")
    void testMaskedRecordWhenNoPermission() {
        when(recordRepository.findById(recordId)).thenReturn(Optional.of(record));
        when(fieldDefinitionRepository.findNodeFieldsWithSort(nodeId)).thenReturn(mockFields);

        DataMaskingDto.MaskedDataResponse response = dataMaskingService.getMaskedRecord(recordId, false);

        assertThat(response).isNotNull();
        assertThat(response.isMasked()).isTrue();
        assertThat(response.getMaskedFieldCount()).isEqualTo(3);

        // Phone mask check
        assertThat(response.getMaskedData().get("phone")).isEqualTo("010-****-5678");

        // Email mask check
        assertThat(response.getMaskedData().get("email").toString()).contains("***@company.com");

        // Resident No mask check
        assertThat(response.getMaskedData().get("residentNo")).isEqualTo("900101-1******");

        // Unmasked normal field check
        assertThat(response.getMaskedData().get("name")).isEqualTo("홍길동");
    }

    @Test
    @DisplayName("getMaskedRecord: 해제 권한이 있을 때 원본 데이터 그대로 반환 검증")
    void testUnmaskedRecordWhenAuthorized() {
        when(recordRepository.findById(recordId)).thenReturn(Optional.of(record));

        DataMaskingDto.MaskedDataResponse response = dataMaskingService.getMaskedRecord(recordId, true);

        assertThat(response).isNotNull();
        assertThat(response.isMasked()).isFalse();
        assertThat(response.getMaskedData().get("phone")).isEqualTo("010-1234-5678");
        assertThat(response.getMaskedData().get("email")).isEqualTo("hong@company.com");
    }
}
