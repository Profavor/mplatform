use serde::{Deserialize, Serialize};
use sqlx::PgPool;
use std::collections::{HashMap, HashSet};
use std::fs::File;
use std::io::BufReader;
use std::path::Path;
use uuid::Uuid;

#[derive(Debug, Deserialize, Default)]
#[serde(rename_all = "camelCase")]
pub struct StockSeedRequest {
    pub clear_existing: Option<bool>,
    pub markets: Option<Vec<String>>,
    pub limit_per_market: Option<i32>,
    pub custom_rows: Option<Vec<serde_json::Value>>,
}

#[derive(Debug, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct StockSeedResponse {
    pub domain_id: Uuid,
    pub domain_name: String,
    pub total_seeded: i32,
    pub total_created: i32,
    pub total_merged: i32,
    pub total_deleted: i32,
    pub seeded_by_market: HashMap<String, i32>,
    pub message: String,
}

pub struct StockDataIngestionJob;

impl StockDataIngestionJob {
    pub const LOCK_ID: i64 = 100_005;

    pub async fn run_ingestion(
        pool: &PgPool,
        req: StockSeedRequest,
        requested_by: &str,
    ) -> anyhow::Result<StockSeedResponse> {
        // 1. Ensure STOCK domain exists
        let domain_row: Option<(Uuid, serde_json::Value)> = sqlx::query_as(
            r#"
            SELECT id, name
            FROM domain
            WHERE specialized_category = 'STOCK' OR name::text ILIKE '%주식%'
            LIMIT 1
            "#,
        )
        .fetch_optional(pool)
        .await?;

        let (domain_id, domain_name) = match domain_row {
            Some((id, name)) => {
                let name_str = name
                    .get("ko")
                    .and_then(|v| v.as_str())
                    .unwrap_or("주식 종목 마스터")
                    .to_string();
                (id, name_str)
            }
            None => {
                let new_id = Uuid::new_v4();
                let name_json = serde_json::json!({"ko": "주식 종목 마스터", "en": "Stock Master"});
                sqlx::query(
                    r#"
                    INSERT INTO domain (id, name, specialized_category, auto_dq_scan_enabled, current_sequence, sort_order)
                    VALUES ($1, $2, 'STOCK', true, 0, 1)
                    "#,
                )
                .bind(new_id)
                .bind(name_json)
                .execute(pool)
                .await?;
                (new_id, "주식 종목 마스터".to_string())
            }
        };

        // 2. Fetch Classification Nodes for this domain
        #[derive(sqlx::FromRow)]
        struct NodeRow {
            id: Uuid,
            path: Option<String>,
            name: serde_json::Value,
        }
        let nodes = sqlx::query_as::<_, NodeRow>(
            r#"
            SELECT id, path, name
            FROM classification_node
            WHERE domain_id = $1 AND is_deleted = false
            "#,
        )
        .bind(domain_id)
        .fetch_all(pool)
        .await?;

        let mut node_map: HashMap<String, Uuid> = HashMap::new();
        for n in &nodes {
            let path_upper = n.path.as_deref().unwrap_or("").to_uppercase();
            let ko_name = n
                .name
                .get("ko")
                .and_then(|v| v.as_str())
                .unwrap_or("")
                .to_uppercase();
            let en_name = n
                .name
                .get("en")
                .and_then(|v| v.as_str())
                .unwrap_or("")
                .to_uppercase();

            if path_upper.contains("KOSPI")
                || ko_name.contains("KOSPI")
                || en_name.contains("KOSPI")
            {
                node_map.insert("KOSPI".to_string(), n.id);
            } else if path_upper.contains("KOSDAQ")
                || ko_name.contains("KOSDAQ")
                || en_name.contains("KOSDAQ")
            {
                node_map.insert("KOSDAQ".to_string(), n.id);
            } else if path_upper.contains("KONEX")
                || ko_name.contains("KONEX")
                || en_name.contains("KONEX")
            {
                node_map.insert("KONEX".to_string(), n.id);
            } else if path_upper.contains("미국")
                || path_upper.contains("US")
                || ko_name.contains("미국")
                || en_name.contains("US")
            {
                node_map.insert("US_MARKET".to_string(), n.id);
            } else if path_upper.contains("아시아")
                || path_upper.contains("ASIA")
                || ko_name.contains("아시아")
                || en_name.contains("ASIA")
            {
                node_map.insert("ASIA_MARKET".to_string(), n.id);
            } else if path_upper.contains("유럽")
                || path_upper.contains("EUROPE")
                || ko_name.contains("유럽")
                || en_name.contains("EUROPE")
            {
                node_map.insert("EUROPE_MARKET".to_string(), n.id);
            }
        }

        let default_node_id = nodes.first().map(|n| n.id).unwrap_or_else(Uuid::new_v4);

        // 3. Clear existing records if requested
        let mut total_deleted = 0;
        if req.clear_existing.unwrap_or(false) {
            let del_res = sqlx::query(
                r#"
                DELETE FROM record
                WHERE node_id IN (SELECT id FROM classification_node WHERE domain_id = $1)
                "#,
            )
            .bind(domain_id)
            .execute(pool)
            .await?;
            total_deleted = del_res.rows_affected() as i32;
            tracing::info!(
                "Cleared {} existing stock records for domain [{}]",
                total_deleted,
                domain_id
            );
        }

        // 4. Load Master Stock Data (all 3,525 records)
        let stock_rows: Vec<serde_json::Value> = match req.custom_rows {
            Some(rows) if !rows.is_empty() => rows,
            _ => Self::load_dataset_file()?,
        };

        // 5. Pre-fetch Live Market Quotes across KRX and Global exchanges
        let live_map = Self::fetch_all_live_market_data().await;

        let allowed_markets: Option<HashSet<String>> = req
            .markets
            .map(|m| m.into_iter().map(|s| s.to_uppercase()).collect());
        let limit_per_market = req.limit_per_market;

        let mut seeded_by_market: HashMap<String, i32> = HashMap::new();
        let mut total_read: i32 = 0;
        let mut total_seeded: i32 = 0;
        let mut total_created: i32 = 0;
        let mut total_merged: i32 = 0;

        let stock_channel_id: Option<Uuid> = sqlx::query_scalar(
            "SELECT id FROM integration_channels WHERE channel_code = 'CH-KRX-INBOUND-001' OR type = 'SPRING_BATCH' LIMIT 1"
        )
        .fetch_optional(pool)
        .await
        .unwrap_or(None);

        for mut row in stock_rows {
            total_read += 1;
            let market_code = row
                .get("market_node_code")
                .and_then(|v| v.as_str())
                .unwrap_or("KOSPI")
                .to_uppercase();

            if let Some(ref allowed) = allowed_markets {
                if !allowed.contains(&market_code) {
                    continue;
                }
            }

            let current_count = *seeded_by_market.get(&market_code).unwrap_or(&0);
            if let Some(limit) = limit_per_market {
                if current_count >= limit {
                    continue;
                }
            }

            let target_node_id = node_map
                .get(&market_code)
                .copied()
                .unwrap_or(default_node_id);

            // Extract identifier
            let ticker = row
                .get("ticker_code")
                .and_then(|v| v.as_str())
                .unwrap_or("")
                .trim()
                .to_string();

            if ticker.is_empty() {
                continue;
            }

            // Overlay real-time market quote if available
            if let Some(live_quote) = live_map.get(&ticker) {
                if let Some(quote_obj) = live_quote.as_object() {
                    if let Some(row_obj) = row.as_object_mut() {
                        for (k, v) in quote_obj {
                            if !v.is_null() {
                                row_obj.insert(k.clone(), v.clone());
                            }
                        }
                    }
                }
            }

            // Remove market_node_code before saving
            if let Some(obj) = row.as_object_mut() {
                obj.remove("market_node_code");
            }

            // Check if record exists
            #[derive(sqlx::FromRow)]
            struct ExistingRow {
                id: Uuid,
                data: Option<serde_json::Value>,
                version: i32,
            }
            let existing = sqlx::query_as::<_, ExistingRow>(
                r#"
                SELECT r.id, r.data, r.version
                FROM record r
                JOIN classification_node n ON r.node_id = n.id
                WHERE n.domain_id = $1 AND r.data->>'ticker_code' = $2 AND r.status = 'ACTIVE'
                LIMIT 1
                "#,
            )
            .bind(domain_id)
            .bind(&ticker)
            .fetch_optional(pool)
            .await?;

            let row_obj = row.as_object().cloned().unwrap_or_default();

            if let Some(ex) = existing {
                // MERGE
                let original_prev_data = ex.data.clone().unwrap_or(serde_json::json!({}));
                let mut prev_obj = original_prev_data.as_object().cloned().unwrap_or_default();

                // Merge incoming fields
                for (k, v) in &row_obj {
                    prev_obj.insert(k.clone(), v.clone());
                }
                let merged_data = serde_json::Value::Object(prev_obj);

                // Change detection: If completely identical, skip version increment & redundant history
                if original_prev_data == merged_data {
                    *seeded_by_market.entry(market_code).or_insert(0) += 1;
                    total_seeded += 1;
                    continue;
                }

                let new_version = ex.version + 1;

                sqlx::query(
                    r#"
                    UPDATE record
                    SET data = $1, version = $2, node_id = $3, updated_at = NOW()
                    WHERE id = $4
                    "#,
                )
                .bind(&merged_data)
                .bind(new_version)
                .bind(target_node_id)
                .bind(ex.id)
                .execute(pool)
                .await?;

                // Audit History with previous_data preserved
                let history_id = Uuid::new_v4();
                let _ = sqlx::query(
                    r#"
                    INSERT INTO record_history (id, record_id, change_type, changed_by, source_system, previous_data, new_data, version, changed_at)
                    VALUES ($1, $2, 'INBOUND_MERGE', $3, 'KRX & Global Stock Inbound', $4, $5, $6, NOW())
                    "#,
                )
                .bind(history_id)
                .bind(ex.id)
                .bind(requested_by)
                .bind(&original_prev_data)
                .bind(&merged_data)
                .bind(new_version)
                .execute(pool)
                .await;

                // Also record individual integration log if channel exists
                if let Some(cid) = stock_channel_id {
                    let log_id = Uuid::new_v4();
                    let inbound_payload = serde_json::to_string(&row_obj).unwrap_or_default();
                    let mapped_payload = serde_json::to_string(&merged_data).unwrap_or_default();
                    let _ = sqlx::query(
                        r#"
                        INSERT INTO integration_logs (id, channel_id, record_id, event_type, status, original_payload, mapped_payload, created_at, retry_count)
                        VALUES ($1, $2, $3, 'INBOUND_MERGE', 'SUCCESS', $4, $5, NOW(), 0)
                        "#
                    )
                    .bind(log_id)
                    .bind(cid)
                    .bind(ex.id)
                    .bind(inbound_payload)
                    .bind(mapped_payload)
                    .execute(pool)
                    .await;
                }

                total_merged += 1;
            } else {
                // INSERT
                let new_rec_id = Uuid::new_v4();
                let search_text = format!(
                    "{} {} {}",
                    row.get("stock_name").and_then(|v| v.as_str()).unwrap_or(""),
                    ticker,
                    row.get("industry_sector")
                        .and_then(|v| v.as_str())
                        .unwrap_or("")
                );
                let search_val = serde_json::Value::String(search_text);

                sqlx::query(
                    r#"
                    INSERT INTO record (id, node_id, status, source_system, data, searchable_data, version, created_at, updated_at)
                    VALUES ($1, $2, 'ACTIVE', 'INBOUND_KRX_GLOBAL_STOCK', $3, $4, 1, NOW(), NOW())
                    "#,
                )
                .bind(new_rec_id)
                .bind(target_node_id)
                .bind(&row)
                .bind(&search_val)
                .execute(pool)
                .await?;

                // Audit History
                let history_id = Uuid::new_v4();
                let _ = sqlx::query(
                    r#"
                    INSERT INTO record_history (id, record_id, change_type, changed_by, source_system, previous_data, new_data, version, changed_at)
                    VALUES ($1, $2, 'INBOUND_INGEST', $3, 'KRX & Global Stock Inbound', NULL, $4, 1, NOW())
                    "#,
                )
                .bind(history_id)
                .bind(new_rec_id)
                .bind(requested_by)
                .bind(&row)
                .execute(pool)
                .await;

                // Also record individual integration log if channel exists
                if let Some(cid) = stock_channel_id {
                    let log_id = Uuid::new_v4();
                    let inbound_payload = serde_json::to_string(&row_obj).unwrap_or_default();
                    let mapped_payload = serde_json::to_string(&row).unwrap_or_default();
                    let _ = sqlx::query(
                        r#"
                        INSERT INTO integration_logs (id, channel_id, record_id, event_type, status, original_payload, mapped_payload, created_at, retry_count)
                        VALUES ($1, $2, $3, 'INBOUND_INGEST', 'SUCCESS', $4, $5, NOW(), 0)
                        "#
                    )
                    .bind(log_id)
                    .bind(cid)
                    .bind(new_rec_id)
                    .bind(inbound_payload)
                    .bind(mapped_payload)
                    .execute(pool)
                    .await;
                }

                total_created += 1;
            }

            *seeded_by_market.entry(market_code).or_insert(0) += 1;
            total_seeded += 1;
        }

        let total_written = total_created + total_merged;
        let total_skipped = total_read.saturating_sub(total_written);

        tracing::info!(
            "✅ Stock data ingestion completed: total_read={}, created={}, merged={}, unchanged={}, markets={:?}",
            total_read,
            total_created,
            total_merged,
            total_skipped,
            seeded_by_market
        );

        // Record summary integration log if stock channel exists
        if let Some(cid) = stock_channel_id {
            let log_id = Uuid::new_v4();
            let original = format!(
                "Spring Batch Completed. Read: {}, Written: {}, Skipped: {}",
                total_read, total_written, total_skipped
            );
            let mapped = serde_json::json!({
                "readCount": total_read,
                "writeCount": total_written,
                "createdCount": total_created,
                "mergedCount": total_merged,
                "skippedCount": total_skipped,
                "status": "COMPLETED"
            })
            .to_string();

            let _ = sqlx::query(
                r#"
                INSERT INTO integration_logs (id, channel_id, event_type, status, original_payload, mapped_payload, created_at, retry_count)
                VALUES ($1, $2, 'SPRING_BATCH_STOCK_INGESTION', 'SUCCESS', $3, $4, NOW(), 0)
                "#
            )
            .bind(log_id)
            .bind(cid)
            .bind(original)
            .bind(mapped)
            .execute(pool)
            .await;
        }

        Ok(StockSeedResponse {
            domain_id,
            domain_name,
            total_seeded: total_read,
            total_created,
            total_merged,
            total_deleted,
            seeded_by_market,
            message: format!(
                "총 {}개의 실제 상장 주식(신규: {}건, 머지: {}건, 미변경: {}건) 데이터가 성공적으로 적재/병합되었습니다.",
                total_read, total_created, total_merged, total_skipped
            ),
        })
    }

    fn load_dataset_file() -> anyhow::Result<Vec<serde_json::Value>> {
        let paths = [
            "./data/stock_real_master_data.json",
            "/app/data/stock_real_master_data.json",
            "../backend/src/main/resources/data/stock_real_master_data.json",
            "/home/profavor/dev/mplatform/backend/src/main/resources/data/stock_real_master_data.json",
        ];

        for p in &paths {
            if Path::new(p).exists() {
                let file = File::open(p)?;
                let reader = BufReader::new(file);
                let rows: Vec<serde_json::Value> = serde_json::from_reader(reader)?;
                tracing::info!("Loaded {} stock records from {}", rows.len(), p);
                return Ok(rows);
            }
        }

        const EMBEDDED_MASTER_DATA: &str = include_str!("../../data/stock_real_master_data.json");
        let rows: Vec<serde_json::Value> = serde_json::from_str(EMBEDDED_MASTER_DATA)?;
        tracing::info!("Loaded {} stock records from embedded data", rows.len());
        Ok(rows)
    }

    pub async fn fetch_all_live_market_data() -> HashMap<String, serde_json::Value> {
        let client = reqwest::Client::builder()
            .timeout(std::time::Duration::from_secs(10))
            .build()
            .unwrap_or_else(|_| reqwest::Client::new());

        let mut live_map = HashMap::new();
        let mut join_set = tokio::task::JoinSet::new();

        // KOSPI: 15 pages (1500 items)
        for page in 1..=15 {
            let cl = client.clone();
            join_set.spawn(async move { Self::fetch_domestic_page(&cl, "KOSPI", page).await });
        }

        // KOSDAQ: 20 pages (2000 items)
        for page in 1..=20 {
            let cl = client.clone();
            join_set.spawn(async move { Self::fetch_domestic_page(&cl, "KOSDAQ", page).await });
        }

        // NASDAQ: 3 pages (300 items)
        for page in 1..=3 {
            let cl = client.clone();
            join_set.spawn(async move { Self::fetch_global_page(&cl, "NASDAQ", page).await });
        }

        // NYSE: 3 pages (300 items)
        for page in 1..=3 {
            let cl = client.clone();
            join_set.spawn(async move { Self::fetch_global_page(&cl, "NYSE", page).await });
        }

        while let Some(res) = join_set.join_next().await {
            if let Ok(Ok(items)) = res {
                for item in items {
                    if let Some(ticker) = item.get("ticker_code").and_then(|v| v.as_str()) {
                        if !ticker.is_empty() {
                            live_map.insert(ticker.to_string(), item);
                        }
                    }
                }
            }
        }

        tracing::info!(
            "Pre-fetched {} live market quotes into in-memory map",
            live_map.len()
        );
        live_map
    }

    async fn fetch_domestic_page(
        client: &reqwest::Client,
        market: &str,
        page: usize,
    ) -> anyhow::Result<Vec<serde_json::Value>> {
        let url = format!(
            "https://m.stock.naver.com/api/stocks/marketValue/{}?page={}&pageSize=100",
            market, page
        );
        let resp = client
            .get(&url)
            .header(
                "User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36",
            )
            .send()
            .await?;

        if !resp.status().is_success() {
            anyhow::bail!("Failed HTTP {}", resp.status());
        }

        let json: serde_json::Value = resp.json().await?;
        let stocks = json
            .get("stocks")
            .and_then(|v| v.as_array())
            .cloned()
            .unwrap_or_default();

        let clean_num = |v: Option<&serde_json::Value>| -> f64 {
            match v {
                Some(serde_json::Value::Number(n)) => n.as_f64().unwrap_or(0.0),
                Some(serde_json::Value::String(s)) => s
                    .replace(",", "")
                    .replace("%", "")
                    .trim()
                    .parse()
                    .unwrap_or(0.0),
                _ => 0.0,
            }
        };

        let mut results = Vec::new();
        for item in stocks {
            let ticker = item
                .get("itemCode")
                .and_then(|v| v.as_str())
                .unwrap_or("")
                .to_string();
            if ticker.is_empty() {
                continue;
            }
            let name = item
                .get("stockName")
                .and_then(|v| v.as_str())
                .unwrap_or("")
                .to_string();
            let close_price =
                clean_num(item.get("closePriceRaw").or_else(|| item.get("closePrice")));
            let change_price = clean_num(
                item.get("compareToPreviousClosePriceRaw")
                    .or_else(|| item.get("compareToPreviousClosePrice")),
            );
            let fluc_rate = clean_num(
                item.get("fluctuationsRatioRaw")
                    .or_else(|| item.get("fluctuationsRatio")),
            );
            let volume = clean_num(
                item.get("accumulatedTradingVolumeRaw")
                    .or_else(|| item.get("accumulatedTradingVolume")),
            ) as i64;
            let value = clean_num(
                item.get("accumulatedTradingValueRaw")
                    .or_else(|| item.get("accumulatedTradingValue")),
            ) as i64;
            let mkt_cap = clean_num(
                item.get("marketValueRaw")
                    .or_else(|| item.get("marketValue")),
            ) as i64;
            let logo = item
                .get("itemLogoUrl")
                .or_else(|| item.get("itemLogoPngUrl"))
                .and_then(|v| v.as_str())
                .unwrap_or("")
                .to_string();
            let traded_at = item
                .get("localTradedAt")
                .and_then(|v| v.as_str())
                .unwrap_or("")
                .to_string();

            let mut row = serde_json::json!({
                "ticker_code": ticker,
                "current_price": close_price as i64,
                "change_price": change_price,
                "fluctuation_rate": fluc_rate,
                "accumulated_trading_volume": volume,
                "accumulated_trading_value": value,
                "market_cap": mkt_cap,
                "price_base_date": if !traded_at.is_empty() { traded_at.chars().take(10).collect::<String>() } else { chrono::Utc::now().naive_utc().format("%Y-%m-%d").to_string() }
            });
            if !name.is_empty() {
                row["stock_name"] = serde_json::Value::String(name);
            }
            if !logo.is_empty() {
                row["logo_image_url"] = serde_json::Value::String(logo);
            }
            results.push(row);
        }
        Ok(results)
    }

    async fn fetch_global_page(
        client: &reqwest::Client,
        exchange: &str,
        page: usize,
    ) -> anyhow::Result<Vec<serde_json::Value>> {
        let url = format!(
            "https://api.stock.naver.com/stock/exchange/{}/marketValue?page={}&pageSize=100",
            exchange, page
        );
        let resp = client
            .get(&url)
            .header(
                "User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36",
            )
            .send()
            .await?;

        if !resp.status().is_success() {
            anyhow::bail!("Failed HTTP {}", resp.status());
        }

        let json: serde_json::Value = resp.json().await?;
        let stocks = json
            .get("stocks")
            .and_then(|v| v.as_array())
            .cloned()
            .unwrap_or_default();

        let clean_num = |v: Option<&serde_json::Value>| -> f64 {
            match v {
                Some(serde_json::Value::Number(n)) => n.as_f64().unwrap_or(0.0),
                Some(serde_json::Value::String(s)) => s
                    .replace(",", "")
                    .replace("%", "")
                    .trim()
                    .parse()
                    .unwrap_or(0.0),
                _ => 0.0,
            }
        };

        let mut results = Vec::new();
        for item in stocks {
            let ticker = item
                .get("symbolCode")
                .or_else(|| item.get("itemCode"))
                .and_then(|v| v.as_str())
                .unwrap_or("")
                .to_string();
            if ticker.is_empty() {
                continue;
            }
            let name = item
                .get("stockName")
                .and_then(|v| v.as_str())
                .unwrap_or("")
                .to_string();
            let close_price =
                clean_num(item.get("closePriceRaw").or_else(|| item.get("closePrice")));
            let change_price = clean_num(
                item.get("compareToPreviousClosePriceRaw")
                    .or_else(|| item.get("compareToPreviousClosePrice")),
            );
            let fluc_rate = clean_num(
                item.get("fluctuationsRatioRaw")
                    .or_else(|| item.get("fluctuationsRatio")),
            );
            let volume = clean_num(
                item.get("accumulatedTradingVolumeRaw")
                    .or_else(|| item.get("accumulatedTradingVolume")),
            ) as i64;
            let value = clean_num(
                item.get("accumulatedTradingValueRaw")
                    .or_else(|| item.get("accumulatedTradingValue")),
            ) as i64;
            let mkt_cap = clean_num(
                item.get("marketValueRaw")
                    .or_else(|| item.get("marketValue")),
            ) as i64;
            let logo = item
                .get("itemLogoUrl")
                .or_else(|| item.get("itemLogoPngUrl"))
                .and_then(|v| v.as_str())
                .unwrap_or("")
                .to_string();
            let traded_at = item
                .get("localTradedAt")
                .and_then(|v| v.as_str())
                .unwrap_or("")
                .to_string();

            let mut row = serde_json::json!({
                "ticker_code": ticker,
                "current_price": close_price as i64,
                "change_price": change_price,
                "fluctuation_rate": fluc_rate,
                "accumulated_trading_volume": volume,
                "accumulated_trading_value": value,
                "market_cap": mkt_cap,
                "price_base_date": if !traded_at.is_empty() { traded_at.chars().take(10).collect::<String>() } else { chrono::Utc::now().naive_utc().format("%Y-%m-%d").to_string() }
            });
            if !name.is_empty() {
                row["stock_name"] = serde_json::Value::String(name);
            }
            if !logo.is_empty() {
                row["logo_image_url"] = serde_json::Value::String(logo);
            }
            results.push(row);
        }
        Ok(results)
    }

    pub async fn fetch_realtime_market_data(
        market: &str,
        page_size: usize,
    ) -> anyhow::Result<Vec<serde_json::Value>> {
        let client = reqwest::Client::builder()
            .timeout(std::time::Duration::from_secs(10))
            .build()?;

        let market_api = match market.to_uppercase().as_str() {
            "KOSDAQ" => "KOSDAQ",
            "KONEX" => "KONEX",
            _ => "KOSPI",
        };

        let url = format!(
            "https://m.stock.naver.com/api/stocks/marketValue/{}?page=1&pageSize={}",
            market_api,
            page_size.min(100)
        );

        let resp = client
            .get(&url)
            .header(
                "User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36",
            )
            .send()
            .await?;

        if !resp.status().is_success() {
            anyhow::bail!("Failed to fetch live stock API: HTTP {}", resp.status());
        }

        let json_body: serde_json::Value = resp.json().await?;
        let stocks = json_body
            .get("stocks")
            .and_then(|v| v.as_array())
            .cloned()
            .unwrap_or_default();

        let clean_num = |v: Option<&serde_json::Value>| -> f64 {
            match v {
                Some(serde_json::Value::Number(n)) => n.as_f64().unwrap_or(0.0),
                Some(serde_json::Value::String(s)) => s
                    .replace(",", "")
                    .replace("%", "")
                    .trim()
                    .parse()
                    .unwrap_or(0.0),
                _ => 0.0,
            }
        };

        let mut results = Vec::new();
        for item in stocks {
            let ticker = item
                .get("itemCode")
                .and_then(|v| v.as_str())
                .unwrap_or("")
                .to_string();
            if ticker.is_empty() {
                continue;
            }
            let name = item
                .get("stockName")
                .and_then(|v| v.as_str())
                .unwrap_or("")
                .to_string();
            let close_price =
                clean_num(item.get("closePriceRaw").or_else(|| item.get("closePrice")));
            let change_price = clean_num(
                item.get("compareToPreviousClosePriceRaw")
                    .or_else(|| item.get("compareToPreviousClosePrice")),
            );
            let fluc_rate = clean_num(item.get("fluctuationsRatio"));
            let volume = clean_num(
                item.get("accumulatedTradingVolumeRaw")
                    .or_else(|| item.get("accumulatedTradingVolume")),
            ) as i64;
            let value = clean_num(
                item.get("accumulatedTradingValueRaw")
                    .or_else(|| item.get("accumulatedTradingValue")),
            ) as i64;
            let mkt_cap = clean_num(
                item.get("marketValueRaw")
                    .or_else(|| item.get("marketValue")),
            ) as i64;
            let logo = item
                .get("itemLogoUrl")
                .and_then(|v| v.as_str())
                .unwrap_or("")
                .to_string();
            let traded_at = item
                .get("localTradedAt")
                .and_then(|v| v.as_str())
                .unwrap_or("")
                .to_string();

            let row = serde_json::json!({
                "market_node_code": market_api,
                "ticker_code": ticker,
                "stock_name": name,
                "current_price": close_price as i64,
                "change_price": change_price,
                "fluctuation_rate": fluc_rate,
                "accumulated_trading_volume": volume,
                "accumulated_trading_value": value,
                "market_cap": mkt_cap,
                "logo_image_url": logo,
                "price_base_date": if !traded_at.is_empty() { traded_at.chars().take(10).collect::<String>() } else { chrono::Utc::now().naive_utc().format("%Y-%m-%d").to_string() }
            });
            results.push(row);
        }
        Ok(results)
    }

    pub async fn fetch_single_stock_realtime(ticker: &str) -> anyhow::Result<serde_json::Value> {
        let client = reqwest::Client::builder()
            .timeout(std::time::Duration::from_secs(5))
            .build()?;

        let url = format!("https://m.stock.naver.com/api/stock/{}/basic", ticker);
        let resp = client
            .get(&url)
            .header(
                "User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36",
            )
            .send()
            .await?;

        if !resp.status().is_success() {
            anyhow::bail!(
                "Failed to fetch live stock {}: HTTP {}",
                ticker,
                resp.status()
            );
        }

        let item: serde_json::Value = resp.json().await?;
        let name = item
            .get("stockName")
            .and_then(|v| v.as_str())
            .unwrap_or("")
            .to_string();

        let clean_num = |v: Option<&serde_json::Value>| -> f64 {
            match v {
                Some(serde_json::Value::Number(n)) => n.as_f64().unwrap_or(0.0),
                Some(serde_json::Value::String(s)) => s
                    .replace(",", "")
                    .replace("%", "")
                    .trim()
                    .parse()
                    .unwrap_or(0.0),
                _ => 0.0,
            }
        };

        let close_price = clean_num(item.get("closePrice"));
        let change_price = clean_num(item.get("compareToPreviousClosePrice"));
        let fluc_rate = clean_num(item.get("fluctuationsRatio"));
        let traded_at = item
            .get("localTradedAt")
            .and_then(|v| v.as_str())
            .unwrap_or("")
            .to_string();

        Ok(serde_json::json!({
            "ticker_code": ticker,
            "stock_name": name,
            "current_price": close_price as i64,
            "change_price": change_price,
            "fluctuation_rate": fluc_rate,
            "price_base_date": if !traded_at.is_empty() { traded_at.chars().take(10).collect::<String>() } else { chrono::Utc::now().naive_utc().format("%Y-%m-%d").to_string() }
        }))
    }
}
