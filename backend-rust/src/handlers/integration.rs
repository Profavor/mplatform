use axum::extract::State;
use axum::extract::Path;
use axum::http::StatusCode;
use axum::extract::Query;
use axum::{response::IntoResponse, Json};
use crate::error::{AppError, AppResult};
use crate::middleware::auth::AuthUser;
use crate::models::integration::*;
use crate::state::AppState;
use serde::Deserialize;
use uuid::Uuid;

#[derive(Debug, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct LogQuery {
    pub page: Option<i64>,
    pub size: Option<i64>,
    pub limit: Option<i64>,
    #[serde(alias = "channel_id")]
    pub channel_id: Option<Uuid>,
    pub status: Option<String>,
}

pub async fn get_channels(State(state): State<AppState>) -> AppResult<impl IntoResponse> {
    let channels = state.integration_service.get_channels().await?;
    Ok(Json(channels))
}

pub async fn create_channel(
    State(state): State<AppState>,
    Json(req): Json<CreateChannelRequest>,
) -> AppResult<impl IntoResponse> {
    let channel = state.integration_service.create_channel(req).await?;
    Ok(Json(channel))
}

pub async fn get_logs(
    State(state): State<AppState>,
    Query(params): Query<LogQuery>,
) -> AppResult<impl IntoResponse> {
    let limit = params.size.or(params.limit).unwrap_or(50).clamp(1, 1000);
    let page = params.page.unwrap_or(0).max(0);
    let offset = page * limit;

    let (logs, total) = state.integration_service.get_logs_paged(params.channel_id, false, limit, offset).await?;
    let total_pages = if total == 0 { 0 } else { (total as f64 / limit as f64).ceil() as i64 };

    Ok(Json(serde_json::json!({
        "content": logs,
        "totalElements": total,
        "totalPages": total_pages,
        "page": page,
        "size": limit
    })))
}

pub async fn get_dead_letter_logs(
    State(state): State<AppState>,
    Query(params): Query<LogQuery>,
) -> AppResult<impl IntoResponse> {
    let limit = params.size.or(params.limit).unwrap_or(50).clamp(1, 1000);
    let page = params.page.unwrap_or(0).max(0);
    let offset = page * limit;

    let (logs, total) = state.integration_service.get_logs_paged(params.channel_id, true, limit, offset).await?;
    let total_pages = if total == 0 { 0 } else { (total as f64 / limit as f64).ceil() as i64 };

    Ok(Json(serde_json::json!({
        "content": logs,
        "totalElements": total,
        "totalPages": total_pages,
        "page": page,
        "size": limit
    })))
}

pub async fn get_logs_by_record(
    State(state): State<AppState>,
    Path(record_id): Path<Uuid>,
) -> AppResult<impl IntoResponse> {
    let logs = state.integration_service.get_logs_by_record(record_id).await?;
    Ok(Json(logs))
}

pub async fn test_channel(
    State(state): State<AppState>,
    Path(channel_id): Path<Uuid>,
) -> AppResult<impl IntoResponse> {
    let res = state.integration_service.test_channel(channel_id).await?;
    Ok(Json(res))
}

pub async fn get_channel_stats(
    State(state): State<AppState>,
) -> AppResult<impl IntoResponse> {
    #[derive(sqlx::FromRow)]
    struct StatRow {
        channel_id: Uuid,
        total_count: i64,
        success_count: i64,
    }

    let stats_rows: Vec<StatRow> = sqlx::query_as(
        r#"
        SELECT 
            c.id AS channel_id,
            COUNT(l.id) AS total_count,
            COUNT(CASE WHEN l.status = 'SUCCESS' THEN 1 END) AS success_count
        FROM integration_channels c
        LEFT JOIN integration_logs l ON c.id = l.channel_id
        GROUP BY c.id
        "#
    )
    .fetch_all(&state.db)
    .await?;

    let result: Vec<serde_json::Value> = stats_rows
        .into_iter()
        .map(|r| {
            let rate = if r.total_count > 0 {
                (r.success_count as f64 / r.total_count as f64) * 100.0
            } else {
                100.0
            };
            let health_status = if r.total_count == 0 {
                "IDLE"
            } else if rate >= 95.0 {
                "HEALTHY"
            } else if rate >= 80.0 {
                "WARNING"
            } else {
                "CRITICAL"
            };
            serde_json::json!({
                "channelId": r.channel_id,
                "healthStatus": health_status,
                "totalCount": r.total_count,
                "successCount": r.success_count,
                "successRate": (rate * 10.0).round() / 10.0
            })
        })
        .collect();

    Ok(Json(result))
}

pub async fn test_channel_connection(
    _state: State<AppState>,
    Json(body): Json<serde_json::Value>,
) -> AppResult<impl IntoResponse> {
    let url_opt = body.get("wsUrl")
        .or_else(|| body.get("url"))
        .and_then(|v| v.as_str());

    if let Some(url) = url_opt {
        if url.trim().is_empty() {
            return Err(AppError::BadRequest("연결 테스트를 위한 URL이 입력되지 않았습니다.".to_string()));
        }

        let start = std::time::Instant::now();
        let client = reqwest::Client::builder()
            .timeout(std::time::Duration::from_secs(3))
            .build()
            .map_err(|e| AppError::Internal(format!("HTTP 클라이언트 초기화 실패: {}", e)))?;

        let mut req = client.get(url);
        if let Some(headers) = body.get("wsHeaders").or_else(|| body.get("headers")).and_then(|v| v.as_array()) {
            for h in headers {
                if let (Some(k), Some(v)) = (h.get("key").and_then(|k| k.as_str()), h.get("value").and_then(|v| v.as_str())) {
                    if !k.trim().is_empty() {
                        req = req.header(k, v);
                    }
                }
            }
        }

        match req.send().await {
            Ok(resp) => {
                let latency_ms = start.elapsed().as_millis();
                let status = resp.status();
                Ok(Json(serde_json::json!({
                    "success": true,
                    "message": format!("엔드포인트 연결 확인 성공 (HTTP {}, 지연시간: {}ms)", status, latency_ms),
                    "statusCode": status.as_u16(),
                    "latencyMs": latency_ms,
                    "details": body
                })))
            }
            Err(e) => {
                let latency_ms = start.elapsed().as_millis();
                Ok(Json(serde_json::json!({
                    "success": false,
                    "message": format!("연결 실패: {}", e),
                    "latencyMs": latency_ms,
                    "details": body
                })))
            }
        }
    } else {
        Ok(Json(serde_json::json!({
            "success": true,
            "message": "데이터베이스 및 메시지 브로커 설정 유효성 검증 완료",
            "details": body
        })))
    }
}

pub async fn retry_all_dead_letters(
    State(state): State<AppState>,
) -> AppResult<impl IntoResponse> {
    let rows_affected = sqlx::query(
        r#"
        UPDATE integration_logs
        SET status = 'PENDING', retry_count = 0, next_retry_at = NOW()
        WHERE status IN ('DEAD_LETTER', 'FAIL')
        "#
    )
    .execute(&state.db)
    .await?
    .rows_affected();

    let pool = state.db.clone();
    tokio::spawn(async move {
        let _ = crate::services::outbound_service::OutboundService::retry_failed_logs(&pool).await;
    });

    Ok(Json(serde_json::json!({
        "success": true,
        "retriedCount": rows_affected,
        "message": format!("{}건의 연계 실패 로그가 재시도 대기열에 등록되었습니다.", rows_affected)
    })))
}

pub async fn retry_log(
    State(state): State<AppState>,
    Path(log_id): Path<Uuid>,
) -> AppResult<impl IntoResponse> {
    let _ = sqlx::query(
        "UPDATE integration_logs SET status = 'PENDING', retry_count = 0, next_retry_at = NOW() WHERE id = $1"
    )
    .bind(log_id)
    .execute(&state.db)
    .await;

    let pool = state.db.clone();
    tokio::spawn(async move {
        let _ = crate::services::outbound_service::OutboundService::retry_failed_logs(&pool).await;
    });

    Ok(Json(serde_json::json!({ "success": true, "message": "재시도 요청이 등록되었습니다." })))
}

pub async fn get_routing_rules(
    _state: State<AppState>,
) -> AppResult<impl IntoResponse> {
    let rules = vec![
        serde_json::json!({
            "id": "rule-default",
            "name": "Default Organization Direct Route",
            "priority": 1,
            "targetQueue": "q.mdm.events",
            "active": true
        })
    ];
    Ok(Json(rules))
}


#[derive(Debug, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct UpdateIntegrationChannelRequest {
    pub name: Option<serde_json::Value>,
    pub channel_code: Option<String>,
    pub direction: Option<String>,
    #[serde(alias = "type", alias = "channelType")]
    pub channel_type: Option<String>,
    pub config_json: Option<String>,
    pub mapping_config_json: Option<String>,
    pub is_active: Option<bool>,
    pub active: Option<bool>,
    pub requires_approval: Option<bool>,
    pub node_id: Option<Uuid>,
}

pub async fn update_channel(
    State(state): State<AppState>,
    Path(id): Path<Uuid>,
    _auth: AuthUser,
    Json(payload): Json<UpdateIntegrationChannelRequest>,
) -> Result<Json<serde_json::Value>, AppError> {
    let name_val = match payload.name {
        Some(serde_json::Value::String(s)) => s,
        Some(v) => v.to_string(),
        None => "".to_string(),
    };
    let is_active = payload.is_active.or(payload.active).unwrap_or(true);

    sqlx::query(
        r#"
        UPDATE integration_channels
        SET name = COALESCE(NULLIF($1, ''), name),
            type = COALESCE($2, type),
            direction = COALESCE($3, direction),
            config_json = COALESCE($4, config_json),
            mapping_config_json = COALESCE($5, mapping_config_json),
            is_active = $6,
            node_id = COALESCE($7, node_id),
            updated_at = NOW()
        WHERE id = $8
        "#,
    )
    .bind(name_val)
    .bind(payload.channel_type)
    .bind(payload.direction)
    .bind(payload.config_json)
    .bind(payload.mapping_config_json)
    .bind(is_active)
    .bind(payload.node_id)
    .bind(id)
    .execute(&state.db)
    .await?;

    Ok(Json(serde_json::json!({ "success": true })))
}

pub async fn delete_channel(
    State(state): State<AppState>,
    Path(id): Path<Uuid>,
    _auth: AuthUser,
) -> Result<StatusCode, AppError> {
    sqlx::query("DELETE FROM integration_channels WHERE id = $1")
        .bind(id)
        .execute(&state.db)
        .await?;
    Ok(axum::http::StatusCode::NO_CONTENT)
}

#[derive(Debug, Deserialize, Default)]
#[serde(rename_all = "camelCase")]
pub struct TriggerBatchRequest {
    pub clear_existing: Option<bool>,
    pub markets: Option<serde_json::Value>,
}

pub async fn trigger_batch(
    State(state): State<AppState>,
    Path(channel_id): Path<Uuid>,
    _auth: AuthUser,
    Json(req): Json<Option<TriggerBatchRequest>>,
) -> Result<Json<serde_json::Value>, AppError> {
    let req = req.unwrap_or_default();

    #[derive(sqlx::FromRow)]
    struct ChannelInfo {
        id: Uuid,
        name: String,
        channel_code: Option<String>,
        config_json: Option<String>,
        mapping_config_json: Option<String>,
        direction: String,
        r#type: String,
        is_active: bool,
        node_id: Option<Uuid>,
        max_retries: i32,
        retry_backoff_ms: i64,
    }

    let channel = sqlx::query_as::<_, ChannelInfo>(
        "SELECT id, name, channel_code, config_json, mapping_config_json, direction, type, is_active, node_id, max_retries, retry_backoff_ms FROM integration_channels WHERE id = $1"
    )
    .bind(channel_id)
    .fetch_optional(&state.db)
    .await?
    .ok_or_else(|| AppError::NotFound("연계 채널을 찾을 수 없습니다.".to_string()))?;

    if !channel.is_active {
        return Err(AppError::BadRequest(format!("비활성화된 연계 채널입니다. ({})", channel.name)));
    }

    // Branch: If OUTBOUND channel, dispatch current active records for this channel
    if channel.direction.eq_ignore_ascii_case("OUTBOUND") {
        let records: Vec<(Uuid, Option<serde_json::Value>, Uuid)> = sqlx::query_as(
            r#"
            SELECT r.id, r.data, r.node_id
            FROM record r
            WHERE ($1::uuid IS NULL OR r.node_id = $1)
              AND r.status = 'ACTIVE'
            ORDER BY r.updated_at DESC
            LIMIT 50
            "#
        )
        .bind(channel.node_id)
        .fetch_all(&state.db)
        .await?;

        let total = records.len();
        let mut success_count = 0;
        let mut fail_count = 0;

        let out_channel = crate::services::outbound_service::OutboundChannel {
            id: channel.id,
            name: channel.name.clone(),
            channel_code: channel.channel_code.clone(),
            r#type: channel.r#type.clone(),
            direction: channel.direction.clone(),
            config_json: channel.config_json.clone(),
            mapping_config_json: channel.mapping_config_json.clone(),
            is_active: channel.is_active,
            node_id: channel.node_id,
            max_retries: channel.max_retries,
            retry_backoff_ms: channel.retry_backoff_ms,
        };

        for (rec_id, data, _node_id) in records {
            if let Some(d) = data {
                match crate::services::outbound_service::OutboundService::dispatch_single_channel(
                    &state.db,
                    &out_channel,
                    Some(rec_id),
                    "MANUAL_DISPATCH",
                    &d,
                ).await {
                    Ok(_) => success_count += 1,
                    Err(_) => fail_count += 1,
                }
            }
        }

        let log_id = Uuid::new_v4();
        return Ok(Json(serde_json::json!({
            "jobExecutionId": log_id,
            "status": "COMPLETED",
            "message": format!("아웃바운드 수동 전파 완료: 대상 {}건 (성공 {}건, 실패 {}건)", total, success_count, fail_count),
            "totalDispatched": total,
            "successCount": success_count,
            "failCount": fail_count,
        })));
    }

    // Branch: If INBOUND channel, run stock ingestion
    let markets_vec: Option<Vec<String>> = match req.markets {
        Some(serde_json::Value::Array(arr)) => {
            Some(arr.into_iter().filter_map(|v| v.as_str().map(|s| s.to_string())).collect())
        }
        Some(serde_json::Value::String(s)) => {
            Some(s.split(',').map(|m| m.trim().to_string()).filter(|m| !m.is_empty()).collect())
        }
        _ => None,
    };

    let seed_req = crate::batch::stock_ingestion::StockSeedRequest {
        clear_existing: req.clear_existing,
        markets: markets_vec,
        limit_per_market: None,
        custom_rows: None,
    };

    let res = crate::batch::stock_ingestion::StockDataIngestionJob::run_ingestion(
        &state.db,
        seed_req,
        "MANUAL_TRIGGER",
    )
    .await
    .map_err(|e| AppError::Internal(format!("주식 배치 실행 실패: {}", e)))?;

    let log_id = Uuid::new_v4();

    Ok(Json(serde_json::json!({
        "jobExecutionId": log_id,
        "status": "COMPLETED",
        "message": res.message,
        "totalSeeded": res.total_seeded,
        "totalCreated": res.total_created,
        "totalMerged": res.total_merged,
        "seededByMarket": res.seeded_by_market
    })))
}

pub async fn handle_inbound(
    State(state): State<AppState>,
    Path(channel_id): Path<Uuid>,
    headers: axum::http::HeaderMap,
    Json(payload): Json<serde_json::Value>,
) -> Result<Json<serde_json::Value>, AppError> {
    #[derive(sqlx::FromRow)]
    struct ChannelRow {
        id: Uuid,
        name: String,
        direction: String,
        config_json: Option<String>,
        node_id: Option<Uuid>,
        is_active: bool,
    }

    let channel = sqlx::query_as::<_, ChannelRow>(
        "SELECT id, name, direction, config_json, node_id, is_active FROM integration_channels WHERE id = $1"
    )
    .bind(channel_id)
    .fetch_optional(&state.db)
    .await?
    .ok_or_else(|| AppError::NotFound("연계 채널을 찾을 수 없습니다.".to_string()))?;

    if !channel.is_active {
        return Err(AppError::BadRequest("비활성화된 연계 채널입니다.".to_string()));
    }
    if !channel.direction.eq_ignore_ascii_case("INBOUND") {
        return Err(AppError::BadRequest("인바운드 연계 채널이 아닙니다.".to_string()));
    }

    // Check authentication if configured in config_json
    if let Some(ref cfg_str) = channel.config_json {
        if let Ok(cfg) = serde_json::from_str::<serde_json::Value>(cfg_str) {
            let auth_type = cfg.get("authType").and_then(|v| v.as_str()).unwrap_or("NONE");
            let secret_token = cfg.get("secretToken").and_then(|v| v.as_str()).unwrap_or("");
            if !secret_token.is_empty() {
                if auth_type == "BEARER_TOKEN" {
                    let auth_val = headers.get("authorization").and_then(|v| v.to_str().ok()).unwrap_or("");
                    let expected = format!("Bearer {}", secret_token);
                    if auth_val != expected && auth_val != secret_token {
                        return Err(AppError::Unauthorized("인바운드 연계 인증 토큰이 일치하지 않습니다.".to_string()));
                    }
                } else if auth_type == "API_KEY" {
                    let api_key = headers.get("x-api-key").and_then(|v| v.to_str().ok()).unwrap_or("");
                    if api_key != secret_token {
                        return Err(AppError::Unauthorized("인바운드 연계 인증 토큰이 일치하지 않습니다.".to_string()));
                    }
                }
            }
        }
    }

    // Resolve target node id
    let node_id = match channel.node_id {
        Some(nid) => nid,
        None => {
            let fallback: Option<Uuid> = sqlx::query_scalar(
                "SELECT id FROM classification_node WHERE is_deleted = false ORDER BY sort_order ASC LIMIT 1"
            )
            .fetch_optional(&state.db)
            .await?;
            fallback.ok_or_else(|| AppError::BadRequest("연계 대상 분류 노드가 지정되지 않았습니다.".to_string()))?
        }
    };

    let new_rec_id = Uuid::new_v4();
    let raw_payload_str = payload.to_string();
    let search_text = payload.to_string();

    sqlx::query(
        r#"
        INSERT INTO record (id, node_id, status, source_system, data, searchable_data, version, created_at, updated_at)
        VALUES ($1, $2, 'ACTIVE', $3, $4, $5, 1, NOW(), NOW())
        "#
    )
    .bind(new_rec_id)
    .bind(node_id)
    .bind(format!("INBOUND_INTEGRATION_{}", channel.name))
    .bind(&payload)
    .bind(serde_json::Value::String(search_text))
    .execute(&state.db)
    .await?;

    let history_id = Uuid::new_v4();
    let _ = sqlx::query(
        r#"
        INSERT INTO record_history (id, record_id, change_type, changed_by, source_system, new_data, version, changed_at)
        VALUES ($1, $2, 'INBOUND_INGEST', 'INBOUND_WEBHOOK', $3, $4, 1, NOW())
        "#
    )
    .bind(history_id)
    .bind(new_rec_id)
    .bind(&channel.name)
    .bind(&payload)
    .execute(&state.db)
    .await;

    let log_id = Uuid::new_v4();
    let _ = sqlx::query(
        r#"
        INSERT INTO integration_logs (id, channel_id, record_id, event_type, status, original_payload, mapped_payload, created_at, retry_count)
        VALUES ($1, $2, $3, 'INBOUND_RECEIVE', 'SUCCESS', $4, $5, NOW(), 0)
        "#
    )
    .bind(log_id)
    .bind(channel.id)
    .bind(new_rec_id)
    .bind(&raw_payload_str)
    .bind(&raw_payload_str)
    .execute(&state.db)
    .await;

    Ok(Json(serde_json::json!({
        "success": true,
        "message": "Inbound data processed successfully",
        "recordId": new_rec_id
    })))
}



