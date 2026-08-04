package com.classification.domain_system.service;

import com.classification.domain_system.dto.RecordLineageDto;
import com.classification.domain_system.entity.IntegrationLog;
import com.classification.domain_system.entity.Record;
import com.classification.domain_system.entity.RecordHistory;
import com.classification.domain_system.entity.User;
import com.classification.domain_system.exception.ResourceNotFoundException;
import com.classification.domain_system.repository.IntegrationLogRepository;
import com.classification.domain_system.repository.RecordHistoryRepository;
import com.classification.domain_system.repository.RecordRepository;
import com.classification.domain_system.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class RecordLineageService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final RecordRepository recordRepository;
    private final RecordHistoryRepository recordHistoryRepository;
    private final IntegrationLogRepository integrationLogRepository;
    private final UserRepository userRepository;

    public RecordLineageService(RecordRepository recordRepository,
                                RecordHistoryRepository recordHistoryRepository,
                                IntegrationLogRepository integrationLogRepository,
                                UserRepository userRepository) {
        this.recordRepository = recordRepository;
        this.recordHistoryRepository = recordHistoryRepository;
        this.integrationLogRepository = integrationLogRepository;
        this.userRepository = userRepository;
    }

    public RecordLineageDto.RecordLineageResponse getRecordLineage(UUID recordId) {
        Record record = recordRepository.findById(recordId)
                .orElseThrow(() -> new ResourceNotFoundException("Record not found: " + recordId));

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime recordCreatedAt = record.getCreatedAt() != null ? record.getCreatedAt() : now;
        String rawCode = "REC-" + record.getId().toString().substring(0, 8);
        String recordDisplayName = extractRecordDisplayName(record.getData());
        String displayCode = recordDisplayName != null ? recordDisplayName : rawCode;

        RecordLineageDto.RecordLineageResponse response = new RecordLineageDto.RecordLineageResponse(
                record.getId(), displayCode
        );

        parseRecordNameAndEmpNo(record.getData(), response);

        // 1. Source System Node (데이터 생성의 출발점)
        String sourceNodeId = "SRC-1";
        RecordLineageDto.LineageNode sourceNode = new RecordLineageDto.LineageNode(
                sourceNodeId,
                "Source System: " + (record.getSourceSystem() != null ? record.getSourceSystem() : "Master Data Portal"),
                "SOURCE",
                formatDateTime(recordCreatedAt)
        );
        response.getNodes().add(sourceNode);

        // 2. Record Revision History Nodes (생애주기 버전 이력: Version 1 -> Version 2 오름차순)
        List<RecordHistory> histories = recordHistoryRepository.findByRecordIdOrderByVersionAsc(recordId);
        String lastHistoryNodeId = sourceNodeId;

        for (RecordHistory history : histories) {
            String historyNodeId = "HIST-" + history.getId();
            LocalDateTime historyTime = history.getChangedAt() != null ? history.getChangedAt() : recordCreatedAt;
            RecordLineageDto.LineageNode histNode = new RecordLineageDto.LineageNode(
                    historyNodeId,
                    "Version " + history.getVersion() + " (" + history.getChangeType() + ")",
                    "RECORD_VERSION",
                    formatDateTime(historyTime)
            );

            String rawUser = history.getChangedBy();
            String resolvedUser = resolveUserName(rawUser);
            histNode.getDetails().put("changedBy", resolvedUser);
            histNode.getDetails().put("version", history.getVersion());
            if (history.getPreviousData() != null) {
                histNode.getDetails().put("previousData", history.getPreviousData());
            }
            if (history.getNewData() != null) {
                histNode.getDetails().put("newData", history.getNewData());
            }
            response.getNodes().add(histNode);
            response.getEdges().add(new RecordLineageDto.LineageEdge(lastHistoryNodeId, historyNodeId, "MODIFIED_TO"));
            lastHistoryNodeId = historyNodeId;
        }

        // 3. Root Record Node (최종 통합 관리 중인 Master Record)
        String recordNodeId = "REC-" + record.getId();
        RecordLineageDto.LineageNode rootNode = new RecordLineageDto.LineageNode(
                recordNodeId,
                "Golden Master Record (" + displayCode + ")",
                "RECORD",
                formatDateTime(record.getUpdatedAt() != null ? record.getUpdatedAt() : recordCreatedAt)
        );
        rootNode.getDetails().put("status", record.getStatus() != null ? record.getStatus() : "ACTIVE");
        response.getNodes().add(rootNode);
        response.getEdges().add(new RecordLineageDto.LineageEdge(lastHistoryNodeId, recordNodeId, "EVOLVED_TO"));

        // 4. Outbound Integration Nodes (외부 전파 파이프라인)
        List<IntegrationLog> integrationLogs = integrationLogRepository.findByRecordIdOrderByCreatedAtDesc(recordId);
        for (IntegrationLog log : integrationLogs) {
            String logNodeId = "OUT-" + log.getId();
            String channelName = log.getChannel() != null ? log.getChannel().getName() : "Outbound Channel";
            LocalDateTime logTime = log.getCreatedAt() != null ? log.getCreatedAt() : recordCreatedAt;
            RecordLineageDto.LineageNode outNode = new RecordLineageDto.LineageNode(
                    logNodeId,
                    "Outbound: " + channelName,
                    "OUTBOUND",
                    formatDateTime(logTime)
            );
            outNode.getDetails().put("status", log.getStatus() != null ? log.getStatus() : "SUCCESS");
            response.getNodes().add(outNode);
            response.getEdges().add(new RecordLineageDto.LineageEdge(recordNodeId, logNodeId, "DISPATCHED_TO"));
        }

        return response;
    }

    private String extractRecordDisplayName(String jsonContent) {
        if (jsonContent == null || jsonContent.isBlank()) return null;
        try {
            JsonNode root = objectMapper.readTree(jsonContent);
            String name = null;
            if (root.has("NAME")) {
                JsonNode nameNode = root.get("NAME");
                if (nameNode.isObject() && nameNode.has("ko")) {
                    name = nameNode.get("ko").asText();
                } else if (nameNode.isValueNode()) {
                    name = nameNode.asText();
                }
            }
            String empNo = root.has("EMP_NO") ? root.get("EMP_NO").asText() : null;

            if (name != null && empNo != null) {
                return name + " (" + empNo + ")";
            } else if (empNo != null) {
                return empNo;
            } else if (name != null) {
                return name;
            }

            if (root.has("TITLE")) return root.get("TITLE").asText();
            if (root.has("CODE")) return root.get("CODE").asText();
        } catch (Exception ignored) {}
        return null;
    }

    private void parseRecordNameAndEmpNo(String jsonContent, RecordLineageDto.RecordLineageResponse response) {
        if (jsonContent == null || jsonContent.isBlank()) return;
        try {
            JsonNode root = objectMapper.readTree(jsonContent);
            if (root.has("NAME")) {
                JsonNode nameNode = root.get("NAME");
                if (nameNode.isObject()) {
                    java.util.Map<String, String> nameMap = objectMapper.convertValue(nameNode, java.util.Map.class);
                    response.setRecordNameObj(nameMap);
                } else if (nameNode.isValueNode()) {
                    response.setRecordNameObj(nameNode.asText());
                }
            }
            if (root.has("EMP_NO")) {
                response.setEmpNo(root.get("EMP_NO").asText());
            }
        } catch (Exception ignored) {}
    }

    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return LocalDateTime.now().format(DATE_FORMATTER);
        }
        return dateTime.format(DATE_FORMATTER);
    }

    private String resolveUserName(String rawUser) {
        if (rawUser == null || rawUser.isBlank()) {
            return "System Admin";
        }
        Optional<User> byId = userRepository.findById(rawUser);
        if (byId.isPresent()) {
            return byId.get().getUsername();
        }
        Optional<User> byName = userRepository.findByUsername(rawUser);
        if (byName.isPresent()) {
            return byName.get().getUsername();
        }
        return rawUser.length() > 8 ? rawUser.substring(0, 8) + "..." : rawUser;
    }
}
