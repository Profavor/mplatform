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
                            String reutersCode = s.path("reutersCode").asText(null);

                            double price = parseNumber(s.path("closePrice").asText("0"));
                            long mktCap = (long) parseNumber(s.path("marketValue").asText("0"));

                            StockDetailInfo detail = fetchOverseasStockDetail(reutersCode, symbol);
                            double week52High = (detail != null && detail.week52High != null && detail.week52High > 0)
                                    ? detail.week52High : price;
                            double week52Low = (detail != null && detail.week52Low != null && detail.week52Low > 0)
                                    ? detail.week52Low : price;

                            double openPrice = parseNumber(s.path("openPriceRaw").asText(s.path("openPrice").asText("0")));
                            double highPrice = parseNumber(s.path("highPriceRaw").asText(s.path("highPrice").asText("0")));
                            double lowPrice = parseNumber(s.path("lowPriceRaw").asText(s.path("lowPrice").asText("0")));
                            double changePrice = parseNumber(s.path("compareToPreviousClosePriceRaw").asText(s.path("compareToPreviousClosePrice").asText("0")));
                            double fluctuationRate = parseNumber(s.path("fluctuationsRatioRaw").asText(s.path("fluctuationsRatio").asText("0")));
                            long vol = (long) parseNumber(s.path("accumulatedTradingVolumeRaw").asText(s.path("accumulatedTradingVolume").asText("0")));
                            long val = (long) parseNumber(s.path("accumulatedTradingValueRaw").asText("0"));
                            String logoUrl = s.path("itemLogoUrl").asText(s.path("itemLogoPngUrl").asText(null));

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
                                    .week52High(week52High)
                                    .week52Low(week52Low)
                                    .openPrice(openPrice > 0 ? openPrice : price)
                                    .highPrice(highPrice > 0 ? highPrice : price)
                                    .lowPrice(lowPrice > 0 ? lowPrice : price)
                                    .changePrice(changePrice)
                                    .fluctuationRate(fluctuationRate)
                                    .accumulatedTradingVolume(vol)
                                    .accumulatedTradingValue(val)
                                    .logoImageUrl(logoUrl)
                                    .per(detail != null ? detail.per : null)
                                    .eps(detail != null ? detail.eps : null)
                                    .pbr(detail != null ? detail.pbr : null)
                                    .bps(detail != null ? detail.bps : null)
                                    .dividendYieldRatio(detail != null ? detail.dividendYieldRatio : null)
                                    .dividendPerShare(detail != null ? detail.dividendPerShare : null)
                                    .dividendAt(detail != null ? detail.dividendAt : null)
                                    .exDividendAt(detail != null ? detail.exDividendAt : null)
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

                            // Query exact investor trend and 52-week price detail if available
                            InvestorTrend trend = fetchInvestorTrend(ticker);
                            StockDetailInfo detail = fetchDomesticStockDetail(ticker);

                            long foreignDaily = trend != null ? trend.foreignDaily : (long) (vol * 0.05);
                            long instDaily = trend != null ? trend.instDaily : (long) (vol * 0.03);
                            long retailDaily = trend != null ? trend.retailDaily : -(foreignDaily + instDaily);
                            double foreignRatio = trend != null ? trend.foreignRatio : 15.0;

                            double week52High = (detail != null && detail.week52High != null && detail.week52High > 0)
                                    ? detail.week52High : price;
                            double week52Low = (detail != null && detail.week52Low != null && detail.week52Low > 0)
                                    ? detail.week52Low : price;

                            double openPrice = parseNumber(s.path("openPriceRaw").asText(s.path("openPrice").asText("0")));
                            double highPrice = parseNumber(s.path("highPriceRaw").asText(s.path("highPrice").asText("0")));
                            double lowPrice = parseNumber(s.path("lowPriceRaw").asText(s.path("lowPrice").asText("0")));
                            double fluctuationRate = parseNumber(s.path("fluctuationsRatioRaw").asText(s.path("fluctuationsRatio").asText("0")));
                            long tradingValue = (long) parseNumber(s.path("accumulatedTradingValueRaw").asText("0"));
                            String logoUrl = s.path("itemLogoUrl").asText(s.path("itemLogoPngUrl").asText(null));

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
                                    .week52High(week52High)
                                    .week52Low(week52Low)
                                    .openPrice(openPrice > 0 ? openPrice : price)
                                    .highPrice(highPrice > 0 ? highPrice : price)
                                    .lowPrice(lowPrice > 0 ? lowPrice : price)
                                    .changePrice(change)
                                    .fluctuationRate(fluctuationRate)
                                    .accumulatedTradingVolume(vol)
                                    .accumulatedTradingValue(tradingValue)
                                    .logoImageUrl(logoUrl)
                                    .per(detail != null ? detail.per : null)
                                    .eps(detail != null ? detail.eps : null)
                                    .cnsPer(detail != null ? detail.cnsPer : null)
                                    .cnsEps(detail != null ? detail.cnsEps : null)
                                    .pbr(detail != null ? detail.pbr : null)
                                    .bps(detail != null ? detail.bps : null)
                                    .dividendYieldRatio(detail != null ? detail.dividendYieldRatio : null)
                                    .dividendPerShare(detail != null ? detail.dividendPerShare : null)
                                    .foreignExhaustionRatio(detail != null ? detail.foreignExhaustionRatio : null)
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
                                .marginBalanceShares(r.get("margin_balance_shares") != null ? ((Number) r.get("margin_balance_shares")).longValue() : null)
                                .marginBalanceRatio(r.get("margin_balance_ratio") != null ? ((Number) r.get("margin_balance_ratio")).doubleValue() : null)
                                .shortSellingBalanceShares(r.get("short_selling_balance_shares") != null ? ((Number) r.get("short_selling_balance_shares")).longValue() : null)
                                .shortSellingRatio(r.get("short_selling_ratio") != null ? ((Number) r.get("short_selling_ratio")).doubleValue() : null)
                                .isShortSellingOverheated(r.get("is_short_selling_overheated") != null ? (Boolean) r.get("is_short_selling_overheated") : false)
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

    private StockDetailInfo fetchDomesticStockDetail(String ticker) {
        try {
            String url = "https://m.stock.naver.com/api/stock/" + ticker + "/integration";
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", USER_AGENT);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode totalInfos = root.path("totalInfos");
                if (totalInfos.isArray()) {
                    StockDetailInfo detail = new StockDetailInfo();
                    for (JsonNode info : totalInfos) {
                        String code = info.path("code").asText("");
                        String val = info.path("value").asText(null);
                        if ("highPriceOf52Weeks".equalsIgnoreCase(code)) {
                            detail.week52High = parseNumber(val);
                        } else if ("lowPriceOf52Weeks".equalsIgnoreCase(code)) {
                            detail.week52Low = parseNumber(val);
                        } else if ("per".equalsIgnoreCase(code)) {
                            detail.per = parseNumber(val);
                        } else if ("eps".equalsIgnoreCase(code)) {
                            detail.eps = parseNumber(val);
                        } else if ("cnsPer".equalsIgnoreCase(code)) {
                            detail.cnsPer = parseNumber(val);
                        } else if ("cnsEps".equalsIgnoreCase(code)) {
                            detail.cnsEps = parseNumber(val);
                        } else if ("pbr".equalsIgnoreCase(code)) {
                            detail.pbr = parseNumber(val);
                        } else if ("bps".equalsIgnoreCase(code)) {
                            detail.bps = parseNumber(val);
                        } else if ("dividendYieldRatio".equalsIgnoreCase(code)) {
                            detail.dividendYieldRatio = parseNumber(val);
                        } else if ("dividend".equalsIgnoreCase(code)) {
                            detail.dividendPerShare = parseNumber(val);
                        } else if ("foreignRate".equalsIgnoreCase(code)) {
                            detail.foreignExhaustionRatio = parseNumber(val);
                        }
                    }
                    return detail;
                }
            }
        } catch (Exception e) {
            log.debug("Integration detail not found for domestic ticker {}: {}", ticker, e.getMessage());
        }
        return null;
    }

    private StockDetailInfo fetchOverseasStockDetail(String reutersCode, String symbol) {
        String targetCode = (reutersCode != null && !reutersCode.isBlank()) ? reutersCode : symbol;
        if (targetCode == null || targetCode.isBlank()) {
            return null;
        }

        try {
            String url = "https://api.stock.naver.com/stock/" + targetCode + "/basic";
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", USER_AGENT);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode totalInfos = root.path("stockItemTotalInfos");
                if (totalInfos.isArray()) {
                    StockDetailInfo detail = new StockDetailInfo();
                    for (JsonNode info : totalInfos) {
                        String code = info.path("code").asText("");
                        String val = info.path("value").asText(null);
                        if ("highPriceOf52Weeks".equalsIgnoreCase(code)) {
                            detail.week52High = parseNumber(val);
                        } else if ("lowPriceOf52Weeks".equalsIgnoreCase(code)) {
                            detail.week52Low = parseNumber(val);
                        } else if ("per".equalsIgnoreCase(code)) {
                            detail.per = parseNumber(val);
                        } else if ("eps".equalsIgnoreCase(code)) {
                            detail.eps = parseNumber(val);
                        } else if ("pbr".equalsIgnoreCase(code)) {
                            detail.pbr = parseNumber(val);
                        } else if ("bps".equalsIgnoreCase(code)) {
                            detail.bps = parseNumber(val);
                        } else if ("dividendYieldRatio".equalsIgnoreCase(code)) {
                            detail.dividendYieldRatio = parseNumber(val);
                        } else if ("dividend".equalsIgnoreCase(code)) {
                            detail.dividendPerShare = parseNumber(val);
                        } else if ("dividendAt".equalsIgnoreCase(code)) {
                            detail.dividendAt = val;
                        } else if ("exDividendAt".equalsIgnoreCase(code)) {
                            detail.exDividendAt = val;
                        }
                    }
                    return detail;
                }
            }
        } catch (Exception e) {
            log.debug("Basic detail not found for US symbol {}: {}", targetCode, e.getMessage());
        }
        return null;
    }

    private double parseNumber(String val) {
        if (val == null || val.isBlank() || "N/A".equalsIgnoreCase(val)) return 0.0;
        try {
            return Double.parseDouble(val.replace(",", "").replace("+", "").replace("%", "").replace("배", "").replace("원", "").replace("USD", "").trim());
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

    private static class StockDetailInfo {
        Double week52High;
        Double week52Low;
        Double per;
        Double eps;
        Double cnsPer;
        Double cnsEps;
        Double pbr;
        Double bps;
        Double dividendYieldRatio;
        Double dividendPerShare;
        String dividendAt;
        String exDividendAt;
        Double foreignExhaustionRatio;
    }
}
