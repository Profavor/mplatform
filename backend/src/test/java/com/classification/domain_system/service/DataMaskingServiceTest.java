package com.classification.domain_system.service;

import com.classification.domain_system.dto.DataMaskingDto;
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

    @InjectMocks
    private DataMaskingService dataMaskingService;

    private UUID recordId;
    private Record record;

    @BeforeEach
    void setUp() {
        recordId = UUID.randomUUID();
        record = new Record();
        record.setId(recordId);
        record.setData("{\"name\":\"홍길동\",\"phone\":\"010-1234-5678\",\"email\":\"hong@company.com\",\"residentNo\":\"900101-1234567\"}");
    }

    @Test
    @DisplayName("getMaskedRecord: 권한 없을 때 PII 민감정보 동적 마스킹 처리 검증")
    void testMaskedRecordWhenNoPermission() {
        when(recordRepository.findById(recordId)).thenReturn(Optional.of(record));

        DataMaskingDto.MaskedDataResponse response = dataMaskingService.getMaskedRecord(recordId, false);

        assertThat(response).isNotNull();
        assertThat(response.isMasked()).isTrue();
        assertThat(response.getMaskedFieldCount()).isGreaterThanOrEqualTo(3);

        // Phone mask check
        assertThat(response.getMaskedData().get("phone")).isEqualTo("010-****-5678");

        // Email mask check
        assertThat(response.getMaskedData().get("email").toString()).contains("***@company.com");

        // Resident No mask check
        assertThat(response.getMaskedData().get("residentNo")).isEqualTo("900101-1******");
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
