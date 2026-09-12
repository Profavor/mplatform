package com.classification.domain_system.batch.stock;

import com.classification.domain_system.batch.stock.dto.StockApiRawItem;
import com.classification.domain_system.batch.stock.processor.StockItemProcessor;
import com.classification.domain_system.entity.ClassificationNode;
import com.classification.domain_system.entity.Domain;
import com.classification.domain_system.entity.Record;
import com.classification.domain_system.service.RecordService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

class StockItemProcessorTest {

    @Mock
    private RecordService recordService;

    private StockItemProcessor processor;
    private Domain testDomain;
    private ClassificationNode kospiNode;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testDomain = new Domain();
        testDomain.setId(UUID.randomUUID());
        testDomain.setName(Map.of("ko", "주식 종목 마스터"));

        kospiNode = new ClassificationNode();
        kospiNode.setId(UUID.randomUUID());
        kospiNode.setName(Map.of("ko", "유가증권시장 (KOSPI)"));
        kospiNode.setDomain(testDomain);

        Map<String, ClassificationNode> nodeMap = Map.of("KOSPI", kospiNode);

        processor = new StockItemProcessor(recordService, nodeMap, kospiNode);
    }

    @Test
    @DisplayName("원천 주식 DTO를 MDM Record 엔티티로 정규화 및 수급 데이터 매핑 검증")
    void process_ShouldNormalizeStockItemToRecord() throws Exception {
        StockApiRawItem rawItem = StockApiRawItem.builder()
                .marketNodeCode("KOSPI")
                .tickerCode("005930")
                .stockName("삼성전자")
                .stockNameEn("Samsung Electronics")
                .marketType("KOSPI")
                .currentPrice(78500.0)
                .previousClosePrice(77500.0)
                .openPrice(77800.0)
                .highPrice(79000.0)
                .lowPrice(77600.0)
                .changePrice(1000.0)
                .fluctuationRate(1.29)
                .accumulatedTradingVolume(15420000L)
                .accumulatedTradingValue(1210000000000L)
                .per(14.52)
                .eps(5406.0)
                .cnsPer(11.20)
                .cnsEps(7000.0)
                .pbr(1.42)
                .bps(55280.0)
                .dividendYieldRatio(2.15)
                .dividendPerShare(1444.0)
                .dividendAt("2024-04-19")
                .exDividendAt("2023-12-27")
                .foreignExhaustionRatio(56.12)
                .logoImageUrl("https://ssl.pstatic.net/imgstock/images5/logo/KR7005930003.png")
                .marketCap(468627930175000L)
                .foreignDailyNetBuy(482000L)
                .instDailyNetBuy(150000L)
                .retailDailyNetBuy(-632000L)
                .foreignOwnershipRatio(46.72)
                .currency("KRW")
                .build();

        given(recordService.generateSearchableData(eq(kospiNode.getId()), any())).willReturn("삼성전자 005930 KOSPI");

        Record record = processor.process(rawItem);

        assertThat(record).isNotNull();
        assertThat(record.getNode()).isEqualTo(kospiNode);
        assertThat(record.getStatus()).isEqualTo("ACTIVE");
        assertThat(record.getSourceSystem()).isEqualTo("SPRING_BATCH_STOCK_INGESTION");
        assertThat(record.getSearchableData()).contains("삼성전자");

        Map<String, Object> data = objectMapper.readValue(record.getData(), Map.class);
        assertThat(data.get("ticker_code")).isEqualTo("005930");
        assertThat(data.get("stock_name")).isEqualTo("삼성전자");
        assertThat(data.get("current_price")).isEqualTo(78500.0);
        assertThat(data.get("foreign_daily_net_buy")).isEqualTo(482000);
        assertThat(data.get("foreign_ownership_ratio")).isEqualTo(46.72);

        // 19개 네이버 API 신규 지표 매핑 검증
        assertThat(data.get("open_price")).isEqualTo(77800.0);
        assertThat(data.get("high_price")).isEqualTo(79000.0);
        assertThat(data.get("low_price")).isEqualTo(77600.0);
        assertThat(data.get("change_price")).isEqualTo(1000.0);
        assertThat(data.get("fluctuation_rate")).isEqualTo(1.29);
        assertThat(data.get("accumulated_trading_volume")).isEqualTo(15420000);
        assertThat(data.get("accumulated_trading_value")).isEqualTo(1210000000000L);
        assertThat(data.get("per")).isEqualTo(14.52);
        assertThat(data.get("eps")).isEqualTo(5406.0);
        assertThat(data.get("cns_per")).isEqualTo(11.20);
        assertThat(data.get("cns_eps")).isEqualTo(7000.0);
        assertThat(data.get("pbr")).isEqualTo(1.42);
        assertThat(data.get("bps")).isEqualTo(55280.0);
        assertThat(data.get("dividend_yield_ratio")).isEqualTo(2.15);
        assertThat(data.get("dividend_per_share")).isEqualTo(1444.0);
        assertThat(data.get("dividend_date")).isEqualTo("2024-04-19");
        assertThat(data.get("ex_dividend_date")).isEqualTo("2023-12-27");
        assertThat(data.get("foreign_exhaustion_ratio")).isEqualTo(56.12);
        assertThat(data.get("logo_image_url")).isEqualTo("https://ssl.pstatic.net/imgstock/images5/logo/KR7005930003.png");

        // 미제공/하드코딩 6개 필드 영구 삭제 검증
        assertThat(data).doesNotContainKeys(
                "margin_balance_shares", "margin_balance_ratio", "is_short_selling_overheated",
                "transfer_agent", "settlement_cycle", "investor_relations_url"
        );
    }
}
