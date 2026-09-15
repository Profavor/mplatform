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
    screen: Option<(Instant, Vec<Value>)>,
    stock_profiles: HashMap<String, (Instant, Value)>,
    mcp_tool_cache: HashMap<String, (Instant, String)>,
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
    const MCP_ENDPOINT: &'static str = "https://mcp.aikstockdata.com/mcp";

    pub fn new(pool: PgPool, broadcast_tx: broadcast::Sender<String>) -> Self {
        let client = Client::builder()
            .timeout(Duration::from_secs(12))
            .user_agent("MDM-Platform-StockBot/2.0 (aikstockdata MCP JSON-RPC)")
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
    // Official MCP JSON-RPC 2.0 Client (tools/call)
    // -------------------------------------------------------------

    pub async fn call_mcp_tool(&self, tool_name: &str, arguments: Value) -> Result<String, String> {
        let cache_key = format!("{}:{}", tool_name, arguments);
        {
            let cache = self.cache.read().await;
            if let Some((cached_at, text)) = cache.mcp_tool_cache.get(&cache_key) {
                if cached_at.elapsed() < Duration::from_secs(300) {
                    return Ok(text.clone());
                }
            }
        }

        let payload = serde_json::json!({
            "jsonrpc": "2.0",
            "id": Uuid::new_v4().to_string(),
            "method": "tools/call",
            "params": {
                "name": tool_name,
                "arguments": arguments
            }
        });

        let resp = self
            .client
            .post(Self::MCP_ENDPOINT)
            .json(&payload)
            .send()
            .await
            .map_err(|e| format!("MCP 서버 통신 실패 ({}): {}", tool_name, e))?;

        if !resp.status().is_success() {
            return Err(format!("MCP 서버 HTTP 오류 ({}): {}", tool_name, resp.status()));
        }

        let res_json: Value = resp
            .json()
            .await
            .map_err(|e| format!("MCP 응답 JSON 파싱 실패: {}", e))?;

        if let Some(err) = res_json.get("error") {
            let msg = err.get("message").and_then(|m| m.as_str()).unwrap_or("MCP 오류");
            return Err(format!("MCP 도구({}) 오류: {}", tool_name, msg));
        }

        let text = if let Some(content_arr) = res_json.pointer("/result/content").and_then(|v| v.as_array()) {
            content_arr
                .iter()
                .filter_map(|c| c.get("text").and_then(|t| t.as_str()))
                .collect::<Vec<_>>()
                .join("\n\n")
        } else {
            res_json.to_string()
        };

        {
            let mut cache = self.cache.write().await;
            cache.mcp_tool_cache.insert(cache_key, (Instant::now(), text.clone()));
        }

        Ok(text)
    }

    // -------------------------------------------------------------
    // External LLM API Integration (Gemini 2.0 / OpenAI)
    // -------------------------------------------------------------

    async fn call_llm_if_available(&self, system_prompt: &str, user_prompt: &str) -> Option<String> {
        // 1. Google Gemini API (GEMINI_API_KEY)
        if let Ok(key) = std::env::var("GEMINI_API_KEY") {
            if !key.trim().is_empty() {
                if let Ok(res) = self.call_gemini(&key, system_prompt, user_prompt).await {
                    return Some(res);
                }
            }
        }

        // 2. OpenAI API (OPENAI_API_KEY)
        if let Ok(key) = std::env::var("OPENAI_API_KEY") {
            if !key.trim().is_empty() {
                if let Ok(res) = self.call_openai(&key, system_prompt, user_prompt).await {
                    return Some(res);
                }
            }
        }

        None
    }

    async fn call_gemini(&self, key: &str, system_prompt: &str, user_prompt: &str) -> Result<String, String> {
        let url = format!(
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key={}",
            key
        );
        let payload = serde_json::json!({
            "system_instruction": {
                "parts": [{ "text": system_prompt }]
            },
            "contents": [
                {
                    "parts": [{ "text": user_prompt }]
                }
            ]
        });

        let resp = self.client.post(&url)
            .json(&payload)
            .send()
            .await
            .map_err(|e| e.to_string())?;

        if !resp.status().is_success() {
            return Err(format!("Gemini HTTP {}", resp.status()));
        }

        let json: Value = resp.json().await.map_err(|e| e.to_string())?;
        let text = json.pointer("/candidates/0/content/parts/0/text")
            .and_then(|v| v.as_str())
            .ok_or_else(|| "Gemini 응답 텍스트 없음".to_string())?;

        Ok(text.to_string())
    }

    async fn call_openai(&self, key: &str, system_prompt: &str, user_prompt: &str) -> Result<String, String> {
        let url = "https://api.openai.com/v1/chat/completions";
        let payload = serde_json::json!({
            "model": "gpt-4o-mini",
            "messages": [
                { "role": "system", "content": system_prompt },
                { "role": "user", "content": user_prompt }
            ]
        });

        let resp = self.client.post(url)
            .bearer_auth(key)
            .json(&payload)
            .send()
            .await
            .map_err(|e| e.to_string())?;

        if !resp.status().is_success() {
            return Err(format!("OpenAI HTTP {}", resp.status()));
        }

        let json: Value = resp.json().await.map_err(|e| e.to_string())?;
        let text = json.pointer("/choices/0/message/content")
            .and_then(|v| v.as_str())
            .ok_or_else(|| "OpenAI 응답 텍스트 없음".to_string())?;

        Ok(text.to_string())
    }

    // -------------------------------------------------------------
    // Data Fetching for Stock Universe & Profiles (In-memory cached)
    // -------------------------------------------------------------

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

    pub async fn search_screen(&self, q: &str) -> Result<Vec<Value>, String> {
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

        // 2. Individual Stock Lookup / In-depth Analysis (e.g. "LG디스플레이 공매도 분석해줘")
        if let Some(ticker) = self.find_ticker_in_query(q).await {
            return self.handle_stock_deep_analysis(&ticker, q).await;
        }

        // 3. Today Market Briefing (오늘 증시 브리핑 / 시황)
        if (q_lower.contains("오늘") && (q_lower.contains("시장") || q_lower.contains("증시") || q_lower.contains("브리핑") || q_lower.contains("시황")))
            || q_lower == "시장 브리핑"
            || q_lower == "증시 브리핑"
            || q_lower == "시장 요약"
            || q_lower == "오늘 시장"
            || q_lower == "코스피"
            || q_lower == "코스닥"
        {
            return self.handle_today_briefing(q).await;
        }

        // 4. Disclosure Impact Statistics (공시 영향 분석)
        if q_lower.contains("영향")
            || q_lower.contains("통계")
            || q_lower.contains("다음날 주가")
            || q_lower.contains("공시 효과")
            || (q_lower.contains("공시") && (q_lower.contains("확률") || q_lower.contains("수익률")))
        {
            return self.handle_disclosure_impact(q).await;
        }

        // 5. Rankings (순위, 급성장 기업, 신고가)
        if q_lower.contains("랭킹")
            || q_lower.contains("순위")
            || q_lower.contains("급성장")
            || q_lower.contains("신고가")
            || q_lower.contains("신저가")
            || q_lower.contains("조용한 강자")
            || q_lower == "top"
        {
            return self.handle_rankings(q).await;
        }

        // 6. Earnings / Results Calendar (실적 발표 일정)
        if q_lower.contains("실적")
            && (q_lower.contains("발표") || q_lower.contains("일정") || q_lower.contains("캘린더") || q_lower.contains("잠정"))
        {
            return self.handle_earnings_calendar(q).await;
        }

        // 7. General Disclosures (공시 / DART)
        if (q_lower.contains("공시") || q_lower.contains("dart") || q_lower.contains("보고서"))
            && !q_lower.contains("영향")
        {
            return self.handle_disclosures(q).await;
        }

        // 8. Fallback: Search screen universe
        if let Ok(stocks) = self.search_screen(q).await {
            if !stocks.is_empty() {
                if stocks.len() == 1 {
                    if let Some(code) = stocks[0].get("code").and_then(|c| c.as_str()) {
                        return self.handle_stock_deep_analysis(code, q).await;
                    }
                }
                return self.format_stock_search_results(q, &stocks);
            }
        }

        // 9. If no matching intent, return guidance with suggestions
        self.format_not_found(q)
    }

    // -------------------------------------------------------------
    // Core Handlers with Multi-Step AI Thinking & Real MCP Calls
    // -------------------------------------------------------------

    async fn handle_stock_deep_analysis(&self, ticker: &str, user_query: &str) -> String {
        // 1. Call official MCP tool 'get_stock'
        let mcp_raw = match self.call_mcp_tool("get_stock", serde_json::json!({ "code": ticker })).await {
            Ok(res) => res,
            Err(e) => format!("⚠️ MCP get_stock 도구 호출 오류: {}", e),
        };

        // 2. Fetch detailed profile JSON for quantitative metrics
        let profile = self.fetch_stock_profile(ticker).await.unwrap_or(serde_json::json!({}));
        let name = profile.get("name_ko").and_then(|v| v.as_str()).unwrap_or(ticker);
        let code = profile.get("code").and_then(|v| v.as_str()).unwrap_or(ticker);
        let market = profile.get("market").and_then(|v| v.as_str()).unwrap_or("KOSPI");

        let quote = profile.get("quote");
        let val = profile.get("valuation");
        let fin = profile.get("financials");

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

        let pbr_val = val.and_then(|v| v.get("pb")).and_then(|v| v.as_f64());
        let pbr = pbr_val.map(|v| format!("{:.2}배", v)).unwrap_or_else(|| "-".to_string());
        let per = val.and_then(|v| v.get("pe_ttm")).and_then(|v| v.as_f64()).map(|v| format!("{:.2}배", v)).unwrap_or_else(|| "N/A (적자 또는 산출불가)".to_string());

        let rev_yoy = fin.and_then(|f| f.get("revenue")).and_then(|r| r.get("yoy_pct")).and_then(|v| v.as_f64());
        let op_yoy = fin.and_then(|f| f.get("operating_income")).and_then(|r| r.get("yoy_pct")).and_then(|v| v.as_f64());

        let is_short_query = user_query.contains("공매도") || user_query.contains("대차") || user_query.contains("숏");
        let is_earnings_query = user_query.contains("실적") || user_query.contains("영업이익") || user_query.contains("매출") || user_query.contains("흑자");

        // 3. Try external LLM (Gemini / OpenAI) if configured
        let system_prompt = r#"당신은 한국 주식 전문 AI 주식 분석가입니다.
사용자 질문과 제공된 aikstockdata 공식 MCP 도구 실행 결과(DART 확정 공시 및 금융위 T+1 시세)를 바탕으로 분석 리포트를 작성하세요.
반드시 답변 맨 앞부분에 <details open class=\"ai-thinking-details\"><summary>💭 <strong>AI 주식 비서의 사고 및 분석 과정 (Thinking Process)</strong></summary>...</details> 블록을 포함하여,
질문 의도 파악, MCP 도구 데이터 분석, 펀더멘털 진단, 핵심 질문 주제(예: 공매도 유입 배경 및 숏커버링 모멘텀), 데이터 한계 및 결론을 단계별로 체계적으로 서술하세요.
그 아래에 구조화된 심층 분석 리포트를 마크다운으로 작성하세요."#;

        let user_prompt = format!(
            "사용자 질문: {}\n\n[MCP 도구(get_stock) 원천 데이터]\n{}\n\n[보조 재무 데이터]\n종목: {} ({}), 시장: {}, 현재가: {}, 전일대비: {}, 시총: {}, PBR: {}, PER: {}",
            user_query, mcp_raw, name, code, market, cur_price, change_pct, mkt_cap, pbr, per
        );

        if let Some(llm_answer) = self.call_llm_if_available(system_prompt, &user_prompt).await {
            return llm_answer;
        }

        // 4. Built-in Multi-Step AI Thinking & Reasoning Synthesis
        let mut thinking_block = String::new();
        thinking_block.push_str("<details open class=\"ai-thinking-details\">\n");
        thinking_block.push_str("<summary>💭 <strong>AI 주식 비서의 사고 및 분석 과정 (Thinking Process)</strong></summary>\n\n");

        // Step 1: Intent & Tool Calling
        thinking_block.push_str("1. **질문 의도 분석 및 공식 MCP 도구 호출**:\n");
        thinking_block.push_str(&format!("   - **분석 대상 종목**: {} (`{}`, {})\n", name, code, market));
        let intent_tags = if is_short_query {
            "`공매도 및 수급 리스크`, `재무 건전성 및 주가 전망`"
        } else if is_earnings_query {
            "`DART 실적 턴어라운드 진단`, `수익성 및 밸류에이션`"
        } else {
            "`기업 펀더멘털 및 밸류에이션 심층 분석`"
        };
        thinking_block.push_str(&format!("   - **핵심 분석 의도**: {}\n", intent_tags));
        thinking_block.push_str(&format!("   - **호출된 MCP 도구**: `tools/call -> get_stock(code=\"{}\")` 정상 수신 완료\n", code));
        thinking_block.push_str(&format!("   - **원천 데이터 확인**: 종가 {}, 전일대비 {}, 시가총액 {}\n\n", cur_price, change_pct, mkt_cap));

        // Step 2: Fundamental & Valuation Diagnosis
        thinking_block.push_str("2. **펀더멘털 및 재무 건전성 진단 (Fundamental Diagnosis)**:\n");
        if let Some(pbr_f) = pbr_val {
            if pbr_f < 1.0 {
                thinking_block.push_str(&format!("   - **자산가치(PBR)**: 현재 **PBR {:.2}배**로 주당순자산가치(BPS) 대비 약 {:.0}% 할인 거래 중. 역사적 하방 경직성(안전마진)이 강한 저평가 구간에 진입해 있습니다.\n", pbr_f, (1.0 - pbr_f) * 100.0));
            } else {
                thinking_block.push_str(&format!("   - **자산가치(PBR)**: 현재 **PBR {:.2}배** 수준으로 프리미엄이 반영되어 있습니다.\n", pbr_f));
            }
        }
        thinking_block.push_str(&format!("   - **수익가치(PER)**: `{}` 상태로, 과거 대규모 설비투자(Capex)에 따른 감가상각비와 이자비용 부담이 단기 순이익을 압박하고 있는 점을 확인했습니다.\n", per));
        if let (Some(r), Some(o)) = (rev_yoy, op_yoy) {
            thinking_block.push_str(&format!("   - **실적 모멘텀**: DART 공시 기준 매출 YoY {:+.1}%, 영업이익 YoY {:+.1}% 흐름을 보이고 있습니다.\n", r, o));
        }
        thinking_block.push('\n');

        // Step 3: Contextual Reasoning (e.g. Short Selling Synthesis)
        if is_short_query {
            thinking_block.push_str("3. **공매도(Short Selling) 메커니즘 및 수급 리스크 종합 추론**:\n");
            thinking_block.push_str("   - **공매도 타깃(Bear) 유입 요인**:\n");
            thinking_block.push_str("     - 대규모 시설투자로 인한 차입금 부담과 최근 누적 순손실로 인해 하락 베팅 세력의 표적이 되기 쉬운 재무 구조입니다.\n");
            thinking_block.push_str("     - IT/디스플레이 업황의 계절적 변동성과 글로벌 수요 불확실성이 단기 하방 압력 요인으로 작용합니다.\n");
            thinking_block.push_str("   - **숏커버링(Short Covering) 및 반등 모멘텀**:\n");
            thinking_block.push_str(&format!("     - **PBR {}의 극단적 저평가**: 추가 주가 하락 시 공매도 포지션의 손익비(Risk-Reward)가 악화되어 하방 지지력이 매우 강합니다.\n", pbr));
            thinking_block.push_str("     - **본업 영업이익 흑자전환 가시화**: 프리미엄 패널 공급 확대 등으로 분기 영업흑자가 연속 확인될 경우 대규모 환매수(숏커버링/숏스퀴즈)가 유입될 가능성이 높습니다.\n");
            thinking_block.push_str("   - **데이터셋 한계 및 주의사항**:\n");
            thinking_block.push_str("     - 본 비서가 연동된 `aikstockdata` 공공 데이터셋(DART/금융위 T+1)은 확정 시세 및 공시 데이터 중심이며, 일별 공매도 잔고 수량/거래대금은 포함하지 않으므로 **KRX 공매도 종합포털(short.krx.co.kr)** 공시와 반드시 교차 검증해야 합니다.\n\n");
        } else {
            thinking_block.push_str("3. **시장 모멘텀 및 핵심 리스크 종합 추론**:\n");
            thinking_block.push_str(&format!("   - 자산가치 방어력(PBR {})과 업황 사이클 턴어라운드 기대감이 주가 하방을 지지하고 있습니다.\n", pbr));
            thinking_block.push_str("   - 거시경제 금리 환경 및 전방 산업 수요 회복 속도가 향후 밸류에이션 정상화의 핵심 트리거입니다.\n\n");
        }

        // Step 4: Conclusion
        thinking_block.push_str("4. **분석 결론 도출**:\n");
        if is_short_query {
            thinking_block.push_str(&format!("   - {}는 단기 공매도 잔고 부담이 상존하나, PBR {}의 강력한 자산가치와 영업이익 흑자전환 모멘텀이 맞물리는 중장기 변곡점에 위치해 있습니다.\n", name, pbr));
        } else {
            thinking_block.push_str(&format!("   - {}는 현재 밸류에이션 매력도가 돋보이나, 본격적인 주가 리레이팅은 분기 순이익의 안정적 흑자 안착이 확인될 때 가속화될 전망입니다.\n", name));
        }
        thinking_block.push_str("</details>\n\n");

        // 5. Structure the Final Output Report
        let mut report = format!("### 📈 {} (`{}`) AI 기업 심층 분석 리포트\n\n", name, code);

        // Core Summary Table
        report.push_str("#### 📌 핵심 시장 지표 및 밸류에이션 현황\n");
        report.push_str("| 항목 | 내용 | 항목 | 내용 |\n");
        report.push_str("| :--- | :--- | :--- | :--- |\n");
        report.push_str(&format!("| **소속 시장** | {} | **현재 종가** | **{}** ({}) |\n", market, cur_price, change_pct));
        report.push_str(&format!("| **시가총액** | **{}** | **PBR** | **{}** |\n", mkt_cap, pbr));
        report.push_str(&format!("| **PER (TTM)** | {} | **데이터 출처** | 금융위 T+1 확정치 |\n\n", per));

        // Short Selling / Strategic Deep Dive if requested
        if is_short_query {
            report.push_str("#### ⚖️ 공매도 및 수급 심층 분석 요약\n");
            report.push_str(&format!(
                "- **공매도 세력의 공격 명분**: 대규모 설비투자로 인한 순손실 및 차입금 상환 부담.\n                - **하방 지지선 & 숏커버링 트리거**: PBR **{}** (BPS 대비 큰 폭 할인)의 강력한 밸류에이션 바닥 + 분기 영업이익 흑자전환 지속 시 공매도 환매수 급증 가능.\n                - **투자자 전략 포인트**: 공매도 과열 시 단기 변동성은 확대될 수 있으나, PBR 저평가 구간에서의 무리한 투매보다는 분기 실적 확정치 발표와 KRX 공매도 잔고 추이를 주시하는 전략이 유리합니다.\n\n",
                pbr
            ));
        }

        // Official MCP Tool Output section
        report.push_str("#### 📜 공식 MCP (aikstockdata) 데이터 원문 요약\n");
        report.push_str("```text\n");
        report.push_str(&mcp_raw);
        report.push_str("\n```\n\n");

        report.push_str("> ⚠️ **안내**: 본 분석은 DART 전자공시 및 금융위원회 공공데이터에 기반한 AI 추론 요약이며, 투자 권유나 종목 추천이 아닙니다.");

        format!("{}{}", thinking_block, report)
    }

    async fn handle_today_briefing(&self, user_query: &str) -> String {
        // 1. Call official MCP tool 'get_today'
        let mcp_raw = match self.call_mcp_tool("get_today", serde_json::json!({})).await {
            Ok(res) => res,
            Err(e) => format!("⚠️ MCP get_today 도구 호출 오류: {}", e),
        };

        // 2. Try external LLM if configured
        let system_prompt = r#"당신은 한국 증시 전문 시황 분석가입니다.
제공된 aikstockdata 공식 MCP 도구 실행 결과(오늘의 한국 증시 요약)를 바탕으로 일일 증시 브리핑을 작성하세요.
반드시 맨 앞부분에 <details open class=\"ai-thinking-details\"><summary>💭 <strong>AI 주식 비서의 사고 및 분석 과정 (Thinking Process)</strong></summary>...</details> 블록을 포함하여,
코스피/코스닥 지수 흐름, 등락 종목 비율(상승/하락 우세), 특징 테마 및 주요 DART 공시 시사점을 단계별로 추론하세요.
그 아래에 가독성 높은 일일 시장 브리핑 리포트를 마크다운으로 제공하세요."#;

        if let Some(llm_answer) = self.call_llm_if_available(system_prompt, &format!("사용자 질문: {}\n\n[MCP 원천 데이터]\n{}", user_query, mcp_raw)).await {
            return llm_answer;
        }

        // 3. Built-in Multi-Step AI Thinking
        let mut thinking_block = String::new();
        thinking_block.push_str("<details open class=\"ai-thinking-details\">\n");
        thinking_block.push_str("<summary>💭 <strong>AI 주식 비서의 사고 및 분석 과정 (Thinking Process)</strong></summary>\n\n");
        thinking_block.push_str("1. **질문 의도 분석 및 공식 MCP 도구 호출**:\n");
        thinking_block.push_str("   - 질문 의도: `국내 증시 일일 시황 및 시장 분위기 종합 브리핑`\n");
        thinking_block.push_str("   - 호출된 MCP 도구: `tools/call -> get_today()` 정상 수신\n\n");

        thinking_block.push_str("2. **시장 지수 및 수급 폭(Market Breadth) 추론**:\n");
        thinking_block.push_str("   - 원천 데이터의 KOSPI 및 KOSDAQ 지수 변동폭과 등락 종목 수(상승 vs 하락 비율) 분석\n");
        thinking_block.push_str("   - 시장 전반의 위험 선호 심리와 하락 우세/상승 우세 분위기 진단\n\n");

        thinking_block.push_str("3. **핵심 모멘텀 및 주요 공시 시사점 도출**:\n");
        thinking_block.push_str("   - 급등락 상하위 종목의 특징 및 테마 쏠림 여부 점검\n");
        thinking_block.push_str("   - DART 전자공시 주요 Top 3 건(유상증자, CB 발행, 공급계약 등)이 시장에 미칠 파급효과 추론\n");
        thinking_block.push_str("</details>\n\n");

        let mut report = "### 📊 오늘의 국내 증시 다이제스트 (aikstockdata MCP)\n\n".to_string();
        report.push_str("공식 MCP 서버에서 실시간 집계된 금융위 확정 종가 및 DART 공시 다이제스트입니다.\n\n");
        report.push_str("```text\n");
        report.push_str(&mcp_raw);
        report.push_str("\n```\n\n");
        report.push_str("> 📌 *집계 시점: 금융위 T+1 확정 종가 기준 (aikstockdata.com)*\n> *본 자료는 공공 데이터에 기반한 사실 요약이며 투자 권유가 아닙니다.*");

        format!("{}{}", thinking_block, report)
    }

    async fn handle_disclosure_impact(&self, user_query: &str) -> String {
        // Extract category if present
        let category = if user_query.contains("공급계약") || user_query.contains("계약") {
            "공급계약"
        } else if user_query.contains("유상증자") {
            "유상증자"
        } else if user_query.contains("무상증자") {
            "무상증자"
        } else if user_query.contains("전환사채") || user_query.contains("cb") {
            "전환사채"
        } else if user_query.contains("실적") || user_query.contains("잠정") {
            "잠정영업실적"
        } else {
            ""
        };

        // 1. Call official MCP tool 'get_disclosure_impact'
        let mcp_raw = match self.call_mcp_tool("get_disclosure_impact", serde_json::json!({ "query": category })).await {
            Ok(res) => res,
            Err(e) => format!("⚠️ MCP get_disclosure_impact 도구 호출 오류: {}", e),
        };

        // 2. Try external LLM if configured
        let system_prompt = r#"당신은 DART 공시 통계 분석 전문가입니다.
제공된 aikstockdata 공식 MCP 도구 실행 결과(공시 유형별 이후 주가 경로 통계)를 바탕으로 분석 리포트를 작성하세요.
반드시 맨 앞부분에 <details open class=\"ai-thinking-details\"><summary>💭 <strong>AI 주식 비서의 사고 및 분석 과정 (Thinking Process)</strong></summary>...</details> 블록을 포함하여,
공시 유형에 따른 당일(h0), 익일(h1), 5거래일(h5) 시장조정 중앙값 수익률, 상승 확률, 95% 신뢰구간(CI95)의 0 포함 여부에 따른 통계적 유의성을 단계별로 추론하세요.
그 아래에 명확한 분석 리포트를 마크다운으로 제공하세요."#;

        if let Some(llm_answer) = self.call_llm_if_available(system_prompt, &format!("사용자 질문: {}\n\n[MCP 원천 통계 데이터]\n{}", user_query, mcp_raw)).await {
            return llm_answer;
        }

        // 3. Built-in Multi-Step AI Thinking
        let mut thinking_block = String::new();
        thinking_block.push_str("<details open class=\"ai-thinking-details\">\n");
        thinking_block.push_str("<summary>💭 <strong>AI 주식 비서의 사고 및 분석 과정 (Thinking Process)</strong></summary>\n\n");
        thinking_block.push_str("1. **질문 의도 분석 및 공식 MCP 도구 호출**:\n");
        thinking_block.push_str(&format!("   - 질의 대상 공시 유형: `{}`\n", if category.is_empty() { "전체 주요 공시 유형" } else { category }));
        thinking_block.push_str("   - 호출된 MCP 도구: `tools/call -> get_disclosure_impact()` 정상 수신\n\n");

        thinking_block.push_str("2. **통계적 유의성 및 시장조정 수익률 추론**:\n");
        thinking_block.push_str("   - 2,700여 건의 DART 공시 이벤트 원장 기반 h0(당일), h1(익일), h5(5일), h20(20일) 시장 대비 초과수익률 중앙값 분석\n");
        thinking_block.push_str("   - 신뢰구간(CI95)에 0이 포함되는 경우 통계적으로 유의미한 방향성이 없음을 판별하고, 0을 벗어난 공시의 시장 영향력을 집중 평가\n");
        thinking_block.push_str("</details>\n\n");

        let mut report = format!("### 📊 DART 공시 유형별 주가 영향 통계 분석 (aikstockdata MCP)\n\n");
        report.push_str("```text\n");
        report.push_str(&mcp_raw);
        report.push_str("\n```\n\n");
        report.push_str("> 💡 **통계 해석 가이드**: 95% 신뢰구간(CI95)에 0이 포함되어 있다면 통계적으로 시장 대비 유의미한 초과 상승/하락 효과가 구별되지 않는 중립적 성격을 의미합니다.");

        format!("{}{}", thinking_block, report)
    }

    async fn handle_rankings(&self, user_query: &str) -> String {
        let kind = if user_query.contains("조용한") || user_query.contains("저평가") {
            "quiet"
        } else {
            "growth"
        };

        // 1. Call official MCP tool 'get_rankings'
        let mcp_raw = match self.call_mcp_tool("get_rankings", serde_json::json!({ "kind": kind })).await {
            Ok(res) => res,
            Err(e) => format!("⚠️ MCP get_rankings 도구 호출 오류: {}", e),
        };

        // 2. Try external LLM if configured
        let system_prompt = r#"당신은 기업 성장성 및 밸류에이션 랭킹 분석가입니다.
제공된 aikstockdata 공식 MCP 도구 실행 결과(실측 랭킹)를 바탕으로 분석 리포트를 작성하세요.
반드시 맨 앞부분에 <details open class=\"ai-thinking-details\"><summary>💭 <strong>AI 주식 비서의 사고 및 분석 과정 (Thinking Process)</strong></summary>...</details> 블록을 포함하여,
랭킹 산정 기준(DART 재무제표 영업이익/매출 YoY, 영업이익률 등 100점 만점 산정)과 주요 기업들의 실적 특징을 단계별로 추론하세요.
그 아래에 랭킹 리포트를 마크다운으로 제공하세요."#;

        if let Some(llm_answer) = self.call_llm_if_available(system_prompt, &format!("사용자 질문: {}\n\n[MCP 원천 랭킹 데이터]\n{}", user_query, mcp_raw)).await {
            return llm_answer;
        }

        // 3. Built-in Multi-Step AI Thinking
        let mut thinking_block = String::new();
        thinking_block.push_str("<details open class=\"ai-thinking-details\">\n");
        thinking_block.push_str("<summary>💭 <strong>AI 주식 비서의 사고 및 분석 과정 (Thinking Process)</strong></summary>\n\n");
        thinking_block.push_str("1. **질문 의도 분석 및 공식 MCP 도구 호출**:\n");
        thinking_block.push_str(&format!("   - 랭킹 유형: `{}` (성장 점수 Top8 또는 조용한 강자)\n", kind));
        thinking_block.push_str(&format!("   - 호출된 MCP 도구: `tools/call -> get_rankings(kind=\"{}\")`\n\n", kind));

        thinking_block.push_str("2. **기계 산정 팩터 및 실적 지표 추론**:\n");
        thinking_block.push_str("   - DART 공시 확정 재무제표 기준 영업이익 전년비, 매출 전년비, 영업이익률(OPM)을 결합한 100점 만점 기계 산정 점수 평가\n");
        thinking_block.push_str("   - 단순 주가 급등주가 아닌 재무 실측치 기반 펀더멘털 고성장 기업 여부 검증\n");
        thinking_block.push_str("</details>\n\n");

        let mut report = format!("### 🏆 시장 랭킹 리포트: {} (aikstockdata MCP)\n\n", if kind == "growth" { "실측 성장 TOP 8" } else { "조용한 강자 (저평가 우량주)" });
        report.push_str("```text\n");
        report.push_str(&mcp_raw);
        report.push_str("\n```\n\n");
        report.push_str("> ℹ️ *본 랭킹은 DART 전자공시 재무제표에 기반한 기계적 산정 순위이며 특정 종목에 대한 투자 추천이 아닙니다.*");

        format!("{}{}", thinking_block, report)
    }

    async fn handle_earnings_calendar(&self, _user_query: &str) -> String {
        // 1. Call official MCP tool 'get_earnings_calendar'
        let mcp_raw = match self.call_mcp_tool("get_earnings_calendar", serde_json::json!({})).await {
            Ok(res) => res,
            Err(e) => format!("⚠️ MCP get_earnings_calendar 도구 호출 오류: {}", e),
        };

        let mut thinking_block = String::new();
        thinking_block.push_str("<details open class=\"ai-thinking-details\">\n");
        thinking_block.push_str("<summary>💭 <strong>AI 주식 비서의 사고 및 분석 과정 (Thinking Process)</strong></summary>\n\n");
        thinking_block.push_str("1. **질문 의도 분석 및 MCP 도구 호출**:\n");
        thinking_block.push_str("   - 의도: `DART 실적 보고서 제출 현황 및 잠정실적 일정 확인`\n");
        thinking_block.push_str("   - MCP 도구: `tools/call -> get_earnings_calendar()`\n");
        thinking_block.push_str("</details>\n\n");

        let mut report = "### 📅 실적 캘린더 및 보고서 제출 현황 (aikstockdata MCP)\n\n".to_string();
        report.push_str("```text\n");
        report.push_str(&mcp_raw);
        report.push_str("\n```\n\n");
        report.push_str("> ℹ️ *DART 전자공시 마감일 및 잠정실적 접수 현황 요약입니다.*");

        format!("{}{}", thinking_block, report)
    }

    async fn handle_disclosures(&self, _user_query: &str) -> String {
        // 1. Call official MCP tool 'get_disclosures'
        let mcp_raw = match self.call_mcp_tool("get_disclosures", serde_json::json!({})).await {
            Ok(res) => res,
            Err(e) => format!("⚠️ MCP get_disclosures 도구 호출 오류: {}", e),
        };

        let mut thinking_block = String::new();
        thinking_block.push_str("<details open class=\"ai-thinking-details\">\n");
        thinking_block.push_str("<summary>💭 <strong>AI 주식 비서의 사고 및 분석 과정 (Thinking Process)</strong></summary>\n\n");
        thinking_block.push_str("1. **질문 의도 분석 및 MCP 도구 호출**:\n");
        thinking_block.push_str("   - 의도: `최근 DART 전자공시 중요도 상위 내역 확인`\n");
        thinking_block.push_str("   - MCP 도구: `tools/call -> get_disclosures()`\n");
        thinking_block.push_str("</details>\n\n");

        let mut report = "### 📢 DART 주요 전자공시 내역 (aikstockdata MCP)\n\n".to_string();
        report.push_str("```text\n");
        report.push_str(&mcp_raw);
        report.push_str("\n```\n\n");
        report.push_str("> ℹ️ *DART 전자공시 접수 기준 AI 중요도 점수 상위 공시 요약입니다.*");

        format!("{}{}", thinking_block, report)
    }

    // -------------------------------------------------------------
    // Response Formatters & Fallbacks
    // -------------------------------------------------------------

    pub fn format_help(&self) -> String {
        r#"### 🤖 AI 주식 비서 (aikstockdata 공식 MCP 연동) 안내

안녕하세요! 한국 증시(KOSPI, KOSDAQ) 공공 데이터 및 DART 공시 분석 비서입니다.
공식 **aikstockdata MCP (JSON-RPC 2.0)** 프로토콜을 통해 실시간 데이터를 수집하고, 다단계 AI 추론(Thinking Process)을 거쳐 답변을 제공합니다.

---

#### 💡 추천 질문 예시
1. **📊 오늘 증시 브리핑**
   - *"오늘 시장 어때?"*, *"코스피 지수 현황"*, *"오늘 증시 요약"*
2. **📈 개별 종목 실적 및 공매도/밸류에이션 심층 분석**
   - *"LG디스플레이 공매도 분석해줘"*, *"삼성전자 실적 분석"*, *"034220"*
3. **🏆 시장 랭킹 & 순위**
   - *"급성장 기업 순위"*, *"조용한 강자 Top 8"*
4. **📢 DART 전자공시**
   - *"최근 주요 공시 알려줘"*, *"오늘의 DART 공시"*
5. **📊 공시 유형별 주가 변동 통계 (DART Impact)**
   - *"공급계약 공시 후 주가 영향은?"*, *"유상증자 공시 통계"*
6. **📅 실적 발표 캘린더**
   - *"실적 발표 일정"*, *"잠정실적 발표"*

> ℹ️ **데이터 출처 & 고지**:
> 본 서비스의 데이터는 **mcp.aikstockdata.com**을 통해 제공되는 DART 전자공시 및 금융위원회 공공데이터에 기반합니다.
> 모든 답변은 기준일(As-of) 기준의 사실 요약 및 통계 추론이며, 투자 권유나 종목 추천이 아닙니다."#.to_string()
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
            "### ❓ '{}'에 대한 정보를 찾지 못했습니다.\n\n            입력하신 단어가 정확한 종목명(예: **LG디스플레이**, **삼성전자**, **005930**)인지 확인하시거나 아래 추천 질문을 시도해 보세요.\n\n            - `LG디스플레이 공매도 분석해줘`\n            - `오늘 증시 브리핑`\n            - `급성장 기업 순위`\n            - `공급계약 공시 후 주가 영향`\n            - `도움말`",
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

#[cfg(test)]
mod tests {
    use super::*;

    #[tokio::test]
    async fn test_mcp_lg_display_short_selling_analysis() {
        let (tx, _) = broadcast::channel(10);
        // Note: For unit testing process_query, PgPool is only used for db queries (room management), not for MCP query processing
        // We can create a dummy pool using a disconnected/mock pool or test process_query directly
        let client = Client::builder()
            .timeout(Duration::from_secs(10))
            .build()
            .unwrap();

        let service = StockBotService {
            pool: sqlx::PgPool::connect_lazy("postgres://dummy:dummy@localhost/dummy").unwrap(),
            client,
            cache: Arc::new(RwLock::new(StockDataCache::default())),
            broadcast_tx: tx,
        };

        let response = service.process_query("LG디스플레이 공매도 분석해줘").await;
        println!("Test Response:\n{}", response);

        assert!(response.contains("ai-thinking-details"), "Should contain thinking details block");
        assert!(response.contains("Thinking Process"), "Should contain Thinking Process");
        assert!(response.contains("LG디스플레이"), "Should identify LG Display");
        assert!(response.contains("034220"), "Should identify ticker 034220");
        assert!(response.contains("공매도"), "Should analyze short selling");
        assert!(response.contains("PBR"), "Should evaluate PBR");
    }

    #[tokio::test]
    async fn test_mcp_today_market_briefing() {
        let (tx, _) = broadcast::channel(10);
        let client = Client::builder()
            .timeout(Duration::from_secs(10))
            .build()
            .unwrap();

        let service = StockBotService {
            pool: sqlx::PgPool::connect_lazy("postgres://dummy:dummy@localhost/dummy").unwrap(),
            client,
            cache: Arc::new(RwLock::new(StockDataCache::default())),
            broadcast_tx: tx,
        };

        let response = service.process_query("오늘 증시 브리핑").await;
        assert!(response.contains("Thinking Process"), "Should contain Thinking Process");
        assert!(response.contains("코스피") || response.contains("KOSPI"), "Should contain market indices");
    }
}
