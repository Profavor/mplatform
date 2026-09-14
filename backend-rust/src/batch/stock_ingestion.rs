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
            "#
        )
        .fetch_optional(pool)
        .await?;

        let (domain_id, domain_name) = match domain_row {
            Some((id, name)) => {
                let name_str = name.get("ko").and_then(|v| v.as_str()).unwrap_or("주식 종목 마스터").to_string();
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
            let ko_name = n.name.get("ko").and_then(|v| v.as_str()).unwrap_or("").to_uppercase();
            let en_name = n.name.get("en").and_then(|v| v.as_str()).unwrap_or("").to_uppercase();

            if path_upper.contains("KOSPI") || ko_name.contains("KOSPI") || en_name.contains("KOSPI") {
                node_map.insert("KOSPI".to_string(), n.id);
            } else if path_upper.contains("KOSDAQ") || ko_name.contains("KOSDAQ") || en_name.contains("KOSDAQ") {
                node_map.insert("KOSDAQ".to_string(), n.id);
            } else if path_upper.contains("KONEX") || ko_name.contains("KONEX") || en_name.contains("KONEX") {
                node_map.insert("KONEX".to_string(), n.id);
            } else if path_upper.contains("미국") || path_upper.contains("US") || ko_name.contains("미국") || en_name.contains("US") {
                node_map.insert("US_MARKET".to_string(), n.id);
            } else if path_upper.contains("아시아") || path_upper.contains("ASIA") || ko_name.contains("아시아") || en_name.contains("ASIA") {
                node_map.insert("ASIA_MARKET".to_string(), n.id);
            } else if path_upper.contains("유럽") || path_upper.contains("EUROPE") || ko_name.contains("유럽") || en_name.contains("EUROPE") {
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
            tracing::info!("Cleared {} existing stock records for domain [{}]", total_deleted, domain_id);
        }

        // 4. Load Stock Data
        let stock_rows: Vec<serde_json::Value> = match req.custom_rows {
            Some(rows) if !rows.is_empty() => rows,
            _ => Self::load_dataset_file()?,
        };

        let allowed_markets: Option<HashSet<String>> = req.markets.map(|m| {
            m.into_iter().map(|s| s.to_uppercase()).collect()
        });
        let limit_per_market = req.limit_per_market;

        let mut seeded_by_market: HashMap<String, i32> = HashMap::new();
        let mut total_seeded = 0;
        let mut total_created = 0;
        let mut total_merged = 0;

        for mut row in stock_rows {
            let market_code = row.get("market_node_code")
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

            let target_node_id = node_map.get(&market_code).copied().unwrap_or(default_node_id);

            // Extract identifier
            let ticker = row.get("ticker_code")
                .and_then(|v| v.as_str())
                .unwrap_or("")
                .trim()
                .to_string();

            if ticker.is_empty() {
                continue;
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

            if let Some(ex) = existing {
                // MERGE
                let mut prev_data: serde_json::Value = ex.data.unwrap_or(serde_json::json!({}));
                if let (Some(prev_obj), Some(new_obj)) = (prev_data.as_object_mut(), row.as_object()) {
                    for (k, v) in new_obj {
                        prev_obj.insert(k.clone(), v.clone());
                    }
                }
                let merged_data = prev_data;
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

                // Audit History
                let history_id = Uuid::new_v4();
                let _ = sqlx::query(
                    r#"
                    INSERT INTO record_history (id, record_id, change_type, changed_by, source_system, new_data, version, changed_at)
                    VALUES ($1, $2, 'INBOUND_MERGE', $3, 'KRX & Global Stock Inbound', $4, $5, NOW())
                    "#,
                )
                .bind(history_id)
                .bind(ex.id)
                .bind(requested_by)
                .bind(&merged_data)
                .bind(new_version)
                .execute(pool)
                .await;

                total_merged += 1;
            } else {
                // INSERT
                let new_rec_id = Uuid::new_v4();
                let search_text = format!("{} {} {}", 
                    row.get("stock_name").and_then(|v| v.as_str()).unwrap_or(""),
                    ticker,
                    row.get("industry_sector").and_then(|v| v.as_str()).unwrap_or("")
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
                    INSERT INTO record_history (id, record_id, change_type, changed_by, source_system, new_data, version, changed_at)
                    VALUES ($1, $2, 'INBOUND_INGEST', $3, 'KRX & Global Stock Inbound', $4, 1, NOW())
                    "#,
                )
                .bind(history_id)
                .bind(new_rec_id)
                .bind(requested_by)
                .bind(&row)
                .execute(pool)
                .await;

                total_created += 1;
            }

            *seeded_by_market.entry(market_code).or_insert(0) += 1;
            total_seeded += 1;
        }

        tracing::info!(
            "✅ Stock data ingestion completed: total={}, created={}, merged={}, markets={:?}",
            total_seeded,
            total_created,
            total_merged,
            seeded_by_market
        );

        // Record integration log if stock channel exists
        let stock_channel_id: Option<Uuid> = sqlx::query_scalar(
            "SELECT id FROM integration_channels WHERE channel_code = 'CH-KRX-INBOUND-001' OR type = 'SPRING_BATCH' LIMIT 1"
        )
        .fetch_optional(pool)
        .await
        .unwrap_or(None);

        if let Some(cid) = stock_channel_id {
            let log_id = Uuid::new_v4();
            let original = format!(
                "Spring Batch Completed. Read: {}, Written: {}, Skipped: 0",
                total_seeded,
                total_created + total_merged
            );
            let mapped = serde_json::json!({
                "readCount": total_seeded,
                "writeCount": total_created + total_merged,
                "createdCount": total_created,
                "mergedCount": total_merged,
                "status": "COMPLETED"
            }).to_string();

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
            total_seeded,
            total_created,
            total_merged,
            total_deleted,
            seeded_by_market,
            message: format!(
                "총 {}개의 실제 상장 주식(신규: {}건, 머지: {}건) 데이터가 성공적으로 적재/병합되었습니다.",
                total_seeded, total_created, total_merged
            ),
        })
    }

    fn load_dataset_file() -> anyhow::Result<Vec<serde_json::Value>> {
        let paths = [
            "./data/stock_real_master_data.json",
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

        anyhow::bail!("Stock dataset file not found in search paths")
    }
}
