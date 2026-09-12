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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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

                            double rawOpen = parseNumber(s.path("openPriceRaw").asText(s.path("openPrice").asText("0")));
                            double rawHigh = parseNumber(s.path("highPriceRaw").asText(s.path("highPrice").asText("0")));
                            double rawLow = parseNumber(s.path("lowPriceRaw").asText(s.path("lowPrice").asText("0")));

                            double openPrice = (detail != null && detail.openPrice != null && detail.openPrice > 0)
                                    ? detail.openPrice : (rawOpen > 0 ? rawOpen : price);
                            double highPrice = (detail != null && detail.highPrice != null && detail.highPrice > 0)
                                    ? detail.highPrice : (rawHigh > 0 ? rawHigh : price);
                            double lowPrice = (detail != null && detail.lowPrice != null && detail.lowPrice > 0)
                                    ? detail.lowPrice : (rawLow > 0 ? rawLow : price);
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
                                    .listedShares(price > 0 ? (long) (mktCap / price) : 0L)
                                    .currency("USD")
                                    .currentPrice(price)
                                    .previousClosePrice(price)
                                    .marketCap(mktCap)
                                    .week52High(week52High)
                                    .week52Low(week52Low)
                                    .openPrice(openPrice)
                                    .highPrice(highPrice)
                                    .lowPrice(lowPrice)
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
                                    .isTradingHalt(false)
                                    .isDelistingRisk(false)
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

                            // 1. Investor trend
                            InvestorTrend trend = fetchInvestorTrend(ticker);
                            // 2. Integration detail (Naver)
                            StockDetailInfo detail = fetchDomesticStockDetail(ticker);
                            // 3. Daum Quote detail (ISIN, ParValue, Capital, ListingDate, FiscalMonth, ForeignHolding, BusinessSummary, WICS sector)
                            DaumQuoteInfo daum = fetchDaumQuoteDetail(ticker);

                            String isinCode = (daum != null && daum.isinCode != null && !daum.isinCode.isBlank())
                                    ? daum.isinCode : ("KR7" + ticker + "000");

                            // 4. KRX Short Selling (Balance Qty & Ratio)
                            KrxShortSellingInfo krx = fetchKrxShortSelling(isinCode);

                            Long foreignDaily = trend != null ? trend.foreignDaily : null;
                            Long instDaily = trend != null ? trend.instDaily : null;
                            Long retailDaily = trend != null ? trend.retailDaily : null;
                            Double foreignRatio = (daum != null && daum.foreignRatio != null)
                                    ? daum.foreignRatio
                                    : (trend != null ? trend.foreignRatio : null);

                            double week52High = (detail != null && detail.week52High != null && detail.week52High > 0)
                                    ? detail.week52High : price;
                            double week52Low = (detail != null && detail.week52Low != null && detail.week52Low > 0)
                                    ? detail.week52Low : price;

                            double rawOpen = parseNumber(s.path("openPriceRaw").asText(s.path("openPrice").asText("0")));
                            double rawHigh = parseNumber(s.path("highPriceRaw").asText(s.path("highPrice").asText("0")));
                            double rawLow = parseNumber(s.path("lowPriceRaw").asText(s.path("lowPrice").asText("0")));

                            double openPrice = (detail != null && detail.openPrice != null && detail.openPrice > 0)
                                    ? detail.openPrice : (rawOpen > 0 ? rawOpen : price);
                            double highPrice = (detail != null && detail.highPrice != null && detail.highPrice > 0)
                                    ? detail.highPrice : (rawHigh > 0 ? rawHigh : price);
                            double lowPrice = (detail != null && detail.lowPrice != null && detail.lowPrice > 0)
                                    ? detail.lowPrice : (rawLow > 0 ? rawLow : price);
                            double fluctuationRate = parseNumber(s.path("fluctuationsRatioRaw").asText(s.path("fluctuationsRatio").asText("0")));
                            long tradingValue = (long) parseNumber(s.path("accumulatedTradingValueRaw").asText("0"));
                            String logoUrl = s.path("itemLogoUrl").asText(s.path("itemLogoPngUrl").asText(null));

                            Double parValue = daum != null ? daum.parValue : null;
                            Long listedShares = (daum != null && daum.listedShares != null)
                                    ? daum.listedShares
                                    : (price > 0 ? (long) (mktCap / price) : null);
                            Long capitalAmount = daum != null ? daum.capitalAmount : null;
                            String industrySector = (daum != null && daum.industrySector != null && !daum.industrySector.isBlank())
                                    ? daum.industrySector
                                    : s.path("stockExchangeType").path("nameKor").asText(market.toUpperCase());
                            String listingDate = daum != null ? daum.listingDate : null;
                            String fiscalMonth = daum != null ? daum.fiscalMonth : null;
                            Long foreignHoldingShares = (daum != null && daum.foreignHoldingShares != null)
                                    ? daum.foreignHoldingShares
                                    : ((foreignRatio != null && listedShares != null) ? (long) (listedShares * (foreignRatio / 100.0)) : null);

                            Boolean isTradingHalt = (daum != null && daum.isTradingSuspended != null)
                                    ? daum.isTradingSuspended
                                    : (!"1".equals(s.path("tradeStopType").path("code").asText("1")));
                            Boolean isDelistingRisk = (daum != null && daum.isAdministrativeIssue != null)
                                    ? daum.isAdministrativeIssue
                                    : false;

                            String summary = (daum != null && daum.businessSummary != null && !daum.businessSummary.isBlank())
                                    ? daum.businessSummary
                                    : ("<p><strong>" + name + "</strong>(" + ticker + ")은 " + market + " 상장 기업입니다.</p>");

                            items.add(StockApiRawItem.builder()
                                    .marketNodeCode(market.toUpperCase())
                                    .tickerCode(ticker)
                                    .isinCode(isinCode)
                                    .stockName(name)
                                    .stockNameEn(s.path("reutersCode").asText(name))
                                    .marketType(market.toUpperCase())
                                    .industrySector(industrySector)
                                    .securityType(name.contains("ETF") || name.contains("KODEX") || name.contains("TIGER") ? "ETF" : "COMMON")
                                    .parValue(parValue)
                                    .listedShares(listedShares)
                                    .capitalAmount(capitalAmount)
                                    .currency("KRW")
                                    .currentPrice(price)
                                    .previousClosePrice(price > change ? price - change : price)
                                    .marketCap(mktCap)
                                    .week52High(week52High)
                                    .week52Low(week52Low)
                                    .openPrice(openPrice)
                                    .highPrice(highPrice)
                                    .lowPrice(lowPrice)
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
                                    .listingDate(listingDate)
                                    .fiscalMonth(fiscalMonth)
                                    .isTradingHalt(isTradingHalt)
                                    .isDelistingRisk(isDelistingRisk)
                                    .shortSellingBalanceShares(krx != null ? krx.balanceShares : null)
                                    .shortSellingRatio(krx != null ? krx.balanceRatio : null)
                                    .foreignOwnershipRatio(foreignRatio)
                                    .foreignHoldingShares(foreignHoldingShares)
                                    .foreignDailyNetBuy(foreignDaily)
                                    .instDailyNetBuy(instDaily)
                                    .retailDailyNetBuy(retailDaily)
                                    .foreignCumulativeNetBuy20d(trend != null ? trend.foreignCum20d : null)
                                    .instCumulativeNetBuy20d(trend != null ? trend.instCum20d : null)
                                    .retailCumulativeNetBuy20d(trend != null ? trend.retailCum20d : null)
                                    .businessSummary(summary)
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

                        double curPrice = r.get("current_price") != null ? ((Number) r.get("current_price")).doubleValue() : 0.0;
                        double opPrice = r.get("open_price") != null ? ((Number) r.get("open_price")).doubleValue() : curPrice;
                        double hiPrice = r.get("high_price") != null ? ((Number) r.get("high_price")).doubleValue() : curPrice;
                        double loPrice = r.get("low_price") != null ? ((Number) r.get("low_price")).doubleValue() : curPrice;

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
                                .currentPrice(curPrice)
                                .openPrice(opPrice > 0 ? opPrice : curPrice)
                                .highPrice(hiPrice > 0 ? hiPrice : curPrice)
                                .lowPrice(loPrice > 0 ? loPrice : curPrice)
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
                                .shortSellingBalanceShares(r.get("short_selling_balance_shares") != null ? ((Number) r.get("short_selling_balance_shares")).longValue() : null)
                                .shortSellingRatio(r.get("short_selling_ratio") != null ? ((Number) r.get("short_selling_ratio")).doubleValue() : null)
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
                        } else if ("openPrice".equalsIgnoreCase(code)) {
                            detail.openPrice = parseNumber(val);
                        } else if ("highPrice".equalsIgnoreCase(code)) {
                            detail.highPrice = parseNumber(val);
                        } else if ("lowPrice".equalsIgnoreCase(code)) {
                            detail.lowPrice = parseNumber(val);
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
                        } else if ("openPrice".equalsIgnoreCase(code)) {
                            detail.openPrice = parseNumber(val);
                        } else if ("highPrice".equalsIgnoreCase(code)) {
                            detail.highPrice = parseNumber(val);
                        } else if ("lowPrice".equalsIgnoreCase(code)) {
                            detail.lowPrice = parseNumber(val);
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

    private DaumQuoteInfo fetchDaumQuoteDetail(String ticker) {
        try {
            String url = "https://finance.daum.net/api/quotes/A" + ticker;
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", USER_AGENT);
            headers.set("Referer", "https://finance.daum.net/quotes/A" + ticker);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                DaumQuoteInfo info = new DaumQuoteInfo();
                info.isinCode = root.path("code").asText(null);
                if (root.hasNonNull("parValue")) {
                    info.parValue = root.path("parValue").asDouble();
                }
                if (root.hasNonNull("listedShareCount")) {
                    info.listedShares = root.path("listedShareCount").asLong();
                }
                if (root.hasNonNull("capitalStock")) {
                    info.capitalAmount = (long) root.path("capitalStock").asDouble();
                }
                info.industrySector = root.path("wicsSectorName").asText(null);
                info.listingDate = root.path("listingDate").asText(null);
                if (root.hasNonNull("settleMonth")) {
                    info.fiscalMonth = String.valueOf(root.path("settleMonth").asInt());
                }
                if (root.hasNonNull("foreignRatio")) {
                    double raw = root.path("foreignRatio").asDouble();
                    info.foreignRatio = Math.round(raw * 10000.0) / 100.0;
                }
                if (root.hasNonNull("foreignOwnShares")) {
                    info.foreignHoldingShares = root.path("foreignOwnShares").asLong();
                }
                info.businessSummary = root.path("companySummary").asText(null);

                JsonNode stockState = root.path("stockState");
                if (!stockState.isMissingNode()) {
                    if (stockState.has("isAdministrativeIssue")) {
                        info.isAdministrativeIssue = stockState.path("isAdministrativeIssue").asBoolean();
                    }
                    if (stockState.has("isTradingSuspended")) {
                        info.isTradingSuspended = stockState.path("isTradingSuspended").asBoolean();
                    }
                }
                return info;
            }
        } catch (Exception e) {
            log.debug("Daum quote detail not found for ticker {}: {}", ticker, e.getMessage());
        }
        return null;
    }

    private KrxShortSellingInfo fetchKrxShortSelling(String isinCode) {
        if (isinCode == null || isinCode.isBlank()) {
            return null;
        }
        try {
            String url = "https://data.krx.co.kr/comm/bldAttendant/getJsonData.cmd";
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", USER_AGENT);
            headers.set("Referer", "https://data.krx.co.kr");
            headers.set("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");

            LocalDate now = LocalDate.now();
            String endDd = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String strtDd = now.minusDays(14).format(DateTimeFormatter.ofPattern("yyyyMMdd"));

            String body = "bld=" + URLEncoder.encode("dbms/MDC_OUT/STAT/srt/MDCSTAT30502_OUT", StandardCharsets.UTF_8)
                    + "&locale=ko_KR&isuCd=" + URLEncoder.encode(isinCode, StandardCharsets.UTF_8)
                    + "&strtDd=" + strtDd + "&endDd=" + endDd;

            HttpEntity<String> entity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode output = root.path("output");
                if (!output.isArray() || output.isEmpty()) {
                    output = root.path("OutBlock_1");
                }
                if (!output.isArray() || output.isEmpty()) {
                    output = root.path("block1");
                }
                if (output.isArray() && output.size() > 0) {
                    JsonNode latest = output.get(0);
                    KrxShortSellingInfo info = new KrxShortSellingInfo();
                    String balQtyStr = latest.path("BAL_QTY").asText(null);
                    String balRtoStr = latest.path("BAL_RTO").asText(null);
                    if (balQtyStr != null) {
                        info.balanceShares = (long) parseNumber(balQtyStr);
                    }
                    if (balRtoStr != null) {
                        info.balanceRatio = parseNumber(balRtoStr);
                    }
                    return info;
                }
            }
        } catch (Exception e) {
            log.debug("KRX short selling info not found for ISIN {}: {}", isinCode, e.getMessage());
        }
        return null;
    }

    private static class DaumQuoteInfo {
        String isinCode;
        Double parValue;
        Long listedShares;
        Long capitalAmount;
        String industrySector;
        String listingDate;
        String fiscalMonth;
        Double foreignRatio;
        Long foreignHoldingShares;
        String businessSummary;
        Boolean isAdministrativeIssue;
        Boolean isTradingSuspended;
    }

    private static class KrxShortSellingInfo {
        Long balanceShares;
        Double balanceRatio;
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
        Double openPrice;
        Double highPrice;
        Double lowPrice;
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
