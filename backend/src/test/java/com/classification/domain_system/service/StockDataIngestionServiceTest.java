package com.classification.domain_system.service;

import com.classification.domain_system.base.BaseServiceTest;
import com.classification.domain_system.dto.SpecializedDomainProvisionRequest;
import com.classification.domain_system.dto.StockSeedRequest;
import com.classification.domain_system.dto.StockSeedResponse;
import com.classification.domain_system.entity.ClassificationNode;
import com.classification.domain_system.entity.Domain;
import com.classification.domain_system.entity.Record;
import com.classification.domain_system.entity.RecordHistory;
import com.classification.domain_system.repository.ClassificationNodeRepository;
import com.classification.domain_system.repository.DomainRepository;
import com.classification.domain_system.repository.RecordHistoryRepository;
import com.classification.domain_system.repository.RecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

class StockDataIngestionServiceTest extends BaseServiceTest {

    @Mock
    private DomainRepository domainRepository;

    @Mock
    private ClassificationNodeRepository nodeRepository;

    @Mock
    private RecordRepository recordRepository;

    @Mock
    private RecordHistoryRepository recordHistoryRepository;

    @Mock
    private SpecializedDomainTemplateService specializedDomainTemplateService;

    @Mock
    private RecordService recordService;

    @Mock
    private com.classification.domain_system.repository.FieldDefinitionRepository fieldDefinitionRepository;

    @Mock
    private com.classification.domain_system.repository.IntegrationChannelRepository integrationChannelRepository;

    @InjectMocks
    private StockDataIngestionService stockDataIngestionService;

    private Domain testStockDomain;
    private ClassificationNode kospiNode;
    private ClassificationNode kosdaqNode;
    private ClassificationNode usMarketNode;

    @BeforeEach
    void setUp() {
        testStockDomain = new Domain();
        testStockDomain.setId(UUID.randomUUID());
        testStockDomain.setSpecializedCategory("STOCK");
        testStockDomain.setName(Map.of("ko", "주식 종목 마스터", "en", "Stock Master"));

        kospiNode = new ClassificationNode();
        kospiNode.setId(UUID.randomUUID());
        kospiNode.setDomain(testStockDomain);
        kospiNode.setName(Map.of("ko", "유가증권시장 (KOSPI)", "en", "KOSPI"));
        kospiNode.setPath("/market/국내 유가증권/유가증권시장 (KOSPI)");

        kosdaqNode = new ClassificationNode();
        kosdaqNode.setId(UUID.randomUUID());
        kosdaqNode.setDomain(testStockDomain);
        kosdaqNode.setName(Map.of("ko", "코스닥시장 (KOSDAQ)", "en", "KOSDAQ"));
        kosdaqNode.setPath("/market/국내 유가증권/코스닥시장 (KOSDAQ)");

        usMarketNode = new ClassificationNode();
        usMarketNode.setId(UUID.randomUUID());
        usMarketNode.setDomain(testStockDomain);
        usMarketNode.setName(Map.of("ko", "미국 주식 (NYSE/NASDAQ)", "en", "US Markets"));
        usMarketNode.setPath("/market/해외 주식/미국 주식 (NYSE/NASDAQ)");
    }

    @Nested
    @DisplayName("seedRealStockData")
    class SeedRealStockData {

        @Test
        @DisplayName("주식 특화도메인에 실제 상장 종목 데이터(시세/수급 포함)를 정상 적재한다")
        void seedAllMarketsSuccessfully() {
            given(domainRepository.findBySpecializedCategory("STOCK")).willReturn(Optional.of(testStockDomain));
            given(nodeRepository.findByDomain_IdAndIsDeletedFalse(testStockDomain.getId()))
                    .willReturn(List.of(kospiNode, kosdaqNode, usMarketNode));
            given(recordService.generateSearchableData(any(), any())).willReturn("{\"ticker_code\":\"005930\"}");
            given(recordRepository.save(any(Record.class))).willAnswer(invocation -> {
                Record r = invocation.getArgument(0);
                r.setId(UUID.randomUUID());
                return r;
            });

            StockSeedRequest request = StockSeedRequest.builder()
                    .clearExisting(false)
                    .build();

            StockSeedResponse response = stockDataIngestionService.seedRealStockData(request, "admin");

            assertThat(response).isNotNull();
            assertThat(response.getDomainId()).isEqualTo(testStockDomain.getId());
            assertThat(response.getTotalSeeded()).isGreaterThan(0);
            assertThat(response.getSeededByMarket()).containsKey("KOSPI");

            verify(specializedDomainTemplateService, times(1)).provisionDomain(any(SpecializedDomainProvisionRequest.class));
            verify(recordRepository, atLeastOnce()).save(any(Record.class));
            verify(recordHistoryRepository, atLeastOnce()).save(any(RecordHistory.class));
        }

        @Test
        @DisplayName("기존에 동일한 아이디 속성(ticker_code)을 가진 레코드가 존재할 경우 새로 만들지 않고 머지(UPDATE)한다")
        void seedStockData_WhenMatchingIdentifierExists_ShouldMergeExistingRecordAndBumpVersion() {
            given(domainRepository.findBySpecializedCategory("STOCK")).willReturn(Optional.of(testStockDomain));
            given(nodeRepository.findByDomain_IdAndIsDeletedFalse(testStockDomain.getId()))
                    .willReturn(List.of(kospiNode, kosdaqNode, usMarketNode));

            // Existing record for LG Display (034220)
            Record existingRecord = new Record();
            existingRecord.setId(UUID.randomUUID());
            existingRecord.setNode(kospiNode);
            existingRecord.setStatus("ACTIVE");
            existingRecord.setData("{\"ticker_code\":\"034220\",\"stock_name\":\"LG디스플레이\",\"current_price\":10000}");
            existingRecord.setVersion(1);

            given(recordRepository.findActiveRecordsByDomainAndFieldValue(testStockDomain.getId(), "ticker_code", "034220"))
                    .willReturn(List.of(existingRecord));
            given(recordService.generateSearchableData(any(), any())).willReturn("LG디스플레이 034220");
            given(recordRepository.save(any(Record.class))).willAnswer(invocation -> invocation.getArgument(0));

            // Custom incoming row with updated price
            Map<String, Object> customRow = new HashMap<>();
            customRow.put("market_node_code", "KOSPI");
            customRow.put("ticker_code", "034220");
            customRow.put("stock_name", "LG디스플레이");
            customRow.put("current_price", 12500);

            StockSeedRequest request = StockSeedRequest.builder()
                    .customRows(List.of(customRow))
                    .clearExisting(false)
                    .build();

            StockSeedResponse response = stockDataIngestionService.seedRealStockData(request, "admin");

            assertThat(response).isNotNull();
            assertThat(response.getTotalSeeded()).isEqualTo(1);
            assertThat(response.getTotalMerged()).isEqualTo(1);
            assertThat(response.getTotalCreated()).isEqualTo(0);

            // Verify existing record version was bumped and merged data saved
            assertThat(existingRecord.getVersion()).isEqualTo(2);
            assertThat(existingRecord.getData()).contains("12500");

            verify(recordRepository, times(1)).save(existingRecord);
            verify(recordHistoryRepository, times(1)).save(argThat(h ->
                    "INBOUND_MERGE".equals(h.getChangeType()) && h.getVersion() == 2));
        }

        @Test
        @DisplayName("기존에 여러 개의 중복 레코드가 존재할 경우 1개로 수렴하여 머지하고 나머지는 MERGED 상태로 처리한다")
        void seedStockData_WhenMultipleDuplicatesExist_ShouldMergeIntoSurvivorAndMarkOthersMerged() {
            given(domainRepository.findBySpecializedCategory("STOCK")).willReturn(Optional.of(testStockDomain));
            given(nodeRepository.findByDomain_IdAndIsDeletedFalse(testStockDomain.getId()))
                    .willReturn(List.of(kospiNode, kosdaqNode, usMarketNode));

            Record rec1 = new Record();
            rec1.setId(UUID.randomUUID());
            rec1.setNode(kospiNode);
            rec1.setStatus("ACTIVE");
            rec1.setData("{\"ticker_code\":\"034220\",\"stock_name\":\"LG디스플레이\",\"current_price\":10000}");
            rec1.setVersion(1);

            Record rec2 = new Record();
            rec2.setId(UUID.randomUUID());
            rec2.setNode(kospiNode);
            rec2.setStatus("ACTIVE");
            rec2.setData("{\"ticker_code\":\"034220\",\"stock_name\":\"LG디스플레이\",\"current_price\":10000}");
            rec2.setVersion(1);

            given(recordRepository.findActiveRecordsByDomainAndFieldValue(testStockDomain.getId(), "ticker_code", "034220"))
                    .willReturn(List.of(rec1, rec2));
            given(recordService.generateSearchableData(any(), any())).willReturn("LG디스플레이 034220");
            given(recordRepository.save(any(Record.class))).willAnswer(invocation -> invocation.getArgument(0));

            Map<String, Object> customRow = new HashMap<>();
            customRow.put("market_node_code", "KOSPI");
            customRow.put("ticker_code", "034220");
            customRow.put("stock_name", "LG디스플레이");
            customRow.put("current_price", 13000);

            StockSeedRequest request = StockSeedRequest.builder()
                    .customRows(List.of(customRow))
                    .clearExisting(false)
                    .build();

            StockSeedResponse response = stockDataIngestionService.seedRealStockData(request, "admin");

            assertThat(response).isNotNull();
            assertThat(response.getTotalMerged()).isEqualTo(1);

            // rec1 is survivor
            assertThat(rec1.getStatus()).isEqualTo("ACTIVE");
            assertThat(rec1.getVersion()).isEqualTo(2);

            // rec2 is marked as MERGED
            assertThat(rec2.getStatus()).isEqualTo("MERGED");
            assertThat(rec2.getMergedIntoRecordId()).isEqualTo(rec1.getId());

            verify(recordRepository, times(1)).save(rec1);
            verify(recordRepository, times(1)).save(rec2);
        }

        @Test
        @DisplayName("특정 시장(KOSPI) 필터 지정 시 해당 시장의 종목만 선별 적재한다")
        void seedFilteredMarketOnly() {
            given(domainRepository.findBySpecializedCategory("STOCK")).willReturn(Optional.of(testStockDomain));
            given(nodeRepository.findByDomain_IdAndIsDeletedFalse(testStockDomain.getId()))
                    .willReturn(List.of(kospiNode, kosdaqNode, usMarketNode));
            given(recordService.generateSearchableData(any(), any())).willReturn("{\"ticker_code\":\"005930\"}");
            given(recordRepository.save(any(Record.class))).willAnswer(invocation -> {
                Record r = invocation.getArgument(0);
                r.setId(UUID.randomUUID());
                return r;
            });

            StockSeedRequest request = StockSeedRequest.builder()
                    .markets(List.of("KOSPI"))
                    .clearExisting(false)
                    .build();

            StockSeedResponse response = stockDataIngestionService.seedRealStockData(request, "admin");

            assertThat(response).isNotNull();
            assertThat(response.getSeededByMarket()).containsKey("KOSPI");
            assertThat(response.getSeededByMarket()).doesNotContainKey("US_MARKET");
        }

        @Test
        @DisplayName("clearExisting=true 지정 시 기존 레코드를 명시적으로 조회하여 개별 삭제 후 적재한다")
        void clearExistingRecordsBeforeSeeding() {
            given(domainRepository.findBySpecializedCategory("STOCK")).willReturn(Optional.of(testStockDomain));
            given(nodeRepository.findByDomain_IdAndIsDeletedFalse(testStockDomain.getId()))
                    .willReturn(List.of(kospiNode, kosdaqNode, usMarketNode));
            
            Record oldRecord = new Record();
            oldRecord.setId(UUID.randomUUID());
            oldRecord.setNode(kospiNode);

            given(recordRepository.findByNode_Domain_Id(testStockDomain.getId())).willReturn(List.of(oldRecord));
            given(recordService.generateSearchableData(any(), any())).willReturn("{\"ticker_code\":\"005930\"}");
            given(recordRepository.save(any(Record.class))).willAnswer(invocation -> {
                Record r = invocation.getArgument(0);
                r.setId(UUID.randomUUID());
                return r;
            });

            StockSeedRequest request = StockSeedRequest.builder()
                    .clearExisting(true)
                    .build();

            StockSeedResponse response = stockDataIngestionService.seedRealStockData(request, "admin");

            assertThat(response).isNotNull();
            assertThat(response.getTotalDeleted()).isEqualTo(1);
            verify(recordRepository, times(1)).delete(oldRecord);
        }
    }
}
