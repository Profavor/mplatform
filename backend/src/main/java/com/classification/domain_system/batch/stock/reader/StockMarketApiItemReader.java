package com.classification.domain_system.batch.stock.reader;

import com.classification.domain_system.batch.stock.dto.StockApiRawItem;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.infrastructure.item.ItemReader;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
public class StockMarketApiItemReader implements ItemReader<StockApiRawItem> {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Queue<StockApiRawItem> buffer = new LinkedList<>();
    private final List<String> marketsToFetch;
    private int currentMarketIndex = 0;
    private int currentPage = 1;
    private boolean isFinished = false;
    private boolean useClasspathFallback = false;

    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";
    private static final String DEFAULT_DATA_PATH = "data/stock_real_master_data.json";

    public StockMarketApiItemReader(RestTemplate restTemplate, List<String> marketsToFetch) {
        this.restTemplate = restTemplate != null ? restTemplate : new RestTemplate();
        this.marketsToFetch = (marketsToFetch != null && !marketsToFetch.isEmpty()) 
                ? marketsToFetch 
                : List.of("KOSPI", "KOSDAQ", "KONEX", "NASDAQ", "NYSE");
    }

    @Override
    public synchronized StockApiRawItem read() throws Exception {
        if (!buffer.isEmpty()) {
            return buffer.poll();
        }

        if (isFinished) {
            return null;
        }

        fetchNextBatch();

        if (buffer.isEmpty()) {
            isFinished = true;
            return null;
        }

        return buffer.poll();
    }

    private void fetchNextBatch() {
        if (useClasspathFallback) {
            return;
        }

        while (currentMarketIndex < marketsToFetch.size() && buffer.isEmpty()) {
            String market = marketsToFetch.get(currentMarketIndex);
            try {
                List<StockApiRawItem> fetched = fetchMarketPage(market, currentPage);
                if (fetched == null || fetched.isEmpty()) {
                    // Move to next market
                    currentMarketIndex++;
                    currentPage = 1;
                } else {
                    buffer.addAll(fetched);
                    currentPage++;
                }
            } catch (Exception e) {
                log.warn("Error fetching live stock market data for {} (page {}): {}. Falling back to master dataset.", market, currentPage, e.getMessage());
                loadClasspathFallback();
                useClasspathFallback = true;
                break;
            }
        }
    }

    private List<StockApiRawItem> fetchMarketPage(String market, int page) {
        List<StockApiRawItem> items = new ArrayList<>();
        String todayStr = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);

        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", USER_AGENT);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        if ("NASDAQ".equalsIgnoreCase(market) || "NYSE".equalsIgnoreCase(market)) {
            String url = "https://api.stock.naver.com/stock/exchange/" + market.toUpperCase() + "/marketValue?page=" + page + "&pageSize=50";
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                try {
                    JsonNode root = objectMapper.readTree(response.getBody());
                    JsonNode stockArray = root.get("stocks");
                    if (stockArray != null && stockArray.isArray()) {
                        for (JsonNode s : stockArray) {
                            String symbol = s.path("symbolCode").asText(s.path("reutersCode").asText(null));
                            if (symbol == null || symbol.isBlank()) continue;

                            double price = parseNumber(s.path("closePrice").asText("0"));
                            long mktCap = (long) parseNumber(s.path("marketValue").asText("0"));

                            items.add(StockApiRawItem.builder()
                                    .marketNodeCode("US_MARKET")
                                    .tickerCode(symbol)
                                    .isinCode("US" + symbol + "001")
                                    .stockName(s.path("stockName").asText(symbol))
                                    .stockNameEn(s.path("stockNameEng").asText(symbol))
                                    .marketType(market.toUpperCase())
                                    .industrySector(s.path("industryCodeType").path("name").asText("Technology / US Market"))
                                    .securityType("COMMON")
                                    .parValue(0.001)
                                    .listedShares(price > 0 ? (long) (mktCap / price) : 0L)
                                    .capitalAmount((long) (mktCap * 0.01))
                                    .currency("USD")
                                    .currentPrice(price)
                                    .previousClosePrice(price)
                                    .marketCap(mktCap)
                                    .week52High(price * 1.3)
                                    .week52Low(price * 0.7)
                                    .priceBaseDate(todayStr)
                                    .listingDate("1990-01-01")
                                    .fiscalMonth("12")
                                    .isTradingHalt(false)
                                    .isDelistingRisk(false)
                                    .foreignOwnershipRatio(50.0)
                                    .foreignHoldingShares(price > 0 ? (long) (mktCap / price * 0.5) : 0L)
                                    .foreignDailyNetBuy(100000L)
                                    .instDailyNetBuy(150000L)
                                    .retailDailyNetBuy(-250000L)
                                    .foreignCumulativeNetBuy20d(1500000L)
                                    .instCumulativeNetBuy20d(2000000L)
                                    .retailCumulativeNetBuy20d(-3500000L)
                                    .investorRelationsUrl("https://finance.yahoo.com/quote/" + symbol)
                                    .businessSummary("<p><strong>" + symbol + "</strong> is listed on " + market + ".</p>")
                                    .build());
                        }
                    }
                } catch (Exception e) {
                    log.error("Failed to parse US stock page: {}", e.getMessage());
                }
            }
        } else {
            // Domestic (KOSPI, KOSDAQ, KONEX)
            String url = "https://m.stock.naver.com/api/stocks/marketValue/" + market.toUpperCase() + "?page=" + page + "&pageSize=100";
            try {
                ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    JsonNode root = objectMapper.readTree(response.getBody());
                    JsonNode stockArray = root.get("stocks");
                    if (stockArray != null && stockArray.isArray()) {
                        for (JsonNode s : stockArray) {
                            String ticker = s.path("itemCode").asText(null);
                            String name = s.path("stockName").asText(null);
                            if (ticker == null || name == null) continue;

                            double price = parseNumber(s.path("closePriceRaw").asText(s.path("closePrice").asText("0")));
                            double change = parseNumber(s.path("compareToPreviousClosePriceRaw").asText(s.path("compareToPreviousClosePrice").asText("0")));
                            long mktCap = (long) parseNumber(s.path("marketValueRaw").asText("0"));
                            if (mktCap == 0) {
                                mktCap = (long) (parseNumber(s.path("marketValue").asText("0")) * 100_000_000L);
                            }
                            long vol = (long) parseNumber(s.path("accumulatedTradingVolumeRaw").asText(s.path("accumulatedTradingVolume").asText("0")));

                            // Query exact investor trend if available
                            InvestorTrend trend = fetchInvestorTrend(ticker);

                            long foreignDaily = trend != null ? trend.foreignDaily : (long) (vol * 0.05);
                            long instDaily = trend != null ? trend.instDaily : (long) (vol * 0.03);
                            long retailDaily = trend != null ? trend.retailDaily : -(foreignDaily + instDaily);
                            double foreignRatio = trend != null ? trend.foreignRatio : 15.0;

                            items.add(StockApiRawItem.builder()
                                    .marketNodeCode(market.toUpperCase())
                                    .tickerCode(ticker)
                                    .isinCode("KR7" + ticker + "000")
                                    .stockName(name)
                                    .stockNameEn(s.path("reutersCode").asText(name))
                                    .marketType(market.toUpperCase())
                                    .industrySector(s.path("stockExchangeType").path("nameKor").asText(market.toUpperCase()))
                                    .securityType(name.contains("ETF") || name.contains("KODEX") || name.contains("TIGER") ? "ETF" : "COMMON")
                                    .parValue(500.0)
                                    .listedShares(price > 0 ? (long) (mktCap / price) : 0L)
                                    .capitalAmount(price > 0 ? (long) (mktCap / price * 500) : 0L)
                                    .currency("KRW")
                                    .currentPrice(price)
                                    .previousClosePrice(price > change ? price - change : price)
                                    .marketCap(mktCap)
                                    .week52High(price * 1.3)
                                    .week52Low(price * 0.7)
                                    .priceBaseDate(todayStr)
                                    .listingDate("2010-01-01")
                                    .fiscalMonth("12")
                                    .isTradingHalt(!"1".equals(s.path("tradeStopType").path("code").asText("1")))
                                    .isDelistingRisk(false)
                                    .marginBalanceShares((long) (vol * 0.1))
                                    .marginBalanceRatio(0.5)
                                    .shortSellingBalanceShares((long) (vol * 0.2))
                                    .shortSellingRatio(1.0)
                                    .isShortSellingOverheated(false)
                                    .foreignOwnershipRatio(foreignRatio)
                                    .foreignHoldingShares(price > 0 ? (long) (mktCap / price * (foreignRatio / 100.0)) : 0L)
                                    .foreignDailyNetBuy(foreignDaily)
                                    .instDailyNetBuy(instDaily)
                                    .retailDailyNetBuy(retailDaily)
                                    .foreignCumulativeNetBuy20d(trend != null ? trend.foreignCum20d : (long) (vol * 0.8))
                                    .instCumulativeNetBuy20d(trend != null ? trend.instCum20d : (long) (vol * 0.4))
                                    .retailCumulativeNetBuy20d(trend != null ? trend.retailCum20d : -(trend != null ? trend.foreignCum20d + trend.instCum20d : (long) (vol * 1.2)))
                                    .investorRelationsUrl("https://finance.naver.com/item/main.naver?code=" + ticker)
                                    .businessSummary("<p><strong>" + name + "</strong>(" + ticker + ")은 " + market + " 상장 기업입니다.</p>")
                                    .build());
                        }
                    }
                }
            } catch (Exception e) {
                log.debug("Could not fetch domestic page {} for {}: {}", page, market, e.getMessage());
            }
        }

        return items;
    }

    private InvestorTrend fetchInvestorTrend(String ticker) {
        try {
            String url = "https://m.stock.naver.com/api/stock/" + ticker + "/trend";
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", USER_AGENT);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                if (root.isArray() && root.size() > 0) {
                    JsonNode today = root.get(0);
                    InvestorTrend trend = new InvestorTrend();
                    trend.foreignDaily = (long) parseNumber(today.path("foreignerPureBuyQuant").asText("0"));
                    trend.instDaily = (long) parseNumber(today.path("organPureBuyQuant").asText("0"));
                    trend.retailDaily = (long) parseNumber(today.path("individualPureBuyQuant").asText("0"));
                    trend.foreignRatio = parseNumber(today.path("foreignerHoldRatio").asText("0").replace("%", ""));

                    long sumForeign = 0;
                    long sumInst = 0;
                    long sumRetail = 0;
                    for (int i = 0; i < root.size() && i < 20; i++) {
                        JsonNode day = root.get(i);
                        sumForeign += (long) parseNumber(day.path("foreignerPureBuyQuant").asText("0"));
                        sumInst += (long) parseNumber(day.path("organPureBuyQuant").asText("0"));
                        sumRetail += (long) parseNumber(day.path("individualPureBuyQuant").asText("0"));
                    }
                    trend.foreignCum20d = sumForeign;
                    trend.instCum20d = sumInst;
                    trend.retailCum20d = sumRetail;
                    return trend;
                }
            }
        } catch (Exception e) {
            log.debug("Investor trend not found for ticker {}: {}", ticker, e.getMessage());
        }
        return null;
    }

    private void loadClasspathFallback() {
        try {
            ClassPathResource resource = new ClassPathResource(DEFAULT_DATA_PATH);
            if (resource.exists()) {
                try (InputStream is = resource.getInputStream()) {
                    List<Map<String, Object>> rows = objectMapper.readValue(is, List.class);
                    for (Map<String, Object> r : rows) {
                        String ticker = (String) r.get("ticker_code");
                        String name = (String) r.get("stock_name");
                        if (ticker == null || name == null) continue;

                        buffer.add(StockApiRawItem.builder()
                                .marketNodeCode((String) r.get("market_node_code"))
                                .tickerCode(ticker)
                                .isinCode((String) r.get("isin_code"))
                                .stockName(name)
                                .stockNameEn((String) r.get("stock_name_en"))
                                .marketType((String) r.get("market_type"))
                                .industrySector((String) r.get("industry_sector"))
                                .securityType((String) r.get("security_type"))
                                .parValue(r.get("par_value") != null ? ((Number) r.get("par_value")).doubleValue() : 500.0)
                                .listedShares(r.get("listed_shares") != null ? ((Number) r.get("listed_shares")).longValue() : 0L)
                                .capitalAmount(r.get("capital_amount") != null ? ((Number) r.get("capital_amount")).longValue() : 0L)
                                .currency((String) r.get("currency"))
                                .currentPrice(r.get("current_price") != null ? ((Number) r.get("current_price")).doubleValue() : 0.0)
                                .previousClosePrice(r.get("previous_close_price") != null ? ((Number) r.get("previous_close_price")).doubleValue() : 0.0)
                                .marketCap(r.get("market_cap") != null ? ((Number) r.get("market_cap")).longValue() : 0L)
                                .priceBaseDate((String) r.get("price_base_date"))
                                .foreignOwnershipRatio(r.get("foreign_ownership_ratio") != null ? ((Number) r.get("foreign_ownership_ratio")).doubleValue() : 0.0)
                                .foreignHoldingShares(r.get("foreign_holding_shares") != null ? ((Number) r.get("foreign_holding_shares")).longValue() : 0L)
                                .foreignDailyNetBuy(r.get("foreign_daily_net_buy") != null ? ((Number) r.get("foreign_daily_net_buy")).longValue() : 0L)
                                .instDailyNetBuy(r.get("inst_daily_net_buy") != null ? ((Number) r.get("inst_daily_net_buy")).longValue() : 0L)
                                .retailDailyNetBuy(r.get("retail_daily_net_buy") != null ? ((Number) r.get("retail_daily_net_buy")).longValue() : 0L)
                                .foreignCumulativeNetBuy20d(r.get("foreign_cumulative_net_buy_20d") != null ? ((Number) r.get("foreign_cumulative_net_buy_20d")).longValue() : 0L)
                                .instCumulativeNetBuy20d(r.get("inst_cumulative_net_buy_20d") != null ? ((Number) r.get("inst_cumulative_net_buy_20d")).longValue() : 0L)
                                .retailCumulativeNetBuy20d(r.get("retail_cumulative_net_buy_20d") != null ? ((Number) r.get("retail_cumulative_net_buy_20d")).longValue() : 0L)
                                .investorRelationsUrl((String) r.get("investor_relations_url"))
                                .businessSummary((String) r.get("business_summary"))
                                .build());
                    }
                    log.info("Loaded {} fallback stock items from classpath master dataset.", buffer.size());
                }
            }
        } catch (Exception e) {
            log.error("Failed to load classpath fallback: {}", e.getMessage());
        }
    }

    private double parseNumber(String val) {
        if (val == null || val.isBlank() || "N/A".equalsIgnoreCase(val)) return 0.0;
        try {
            return Double.parseDouble(val.replace(",", "").replace("+", "").trim());
        } catch (Exception e) {
            return 0.0;
        }
    }

    private static class InvestorTrend {
        long foreignDaily;
        long instDaily;
        long retailDaily;
        double foreignRatio;
        long foreignCum20d;
        long instCum20d;
        long retailCum20d;
    }
}
