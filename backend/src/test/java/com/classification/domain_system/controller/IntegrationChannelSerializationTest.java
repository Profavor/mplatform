package com.classification.domain_system.controller;

import com.classification.domain_system.dto.IntegrationChannelResponse;
import com.classification.domain_system.entity.IntegrationChannel;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class IntegrationChannelSerializationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("IntegrationChannelResponse 직렬화 시 active와 isActive 필드가 모두 포함되어야 한다")
    void responseContainsBothActiveAndIsActive() throws Exception {
        IntegrationChannel channel = new IntegrationChannel();
        channel.setId(UUID.randomUUID());
        channel.setName("{\"ko\":\"테스트 채널\",\"en\":\"Test Channel\"}");
        channel.setType("WEB_SERVICE");
        channel.setDirection("OUTBOUND");
        channel.setActive(true);

        IntegrationChannelResponse response = IntegrationChannelResponse.fromEntity(channel, null);
        String json = objectMapper.writeValueAsString(response);
        JsonNode root = objectMapper.readTree(json);

        assertTrue(root.has("active"), "JSON에 'active' 필드가 포함되어야 합니다.");
        assertTrue(root.has("isActive"), "JSON에 'isActive' 필드가 포함되어야 합니다.");
        assertTrue(root.get("active").asBoolean(), "'active' 값은 true여야 합니다.");
        assertTrue(root.get("isActive").asBoolean(), "'isActive' 값은 true여야 합니다.");
    }

    @Test
    @DisplayName("IntegrationChannel 역직렬화 시 'active': true 또는 'isActive': true 모두 올바르게 매핑되어야 한다")
    void channelDeserializesActiveOrIsActive() throws Exception {
        String jsonWithActive = "{\"name\":\"Test\",\"type\":\"WEB_SERVICE\",\"active\":true}";
        IntegrationChannel channel1 = objectMapper.readValue(jsonWithActive, IntegrationChannel.class);
        assertTrue(channel1.isActive(), "'active': true가 isActive 프로퍼티로 매핑되어야 합니다.");

        String jsonWithIsActive = "{\"name\":\"Test\",\"type\":\"WEB_SERVICE\",\"isActive\":true}";
        IntegrationChannel channel2 = objectMapper.readValue(jsonWithIsActive, IntegrationChannel.class);
        assertTrue(channel2.isActive(), "'isActive': true가 isActive 프로퍼티로 매핑되어야 합니다.");

        String jsonInactive = "{\"name\":\"Test\",\"type\":\"WEB_SERVICE\",\"active\":false}";
        IntegrationChannel channel3 = objectMapper.readValue(jsonInactive, IntegrationChannel.class);
        assertFalse(channel3.isActive(), "'active': false가 isActive 프로퍼티로 매핑되어야 합니다.");
    }
}
