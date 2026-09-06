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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class RecordLineageService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final RecordRepository recordRepository;
    private final RecordHistoryRepository recordHistoryRepository;
    private final IntegrationLogRepository integrationLogRepository;
    private final UserRepository userRepository;
    private final RecordService recordService;
    private final com.classification.domain_system.repository.DomainRepository domainRepository;
    private final com.classification.domain_system.repository.ClassificationNodeRepository nodeRepository;
    private final com.classification.domain_system.repository.IntegrationChannelRepository channelRepository;

    public RecordLineageService(RecordRepository recordRepository,
                                RecordHistoryRepository recordHistoryRepository,
                                IntegrationLogRepository integrationLogRepository,
                                UserRepository userRepository,
                                RecordService recordService,
                                com.classification.domain_system.repository.DomainRepository domainRepository,
                                com.classification.domain_system.repository.ClassificationNodeRepository nodeRepository,
                                com.classification.domain_system.repository.IntegrationChannelRepository channelRepository) {
        this.recordRepository = recordRepository;
        this.recordHistoryRepository = recordHistoryRepository;
        this.integrationLogRepository = integrationLogRepository;
        this.userRepository = userRepository;
        this.recordService = recordService;
        this.domainRepository = domainRepository;
        this.nodeRepository = nodeRepository;
        this.channelRepository = channelRepository;
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

        // All channels in system
        List<com.classification.domain_system.entity.IntegrationChannel> allChannels = channelRepository.findAll();

        // -------------------------------------------------------------
        // STAGE 1 & 2: Source System & Inbound Ingestion Pipeline
        // -------------------------------------------------------------
        String sourceSysName = record.getSourceSystem() != null && !record.getSourceSystem().isBlank()
                ? record.getSourceSystem() : "Master Data Portal";

        com.classification.domain_system.entity.IntegrationChannel matchedInboundChannel = allChannels.stream()
                .filter(c -> "INBOUND".equalsIgnoreCase(c.getDirection()))
                .filter(c -> c.getName().equalsIgnoreCase(sourceSysName) || (c.getChannelCode() != null && c.getChannelCode().equalsIgnoreCase(sourceSysName)))
                .findFirst()
                .orElse(null);

        // Stage 1: Source System Node
        String sourceNodeId = "SRC-1";
        RecordLineageDto.LineageNode sourceNode = new RecordLineageDto.LineageNode(
                sourceNodeId,
                "Source System: " + sourceSysName,
                "SOURCE",
                "SOURCE",
                formatDateTime(recordCreatedAt)
        );
        sourceNode.setHealthStatus("HEALTHY");
        if (matchedInboundChannel != null) {
            sourceNode.getDetails().put("channelCode", matchedInboundChannel.getChannelCode());
            sourceNode.getDetails().put("channelType", matchedInboundChannel.getType());
        }
        response.getNodes().add(sourceNode);

        // Stage 2: Inbound Ingestion Pipeline Node
        String inboundNodeId = "INB-1";
        String inboundLabel = "Inbound Pipeline: " + (matchedInboundChannel != null ? matchedInboundChannel.getName() : "Data Ingestion Adapter");
        RecordLineageDto.LineageNode inboundNode = new RecordLineageDto.LineageNode(
                inboundNodeId,
                inboundLabel,
                "INBOUND",
                "INBOUND_PIPELINE",
                formatDateTime(recordCreatedAt)
        );
        inboundNode.setHealthStatus("HEALTHY");
        if (matchedInboundChannel != null && matchedInboundChannel.getMappingConfigJson() != null) {
            inboundNode.setMappingRules(parseMappingRules(matchedInboundChannel.getMappingConfigJson()));
        }
        response.getNodes().add(inboundNode);
        response.getEdges().add(new RecordLineageDto.LineageEdge(sourceNodeId, inboundNodeId, "INGESTED_VIA"));

        // -------------------------------------------------------------
        // STAGE 3: Master Record (Histories & Golden Master Record)
        // -------------------------------------------------------------
        List<RecordHistory> histories = recordHistoryRepository.findByRecordIdOrderByVersionAsc(recordId);
        String lastHistoryNodeId = inboundNodeId;

        for (RecordHistory history : histories) {
            String historyNodeId = "HIST-" + history.getId();
            LocalDateTime historyTime = history.getChangedAt() != null ? history.getChangedAt() : recordCreatedAt;
            RecordLineageDto.LineageNode histNode = new RecordLineageDto.LineageNode(
                    historyNodeId,
                    "Version " + history.getVersion() + " (" + history.getChangeType() + ")",
                    "RECORD_VERSION",
                    "MASTER_RECORD",
                    formatDateTime(historyTime)
            );
            histNode.setHealthStatus("HEALTHY");

            String rawUser = history.getChangedBy();
            String resolvedUser = resolveUserName(rawUser);
            histNode.getDetails().put("changedBy", resolvedUser);
            histNode.getDetails().put("version", history.getVersion());

            UUID nodeId = (history.getRecord() != null && history.getRecord().getNode() != null)
                    ? history.getRecord().getNode().getId()
                    : (record.getNode() != null ? record.getNode().getId() : null);

            List<String> changedFields = recordService.computeChangedFieldKeys(history.getPreviousData(), history.getNewData());
            histNode.getDetails().put("changedFields", changedFields);

            if (history.getPreviousData() != null) {
                if (nodeId != null) {
                    histNode.getDetails().put("previousData", recordService.processDataForRead(nodeId, history.getPreviousData()));
                } else {
                    histNode.getDetails().put("previousData", history.getPreviousData());
                }
            }
            if (history.getNewData() != null) {
                if (nodeId != null) {
                    histNode.getDetails().put("newData", recordService.processDataForRead(nodeId, history.getNewData()));
                } else {
                    histNode.getDetails().put("newData", history.getNewData());
                }
            }
            response.getNodes().add(histNode);
            response.getEdges().add(new RecordLineageDto.LineageEdge(lastHistoryNodeId, historyNodeId, "MODIFIED_TO"));
            lastHistoryNodeId = historyNodeId;
        }

        // Golden Master Record Node
        String recordNodeId = "REC-" + record.getId();
        RecordLineageDto.LineageNode rootNode = new RecordLineageDto.LineageNode(
                recordNodeId,
                "Golden Master Record (" + displayCode + ")",
                "RECORD",
                "MASTER_RECORD",
                formatDateTime(record.getUpdatedAt() != null ? record.getUpdatedAt() : recordCreatedAt)
        );
        rootNode.getDetails().put("status", record.getStatus() != null ? record.getStatus() : "ACTIVE");
        rootNode.getDetails().put("version", record.getVersion() != null ? record.getVersion() : 1);
        rootNode.setHealthStatus("ACTIVE".equalsIgnoreCase(record.getStatus()) ? "HEALTHY" : "WARNING");
        if (!"ACTIVE".equalsIgnoreCase(record.getStatus())) {
            rootNode.setAnomalyReason("레코드 상태: " + (record.getStatus() != null ? record.getStatus() : "비활성"));
        }
        response.getNodes().add(rootNode);
        response.getEdges().add(new RecordLineageDto.LineageEdge(lastHistoryNodeId, recordNodeId, "EVOLVED_TO"));

        // -------------------------------------------------------------
        // STAGE 4 & 5: Outbound Pipelines & Downstream Consumers
        // -------------------------------------------------------------
        List<IntegrationLog> integrationLogs = integrationLogRepository.findByRecordIdOrderByCreatedAtDesc(recordId);

        // Group integration logs by channel
        java.util.Map<UUID, List<IntegrationLog>> logsByChannel = new java.util.LinkedHashMap<>();
        for (IntegrationLog log : integrationLogs) {
            if (log.getChannel() != null) {
                logsByChannel.computeIfAbsent(log.getChannel().getId(), k -> new ArrayList<>()).add(log);
            }
        }

        // Also identify outbound channels that match domain or are available
        List<com.classification.domain_system.entity.IntegrationChannel> outboundChannels = allChannels.stream()
                .filter(c -> "OUTBOUND".equalsIgnoreCase(c.getDirection()))
                .filter(c -> c.getNodeId() == null || (record.getNode() != null && c.getNodeId().equals(record.getNode().getId())))
                .collect(java.util.stream.Collectors.toList());

        // Merge channels with logs and configured outbound channels
        java.util.Set<UUID> processedChannelIds = new java.util.HashSet<>();
        List<java.util.Map<String, Object>> consumptionSummaryList = new ArrayList<>();

        for (com.classification.domain_system.entity.IntegrationChannel ch : outboundChannels) {
            processedChannelIds.add(ch.getId());
            processOutboundAndConsumerNode(recordNodeId, ch, logsByChannel.get(ch.getId()), response, consumptionSummaryList);
        }

        // Check any other channels that logged for this record
        for (Map.Entry<UUID, List<IntegrationLog>> entry : logsByChannel.entrySet()) {
            if (!processedChannelIds.contains(entry.getKey()) && !entry.getValue().isEmpty()) {
                com.classification.domain_system.entity.IntegrationChannel ch = entry.getValue().get(0).getChannel();
                if (ch != null) {
                    processOutboundAndConsumerNode(recordNodeId, ch, entry.getValue(), response, consumptionSummaryList);
                }
            }
        }

        response.setChannelConsumption(consumptionSummaryList);

        // -------------------------------------------------------------
        // Pipeline Stages Summary Computation
        // -------------------------------------------------------------
        computePipelineStages(response);

        return response;
    }

    private void processOutboundAndConsumerNode(
            String recordNodeId,
            com.classification.domain_system.entity.IntegrationChannel channel,
            List<IntegrationLog> logs,
            RecordLineageDto.RecordLineageResponse response,
            List<java.util.Map<String, Object>> consumptionSummaryList) {

        String outNodeId = "OUT-" + channel.getId();
        String cnsNodeId = "CNS-" + channel.getId();
        String channelName = channel.getName() != null ? channel.getName() : "Outbound Channel";
        String channelCode = channel.getChannelCode() != null ? channel.getChannelCode() : channel.getId().toString().substring(0, 8);

        RecordLineageDto.LineageNode outNode = new RecordLineageDto.LineageNode(
                outNodeId,
                "Outbound: " + channelName,
                "OUTBOUND",
                "OUTBOUND_PIPELINE",
                formatDateTime(channel.getCreatedAt())
        );

        RecordLineageDto.LineageNode cnsNode = new RecordLineageDto.LineageNode(
                cnsNodeId,
                "Consumer: " + channelName,
                "CONSUMER",
                "DOWNSTREAM_CONSUMER",
                formatDateTime(channel.getCreatedAt())
        );

        if (channel.getMappingConfigJson() != null) {
            outNode.setMappingRules(parseMappingRules(channel.getMappingConfigJson()));
        }

        // Metrics & Health calculation
        long totalDispatched = logs != null ? logs.size() : 0;
        long successCount = logs != null ? logs.stream().filter(l -> "SUCCESS".equalsIgnoreCase(l.getStatus())).count() : 0;
        long failCount = logs != null ? logs.stream().filter(l -> "FAIL".equalsIgnoreCase(l.getStatus()) || "DEAD_LETTER".equalsIgnoreCase(l.getStatus())).count() : 0;
        IntegrationLog lastLog = (logs != null && !logs.isEmpty()) ? logs.get(0) : null;

        java.util.Map<String, Object> metrics = new java.util.HashMap<>();
        metrics.put("totalDispatched", totalDispatched);
        metrics.put("successCount", successCount);
        metrics.put("failCount", failCount);
        if (lastLog != null) {
            metrics.put("lastDispatchedAt", formatDateTime(lastLog.getCreatedAt()));
            metrics.put("lastStatus", lastLog.getStatus());
            outNode.setTimestamp(formatDateTime(lastLog.getCreatedAt()));
            cnsNode.setTimestamp(formatDateTime(lastLog.getCreatedAt()));
        }

        outNode.setMetrics(metrics);
        cnsNode.setMetrics(metrics);

        // Anomaly / Health check
        if (lastLog != null && ("FAIL".equalsIgnoreCase(lastLog.getStatus()) || "DEAD_LETTER".equalsIgnoreCase(lastLog.getStatus()))) {
            outNode.setHealthStatus("ERROR");
            cnsNode.setHealthStatus("ERROR");
            String err = lastLog.getErrorMessage() != null ? lastLog.getErrorMessage() : "전송 실패 (오류 발생)";
            outNode.setAnomalyReason("최근 연동 실패: " + err);
            cnsNode.setAnomalyReason("소비 실패: " + err);
        } else if (lastLog != null && lastLog.getRetryCount() > 0) {
            outNode.setHealthStatus("WARNING");
            cnsNode.setHealthStatus("WARNING");
            outNode.setAnomalyReason("전송 지연 (재시도 " + lastLog.getRetryCount() + "회 발생)");
            cnsNode.setAnomalyReason("소비 지연 (재시도 발생)");
        } else if (totalDispatched == 0) {
            outNode.setHealthStatus("WARNING");
            cnsNode.setHealthStatus("WARNING");
            outNode.setAnomalyReason("아직 전파된 연동 이력이 없습니다.");
            cnsNode.setAnomalyReason("소비 대기 중");
        } else {
            outNode.setHealthStatus("HEALTHY");
            cnsNode.setHealthStatus("HEALTHY");
        }

        response.getNodes().add(outNode);
        response.getNodes().add(cnsNode);

        response.getEdges().add(new RecordLineageDto.LineageEdge(recordNodeId, outNodeId, "DISPATCHED_TO"));
        response.getEdges().add(new RecordLineageDto.LineageEdge(outNodeId, cnsNodeId, "CONSUMED_BY"));

        // Add to channel consumption summary
        java.util.Map<String, Object> summary = new java.util.HashMap<>();
        summary.put("channelId", channel.getId());
        summary.put("channelName", channelName);
        summary.put("channelCode", channelCode);
        summary.put("type", channel.getType());
        summary.put("direction", channel.getDirection());
        summary.put("totalDispatched", totalDispatched);
        summary.put("successCount", successCount);
        summary.put("failCount", failCount);
        summary.put("healthStatus", outNode.getHealthStatus());
        summary.put("anomalyReason", outNode.getAnomalyReason());
        summary.put("lastDispatchedAt", lastLog != null ? formatDateTime(lastLog.getCreatedAt()) : "-");
        consumptionSummaryList.add(summary);
    }

    private void computePipelineStages(RecordLineageDto.RecordLineageResponse response) {
        String[] stageKeys = {"SOURCE", "INBOUND_PIPELINE", "MASTER_RECORD", "OUTBOUND_PIPELINE", "DOWNSTREAM_CONSUMER"};
        String[] stageLabels = {"원천 시스템", "수집 파이프라인", "마스터 레코드", "전파 파이프라인", "다운스트림 소비자"};

        List<RecordLineageDto.PipelineStageSummary> stages = new ArrayList<>();
        for (int i = 0; i < stageKeys.length; i++) {
            String key = stageKeys[i];
            String label = stageLabels[i];
            List<RecordLineageDto.LineageNode> stageNodes = response.getNodes().stream()
                    .filter(n -> key.equalsIgnoreCase(n.getStage()))
                    .collect(java.util.stream.Collectors.toList());

            String overallStatus = "HEALTHY";
            if (stageNodes.stream().anyMatch(n -> "ERROR".equalsIgnoreCase(n.getHealthStatus()))) {
                overallStatus = "ERROR";
            } else if (stageNodes.stream().anyMatch(n -> "WARNING".equalsIgnoreCase(n.getHealthStatus()))) {
                overallStatus = "WARNING";
            }

            stages.add(new RecordLineageDto.PipelineStageSummary(key, label, i + 1, stageNodes.size(), overallStatus));
        }
        response.setStages(stages);
    }

    private List<RecordLineageDto.MappingRuleSummary> parseMappingRules(String mappingConfigJson) {
        List<RecordLineageDto.MappingRuleSummary> rules = new ArrayList<>();
        if (mappingConfigJson == null || mappingConfigJson.isBlank()) {
            return rules;
        }
        try {
            JsonNode root = objectMapper.readTree(mappingConfigJson);
            JsonNode mappings = root.get("mappings");
            if (mappings != null && mappings.isArray()) {
                for (JsonNode m : mappings) {
                    String target = m.has("targetField") ? m.get("targetField").asText() : "";
                    String expr = m.has("sourceExpression") ? m.get("sourceExpression").asText() : "";
                    String source = expr.replaceAll("[^a-zA-Z0-9_.]", "");
                    rules.add(new RecordLineageDto.MappingRuleSummary(source, target, expr));
                }
            } else if (root.isObject()) {
                root.fields().forEachRemaining(entry -> {
                    String target = entry.getKey();
                    String expr = entry.getValue().isTextual() ? entry.getValue().asText() : entry.getValue().toString();
                    rules.add(new RecordLineageDto.MappingRuleSummary(target, target, expr));
                });
            }
        } catch (Exception ignored) {}
        return rules;
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

    public RecordLineageDto.DomainLineageResponse getDomainLineage(UUID domainId) {
        com.classification.domain_system.entity.Domain domain = domainRepository.findById(domainId)
                .orElseThrow(() -> new ResourceNotFoundException("Domain not found: " + domainId));

        String domainName = domain.getName() != null ? domain.getName().getOrDefault("ko", domain.getName().getOrDefault("en", "도메인")) : "도메인";
        RecordLineageDto.DomainLineageResponse response = new RecordLineageDto.DomainLineageResponse(domain.getId(), domainName);

        String domainNodeId = "DOMAIN_" + domain.getId();
        RecordLineageDto.LineageNode rootNode = new RecordLineageDto.LineageNode(domainNodeId, domainName, "DOMAIN", formatDateTime(domain.getCreatedAt()));
        response.getNodes().add(rootNode);

        List<com.classification.domain_system.entity.ClassificationNode> nodes = nodeRepository.findByDomain_Id(domainId);
        for (com.classification.domain_system.entity.ClassificationNode n : nodes) {
            String nNodeId = "NODE_" + n.getId();
            String nName = n.getName() != null ? n.getName().getOrDefault("ko", n.getName().getOrDefault("en", "분류 노드")) : "분류 노드";
            RecordLineageDto.LineageNode childNode = new RecordLineageDto.LineageNode(nNodeId, nName, "NODE", formatDateTime(n.getCreatedAt()));
            response.getNodes().add(childNode);
            response.getEdges().add(new RecordLineageDto.LineageEdge(domainNodeId, nNodeId, "CONTAINS"));
        }

        List<com.classification.domain_system.entity.IntegrationChannel> channels = channelRepository.findAll();
        for (com.classification.domain_system.entity.IntegrationChannel ch : channels) {
            String chNodeId = "CHANNEL_" + ch.getId();
            RecordLineageDto.LineageNode chNode = new RecordLineageDto.LineageNode(chNodeId, ch.getName(), "CHANNEL", formatDateTime(ch.getCreatedAt()));
            response.getNodes().add(chNode);
            response.getEdges().add(new RecordLineageDto.LineageEdge(chNodeId, domainNodeId, "SYNC_PIPELINE"));
        }

        return response;
    }
}
