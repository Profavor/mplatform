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

    pub fn normalize_multilingual_stock_name(
        stock_name_val: Option<&serde_json::Value>,
        stock_name_en_val: Option<&serde_json::Value>,
        ticker: &str,
    ) -> serde_json::Value {
        let mut ko = String::new();
        let mut en = String::new();

        if let Some(val) = stock_name_val {
            if let Some(obj) = val.as_object() {
                if let Some(k) = obj.get("ko").and_then(|v| v.as_str()) {
                    ko = k.to_string();
                }
                if let Some(e) = obj.get("en").and_then(|v| v.as_str()) {
                    en = e.to_string();
                }
            } else if let Some(s) = val.as_str() {
                let trimmed = s.trim();
                if trimmed.starts_with('{') && trimmed.ends_with('}') {
                    if let Ok(parsed) = serde_json::from_str::<serde_json::Value>(trimmed) {
                        if let Some(obj) = parsed.as_object() {
                            if let Some(k) = obj.get("ko").and_then(|v| v.as_str()) {
                                ko = k.to_string();
                            }
                            if let Some(e) = obj.get("en").and_then(|v| v.as_str()) {
                                en = e.to_string();
                            }
                        }
                    }
                }
                if ko.is_empty() {
                    ko = trimmed.to_string();
                }
            }
        }

        if en.is_empty() {
            if let Some(val_en) = stock_name_en_val {
                if let Some(s) = val_en.as_str() {
                    let trimmed = s.trim();
                    if !trimmed.is_empty() && trimmed != ticker {
                        en = trimmed.to_string();
                    }
                }
            }
        }

        if en.is_empty() {
            en = ko.clone();
        }

        if ko.is_empty() {
            ko = if !en.is_empty() { en.clone() } else { ticker.to_string() };
        }

        serde_json::json!({
            "ko": ko,
            "en": en
        })
    }

    pub fn merge_stock_name(
        prev_val: Option<&serde_json::Value>,
        incoming_val: &serde_json::Value,
        ticker: &str,
    ) -> serde_json::Value {
        let incoming_norm = Self::normalize_multilingual_stock_name(Some(incoming_val), None, ticker);
        let prev_norm = prev_val.map(|p| Self::normalize_multilingual_stock_name(Some(p), None, ticker));

        if let Some(prev) = prev_norm {
            let mut prev_obj = prev.as_object().cloned().unwrap_or_default();
            let in_obj = incoming_norm.as_object().cloned().unwrap_or_default();

            if let Some(ko) = in_obj.get("ko") {
                let ko_str = ko.as_str().unwrap_or("");
                if !ko_str.is_empty() {
                    prev_obj.insert("ko".to_string(), ko.clone());
                }
            }
            if let Some(en) = in_obj.get("en") {
                let en_str = en.as_str().unwrap_or("");
                let ko_str = prev_obj.get("ko").and_then(|v| v.as_str()).unwrap_or("");
                // Do not overwrite a distinct existing English name with an identical Korean fallback
                if !en_str.is_empty() && (en_str != ko_str || !prev_obj.contains_key("en")) {
                    prev_obj.insert("en".to_string(), en.clone());
                }
            }
            serde_json::Value::Object(prev_obj)
        } else {
            incoming_norm
        }
    }

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

        // 1.1 Ensure all stock domain field definitions exist (aikstockdata.com fields)
        Self::ensure_stock_field_definitions(pool, domain_id).await?;

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

        // 4. Pre-fetch Live Market Quotes across KRX (aikstockdata.com) and Global exchanges (preserved)
        let live_map = Self::fetch_all_live_market_data().await;

        // 5. Load Master Stock Data (all records, combined with aikstock live universe)
        let stock_rows: Vec<serde_json::Value> = match req.custom_rows {
            Some(rows) if !rows.is_empty() => rows,
            _ => Self::load_master_with_live_universe(&live_map)?,
        };

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

            // Extract raw stock name and stock_name_en before removing
            let raw_stock_name = row.get("stock_name").cloned();
            let raw_stock_name_en = row.get("stock_name_en").cloned();
            let normalized_stock_name = Self::normalize_multilingual_stock_name(
                raw_stock_name.as_ref(),
                raw_stock_name_en.as_ref(),
                &ticker,
            );

            // Remove market_node_code and deleted field stock_name_en before saving
            if let Some(obj) = row.as_object_mut() {
                obj.remove("market_node_code");
                obj.remove("stock_name_en");
                obj.insert("stock_name".to_string(), normalized_stock_name);
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

                // Clean up deleted field stock_name_en
                prev_obj.remove("stock_name_en");

                // Merge incoming fields
                for (k, v) in &row_obj {
                    if k == "stock_name_en" {
                        continue;
                    }
                    if k == "stock_name" {
                        let prev_sn = prev_obj.get("stock_name");
                        let merged_sn = Self::merge_stock_name(prev_sn, v, &ticker);
                        prev_obj.insert("stock_name".to_string(), merged_sn);
                        continue;
                    }
                    prev_obj.insert(k.clone(), v.clone());
                }

                // Ensure stock_name is a valid multilingual object even if not updated in incoming row
                if let Some(cur_sn) = prev_obj.get("stock_name") {
                    if !cur_sn.is_object() {
                        let converted = Self::normalize_multilingual_stock_name(Some(cur_sn), None, &ticker);
                        prev_obj.insert("stock_name".to_string(), converted);
                    }
                }

                let merged_data = serde_json::Value::Object(prev_obj);

                // Change detection: If completely identical, skip version increment & redundant history
                if original_prev_data == merged_data {
                    *seeded_by_market.entry(market_code).or_insert(0) += 1;
                    total_seeded += 1;
                    continue;
                }

                let new_version = ex.version + 1;

                let stock_name_search = match merged_data.get("stock_name") {
                    Some(serde_json::Value::Object(map)) => {
                        let ko = map.get("ko").and_then(|v| v.as_str()).unwrap_or("");
                        let en = map.get("en").and_then(|v| v.as_str()).unwrap_or("");
                        format!("{} {}", ko, en).trim().to_string()
                    }
                    Some(serde_json::Value::String(s)) => s.clone(),
                    _ => String::new(),
                };
                let search_text = format!(
                    "{} {} {}",
                    stock_name_search,
                    ticker,
                    merged_data
                        .get("industry_sector")
                        .and_then(|v| v.as_str())
                        .unwrap_or("")
                );
                let search_val = serde_json::Value::String(search_text);

                sqlx::query(
                    r#"
                    UPDATE record
                    SET data = $1, searchable_data = $2, version = $3, node_id = $4, updated_at = NOW()
                    WHERE id = $5
                    "#,
                )
                .bind(&merged_data)
                .bind(&search_val)
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
                let stock_name_search = match row.get("stock_name") {
                    Some(serde_json::Value::Object(map)) => {
                        let ko = map.get("ko").and_then(|v| v.as_str()).unwrap_or("");
                        let en = map.get("en").and_then(|v| v.as_str()).unwrap_or("");
                        format!("{} {}", ko, en).trim().to_string()
                    }
                    Some(serde_json::Value::String(s)) => s.clone(),
                    _ => String::new(),
                };
                let search_text = format!(
                    "{} {} {}",
                    stock_name_search,
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

    pub async fn ensure_stock_field_definitions(
        pool: &PgPool,
        domain_id: Uuid,
    ) -> anyhow::Result<()> {
        // 1. Ensure new field groups exist under appropriate sectors
        let sector_shares: Option<(Uuid,)> = sqlx::query_as(
            "SELECT id FROM sector WHERE domain_id = $1 AND name->>'ko' LIKE '%발행%' LIMIT 1"
        ).bind(domain_id).fetch_optional(pool).await?;

        let sector_valuation: Option<(Uuid,)> = sqlx::query_as(
            "SELECT id FROM sector WHERE domain_id = $1 AND name->>'ko' LIKE '%투자 가치%' LIMIT 1"
        ).bind(domain_id).fetch_optional(pool).await?;

        let sector_ir: Option<(Uuid,)> = sqlx::query_as(
            "SELECT id FROM sector WHERE domain_id = $1 AND name->>'ko' LIKE '%IR%' LIMIT 1"
        ).bind(domain_id).fetch_optional(pool).await?;

        if let Some((sec_id,)) = sector_shares {
            sqlx::query(
                r#"INSERT INTO field_group (id, domain_id, sector_id, name, sort_order, is_default_open)
                   VALUES ('a52e0001-5252-4000-8000-000000000001', $1, $2,
                           '{"ko": "52주 가격 및 변동 지표", "en": "52-Week Price Range & Trends"}', 4, true)
                   ON CONFLICT (id) DO NOTHING"#
            ).bind(domain_id).bind(sec_id).execute(pool).await?;
        }

        if let Some((sec_id,)) = sector_valuation {
            sqlx::query(
                r#"INSERT INTO field_group (id, domain_id, sector_id, name, sort_order, is_default_open)
                   VALUES ('a52e0002-5252-4000-8000-000000000002', $1, $2,
                           '{"ko": "AI 스크리닝 및 랭킹", "en": "AI Screening & Ranking"}', 3, true)
                   ON CONFLICT (id) DO NOTHING"#
            ).bind(domain_id).bind(sec_id).execute(pool).await?;
        }

        if let Some((sec_id,)) = sector_ir {
            sqlx::query(
                r#"INSERT INTO field_group (id, domain_id, sector_id, name, sort_order, is_default_open)
                   VALUES ('a52e0003-5252-4000-8000-000000000003', $1, $2,
                           '{"ko": "최근 주요 공시 및 AI 분석", "en": "Recent Disclosures & AI Analysis"}', 2, true)
                   ON CONFLICT (id) DO NOTHING"#
            ).bind(domain_id).bind(sec_id).execute(pool).await?;
        }

        // 2. Ensure fields with proper field_group_id
        let fields_to_ensure = [
            ("op_income_annualized", "연환산 영업이익(원)", "Annualized Operating Income (KRW)", "NUMBER", 58, "df5a8b3e-e927-4c49-b75e-0d6eca6aa8f0"),
            ("cap_to_op_multiple", "시총/영업이익 배수", "Market Cap to OP Multiple", "NUMBER", 59, "df5a8b3e-e927-4c49-b75e-0d6eca6aa8f0"),
            ("is_turnaround", "흑자전환 여부", "Is Turnaround to Profit", "BOOLEAN", 60, "df5a8b3e-e927-4c49-b75e-0d6eca6aa8f0"),
            ("in_growth_top", "성장 랭킹 TOP 선정 여부", "In Growth Ranking Top", "BOOLEAN", 61, "a52e0002-5252-4000-8000-000000000002"),
            ("in_quiet_top", "조용한 실적주 선정 여부", "In Quiet Performer Top", "BOOLEAN", 62, "a52e0002-5252-4000-8000-000000000002"),
            ("is_52w_high", "52주 신고가 도달 여부", "Is 52-Week High", "BOOLEAN", 63, "a52e0001-5252-4000-8000-000000000001"),
            ("is_52w_low", "52주 신저가 도달 여부", "Is 52-Week Low", "BOOLEAN", 64, "a52e0001-5252-4000-8000-000000000001"),
            ("drawdown_from_52w_high", "52주 최고가 대비 낙폭(%)", "Drawdown from 52W High (%)", "NUMBER", 65, "a52e0001-5252-4000-8000-000000000001"),
            ("pct_from_52w_low", "52주 최저가 대비 상승률(%)", "Pct from 52W Low (%)", "NUMBER", 66, "a52e0001-5252-4000-8000-000000000001"),
            ("fiscal_period", "실측 결산 기수", "Fiscal Period", "TEXT", 67, "69c69f8c-382e-443c-9d1d-c83c4b90b189"),
            ("recent_disclosure_title", "최근 주요 공시 제목", "Recent Disclosure Title", "TEXT", 68, "a52e0003-5252-4000-8000-000000000003"),
            ("recent_disclosure_fact", "최근 주요 공시 쉬운 풀이", "Recent Disclosure Easy Summary", "TEXT", 69, "a52e0003-5252-4000-8000-000000000003"),
            ("recent_disclosure_score", "최근 주요 공시 AI 중요도 점수", "Recent Disclosure AI Score", "NUMBER", 70, "a52e0003-5252-4000-8000-000000000003"),
            ("recent_disclosure_url", "최근 공시 DART 링크", "Recent Disclosure DART URL", "TEXT", 71, "a52e0003-5252-4000-8000-000000000003"),
        ];

        for (key, ko, en, ftype, order, group_id_str) in fields_to_ensure {
            let name_json = serde_json::json!({ "ko": ko, "en": en });
            let gid: Uuid = group_id_str.parse().unwrap_or_default();
            sqlx::query(
                r#"
                INSERT INTO field_definition (
                    id, domain_id, field_key, name, type, field_order, field_group_id,
                    required, is_searchable, is_multi_value, is_table, is_encrypted, is_removed,
                    is_indexed, is_hidden, is_highlighted, is_immutable, is_read_only, created_at, updated_at
                )
                VALUES (gen_random_uuid(), $1, $2, $3, $4, $5, $6, false, true, false, false, false, false, false, false, false, false, false, NOW(), NOW())
                ON CONFLICT (domain_id, field_key) DO UPDATE
                SET field_group_id = COALESCE(field_definition.field_group_id, EXCLUDED.field_group_id),
                    updated_at = NOW()
                "#,
            )
            .bind(domain_id)
            .bind(key)
            .bind(name_json)
            .bind(ftype)
            .bind(order)
            .bind(gid)
            .execute(pool)
            .await?;
        }
        Ok(())
    }

    pub fn load_master_with_live_universe(
        live_map: &HashMap<String, serde_json::Value>,
    ) -> anyhow::Result<Vec<serde_json::Value>> {
        let base_rows = Self::load_dataset_file().unwrap_or_default();
        let mut master_map: HashMap<String, serde_json::Value> = HashMap::new();

        for row in base_rows {
            if let Some(ticker) = row.get("ticker_code").and_then(|v| v.as_str()) {
                if !ticker.is_empty() {
                    master_map.insert(ticker.to_string(), row);
                }
            }
        }

        // Merge or insert all live stocks from aikstockdata & global feeds
        for (ticker, live_val) in live_map {
            if let Some(existing_row) = master_map.get_mut(ticker) {
                if let Some(existing_obj) = existing_row.as_object_mut() {
                    if let Some(live_obj) = live_val.as_object() {
                        for (k, v) in live_obj {
                            if !v.is_null() {
                                existing_obj.insert(k.clone(), v.clone());
                            }
                        }
                    }
                }
            } else {
                master_map.insert(ticker.clone(), live_val.clone());
            }
        }

        let mut combined: Vec<serde_json::Value> = master_map.into_values().collect();
        combined.sort_by(|a, b| {
            let m_a = a.get("market_node_code").and_then(|v| v.as_str()).unwrap_or("");
            let m_b = b.get("market_node_code").and_then(|v| v.as_str()).unwrap_or("");
            let t_a = a.get("ticker_code").and_then(|v| v.as_str()).unwrap_or("");
            let t_b = b.get("ticker_code").and_then(|v| v.as_str()).unwrap_or("");
            m_a.cmp(m_b).then_with(|| t_a.cmp(t_b))
        });

        tracing::info!(
            "Combined master data + live universe yielded {} total stocks",
            combined.len()
        );
        Ok(combined)
    }

    pub async fn fetch_aikstock_screen_data(
        client: &reqwest::Client,
    ) -> anyhow::Result<Vec<serde_json::Value>> {
        let url = "https://aikstockdata.com/data/public/screen.json";
        let resp = client
            .get(url)
            .header(
                "User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36",
            )
            .send()
            .await?;

        if !resp.status().is_success() {
            anyhow::bail!("Failed to fetch aikstockdata screen.json: HTTP {}", resp.status());
        }

        let json: serde_json::Value = resp.json().await?;
        let fields = json
            .get("fields")
            .and_then(|v| v.as_array())
            .ok_or_else(|| anyhow::anyhow!("Missing fields array in screen.json"))?;
        let rows = json
            .get("rows")
            .and_then(|v| v.as_array())
            .ok_or_else(|| anyhow::anyhow!("Missing rows array in screen.json"))?;

        let mut field_map: HashMap<String, usize> = HashMap::new();
        for (idx, f) in fields.iter().enumerate() {
            if let Some(s) = f.as_str() {
                field_map.insert(s.to_string(), idx);
            }
        }

        let get_val = |row: &[serde_json::Value], key: &str| -> Option<serde_json::Value> {
            field_map.get(key).and_then(|&idx| row.get(idx).cloned())
        };

        let today = chrono::Utc::now().naive_utc().format("%Y-%m-%d").to_string();
        let mut results = Vec::new();

        for row_val in rows {
            let row_arr = match row_val.as_array() {
                Some(arr) => arr,
                None => continue,
            };

            let code = match get_val(row_arr, "code").and_then(|v| v.as_str().map(|s| s.to_string())) {
                Some(c) if !c.is_empty() => c,
                _ => continue,
            };

            let name = get_val(row_arr, "name")
                .and_then(|v| v.as_str().map(|s| s.to_string()))
                .unwrap_or_default();

            let market = get_val(row_arr, "market")
                .and_then(|v| v.as_str().map(|s| s.to_string()))
                .unwrap_or_else(|| "KOSPI".to_string());

            let mut obj = serde_json::Map::new();
            obj.insert("ticker_code".to_string(), serde_json::Value::String(code));
            obj.insert(
                "stock_name".to_string(),
                serde_json::json!({
                    "ko": name.clone(),
                    "en": name
                }),
            );
            obj.insert("market_type".to_string(), serde_json::Value::String(market.clone()));
            obj.insert("market_node_code".to_string(), serde_json::Value::String(market));
            obj.insert("price_base_date".to_string(), serde_json::Value::String(today.clone()));

            if let Some(v) = get_val(row_arr, "close") {
                if !v.is_null() { obj.insert("current_price".to_string(), v); }
            }
            if let Some(v) = get_val(row_arr, "change_pct") {
                if !v.is_null() { obj.insert("fluctuation_rate".to_string(), v); }
            }
            if let Some(v) = get_val(row_arr, "value_traded_krw") {
                if !v.is_null() { obj.insert("accumulated_trading_value".to_string(), v); }
            }
            if let Some(v) = get_val(row_arr, "market_cap_krw") {
                if !v.is_null() { obj.insert("market_cap".to_string(), v); }
            }
            if let Some(v) = get_val(row_arr, "op_income_annualized_krw") {
                if !v.is_null() { obj.insert("op_income_annualized".to_string(), v); }
            }
            if let Some(v) = get_val(row_arr, "cap_to_op_multiple") {
                if !v.is_null() { obj.insert("cap_to_op_multiple".to_string(), v); }
            }
            if let Some(v) = get_val(row_arr, "is_turnaround") {
                if !v.is_null() { obj.insert("is_turnaround".to_string(), v); }
            }
            if let Some(v) = get_val(row_arr, "in_growth_top") {
                if !v.is_null() { obj.insert("in_growth_top".to_string(), v); }
            }
            if let Some(v) = get_val(row_arr, "in_quiet_top") {
                if !v.is_null() { obj.insert("in_quiet_top".to_string(), v); }
            }
            if let Some(v) = get_val(row_arr, "is_52w_high") {
                if !v.is_null() { obj.insert("is_52w_high".to_string(), v); }
            }
            if let Some(v) = get_val(row_arr, "is_52w_low") {
                if !v.is_null() { obj.insert("is_52w_low".to_string(), v); }
            }
            if let Some(v) = get_val(row_arr, "week52_high") {
                if !v.is_null() { obj.insert("week52_high".to_string(), v); }
            }
            if let Some(v) = get_val(row_arr, "week52_low") {
                if !v.is_null() { obj.insert("week52_low".to_string(), v); }
            }
            if let Some(v) = get_val(row_arr, "drawdown_from_52w_high_pct") {
                if !v.is_null() { obj.insert("drawdown_from_52w_high".to_string(), v); }
            }
            if let Some(v) = get_val(row_arr, "pct_from_52w_low") {
                if !v.is_null() { obj.insert("pct_from_52w_low".to_string(), v); }
            }
            if let Some(v) = get_val(row_arr, "fiscal_period") {
                if !v.is_null() { obj.insert("fiscal_period".to_string(), v); }
            }
            if let Some(v) = get_val(row_arr, "pe_ttm") {
                if !v.is_null() { obj.insert("per".to_string(), v); }
            }
            if let Some(v) = get_val(row_arr, "pb") {
                if !v.is_null() { obj.insert("pbr".to_string(), v); }
            }

            results.push(serde_json::Value::Object(obj));
        }

        tracing::info!("Fetched {} domestic stocks from aikstockdata.com screen.json", results.len());
        Ok(results)
    }

    pub async fn fetch_aikstock_disclosures(
        client: &reqwest::Client,
    ) -> anyhow::Result<HashMap<String, serde_json::Value>> {
        let url = "https://aikstockdata.com/data/public/disclosures_top100.json";
        let resp = client
            .get(url)
            .header(
                "User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36",
            )
            .send()
            .await?;

        if !resp.status().is_success() {
            anyhow::bail!("Failed to fetch disclosures_top100.json: HTTP {}", resp.status());
        }

        let json: serde_json::Value = resp.json().await?;
        let items = json
            .get("items")
            .and_then(|v| v.as_array())
            .cloned()
            .unwrap_or_default();

        let mut map: HashMap<String, serde_json::Value> = HashMap::new();
        for item in items {
            if let Some(code) = item.get("code").and_then(|v| v.as_str()) {
                if !code.is_empty() && !map.contains_key(code) {
                    map.insert(code.to_string(), item);
                }
            }
        }

        tracing::info!("Fetched {} top disclosures from aikstockdata.com", map.len());
        Ok(map)
    }

    pub async fn fetch_all_live_market_data() -> HashMap<String, serde_json::Value> {
        let client = reqwest::Client::builder()
            .timeout(std::time::Duration::from_secs(15))
            .build()
            .unwrap_or_else(|_| reqwest::Client::new());

        let mut live_map = HashMap::new();

        // 1. Domestic: aikstockdata.com screen.json & disclosures_top100.json
        let (screen_res, disc_res) = tokio::join!(
            Self::fetch_aikstock_screen_data(&client),
            Self::fetch_aikstock_disclosures(&client)
        );

        let disc_map = disc_res.unwrap_or_default();

        if let Ok(screen_stocks) = screen_res {
            for mut stock in screen_stocks {
                if let Some(ticker) = stock.get("ticker_code").and_then(|v| v.as_str()).map(|s| s.to_string()) {
                    if let Some(disc) = disc_map.get(&ticker) {
                        if let Some(stock_obj) = stock.as_object_mut() {
                            if let Some(t) = disc.get("title") {
                                stock_obj.insert("recent_disclosure_title".to_string(), t.clone());
                            }
                            if let Some(f) = disc.get("fact") {
                                stock_obj.insert("recent_disclosure_fact".to_string(), f.clone());
                            }
                            if let Some(s) = disc.get("score") {
                                stock_obj.insert("recent_disclosure_score".to_string(), s.clone());
                            }
                            if let Some(u) = disc.get("url") {
                                stock_obj.insert("recent_disclosure_url".to_string(), u.clone());
                            }
                        }
                    }
                    live_map.insert(ticker, stock);
                }
            }
        }

        // 2. Global: NASDAQ & NYSE (Preserved as requested)
        let mut join_set = tokio::task::JoinSet::new();
        for page in 1..=3 {
            let cl = client.clone();
            join_set.spawn(async move { Self::fetch_global_page(&cl, "NASDAQ", page).await });
        }
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
            "Pre-fetched {} live market records (domestic from aikstockdata.com, global from global API)",
            live_map.len()
        );
        live_map
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
            let name_ko = item
                .get("stockName")
                .and_then(|v| v.as_str())
                .unwrap_or("")
                .to_string();
            let name_en = item
                .get("stockNameEng")
                .and_then(|v| v.as_str())
                .unwrap_or("")
                .to_string();
            let ko = if !name_ko.is_empty() { name_ko } else { name_en.clone() };
            let en = if !name_en.is_empty() { name_en } else { ko.clone() };

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
            if !ko.is_empty() || !en.is_empty() {
                row["stock_name"] = serde_json::json!({
                    "ko": ko,
                    "en": en
                });
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
        let mkt_upper = market.to_uppercase();
        if mkt_upper == "KOSPI" || mkt_upper == "KOSDAQ" || mkt_upper == "KONEX" {
            let client = reqwest::Client::builder()
                .timeout(std::time::Duration::from_secs(10))
                .build()?;
            let all = Self::fetch_aikstock_screen_data(&client).await?;
            let filtered: Vec<serde_json::Value> = all
                .into_iter()
                .filter(|s| {
                    s.get("market_node_code")
                        .and_then(|v| v.as_str())
                        .map(|m| m.eq_ignore_ascii_case(&mkt_upper))
                        .unwrap_or(false)
                })
                .take(page_size)
                .collect();
            return Ok(filtered);
        }

        // Global fallback (NASDAQ / NYSE)
        let client = reqwest::Client::builder()
            .timeout(std::time::Duration::from_secs(10))
            .build()?;
        Self::fetch_global_page(&client, &mkt_upper, 1).await
    }

    pub async fn fetch_single_stock_realtime(ticker: &str) -> anyhow::Result<serde_json::Value> {
        let client = reqwest::Client::builder()
            .timeout(std::time::Duration::from_secs(5))
            .build()?;

        // If 6-digit Korean ticker, try aikstockdata.com individual profile endpoint
        if ticker.len() == 6 && ticker.chars().all(|c| c.is_ascii_digit()) {
            let aik_url = format!("https://aikstockdata.com/data/public/s/{}.json", ticker);
            if let Ok(resp) = client
                .get(&aik_url)
                .header(
                    "User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36",
                )
                .send()
                .await
            {
                if resp.status().is_success() {
                    if let Ok(json) = resp.json::<serde_json::Value>().await {
                        let name_ko = json
                            .get("name_ko")
                            .and_then(|v| v.as_str())
                            .unwrap_or("")
                            .to_string();
                        let quote = json.get("quote");
                        let clpr = quote
                            .and_then(|q| q.get("clpr"))
                            .and_then(|v| v.as_f64())
                            .unwrap_or(0.0);
                        let flt_rt = quote
                            .and_then(|q| q.get("fltRt"))
                            .and_then(|v| v.as_f64())
                            .unwrap_or(0.0);
                        let trd_prc = quote
                            .and_then(|q| q.get("trdPrc"))
                            .and_then(|v| v.as_f64())
                            .unwrap_or(0.0);
                        let mrkt_amt = quote
                            .and_then(|q| q.get("mrktTotAmt"))
                            .and_then(|v| v.as_f64())
                            .unwrap_or(0.0);
                        let bas_dt = quote
                            .and_then(|q| q.get("basDt"))
                            .and_then(|v| v.as_str())
                            .unwrap_or("");
                        let signals = json.get("signals");
                        let valuation = json.get("valuation");

                        let mut res = serde_json::json!({
                            "ticker_code": ticker,
                            "stock_name": {
                                "ko": name_ko.clone(),
                                "en": name_ko
                            },
                            "current_price": clpr as i64,
                            "fluctuation_rate": flt_rt,
                            "accumulated_trading_value": trd_prc as i64,
                            "market_cap": mrkt_amt as i64,
                            "price_base_date": if !bas_dt.is_empty() { bas_dt.to_string() } else { chrono::Utc::now().naive_utc().format("%Y-%m-%d").to_string() }
                        });

                        if let Some(sig) = signals {
                            if let Some(t) = sig.get("is_turnaround") { res["is_turnaround"] = t.clone(); }
                            if let Some(g) = sig.get("in_growth_top") { res["in_growth_top"] = g.clone(); }
                            if let Some(q) = sig.get("in_quiet_top") { res["in_quiet_top"] = q.clone(); }
                            if let Some(h) = sig.get("hi52") { res["is_52w_high"] = h.clone(); }
                            if let Some(l) = sig.get("lo52") { res["is_52w_low"] = l.clone(); }
                        }
                        if let Some(val) = valuation {
                            if let Some(p) = val.get("pe_ttm") { res["per"] = p.clone(); }
                            if let Some(b) = val.get("pb") { res["pbr"] = b.clone(); }
                        }
                        return Ok(res);
                    }
                }
            }
        }

        // Fallback for global tickers or unlisted tickers
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
            "stock_name": {
                "ko": name.clone(),
                "en": name
            },
            "current_price": close_price as i64,
            "change_price": change_price,
            "fluctuation_rate": fluc_rate,
            "price_base_date": if !traded_at.is_empty() { traded_at.chars().take(10).collect::<String>() } else { chrono::Utc::now().naive_utc().format("%Y-%m-%d").to_string() }
        }))
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_normalize_korean_stock_fallback_to_korean() {
        let name = serde_json::json!("삼성전자");
        let en_val = serde_json::json!("005930");
        let res = StockDataIngestionJob::normalize_multilingual_stock_name(
            Some(&name),
            Some(&en_val),
            "005930",
        );
        assert_eq!(res["ko"], "삼성전자");
        assert_eq!(res["en"], "삼성전자");
    }

    #[test]
    fn test_normalize_us_stock() {
        let name = serde_json::json!("엔비디아");
        let en_val = serde_json::json!("NVIDIA Corporation");
        let res = StockDataIngestionJob::normalize_multilingual_stock_name(
            Some(&name),
            Some(&en_val),
            "NVDA",
        );
        assert_eq!(res["ko"], "엔비디아");
        assert_eq!(res["en"], "NVIDIA Corporation");
    }

    #[test]
    fn test_merge_stock_name_preserves_en() {
        let prev = serde_json::json!({
            "ko": "삼성전자",
            "en": "Samsung Electronics"
        });
        let incoming = serde_json::json!({
            "ko": "삼성전자(수정)",
            "en": "삼성전자(수정)"
        });
        let merged = StockDataIngestionJob::merge_stock_name(Some(&prev), &incoming, "005930");
        assert_eq!(merged["ko"], "삼성전자(수정)");
        assert_eq!(merged["en"], "Samsung Electronics");
    }

    #[test]
    fn test_load_master_with_live_universe_merges_and_appends() {
        let mut live_map = HashMap::new();
        live_map.insert(
            "005930".to_string(),
            serde_json::json!({
                "ticker_code": "005930",
                "current_price": 260000,
                "is_turnaround": false,
                "in_growth_top": true,
                "cap_to_op_multiple": 3.4
            }),
        );
        live_map.insert(
            "999999".to_string(),
            serde_json::json!({
                "ticker_code": "999999",
                "market_node_code": "KONEX",
                "current_price": 1000,
                "is_turnaround": true
            }),
        );

        let combined = StockDataIngestionJob::load_master_with_live_universe(&live_map)
            .expect("should combine successfully");

        let samsung = combined
            .iter()
            .find(|r| r.get("ticker_code").and_then(|v| v.as_str()) == Some("005930"))
            .expect("005930 must exist");
        assert_eq!(samsung.get("current_price").and_then(|v| v.as_i64()), Some(260000));
        assert_eq!(samsung.get("in_growth_top").and_then(|v| v.as_bool()), Some(true));

        let new_konex = combined
            .iter()
            .find(|r| r.get("ticker_code").and_then(|v| v.as_str()) == Some("999999"))
            .expect("999999 must exist");
        assert_eq!(new_konex.get("is_turnaround").and_then(|v| v.as_bool()), Some(true));
    }
}

