package com.classification.domain_system.batch.stock.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockApiRawItem {
    private String marketNodeCode;   // KOSPI, KOSDAQ, KONEX, US_MARKET, ASIA_MARKET, EUROPE_MARKET
    private String tickerCode;       // e.g. 005930, AAPL
    private String isinCode;         // e.g. KR7005930003, US0378331005
    private String stockName;        // e.g. 삼성전자, Apple
    private String stockNameEn;      // e.g. Samsung Electronics
    private String marketType;       // e.g. KOSPI, NASDAQ
    private String industrySector;   // e.g. 전기전자, IT
    private String securityType;     // COMMON, PREFERRED, ETF
    private Double parValue;         // 액면가
    private Long listedShares;       // 상장주식수
    private Long capitalAmount;      // 자본금
    private String currency;         // KRW, USD
    private Double currentPrice;     // 최근 기준가/종가
    private Double previousClosePrice;// 전일 종가
    private Long marketCap;          // 시가총액
    private Double week52High;       // 52주 최고가
    private Double week52Low;        // 52주 최저가
    private String priceBaseDate;    // 기준일자
    private String listingDate;      // 상장일자
    private String fiscalMonth;      // 결산월
    private Boolean isTradingHalt;   // 거래정지 여부
    private Boolean isDelistingRisk; // 관리/상장폐지 위험 여부
    private String transferAgent;    // 명의개서대행기관
    private String settlementCycle;  // 결제주기
    private Long marginBalanceShares;// 신용잔고수량
    private Double marginBalanceRatio;// 신용잔고율
    private Long shortSellingBalanceShares; // 공매도잔고수량
    private Double shortSellingRatio;       // 공매도비율
    private Boolean isShortSellingOverheated;// 공매도과열 여부
    private Double foreignOwnershipRatio;   // 외국인 지분율
    private Long foreignHoldingShares;      // 외국인 보유주식수
    private Long foreignDailyNetBuy;        // 외국인 당일 순매수량
    private Long instDailyNetBuy;           // 기관 당일 순매수량
    private Long retailDailyNetBuy;         // 개인 당일 순매수량
    private Long foreignCumulativeNetBuy20d;// 외국인 20일 누적순매수량
    private Long instCumulativeNetBuy20d;   // 기관 20일 누적순매수량
    private Long retailCumulativeNetBuy20d; // 개인 20일 누적순매수량
    private String investorRelationsUrl;    // IR / 종목상세 URL
    private String businessSummary;         // 기업 개요
}
