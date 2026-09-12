package com.classification.domain_system.batch.stock;

import com.classification.domain_system.batch.stock.dto.StockApiRawItem;
import com.classification.domain_system.batch.stock.reader.StockMarketApiItemReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

class StockMarketApiItemReaderTest {

    @Mock
    private RestTemplate restTemplate;

    private StockMarketApiItemReader reader;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("국내 주식 조회 시 integration 상세 API로부터 실제 52주 최고가/최저가를 정확히 파싱하여 매핑한다")
    void read_DomesticStock_ShouldParseReal52WeekHighAndLow() throws Exception {
        reader = new StockMarketApiItemReader(restTemplate, List.of("KOSPI"));

        // 1. Mock marketValue API response for KOSPI
        String marketValueJson = """
        {
          "stocks": [
            {
              "itemCode": "034220",
              "reutersCode": "034220",
              "stockName": "LG디스플레이",
              "closePrice": "8,940",
              "closePriceRaw": "8940",
              "compareToPreviousClosePriceRaw": "-270",
              "marketValueRaw": "4470000000000",
              "accumulatedTradingVolumeRaw": "1000000",
              "stockExchangeType": { "nameKor": "코스피" },
              "tradeStopType": { "code": "1" }
            }
          ]
        }
        """;

        // 2. Mock integration API response for LG디스플레이 (034220)
        String integrationJson = """
        {
          "totalInfos": [
            { "code": "highPriceOf52Weeks", "key": "52주 최고", "value": "17,950" },
            { "code": "lowPriceOf52Weeks", "key": "52주 최저", "value": "8,120" }
          ],
          "dealTrendInfos": []
        }
        """;

        // Mock empty page 2 to terminate reading
        String emptyPageJson = """
        { "stocks": [] }
        """;

        given(restTemplate.exchange(
                argThat((String url) -> url != null && url.contains("/stocks/marketValue/KOSPI?page=1")),
                eq(HttpMethod.GET), any(), eq(String.class)))
                .willReturn(new ResponseEntity<>(marketValueJson, HttpStatus.OK));

        given(restTemplate.exchange(
                argThat((String url) -> url != null && url.contains("/api/stock/034220/integration")),
                eq(HttpMethod.GET), any(), eq(String.class)))
                .willReturn(new ResponseEntity<>(integrationJson, HttpStatus.OK));

        given(restTemplate.exchange(
                argThat((String url) -> url != null && url.contains("/stocks/marketValue/KOSPI?page=2")),
                eq(HttpMethod.GET), any(), eq(String.class)))
                .willReturn(new ResponseEntity<>(emptyPageJson, HttpStatus.OK));

        StockApiRawItem item = reader.read();

        assertThat(item).isNotNull();
        assertThat(item.getTickerCode()).isEqualTo("034220");
        assertThat(item.getCurrentPrice()).isEqualTo(8940.0);

        // Verify that 52-week prices are NOT the dummy values (8940 * 1.3 = 11622.0, 8940 * 0.7 = 6258.0)
        // but the actual parsed values from integration API: 17,950 and 8,120
        assertThat(item.getWeek52High()).isEqualTo(17950.0);
        assertThat(item.getWeek52Low()).isEqualTo(8120.0);
    }

    @Test
    @DisplayName("해외 주식(NASDAQ) 조회 시 basic 상세 API로부터 실제 52주 최고가/최저가를 정확히 파싱하여 매핑한다")
    void read_OverseasStock_ShouldParseReal52WeekHighAndLow() throws Exception {
        reader = new StockMarketApiItemReader(restTemplate, List.of("NASDAQ"));

        // 1. Mock US marketValue API response
        String usMarketValueJson = """
        {
          "stocks": [
            {
              "symbolCode": "NVDA",
              "reutersCode": "NVDA.O",
              "stockName": "엔비디아",
              "stockNameEng": "NVIDIA Corporation",
              "closePrice": "225.91",
              "marketValue": "5444431000000",
              "industryCodeType": { "name": "Technology" }
            }
          ]
        }
        """;

        // 2. Mock US basic detail API response
        String usBasicJson = """
        {
          "stockItemTotalInfos": [
            { "code": "highPriceOf52Weeks", "key": "52주 최고", "value": "236.54" },
            { "code": "lowPriceOf52Weeks", "key": "52주 최저", "value": "164.07" }
          ]
        }
        """;

        String emptyPageJson = """
        { "stocks": [] }
        """;

        given(restTemplate.exchange(
                argThat((String url) -> url != null && url.contains("/exchange/NASDAQ/marketValue?page=1")),
                eq(HttpMethod.GET), any(), eq(String.class)))
                .willReturn(new ResponseEntity<>(usMarketValueJson, HttpStatus.OK));

        given(restTemplate.exchange(
                argThat((String url) -> url != null && url.contains("/stock/NVDA.O/basic")),
                eq(HttpMethod.GET), any(), eq(String.class)))
                .willReturn(new ResponseEntity<>(usBasicJson, HttpStatus.OK));

        given(restTemplate.exchange(
                argThat((String url) -> url != null && url.contains("/exchange/NASDAQ/marketValue?page=2")),
                eq(HttpMethod.GET), any(), eq(String.class)))
                .willReturn(new ResponseEntity<>(emptyPageJson, HttpStatus.OK));

        StockApiRawItem item = reader.read();

        assertThat(item).isNotNull();
        assertThat(item.getTickerCode()).isEqualTo("NVDA");
        assertThat(item.getCurrentPrice()).isEqualTo(225.91);

        // Verify that 52-week prices are NOT the dummy values (225.91 * 1.3, 225.91 * 0.7)
        // but the actual parsed values from basic API: 236.54 and 164.07
        assertThat(item.getWeek52High()).isEqualTo(236.54);
        assertThat(item.getWeek52Low()).isEqualTo(164.07);
    }

    @Test
    @DisplayName("국내 주식 조회 시 integration 상세 API로부터 당일 시가(openPrice), 고가(highPrice), 저가(lowPrice)를 정확히 파싱하여 매핑한다")
    void read_DomesticStock_ShouldParseRealOpenHighAndLowPrices() throws Exception {
        reader = new StockMarketApiItemReader(restTemplate, List.of("KOSPI"));

        String marketValueJson = """
        {
          "stocks": [
            {
              "itemCode": "005930",
              "reutersCode": "005930",
              "stockName": "삼성전자",
              "closePrice": "269,500",
              "closePriceRaw": "269500",
              "compareToPreviousClosePriceRaw": "0",
              "marketValueRaw": "1575572084856000",
              "accumulatedTradingVolumeRaw": "16080982",
              "stockExchangeType": { "nameKor": "코스피" },
              "tradeStopType": { "code": "1" }
            }
          ]
        }
        """;

        String integrationJson = """
        {
          "totalInfos": [
            { "code": "lastClosePrice", "key": "전일", "value": "269,500" },
            { "code": "openPrice", "key": "시가", "value": "271,500" },
            { "code": "highPrice", "key": "고가", "value": "275,000" },
            { "code": "lowPrice", "key": "저가", "value": "266,000" },
            { "code": "highPriceOf52Weeks", "key": "52주 최고", "value": "380,000" },
            { "code": "lowPriceOf52Weeks", "key": "52주 최저", "value": "71,400" }
          ]
        }
        """;

        String emptyPageJson = """
        { "stocks": [] }
        """;

        given(restTemplate.exchange(
                argThat((String url) -> url != null && url.contains("/stocks/marketValue/KOSPI?page=1")),
                eq(HttpMethod.GET), any(), eq(String.class)))
                .willReturn(new ResponseEntity<>(marketValueJson, HttpStatus.OK));

        given(restTemplate.exchange(
                argThat((String url) -> url != null && url.contains("/api/stock/005930/integration")),
                eq(HttpMethod.GET), any(), eq(String.class)))
                .willReturn(new ResponseEntity<>(integrationJson, HttpStatus.OK));

        given(restTemplate.exchange(
                argThat((String url) -> url != null && url.contains("/stocks/marketValue/KOSPI?page=2")),
                eq(HttpMethod.GET), any(), eq(String.class)))
                .willReturn(new ResponseEntity<>(emptyPageJson, HttpStatus.OK));

        StockApiRawItem item = reader.read();

        assertThat(item).isNotNull();
        assertThat(item.getTickerCode()).isEqualTo("005930");
        assertThat(item.getCurrentPrice()).isEqualTo(269500.0);

        // 당일 시가/고가/저가가 현재가(269,500)와 같지 않고, integration API에서 파싱한 실제 값이어야 함
        assertThat(item.getOpenPrice()).isEqualTo(271500.0);
        assertThat(item.getHighPrice()).isEqualTo(275000.0);
        assertThat(item.getLowPrice()).isEqualTo(266000.0);
    }

    @Test
    @DisplayName("해외 주식 조회 시 basic 상세 API로부터 당일 시가(openPrice), 고가(highPrice), 저가(lowPrice)를 정확히 파싱하여 매핑한다")
    void read_OverseasStock_ShouldParseRealOpenHighAndLowPrices() throws Exception {
        reader = new StockMarketApiItemReader(restTemplate, List.of("NASDAQ"));

        String usMarketValueJson = """
        {
          "stocks": [
            {
              "symbolCode": "NVDA",
              "reutersCode": "NVDA.O",
              "stockName": "엔비디아",
              "stockNameEng": "NVIDIA Corporation",
              "closePrice": "224.19",
              "marketValue": "5402979000000",
              "industryCodeType": { "name": "Technology" }
            }
          ]
        }
        """;

        String usBasicJson = """
        {
          "stockItemTotalInfos": [
            { "code": "basePrice", "key": "전일", "value": "225.73" },
            { "code": "openPrice", "key": "시가", "value": "225.35" },
            { "code": "highPrice", "key": "고가", "value": "226.18" },
            { "code": "lowPrice", "key": "저가", "value": "223.51" },
            { "code": "highPriceOf52Weeks", "key": "52주 최고", "value": "236.54" },
            { "code": "lowPriceOf52Weeks", "key": "52주 최저", "value": "164.07" }
          ]
        }
        """;

        String emptyPageJson = """
        { "stocks": [] }
        """;

        given(restTemplate.exchange(
                argThat((String url) -> url != null && url.contains("/exchange/NASDAQ/marketValue?page=1")),
                eq(HttpMethod.GET), any(), eq(String.class)))
                .willReturn(new ResponseEntity<>(usMarketValueJson, HttpStatus.OK));

        given(restTemplate.exchange(
                argThat((String url) -> url != null && url.contains("/stock/NVDA.O/basic")),
                eq(HttpMethod.GET), any(), eq(String.class)))
                .willReturn(new ResponseEntity<>(usBasicJson, HttpStatus.OK));

        given(restTemplate.exchange(
                argThat((String url) -> url != null && url.contains("/exchange/NASDAQ/marketValue?page=2")),
                eq(HttpMethod.GET), any(), eq(String.class)))
                .willReturn(new ResponseEntity<>(emptyPageJson, HttpStatus.OK));

        StockApiRawItem item = reader.read();

        assertThat(item).isNotNull();
        assertThat(item.getTickerCode()).isEqualTo("NVDA");
        assertThat(item.getCurrentPrice()).isEqualTo(224.19);

        // 당일 시가/고가/저가가 basic API에서 파싱한 실제 값이어야 함
        assertThat(item.getOpenPrice()).isEqualTo(225.35);
        assertThat(item.getHighPrice()).isEqualTo(226.18);
        assertThat(item.getLowPrice()).isEqualTo(223.51);
    }

    @Test
    @DisplayName("국내 주식 조회 시 Daum Quote API와 KRX 공매도 API의 실제 데이터를 정확히 파싱하며 임의 추정/가짜 수식을 적용하지 않는다")
    void read_DomesticStock_ShouldParseRealDaumQuoteAndKrxShortSellingData() throws Exception {
        reader = new StockMarketApiItemReader(restTemplate, List.of("KOSPI"));

        String marketValueJson = """
        {
          "stocks": [
            {
              "itemCode": "034220",
              "reutersCode": "034220",
              "stockName": "LG디스플레이",
              "closePrice": "8,900",
              "closePriceRaw": "8900",
              "compareToPreviousClosePriceRaw": "-70",
              "marketValueRaw": "4450000000000",
              "accumulatedTradingVolumeRaw": "2571479",
              "stockExchangeType": { "nameKor": "코스피" },
              "tradeStopType": { "code": "1" }
            }
          ]
        }
        """;

        String daumQuoteJson = """
        {
          "symbolCode": "A034220",
          "code": "KR7034220004",
          "name": "LG디스플레이",
          "market": "KOSPI",
          "parValue": 5000.0,
          "listedShareCount": 500000000,
          "capitalStock": 2500000000000.0,
          "wicsSectorName": "디스플레이패널",
          "listingDate": "2004-07-23",
          "settleMonth": 12,
          "foreignRatio": 0.271861526,
          "foreignOwnShares": 135930763,
          "companySummary": "동사는 1985년 금성소프트웨어로 설립되었으며...",
          "stockState": {
            "isTradingSuspended": false,
            "isAdministrativeIssue": false
          }
        }
        """;

        String krxShortJson = """
        {
          "output": [
            {
              "RPT_DUTY_OCCR_DD": "2026/09/09",
              "BAL_QTY": "3,017,963",
              "LIST_SHRS": "500,000,000",
              "BAL_AMT": "27,010,768,850",
              "BAL_RTO": "0.60"
            }
          ]
        }
        """;

        String emptyPageJson = """
        { "stocks": [] }
        """;

        given(restTemplate.exchange(
                argThat((String url) -> url != null && url.contains("/stocks/marketValue/KOSPI?page=1")),
                eq(HttpMethod.GET), any(), eq(String.class)))
                .willReturn(new ResponseEntity<>(marketValueJson, HttpStatus.OK));

        given(restTemplate.exchange(
                argThat((String url) -> url != null && url.contains("finance.daum.net/api/quotes/A034220")),
                eq(HttpMethod.GET), any(), eq(String.class)))
                .willReturn(new ResponseEntity<>(daumQuoteJson, HttpStatus.OK));

        given(restTemplate.exchange(
                argThat((String url) -> url != null && url.contains("data.krx.co.kr/comm/bldAttendant/getJsonData.cmd")),
                eq(HttpMethod.POST), any(), eq(String.class)))
                .willReturn(new ResponseEntity<>(krxShortJson, HttpStatus.OK));

        given(restTemplate.exchange(
                argThat((String url) -> url != null && url.contains("/stocks/marketValue/KOSPI?page=2")),
                eq(HttpMethod.GET), any(), eq(String.class)))
                .willReturn(new ResponseEntity<>(emptyPageJson, HttpStatus.OK));

        StockApiRawItem item = reader.read();

        assertThat(item).isNotNull();
        assertThat(item.getTickerCode()).isEqualTo("034220");
        assertThat(item.getIsinCode()).isEqualTo("KR7034220004");
        assertThat(item.getParValue()).isEqualTo(5000.0);
        assertThat(item.getListedShares()).isEqualTo(500000000L);
        assertThat(item.getCapitalAmount()).isEqualTo(2500000000000L);
        assertThat(item.getIndustrySector()).isEqualTo("디스플레이패널");
        assertThat(item.getListingDate()).isEqualTo("2004-07-23");
        assertThat(item.getFiscalMonth()).isEqualTo("12");
        assertThat(item.getForeignOwnershipRatio()).isEqualTo(27.19);
        assertThat(item.getForeignHoldingShares()).isEqualTo(135930763L);
        assertThat(item.getShortSellingBalanceShares()).isEqualTo(3017963L);
        assertThat(item.getShortSellingRatio()).isEqualTo(0.60);
        assertThat(item.getBusinessSummary()).contains("금성소프트웨어");

    }

    @Test
    @DisplayName("해외 주식(US_MARKET) 조회 시 국내 전용 필드(공매도, 액면가, 자본금, 외국인 지분율 등)는 하드코딩되거나 채워지지 않고 null이어야 한다")
    void read_OverseasStock_ShouldNotPopulateDomesticFields() throws Exception {
        reader = new StockMarketApiItemReader(restTemplate, List.of("NASDAQ"));

        String usMarketValueJson = """
        {
          "stocks": [
            {
              "symbolCode": "AAPL",
              "reutersCode": "AAPL.O",
              "stockName": "애플",
              "stockNameEng": "Apple Inc.",
              "closePrice": "220.00",
              "marketValue": "3300000000000",
              "industryCodeType": { "name": "Technology" }
            }
          ]
        }
        """;

        String emptyPageJson = """
        { "stocks": [] }
        """;

        given(restTemplate.exchange(
                argThat((String url) -> url != null && url.contains("/exchange/NASDAQ/marketValue?page=1")),
                eq(HttpMethod.GET), any(), eq(String.class)))
                .willReturn(new ResponseEntity<>(usMarketValueJson, HttpStatus.OK));

        given(restTemplate.exchange(
                argThat((String url) -> url != null && url.contains("/exchange/NASDAQ/marketValue?page=2")),
                eq(HttpMethod.GET), any(), eq(String.class)))
                .willReturn(new ResponseEntity<>(emptyPageJson, HttpStatus.OK));

        StockApiRawItem item = reader.read();

        assertThat(item).isNotNull();
        assertThat(item.getTickerCode()).isEqualTo("AAPL");
        assertThat(item.getMarketNodeCode()).isEqualTo("US_MARKET");

        // 국내 전용 필드에 가짜 수치(parValue=0.001, foreignDailyNetBuy=100000 등)가 들어가지 않아야 함
        assertThat(item.getParValue()).isNull();
        assertThat(item.getCapitalAmount()).isNull();
        assertThat(item.getShortSellingBalanceShares()).isNull();
        assertThat(item.getShortSellingRatio()).isNull();
        assertThat(item.getForeignOwnershipRatio()).isNull();
        assertThat(item.getForeignDailyNetBuy()).isNull();
        assertThat(item.getInstDailyNetBuy()).isNull();
        assertThat(item.getRetailDailyNetBuy()).isNull();
    }
}
