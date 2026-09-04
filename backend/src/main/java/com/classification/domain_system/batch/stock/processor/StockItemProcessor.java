package com.classification.domain_system.batch.stock.processor;

import com.classification.domain_system.batch.stock.dto.StockApiRawItem;
import com.classification.domain_system.entity.ClassificationNode;
import com.classification.domain_system.entity.Record;
import com.classification.domain_system.service.RecordService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.infrastructure.item.ItemProcessor;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Slf4j
public class StockItemProcessor implements ItemProcessor<StockApiRawItem, Record> {

    private final RecordService recordService;
    private final Map<String, ClassificationNode> nodeMap;
    private final ClassificationNode defaultNode;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Record process(StockApiRawItem item) throws Exception {
        if (item == null || item.getTickerCode() == null) {
            return null;
        }

        String marketCode = item.getMarketNodeCode() != null ? item.getMarketNodeCode().toUpperCase() : "KOSPI";
        ClassificationNode targetNode = nodeMap.getOrDefault(marketCode, defaultNode);

        Map<String, Object> data = new HashMap<>();
        data.put("ticker_code", item.getTickerCode());
        data.put("isin_code", item.getIsinCode());
        data.put("stock_name", item.getStockName());
        data.put("stock_name_en", item.getStockNameEn());
        data.put("market_type", item.getMarketType());
        data.put("industry_sector", item.getIndustrySector());
        data.put("security_type", item.getSecurityType() != null ? item.getSecurityType() : "COMMON");
        data.put("par_value", item.getParValue());
        data.put("listed_shares", item.getListedShares());
        data.put("capital_amount", item.getCapitalAmount());
        data.put("currency", item.getCurrency() != null ? item.getCurrency() : "KRW");
        data.put("current_price", item.getCurrentPrice());
        data.put("previous_close_price", item.getPreviousClosePrice());
        data.put("market_cap", item.getMarketCap());
        data.put("week52_high", item.getWeek52High());
        data.put("week52_low", item.getWeek52Low());
        data.put("price_base_date", item.getPriceBaseDate());
        data.put("listing_date", item.getListingDate());
        data.put("fiscal_month", item.getFiscalMonth());
        data.put("is_trading_halt", item.getIsTradingHalt() != null ? item.getIsTradingHalt() : false);
        data.put("is_delisting_risk", item.getIsDelistingRisk() != null ? item.getIsDelistingRisk() : false);
        data.put("transfer_agent", item.getTransferAgent() != null ? item.getTransferAgent() : "KSD");
        data.put("settlement_cycle", item.getSettlementCycle() != null ? item.getSettlementCycle() : "T_PLUS_2");
        
        // Exact real investor trading metrics
        data.put("margin_balance_shares", item.getMarginBalanceShares());
        data.put("margin_balance_ratio", item.getMarginBalanceRatio());
        data.put("short_selling_balance_shares", item.getShortSellingBalanceShares());
        data.put("short_selling_ratio", item.getShortSellingRatio());
        data.put("is_short_selling_overheated", item.getIsShortSellingOverheated() != null ? item.getIsShortSellingOverheated() : false);
        data.put("foreign_ownership_ratio", item.getForeignOwnershipRatio());
        data.put("foreign_holding_shares", item.getForeignHoldingShares());
        data.put("foreign_daily_net_buy", item.getForeignDailyNetBuy());
        data.put("inst_daily_net_buy", item.getInstDailyNetBuy());
        data.put("retail_daily_net_buy", item.getRetailDailyNetBuy());
        data.put("foreign_cumulative_net_buy_20d", item.getForeignCumulativeNetBuy20d());
        data.put("inst_cumulative_net_buy_20d", item.getInstCumulativeNetBuy20d());
        data.put("retail_cumulative_net_buy_20d", item.getRetailCumulativeNetBuy20d());
        
        data.put("investor_relations_url", item.getInvestorRelationsUrl());
        data.put("business_summary", item.getBusinessSummary());

        String dataJson = objectMapper.writeValueAsString(data);

        Record record = new Record();
        record.setNode(targetNode);
        record.setStatus("ACTIVE");
        record.setSourceSystem("SPRING_BATCH_STOCK_INGESTION");
        record.setData(dataJson);
        record.setVersion(1);

        if (recordService != null && targetNode != null && targetNode.getId() != null) {
            String searchableText = recordService.generateSearchableData(targetNode.getId(), dataJson);
            record.setSearchableData(searchableText);
        } else {
            record.setSearchableData(item.getStockName() + " " + item.getTickerCode());
        }

        return record;
    }
}
