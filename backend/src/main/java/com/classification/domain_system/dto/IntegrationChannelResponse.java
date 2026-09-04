package com.classification.domain_system.dto;

import com.classification.domain_system.entity.IntegrationChannel;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class IntegrationChannelResponse {
    private UUID id;
    private String name;
    private String type;
    private String direction;
    private UUID nodeId;
    private String configJson;
    private String mappingConfigJson;
    @com.fasterxml.jackson.annotation.JsonProperty("active")
    private boolean isActive;

    @com.fasterxml.jackson.annotation.JsonProperty("isActive")
    public boolean getIsActive() {
        return this.isActive;
    }
    private boolean requiresApproval;
    private int maxRetries;
    private long retryBackoffMs;
    private boolean useExponentialBackoff;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static IntegrationChannelResponse fromEntity(IntegrationChannel channel, String maskedConfigJson) {
        IntegrationChannelResponse dto = new IntegrationChannelResponse();
        dto.setId(channel.getId());
        dto.setName(channel.getName());
        dto.setType(channel.getType());
        dto.setDirection(channel.getDirection());
        dto.setNodeId(channel.getNodeId());
        dto.setConfigJson(maskedConfigJson != null ? maskedConfigJson : channel.getConfigJson());
        dto.setMappingConfigJson(channel.getMappingConfigJson());
        dto.setActive(channel.isActive());
        dto.setRequiresApproval(channel.isRequiresApproval());
        dto.setMaxRetries(channel.getMaxRetries());
        dto.setRetryBackoffMs(channel.getRetryBackoffMs());
        dto.setUseExponentialBackoff(channel.isUseExponentialBackoff());
        dto.setCreatedAt(channel.getCreatedAt());
        dto.setUpdatedAt(channel.getUpdatedAt());
        return dto;
    }
}
