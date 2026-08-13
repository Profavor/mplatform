package com.classification.domain_system.service;

import com.classification.domain_system.dto.IntegrationChannelResponse;
import com.classification.domain_system.entity.IntegrationChannel;
import com.classification.domain_system.integration.JdbcDynamicExecutionService;
import com.classification.domain_system.repository.IntegrationChannelRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Lazy;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class IntegrationChannelService {

    public static final String MASKED_PLACEHOLDER = "********";
    private static final java.util.Set<String> SENSITIVE_KEYS = java.util.Set.of("password", "secretToken");

    private final IntegrationChannelRepository repository;
    private final FieldEncryptionService encryptionService;
    private final JdbcDynamicExecutionService jdbcService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<IntegrationChannelResponse> getAllChannels() {
        return repository.findAll().stream()
                .map(this::toMaskedResponse)
                .collect(Collectors.toList());
    }

    public Optional<IntegrationChannelResponse> getChannelById(UUID id) {
        return repository.findById(id).map(this::toMaskedResponse);
    }

    public IntegrationChannelResponse createChannel(IntegrationChannel channel) {
        if (channel.getDirection() == null || channel.getDirection().isBlank()) {
            channel.setDirection("OUTBOUND");
        }
        
        channel.setConfigJson(encryptConfigJson(channel.getConfigJson(), null));
        IntegrationChannel saved = repository.save(channel);
        return toMaskedResponse(saved);
    }

    public Optional<IntegrationChannelResponse> updateChannel(UUID id, IntegrationChannel updated) {
        return repository.findById(id).map(existing -> {
            String oldConfig = existing.getConfigJson();
            
            existing.setName(updated.getName());
            existing.setType(updated.getType());
            if (updated.getDirection() != null && !updated.getDirection().isBlank()) {
                existing.setDirection(updated.getDirection());
            }
            existing.setNodeId(updated.getNodeId());
            existing.setMappingConfigJson(updated.getMappingConfigJson());
            existing.setActive(updated.isActive());
            
            existing.setConfigJson(encryptConfigJson(updated.getConfigJson(), oldConfig));
            
            IntegrationChannel saved = repository.save(existing);
            
            // Invalidate cache if credentials or active status changed
            invalidateCacheIfNeeded(oldConfig);
            
            return toMaskedResponse(saved);
        });
    }

    public boolean deleteChannel(UUID id) {
        return repository.findById(id).map(existing -> {
            String oldConfig = existing.getConfigJson();
            repository.deleteById(id);
            invalidateCacheIfNeeded(oldConfig);
            return true;
        }).orElse(false);
    }

    private void invalidateCacheIfNeeded(String configJson) {
        if (configJson == null || configJson.isBlank()) return;
        try {
            JsonNode rootNode = objectMapper.readTree(configJson);
            if (rootNode.has("url") && rootNode.has("user") && rootNode.has("password")) {
                String url = rootNode.get("url").asText();
                String user = rootNode.get("user").asText();
                String password = rootNode.get("password").asText();
                if (encryptionService.isEncrypted(password)) {
                    password = encryptionService.decrypt(password);
                }
                jdbcService.invalidateDataSource(url, user, password);
            }
        } catch (Exception e) {
            log.warn("Failed to invalidate cache", e);
        }
    }

    private String encryptConfigJson(String newConfigJson, String oldConfigJson) {
        if (newConfigJson == null || newConfigJson.isBlank()) {
            return newConfigJson;
        }
        try {
            JsonNode rootNode = objectMapper.readTree(newConfigJson);
            if (rootNode.isObject()) {
                ObjectNode objNode = (ObjectNode) rootNode;
                for (String sensitiveKey : SENSITIVE_KEYS) {
                    if (objNode.has(sensitiveKey)) {
                        String value = objNode.get(sensitiveKey).asText();
                        if (MASKED_PLACEHOLDER.equals(value)) {
                            // Restore old value
                            if (oldConfigJson != null && !oldConfigJson.isBlank()) {
                                JsonNode oldNode = objectMapper.readTree(oldConfigJson);
                                if (oldNode.has(sensitiveKey)) {
                                    objNode.put(sensitiveKey, oldNode.get(sensitiveKey).asText());
                                } else {
                                    objNode.remove(sensitiveKey);
                                }
                            } else {
                                objNode.remove(sensitiveKey);
                            }
                        } else if (!encryptionService.isEncrypted(value)) {
                            objNode.put(sensitiveKey, encryptionService.encrypt(value));
                        }
                    }
                }
                return objectMapper.writeValueAsString(objNode);
            }
        } catch (Exception e) {
            log.warn("Failed to process configJson for encryption", e);
        }
        return newConfigJson;
    }

    private IntegrationChannelResponse toMaskedResponse(IntegrationChannel channel) {
        String maskedConfig = maskConfigJson(channel.getConfigJson());
        return IntegrationChannelResponse.fromEntity(channel, maskedConfig);
    }

    private String maskConfigJson(String configJson) {
        if (configJson == null || configJson.isBlank()) {
            return configJson;
        }
        try {
            JsonNode rootNode = objectMapper.readTree(configJson);
            if (rootNode.isObject()) {
                ObjectNode objNode = (ObjectNode) rootNode;
                for (String sensitiveKey : SENSITIVE_KEYS) {
                    if (objNode.has(sensitiveKey)) {
                        objNode.put(sensitiveKey, MASKED_PLACEHOLDER);
                    }
                }
                return objectMapper.writeValueAsString(objNode);
            }
        } catch (Exception e) {
            log.warn("Failed to process configJson for masking", e);
        }
        return configJson;
    }
}
