use axum::http::StatusCode;
use axum::{
    extract::{Path, Query, State},
    Json,
};
use serde::{Deserialize, Serialize};
use uuid::Uuid;

use crate::{
    error::AppError,
    middleware::auth::AuthUser,
    models::record::{Record, RecordHistory},
    state::AppState,
};

#[derive(Debug, Deserialize)]
pub struct RollbackRequest {
    pub target_version: i32,
    pub reason: Option<String>,
}

#[derive(Debug, Deserialize)]
pub struct TimeMachineQuery {
    pub from_version: Option<i32>,
    pub to_version: Option<i32>,
    pub as_of: Option<String>,
}

pub async fn get_record_history(
    State(state): State<AppState>,
    Path(id): Path<Uuid>,
    _auth: AuthUser,
) -> Result<Json<Vec<RecordHistory>>, AppError> {
    let mut history = sqlx::query_as::<_, RecordHistory>(
        r#"
        SELECT * FROM record_history 
        WHERE record_id = $1 
        ORDER BY version DESC, changed_at DESC
        "#,
    )
    .bind(id)
    .fetch_all(&state.db)
    .await?;

    let rec_node: Option<(Uuid,)> = sqlx::query_as("SELECT node_id FROM record WHERE id = $1")
        .bind(id)
        .fetch_optional(&state.db)
        .await?;

    if let Some((node_id,)) = rec_node {
        if let Ok(fields) =
            crate::handlers::field_definition::fetch_effective_fields(&state.db, node_id).await
        {
            for h in history.iter_mut() {
                if let Some(prev) = h.previous_data.as_mut() {
                    crate::services::data_masking_service::DataMaskingService::mask_json_data(
                        prev, &fields, false,
                    );
                }
                if let Some(new_d) = h.new_data.as_mut() {
                    crate::services::data_masking_service::DataMaskingService::mask_json_data(
                        new_d, &fields, false,
                    );
                }
            }
        }
    }

    Ok(Json(history))
}

pub async fn rollback_record(
    State(state): State<AppState>,
    Path(id): Path<Uuid>,
    auth: AuthUser,
    Json(payload): Json<RollbackRequest>,
) -> Result<Json<Record>, AppError> {
    let target_history = sqlx::query_as::<_, RecordHistory>(
        "SELECT * FROM record_history WHERE record_id = $1 AND version = $2",
    )
    .bind(id)
    .bind(payload.target_version)
    .fetch_optional(&state.db)
    .await?
    .ok_or_else(|| {
        AppError::NotFound(format!(
            "Version {} not found for rollback",
            payload.target_version
        ))
    })?;

    let restored_data = target_history.new_data.unwrap_or(serde_json::json!({}));

    // Update current record
    let updated = sqlx::query_as::<_, Record>(
        r#"
        UPDATE record
        SET data = $1, version = version + 1, updated_at = NOW()
        WHERE id = $2
        RETURNING *
        "#,
    )
    .bind(&restored_data)
    .bind(id)
    .fetch_one(&state.db)
    .await?;

    // Record rollback history
    let new_hid = Uuid::new_v4();
    let reason = payload
        .reason
        .unwrap_or_else(|| format!("Rollback to version {}", payload.target_version));
    sqlx::query(
        r#"
        INSERT INTO record_history (
            id, record_id, version, change_type, changed_by,
            new_data, source_system, changed_at
        ) VALUES (
            $1, $2, $3, 'ROLLBACK', $4,
            $5, $6, NOW()
        )
        "#,
    )
    .bind(new_hid)
    .bind(id)
    .bind(updated.version)
    .bind(&auth.claims.sub)
    .bind(&restored_data)
    .bind(reason)
    .execute(&state.db)
    .await?;

    Ok(Json(updated))
}

pub async fn get_record_lineage(
    State(state): State<AppState>,
    Path(id): Path<Uuid>,
    _auth: AuthUser,
) -> Result<Json<serde_json::Value>, AppError> {
    let rec = sqlx::query_as::<_, Record>("SELECT * FROM record WHERE id = $1")
        .bind(id)
        .fetch_optional(&state.db)
        .await?
        .ok_or_else(|| AppError::NotFound("Record not found".to_string()))?;

    let history = sqlx::query_as::<_, RecordHistory>(
        "SELECT * FROM record_history WHERE record_id = $1 ORDER BY version ASC",
    )
    .bind(id)
    .fetch_all(&state.db)
    .await?;

    let mut nodes = Vec::new();
    let mut links = Vec::new();

    // Source system node
    let src_node_id = format!("src_{}", id);
    nodes.push(serde_json::json!({
        "id": src_node_id,
        "name": rec.source_system.as_deref().unwrap_or("System"),
        "type": "SOURCE_SYSTEM"
    }));

    // Master record node
    let rec_node_id = format!("rec_{}", id);
    nodes.push(serde_json::json!({
        "id": rec_node_id,
        "name": format!("Record v{}", rec.version),
        "type": "MASTER_RECORD",
        "status": rec.status
    }));

    links.push(serde_json::json!({
        "source": src_node_id,
        "target": rec_node_id,
        "relation": "CREATED_FROM"
    }));

    // History version links
    for h in history {
        let h_id = format!("ver_{}_{}", id, h.version);
        nodes.push(serde_json::json!({
            "id": h_id,
            "name": format!("v{} ({})", h.version, h.change_type.as_deref().unwrap_or("UPDATE")),
            "type": "VERSION_SNAPSHOT",
            "changedBy": h.changed_by
        }));
        links.push(serde_json::json!({
            "source": h_id,
            "target": rec_node_id,
            "relation": "CONTRIBUTES_TO"
        }));
    }

    // Outbound Pipeline & Downstream Consumers (Stage 4 & 5)
    #[derive(sqlx::FromRow)]
    struct OutboundLineageChannel {
        id: Uuid,
        name: String,
        channel_code: Option<String>,
        mapping_config_json: Option<String>,
    }

    let outbound_channels = sqlx::query_as::<_, OutboundLineageChannel>(
        r#"
        SELECT id, name, channel_code, mapping_config_json
        FROM integration_channels
        WHERE is_active = true AND direction = 'OUTBOUND'
          AND (node_id = $1 OR node_id IS NULL)
        "#,
    )
    .bind(rec.node_id)
    .fetch_all(&state.db)
    .await
    .unwrap_or_default();

    for ch in outbound_channels {
        let ch_name = if let Ok(val) = serde_json::from_str::<serde_json::Value>(&ch.name) {
            val.get("ko")
                .or_else(|| val.get("en"))
                .and_then(|v| v.as_str())
                .unwrap_or(&ch.name)
                .to_string()
        } else {
            ch.name.clone()
        };

        let log_status: Option<(String,)> = sqlx::query_as(
            "SELECT status FROM integration_logs WHERE channel_id = $1 AND record_id = $2 ORDER BY created_at DESC LIMIT 1"
        )
        .bind(ch.id)
        .bind(id)
        .fetch_optional(&state.db)
        .await
        .unwrap_or(None);

        let out_id = format!("out_{}", ch.id);
        let cns_id = format!("cns_{}", ch.id);
        let node_status = log_status
            .as_ref()
            .map(|s| s.0.as_str())
            .unwrap_or("HEALTHY");

        nodes.push(serde_json::json!({
            "id": out_id,
            "name": format!("Outbound: {}", ch_name),
            "type": "OUTBOUND",
            "stage": "OUTBOUND_PIPELINE",
            "status": node_status
        }));

        links.push(serde_json::json!({
            "source": rec_node_id,
            "target": out_id,
            "relation": "DISPATCHES_TO"
        }));

        nodes.push(serde_json::json!({
            "id": cns_id,
            "name": format!("Consumer: {}", ch_name),
            "type": "CONSUMER",
            "stage": "DOWNSTREAM_CONSUMER",
            "status": node_status
        }));

        links.push(serde_json::json!({
            "source": out_id,
            "target": cns_id,
            "relation": "CONSUMED_BY"
        }));
    }

    Ok(Json(serde_json::json!({
        "recordId": id,
        "nodes": nodes,
        "links": links
    })))
}

pub async fn get_record_timemachine_diff(
    State(state): State<AppState>,
    Path(id): Path<Uuid>,
    Query(query): Query<TimeMachineQuery>,
    _auth: AuthUser,
) -> Result<Json<serde_json::Value>, AppError> {
    let from_v = query.from_version.unwrap_or(1);
    let to_v = query.to_version.unwrap_or(2);

    let h1 = sqlx::query_as::<_, RecordHistory>(
        "SELECT * FROM record_history WHERE record_id = $1 AND version = $2",
    )
    .bind(id)
    .bind(from_v)
    .fetch_optional(&state.db)
    .await?;

    let h2 = sqlx::query_as::<_, RecordHistory>(
        "SELECT * FROM record_history WHERE record_id = $1 AND version = $2",
    )
    .bind(id)
    .bind(to_v)
    .fetch_optional(&state.db)
    .await?;

    let v1_data = h1.and_then(|h| h.new_data).unwrap_or(serde_json::json!({}));
    let v2_data = h2.and_then(|h| h.new_data).unwrap_or(serde_json::json!({}));

    Ok(Json(serde_json::json!({
        "recordId": id,
        "fromVersion": from_v,
        "toVersion": to_v,
        "fromData": v1_data,
        "toData": v2_data,
        "hasDifferences": v1_data != v2_data
    })))
}

// --------------------------------------------------------------------
// Masking Preview & Masked Record
// --------------------------------------------------------------------

pub async fn get_masked_record(
    State(state): State<AppState>,
    Path(id): Path<Uuid>,
    _auth: AuthUser,
) -> Result<Json<serde_json::Value>, AppError> {
    let rec = sqlx::query_as::<_, Record>("SELECT * FROM record WHERE id = $1")
        .bind(id)
        .fetch_optional(&state.db)
        .await?
        .ok_or_else(|| AppError::NotFound("Record not found".to_string()))?;

    let masked = mask_json(rec.data.unwrap_or(serde_json::json!({})));

    Ok(Json(serde_json::json!({
        "id": rec.id,
        "nodeId": rec.node_id,
        "status": rec.status,
        "version": rec.version,
        "data": masked
    })))
}

pub async fn preview_masked_record(
    State(_state): State<AppState>,
    Path(_id): Path<Uuid>,
    Json(payload): Json<serde_json::Value>,
) -> Result<Json<serde_json::Value>, AppError> {
    let masked = mask_json(payload);
    Ok(Json(masked))
}

fn mask_json(mut val: serde_json::Value) -> serde_json::Value {
    if let Some(obj) = val.as_object_mut() {
        for (k, v) in obj.iter_mut() {
            let k_lower = k.to_lowercase();
            if k_lower.contains("ssn") || k_lower.contains("resident") || k_lower.contains("rrn") {
                *v = serde_json::json!("******-*******");
            } else if k_lower.contains("phone")
                || k_lower.contains("tel")
                || k_lower.contains("mobile")
            {
                *v = serde_json::json!("010-****-****");
            } else if k_lower.contains("email") {
                *v = serde_json::json!("***@***.com");
            } else if k_lower.contains("account") || k_lower.contains("card") {
                *v = serde_json::json!("****-****-****-****");
            }
        }
    }
    val
}
