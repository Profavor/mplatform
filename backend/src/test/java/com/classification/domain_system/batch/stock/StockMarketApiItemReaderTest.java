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
}
