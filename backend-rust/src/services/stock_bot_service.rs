use crate::error::AppResult;
use crate::models::chat::*;
use crate::repositories::chat_repo::ChatRepository;
use crate::services::chat_service::ChatService;
use reqwest::Client;
use serde_json::Value;
use sqlx::PgPool;
use std::collections::HashMap;
use std::sync::Arc;
use std::time::{Duration, Instant};
use tokio::sync::broadcast;
use tokio::sync::RwLock;
use uuid::Uuid;

fn format_krw(val: f64) -> String {
    let s = format!("{:.0}", val);
    let is_neg = s.starts_with('-');
    let digits = if is_neg { &s[1..] } else { &s[..] };
    let mut out = String::new();
    let mut count = 0;
    for c in digits.chars().rev() {
        if count > 0 && count % 3 == 0 {
            out.push(',');
        }
        out.push(c);
        count += 1;
    }
    if is_neg {
        out.push('-');
    }
    let formatted: String = out.chars().rev().collect();
    format!("{}원", formatted)
}

#[derive(Default)]
struct StockDataCache {
    today: Option<(Instant, Value)>,
    rankings: Option<(Instant, Value)>,
    screen: Option<(Instant, Vec<Value>)>,
    disclosures: Option<(Instant, Vec<Value>)>,
    disclosure_impact: Option<(Instant, Value)>,
    stock_profiles: HashMap<String, (Instant, Value)>,
}

#[derive(Clone)]
pub struct StockBotService {
    pool: PgPool,
    client: Client,
    cache: Arc<RwLock<StockDataCache>>,
    broadcast_tx: broadcast::Sender<String>,
}

impl StockBotService {
    pub const BOT_USER_ID: &'static str = "AI_STOCK_BOT";
    pub const BOT_NAME: &'static str = "AI 주식 비서";

    pub fn new(pool: PgPool, broadcast_tx: broadcast::Sender<String>) -> Self {
        let client = Client::builder()
            .timeout(Duration::from_secs(10))
            .user_agent("MDM-Platform-StockBot/1.0 (aikstockdata integration)")
            .build()
            .unwrap_or_default();

        Self {
            pool,
            client,
            cache: Arc::new(RwLock::new(StockDataCache::default())),
            broadcast_tx,
        }
    }

    // -------------------------------------------------------------
    // Data Fetching with In-Memory Caching (aikstockdata.com endpoints)
    // -------------------------------------------------------------

    pub async fn fetch_today(&self) -> Result<Value, String> {
        {
            let cache = self.cache.read().await;
            if let Some((cached_at, val)) = &cache.today {
                if cached_at.elapsed() < Duration::from_secs(300) {
                    return Ok(val.clone());
                }
            }
        }

        let url = "https://aikstockdata.com/data/public/today.json";
        let resp = self
            .client
            .get(url)
            .send()
            .await
            .map_err(|e| format!("aikstockdata today.json 요청 실패: {}", e))?;

        if !resp.status().is_success() {
            return Err(format!("aikstockdata today.json HTTP 오류: {}", resp.status()));
        }

        let val: Value = resp
            .json()
            .await
            .map_err(|e| format!("today.json 파싱 실패: {}", e))?;

        let mut cache = self.cache.write().await;
        cache.today = Some((Instant::now(), val.clone()));
        Ok(val)
    }

    pub async fn fetch_rankings(&self) -> Result<Value, String> {
        {
            let cache = self.cache.read().await;
            if let Some((cached_at, val)) = &cache.rankings {
                if cached_at.elapsed() < Duration::from_secs(600) {
                    return Ok(val.clone());
                }
            }
        }

        let url = "https://aikstockdata.com/data/public/rankings.json";
        let resp = self
            .client
            .get(url)
            .send()
            .await
            .map_err(|e| format!("rankings.json 요청 실패: {}", e))?;

        if !resp.status().is_success() {
            return Err(format!("rankings.json HTTP 오류: {}", resp.status()));
        }

        let val: Value = resp
            .json()
            .await
            .map_err(|e| format!("rankings.json 파싱 실패: {}", e))?;

        let mut cache = self.cache.write().await;
        cache.rankings = Some((Instant::now(), val.clone()));
        Ok(val)
    }

    pub async fn fetch_screen(&self) -> Result<Vec<Value>, String> {
        {
            let cache = self.cache.read().await;
            if let Some((cached_at, list)) = &cache.screen {
                if cached_at.elapsed() < Duration::from_secs(1800) {
                    return Ok(list.clone());
                }
            }
        }

        let url = "https://aikstockdata.com/data/public/screen.json";
        let resp = self
            .client
            .get(url)
            .send()
            .await
            .map_err(|e| format!("screen.json 요청 실패: {}", e))?;

        if !resp.status().is_success() {
            return Err(format!("screen.json HTTP 오류: {}", resp.status()));
        }

        let json_val: Value = resp
            .json()
            .await
            .map_err(|e| format!("screen.json 파싱 실패: {}", e))?;

        let fields: Vec<String> = json_val
            .get("fields")
            .and_then(|v| v.as_array())
            .map(|arr| arr.iter().filter_map(|x| x.as_str().map(String::from)).collect())
            .unwrap_or_default();

        let mut list = Vec::new();
        if let Some(rows) = json_val.get("rows").and_then(|v| v.as_array()) {
            for row in rows {
                if let Some(vals) = row.as_array() {
                    let mut obj = serde_json::Map::new();
                    for (i, field_name) in fields.iter().enumerate() {
                        if let Some(val) = vals.get(i) {
                            obj.insert(field_name.clone(), val.clone());
                        }
                    }
                    list.push(Value::Object(obj));
                }
            }
        }

        let mut cache = self.cache.write().await;
        cache.screen = Some((Instant::now(), list.clone()));
        Ok(list)
    }

    pub async fn fetch_disclosures(&self) -> Result<Vec<Value>, String> {
        {
            let cache = self.cache.read().await;
            if let Some((cached_at, list)) = &cache.disclosures {
                if cached_at.elapsed() < Duration::from_secs(300) {
                    return Ok(list.clone());
                }
            }
        }

        let url = "https://aikstockdata.com/data/public/disclosures_top100.json";
        let resp = self
            .client
            .get(url)
            .send()
            .await
            .map_err(|e| format!("disclosures_top100.json 요청 실패: {}", e))?;

        if !resp.status().is_success() {
            return Err(format!("disclosures_top100.json HTTP 오류: {}", resp.status()));
        }

        let json_val: Value = resp
            .json()
            .await
            .map_err(|e| format!("disclosures_top100.json 파싱 실패: {}", e))?;

        let list = if let Some(arr) = json_val.as_array() {
            arr.clone()
        } else if let Some(arr) = json_val.get("disclosures").and_then(|v| v.as_array()) {
            arr.clone()
        } else {
            vec![]
        };

        let mut cache = self.cache.write().await;
        cache.disclosures = Some((Instant::now(), list.clone()));
        Ok(list)
    }

    pub async fn fetch_disclosure_impact(&self) -> Result<Value, String> {
        {
            let cache = self.cache.read().await;
            if let Some((cached_at, val)) = &cache.disclosure_impact {
                if cached_at.elapsed() < Duration::from_secs(1800) {
                    return Ok(val.clone());
                }
            }
        }

        let url = "https://aikstockdata.com/data/public/disclosure_impact.json";
        let resp = self
            .client
            .get(url)
            .send()
            .await
            .map_err(|e| format!("disclosure_impact.json 요청 실패: {}", e))?;

        if !resp.status().is_success() {
            return Err(format!("disclosure_impact.json HTTP 오류: {}", resp.status()));
        }

        let val: Value = resp
            .json()
            .await
            .map_err(|e| format!("disclosure_impact.json 파싱 실패: {}", e))?;

        let mut cache = self.cache.write().await;
        cache.disclosure_impact = Some((Instant::now(), val.clone()));
        Ok(val)
    }

    pub async fn fetch_stock_profile(&self, ticker: &str) -> Result<Value, String> {
        {
            let cache = self.cache.read().await;
            if let Some((cached_at, val)) = cache.stock_profiles.get(ticker) {
                if cached_at.elapsed() < Duration::from_secs(300) {
                    return Ok(val.clone());
                }
            }
        }

        let url = format!("https://aikstockdata.com/data/public/s/{}.json", ticker);
        let resp = self
            .client
            .get(&url)
            .send()
            .await
            .map_err(|e| format!("종목({}) 상세 조회 실패: {}", ticker, e))?;

        if !resp.status().is_success() {
            return Err(format!("종목({}) 데이터를 찾을 수 없습니다 (HTTP {})", ticker, resp.status()));
        }

        let val: Value = resp
            .json()
            .await
            .map_err(|e| format!("종목({}) 파싱 실패: {}", ticker, e))?;

        let mut cache = self.cache.write().await;
        cache
            .stock_profiles
            .insert(ticker.to_string(), (Instant::now(), val.clone()));
        Ok(val)
    }

    // -------------------------------------------------------------
    // Query Analysis & Natural Language Routing (MCP Capabilities)
    // -------------------------------------------------------------

    pub async fn process_query(&self, raw_query: &str) -> String {
        let q = raw_query.trim();
        if q.is_empty() {
            return self.format_help();
        }

        let q_lower = q.to_lowercase();

        // 1. Help / Guide
        if q_lower == "도움말"
            || q_lower == "help"
            || q_lower == "?"
            || q_lower.contains("기능")
            || q_lower.contains("안내")
            || q_lower.contains("사용법")
            || q_lower.contains("질문 예시")
        {
            return self.format_help();
        }

        // 2. Today Market Briefing
        if (q_lower.contains("오늘") && (q_lower.contains("시장") || q_lower.contains("증시") || q_lower.contains("브리핑") || q_lower.contains("시황")))
            || q_lower == "시장 브리핑"
            || q_lower == "증시 브리핑"
            || q_lower == "시장 요약"
            || q_lower == "오늘 시장"
            || q_lower == "코스피"
            || q_lower == "코스닥"
        {
            return self.handle_today_briefing().await;
        }

        // 3. Disclosure Impact Statistics (공시 영향 분석)
        if q_lower.contains("영향")
            || q_lower.contains("통계")
            || q_lower.contains("다음날 주가")
            || q_lower.contains("공시 효과")
            || (q_lower.contains("공시") && (q_lower.contains("확률") || q_lower.contains("수익률")))
        {
            return self.handle_disclosure_impact(&q_lower).await;
        }

        // 4. Rankings (순위, 급성장 기업, 신고가)
        if q_lower.contains("랭킹")
            || q_lower.contains("순위")
            || q_lower.contains("급성장")
            || q_lower.contains("신고가")
            || q_lower.contains("신저가")
            || q_lower.contains("조용한 강자")
            || q_lower == "top"
        {
            return self.handle_rankings(&q_lower).await;
        }

        // 5. Earnings / Results Calendar (실적 발표 일정)
        if q_lower.contains("실적")
            && (q_lower.contains("발표") || q_lower.contains("일정") || q_lower.contains("캘린더") || q_lower.contains("잠정"))
        {
            return self.handle_earnings_calendar().await;
        }

        // 6. General Disclosures (공시 / DART)
        if (q_lower.contains("공시") || q_lower.contains("dart") || q_lower.contains("보고서"))
            && !q_lower.contains("영향")
        {
            // If a specific stock is also mentioned in the disclosure query, let stock search handle it or filter disclosures
            if let Some(ticker) = self.find_ticker_in_query(q).await {
                return self.handle_stock_detail(&ticker, Some(q)).await;
            }
            return self.handle_disclosures().await;
        }

        // 7. Individual Stock Lookup / Analysis
        if let Some(ticker) = self.find_ticker_in_query(q).await {
            return self.handle_stock_detail(&ticker, Some(q)).await;
        }

        // 8. Fallback: Search screen universe
        if let Ok(stocks) = self.search_screen(q).await {
            if !stocks.is_empty() {
                if stocks.len() == 1 {
                    if let Some(code) = stocks[0].get("code").and_then(|c| c.as_str()) {
                        return self.handle_stock_detail(code, Some(q)).await;
                    }
                }
                return self.format_stock_search_results(q, &stocks);
            }
        }

        // 9. If no matching intent, return guidance with suggestions
        self.format_not_found(q)
    }

    async fn find_ticker_in_query(&self, q: &str) -> Option<String> {
        // Direct 6-digit number match
        let words: Vec<&str> = q.split(|c: char| c.is_whitespace() || c == ',' || c == '.' || c == ':').collect();
        for word in &words {
            let clean = word.trim();
            if clean.len() == 6 && clean.chars().all(|c| c.is_ascii_digit()) {
                return Some(clean.to_string());
            }
        }

        // Search screen.json for matching names
        if let Ok(screen_list) = self.fetch_screen().await {
            let mut matches: Vec<(String, String)> = Vec::new();
            for item in screen_list {
                let name = item.get("name").and_then(|n| n.as_str()).unwrap_or("");
                let code = item.get("code").and_then(|c| c.as_str()).unwrap_or("");

                if !name.is_empty() && q.contains(name) {
                    matches.push((name.to_string(), code.to_string()));
                }
            }

            // Sort matches: longest name first (e.g. "LG디스플레이" [6 chars] before "LG" [2 chars])
            matches.sort_by(|a, b| b.0.chars().count().cmp(&a.0.chars().count()));

            if let Some((_, code)) = matches.first() {
                return Some(code.clone());
            }
        }

        None
    }

    async fn search_screen(&self, q: &str) -> Result<Vec<Value>, String> {
        let list = self.fetch_screen().await?;
        let q_clean = q.trim().to_lowercase();

        let mut matches = Vec::new();
        for item in list {
            let name = item.get("name").and_then(|n| n.as_str()).unwrap_or("").to_lowercase();
            let code = item.get("code").and_then(|c| c.as_str()).unwrap_or("");

            if name.contains(&q_clean) || code.contains(&q_clean) {
                matches.push(item);
                if matches.len() >= 10 {
                    break;
                }
            }
        }

        Ok(matches)
    }

    // -------------------------------------------------------------
    // Response Formatters (Rich Markdown & Tables)
    // -------------------------------------------------------------

    pub fn format_help(&self) -> String {
        r#"### 🤖 AI 주식 비서 (aikstockdata) 안내

안녕하세요! 한국 증시(KOSPI, KOSDAQ) 공공 데이터 및 DART 공시 분석 비서입니다.
원하시는 정보에 대해 자연어로 질문하시면 즉시 분석해 드립니다.

---

#### 💡 추천 질문 예시
1. **📊 오늘 증시 브리핑**
   - *"오늘 시장 어때?"*, *"코스피 지수 현황"*, *"오늘 증시 요약"*
2. **📈 개별 종목 실적 및 밸류에이션**
   - *"삼성전자 실적 분석"*, *"SK하이닉스 주가 및 지표"*, *"005930"*
3. **🏆 시장 랭킹 & 순위**
   - *"급성장 기업 순위"*, *"52주 신고가 종목"*, *"조용한 강자 Top 5"*
4. **📢 DART 전자공시**
   - *"최근 주요 공시 알려줘"*, *"오늘의 DART 공시"*
5. **📊 공시 유형별 주가 변동 통계 (DART Impact)**
   - *"공급계약 공시 후 주가 영향은?"*, *"유상증자 공시 통계"*, *"무상증자 다음날 주가"*
6. **📅 실적 발표 캘린더**
   - *"실적 발표 일정"*, *"잠정실적 발표"*

> ℹ️ **데이터 출처 & 고지**:
> 본 서비스의 데이터는 **aikstockdata.com**을 통해 제공되는 DART 전자공시 및 금융위원회 공공데이터에 기반합니다.
> 모든 데이터는 기준일(As-of) 기준의 사실 요약이며, 투자 권유나 종목 추천이 아닙니다."#.to_string()
    }

    async fn handle_today_briefing(&self) -> String {
        let data = match self.fetch_today().await {
            Ok(d) => d,
            Err(e) => return format!("⚠️ 시장 데이터를 불러오지 못했습니다: {}", e),
        };

        let as_of = data.get("as_of_iso").and_then(|v| v.as_str()).unwrap_or("최근 거래일");
        let gen_time = data.get("generated_kst").and_then(|v| v.as_str()).unwrap_or("");

        let mut md = format!("### 📊 오늘의 국내 증시 다이제스트 ({})\n\n", as_of);

        // 1. Market Index (KOSPI, KOSDAQ)
        if let Some(indices) = data.get("market_index").and_then(|v| v.as_object()) {
            md.push_str("#### 🏛️ 주요 지수 현황\n");
            md.push_str("| 지수명 | 종가 | 전일 대비 | 등락률 | 구성 종목수 |\n");
            md.push_str("| :--- | :---: | :---: | :---: | :---: |\n");

            for (key, idx) in indices {
                let close = idx.get("close").and_then(|v| v.as_f64()).unwrap_or(0.0);
                let change = idx.get("change").and_then(|v| v.as_f64()).unwrap_or(0.0);
                let pct = idx.get("change_pct").and_then(|v| v.as_f64()).unwrap_or(0.0);
                let constituents = idx.get("constituents").and_then(|v| v.as_f64()).unwrap_or(0.0) as i64;

                let sign = if change > 0.0 { "▲" } else if change < 0.0 { "▼" } else { "-" };
                let color_pct = format!("{} {:.2}%", sign, pct);

                md.push_str(&format!(
                    "| **{}** | **{:.2}** | {}{:.2} | {} | {}개 |\n",
                    key, close, sign, change.abs(), color_pct, constituents
                ));
            }
            md.push('\n');
        }

        // 2. Market Breadth (등락 종목수)
        if let Some(breadth) = data.get("market_breadth") {
            let up = breadth.get("up").and_then(|v| v.as_i64()).unwrap_or(0);
            let down = breadth.get("down").and_then(|v| v.as_i64()).unwrap_or(0);
            let flat = breadth.get("flat").and_then(|v| v.as_i64()).unwrap_or(0);
            let total = breadth.get("total").and_then(|v| v.as_i64()).unwrap_or(0);
            let tone = breadth.get("tone").and_then(|v| v.as_str()).unwrap_or("");
            let advance_ratio = breadth.get("advance_ratio_ex_flat_pct").and_then(|v| v.as_f64()).unwrap_or(0.0);

            md.push_str("#### 🌡️ 시장 온도 및 등락 분포\n");
            md.push_str(&format!(
                "- **시장 분위기**: `{}` (상승 비율: {:.1}%)\n",
                tone, advance_ratio
            ));
            md.push_str(&format!(
                "- **종목 수**: 상승 `{}개` | 하락 `{}개` | 보합 `{}개` (총 {}개)\n\n",
                up, down, flat, total
            ));
        }

        // 3. Top Gainers
        if let Some(movers) = data.get("movers_up").and_then(|v| v.as_array()) {
            if !movers.is_empty() {
                md.push_str("#### 🚀 상승률 상위 종목 (Top 5)\n");
                md.push_str("| 종목명 | 코드 | 등락률 |\n");
                md.push_str("| :--- | :---: | :---: |\n");
                for item in movers.iter().take(5) {
                    let name = item.get("name").and_then(|v| v.as_str()).unwrap_or("");
                    let code = item.get("code").and_then(|v| v.as_str()).unwrap_or("");
                    let pct = item.get("change_pct").and_then(|v| v.as_f64()).unwrap_or(0.0);
                    md.push_str(&format!("| **{}** | `{}` | ▲ +{:.2}% |\n", name, code, pct));
                }
                md.push('\n');
            }
        }

        // 4. Growth Top 3
        if let Some(growth) = data.get("growth_top3").and_then(|v| v.as_array()) {
            if !growth.is_empty() {
                md.push_str("#### 🌟 실적 성장 Top 3\n");
                for (i, item) in growth.iter().enumerate() {
                    let name = item.get("name").and_then(|v| v.as_str()).unwrap_or("");
                    let code = item.get("code").and_then(|v| v.as_str()).unwrap_or("");
                    let op_yoy = item.get("영업이익YoY%").and_then(|v| v.as_f64()).unwrap_or(0.0);
                    let rev_yoy = item.get("매출YoY%").and_then(|v| v.as_f64()).unwrap_or(0.0);
                    let opm = item.get("OPM%").and_then(|v| v.as_f64()).unwrap_or(0.0);

                    md.push_str(&format!(
                        "{}. **{}** (`{}`) — 영업이익 YoY **+{:.1}%**, 매출 YoY **+{:.1}%**, 영업이익률 **{:.1}%**\n",
                        i + 1, name, code, op_yoy, rev_yoy, opm
                    ));
                }
                md.push('\n');
            }
        }

        // 5. Top Disclosures
        if let Some(disclosures) = data.get("top_disclosures").and_then(|v| v.as_array()) {
            if !disclosures.is_empty() {
                md.push_str("#### 📢 오늘의 핵심 DART 공시\n");
                for item in disclosures.iter().take(4) {
                    let name = item.get("name").and_then(|v| v.as_str()).unwrap_or("");
                    let label = item.get("label").and_then(|v| v.as_str()).unwrap_or("");
                    let fact = item.get("fact").and_then(|v| v.as_str()).unwrap_or("");
                    let score = item.get("score").and_then(|v| v.as_i64()).unwrap_or(0);

                    md.push_str(&format!(
                        "- **{}** [`{}`] (중요도: {}점): {}\n",
                        name, label, score, fact
                    ));
                }
                md.push('\n');
            }
        }

        md.push_str(&format!(
            "> 📌 *집계 시점: {} (aikstockdata.com)*\n> *본 자료는 추천이 아니며 공공 데이터에 기반한 사실 요약입니다.*",
            gen_time
        ));

        md
    }

    async fn handle_stock_detail(&self, ticker: &str, user_query: Option<&str>) -> String {
        let profile = match self.fetch_stock_profile(ticker).await {
            Ok(p) => p,
            Err(e) => return format!("⚠️ 종목 분석 정보를 가져오지 못했습니다: {}", e),
        };

        let name = profile.get("name_ko").and_then(|v| v.as_str()).unwrap_or(ticker);
        let code = profile.get("code").and_then(|v| v.as_str()).unwrap_or(ticker);
        let market = profile.get("market").and_then(|v| v.as_str()).unwrap_or("");

        let mut md = format!("### 📈 {} (`{}`) 기업 분석 리포트\n\n", name, code);

        // 1. Overview Table
        md.push_str("#### 📌 기본 개요 및 시장 정보\n");
        md.push_str("| 구분 | 내용 | 구분 | 내용 |\n");
        md.push_str("| :--- | :--- | :--- | :--- |\n");

        let sec_class = profile.get("security_class").and_then(|v| v.as_str()).unwrap_or("보통주");
        md.push_str(&format!("| **시장 구분** | {} | **증권 구분** | {} |\n", market, sec_class));

        // 2. Quote & Valuation
        let quote = profile.get("quote");
        let val = profile.get("valuation");

        let cur_price = quote.and_then(|q| q.get("close")).and_then(|v| v.as_f64()).map(format_krw).unwrap_or_else(|| "-".to_string());
        let change_pct = quote.and_then(|q| q.get("change_pct")).and_then(|v| v.as_f64()).map(|v| format!("{:+.2}%", v)).unwrap_or_else(|| "-".to_string());
        let mkt_cap = quote.and_then(|q| q.get("market_cap_krw")).and_then(|v| v.as_f64()).map(|v| {
            let eok = v / 100_000_000.0;
            if eok >= 10000.0 {
                format!("{:.1}조원", eok / 10000.0)
            } else {
                format!("{:.0}억원", eok)
            }
        }).unwrap_or_else(|| "-".to_string());

        let per = val.and_then(|v| v.get("pe_ttm")).and_then(|v| v.as_f64()).map(|v| format!("{:.2}배", v)).unwrap_or_else(|| "-".to_string());
        let pbr = val.and_then(|v| v.get("pb")).and_then(|v| v.as_f64()).map(|v| format!("{:.2}배", v)).unwrap_or_else(|| "-".to_string());

        md.push_str(&format!("| **현재 종가** | **{}** ({}) | **시가총액** | **{}** |\n", cur_price, change_pct, mkt_cap));
        md.push_str(&format!("| **PER (TTM)** | {} | **PBR** | {} |\n\n", per, pbr));

        // 3. Financials (실적 지표)
        if let Some(fin) = profile.get("financials") {
            let period_ko = fin.get("period_ko").and_then(|v| v.as_str()).unwrap_or("최신 공시");
            let rev_yoy = fin.get("revenue").and_then(|r| r.get("yoy_pct")).and_then(|v| v.as_f64()).map(|v| format!("{:+.1}%", v)).unwrap_or_else(|| "-".to_string());
            let op_yoy = fin.get("operating_income").and_then(|r| r.get("yoy_pct")).and_then(|v| v.as_f64()).map(|v| format!("{:+.1}%", v)).unwrap_or_else(|| "-".to_string());
            let net_yoy = fin.get("net_income").and_then(|r| r.get("yoy_pct")).and_then(|v| v.as_f64()).map(|v| format!("{:+.1}%", v)).unwrap_or_else(|| "-".to_string());

            md.push_str("#### 📊 DART 정기보고서 실적 (실측치)\n");
            md.push_str(&format!("- **실적 기준**: `{}` ({})\n", period_ko, fin.get("basis").and_then(|v| v.as_str()).unwrap_or("연결")));
            md.push_str(&format!("- **매출액 YoY**: `{}`\n", rev_yoy));
            md.push_str(&format!("- **영업이익 YoY**: `{}`\n", op_yoy));
            md.push_str(&format!("- **당기순이익 YoY**: `{}`\n\n", net_yoy));
        }

        // 4. Machine Signals
        if let Some(sig) = profile.get("signals") {
            let growth = sig.get("growth_top8").and_then(|v| v.as_bool()).unwrap_or(false);
            let quiet = sig.get("quiet_top").and_then(|v| v.as_bool()).unwrap_or(false);
            let g_score = sig.get("growth_score").and_then(|v| v.as_f64()).map(|s| format!("{:.1}점", s)).unwrap_or_else(|| "일반".to_string());

            md.push_str("#### 🏆 기계 산정 평가 시그널\n");

            if growth {
                md.push_str(&format!("- **급성장 랭킹 Top 8 편입**: ✅ 선정 (성장 점수: `{}`)\n", g_score));
            }
            if quiet {
                md.push_str("- **조용한 강자(저평가 우량주) 편입**: ✅ 선정\n");
            }
            if !growth && !quiet {
                md.push_str(&format!("- **성장 점수**: {}\n", g_score));
            }
            md.push('\n');
        }

        // 5. Recent DART Signals or Disclosures
        if let Some(disclosures) = profile.get("recent_disclosures").and_then(|v| v.as_array()) {
            if !disclosures.is_empty() {
                md.push_str("#### 📢 최근 DART 전자공시\n");
                for d in disclosures.iter().take(3) {
                    let label = d.get("label").and_then(|v| v.as_str()).unwrap_or("");
                    let date = d.get("date").and_then(|v| v.as_str()).unwrap_or("");
                    let summary = d.get("summary").and_then(|v| v.as_str()).unwrap_or("");
                    md.push_str(&format!("- `{}` **{}**: {}\n", date, label, summary));
                }
                md.push('\n');
            }
        }

        // 6. Short selling / 공매도 안내 if requested
        if let Some(q) = user_query {
            if q.contains("공매도") {
                md.push_str("#### 💡 공매도 및 수급 안내\n");
                md.push_str("- 본 분석기는 DART 전자공시 확정 재무제표, 밸류에이션(PER/PBR), 52주 변동률 및 AI 팩터 스코어를 제공합니다.\n");
                md.push_str("- 실시간 일별 공매도 거래대금 및 잔고 수량은 **KRX 한국거래소 공매도 종합포털(short.krx.co.kr)** 공시를 함께 참조하시기 바랍니다.\n\n");
            }
        }

        md.push_str("> ⚠️ **투자 유의사항**: 위 데이터는 aikstockdata.com 확정치(T+1) 및 DART 공시 기반의 통계 정보이며, 미래 수익률을 보장하지 않습니다.");
        md
    }

    async fn handle_rankings(&self, q: &str) -> String {
        let rankings = match self.fetch_rankings().await {
            Ok(r) => r,
            Err(e) => return format!("⚠️ 랭킹 데이터를 가져오지 못했습니다: {}", e),
        };

        let as_of = rankings.get("as_of_iso").and_then(|v| v.as_str()).unwrap_or("최근");

        if q.contains("신고가") || q.contains("52주") {
            let mut md = format!("### 🏆 52주 신고가 및 신저가 현황 ({})\n\n", as_of);
            if let Some(hi52) = rankings.get("hi52") {
                if let Some(highs) = hi52.get("high").and_then(|v| v.as_array()) {
                    md.push_str("#### 🔝 52주 신고가 기록 종목 (상위 5건)\n");
                    md.push_str("| 종목명 | 코드 | 시장 | 신고가 |\n");
                    md.push_str("| :--- | :---: | :---: | :---: |\n");
                    for item in highs.iter().take(5) {
                        let name = item.get("name").and_then(|v| v.as_str()).unwrap_or("");
                        let code = item.get("code").and_then(|v| v.as_str()).unwrap_or("");
                        let mkt = item.get("market").and_then(|v| v.as_str()).unwrap_or("");
                        let price = item.get("price").and_then(|v| v.as_f64()).map(format_krw).unwrap_or_else(|| "-".to_string());
                        md.push_str(&format!("| **{}** | `{}` | {} | **{}** |\n", name, code, mkt, price));
                    }
                    md.push('\n');
                }
            }
            md.push_str("> *기계적 집계 순위이며 투자 추천이 아닙니다.*");
            return md;
        }

        // Default: Growth top 8
        let mut md = format!("### 🚀 실적 급성장 기업 랭킹 (Top 8) — {}\n\n", as_of);
        md.push_str("| 순위 | 종목명 | 코드 | 영업이익 YoY | 매출 YoY | 영업이익률 |\n");
        md.push_str("| :---: | :--- | :---: | :---: | :---: | :---: |\n");

        if let Some(list) = rankings.get("growth_top8").and_then(|v| v.as_array()) {
            for (idx, item) in list.iter().enumerate() {
                let name = item.get("name").and_then(|v| v.as_str()).unwrap_or("");
                let code = item.get("code").and_then(|v| v.as_str()).unwrap_or("");
                let op_yoy = item.get("영업이익YoY%").and_then(|v| v.as_f64()).unwrap_or(0.0);
                let rev_yoy = item.get("매출YoY%").and_then(|v| v.as_f64()).unwrap_or(0.0);
                let opm = item.get("OPM%").and_then(|v| v.as_f64()).unwrap_or(0.0);

                let rank_emoji = match idx {
                    0 => "🥇 1",
                    1 => "🥈 2",
                    2 => "🥉 3",
                    _ => &format!("{}위", idx + 1),
                };

                md.push_str(&format!(
                    "| {} | **{}** | `{}` | ▲ +{:.1}% | +{:.1}% | {:.1}% |\n",
                    rank_emoji, name, code, op_yoy, rev_yoy, opm
                ));
            }
            md.push('\n');
        }

        md.push_str("> ℹ️ *재무제표 공시 기준 기계 산정 순위이며, 특정 종목 추천이 아닙니다.*");
        md
    }

    async fn handle_disclosure_impact(&self, q: &str) -> String {
        let impact = match self.fetch_disclosure_impact().await {
            Ok(i) => i,
            Err(e) => return format!("⚠️ 공시 영향 통계 데이터를 가져오지 못했습니다: {}", e),
        };

        let summary = match impact.get("summary").and_then(|v| v.as_array()) {
            Some(s) => s,
            None => return "⚠️ 공시 통계 요약 데이터를 찾을 수 없습니다.".to_string(),
        };

        // Find relevant disclosure category
        let mut target_item: Option<&Value> = None;
        if q.contains("공급계약") || q.contains("계약") {
            target_item = summary.iter().find(|i| i.get("label").and_then(|l| l.as_str()).unwrap_or("").contains("공급계약"));
        } else if q.contains("유상증자") {
            target_item = summary.iter().find(|i| i.get("label").and_then(|l| l.as_str()).unwrap_or("").contains("유상증자"));
        } else if q.contains("무상증자") {
            target_item = summary.iter().find(|i| i.get("label").and_then(|l| l.as_str()).unwrap_or("").contains("무상증자"));
        } else if q.contains("사채") || q.contains("cb") || q.contains("전환사채") {
            target_item = summary.iter().find(|i| i.get("label").and_then(|l| l.as_str()).unwrap_or("").contains("전환사채"));
        } else if q.contains("실적") || q.contains("잠정") {
            target_item = summary.iter().find(|i| i.get("label").and_then(|l| l.as_str()).unwrap_or("").contains("잠정영업실적"));
        }

        // If specific category found, format detailed statistical breakdown
        if let Some(item) = target_item {
            let label = item.get("label").and_then(|l| l.as_str()).unwrap_or("공시");
            let mut md = format!("### 📊 DART 공시 유형별 주가 영향 통계: `{}`\n\n", label);
            md.push_str("2,700여 건의 DART 공시 전후 실제 시장초과 수익률(Market-adjusted Excess Return)과 95% 신뢰구간을 분석한 결과입니다.\n\n");

            md.push_str("| 분석 시점 (Horizon) | 시장초과수익률 중앙값 | 상승 확률 (Up Ratio) | 95% 신뢰구간 (CI95) | 유의성 |\n");
            md.push_str("| :---: | :---: | :---: | :---: | :---: |\n");

            let horizons = [("당일 (h0)", "h0"), ("익일 (h1)", "h1"), ("5거래일 후 (h5)", "h5"), ("20거래일 후 (h20)", "h20")];
            for (name, key) in horizons {
                if let Some(h) = item.get(key) {
                    let median = h.get("median_excess_pct").and_then(|v| v.as_f64()).unwrap_or(0.0);
                    let up_ratio = h.get("up_ratio_pct").and_then(|v| v.as_f64()).unwrap_or(0.0);
                    let ci_zero = h.get("ci_includes_zero").and_then(|v| v.as_bool()).unwrap_or(true);
                    let ci_low = h.get("median_ci95").and_then(|v| v.as_array()).and_then(|a| a.first()).and_then(|v| v.as_f64()).unwrap_or(0.0);
                    let ci_high = h.get("median_ci95").and_then(|v| v.as_array()).and_then(|a| a.get(1)).and_then(|v| v.as_f64()).unwrap_or(0.0);

                    let sign = if median > 0.0 { "+" } else { "" };
                    let sig_text = if !ci_zero { "✅ 유의미 (0 불포함)" } else { "⚖️ 0 포함 (중립)" };

                    md.push_str(&format!(
                        "| **{}** | **{}{:.2}%** | {:.1}% | [{:.2}%, {:.2}%] | {} |\n",
                        name, sign, median, up_ratio, ci_low, ci_high, sig_text
                    ));
                }
            }
            md.push_str("\n> 💡 **해석 팁**: 신뢰구간(CI95)에 0이 포함되는 경우 통계적으로 시장 대비 뚜렷한 초과 상승/하락 효과가 구별되지 않음을 의미합니다.\n");
            md.push_str("> *출처: aikstockdata.com (DART 공시 이벤트 원장 기반 집계)*");
            return md;
        }

        // Overview of multiple disclosure types
        let mut md = "### 📊 주요 DART 공시 유형별 주가 변동 통계 (h0 / h1)\n\n".to_string();
        md.push_str("| 공시 유형 | 당일 중앙값 (h0) | 익일 중앙값 (h1) | 익일 상승확률 |\n");
        md.push_str("| :--- | :---: | :---: | :---: |\n");

        for item in summary.iter().take(6) {
            let label = item.get("label").and_then(|l| l.as_str()).unwrap_or("");
            let h0_med = item.get("h0").and_then(|h| h.get("median_excess_pct")).and_then(|v| v.as_f64()).unwrap_or(0.0);
            let h1_med = item.get("h1").and_then(|h| h.get("median_excess_pct")).and_then(|v| v.as_f64()).unwrap_or(0.0);
            let h1_up = item.get("h1").and_then(|h| h.get("up_ratio_pct")).and_then(|v| v.as_f64()).unwrap_or(0.0);

            md.push_str(&format!(
                "| **{}** | {:+.2}% | **{:+.2}%** | {:.1}% |\n",
                label, h0_med, h1_med, h1_up
            ));
        }

        md.push_str("\n> 궁금하신 특정 공시 유형(예: `공급계약 공시 통계`, `유상증자 공시 영향`)을 입력하시면 세부 신뢰구간을 확인할 수 있습니다.");
        md
    }

    async fn handle_disclosures(&self) -> String {
        let list = match self.fetch_disclosures().await {
            Ok(l) => l,
            Err(e) => return format!("⚠️ 최신 공시 내역을 가져오지 못했습니다: {}", e),
        };

        let mut md = "### 📢 DART 주요 전자공시 내역 (최근 5건)\n\n".to_string();
        md.push_str("| 기업명 | 공시 유형 | 중요도 점수 | 핵심 요약 내용 |\n");
        md.push_str("| :--- | :---: | :---: | :--- |\n");

        for item in list.iter().take(5) {
            let name = item.get("name").and_then(|v| v.as_str()).unwrap_or("");
            let label = item.get("label").and_then(|v| v.as_str()).unwrap_or("");
            let score = item.get("score").and_then(|v| v.as_i64()).unwrap_or(0);
            let fact = item.get("fact").and_then(|v| v.as_str()).unwrap_or("");

            let badge = if score >= 70 { "🔥 높음" } else if score >= 40 { "⚡ 보통" } else { "📋 일반" };

            md.push_str(&format!(
                "| **{}** | `{}` | {} ({}점) | {} |\n",
                name, label, badge, score, fact
            ));
        }

        md.push_str("\n> *DART 전자공시 AI 요약 점수 기준이며 특정 투자 추천이 아닙니다.*");
        md
    }

    async fn handle_earnings_calendar(&self) -> String {
        let today_data = match self.fetch_today().await {
            Ok(d) => d,
            Err(e) => return format!("⚠️ 실적 캘린더를 불러오지 못했습니다: {}", e),
        };

        let mut md = "### 📅 최근 잠정실적 및 실적 발표 현황\n\n".to_string();

        if let Some(earnings) = today_data.get("recent_earnings").and_then(|v| v.as_array()) {
            if !earnings.is_empty() {
                md.push_str("| 기업명 | 시장 | 실적 기준 | 영업이익 YoY | 상태 |\n");
                md.push_str("| :--- | :---: | :---: | :---: | :---: |\n");
                for item in earnings.iter().take(6) {
                    let name = item.get("name").and_then(|v| v.as_str()).unwrap_or("");
                    let market = item.get("market").and_then(|v| v.as_str()).unwrap_or("");
                    let period = item.get("period").and_then(|v| v.as_str()).unwrap_or("");
                    let op_yoy = item.get("op_yoy_pct").and_then(|v| v.as_f64()).map(|v| format!("{:+.1}%", v)).unwrap_or_else(|| "-".to_string());
                    let status = item.get("status").and_then(|v| v.as_str()).unwrap_or("발표완료");

                    md.push_str(&format!("| **{}** | {} | `{}` | **{}** | {} |\n", name, market, period, op_yoy, status));
                }
                md.push('\n');
            } else {
                md.push_str("최근 집계된 실적 발표 내역이 없습니다.\n\n");
            }
        }

        md.push_str("> *정기보고서 및 잠정실적 공시 기반 요약 (aikstockdata.com)*");
        md
    }

    fn format_stock_search_results(&self, q: &str, stocks: &[Value]) -> String {
        let mut md = format!("### 🔍 '{}' 종목 검색 결과 (총 {}건)\n\n", q, stocks.len());
        md.push_str("상세 분석을 원하시면 종목명을 클릭하거나 다시 입력해 주세요.\n\n");
        md.push_str("| 종목명 | 코드 | 소속 시장 | 현재 종가 | 시가총액 |\n");
        md.push_str("| :--- | :---: | :---: | :---: | :---: |\n");

        for s in stocks {
            let name = s.get("name").and_then(|v| v.as_str()).unwrap_or("");
            let code = s.get("code").and_then(|c| c.as_str()).unwrap_or("");
            let mkt = s.get("market").and_then(|v| v.as_str()).unwrap_or("");
            let price = s.get("price").and_then(|v| v.as_f64()).map(format_krw).unwrap_or_else(|| "-".to_string());
            let mkt_cap = s.get("market_cap_eok").and_then(|v| v.as_f64()).map(|e| {
                if e >= 10000.0 {
                    format!("{:.1}조원", e / 10000.0)
                } else {
                    format!("{:.0}억원", e)
                }
            }).unwrap_or_else(|| "-".to_string());

            md.push_str(&format!("| **{}** | `{}` | {} | {} | {} |\n", name, code, mkt, price, mkt_cap));
        }

        md
    }

    fn format_not_found(&self, q: &str) -> String {
        format!(
            "### ❓ '{}'에 대한 정보를 찾지 못했습니다.\n\n\
            입력하신 단어가 정확한 종목명(예: **삼성전자**, **005930**)인지 확인하시거나 아래 추천 질문을 시도해 보세요.\n\n\
            - `오늘 증시 브리핑`\n\
            - `급성장 기업 순위`\n\
            - `최근 주요 공시`\n\
            - `공급계약 공시 후 주가 영향`\n\
            - `도움말`",
            q
        )
    }

    // -------------------------------------------------------------
    // Chat Room Integration & Message Event Broadcast
    // -------------------------------------------------------------

    pub async fn get_or_create_stock_bot_room(&self, user_id: &str) -> AppResult<ChatMessageRoom> {
        // Check if user already has a 1:1 room with AI_STOCK_BOT
        let row_opt = sqlx::query(
            r#"
            SELECT r.id, r.name, r.is_group, r.created_by, r.created_at,
                   r.last_message, r.last_message_at, r.version,
                   0::bigint AS unread_count
            FROM chat_message_room r
            JOIN chat_message_room_member m1 ON r.id = m1.room_id AND (m1.user_id = $1 OR m1.user_id IN (SELECT id FROM users WHERE username = $1))
            JOIN chat_message_room_member m2 ON r.id = m2.room_id AND m2.user_id = $2
            WHERE r.is_group = false
            LIMIT 1
            "#,
        )
        .bind(user_id)
        .bind(Self::BOT_USER_ID)
        .fetch_optional(&self.pool)
        .await
        .unwrap_or(None);

        if let Some(r) = row_opt {
            let room = ChatMessageRoom {
                id: sqlx::Row::get(&r, "id"),
                name: sqlx::Row::get(&r, "name"),
                is_group: sqlx::Row::get(&r, "is_group"),
                created_by: sqlx::Row::get(&r, "created_by"),
                created_at: sqlx::Row::get(&r, "created_at"),
                last_message: sqlx::Row::get(&r, "last_message"),
                last_message_at: sqlx::Row::get(&r, "last_message_at"),
                version: sqlx::Row::get(&r, "version"),
                unread_count: Some(0),
            };
            return Ok(room);
        }

        // Create new dedicated Stock Bot room
        let room_name = "🤖 AI 주식 비서 (aikstockdata)";
        let members = vec![user_id.to_string(), Self::BOT_USER_ID.to_string()];
        let room = ChatRepository::create_room(&self.pool, room_name, false, user_id, &members).await?;

        // Send initial greeting message
        let welcome_text = self.format_help();
        let _ = ChatRepository::save_message(
            &self.pool,
            room.id,
            Self::BOT_USER_ID,
            Some(Self::BOT_NAME),
            "TEXT",
            Some(&welcome_text),
            None,
            None,
            None,
        )
        .await;

        Ok(room)
    }

    pub async fn should_bot_respond(&self, room_id: Uuid, sender_id: &str, content: &str) -> bool {
        // Never respond to bot or system messages to prevent loops
        if sender_id == Self::BOT_USER_ID || sender_id == "SYSTEM" {
            return false;
        }

        // 1. Explicit mention in any room (@stock, @주식, @bot)
        let trimmed = content.trim();
        if trimmed.starts_with("@stock") || trimmed.starts_with("@주식") || trimmed.starts_with("@bot") {
            return true;
        }

        // 2. Room is a bot room (contains AI_STOCK_BOT member)
        let is_bot_room: bool = sqlx::query_scalar(
            "SELECT EXISTS(SELECT 1 FROM chat_message_room_member WHERE room_id = $1 AND user_id = $2)"
        )
        .bind(room_id)
        .bind(Self::BOT_USER_ID)
        .fetch_one(&self.pool)
        .await
        .unwrap_or(false);

        is_bot_room
    }

    pub async fn respond_to_message(
        &self,
        room_id: Uuid,
        user_content: &str,
        chat_service: &ChatService,
    ) -> AppResult<()> {
        let mut clean_query = user_content.trim();
        if clean_query.starts_with("@stock") {
            clean_query = clean_query.trim_start_matches("@stock").trim();
        } else if clean_query.starts_with("@주식") {
            clean_query = clean_query.trim_start_matches("@주식").trim();
        } else if clean_query.starts_with("@bot") {
            clean_query = clean_query.trim_start_matches("@bot").trim();
        }

        let bot_response = self.process_query(clean_query).await;

        // Send message through chat service (which saves and broadcasts WebSocket event)
        let _ = chat_service
            .send_message(
                room_id,
                Self::BOT_USER_ID,
                Some(Self::BOT_NAME),
                SendMessageRequest {
                    room_id: Some(room_id),
                    message_type: Some("TEXT".to_string()),
                    content: Some(bot_response),
                    file_url: None,
                    file_name: None,
                    file_size: None,
                },
            )
            .await?;

        Ok(())
    }
}
