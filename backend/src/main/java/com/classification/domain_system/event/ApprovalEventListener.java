package com.classification.domain_system.event;

import com.classification.domain_system.entity.ApprovalRequest;
import com.classification.domain_system.entity.ApprovalStep;
import com.classification.domain_system.entity.Record;
import com.classification.domain_system.entity.FieldDefinition;
import com.classification.domain_system.entity.Domain;
import com.classification.domain_system.repository.*;
import com.classification.domain_system.service.CalculatedFieldEvaluator;
import com.classification.domain_system.service.FieldDefinitionService;
import com.classification.domain_system.service.RecordHistoryWriter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class ApprovalEventListener {

    private final ApprovalRequestRepository approvalRepository;
    private final ApprovalStepRepository stepRepository;
    private final RecordRepository recordRepository;
    private final RecordHistoryRepository recordHistoryRepository;
    private final FieldDefinitionService fieldDefinitionService;
    private final com.classification.domain_system.service.NumberingService numberingService;
    private final org.springframework.context.ApplicationEventPublisher applicationEventPublisher;
    private final CalculatedFieldEvaluator calculatedFieldEvaluator;
    private final RecordHistoryWriter recordHistoryWriter;
    private final com.classification.domain_system.service.NotificationService notificationService;
    private final com.classification.domain_system.repository.UserRepository userRepository;
    private final com.classification.domain_system.repository.DomainRepository domainRepository;
    private final com.classification.domain_system.websocket.WebSocketPublisher webSocketPublisher;
    private final com.classification.domain_system.repository.BatchJobRepository batchJobRepository;
    private final com.classification.domain_system.repository.StagingRecordRepository stagingRecordRepository;
    
    @org.springframework.beans.factory.annotation.Autowired
    @org.springframework.context.annotation.Lazy
    private com.classification.domain_system.service.RecordMergeService recordMergeService;
    
    @org.springframework.beans.factory.annotation.Autowired
    @org.springframework.context.annotation.Lazy
    private com.classification.domain_system.service.ClassificationNodeService classificationNodeService;

    @EventListener
    @Transactional
    public void onApprovalRequestCreated(ApprovalRequestCreatedEvent event) {
        ApprovalRequest approval = event.getApprovalRequest();
        log.info("[EVENT_DRIVEN] Received ApprovalRequestCreatedEvent for Request ID: {}", approval.getId());
        
        long realStepCount = approval.getSteps().stream().filter(s -> s.getStepOrder() > 0).count();
        if (realStepCount == 0) {
            approval.setStatus("APPROVED");
            approval.getSteps().stream().filter(s -> s.getStepOrder() == 0).forEach(s -> {
                s.setStatus("APPROVED");
                s.setComment("시스템 자동 승인 (결재선 미설정)");
                stepRepository.saveAndFlush(s);
            });
            approvalRepository.saveAndFlush(approval);
            applyFinalApproval(approval);
            return;
        }

        autoApproveStepsIfRequesterIsAssignee(approval);

        if (notificationService != null && approval.getSteps() != null && approval.getCurrentStepOrder() != null) {
            String actionLabel = resolveActionLabel(approval.getTargetType());
            String requesterName = resolveUserName(approval.getRequesterId());
            String domainName = resolveDomainName(approval);
            String classificationName = resolveClassificationName(approval);
            String summary = extractChangeSummary(approval);

            approval.getSteps().stream()
                    .filter(s -> s.getStepOrder().equals(approval.getCurrentStepOrder()))
                    .forEach(s -> sendPendingStepNotification(s, actionLabel, requesterName, domainName, classificationName, summary, approval.getId(), approval.getTargetType()));
        }

        broadcastApprovalStatusChange(approval, "CREATED");
    }

    @EventListener
    @Transactional
    public void onApprovalStepApproved(ApprovalStepApprovedEvent event) {
        ApprovalRequest requestFromEvent = event.getApprovalRequest();
        ApprovalStep approvedStep = event.getApprovedStep();
        log.info("[EVENT_DRIVEN] Received ApprovalStepApprovedEvent for Request ID: {}, Approved Step Order: {}", 
                requestFromEvent.getId(), approvedStep.getStepOrder());

        try {
            ApprovalRequest approval = approvalRepository.findByIdWithLock(requestFromEvent.getId())
                    .orElse(requestFromEvent);

            if (!"PENDING".equals(approval.getStatus())) {
                log.info("ApprovalRequest {} status is already {}, skipping advancement.", approval.getId(), approval.getStatus());
                return;
            }

            boolean allApproved = approval.getSteps().stream()
                    .filter(s -> s.getStepOrder().equals(approval.getCurrentStepOrder()))
                    .allMatch(s -> "APPROVED".equals(s.getStatus()));
                    
            if (allApproved) {
                Integer nextOrder = approval.getSteps().stream()
                        .map(ApprovalStep::getStepOrder)
                        .filter(order -> order > approval.getCurrentStepOrder())
                        .min(Integer::compareTo)
                        .orElse(null);
                        
                if (nextOrder != null) {
                    if (notificationService != null && approval.getRequesterId() != null) {
                        try {
                            String actionLabel = resolveActionLabel(approval.getTargetType());
                            String domainName = resolveDomainName(approval);
                            String classificationName = resolveClassificationName(approval);
                            notificationService.createNotification(
                                    approval.getRequesterId(),
                                    "@i18n:notifications.approval_step_approved",
                                    buildStepApprovedMessage(actionLabel, approvedStep.getStepOrder(), domainName, classificationName, approval.getTargetType()),
                                    "APPROVAL",
                                    "/approvals?requestId=" + approval.getId()
                            );
                        } catch (Exception ex) {
                            log.warn("Failed to create approval notification for requester {}", approval.getRequesterId(), ex);
                        }
                    }

                    approval.getSteps().stream()
                            .filter(s -> s.getStepOrder().equals(nextOrder))
                            .forEach(s -> s.setStatus("PENDING"));
                    approval.setCurrentStepOrder(nextOrder);
                    approvalRepository.saveAndFlush(approval);
                    
                    autoApproveStepsIfRequesterIsAssignee(approval);

                    if (notificationService != null && approval.getSteps() != null) {
                        String nextActionLabel = resolveActionLabel(approval.getTargetType());
                        String nextRequesterName = resolveUserName(approval.getRequesterId());
                        String nextDomainName = resolveDomainName(approval);
                        String nextClassificationName = resolveClassificationName(approval);
                        String nextSummary = extractChangeSummary(approval);

                        approval.getSteps().stream()
                                .filter(s -> s.getStepOrder().equals(approval.getCurrentStepOrder()))
                                .forEach(s -> sendPendingStepNotification(s, nextActionLabel, nextRequesterName, nextDomainName, nextClassificationName, nextSummary, approval.getId(), approval.getTargetType()));
                    }
                } else {
                    approval.setStatus("APPROVED");
                    approvalRepository.saveAndFlush(approval);
                    applyFinalApproval(approval);

                    if (notificationService != null && approval.getRequesterId() != null) {
                        try {
                            String finalActionLabel = resolveActionLabel(approval.getTargetType());
                            String finalDomainName = resolveDomainName(approval);
                            String finalClassificationName = resolveClassificationName(approval);
                            notificationService.createNotification(
                                    approval.getRequesterId(),
                                    "@i18n:notifications.approval_finalized",
                                    buildFinalizedMessage(finalActionLabel, finalDomainName, finalClassificationName, approval.getTargetType()),
                                    "APPROVAL",
                                    "/approvals?requestId=" + approval.getId()
                            );
                        } catch (Exception ex) {
                            log.warn("Failed to create final approval notification for requester {}", approval.getRequesterId(), ex);
                        }
                    }
                }
            }

            broadcastApprovalStatusChange(approval, "UPDATED");
        } catch (org.springframework.orm.ObjectOptimisticLockingFailureException | jakarta.persistence.OptimisticLockException e) {
            log.warn("Concurrent modification detected for ApprovalRequest ID: {}. Another thread already advanced or finalized this request.", requestFromEvent.getId());
        }
    }

    void autoApproveStepsIfRequesterIsAssignee(ApprovalRequest approval) {
        if (approval == null || approval.getCurrentStepOrder() == null || approval.getSteps() == null) {
            return;
        }
        boolean progress = true;
        while (progress) {
            progress = false;
            final int currentOrder = approval.getCurrentStepOrder();
            List<ApprovalStep> pendingAutoSteps = approval.getSteps().stream()
                    .filter(s -> s.getStepOrder() != null
                            && s.getStepOrder().equals(currentOrder) 
                            && "PENDING".equals(s.getStatus()) 
                            && s.getAssigneeId() != null
                            && s.getAssigneeId().equals(approval.getRequesterId()))
                    .toList();

            
            if (!pendingAutoSteps.isEmpty()) {
                for (ApprovalStep step : pendingAutoSteps) {
                    step.setStatus("APPROVED");
                    step.setComment("시스템 자동 승인 (기안자 자동 전결)");
                    stepRepository.saveAndFlush(step);
                }
                
                boolean allApproved = approval.getSteps().stream()
                        .filter(s -> s.getStepOrder().equals(currentOrder))
                        .allMatch(s -> "APPROVED".equals(s.getStatus()));
                
                if (allApproved) {
                    Integer nextOrder = approval.getSteps().stream()
                            .map(ApprovalStep::getStepOrder)
                            .filter(order -> order > currentOrder)
                            .min(Integer::compareTo)
                            .orElse(null);
                    
                    if (nextOrder != null) {
                        approval.getSteps().stream()
                                .filter(s -> s.getStepOrder().equals(nextOrder))
                                .forEach(s -> s.setStatus("PENDING"));
                        approval.setCurrentStepOrder(nextOrder);
                        approvalRepository.saveAndFlush(approval);
                        progress = true;
                    } else {
                        approval.setStatus("APPROVED");
                        approvalRepository.saveAndFlush(approval);
                        applyFinalApproval(approval);
                    }
                }
            }
        }
    }

    private void applyFinalApproval(ApprovalRequest approval) {
        if ("RECORD".equals(approval.getTargetType())) {
            Record record = recordRepository.findById(approval.getTargetId())
                    .orElseThrow(() -> new RuntimeException("Record not found"));
            record.setStatus("ACTIVE");
            
            String changesJson = approval.getChanges();
            Domain domain = record.getNode().getDomain();
            if (domain != null && domain.getNumberingPattern() != null && !domain.getNumberingPattern().isBlank()) {
                String issuedCode = numberingService.issueNextCode(domain.getId());
                if (domain.getIdentifierFieldId() != null && !issuedCode.isBlank()) {
                    String targetKey = domain.getIdentifierFieldId().toString();
                    List<FieldDefinition> fields = fieldDefinitionService.getEffectiveFields(record.getNode().getId());
                    for (FieldDefinition f : fields) {
                        if (f.getId() != null && f.getId().toString().equals(domain.getIdentifierFieldId().toString())) {
                            if (f.getKey() != null) {
                                targetKey = f.getKey();
                            }
                            break;
                        }
                    }
                    changesJson = injectIdentifierValue(changesJson, targetKey, issuedCode);
                }
            }

            String finalData = recomputeCalculatedFields(record.getNode().getId(), changesJson);
            record.setData(finalData);
            recordRepository.save(record);
            logHistory(record, "CREATE", approval.getRequesterId(), null, finalData, approval.getId());
            applicationEventPublisher.publishEvent(new MasterDataChangedEvent(this, record.getId(), record.getNode().getId(), "CREATE", finalData));
        } else if ("RECORD_UPDATE".equals(approval.getTargetType())) {
            Record record = recordRepository.findById(approval.getTargetId())
                    .orElseThrow(() -> new RuntimeException("Record not found"));
            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(approval.getChanges());
                String prevData = record.getData();
                if (root.has("after")) {
                    String afterData = recomputeCalculatedFields(record.getNode().getId(), root.get("after").toString());
                    record.setData(afterData);
                }
                record.setStatus("ACTIVE");
                recordRepository.save(record);
                logHistory(record, "UPDATE", approval.getRequesterId(), prevData, record.getData(), approval.getId());
                applicationEventPublisher.publishEvent(new MasterDataChangedEvent(this, record.getId(), record.getNode().getId(), "UPDATE", record.getData()));
            } catch (Exception e) {
                log.error("Error applying final approval for RECORD_UPDATE", e);
            }
        } else if ("RECORD_DELETE".equals(approval.getTargetType())) {
            Record record = recordRepository.findById(approval.getTargetId())
                    .orElseThrow(() -> new RuntimeException("Record not found"));
            String deletedData = record.getData();
            logHistory(record, "DELETE", approval.getRequesterId(), deletedData, null, approval.getId());
            recordRepository.delete(record);
            applicationEventPublisher.publishEvent(new MasterDataChangedEvent(this, record.getId(), record.getNode().getId(), "DELETE", deletedData));
        } else if ("RECORD_MERGE".equals(approval.getTargetType())) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                com.classification.domain_system.service.RecordMergeService.MergeRequest request = mapper.readValue(approval.getChanges(), com.classification.domain_system.service.RecordMergeService.MergeRequest.class);
                if (recordMergeService != null) {
                    recordMergeService.mergeRecords(request, approval.getRequesterId());
                }
            } catch (Exception e) {
                log.error("Error applying final approval for RECORD_MERGE", e);
            }
        } else if (approval.getTargetType() != null && approval.getTargetType().startsWith("SCHEMA_")) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode changes = mapper.readTree(approval.getChanges());
                if ("SCHEMA_FIELD_ADD".equals(approval.getTargetType())) {
                    com.classification.domain_system.dto.FieldDefinitionRequest request = mapper.treeToValue(changes.get("request"), com.classification.domain_system.dto.FieldDefinitionRequest.class);
                    if (changes.has("nodeId")) {
                        fieldDefinitionService.addFieldDirect(UUID.fromString(changes.get("nodeId").asText()), request);
                    } else if (changes.has("domainId")) {
                        fieldDefinitionService.addDomainFieldDirect(UUID.fromString(changes.get("domainId").asText()), request);
                    }
                } else if ("SCHEMA_FIELD_UPDATE".equals(approval.getTargetType())) {
                    com.classification.domain_system.dto.FieldDefinitionRequest request = mapper.treeToValue(changes.get("request"), com.classification.domain_system.dto.FieldDefinitionRequest.class);
                    UUID fieldId = UUID.fromString(changes.get("fieldId").asText());
                    if (changes.has("nodeId")) {
                        fieldDefinitionService.updateFieldDirect(UUID.fromString(changes.get("nodeId").asText()), fieldId, request);
                    } else if (changes.has("domainId")) {
                        fieldDefinitionService.updateDomainFieldDirect(UUID.fromString(changes.get("domainId").asText()), fieldId, request);
                    }
                } else if ("SCHEMA_FIELD_DELETE".equals(approval.getTargetType())) {
                    UUID fieldId = UUID.fromString(changes.get("fieldId").asText());
                    UUID domainId = changes.has("domainId") ? UUID.fromString(changes.get("domainId").asText()) : null;
                    String requester = resolveRequesterName(approval);
                    fieldDefinitionService.deleteDomainFieldDirect(domainId, fieldId, requester);
                } else if ("SCHEMA_NODE_CREATE".equals(approval.getTargetType())) {
                    com.classification.domain_system.dto.ClassificationNodeRequest request = mapper.treeToValue(changes.get("request"), com.classification.domain_system.dto.ClassificationNodeRequest.class);
                    classificationNodeService.createNodeDirect(UUID.fromString(changes.get("domainId").asText()), request);
                } else if ("SCHEMA_NODE_MOVE".equals(approval.getTargetType()) || "SCHEMA_NODE_UPDATE".equals(approval.getTargetType())) {
                    com.classification.domain_system.dto.ClassificationNodeRequest request = mapper.treeToValue(changes.get("request"), com.classification.domain_system.dto.ClassificationNodeRequest.class);
                    classificationNodeService.updateNodeDirect(UUID.fromString(changes.get("domainId").asText()), UUID.fromString(changes.get("nodeId").asText()), request);
                }
            } catch (Exception e) {
                log.error("Error applying final approval for schema change", e);
            }
        } else if ("BATCH_RECORD".equals(approval.getTargetType())) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode changesNode = mapper.readTree(approval.getChanges());
                List<UUID> recordIds = new ArrayList<>();
                if (changesNode.has("recordIds") && changesNode.get("recordIds").isArray()) {
                    for (JsonNode id : changesNode.get("recordIds")) {
                        recordIds.add(UUID.fromString(id.asText()));
                    }
                }
                
                List<Record> records = recordRepository.findAllById(recordIds);
                for (Record record : records) {
                    record.setStatus("ACTIVE");
                }
                recordRepository.saveAll(records);
                
                UUID batchId = UUID.fromString(changesNode.get("batchId").asText());
                batchJobRepository.findById(batchId).ifPresent(job -> {
                    job.setStatus("COMMITTED");
                    batchJobRepository.save(job);
                });
                
                List<com.classification.domain_system.entity.StagingRecord> stagings = stagingRecordRepository.findByBatchId(batchId);
                for (com.classification.domain_system.entity.StagingRecord sr : stagings) {
                    if ("PENDING_APPROVAL".equals(sr.getStatus())) {
                        sr.setStatus("COMMITTED");
                    }
                }
                stagingRecordRepository.saveAll(stagings);
            } catch (Exception e) {
                log.error("Error applying final approval for BATCH_RECORD", e);
            }
        }
    }

    private void logHistory(Record record, String changeType, String changedBy, String prevData, String newData, UUID approvalRequestId) {
        recordHistoryWriter.logHistory(record, changeType, changedBy, prevData, newData, approvalRequestId);
    }

    private String recomputeCalculatedFields(UUID nodeId, String dataJson) {
        return calculatedFieldEvaluator.recomputeCalculatedFields(nodeId, dataJson);
    }

    private String injectIdentifierValue(String dataJson, String fieldKey, String value) {
        if (dataJson == null || dataJson.isBlank() || fieldKey == null) return dataJson;
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> data = mapper.readValue(dataJson, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
            data.put(fieldKey, value);
            return mapper.writeValueAsString(data);
        } catch (Exception e) {
            log.error("Failed to inject identifier value into data JSON", e);
            return dataJson;
        }
    }

    private String resolveActionLabel(String targetType) {
        if (targetType == null) return "결재";
        switch (targetType) {
            case "RECORD": return "신규 등록";
            case "RECORD_UPDATE": return "정보 변경";
            case "RECORD_DELETE": return "삭제/폐기";
            case "RECORD_MERGE": return "골든레코드 병합";
            default:
                if (targetType.startsWith("SCHEMA_")) return "스키마 변경";
                return "결재";
        }
    }

    private String resolveUserName(String userId) {
        if (userId == null) return "사용자";
        return userRepository.findById(userId)
                .map(com.classification.domain_system.entity.User::getUsername)
                .orElse("사용자");
    }

    private String resolveDomainName(ApprovalRequest approval) {
        if (approval.getClassificationNode() != null && approval.getClassificationNode().getDomain() != null) {
            Map<String, String> nameMap = approval.getClassificationNode().getDomain().getName();
            if (nameMap != null && nameMap.containsKey("ko")) return nameMap.get("ko");
            if (nameMap != null && !nameMap.isEmpty()) return nameMap.values().iterator().next();
        }
        if (approval.getChanges() != null && !approval.getChanges().isBlank()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(approval.getChanges());
                if (root.has("domainName") && root.get("domainName").isValueNode()) {
                    return root.get("domainName").asText();
                }
                if (root.has("domainId") && root.get("domainId").isValueNode()) {
                    UUID domainId = UUID.fromString(root.get("domainId").asText());
                    var dOpt = domainRepository.findById(domainId);
                    if (dOpt.isPresent() && dOpt.get().getName() != null) {
                        Map<String, String> dNameMap = dOpt.get().getName();
                        if (dNameMap.containsKey("ko")) return dNameMap.get("ko");
                        if (!dNameMap.isEmpty()) return dNameMap.values().iterator().next();
                    }
                }
            } catch (Exception ignored) {}
        }
        return "도메인";
    }

    private String resolveClassificationName(ApprovalRequest approval) {
        if (approval.getClassificationNode() != null) {
            Map<String, String> nameMap = approval.getClassificationNode().getName();
            if (nameMap != null && nameMap.containsKey("ko")) return nameMap.get("ko");
            if (nameMap != null && !nameMap.isEmpty()) return nameMap.values().iterator().next();
        }
        if (approval.getChanges() != null && !approval.getChanges().isBlank()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(approval.getChanges());
                if (root.has("classificationName") && root.get("classificationName").isValueNode()) {
                    return root.get("classificationName").asText();
                }
                if (root.has("nodeName") && root.get("nodeName").isValueNode()) {
                    return root.get("nodeName").asText();
                }
            } catch (Exception ignored) {}
        }
        return "분류";
    }

    private String formatFieldKey(String key) {
        if (key == null) return "";
        return switch (key) {
            case "key" -> "key";
            case "type" -> "type";
            case "name" -> "name";
            case "code" -> "code";
            case "title" -> "title";
            case "description" -> "description";
            case "dataType" -> "dataType";
            case "required" -> "required";
            default -> key;
        };
    }

    private String extractChangeSummary(ApprovalRequest approval) {
        if (approval.getChanges() == null || approval.getChanges().isBlank()) return "";
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(approval.getChanges());
            List<String> summaryParts = new ArrayList<>();

            // 0. Include domainName if available
            if (approval.getTargetType() != null && approval.getTargetType().startsWith("SCHEMA_")) {
                String domainName = resolveDomainName(approval);
                if (domainName != null && !domainName.isBlank() && !"도메인".equals(domainName)) {
                    summaryParts.add("도메인: " + domainName);
                }
            }

            // Explicitly handle fieldName and fieldKey for schema changes
            if (root.has("fieldName")) {
                JsonNode fNameNode = root.get("fieldName");
                String fNameStr = fNameNode.isObject() 
                    ? (fNameNode.has("ko") ? fNameNode.get("ko").asText() : (fNameNode.elements().hasNext() ? fNameNode.elements().next().asText() : fNameNode.asText()))
                    : fNameNode.asText();
                if (!fNameStr.isBlank()) {
                    summaryParts.add("필드명: " + fNameStr);
                }
            }
            if (root.has("fieldKey") && !root.get("fieldKey").asText().isBlank()) {
                summaryParts.add("필드키: " + root.get("fieldKey").asText());
            }

            // If DELETE action with before object, extract field info if not present
            if ("SCHEMA_FIELD_DELETE".equals(approval.getTargetType()) && root.has("before")) {
                JsonNode beforeNode = root.get("before");
                if (!root.has("fieldName") && beforeNode.has("name")) {
                    JsonNode nVal = beforeNode.get("name");
                    String fNameStr = nVal.isObject() 
                        ? (nVal.has("ko") ? nVal.get("ko").asText() : (nVal.elements().hasNext() ? nVal.elements().next().asText() : nVal.asText()))
                        : nVal.asText();
                    if (!fNameStr.isBlank()) summaryParts.add("필드명: " + fNameStr);
                }
                if (!root.has("fieldKey") && beforeNode.has("key")) {
                    summaryParts.add("필드키: " + beforeNode.get("key").asText());
                }
            }

            // Ignore raw technical IDs
            List<String> ignoreKeys = List.of("id", "fieldId", "version", "fieldGroupId", "domainId", "nodeId", "requesterId", "domainName", "classificationName", "fieldName", "fieldKey");

            // 1. Record updates (before & after comparison)
            if (root.has("before") && root.has("after")) {
                JsonNode beforeNode = root.get("before");
                JsonNode afterNode = root.get("after");
                afterNode.fieldNames().forEachRemaining(key -> {
                    if (summaryParts.size() < 4 && !key.startsWith("_") && !ignoreKeys.contains(key)) {
                        String bVal = beforeNode.has(key) && beforeNode.get(key).isValueNode() ? beforeNode.get(key).asText() : "";
                        String aVal = afterNode.has(key) && afterNode.get(key).isValueNode() ? afterNode.get(key).asText() : "";
                        if (!bVal.equals(aVal)) {
                            summaryParts.add(formatFieldKey(key) + ": " + (bVal.isBlank() ? "N/A" : bVal) + " ➔ " + (aVal.isBlank() ? "N/A" : aVal));
                        }
                    }
                });
            } else if (summaryParts.size() <= 1) {
                // 2. Record creation or general schema change request
                JsonNode dataNode = root;
                if (dataNode.has("after")) dataNode = dataNode.get("after");
                if (dataNode.has("request")) dataNode = dataNode.get("request");
                
                final JsonNode targetNode = dataNode;
                List<String> priorityKeys = List.of("name", "key", "code", "title", "type", "empNo", "userName");
                for (String pKey : priorityKeys) {
                    if (targetNode.has(pKey) && targetNode.get(pKey).isValueNode()) {
                        summaryParts.add(formatFieldKey(pKey) + ": " + targetNode.get(pKey).asText());
                    }
                }
            }
            return String.join(", ", summaryParts);
        } catch (Exception e) {
            return "";
        }
    }

    private String resolveSchemaActionLabel(String targetType) {
        if (targetType == null) return "스키마 변경";
        return switch (targetType) {
            case "SCHEMA_FIELD_DELETE" -> "스키마 필드 삭제";
            case "SCHEMA_FIELD_ADD" -> "스키마 필드 추가";
            case "SCHEMA_FIELD_UPDATE" -> "스키마 필드 변경";
            case "SCHEMA_NODE_CREATE" -> "분류 노드 생성";
            case "SCHEMA_NODE_UPDATE" -> "분류 노드 변경";
            case "SCHEMA_NODE_MOVE" -> "분류 노드 이동";
            default -> "스키마 변경";
        };
    }

    private String resolveRequesterName(ApprovalRequest approval) {
        if (approval == null) return null;
        if (approval.getRequesterId() != null && !approval.getRequesterId().isBlank()) {
            return approval.getRequesterId();
        }
        if (approval.getRequesterName() != null && !approval.getRequesterName().isBlank()) {
            return approval.getRequesterName();
        }
        return null;
    }

    private String buildNotificationMessage(String actionLabel, String requesterName, String domainName, String classificationName, String summary, String targetType) {
        StringBuilder sb = new StringBuilder();
        if (targetType != null && targetType.startsWith("SCHEMA_")) {
            sb.append("[").append(resolveSchemaActionLabel(targetType)).append("] ");
        } else {
            sb.append("[").append(domainName).append(" > ").append(classificationName).append("] ");
        }
        sb.append(requesterName).append(" | ").append(resolveSchemaActionLabel(targetType));
        if (!summary.isBlank()) {
            sb.append(" (").append(summary).append(")");
        }
        return sb.toString();
    }

    private String buildStepApprovedMessage(String actionLabel, Integer stepOrder, String domainName, String classificationName, String targetType) {
        String tag = (targetType != null && targetType.startsWith("SCHEMA_")) ? "[스키마 변경]" : "[" + domainName + " > " + classificationName + "]";
        return tag + " " + actionLabel + " Step " + stepOrder + " Approved";
    }

    private String buildFinalizedMessage(String actionLabel, String domainName, String classificationName, String targetType) {
        String tag = (targetType != null && targetType.startsWith("SCHEMA_")) ? "[스키마 변경]" : "[" + domainName + " > " + classificationName + "]";
        return tag + " " + actionLabel + " Final Approved";
    }

    private void sendPendingStepNotification(ApprovalStep step, String actionLabel, String requesterName, String domainName, String classificationName, String summary, UUID approvalId, String targetType) {
        if (step == null || notificationService == null) return;
        List<String> targetUserIds = new ArrayList<>();
        if (step.getAssigneeId() != null && !step.getAssigneeId().isBlank()) {
            targetUserIds.add(step.getAssigneeId());
        }
        if (step.getAssigneeRole() != null && !step.getAssigneeRole().isBlank()) {
            List<com.classification.domain_system.entity.User> roleUsers = userRepository.findAll().stream()
                    .filter(u -> step.getAssigneeRole().equalsIgnoreCase(u.getRole()))
                    .toList();
            for (com.classification.domain_system.entity.User u : roleUsers) {
                if (!targetUserIds.contains(u.getId())) {
                    targetUserIds.add(u.getId());
                }
            }
        }
        for (String targetUserId : targetUserIds) {
            try {
                notificationService.createNotification(
                        targetUserId,
                        "@i18n:notifications.approval_pending",
                        buildNotificationMessage(actionLabel, requesterName, domainName, classificationName, summary, targetType),
                        "APPROVAL",
                        "/approvals?requestId=" + approvalId
                );
            } catch (Exception ex) {
                log.warn("Failed to create approval notification for assignee {}", targetUserId, ex);
            }
        }
    }

    private void broadcastApprovalStatusChange(ApprovalRequest approval, String eventType) {
        if (approval == null || webSocketPublisher == null) return;
        java.util.Map<String, Object> payload = java.util.Map.of(
                "eventType", eventType,
                "approvalId", approval.getId(),
                "status", approval.getStatus() != null ? approval.getStatus() : "",
                "currentStepOrder", approval.getCurrentStepOrder() != null ? approval.getCurrentStepOrder() : 0,
                "targetType", approval.getTargetType() != null ? approval.getTargetType() : "",
                "targetId", approval.getTargetId() != null ? approval.getTargetId() : ""
        );
        webSocketPublisher.publishApprovalEvent("/topic/approvals/" + approval.getId(), payload);
        webSocketPublisher.publishApprovalEvent("/topic/approvals/status-changes", payload);
    }
}
