package com.classification.domain_system.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ClassificationAxisDeserializationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("JSON name이 String이고 code 필드가 들어와도 ClassificationAxisRequest로 정상 역직렬화된다")
    void deserialize_StringNameAndCode_Success() throws Exception {
        String json = """
                {
                    "code": "PLANT",
                    "name": "플랜트",
                    "description": "Plant",
                    "sortOrder": 1
                }
                """;

        ClassificationAxisRequest req = objectMapper.readValue(json, ClassificationAxisRequest.class);

        assertThat(req.getAxisCode()).isEqualTo("PLANT");
        assertThat(req.getCode()).isEqualTo("PLANT");
        assertThat(req.getName()).isNotNull();
        assertThat(req.getName().get("ko")).isEqualTo("플랜트");
        assertThat(req.getDescription()).isEqualTo("Plant");
        assertThat(req.getSortOrder()).isEqualTo(1);
    }

    @Test
    @DisplayName("JSON name이 Map이고 axisCode 필드가 들어올 때도 ClassificationAxisRequest로 정상 역직렬화된다")
    void deserialize_MapNameAndAxisCode_Success() throws Exception {
        String json = """
                {
                    "axisCode": "PLANT",
                    "name": {
                        "ko": "플랜트",
                        "en": "Plant"
                    },
                    "description": "Plant",
                    "sortOrder": 1
                }
                """;

        ClassificationAxisRequest req = objectMapper.readValue(json, ClassificationAxisRequest.class);

        assertThat(req.getAxisCode()).isEqualTo("PLANT");
        assertThat(req.getCode()).isEqualTo("PLANT");
        assertThat(req.getName()).isNotNull();
        assertThat(req.getName().get("ko")).isEqualTo("플랜트");
        assertThat(req.getName().get("en")).isEqualTo("Plant");
        assertThat(req.getDescription()).isEqualTo("Plant");
        assertThat(req.getSortOrder()).isEqualTo(1);
    }

    @Test
    @DisplayName("ClassificationAxisResponse 직렬화 시 axisCode와 code가 모두 JSON에 포함된다")
    void serialize_Response_ContainsCodeAndAxisCode() throws Exception {
        ClassificationAxisResponse res = ClassificationAxisResponse.builder()
                .axisCode("PLANT")
                .name(Map.of("ko", "플랜트"))
                .build();

        String json = objectMapper.writeValueAsString(res);

        assertThat(json).contains("\"axisCode\":\"PLANT\"");
        assertThat(json).contains("\"code\":\"PLANT\"");
    }
}
