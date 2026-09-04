package com.classification.domain_system.service;

import com.classification.domain_system.dto.SpecializedDomainProvisionRequest;
import com.classification.domain_system.dto.StockSeedRequest;
import com.classification.domain_system.dto.StockSeedResponse;
import com.classification.domain_system.entity.ClassificationNode;
import com.classification.domain_system.entity.Domain;
import com.classification.domain_system.entity.IntegrationChannel;
import com.classification.domain_system.entity.Record;
import com.classification.domain_system.entity.RecordHistory;
import com.classification.domain_system.exception.BusinessException;
import com.classification.domain_system.exception.ErrorCode;
import com.classification.domain_system.exception.ResourceNotFoundException;
import com.classification.domain_system.integration.IntegrationLogService;
import com.classification.domain_system.repository.ClassificationNodeRepository;
import com.classification.domain_system.repository.DomainRepository;
import com.classification.domain_system.repository.IntegrationChannelRepository;
import com.classification.domain_system.repository.RecordHistoryRepository;
import com.classification.domain_system.repository.RecordRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockDataIngestionService {

    private final DomainRepository domainRepository;
    private final ClassificationNodeRepository nodeRepository;
    private final RecordRepository recordRepository;
    private final RecordHistoryRepository recordHistoryRepository;
    private final com.classification.domain_system.repository.FieldDefinitionRepository fieldDefinitionRepository;
    private final SpecializedDomainTemplateService specializedDomainTemplateService;
    private final RecordService recordService;
    private final IntegrationChannelRepository integrationChannelRepository;
    private final Optional<IntegrationLogService> integrationLogService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String STOCK_CATEGORY = "STOCK";
    private static final String DEFAULT_DATA_PATH = "data/stock_real_master_data.json";

    @Transactional
    public StockSeedResponse seedRealStockData(StockSeedRequest request, String requestedBy) {
        if (request == null) {
            request = new StockSeedRequest();
        }

        // 1. Ensure/Provision STOCK Domain & Schema (Sectors, FieldGroups, Fields, Nodes)
        specializedDomainTemplateService.provisionDomain(
                SpecializedDomainProvisionRequest.builder()
                        .category(STOCK_CATEGORY)
                        .build()
        );

        Domain domain = domainRepository.findBySpecializedCategory(STOCK_CATEGORY)
                .orElseThrow(() -> new ResourceNotFoundException("주식 특화도메인을 찾을 수 없습니다: " + STOCK_CATEGORY));

        // 1-1. Determine Domain Identifier Field Key dynamically
        String identifierFieldKey = null;
        if (domain.getIdentifierFieldId() != null && fieldDefinitionRepository != null) {
            com.classification.domain_system.entity.FieldDefinition idDef = fieldDefinitionRepository.findById(domain.getIdentifierFieldId()).orElse(null);
            if (idDef != null && idDef.getKey() != null && !idDef.getKey().isBlank()) {
                identifierFieldKey = idDef.getKey();
            }
        }
        if (identifierFieldKey == null && domain.getIdentifierField() != null && domain.getIdentifierField().getKey() != null) {
            identifierFieldKey = domain.getIdentifierField().getKey();
        }
        if (identifierFieldKey == null && fieldDefinitionRepository != null) {
            List<com.classification.domain_system.entity.FieldDefinition> domainFields = fieldDefinitionRepository.findByDomain_Id(domain.getId());
            if (domainFields != null) {
                for (com.classification.domain_system.entity.FieldDefinition fd : domainFields) {
                    String k = fd.getKey() != null ? fd.getKey().toLowerCase() : "";
                    if (k.equals("ticker_code") || k.endsWith("_code") || k.endsWith("_id") || Boolean.TRUE.equals(fd.getRequired())) {
                        identifierFieldKey = fd.getKey();
                        break;
                    }
                }
            }
        }
        if (identifierFieldKey == null) {
            identifierFieldKey = "ticker_code";
        }

        // 2. Ensure Inbound Integration Channel exists for STOCK domain
        IntegrationChannel inboundChannel = ensureStockInboundChannel(domain);

        // 3. Find and Map classification nodes
        List<ClassificationNode> allNodes = nodeRepository.findByDomain_IdAndIsDeletedFalse(domain.getId());
        Map<String, ClassificationNode> nodeMap = new HashMap<>();
        for (ClassificationNode n : allNodes) {
            String path = n.getPath() != null ? n.getPath().toUpperCase() : "";
            Map<String, String> nameMap = n.getName();
            String koName = nameMap != null && nameMap.get("ko") != null ? nameMap.get("ko").toUpperCase() : "";
            String enName = nameMap != null && nameMap.get("en") != null ? nameMap.get("en").toUpperCase() : "";

            if (path.contains("KOSPI") || koName.contains("KOSPI") || enName.contains("KOSPI")) {
                nodeMap.put("KOSPI", n);
            } else if (path.contains("KOSDAQ") || koName.contains("KOSDAQ") || enName.contains("KOSDAQ")) {
                nodeMap.put("KOSDAQ", n);
            } else if (path.contains("KONEX") || koName.contains("KONEX") || enName.contains("KONEX")) {
                nodeMap.put("KONEX", n);
            } else if (path.contains("미국") || path.contains("US") || koName.contains("미국") || enName.contains("US")) {
                nodeMap.put("US_MARKET", n);
            } else if (path.contains("아시아") || path.contains("ASIA") || koName.contains("아시아") || enName.contains("ASIA")) {
                nodeMap.put("ASIA_MARKET", n);
            } else if (path.contains("유럽") || path.contains("EUROPE") || koName.contains("유럽") || enName.contains("EUROPE")) {
                nodeMap.put("EUROPE_MARKET", n);
            }
        }

        // Fallback node if specific market node is missing
        ClassificationNode defaultNode = allNodes.stream()
                .filter(n -> n.getDepth() != null && n.getDepth() > 0)
                .findFirst()
                .orElse(allNodes.isEmpty() ? null : allNodes.get(0));

        if (defaultNode == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "주식 도메인에 유효한 분류 노드가 존재하지 않습니다.");
        }

        // 4. Optionally clear existing records (Individual safe delete, NO TRUNCATE)
        int totalDeleted = 0;
        if (Boolean.TRUE.equals(request.getClearExisting())) {
            List<Record> existingRecords = recordRepository.findByNode_Domain_Id(domain.getId());
            if (existingRecords != null && !existingRecords.isEmpty()) {
                totalDeleted = existingRecords.size();
                for (Record r : existingRecords) {
                    recordRepository.delete(r);
                }
                log.info("Successfully deleted {} existing stock records for domain [{}]", totalDeleted, domain.getId());
            }
        }

        // 5. Load Real Stock Dataset
        List<Map<String, Object>> stockRows = request.getCustomRows() != null && !request.getCustomRows().isEmpty()
                ? request.getCustomRows()
                : loadMasterDataset();
        if (stockRows.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "적재할 주식 마스터 데이터셋이 비어 있습니다.");
        }

        // 6. Ingest Records per Market
        Set<String> allowedMarkets = request.getMarkets() != null && !request.getMarkets().isEmpty()
                ? new HashSet<>(request.getMarkets().stream().map(String::toUpperCase).toList())
                : null;
        Integer limitPerMarket = request.getLimitPerMarket();

        Map<String, Integer> seededByMarket = new LinkedHashMap<>();
        int totalSeeded = 0;
        int totalCreated = 0;
        int totalMerged = 0;

        for (Map<String, Object> row : stockRows) {
            String marketCode = row.get("market_node_code") != null ? String.valueOf(row.get("market_node_code")).toUpperCase() : "KOSPI";
            if (allowedMarkets != null && !allowedMarkets.contains(marketCode)) {
                continue;
            }
            if (limitPerMarket != null && seededByMarket.getOrDefault(marketCode, 0) >= limitPerMarket) {
                continue;
            }

            ClassificationNode targetNode = nodeMap.getOrDefault(marketCode, defaultNode);

            // Clean row data before storing: remove internal helper keys
            Map<String, Object> recordData = new HashMap<>(row);
            recordData.remove("market_node_code");

            try {
                String idVal = null;
                if (recordData.get(identifierFieldKey) != null) {
                    idVal = String.valueOf(recordData.get(identifierFieldKey)).trim();
                } else if (recordData.get("ticker_code") != null) {
                    idVal = String.valueOf(recordData.get("ticker_code")).trim();
                }

                List<Record> existingRecords = Collections.emptyList();
                if (idVal != null && !idVal.isBlank()) {
                    existingRecords = recordRepository.findActiveRecordsByDomainAndFieldValue(domain.getId(), identifierFieldKey, idVal);
                }

                if (existingRecords != null && !existingRecords.isEmpty()) {
                    // MERGE: Update primary existing record
                    Record survivor = existingRecords.get(0);
                    String prevData = survivor.getData();

                    Map<String, Object> survivorMap = new HashMap<>();
                    if (prevData != null && !prevData.isBlank()) {
                        try {
                            survivorMap = objectMapper.readValue(prevData, new TypeReference<Map<String, Object>>() {});
                        } catch (Exception ex) {
                            log.warn("Could not parse previous data for record [{}]: {}", survivor.getId(), ex.getMessage());
                        }
                    }

                    // Merge incoming fields
                    for (Map.Entry<String, Object> entry : recordData.entrySet()) {
                        if (entry.getValue() != null) {
                            survivorMap.put(entry.getKey(), entry.getValue());
                        }
                    }

                    String mergedDataJson = objectMapper.writeValueAsString(survivorMap);
                    survivor.setData(mergedDataJson);
                    if (targetNode != null) {
                        survivor.setNode(targetNode);
                    }
                    survivor.setStatus("ACTIVE");
                    survivor.setSourceSystem("INBOUND_KRX_GLOBAL_STOCK");
                    survivor.setUpdatedAt(LocalDateTime.now());
                    survivor.setVersion((survivor.getVersion() != null ? survivor.getVersion() : 1) + 1);

                    String searchableText = recordService.generateSearchableData(survivor.getNode().getId(), mergedDataJson);
                    survivor.setSearchableData(searchableText);

                    Record savedRecord = recordRepository.save(survivor);

                    // Audit history for MERGE
                    RecordHistory history = new RecordHistory();
                    history.setRecordId(savedRecord.getId());
                    history.setChangeType("INBOUND_MERGE");
                    history.setChangedBy(requestedBy != null ? requestedBy : "system");
                    history.setSourceSystem("Inbound Integration Channel [KRX & Global Stock]");
                    history.setPreviousData(prevData);
                    history.setNewData(mergedDataJson);
                    history.setVersion(savedRecord.getVersion());
                    recordHistoryRepository.save(history);

                    // If duplicate records previously existed, consolidate them into survivor
                    if (existingRecords.size() > 1) {
                        for (int i = 1; i < existingRecords.size(); i++) {
                            Record dup = existingRecords.get(i);
                            String prevDupData = dup.getData();
                            dup.setStatus("MERGED");
                            dup.setMergedIntoRecordId(savedRecord.getId());
                            dup.setUpdatedAt(LocalDateTime.now());
                            recordRepository.save(dup);

                            RecordHistory dupHistory = new RecordHistory();
                            dupHistory.setRecordId(dup.getId());
                            dupHistory.setChangeType("MERGED_INTO");
                            dupHistory.setChangedBy(requestedBy != null ? requestedBy : "system");
                            dupHistory.setSourceSystem("Inbound Integration Channel [KRX & Global Stock]");
                            dupHistory.setPreviousData(prevDupData);
                            dupHistory.setNewData(null);
                            dupHistory.setVersion(dup.getVersion() != null ? dup.getVersion() : 1);
                            recordHistoryRepository.save(dupHistory);
                        }
                    }

                    totalMerged++;
                    seededByMarket.put(marketCode, seededByMarket.getOrDefault(marketCode, 0) + 1);
                    totalSeeded++;
                } else {
                    // INSERT: New record
                    String dataJson = objectMapper.writeValueAsString(recordData);

                    Record record = new Record();
                    record.setNode(targetNode);
                    record.setStatus("ACTIVE");
                    record.setSourceSystem("INBOUND_KRX_GLOBAL_STOCK");
                    record.setData(dataJson);
                    record.setVersion(1);

                    String searchableText = recordService.generateSearchableData(targetNode.getId(), dataJson);
                    record.setSearchableData(searchableText);

                    Record savedRecord = recordRepository.save(record);

                    RecordHistory history = new RecordHistory();
                    history.setRecordId(savedRecord.getId());
                    history.setChangeType("INBOUND_INGEST");
                    history.setChangedBy(requestedBy != null ? requestedBy : "system");
                    history.setSourceSystem("Inbound Integration Channel [KRX & Global Stock]");
                    history.setNewData(dataJson);
                    history.setVersion(1);
                    recordHistoryRepository.save(history);

                    totalCreated++;
                    seededByMarket.put(marketCode, seededByMarket.getOrDefault(marketCode, 0) + 1);
                    totalSeeded++;
                }
            } catch (Exception e) {
                log.error("Failed to seed stock record: {}", row.get("stock_name"), e);
            }
        }

        // 7. Log Inbound Integration History
        if (inboundChannel != null && integrationLogService.isPresent()) {
            try {
                integrationLogService.get().logSuccess(
                        inboundChannel.getId(),
                        null,
                        "STOCK_DATA_INGESTION",
                        "Ingested " + totalSeeded + " real stocks (Created: " + totalCreated + ", Merged: " + totalMerged + ") across markets: " + seededByMarket.keySet(),
                        objectMapper.writeValueAsString(seededByMarket)
                );
            } catch (Exception e) {
                log.warn("Could not write integration log for channel: {}", e.getMessage());
            }
        }

        log.info("Stock data ingestion completed. Seeded {} records (Created: {}, Merged: {}) across {}", totalSeeded, totalCreated, totalMerged, seededByMarket);

        return StockSeedResponse.builder()
                .domainId(domain.getId())
                .domainName(domain.getName() != null ? domain.getName().get("ko") : "주식 종목 마스터")
                .totalSeeded(totalSeeded)
                .totalCreated(totalCreated)
                .totalMerged(totalMerged)
                .totalDeleted(totalDeleted)
                .seededByMarket(seededByMarket)
                .message(String.format("총 %d개의 실제 상장 주식(신규: %d건, 머지: %d건) 데이터가 성공적으로 적재/병합되었습니다.", totalSeeded, totalCreated, totalMerged))
                .build();
    }

    private IntegrationChannel ensureStockInboundChannel(Domain domain) {
        try {
            List<IntegrationChannel> channels = integrationChannelRepository.findAll();
            for (IntegrationChannel ch : channels) {
                if ("STOCK_INBOUND_CHANNEL".equalsIgnoreCase(ch.getName()) || 
                    (ch.getName() != null && ch.getName().contains("Stock Inbound"))) {
                    return ch;
                }
            }

            IntegrationChannel channel = new IntegrationChannel();
            channel.setName("KRX & Global Stock Inbound Pipeline");
            channel.setType("WEB_SERVICE");
            channel.setDirection("INBOUND");
            channel.setActive(true);
            channel.setConfigJson("{\"domainId\":\"" + domain.getId() + "\",\"protocol\":\"REST_API\",\"description\":\"실제 국내/해외 상장 주식 마스터 및 시세/수급 데이터 인바운드 연계 파이프라인\"}");
            channel.setMappingConfigJson("{\"mappings\":[]}");
            return integrationChannelRepository.save(channel);
        } catch (Exception e) {
            log.warn("Could not ensure stock inbound channel: {}", e.getMessage());
            return null;
        }
    }

    public List<Map<String, Object>> loadMasterDataset() {
        try {
            ClassPathResource resource = new ClassPathResource(DEFAULT_DATA_PATH);
            if (!resource.exists()) {
                log.warn("Default stock dataset not found at classpath: {}", DEFAULT_DATA_PATH);
                return Collections.emptyList();
            }
            try (InputStream is = resource.getInputStream()) {
                return objectMapper.readValue(is, new TypeReference<List<Map<String, Object>>>() {});
            }
        } catch (Exception e) {
            log.error("Error reading stock dataset from classpath: {}", DEFAULT_DATA_PATH, e);
            return Collections.emptyList();
        }
    }
}
