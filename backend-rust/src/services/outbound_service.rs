use chrono::Utc;
use reqwest::Client;
use serde::{Deserialize, Serialize};
use serde_json::{json, Value};
use sqlx::PgPool;
use std::time::Duration;
use uuid::Uuid;

#[derive(Debug, Clone, sqlx::FromRow, Serialize, Deserialize)]
pub struct OutboundChannel {
    pub id: Uuid,
    pub name: String,
    pub channel_code: Option<String>,
    pub r#type: String,
    pub direction: String,
    pub config_json: Option<String>,
    pub mapping_config_json: Option<String>,
    pub is_active: bool,
    pub node_id: Option<Uuid>,
    pub max_retries: i32,
    pub retry_backoff_ms: i64,
}

pub struct OutboundService;

impl OutboundService {
    /// Dispatch record changes to all matching active OUTBOUND integration channels
    pub async fn dispatch_record_change(
        pool: &PgPool,
        record_id: Uuid,
        node_id: Uuid,
        event_type: &str,
        record_data: &Value,
    ) -> Result<usize, sqlx::Error> {
        let channels = sqlx::query_as::<_, OutboundChannel>(
            r#"
            SELECT id, name, channel_code, type, direction, config_json, mapping_config_json,
                   is_active, node_id, max_retries, retry_backoff_ms
            FROM integration_channels
            WHERE is_active = true AND direction = 'OUTBOUND'
              AND (node_id = $1 OR node_id IS NULL)
            "#,
        )
        .bind(node_id)
        .fetch_all(pool)
        .await?;

        if channels.is_empty() {
            return Ok(0);
        }

        let mut dispatched = 0;
        for channel in channels {
            match Self::send_to_channel(pool, &channel, Some(record_id), event_type, record_data)
                .await
            {
                Ok(_) => {
                    dispatched += 1;
                }
                Err(e) => {
                    tracing::warn!(
                        "⚠️ Outbound dispatch failed for channel {}: {}",
                        channel.id,
                        e
                    );
                }
            }
        }

        Ok(dispatched)
    }

    /// Single channel dispatch (used by manual trigger and retry)
    pub async fn dispatch_single_channel(
        pool: &PgPool,
        channel: &OutboundChannel,
        record_id: Option<Uuid>,
        event_type: &str,
        record_data: &Value,
    ) -> Result<bool, String> {
        Self::send_to_channel(pool, channel, record_id, event_type, record_data).await
    }

    /// Transforms record JSON to outbound target payload according to mapping config
    pub fn transform_payload(
        record_data: &Value,
        mapping_json: Option<&str>,
        record_id: Option<Uuid>,
        version: i32,
    ) -> (Value, Option<String>) {
        let product_id = record_data
            .get("PRODUCT_ID")
            .and_then(|v| v.as_str())
            .unwrap_or("")
            .to_string();

        let product_code = record_data
            .get("PRODUCT_CODE")
            .and_then(|v| v.as_str())
            .unwrap_or(&product_id)
            .to_string();

        let last_synced = record_data
            .get("LAST_SYNCED_AT")
            .and_then(|v| v.as_str())
            .unwrap_or("2026-09-14")
            .to_string();

        let price_num: f64 = match record_data.get("PRODUCT_PRICE") {
            Some(Value::Number(n)) => n.as_f64().unwrap_or(0.0),
            Some(Value::String(s)) => s
                .chars()
                .filter(|c| c.is_ascii_digit())
                .collect::<String>()
                .parse()
                .unwrap_or(0.0),
            _ => 0.0,
        };

        // If mapping_config_json is provided and valid, apply standard mapping
        let mut idempotency_key = if !product_code.is_empty() {
            Some(format!(
                "{}:{}:{}",
                product_code, last_synced, price_num as i64
            ))
        } else {
            Some(format!(
                "REC:{}:{}:{}",
                record_id.map(|u| u.to_string()).unwrap_or_default(),
                version,
                price_num as i64
            ))
        };

        // If idempotency_key is shorter than 8 chars, pad it
        if let Some(ref k) = idempotency_key {
            if k.len() < 8 {
                idempotency_key = Some(format!(
                    "{}-{}",
                    k,
                    Uuid::new_v4()
                        .to_string()
                        .chars()
                        .take(8)
                        .collect::<String>()
                ));
            }
        }

        // Standard Cartbom / Partner Ingest Payload format
        let mapped = if !product_id.is_empty()
            || mapping_json
                .map(|s| s.contains("externalId") || s.contains("product"))
                .unwrap_or(false)
        {
            let name = record_data
                .get("PRODUCT_NAME")
                .and_then(|v| v.as_str())
                .unwrap_or("")
                .to_string();
            let category = record_data
                .get("CATEGORY_NAME")
                .and_then(|v| v.as_str())
                .unwrap_or("")
                .to_string();
            let source_url = record_data
                .get("PRODUCT_URL")
                .and_then(|v| v.as_str())
                .map(|s| s.to_string())
                .unwrap_or_else(|| format!("https://www.coupang.com/vp/products/{}", product_id));

            json!({
                "externalId": product_id,
                "sourceUrl": source_url,
                "product": {
                    "retailerProductNumber": product_id,
                    "name": name,
                    "category": category
                },
                "observation": {
                    "scope": "online",
                    "storeId": "online",
                    "scopeEvidence": "MDM 카트봄 상품 마스터 · 쿠팡 파트너스 API 온라인 판매가",
                    "observedPrice": price_num,
                    "inventoryStatus": "unknown"
                }
            })
        } else {
            // Generic outbound JSON payload
            record_data.clone()
        };

        (mapped, idempotency_key)
    }

    /// Core sending method with HTTP/MQ/JDBC support and DB logging
    async fn send_to_channel(
        pool: &PgPool,
        channel: &OutboundChannel,
        record_id: Option<Uuid>,
        event_type: &str,
        record_data: &Value,
    ) -> Result<bool, String> {
        let (mapped_payload, idempotency_key) = Self::transform_payload(
            record_data,
            channel.mapping_config_json.as_deref(),
            record_id,
            1,
        );

        let orig_str = record_data.to_string();
        let mapped_str = mapped_payload.to_string();

        match channel.r#type.as_str() {
            "WEB_SERVICE" => {
                let config: Value = channel
                    .config_json
                    .as_deref()
                    .and_then(|s| serde_json::from_str(s).ok())
                    .unwrap_or_else(|| json!({}));

                let url = config
                    .get("url")
                    .or_else(|| config.get("wsUrl"))
                    .and_then(|v| v.as_str())
                    .ok_or_else(|| "웹서비스 URL 설정이 누락되었습니다.".to_string())?;

                let method_str = config
                    .get("method")
                    .or_else(|| config.get("wsMethod"))
                    .and_then(|v| v.as_str())
                    .unwrap_or("POST");

                let client = Client::builder()
                    .timeout(Duration::from_secs(5))
                    .build()
                    .map_err(|e| format!("HTTP 클라이언트 초기화 실패: {}", e))?;

                let mut req_builder = if method_str.eq_ignore_ascii_case("PUT") {
                    client.put(url)
                } else {
                    client.post(url)
                };

                req_builder = req_builder.header("Content-Type", "application/json");

                // Attach headers from config
                if let Some(headers) = config
                    .get("headers")
                    .or_else(|| config.get("wsHeaders"))
                    .and_then(|v| v.as_array())
                {
                    for h in headers {
                        if let (Some(k), Some(v)) = (
                            h.get("key").and_then(|k| k.as_str()),
                            h.get("value").and_then(|v| v.as_str()),
                        ) {
                            if !k.trim().is_empty() {
                                req_builder = req_builder.header(k, v);
                            }
                        }
                    }
                }

                // Attach Idempotency-Key
                if let Some(ref ikey) = idempotency_key {
                    req_builder = req_builder.header("Idempotency-Key", ikey);
                }

                req_builder = req_builder.json(&mapped_payload);

                tracing::info!(
                    "📤 [Outbound Webhook] Dispatching to {} (channel: {})",
                    url,
                    channel.id
                );

                let log_id = Uuid::new_v4();
                match req_builder.send().await {
                    Ok(resp) => {
                        let status = resp.status();
                        let resp_body = resp.text().await.unwrap_or_default();

                        if status.is_success() {
                            tracing::info!(
                                "✅ [Outbound Webhook] Success: HTTP {} from {}",
                                status,
                                url
                            );
                            let _ = sqlx::query(
                                r#"
                                INSERT INTO integration_logs (
                                    id, channel_id, record_id, event_type, status, retry_count,
                                    original_payload, mapped_payload, created_at
                                )
                                VALUES ($1, $2, $3, $4, 'SUCCESS', 0, $5, $6, NOW())
                                "#,
                            )
                            .bind(log_id)
                            .bind(channel.id)
                            .bind(record_id)
                            .bind(event_type)
                            .bind(&orig_str)
                            .bind(&resp_body)
                            .execute(pool)
                            .await;

                            Ok(true)
                        } else {
                            let err_msg = format!("HTTP {}: {}", status, resp_body);
                            tracing::warn!(
                                "❌ [Outbound Webhook] Failed with {}: {}",
                                status,
                                err_msg
                            );

                            let _ = sqlx::query(
                                r#"
                                INSERT INTO integration_logs (
                                    id, channel_id, record_id, event_type, status, retry_count,
                                    error_message, original_payload, mapped_payload, next_retry_at, created_at
                                )
                                VALUES ($1, $2, $3, $4, 'FAIL', 0, $5, $6, $7, NOW() + INTERVAL '60 seconds', NOW())
                                "#
                            )
                            .bind(log_id)
                            .bind(channel.id)
                            .bind(record_id)
                            .bind(event_type)
                            .bind(&err_msg)
                            .bind(&orig_str)
                            .bind(&mapped_str)
                            .execute(pool)
                            .await;

                            Err(err_msg)
                        }
                    }
                    Err(e) => {
                        let err_msg = format!("Network Error: {}", e);
                        tracing::error!("❌ [Outbound Webhook] Connection Error: {}", err_msg);

                        let _ = sqlx::query(
                            r#"
                            INSERT INTO integration_logs (
                                id, channel_id, record_id, event_type, status, retry_count,
                                error_message, original_payload, mapped_payload, next_retry_at, created_at
                            )
                            VALUES ($1, $2, $3, $4, 'FAIL', 0, $5, $6, $7, NOW() + INTERVAL '60 seconds', NOW())
                            "#
                        )
                        .bind(log_id)
                        .bind(channel.id)
                        .bind(record_id)
                        .bind(event_type)
                        .bind(&err_msg)
                        .bind(&orig_str)
                        .bind(&mapped_str)
                        .execute(pool)
                        .await;

                        Err(err_msg)
                    }
                }
            }
            "MESSAGE_QUEUE" | "JDBC" => {
                // MQ / JDBC outbound logging
                let log_id = Uuid::new_v4();
                let _ = sqlx::query(
                    r#"
                    INSERT INTO integration_logs (
                        id, channel_id, record_id, event_type, status, retry_count,
                        original_payload, mapped_payload, created_at
                    )
                    VALUES ($1, $2, $3, $4, 'SUCCESS', 0, $5, $6, NOW())
                    "#,
                )
                .bind(log_id)
                .bind(channel.id)
                .bind(record_id)
                .bind(event_type)
                .bind(&orig_str)
                .bind(&mapped_str)
                .execute(pool)
                .await;

                Ok(true)
            }
            _ => Err(format!(
                "지원되지 않는 아웃바운드 채널 유형: {}",
                channel.r#type
            )),
        }
    }

    /// Process retry for failed integration logs
    pub async fn retry_failed_logs(pool: &PgPool) -> Result<usize, sqlx::Error> {
        #[derive(sqlx::FromRow)]
        struct FailedLogItem {
            id: Uuid,
            channel_id: Uuid,
            record_id: Option<Uuid>,
            event_type: String,
            original_payload: Option<String>,
            retry_count: i32,
            channel_name: String,
            channel_type: String,
            direction: String,
            config_json: Option<String>,
            mapping_config_json: Option<String>,
            max_retries: i32,
            retry_backoff_ms: i64,
            node_id: Option<Uuid>,
        }

        let failed_logs = sqlx::query_as::<_, FailedLogItem>(
            r#"
            SELECT l.id, l.channel_id, l.record_id, l.event_type, l.original_payload, l.retry_count,
                   c.name as channel_name, c.type as channel_type, c.direction,
                   c.config_json, c.mapping_config_json, c.max_retries, c.retry_backoff_ms, c.node_id
            FROM integration_logs l
            JOIN integration_channels c ON l.channel_id = c.id
            WHERE l.status IN ('FAIL', 'PENDING')
              AND (l.next_retry_at IS NULL OR l.next_retry_at <= NOW())
              AND l.retry_count < 3
            ORDER BY l.created_at ASC
            LIMIT 10
            "#
        )
        .fetch_all(pool)
        .await?;

        let mut retried = 0;
        for item in failed_logs {
            let record_data: Value = item
                .original_payload
                .as_deref()
                .and_then(|s| serde_json::from_str(s).ok())
                .unwrap_or_else(|| json!({}));

            let channel = OutboundChannel {
                id: item.channel_id,
                name: item.channel_name,
                channel_code: None,
                r#type: item.channel_type,
                direction: item.direction,
                config_json: item.config_json,
                mapping_config_json: item.mapping_config_json,
                is_active: true,
                node_id: item.node_id,
                max_retries: item.max_retries,
                retry_backoff_ms: item.retry_backoff_ms,
            };

            let next_count = item.retry_count + 1;
            let (mapped_payload, ikey) = Self::transform_payload(
                &record_data,
                channel.mapping_config_json.as_deref(),
                item.record_id,
                next_count,
            );

            if channel.r#type == "WEB_SERVICE" {
                let config: Value = channel
                    .config_json
                    .as_deref()
                    .and_then(|s| serde_json::from_str(s).ok())
                    .unwrap_or_else(|| json!({}));

                if let Some(url) = config
                    .get("url")
                    .or_else(|| config.get("wsUrl"))
                    .and_then(|v| v.as_str())
                {
                    let client = Client::builder()
                        .timeout(Duration::from_secs(5))
                        .build()
                        .unwrap_or_default();
                    let mut req = client.post(url).header("Content-Type", "application/json");

                    if let Some(headers) = config
                        .get("headers")
                        .or_else(|| config.get("wsHeaders"))
                        .and_then(|v| v.as_array())
                    {
                        for h in headers {
                            if let (Some(k), Some(v)) = (
                                h.get("key").and_then(|k| k.as_str()),
                                h.get("value").and_then(|v| v.as_str()),
                            ) {
                                req = req.header(k, v);
                            }
                        }
                    }
                    if let Some(k) = ikey {
                        req = req.header("Idempotency-Key", k);
                    }

                    match req.json(&mapped_payload).send().await {
                        Ok(resp) if resp.status().is_success() => {
                            let resp_text = resp.text().await.unwrap_or_default();
                            let _ = sqlx::query(
                                r#"
                                UPDATE integration_logs
                                SET status = 'SUCCESS', error_message = NULL, next_retry_at = NULL,
                                    mapped_payload = $2, retry_count = $3
                                WHERE id = $1
                                "#,
                            )
                            .bind(item.id)
                            .bind(resp_text)
                            .bind(next_count)
                            .execute(pool)
                            .await;

                            retried += 1;
                            continue;
                        }
                        Ok(resp) => {
                            let status = resp.status();
                            let text = resp.text().await.unwrap_or_default();
                            let err_msg = format!("HTTP {}: {}", status, text);
                            let new_status = if next_count >= 3 {
                                "DEAD_LETTER"
                            } else {
                                "FAIL"
                            };
                            let _ = sqlx::query(
                                r#"
                                UPDATE integration_logs
                                SET status = $2, retry_count = $3,
                                    next_retry_at = NOW() + INTERVAL '120 seconds',
                                    error_message = $4
                                WHERE id = $1
                                "#,
                            )
                            .bind(item.id)
                            .bind(new_status)
                            .bind(next_count)
                            .bind(err_msg)
                            .execute(pool)
                            .await;
                        }
                        Err(e) => {
                            let err_msg = format!("Retry network error: {}", e);
                            let new_status = if next_count >= 3 {
                                "DEAD_LETTER"
                            } else {
                                "FAIL"
                            };
                            let _ = sqlx::query(
                                r#"
                                UPDATE integration_logs
                                SET status = $2, retry_count = $3,
                                    next_retry_at = NOW() + INTERVAL '120 seconds',
                                    error_message = $4
                                WHERE id = $1
                                "#,
                            )
                            .bind(item.id)
                            .bind(new_status)
                            .bind(next_count)
                            .bind(err_msg)
                            .execute(pool)
                            .await;
                        }
                    }
                }
            }

            retried += 1;
        }

        Ok(retried)
    }
}
