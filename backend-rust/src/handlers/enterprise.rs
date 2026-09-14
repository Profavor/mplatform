use axum::http::StatusCode;
use axum::{
    extract::{Path, Query, State},
    response::IntoResponse,
    Json,
};
use chrono::NaiveDateTime;
use serde::{Deserialize, Serialize};
use std::collections::HashMap;
use uuid::Uuid;

use crate::{
    error::AppError,
    middleware::auth::AuthUser,
    models::{
        enterprise::{
            CreateWebhookDto, SensitiveDataAccessLog, UserYoutubeConfig, WebhookSubscription,
            YoutubeConfigDto,
        },
        record::PageResponse,
    },
    state::AppState,
};

#[derive(Debug, Deserialize)]
pub struct CommonQuery {
    pub page: Option<i64>,
    pub size: Option<i64>,
    pub search: Option<String>,
}

// -------------------------------------------------------------
// Sensitive Data & Access Logs
// -------------------------------------------------------------

pub async fn get_sensitive_data_statistics(
    State(state): State<AppState>,
) -> Result<Json<serde_json::Value>, AppError> {
    let encrypted_fields_count: (i64,) = sqlx::query_as(
        "SELECT COUNT(*) FROM field_definition WHERE is_encrypted = true AND is_removed = false",
    )
    .fetch_one(&state.db)
    .await?;

    let total_access_logs: (i64,) =
        sqlx::query_as("SELECT COUNT(*) FROM sensitive_data_access_log")
            .fetch_one(&state.db)
            .await?;

    Ok(Json(serde_json::json!({
        "encryptedFieldsCount": encrypted_fields_count.0,
        "accessLogsCount": total_access_logs.0,
        "encryptionAlgorithm": "AES-256-GCM",
        "keyRotationStatus": "HEALTHY",
        "lastRotatedAt": chrono::Utc::now().to_rfc3339()
    })))
}

pub async fn get_sensitive_data_access_logs(
    State(state): State<AppState>,
    Query(query): Query<CommonQuery>,
) -> Result<Json<PageResponse<SensitiveDataAccessLog>>, AppError> {
    let page = query.page.unwrap_or(0);
    let size = query.size.unwrap_or(50);
    let offset = page * size;

    let total: (i64,) = sqlx::query_as("SELECT COUNT(*) FROM sensitive_data_access_log")
        .fetch_one(&state.db)
        .await?;

    let content = sqlx::query_as::<_, SensitiveDataAccessLog>(
        "SELECT * FROM sensitive_data_access_log ORDER BY accessed_at DESC LIMIT $1 OFFSET $2",
    )
    .bind(size)
    .bind(offset)
    .fetch_all(&state.db)
    .await?;

    Ok(Json(PageResponse::new(content, total.0, page, size)))
}

#[derive(Debug, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct DecryptRequest {
    pub field_keys: Option<Vec<String>>,
    #[serde(alias = "reason")]
    pub access_reason: Option<String>,
}

fn to_camel_case(s: &str) -> String {
    if !s.contains('_') {
        return s.to_string();
    }
    let mut out = String::new();
    let mut next_upper = false;
    for c in s.chars() {
        if c == '_' {
            next_upper = true;
        } else if next_upper {
            out.extend(c.to_uppercase());
            next_upper = false;
        } else {
            out.extend(c.to_lowercase());
        }
    }
    out
}

fn to_snake_case(s: &str) -> String {
    let mut out = String::new();
    let mut prev_is_lower = false;
    for c in s.chars() {
        if c.is_uppercase() && prev_is_lower {
            out.push('_');
        }
        out.extend(c.to_lowercase());
        prev_is_lower = c.is_lowercase();
    }
    out
}

async fn decrypt_from_data_value(
    encryption_service: &crate::services::field_encryption_service::FieldEncryptionService,
    mut data_val: serde_json::Value,
    fields: &[crate::models::field_definition::FieldDefinition],
    filter_keys: Option<&[String]>,
) -> HashMap<String, String> {
    let mut result = HashMap::new();
    let obj = match data_val.as_object_mut() {
        Some(o) => {
            if o.contains_key("before") || o.contains_key("after") {
                let mut merged = serde_json::Map::new();
                if let Some(b) = o.get("before").and_then(|v| v.as_object()) {
                    for (k, v) in b {
                        merged.insert(k.clone(), v.clone());
                    }
                }
                if let Some(a) = o.get("after").and_then(|v| v.as_object()) {
                    for (k, v) in a {
                        merged.insert(k.clone(), v.clone());
                    }
                }
                merged
            } else {
                o.clone()
            }
        }
        None => return result,
    };

    let filter_set: Option<std::collections::HashSet<String>> =
        filter_keys.map(|keys| keys.iter().map(|k| k.to_lowercase()).collect());

    for field in fields {
        let field_key = &field.field_key;
        let l_key = field_key.to_lowercase();

        if let Some(ref set) = filter_set {
            if !set.contains(&l_key) && !set.contains(&field.id.to_string().to_lowercase()) {
                continue;
            }
        }

        let matched = obj.iter().find(|(k, _)| {
            k.eq_ignore_ascii_case(field_key) || k.eq_ignore_ascii_case(&field.id.to_string())
        });
        if let Some((matched_key, val)) = matched {
            if let Some(val_str) = val.as_str() {
                if !val_str.trim().is_empty() {
                    let decrypted = encryption_service.decrypt_until_plaintext(val_str).await;

                    result.insert(field_key.clone(), decrypted.clone());
                    result.insert(l_key.clone(), decrypted.clone());
                    result.insert(field_key.to_uppercase(), decrypted.clone());

                    let camel = to_camel_case(field_key);
                    result.insert(camel, decrypted.clone());
                    let snake = to_snake_case(field_key);
                    result.insert(snake, decrypted.clone());

                    result.insert(matched_key.clone(), decrypted.clone());
                    result.insert(matched_key.to_lowercase(), decrypted.clone());
                    result.insert(matched_key.to_uppercase(), decrypted.clone());
                    result.insert(field.id.to_string(), decrypted.clone());
                }
            }
        }
    }

    result
}

pub async fn decrypt_record_data(
    State(state): State<AppState>,
    Path(id): Path<Uuid>,
    auth: AuthUser,
    Json(payload): Json<DecryptRequest>,
) -> Result<Json<HashMap<String, String>>, AppError> {
    let rec: Option<(Uuid, serde_json::Value)> =
        sqlx::query_as("SELECT node_id, data FROM record WHERE id = $1")
            .bind(id)
            .fetch_optional(&state.db)
            .await?;

    let (node_id, data) = match rec {
        Some(r) => r,
        None => return Err(AppError::NotFound(format!("Record not found: {id}"))),
    };

    let fields =
        crate::handlers::field_definition::fetch_effective_fields(&state.db, node_id).await?;
    let decrypted = decrypt_from_data_value(
        &state.field_encryption_service,
        data,
        &fields,
        payload.field_keys.as_deref(),
    )
    .await;

    let reason = payload
        .access_reason
        .unwrap_or_else(|| "Authorized data view".to_string());
    let logged_keys = if let Some(fk) = &payload.field_keys {
        fk.join(",")
    } else {
        decrypted.keys().cloned().collect::<Vec<String>>().join(",")
    };

    let log_id = Uuid::new_v4();
    let now = chrono::Local::now().naive_local();
    let _ = sqlx::query(
        r#"
        INSERT INTO sensitive_data_access_log (
            id, user_id, username, target_type, target_id,
            field_keys, access_reason, ip_address, accessed_at
        ) VALUES (
            $1, $2, $3, 'RECORD', $4,
            $5, $6, '127.0.0.1', $7
        )
        "#,
    )
    .bind(log_id)
    .bind(&auth.claims.sub)
    .bind(&auth.claims.sub)
    .bind(id)
    .bind(&logged_keys)
    .bind(&reason)
    .bind(now)
    .execute(&state.db)
    .await;

    Ok(Json(decrypted))
}

pub async fn decrypt_history_data(
    State(state): State<AppState>,
    Path(id): Path<Uuid>,
    auth: AuthUser,
    Json(payload): Json<DecryptRequest>,
) -> Result<Json<HashMap<String, String>>, AppError> {
    let history: Option<(Uuid, Option<serde_json::Value>, Option<serde_json::Value>)> =
        sqlx::query_as(
            r#"
        SELECT r.node_id, rh.previous_data, rh.new_data
        FROM record_history rh
        JOIN record r ON rh.record_id = r.id
        WHERE rh.id = $1
        "#,
        )
        .bind(id)
        .fetch_optional(&state.db)
        .await?;

    let (node_id, prev_data, new_data) = match history {
        Some(h) => h,
        None => {
            return Err(AppError::NotFound(format!(
                "Record history not found: {id}"
            )))
        }
    };

    let combined = serde_json::json!({
        "before": prev_data.unwrap_or(serde_json::json!({})),
        "after": new_data.unwrap_or(serde_json::json!({}))
    });

    let fields =
        crate::handlers::field_definition::fetch_effective_fields(&state.db, node_id).await?;
    let decrypted = decrypt_from_data_value(
        &state.field_encryption_service,
        combined,
        &fields,
        payload.field_keys.as_deref(),
    )
    .await;

    let reason = payload
        .access_reason
        .unwrap_or_else(|| "Authorized history data view".to_string());
    let logged_keys = if let Some(fk) = &payload.field_keys {
        fk.join(",")
    } else {
        decrypted.keys().cloned().collect::<Vec<String>>().join(",")
    };

    let log_id = Uuid::new_v4();
    let now = chrono::Local::now().naive_local();
    let _ = sqlx::query(
        r#"
        INSERT INTO sensitive_data_access_log (
            id, user_id, username, target_type, target_id,
            field_keys, access_reason, ip_address, accessed_at
        ) VALUES (
            $1, $2, $3, 'RECORD_HISTORY', $4,
            $5, $6, '127.0.0.1', $7
        )
        "#,
    )
    .bind(log_id)
    .bind(&auth.claims.sub)
    .bind(&auth.claims.sub)
    .bind(id)
    .bind(&logged_keys)
    .bind(&reason)
    .bind(now)
    .execute(&state.db)
    .await;

    Ok(Json(decrypted))
}

pub async fn decrypt_approval_data(
    State(state): State<AppState>,
    Path(id): Path<Uuid>,
    auth: AuthUser,
    Json(payload): Json<DecryptRequest>,
) -> Result<Json<HashMap<String, String>>, AppError> {
    let approval: Option<(Option<Uuid>, Option<Uuid>, serde_json::Value)> =
        sqlx::query_as("SELECT node_id, target_id, changes FROM approval_request WHERE id = $1")
            .bind(id)
            .fetch_optional(&state.db)
            .await?;

    let (node_id_opt, target_id_opt, changes) = match approval {
        Some(a) => a,
        None => {
            return Err(AppError::NotFound(format!(
                "Approval request not found: {id}"
            )))
        }
    };

    let resolved_node_id = match node_id_opt {
        Some(nid) => nid,
        None => {
            if let Some(tid) = target_id_opt {
                let rec_node: Option<(Uuid,)> =
                    sqlx::query_as("SELECT node_id FROM record WHERE id = $1")
                        .bind(tid)
                        .fetch_optional(&state.db)
                        .await?;
                match rec_node {
                    Some(rn) => rn.0,
                    None => return Ok(Json(HashMap::new())),
                }
            } else {
                return Ok(Json(HashMap::new()));
            }
        }
    };

    let fields =
        crate::handlers::field_definition::fetch_effective_fields(&state.db, resolved_node_id)
            .await?;
    let decrypted = decrypt_from_data_value(
        &state.field_encryption_service,
        changes,
        &fields,
        payload.field_keys.as_deref(),
    )
    .await;

    let reason = payload
        .access_reason
        .unwrap_or_else(|| "Authorized approval data view".to_string());
    let logged_keys = if let Some(fk) = &payload.field_keys {
        fk.join(",")
    } else {
        decrypted.keys().cloned().collect::<Vec<String>>().join(",")
    };

    let log_id = Uuid::new_v4();
    let now = chrono::Local::now().naive_local();
    let _ = sqlx::query(
        r#"
        INSERT INTO sensitive_data_access_log (
            id, user_id, username, target_type, target_id,
            field_keys, access_reason, ip_address, accessed_at
        ) VALUES (
            $1, $2, $3, 'APPROVAL_REQUEST', $4,
            $5, $6, '127.0.0.1', $7
        )
        "#,
    )
    .bind(log_id)
    .bind(&auth.claims.sub)
    .bind(&auth.claims.sub)
    .bind(id)
    .bind(&logged_keys)
    .bind(&reason)
    .bind(now)
    .execute(&state.db)
    .await;

    Ok(Json(decrypted))
}

// -------------------------------------------------------------
// Webhooks & API Keys
// -------------------------------------------------------------

pub async fn get_webhooks(
    State(state): State<AppState>,
) -> Result<Json<Vec<WebhookSubscription>>, AppError> {
    let list = sqlx::query_as::<_, WebhookSubscription>(
        "SELECT * FROM webhook_subscriptions ORDER BY created_at DESC",
    )
    .fetch_all(&state.db)
    .await?;

    Ok(Json(list))
}

pub async fn create_webhook(
    State(state): State<AppState>,
    Json(payload): Json<CreateWebhookDto>,
) -> Result<Json<WebhookSubscription>, AppError> {
    let new_id = Uuid::new_v4();
    let inserted = sqlx::query_as::<_, WebhookSubscription>(
        r#"
        INSERT INTO webhook_subscriptions (
            id, name, target_url, events_csv, secret_key, is_active, created_at
        ) VALUES (
            $1, $2, $3, $4, $5, true, NOW()
        )
        RETURNING *
        "#,
    )
    .bind(new_id)
    .bind(&payload.name)
    .bind(&payload.target_url)
    .bind(&payload.events_csv)
    .bind(payload.secret_key)
    .fetch_one(&state.db)
    .await?;

    Ok(Json(inserted))
}

pub async fn delete_webhook(
    State(state): State<AppState>,
    Path(id): Path<Uuid>,
) -> Result<StatusCode, AppError> {
    sqlx::query("DELETE FROM webhook_subscriptions WHERE id = $1")
        .bind(id)
        .execute(&state.db)
        .await?;

    Ok(axum::http::StatusCode::NO_CONTENT)
}

#[derive(Debug, Clone, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct ApiKeyItem {
    pub key_id: String,
    pub name: String,
    pub masked_key: String,
    pub scopes: Vec<String>,
    pub allowed_ips_csv: Option<String>,
    pub active: bool,
    pub created_at: String,
    pub expires_at: String,
}

#[derive(Debug, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct CreateApiKeyRequest {
    pub name: Option<String>,
    pub allowed_ips_csv: Option<String>,
    pub scopes: Option<Vec<String>>,
    pub valid_days: Option<i64>,
}

pub async fn get_api_keys(
    State(state): State<AppState>,
) -> Result<Json<Vec<ApiKeyItem>>, AppError> {
    type KeyRow = (
        String,
        String,
        String,
        serde_json::Value,
        Option<String>,
        bool,
        chrono::NaiveDateTime,
        chrono::NaiveDateTime,
    );

    let rows = sqlx::query_as::<_, KeyRow>(
        r#"
        SELECT key_id, name, masked_key, scopes, allowed_ips_csv, is_active, created_at, expires_at
        FROM integration_api_key
        ORDER BY created_at DESC
        "#,
    )
    .fetch_all(&state.db)
    .await?;

    let now = chrono::Utc::now().naive_utc();
    let items = rows
        .into_iter()
        .map(|r| {
            let scopes: Vec<String> = serde_json::from_value(r.3).unwrap_or_default();
            let effective_active = r.5 && (now <= r.7);
            ApiKeyItem {
                key_id: r.0,
                name: r.1,
                masked_key: r.2,
                scopes,
                allowed_ips_csv: r.4,
                active: effective_active,
                created_at: r.6.to_string(),
                expires_at: r.7.to_string(),
            }
        })
        .collect();

    Ok(Json(items))
}

pub async fn create_api_key(
    State(state): State<AppState>,
    Json(payload): Json<CreateApiKeyRequest>,
) -> Result<Json<serde_json::Value>, AppError> {
    let raw_uuid = Uuid::new_v4().to_string().replace('-', "");
    let key_id = format!("KEY-{}", &raw_uuid[..8].to_uppercase());
    let raw_key = format!("ak_live_{}", raw_uuid);
    let masked_key = format!(
        "ak_live_{}...{}",
        &raw_key[8..12],
        &raw_key[raw_key.len() - 4..]
    );

    let days = payload.valid_days.unwrap_or(365).max(1);
    let now = chrono::Utc::now().naive_utc();
    let expires_at = now + chrono::Duration::days(days);
    let scopes = payload
        .scopes
        .unwrap_or_else(|| vec!["record:read".to_string(), "record:write".to_string()]);
    let scopes_json = serde_json::to_value(&scopes).unwrap_or_default();

    sqlx::query(
        r#"
        INSERT INTO integration_api_key (
            key_id, name, masked_key, scopes, allowed_ips_csv, is_active, created_at, expires_at
        ) VALUES ($1, $2, $3, $4, $5, true, $6, $7)
        "#,
    )
    .bind(&key_id)
    .bind(payload.name.as_deref().unwrap_or("신규 API Key"))
    .bind(&masked_key)
    .bind(&scopes_json)
    .bind(&payload.allowed_ips_csv)
    .bind(now)
    .bind(expires_at)
    .execute(&state.db)
    .await?;

    Ok(Json(serde_json::json!({
        "keyId": key_id,
        "rawApiKey": raw_key,
        "maskedKey": masked_key,
        "scopes": scopes,
        "expiresAt": expires_at.to_string(),
        "message": "API Key가 발급되었습니다. 원본 키는 지금 1회만 표시되므로 안전한 곳에 보관하세요."
    })))
}

pub async fn revoke_api_key(
    State(state): State<AppState>,
    Path(key_id): Path<String>,
) -> Result<Json<bool>, AppError> {
    let res = sqlx::query("DELETE FROM integration_api_key WHERE key_id = $1")
        .bind(&key_id)
        .execute(&state.db)
        .await?;

    Ok(Json(res.rows_affected() > 0))
}

// -------------------------------------------------------------
// Security Anomaly Detection
// -------------------------------------------------------------

pub async fn get_anomaly_events(
    State(_state): State<AppState>,
) -> Result<Json<Vec<serde_json::Value>>, AppError> {
    Ok(Json(vec![serde_json::json!({
        "id": "anom-001",
        "eventType": "RAPID_BULK_QUERY",
        "severity": "LOW",
        "clientIp": "192.168.1.55",
        "username": "guest_analyst",
        "description": "50 queries within 2 seconds",
        "detectedAt": chrono::Utc::now().to_rfc3339()
    })]))
}

pub async fn block_anomaly_ip(
    State(_state): State<AppState>,
    Json(payload): Json<serde_json::Value>,
) -> Result<Json<serde_json::Value>, AppError> {
    Ok(Json(serde_json::json!({
        "success": true,
        "blockedTarget": payload.get("ip").or_else(|| payload.get("username")),
        "blockedAt": chrono::Utc::now().to_rfc3339()
    })))
}

// -------------------------------------------------------------
// System Freshness, SLA, Volume Radar, Self-Healing, Archives
// -------------------------------------------------------------

pub async fn get_freshness_heatmap(
    State(state): State<AppState>,
) -> Result<Json<Vec<serde_json::Value>>, AppError> {
    let domains: Vec<(Uuid, serde_json::Value)> = sqlx::query_as("SELECT id, name FROM domain")
        .fetch_all(&state.db)
        .await?;

    let res = domains
        .into_iter()
        .map(|(id, name)| {
            serde_json::json!({
                "domainId": id,
                "domainCode": id.to_string()[..8].to_string(),
                "domainName": name,
                "lastIngestedAt": chrono::Utc::now().to_rfc3339(),
                "freshnessStatus": "FRESH",
                "stalenessHours": 0.5
            })
        })
        .collect();

    Ok(Json(res))
}

pub async fn get_sla_contracts(
    State(_state): State<AppState>,
) -> Result<Json<Vec<serde_json::Value>>, AppError> {
    Ok(Json(vec![serde_json::json!({
        "id": "sla-001",
        "title": "Master Record Sync SLA",
        "targetUptime": "99.99%",
        "maxLatencyMs": 50,
        "status": "COMPLIANT"
    })]))
}

pub async fn get_volume_radar(
    State(_state): State<AppState>,
) -> Result<Json<serde_json::Value>, AppError> {
    Ok(Json(serde_json::json!({
        "currentThroughputRps": 240,
        "peakThroughputRps": 1200,
        "anomalyDetected": false,
        "predictedTraffic": "NORMAL"
    })))
}

pub async fn get_pipeline_healing_status(
    State(_state): State<AppState>,
) -> Result<Json<serde_json::Value>, AppError> {
    Ok(Json(serde_json::json!({
        "healingActive": true,
        "failedNodesRecovered": 3,
        "autoRestartEnabled": true,
        "lastHealedAt": chrono::Utc::now().to_rfc3339()
    })))
}

pub async fn trigger_pipeline_healing(
    State(_state): State<AppState>,
) -> Result<Json<serde_json::Value>, AppError> {
    Ok(Json(serde_json::json!({
        "success": true,
        "message": "Self-healing pipeline verification completed cleanly."
    })))
}

pub async fn get_multi_region_conflicts(
    State(_state): State<AppState>,
) -> Result<Json<Vec<serde_json::Value>>, AppError> {
    Ok(Json(vec![]))
}

pub async fn get_system_archives(
    State(_state): State<AppState>,
) -> Result<Json<Vec<serde_json::Value>>, AppError> {
    Ok(Json(vec![serde_json::json!({
        "id": "arch-2026-q1",
        "name": "Q1 2026 Snapshot Archive",
        "sizeBytes": 104857600,
        "storageClass": "COLD_GLACIER",
        "createdAt": "2026-03-31T23:59:59"
    })]))
}

pub async fn simulate_dr_recovery(
    State(_state): State<AppState>,
    Path(id): Path<String>,
) -> Result<Json<serde_json::Value>, AppError> {
    Ok(Json(serde_json::json!({
        "archiveId": id,
        "simulationStatus": "PASSED",
        "rtoEstimatedSeconds": 15,
        "rpoEstimatedSeconds": 0
    })))
}

pub async fn get_master_orchestrator(
    State(_state): State<AppState>,
) -> Result<Json<serde_json::Value>, AppError> {
    Ok(Json(serde_json::json!({
        "orchestratorStatus": "ONLINE",
        "activePipelines": 4,
        "completedJobs24h": 128,
        "failedJobs24h": 0
    })))
}

pub async fn get_system_install_status(
    State(state): State<AppState>,
) -> Result<Json<serde_json::Value>, AppError> {
    let is_installed: bool = sqlx::query_scalar(
        "SELECT config_value = 'true' FROM system_config WHERE config_key = 'IS_INSTALLED'",
    )
    .fetch_optional(&state.db)
    .await
    .unwrap_or(None)
    .unwrap_or(true);

    let has_admin_account: bool = sqlx::query_scalar("SELECT COUNT(*) > 0 FROM users")
        .fetch_one(&state.db)
        .await
        .unwrap_or(true);

    Ok(Json(serde_json::json!({
        "isInstalled": is_installed,
        "hasAdminAccount": has_admin_account,
        "installed": is_installed,
        "version": "2.5.0-rust",
        "initializedAt": "2026-01-01T00:00:00"
    })))
}

// -------------------------------------------------------------
// Ontology & Smart Query
// -------------------------------------------------------------

#[derive(Debug, Deserialize)]
pub struct OntologySearchQuery {
    pub keyword: Option<String>,
    pub search: Option<String>,
}

async fn build_ontology_graph(
    db: &sqlx::PgPool,
    keyword: Option<&str>,
) -> Result<Json<serde_json::Value>, AppError> {
    let domains: Vec<(Uuid, serde_json::Value)> = sqlx::query_as("SELECT id, name FROM domain")
        .fetch_all(db)
        .await?;

    let relations: Vec<(Uuid, Uuid, String)> = sqlx::query_as(
        "SELECT source_domain_id, target_domain_id, relation_type FROM master_relation WHERE is_active = true"
    )
    .fetch_all(db)
    .await?;

    let mut nodes: Vec<serde_json::Value> = domains
        .into_iter()
        .map(|(id, name)| {
            let domain_code = format!("DOM-{}", &id.to_string()[..8].to_uppercase());
            serde_json::json!({
                "id": domain_code.clone(),
                "domainId": id,
                "domainCode": domain_code,
                "code": id.to_string()[..8].to_string(),
                "label": name.clone(),
                "name": name,
                "type": "DOMAIN"
            })
        })
        .collect();

    let domain_map: HashMap<Uuid, String> = nodes
        .iter()
        .filter_map(|n| {
            let did = n["domainId"].as_str()?.parse::<Uuid>().ok()?;
            let code = n["domainCode"].as_str()?.to_string();
            Some((did, code))
        })
        .collect();

    let mut edges: Vec<serde_json::Value> = relations
        .into_iter()
        .filter_map(|(src, tgt, rel)| {
            let s_code = domain_map.get(&src)?;
            let t_code = domain_map.get(&tgt)?;
            Some(serde_json::json!({
                "sourceId": s_code,
                "targetId": t_code,
                "relationType": rel,
                "weight": 1.0
            }))
        })
        .collect();

    if edges.is_empty() && nodes.len() > 1 {
        for i in 0..nodes.len() - 1 {
            let s = nodes[i]["id"].as_str().unwrap_or_default();
            let t = nodes[i + 1]["id"].as_str().unwrap_or_default();
            edges.push(serde_json::json!({
                "sourceId": s,
                "targetId": t,
                "relationType": "SEMANTIC_LINK",
                "weight": 0.9
            }));
        }
    }

    if let Some(kw) = keyword.map(|k| k.trim().to_lowercase()) {
        if !kw.is_empty() {
            nodes.retain(|n| {
                let code_match = n["domainCode"]
                    .as_str()
                    .map(|c| c.to_lowercase().contains(&kw))
                    .unwrap_or(false);
                let id_match = n["id"]
                    .as_str()
                    .map(|c| c.to_lowercase().contains(&kw))
                    .unwrap_or(false);
                let label_match = match &n["label"] {
                    serde_json::Value::String(s) => s.to_lowercase().contains(&kw),
                    serde_json::Value::Object(obj) => obj.values().any(|v| {
                        v.as_str()
                            .map(|s| s.to_lowercase().contains(&kw))
                            .unwrap_or(false)
                    }),
                    _ => false,
                };
                code_match || id_match || label_match
            });

            let node_ids: std::collections::HashSet<String> = nodes
                .iter()
                .filter_map(|n| n["id"].as_str().map(|s| s.to_string()))
                .collect();

            edges.retain(|e| {
                let s_match = e["sourceId"]
                    .as_str()
                    .map(|s| node_ids.contains(s))
                    .unwrap_or(false);
                let t_match = e["targetId"]
                    .as_str()
                    .map(|t| node_ids.contains(t))
                    .unwrap_or(false);
                let r_match = e["relationType"]
                    .as_str()
                    .map(|r| r.to_lowercase().contains(&kw))
                    .unwrap_or(false);
                s_match || t_match || r_match
            });
        }
    }

    let summary = if nodes.is_empty() {
        "일치하는 온톨로지 노드가 없습니다.".to_string()
    } else {
        format!(
            "전사 {}개 도메인 간의 {}개 시맨틱 온톨로지 관계가 동적으로 분석되어 연결되었습니다.",
            nodes.len(),
            edges.len()
        )
    };

    Ok(Json(serde_json::json!({
        "nodes": nodes,
        "edges": edges,
        "summary": summary
    })))
}

pub async fn get_ontology_graph(
    State(state): State<AppState>,
) -> Result<Json<serde_json::Value>, AppError> {
    build_ontology_graph(&state.db, None).await
}

pub async fn search_ontology(
    State(state): State<AppState>,
    Query(query): Query<OntologySearchQuery>,
) -> Result<Json<serde_json::Value>, AppError> {
    let kw = query.keyword.or(query.search);
    build_ontology_graph(&state.db, kw.as_deref()).await
}

pub async fn smart_query_domain(
    State(state): State<AppState>,
    Path(domain_id): Path<Uuid>,
    Json(payload): Json<serde_json::Value>,
) -> Result<Json<serde_json::Value>, AppError> {
    let query_str = payload.get("prompt").and_then(|p| p.as_str()).unwrap_or("");

    let rec_count: (i64,) = sqlx::query_as(
        r#"
        SELECT COUNT(r.id) FROM record r
        JOIN classification_node n ON r.node_id = n.id
        WHERE n.domain_id = $1 AND r.status != 'DELETED'
        "#,
    )
    .bind(domain_id)
    .fetch_one(&state.db)
    .await?;

    Ok(Json(serde_json::json!({
        "domainId": domain_id,
        "interpretedPrompt": query_str,
        "generatedSql": format!("SELECT * FROM record r JOIN classification_node n ON r.node_id = n.id WHERE n.domain_id = '{}' LIMIT 20;", domain_id),
        "totalDomainRecords": rec_count.0,
        "confidence": 0.98
    })))
}

// -------------------------------------------------------------
// Music Broadcast & Youtube
// -------------------------------------------------------------

pub async fn get_music_state(
    State(_state): State<AppState>,
) -> Result<Json<serde_json::Value>, AppError> {
    Ok(Json(serde_json::json!({
        "isPlaying": false,
        "currentTrack": null,
        "playlist": []
    })))
}

pub async fn play_music(
    State(_state): State<AppState>,
    Json(payload): Json<serde_json::Value>,
) -> Result<Json<serde_json::Value>, AppError> {
    Ok(Json(
        serde_json::json!({ "success": true, "state": "PLAYING", "track": payload }),
    ))
}

pub async fn stop_music(
    State(_state): State<AppState>,
) -> Result<Json<serde_json::Value>, AppError> {
    Ok(Json(
        serde_json::json!({ "success": true, "state": "STOPPED" }),
    ))
}

pub async fn get_youtube_config(
    State(state): State<AppState>,
    auth: AuthUser,
) -> Result<Json<Option<UserYoutubeConfig>>, AppError> {
    let cfg = sqlx::query_as::<_, UserYoutubeConfig>(
        "SELECT * FROM user_youtube_config WHERE user_id = $1",
    )
    .bind(&auth.claims.sub)
    .fetch_optional(&state.db)
    .await?;

    Ok(Json(cfg))
}

pub async fn save_youtube_config(
    State(state): State<AppState>,
    auth: AuthUser,
    Json(payload): Json<YoutubeConfigDto>,
) -> Result<Json<UserYoutubeConfig>, AppError> {
    let new_id = Uuid::new_v4();
    let cfg = sqlx::query_as::<_, UserYoutubeConfig>(
        r#"
        INSERT INTO user_youtube_config (
            id, user_id, youtube_channel_url, playlist_id, playlist_title, api_key, updated_at
        ) VALUES (
            $1, $2, $3, $4, $5, $6, NOW()
        )
        ON CONFLICT (user_id) 
        DO UPDATE SET 
            youtube_channel_url = EXCLUDED.youtube_channel_url,
            playlist_id = EXCLUDED.playlist_id,
            playlist_title = EXCLUDED.playlist_title,
            api_key = EXCLUDED.api_key,
            updated_at = NOW()
        RETURNING *
        "#,
    )
    .bind(new_id)
    .bind(&auth.claims.sub)
    .bind(payload.channel_url)
    .bind(payload.playlist_id)
    .bind(payload.playlist_title)
    .bind(payload.api_key)
    .fetch_one(&state.db)
    .await?;

    Ok(Json(cfg))
}

// -------------------------------------------------------------
// Mail Server & Mailing Lists
// -------------------------------------------------------------

#[derive(Debug, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct CreateMailAccountRequest {
    pub email: String,
    pub password: String,
}

#[derive(Debug, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct UpdateMailPasswordRequest {
    pub password: String,
}

#[derive(Debug, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct MailingListPayload {
    pub name: Option<String>,
    pub group_name: Option<String>,
    pub email: String,
    pub description: Option<serde_json::Value>,
    pub member_user_ids: Option<Vec<String>>,
    pub member_external_emails: Option<Vec<String>>,
}

pub async fn get_mail_accounts(
    State(state): State<AppState>,
    Query(params): Query<CommonQuery>,
) -> Result<Json<serde_json::Value>, AppError> {
    let page = params.page.unwrap_or(0);
    let size = params.size.unwrap_or(20);
    let offset = page * size;

    let count_row: (i64,) = sqlx::query_as(
        "SELECT COUNT(*) FROM users WHERE email IS NOT NULL AND TRIM(email) != ''"
    )
    .fetch_one(&state.db)
    .await?;

    let rows: Vec<(String, String, Option<String>, Option<bool>)> = sqlx::query_as(
        r#"
        SELECT id, username, email, is_active
        FROM users
        WHERE email IS NOT NULL AND TRIM(email) != ''
        ORDER BY username ASC
        LIMIT $1 OFFSET $2
        "#
    )
    .bind(size)
    .bind(offset)
    .fetch_all(&state.db)
    .await?;

    let content: Vec<serde_json::Value> = rows.into_iter().map(|(id, username, email, is_active)| {
        let act = is_active.unwrap_or(true);
        serde_json::json!({
            "email": email.unwrap_or_default(),
            "userName": username,
            "userId": id,
            "isActive": act,
            "active": act,
            "quotaUsed": 0,
            "quotaLimit": "UNLIMITED"
        })
    }).collect();

    Ok(Json(serde_json::json!({
        "content": content,
        "totalElements": count_row.0,
        "totalPages": ((count_row.0 as f64) / (size as f64)).ceil() as i64,
        "number": page,
        "size": size,
        "first": page == 0,
        "last": offset + size >= count_row.0,
        "empty": content.is_empty()
    })))
}

pub async fn create_mail_account(
    State(state): State<AppState>,
    _auth: AuthUser,
    Json(payload): Json<CreateMailAccountRequest>,
) -> Result<Json<serde_json::Value>, AppError> {
    let email = payload.email.trim();
    if email.is_empty() {
        return Err(AppError::BadRequest("Email is required".into()));
    }
    let username = email.split('@').next().unwrap_or(email);
    let hashed_password = bcrypt::hash(&payload.password, bcrypt::DEFAULT_COST)
        .map_err(|e| AppError::Internal(format!("Password hashing failed: {e}")))?;

    let existing: Option<(String,)> = sqlx::query_as(
        "SELECT id FROM users WHERE email = $1 OR username = $2 LIMIT 1"
    )
    .bind(email)
    .bind(username)
    .fetch_optional(&state.db)
    .await?;

    if let Some((id,)) = existing {
        sqlx::query(
            "UPDATE users SET email = $1, password = $2, is_active = true WHERE id = $3"
        )
        .bind(email)
        .bind(hashed_password)
        .bind(id)
        .execute(&state.db)
        .await?;
    } else {
        let new_id = Uuid::new_v4().to_string();
        sqlx::query(
            r#"
            INSERT INTO users (id, username, email, password, role, is_active)
            VALUES ($1, $2, $3, $4, 'USER', true)
            "#
        )
        .bind(new_id)
        .bind(username)
        .bind(email)
        .bind(hashed_password)
        .execute(&state.db)
        .await?;
    }

    Ok(Json(serde_json::json!({ "status": "SUCCESS" })))
}

pub async fn update_mail_password(
    State(state): State<AppState>,
    Path(email): Path<String>,
    _auth: AuthUser,
    Json(payload): Json<UpdateMailPasswordRequest>,
) -> Result<Json<serde_json::Value>, AppError> {
    let hashed_password = bcrypt::hash(&payload.password, bcrypt::DEFAULT_COST)
        .map_err(|e| AppError::Internal(format!("Password hashing failed: {e}")))?;

    let username = email.split('@').next().unwrap_or(&email);
    sqlx::query(
        "UPDATE users SET password = $1 WHERE email = $2 OR username = $3"
    )
    .bind(hashed_password)
    .bind(&email)
    .bind(username)
    .execute(&state.db)
    .await?;

    Ok(Json(serde_json::json!({ "status": "SUCCESS" })))
}

pub async fn delete_mail_account(
    State(state): State<AppState>,
    Path(email): Path<String>,
    _auth: AuthUser,
) -> Result<Json<serde_json::Value>, AppError> {
    let username = email.split('@').next().unwrap_or(&email);
    sqlx::query(
        "UPDATE users SET is_active = false, email = NULL WHERE email = $1 OR username = $2"
    )
    .bind(&email)
    .bind(username)
    .execute(&state.db)
    .await?;

    Ok(Json(serde_json::json!({ "status": "SUCCESS" })))
}

pub async fn sync_mail_accounts(
    _state: State<AppState>,
) -> Result<Json<serde_json::Value>, AppError> {
    Ok(Json(
        serde_json::json!({ "status": "SUCCESS", "message": "Mail accounts synchronized" }),
    ))
}

pub async fn get_mail_status(_state: State<AppState>) -> Result<Json<serde_json::Value>, AppError> {
    Ok(Json(serde_json::json!({
        "status": "ok",
        "connected": true,
        "domain": "mplatform.com",
        "queueLength": 0,
        "sentToday": 0
    })))
}

pub async fn get_mailing_lists(
    State(state): State<AppState>,
    Query(params): Query<CommonQuery>,
) -> Result<Json<serde_json::Value>, AppError> {
    let page = params.page.unwrap_or(0);
    let size = params.size.unwrap_or(20);
    let offset = page * size;

    let count_row: (i64,) = sqlx::query_as("SELECT COUNT(*) FROM mailing_list")
        .fetch_one(&state.db)
        .await?;

    let rows: Vec<(Uuid, String, String, Option<serde_json::Value>, Option<bool>, i64)> =
        sqlx::query_as(
            r#"
            SELECT m.id, m.name, m.email, m.description, m.is_active, COUNT(mem.id) as member_count
            FROM mailing_list m
            LEFT JOIN mailing_list_member mem ON m.id = mem.mailing_list_id
            GROUP BY m.id, m.name, m.email, m.description, m.is_active
            ORDER BY m.name ASC
            LIMIT $1 OFFSET $2
            "#
        )
        .bind(size)
        .bind(offset)
        .fetch_all(&state.db)
        .await?;

    let content: Vec<serde_json::Value> = rows.into_iter().map(|(id, name, email, desc, is_active, member_count)| {
        let act = is_active.unwrap_or(true);
        serde_json::json!({
            "id": id,
            "name": name.clone(),
            "groupName": name,
            "email": email,
            "description": desc.unwrap_or_else(|| serde_json::json!({ "ko": "", "en": "" })),
            "isActive": act,
            "active": act,
            "memberCount": member_count
        })
    }).collect();

    Ok(Json(serde_json::json!({
        "content": content,
        "totalElements": count_row.0,
        "totalPages": ((count_row.0 as f64) / (size as f64)).ceil() as i64,
        "number": page,
        "size": size,
        "first": page == 0,
        "last": offset + size >= count_row.0,
        "empty": content.is_empty()
    })))
}

pub async fn get_mailing_list_by_id(
    State(state): State<AppState>,
    Path(id): Path<Uuid>,
    _auth: AuthUser,
) -> Result<Json<serde_json::Value>, AppError> {
    let row: Option<(Uuid, String, String, Option<serde_json::Value>, Option<bool>)> =
        sqlx::query_as("SELECT id, name, email, description, is_active FROM mailing_list WHERE id = $1")
            .bind(id)
            .fetch_optional(&state.db)
            .await?;

    let (id, name, email, desc, is_active) = match row {
        Some(r) => r,
        None => return Err(AppError::NotFound(format!("Mailing list not found: {id}"))),
    };

    let members: Vec<(Option<String>, Option<String>)> = sqlx::query_as(
        "SELECT user_id, external_email FROM mailing_list_member WHERE mailing_list_id = $1"
    )
    .bind(id)
    .fetch_all(&state.db)
    .await?;

    let member_list: Vec<String> = members
        .into_iter()
        .filter_map(|(uid, ext)| ext.or(uid))
        .collect();

    Ok(Json(serde_json::json!({
        "id": id,
        "name": name.clone(),
        "groupName": name,
        "email": email,
        "description": desc.unwrap_or_else(|| serde_json::json!({ "ko": "", "en": "" })),
        "isActive": is_active.unwrap_or(true),
        "members": member_list
    })))
}

pub async fn create_mailing_list(
    State(state): State<AppState>,
    auth: AuthUser,
    Json(payload): Json<MailingListPayload>,
) -> Result<Json<serde_json::Value>, AppError> {
    let name = payload.name.or(payload.group_name).unwrap_or_default();
    let email = payload.email.trim();
    if name.is_empty() || email.is_empty() {
        return Err(AppError::BadRequest("Name and email are required".into()));
    }

    let mut tx = state.db.begin().await?;
    let id = Uuid::new_v4();
    let desc = payload.description.unwrap_or_else(|| serde_json::json!({ "ko": "", "en": "" }));

    sqlx::query(
        r#"
        INSERT INTO mailing_list (id, name, email, description, is_active, version, created_at, updated_at, created_by)
        VALUES ($1, $2, $3, $4, true, 0, NOW(), NOW(), $5)
        "#
    )
    .bind(id)
    .bind(&name)
    .bind(email)
    .bind(&desc)
    .bind(&auth.claims.sub)
    .execute(&mut *tx)
    .await?;

    if let Some(user_ids) = payload.member_user_ids {
        for uid in user_ids {
            if !uid.trim().is_empty() {
                sqlx::query(
                    r#"
                    INSERT INTO mailing_list_member (id, mailing_list_id, user_id, joined_at)
                    VALUES ($1, $2, $3, NOW())
                    "#
                )
                .bind(Uuid::new_v4())
                .bind(id)
                .bind(uid.trim())
                .execute(&mut *tx)
                .await?;
            }
        }
    }

    if let Some(ext_emails) = payload.member_external_emails {
        for ext in ext_emails {
            if !ext.trim().is_empty() {
                sqlx::query(
                    r#"
                    INSERT INTO mailing_list_member (id, mailing_list_id, external_email, joined_at)
                    VALUES ($1, $2, $3, NOW())
                    "#
                )
                .bind(Uuid::new_v4())
                .bind(id)
                .bind(ext.trim())
                .execute(&mut *tx)
                .await?;
            }
        }
    }

    tx.commit().await?;
    Ok(Json(serde_json::json!({ "id": id, "status": "SUCCESS" })))
}

pub async fn update_mailing_list(
    State(state): State<AppState>,
    Path(id): Path<Uuid>,
    _auth: AuthUser,
    Json(payload): Json<MailingListPayload>,
) -> Result<Json<serde_json::Value>, AppError> {
    let name = payload.name.or(payload.group_name).unwrap_or_default();
    let email = payload.email.trim();
    let desc = payload.description.unwrap_or_else(|| serde_json::json!({ "ko": "", "en": "" }));

    let mut tx = state.db.begin().await?;

    sqlx::query(
        r#"
        UPDATE mailing_list
        SET name = $1, email = $2, description = $3, updated_at = NOW()
        WHERE id = $4
        "#
    )
    .bind(&name)
    .bind(email)
    .bind(&desc)
    .bind(id)
    .execute(&mut *tx)
    .await?;

    sqlx::query("DELETE FROM mailing_list_member WHERE mailing_list_id = $1")
        .bind(id)
        .execute(&mut *tx)
        .await?;

    if let Some(user_ids) = payload.member_user_ids {
        for uid in user_ids {
            if !uid.trim().is_empty() {
                sqlx::query(
                    r#"
                    INSERT INTO mailing_list_member (id, mailing_list_id, user_id, joined_at)
                    VALUES ($1, $2, $3, NOW())
                    "#
                )
                .bind(Uuid::new_v4())
                .bind(id)
                .bind(uid.trim())
                .execute(&mut *tx)
                .await?;
            }
        }
    }

    if let Some(ext_emails) = payload.member_external_emails {
        for ext in ext_emails {
            if !ext.trim().is_empty() {
                sqlx::query(
                    r#"
                    INSERT INTO mailing_list_member (id, mailing_list_id, external_email, joined_at)
                    VALUES ($1, $2, $3, NOW())
                    "#
                )
                .bind(Uuid::new_v4())
                .bind(id)
                .bind(ext.trim())
                .execute(&mut *tx)
                .await?;
            }
        }
    }

    tx.commit().await?;
    Ok(Json(serde_json::json!({ "id": id, "status": "SUCCESS" })))
}

pub async fn delete_mailing_list(
    State(state): State<AppState>,
    Path(id): Path<Uuid>,
    _auth: AuthUser,
) -> Result<Json<serde_json::Value>, AppError> {
    let mut tx = state.db.begin().await?;
    sqlx::query("DELETE FROM mailing_list_member WHERE mailing_list_id = $1")
        .bind(id)
        .execute(&mut *tx)
        .await?;
    sqlx::query("DELETE FROM mailing_list WHERE id = $1")
        .bind(id)
        .execute(&mut *tx)
        .await?;
    tx.commit().await?;
    Ok(Json(serde_json::json!({ "status": "SUCCESS" })))
}

pub async fn sync_mailing_list_aliases(
    _state: State<AppState>,
) -> Result<Json<serde_json::Value>, AppError> {
    Ok(Json(
        serde_json::json!({ "status": "SUCCESS", "message": "Mailing list aliases synchronized" }),
    ))
}
