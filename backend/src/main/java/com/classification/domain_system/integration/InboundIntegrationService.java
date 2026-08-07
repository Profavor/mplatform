package com.classification.domain_system.integration;

import com.classification.domain_system.entity.ClassificationNode;
import com.classification.domain_system.entity.IntegrationChannel;
import com.classification.domain_system.entity.Record;
import com.classification.domain_system.entity.RecordHistory;
import com.classification.domain_system.repository.ClassificationNodeRepository;
import com.classification.domain_system.repository.IntegrationChannelRepository;
import com.classification.domain_system.repository.RecordHistoryRepository;
import com.classification.domain_system.repository.RecordRepository;
import com.classification.domain_system.service.MatchingService;
import com.classification.domain_system.service.NotificationService;
import com.classification.domain_system.service.dq.DqRuleEngine;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class InboundIntegrationService {

    private final IntegrationChannelRepository channelRepository;
    private final ClassificationNodeRepository nodeRepository;
    private final RecordRepository recordRepository;
    private final RecordHistoryRepository recordHistoryRepository;
    private final MatchingService matchingService;
    private final DqRuleEngine dqRuleEngine;
    private final DataMappingTransformer mappingTransformer;
    private final IntegrationLogService logService;
    private final com.classification.domain_system.service.ApprovalService approvalService;
    private final com.classification.domain_system.repository.SourcePriorityRepository sourcePriorityRepository;
    private final com.classification.domain_system.repository.RecordFieldSourceRepository recordFieldSourceRepository;
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public String processInboundData(UUID channelId, String rawPayload, String authHeader, String xApiKeyHeader, String apiKeyParam) {
        IntegrationChannel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new IllegalArgumentException("연계 채널을 찾을 수 없습니다. ID: " + channelId));

        if (!channel.isActive()) {
            throw new IllegalArgumentException("The integration channel is disabled. Channel: " + channel.getName());
        }

        if (!"INBOUND".equalsIgnoreCase(channel.getDirection())) {
            throw new IllegalArgumentException("Not an inbound integration channel. Direction: " + channel.getDirection());
        }

        // Inbound Authentication Check
        validateInboundAuthentication(channel, rawPayload, authHeader, xApiKeyHeader, apiKeyParam);

        try {
            String transformedPayload = mappingTransformer.transformPayload(rawPayload, channel.getMappingConfigJson());

            // 채널에 연계 노드(또는 도메인)가 설정된 경우 Record로 저장
            List<UUID> savedRecordIds = new ArrayList<>();
            ClassificationNode targetNode = null;
            if (channel.getNodeId() != null) {
                targetNode = nodeRepository.findById(channel.getNodeId()).orElse(null);
            }
            if (targetNode == null) {
                UUID domainId = extractDomainIdFromConfig(channel.getConfigJson());
                if (domainId != null) {
                    List<ClassificationNode> rootNodes = nodeRepository.findByDomain_IdAndParentIsNullAndIsDeletedFalseOrderByOrderAsc(domainId);
                    if (!rootNodes.isEmpty()) {
                        targetNode = rootNodes.get(0);
                        log.info("[Inbound] NodeId for Channel [{}] is empty. Automatically assigning Root Node [{}] of Domain [{}].", channel.getName(), targetNode.getId(), domainId);
                    }
                }
            }

            if (targetNode != null) {
                savedRecordIds = saveAsRecords(targetNode, transformedPayload, channel.getName(), channel);
            } else {
                throw new IllegalArgumentException("Target domain and classification node are not configured for the inbound integration channel. Please check the channel settings.");
            }

            UUID firstRecordId = savedRecordIds.isEmpty() ? null : savedRecordIds.get(0);
            logService.logSuccess(channelId, firstRecordId, "INBOUND_RECEIVE", rawPayload, transformedPayload);

            log.info("Inbound data processed successfully for channel [{}]: saved {} record(s)", channel.getName(), savedRecordIds.size());
            return transformedPayload;
        } catch (IllegalArgumentException e) {
            // payload 파싱 오류 등 클라이언트 측 문제 → 400으로 전파 (로그만 남기고 re-throw)
            String stackTrace = getStackTraceAsString(e);
            logService.logError(channelId, null, "INBOUND_RECEIVE", rawPayload, null, e.getMessage(), stackTrace, 1);
            log.warn("Inbound data rejected for channel [{}]: {}", channelId, e.getMessage());
            throw e; // Controller에서 400 BadRequest로 처리됨
        } catch (Exception e) {
            String stackTrace = getStackTraceAsString(e);
            logService.logError(channelId, null, "INBOUND_RECEIVE", rawPayload, null, e.getMessage(), stackTrace, 1);
            log.error("Failed to process inbound data for channel [{}]: {}", channelId, e.getMessage(), e);

            if (notificationService != null) {
                try {
                    notificationService.createNotification(
                            null,
                            "[Inbound 연계 오류] " + channel.getName(),
                            "인바운드 데이터 연계 처리 중 오류 발생: " + e.getMessage(),
                            "SYSTEM",
                            "/admin/integration/channels"
                    );
                } catch (Exception notificationEx) {
                    log.error("Failed to send integration error notification", notificationEx);
                }
            }

            throw new RuntimeException("An error occurred during inbound data processing: " + e.getMessage(), e);
        }
    }

    private String getStackTraceAsString(Throwable throwable) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : throwable.getStackTrace()) {
            sb.append(element.toString()).append("\n");
        }
        return sb.toString();
    }

    /**
     * 변환된 payload를 Record로 저장하고 RecordHistory를 함께 생성한다.
     * 중복 검사(MatchingService)를 거쳐 중복 데이터는 UPDATE, 신규 데이터는 INSERT하며, DQE 규칙 평가를 수행한다.
     */
    private List<UUID> saveAsRecords(ClassificationNode node, String transformedPayload, String sourceSystem, IntegrationChannel channel) {
        List<UUID> savedIds = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(transformedPayload);
            if (root.isArray()) {
                // 배열: 각 요소를 개별 Record로 Upsert 처리
                for (JsonNode item : root) {
                    String itemJson = objectMapper.writeValueAsString(item);
                    UUID id = saveOrUpdateSingleRecord(node, itemJson, sourceSystem, channel);
                    if (id != null) savedIds.add(id);
                }
            } else {
                // 단일 객체 Upsert 처리
                UUID id = saveOrUpdateSingleRecord(node, transformedPayload, sourceSystem, channel);
                if (id != null) savedIds.add(id);
            }
        } catch (Exception e) {
            log.error("[Inbound] Error saving Record/RecordHistory: {}", e.getMessage(), e);
        }
        return savedIds;
    }

    private UUID saveOrUpdateSingleRecord(ClassificationNode node, String itemJson, String sourceSystem, IntegrationChannel channel) {
        if (channel != null && channel.isRequiresApproval()) {
            com.classification.domain_system.dto.RecordRequest req = new com.classification.domain_system.dto.RecordRequest();
            req.setData(itemJson);
            
            MatchingService.DuplicateResult dupCheck = matchingService.checkDuplicates(node.getId(), itemJson);
            if (dupCheck.hasDuplicates && dupCheck.duplicateRecordIds != null && !dupCheck.duplicateRecordIds.isEmpty()) {
                UUID existingId = dupCheck.duplicateRecordIds.get(0);
                com.classification.domain_system.entity.ApprovalRequest appReq = approvalService.requestRecordUpdate(existingId, req);
                log.info("[Inbound Approval] Record update sent to approval queue (id: {}). ApprovalRequest ID: {}", existingId, appReq.getId());
                return existingId;
            } else {
                com.classification.domain_system.entity.ApprovalRequest appReq = approvalService.requestRecordCreation(node.getId(), req);
                log.info("[Inbound Approval] Record creation sent to approval queue. ApprovalRequest ID: {}", appReq.getId());
                return appReq.getId();
            }
        }
        MatchingService.DuplicateResult dupCheck = matchingService.checkDuplicates(node.getId(), itemJson);

        if (dupCheck.hasDuplicates && dupCheck.duplicateRecordIds != null && !dupCheck.duplicateRecordIds.isEmpty()) {
            // UPDATE: 기존 레코드 갱신
            UUID existingId = dupCheck.duplicateRecordIds.get(0);
            Record existingRecord = recordRepository.findById(existingId).orElse(null);
            if (existingRecord != null) {
                String prevData = existingRecord.getData();
                
                UUID domainId = node.getDomain().getId();
                List<com.classification.domain_system.entity.SourcePriority> priorities = sourcePriorityRepository.findByDomainIdOrderByPriorityAsc(domainId);

                String mergedDataJson = itemJson;
                if (priorities.isEmpty()) {
                    log.info("[Survivorship] Survivorship not configured, applying legacy full overwrite: domainId={}", domainId);
                } else {
                    try {
                        Map<String, Object> existingMap = objectMapper.readValue(prevData != null ? prevData : "{}", new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
                        Map<String, Object> incomingMap = objectMapper.readValue(itemJson, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
                        
                        int currentSystemPriority = priorities.stream()
                                .filter(p -> p.getSourceSystem().equalsIgnoreCase(sourceSystem))
                                .map(com.classification.domain_system.entity.SourcePriority::getPriority)
                                .findFirst()
                                .orElse(999);

                        Map<String, Object> finalMap = new HashMap<>(existingMap);

                        for (Map.Entry<String, Object> entry : incomingMap.entrySet()) {
                            String key = entry.getKey();
                            Object val = entry.getValue();
                            if (val == null || val.toString().isBlank()) {
                                continue;
                            }

                            com.classification.domain_system.entity.RecordFieldSource fieldSource = recordFieldSourceRepository
                                    .findByRecordIdAndFieldKey(existingId, key)
                                    .orElse(null);

                            int lastPriority = 999;
                            if (fieldSource != null) {
                                final String prevSys = fieldSource.getSourceSystem();
                                lastPriority = priorities.stream()
                                        .filter(p -> p.getSourceSystem().equalsIgnoreCase(prevSys))
                                        .map(com.classification.domain_system.entity.SourcePriority::getPriority)
                                        .findFirst()
                                        .orElse(999);
                            }

                            if (currentSystemPriority <= lastPriority) {
                                finalMap.put(key, val);
                                if (fieldSource == null) {
                                    fieldSource = new com.classification.domain_system.entity.RecordFieldSource();
                                    fieldSource.setRecordId(existingId);
                                    fieldSource.setFieldKey(key);
                                }
                                fieldSource.setSourceSystem(sourceSystem);
                                recordFieldSourceRepository.save(fieldSource);
                            }
                        }
                        mergedDataJson = objectMapper.writeValueAsString(finalMap);
                    } catch (Exception e) {
                        log.error("[Survivorship Error] Survivorship merge failed: {}", e.getMessage());
                    }
                }

                existingRecord.setData(mergedDataJson);
                existingRecord.setSourceSystem(sourceSystem);
                existingRecord.setUpdatedAt(LocalDateTime.now());
                Record saved = recordRepository.save(existingRecord);

                RecordHistory history = new RecordHistory();
                history.setRecordId(saved.getId());
                history.setChangeType("UPDATE");
                history.setChangedBy(null);
                history.setPreviousData(prevData);
                history.setNewData(itemJson);
                history.setApprovalRequestId(null);
                history.setVersion(saved.getVersion());
                history.setSourceSystem(sourceSystem);
                recordHistoryRepository.save(history);

                // DQE 평가 수행
                try {
                    dqRuleEngine.evaluate(node.getId(), itemJson, saved.getId());
                } catch (Exception e) {
                    log.error("[Inbound DQE] Error evaluating record [{}]: {}", saved.getId(), e.getMessage());
                }

                log.info("[Inbound] Existing record found, UPDATE processed: id={}, ver={}", saved.getId(), saved.getVersion());
                return saved.getId();
            }
        }

        // INSERT: 신규 레코드 생성
        Record record = new Record();
        record.setNode(node);
        record.setStatus("ACTIVE");
        record.setSourceSystem(sourceSystem);
        record.setData(itemJson);
        Record saved = recordRepository.save(record);

        RecordHistory history = new RecordHistory();
        history.setRecordId(saved.getId());
        history.setChangeType("CREATE");
        history.setChangedBy(null);
        history.setPreviousData(null);
        history.setNewData(itemJson);
        history.setApprovalRequestId(null);
        history.setVersion(1);
        history.setSourceSystem(sourceSystem);
        recordHistoryRepository.save(history);

        // DQE 평가 수행
        try {
            dqRuleEngine.evaluate(node.getId(), itemJson, saved.getId());
        } catch (Exception e) {
            log.error("[Inbound DQE] Error evaluating record [{}]: {}", saved.getId(), e.getMessage());
        }

        log.info("[Inbound] New record INSERT processed: id={}", saved.getId());
        return saved.getId();
    }


    private void validateInboundAuthentication(IntegrationChannel channel, String rawPayload, String authHeader, String xApiKeyHeader, String apiKeyParam) {
        String configJson = channel.getConfigJson();
        if (configJson == null || configJson.isBlank()) {
            return;
        }

        try {
            JsonNode config = objectMapper.readTree(configJson);
            String authType = config.has("authType") ? config.get("authType").asText() : "NONE";
            String secretToken = config.has("secretToken") ? config.get("secretToken").asText() : "";

            if ("NONE".equalsIgnoreCase(authType) || secretToken.isBlank()) {
                return; // 인증 미설정 채널
            }

            boolean isAuthenticated = false;

            if ("BEARER_TOKEN".equalsIgnoreCase(authType)) {
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    String extractedToken = authHeader.substring(7).trim();
                    if (secretToken.equals(extractedToken)) {
                        isAuthenticated = true;
                    }
                }
            } else if ("API_KEY".equalsIgnoreCase(authType)) {
                if (secretToken.equals(xApiKeyHeader) || secretToken.equals(apiKeyParam)) {
                    isAuthenticated = true;
                }
            }

            if (!isAuthenticated) {
                String errorMsg = "Inbound 인증 실패: 유효하지 않은 인증 토큰입니다. (AuthType: " + authType + ")";
                logService.logError(channel.getId(), null, "INBOUND_RECEIVE", rawPayload, null, errorMsg, "SecurityException", 1);
                throw new SecurityException(errorMsg);
            }
        } catch (SecurityException se) {
            throw se;
        } catch (Exception e) {
            log.error("Authentication check exception for channel [{}]: {}", channel.getName(), e.getMessage());
        }
    }

    private UUID extractDomainIdFromConfig(String configJson) {
        if (configJson == null || configJson.isBlank()) return null;
        try {
            JsonNode config = objectMapper.readTree(configJson);
            if (config.has("domainId") && !config.get("domainId").isNull() && !config.get("domainId").asText().isBlank()) {
                return UUID.fromString(config.get("domainId").asText());
            }
        } catch (Exception e) {
            log.error("[Inbound] Failed to extract domainId from configJson: {}", e.getMessage());
        }
        return null;
    }
}
