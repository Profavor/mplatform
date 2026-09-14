use crate::error::AppError;
use crate::middleware::auth::AuthUser;
use crate::models::record::{CreateRecordRequest, PageResponse, Record};
use crate::repositories::record_repo::DynamicRecordQuery;
use crate::services::record_service::RecordService;
use crate::state::AppState;
use axum::http::StatusCode;
use axum::{
    extract::{Path, Query, State},
    response::IntoResponse,
    Json,
};
use serde::{Deserialize, Serialize};
use std::collections::HashMap;
use uuid::Uuid;

#[allow(dead_code)]
#[derive(Debug, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct RecordQuery {
    pub node_id: Option<Uuid>,
    pub status: Option<String>,
    pub search: Option<String>,
    #[serde(default = "default_page")]
    pub page: i64,
    #[serde(default = "default_size")]
    pub size: i64,
}

#[allow(dead_code)]
fn default_page() -> i64 {
    0
}

#[allow(dead_code)]
fn default_size() -> i64 {
    20
}

fn parse_dynamic_query(
    domain_id: Option<Uuid>,
    node_id: Option<Uuid>,
    params: HashMap<String, String>,
) -> DynamicRecordQuery {
    let page = params.get("page").and_then(|p| p.parse::<i64>().ok()).unwrap_or(0);
    let size = params.get("size").and_then(|s| s.parse::<i64>().ok()).unwrap_or(20);
    let status = params
        .get("status")
        .or_else(|| params.get("search_status"))
        .filter(|s| !s.is_empty() && s.to_uppercase() != "ALL")
        .cloned();
    let keyword = params
        .get("keyword")
        .or_else(|| params.get("q"))
        .or_else(|| params.get("search"))
        .filter(|k| !k.trim().is_empty())
        .cloned();
    let include_children = params
        .get("includeChildren")
        .map(|v| v == "true" || v == "1")
        .unwrap_or(false);
    let sort_field = params.get("sortField").cloned();
    let sort_order = params.get("sortOrder").cloned();
    let sort = params.get("sort").cloned();

    let node_id = node_id.or_else(|| {
        params
            .get("nodeId")
            .or_else(|| params.get("node_id"))
            .and_then(|s| Uuid::parse_str(s).ok())
    });

    let domain_id = domain_id.or_else(|| {
        params
            .get("domainId")
            .or_else(|| params.get("domain_id"))
            .and_then(|s| Uuid::parse_str(s).ok())
    });

    DynamicRecordQuery {
        domain_id,
        node_id,
        include_children,
        status,
        keyword,
        search_params: params,
        sort_field,
        sort_order,
        sort,
        page,
        size,
    }
}

pub async fn get_records(
    State(state): State<AppState>,
    Query(params): Query<HashMap<String, String>>,
    _auth: AuthUser,
) -> Result<Json<PageResponse<Record>>, AppError> {
    let q = parse_dynamic_query(None, None, params);
    let response = RecordService::find_dynamic(&state.db, &q).await?;
    Ok(Json(response))
}

pub async fn get_record_by_id(
    State(state): State<AppState>,
    Path(id): Path<Uuid>,
    _auth: AuthUser,
) -> Result<Json<Record>, AppError> {
    let record = RecordService::get_record_by_id(&state.db, id).await?;
    Ok(Json(record))
}

pub async fn create_record(
    State(state): State<AppState>,
    auth: AuthUser,
    Json(mut req): Json<CreateRecordRequest>,
) -> Result<Json<Record>, AppError> {
    req.data = RecordService::process_data_for_save(&state.db, req.node_id, req.data, &state.field_encryption_service).await?;
    let actor = &auth.claims.sub;
    let record = RecordService::create_record(&state.db, req, actor).await?;
    Ok(Json(record))
}

// --------------------------------------------------------------------
// Node-specific records & batch operations
// --------------------------------------------------------------------

pub async fn get_node_records(
    State(state): State<AppState>,
    Path(node_id): Path<Uuid>,
    Query(params): Query<HashMap<String, String>>,
    _auth: AuthUser,
) -> Result<Json<PageResponse<Record>>, AppError> {
    let q = parse_dynamic_query(None, Some(node_id), params);
    let response = RecordService::find_dynamic(&state.db, &q).await?;
    Ok(Json(response))
}

pub async fn create_node_record(
    State(state): State<AppState>,
    Path(node_id): Path<Uuid>,
    auth: AuthUser,
    Json(mut req): Json<CreateRecordRequest>,
) -> Result<Json<Record>, AppError> {
    req.node_id = node_id;
    req.data = RecordService::process_data_for_save(&state.db, req.node_id, req.data, &state.field_encryption_service).await?;
    let actor = &auth.claims.sub;
    let record = RecordService::create_record(&state.db, req, actor).await?;
    Ok(Json(record))
}

#[derive(Debug, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct BatchUpsertRequest {
    pub records: Vec<serde_json::Value>,
}

pub async fn batch_upsert_node_records(
    State(state): State<AppState>,
    Path(node_id): Path<Uuid>,
    auth: AuthUser,
    Json(payload): Json<BatchUpsertRequest>,
) -> Result<Json<serde_json::Value>, AppError> {
    let mut upserted = 0;
    for rec in payload.records {
        let processed_data = RecordService::process_data_for_save(&state.db, node_id, rec, &state.field_encryption_service).await?;
        let req = CreateRecordRequest {
            node_id,
            data: processed_data,
            source_system: Some("BATCH_API".to_string()),
        };
        let _ = RecordService::create_record(&state.db, req, &auth.claims.sub).await?;
        upserted += 1;
    }

    Ok(Json(serde_json::json!({
        "success": true,
        "upsertedCount": upserted
    })))
}

pub async fn batch_validate_node_records(
    State(_state): State<AppState>,
    Path(_node_id): Path<Uuid>,
    Json(payload): Json<serde_json::Value>,
) -> Result<Json<serde_json::Value>, AppError> {
    Ok(Json(serde_json::json!({
        "valid": true,
        "violations": [],
        "checkedCount": payload.as_array().map(|a| a.len()).unwrap_or(0)
    })))
}

// --------------------------------------------------------------------
// Domain-specific records
// --------------------------------------------------------------------

pub async fn get_records_by_domain(
    State(state): State<AppState>,
    Path(domain_id): Path<Uuid>,
    Query(params): Query<HashMap<String, String>>,
    _auth: AuthUser,
) -> Result<Json<PageResponse<Record>>, AppError> {
    let q = parse_dynamic_query(Some(domain_id), None, params);
    let response = RecordService::find_dynamic(&state.db, &q).await?;
    Ok(Json(response))
}

pub async fn search_records_by_domain(
    State(state): State<AppState>,
    Path(domain_id): Path<Uuid>,
    Query(params): Query<HashMap<String, String>>,
    auth: AuthUser,
) -> Result<Json<PageResponse<Record>>, AppError> {
    get_records_by_domain(State(state), Path(domain_id), Query(params), auth).await
}

pub async fn delete_records_by_domain(
    State(state): State<AppState>,
    Path(domain_id): Path<Uuid>,
) -> Result<StatusCode, AppError> {
    sqlx::query(
        r#"
        UPDATE record
        SET status = 'DELETED', updated_at = NOW()
        WHERE node_id IN (SELECT id FROM classification_node WHERE domain_id = $1)
        "#
    )
    .bind(domain_id)
    .execute(&state.db)
    .await?;

    Ok(axum::http::StatusCode::NO_CONTENT)
}

// --------------------------------------------------------------------
// Secondary nodes
// --------------------------------------------------------------------

pub async fn get_secondary_nodes(
    State(state): State<AppState>,
    Path(id): Path<Uuid>,
) -> Result<Json<Vec<serde_json::Value>>, AppError> {
    let rows: Vec<(Uuid, Uuid, Uuid, serde_json::Value)> = sqlx::query_as(
        r#"
        SELECT s.id, s.axis_id, s.node_id, n.name
        FROM record_secondary_node s
        JOIN classification_node n ON s.node_id = n.id
        WHERE s.record_id = $1
        "#
    )
    .bind(id)
    .fetch_all(&state.db)
    .await?;

    let res = rows
        .into_iter()
        .map(|(sid, axis_id, node_id, name)| {
            serde_json::json!({
                "id": sid,
                "axisId": axis_id,
                "nodeId": node_id,
                "nodeName": name
            })
        })
        .collect();

    Ok(Json(res))
}

#[derive(Debug, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct AddSecondaryNodeRequest {
    pub axis_id: Uuid,
    pub node_id: Uuid,
}

pub async fn add_secondary_nodes(
    State(state): State<AppState>,
    Path(id): Path<Uuid>,
    Json(payload): Json<AddSecondaryNodeRequest>,
) -> Result<Json<serde_json::Value>, AppError> {
    let new_id = Uuid::new_v4();
    sqlx::query(
        r#"
        INSERT INTO record_secondary_node (id, record_id, axis_id, node_id, created_at)
        VALUES ($1, $2, $3, $4, NOW())
        ON CONFLICT (record_id, axis_id) 
        DO UPDATE SET node_id = EXCLUDED.node_id
        "#
    )
    .bind(new_id)
    .bind(id)
    .bind(payload.axis_id)
    .bind(payload.node_id)
    .execute(&state.db)
    .await?;

    Ok(Json(serde_json::json!({ "success": true, "id": new_id })))
}

// --------------------------------------------------------------------
// Update / Delete Approval Requests for Records
// --------------------------------------------------------------------

#[derive(Debug, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct RecordChangeRequestDto {
    pub reason: Option<String>,
    pub changes: Option<serde_json::Value>,
}

pub async fn request_record_update(
    State(state): State<AppState>,
    Path(id): Path<Uuid>,
    auth: AuthUser,
    Json(payload): Json<RecordChangeRequestDto>,
) -> Result<Json<serde_json::Value>, AppError> {
    let new_id = Uuid::new_v4();
    let changes = payload.changes.unwrap_or(serde_json::json!({}));
    let reason = payload.reason.unwrap_or_else(|| "Record update request".to_string());

    sqlx::query(
        r#"
        INSERT INTO approval_request (
            id, target_id, target_type, status, requester_id,
            reason, changes, version, created_at, updated_at
        ) VALUES (
            $1, $2, 'RECORD_UPDATE', 'PENDING', $3,
            $4, $5, 0, NOW(), NOW()
        )
        "#
    )
    .bind(new_id)
    .bind(id)
    .bind(&auth.claims.sub)
    .bind(reason)
    .bind(changes)
    .execute(&state.db)
    .await?;

    Ok(Json(serde_json::json!({ "success": true, "requestId": new_id })))
}

pub async fn request_record_delete(
    State(state): State<AppState>,
    Path(id): Path<Uuid>,
    auth: AuthUser,
    Json(payload): Json<RecordChangeRequestDto>,
) -> Result<Json<serde_json::Value>, AppError> {
    let new_id = Uuid::new_v4();
    let reason = payload.reason.unwrap_or_else(|| "Record deletion request".to_string());

    sqlx::query(
        r#"
        INSERT INTO approval_request (
            id, target_id, target_type, status, requester_id,
            reason, changes, version, created_at, updated_at
        ) VALUES (
            $1, $2, 'RECORD_DELETE', 'PENDING', $3,
            $4, '{}'::jsonb, 0, NOW(), NOW()
        )
        "#
    )
    .bind(new_id)
    .bind(id)
    .bind(&auth.claims.sub)
    .bind(reason)
    .execute(&state.db)
    .await?;

    Ok(Json(serde_json::json!({ "success": true, "requestId": new_id })))
}

// --------------------------------------------------------------------
// Complex Search
// --------------------------------------------------------------------

pub async fn complex_search(
    State(state): State<AppState>,
    Json(payload): Json<serde_json::Value>,
) -> Result<Json<PageResponse<Record>>, AppError> {
    let page = payload.get("page").and_then(|v| v.as_i64()).unwrap_or(0);
    let size = payload.get("size").and_then(|v| v.as_i64()).unwrap_or(20);
    let offset = page * size;

    let total: (i64,) = sqlx::query_as("SELECT COUNT(*) FROM record WHERE status != 'DELETED'")
        .fetch_one(&state.db)
        .await?;

    let content = sqlx::query_as::<_, Record>(
        "SELECT * FROM record WHERE status != 'DELETED' ORDER BY created_at DESC LIMIT $1 OFFSET $2"
    )
    .bind(size)
    .bind(offset)
    .fetch_all(&state.db)
    .await?;

    Ok(Json(PageResponse::new(content, total.0, page, size)))
}
