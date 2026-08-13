package com.classification.domain_system.service;

import com.classification.domain_system.dto.IntegrationChannelResponse;
import com.classification.domain_system.entity.IntegrationChannel;
import com.classification.domain_system.repository.IntegrationChannelRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class IntegrationChannelService {

    public static final String MASKED_PLACEHOLDER = "********";

    private final IntegrationChannelRepository repository;
    private final FieldEncryptionService encryptionService;
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
            existing.setName(updated.getName());
            existing.setType(updated.getType());
            if (updated.getDirection() != null && !updated.getDirection().isBlank()) {
                existing.setDirection(updated.getDirection());
            }
            existing.setNodeId(updated.getNodeId());
            existing.setMappingConfigJson(updated.getMappingConfigJson());
            existing.setActive(updated.isActive());
            
            existing.setConfigJson(encryptConfigJson(updated.getConfigJson(), existing.getConfigJson()));
            
            IntegrationChannel saved = repository.save(existing);
            return toMaskedResponse(saved);
        });
    }

    public boolean deleteChannel(UUID id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        }
        return false;
    }

    private String encryptConfigJson(String newConfigJson, String oldConfigJson) {
        if (newConfigJson == null || newConfigJson.isBlank()) {
            return newConfigJson;
        }
        try {
            JsonNode rootNode = objectMapper.readTree(newConfigJson);
            if (rootNode.isObject()) {
                ObjectNode objNode = (ObjectNode) rootNode;
                if (objNode.has("password")) {
                    String password = objNode.get("password").asText();
                    if (MASKED_PLACEHOLDER.equals(password)) {
                        // Restore old password
                        if (oldConfigJson != null && !oldConfigJson.isBlank()) {
                            JsonNode oldNode = objectMapper.readTree(oldConfigJson);
                            if (oldNode.has("password")) {
                                objNode.put("password", oldNode.get("password").asText());
                            } else {
                                objNode.remove("password");
                            }
                        } else {
                            objNode.remove("password");
                        }
                    } else if (!encryptionService.isEncrypted(password)) {
                        objNode.put("password", encryptionService.encrypt(password));
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
                if (objNode.has("password")) {
                    objNode.put("password", MASKED_PLACEHOLDER);
                }
                return objectMapper.writeValueAsString(objNode);
            }
        } catch (Exception e) {
            log.warn("Failed to process configJson for masking", e);
        }
        return configJson;
    }
}
