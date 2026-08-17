package com.classification.domain_system.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class RecordServiceChangedKeysTest {

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private FieldEncryptionService fieldEncryptionService;

    @Mock
    private DataMaskingService dataMaskingService;

    @Mock
    private FieldDefinitionService fieldDefinitionService;

    @InjectMocks
    private RecordService recordService;

    @Test
    @DisplayName("computeChangedFieldKeys - 암호화된 필드가 서로 다른 암호문일 때 정확히 변경된 필드로 감지한다")
    void testComputeChangedFieldKeys_EncryptedFields() {
        String prevJson = "{\"name\":\"홍길동\",\"resident_number\":\"vault:v1:AAAA\",\"_idx_resident_number\":\"1111\"}";
        String newJson = "{\"name\":\"홍길동\",\"resident_number\":\"vault:v1:BBBB\",\"_idx_resident_number\":\"2222\"}";

        List<String> changed = recordService.computeChangedFieldKeys(prevJson, newJson);

        assertThat(changed).containsExactly("resident_number");
    }

    @Test
    @DisplayName("computeChangedFieldKeys - 일반 필드 변경 및 메타데이터/Blind Index 제외 검증")
    void testComputeChangedFieldKeys_ExcludeMetadata() {
        String prevJson = "{\"id\":\"123\",\"name\":\"홍길동\",\"dept\":\"인사팀\",\"status\":\"ACTIVE\"}";
        String newJson = "{\"id\":\"123\",\"name\":\"홍길동\",\"dept\":\"개발팀\",\"status\":\"ACTIVE\"}";

        List<String> changed = recordService.computeChangedFieldKeys(prevJson, newJson);

        assertThat(changed).containsExactly("dept");
    }
}
