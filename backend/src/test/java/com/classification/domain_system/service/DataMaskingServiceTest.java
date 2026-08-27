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
    @Mock private FieldEncryptionService fieldEncryptionService;

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

    @Test
    @DisplayName("maskJsonData: 암호화 필드 조회 시 decrypt()를 호출하지 않고 _mask_ 캐시 값을 반환해야 한다")
    void testMaskJsonDataUsesMaskCacheWithoutDecryption() throws Exception {
        FieldDefinition fEnc = new FieldDefinition();
        fEnc.setKey("resident_number");
        fEnc.setIsEncrypted(true);
        fEnc.setMaskingPattern("RRN");

        String dataJson = "{\"resident_number\":\"vault:v1:k8s928374982374\",\"_mask_resident_number\":\"900101-1******\"}";

        String resultJson = dataMaskingService.maskJsonData(dataJson, java.util.List.of(fEnc), false);
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        java.util.Map<String, Object> map = mapper.readValue(resultJson, java.util.Map.class);

        assertThat(map.get("resident_number")).isEqualTo("900101-1******");
        org.mockito.Mockito.verify(fieldEncryptionService, org.mockito.Mockito.never()).decrypt(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    @DisplayName("maskJsonData: canUnmask가 true이더라도 암호화 필드를 자동 복호화하지 않고 마스킹 값을 반환해야 한다")
    void testMaskJsonDataDoesNotAutoDecryptEvenWhenCanUnmaskIsTrue() throws Exception {
        FieldDefinition fEnc = new FieldDefinition();
        fEnc.setKey("resident_number");
        fEnc.setIsEncrypted(true);
        fEnc.setMaskingPattern("RRN");

        String dataJson = "{\"resident_number\":\"vault:v1:k8s928374982374\",\"_mask_resident_number\":\"900101-1******\"}";

        // canUnmask = true로 조회하더라도 자동 복호화 금지!
        String resultJson = dataMaskingService.maskJsonData(dataJson, java.util.List.of(fEnc), true);
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        java.util.Map<String, Object> map = mapper.readValue(resultJson, java.util.Map.class);

        assertThat(map.get("resident_number")).isEqualTo("900101-1******");
        org.mockito.Mockito.verify(fieldEncryptionService, org.mockito.Mockito.never()).decrypt(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    @DisplayName("maskJsonData: _mask_ 캐시가 없는 기존 데이터의 경우 복호화 없이 ******** 고정 마스킹을 반환해야 한다")
    void testMaskJsonDataFallsBackToStaticMaskWhenNoCache() throws Exception {
        FieldDefinition fEnc = new FieldDefinition();
        fEnc.setKey("credit_card");
        fEnc.setIsEncrypted(true);

        String dataJson = "{\"credit_card\":\"vault:v1:legacyCiphertext\"}";

        String resultJson = dataMaskingService.maskJsonData(dataJson, java.util.List.of(fEnc), false);
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        java.util.Map<String, Object> map = mapper.readValue(resultJson, java.util.Map.class);

        assertThat(map.get("credit_card")).isEqualTo("********");
        org.mockito.Mockito.verify(fieldEncryptionService, org.mockito.Mockito.never()).decrypt(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    @DisplayName("maskJsonData: 대소문자 다른 필드 정의가 중복 존재할 때 isEncrypted=true가 우선 적용되어 마스킹되는지 검증")
    void testMaskJsonDataEncryptedPriorityCaseInsensitive() {
        FieldDefinition upperEnc = new FieldDefinition();
        upperEnc.setKey("TAX_INVOICE_EMAIL");
        upperEnc.setIsEncrypted(true);
        upperEnc.setMaskingPattern("EMAIL");

        FieldDefinition lowerPlain = new FieldDefinition();
        lowerPlain.setKey("tax_invoice_email");
        lowerPlain.setIsEncrypted(false);

        // 노드/도메인 상속으로 인해 소문자(미암호화)가 리스트 뒤에 오더라도 암호화 플래그가 보존되어야 함
        java.util.List<FieldDefinition> fields = java.util.List.of(upperEnc, lowerPlain);

        String json = "{\"tax_invoice_email\":\"money@mplatform.com\",\"_mask_tax_invoice_email\":\"m***@mplatform.com\"}";
        String result = dataMaskingService.maskJsonData(json, fields, false);

        assertThat(result).contains("m***@mplatform.com");
        assertThat(result).doesNotContain("money@mplatform.com");
    }
}

