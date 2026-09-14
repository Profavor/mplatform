use crate::error::AppError;
use crate::middleware::auth::AuthUser;
use crate::models::approval::WorkflowConfig;
use crate::models::record::{CreateRecordRequest, PageResponse, Record, RecordChangeRequestDto};
use crate::repositories::record_repo::{DynamicRecordQuery, RecordRepository};
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
    let page = params
        .get("page")
        .and_then(|p| p.parse::<i64>().ok())
        .unwrap_or(0);
    let size = params
        .get("size")
        .and_then(|s| s.parse::<i64>().ok())
        .unwrap_or(20);
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
    let node_id = req
        .node_id
        .ok_or_else(|| AppError::BadRequest("nodeId is required".to_string()))?;
    req.data = RecordService::process_data_for_save(
        &state.db,
        node_id,
        req.data,
        &state.field_encryption_service,
    )
    .await?;
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
    let effective_node_id = req.node_id.unwrap_or(node_id);
    req.node_id = Some(effective_node_id);
    req.data = RecordService::process_data_for_save(
        &state.db,
        effective_node_id,
        req.data,
        &state.field_encryption_service,
    )
    .await?;
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
        let processed_data = RecordService::process_data_for_save(
            &state.db,
            node_id,
            rec,
            &state.field_encryption_service,
        )
        .await?;
        let req = CreateRecordRequest {
            node_id: Some(node_id),
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
        "#,
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
        "#,
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
        "#,
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

pub async fn request_record_update(
    State(state): State<AppState>,
    Path(id): Path<Uuid>,
    auth: AuthUser,
    Json(payload): Json<RecordChangeRequestDto>,
) -> Result<Json<serde_json::Value>, AppError> {
    let record = RecordRepository::find_by_id(&state.db, id)
        .await?
        .ok_or_else(|| AppError::NotFound(format!("Record not found: {id}")))?;

    let requester = payload.get_requester(&auth.claims.sub);
    let reason = payload.get_reason();
    let patch_data = payload.get_patch_data();

    // Merge incoming partial data into existing record data
    let mut current_map = match record.data.clone() {
        Some(serde_json::Value::Object(map)) => map,
        _ => serde_json::Map::new(),
    };
    let prev_data = serde_json::Value::Object(current_map.clone());

    if let serde_json::Value::Object(patch_map) = patch_data {
        for (k, v) in patch_map {
            current_map.insert(k, v);
        }
    }
    let combined_data = serde_json::Value::Object(current_map);
    let processed_data = RecordService::process_data_for_save(
        &state.db,
        record.node_id,
        combined_data,
        &state.field_encryption_service,
    )
    .await?;

    let changes_payload = serde_json::json!({
        "before": prev_data,
        "after": processed_data
    });

    // Resolve workflow config for UPDATE
    let workflow_config = if let Some(w_id) = payload.workflow_config_id {
        sqlx::query_as::<_, WorkflowConfig>(
            "SELECT * FROM workflow_config WHERE id = $1 AND is_active = true",
        )
        .bind(w_id)
        .fetch_optional(&state.db)
        .await?
    } else {
        let node_cfg = sqlx::query_as::<_, WorkflowConfig>(
            "SELECT * FROM workflow_config WHERE node_id = $1 AND action_type = 'UPDATE' AND is_active = true ORDER BY is_default DESC LIMIT 1"
        )
        .bind(record.node_id)
        .fetch_optional(&state.db)
        .await?;

        if node_cfg.is_some() {
            node_cfg
        } else {
            sqlx::query_as::<_, WorkflowConfig>(
                r#"
                SELECT w.* FROM workflow_config w
                JOIN classification_node n ON n.domain_id = w.domain_id
                WHERE n.id = $1 AND w.node_id IS NULL AND w.action_type = 'UPDATE' AND w.is_active = true
                ORDER BY w.is_default DESC LIMIT 1
                "#
            )
            .bind(record.node_id)
            .fetch_optional(&state.db)
            .await?
        }
    };

    // Determine whether workflow has approval steps
    let mut step_count = 0;
    let mut configured_steps = Vec::new();
    if let Some(ref cfg) = workflow_config {
        if let Some(ref sc) = cfg.steps_config {
            if let Ok(root) = serde_json::from_str::<serde_json::Value>(sc) {
                let steps_arr = root
                    .get("approvalLine")
                    .and_then(|v| v.as_array())
                    .or_else(|| root.get("steps").and_then(|v| v.as_array()));
                if let Some(steps) = steps_arr {
                    for step in steps {
                        let order = step
                            .get("stepOrder")
                            .or_else(|| step.get("step_order"))
                            .and_then(|v| v.as_i64())
                            .unwrap_or(0);
                        if order > 0 {
                            step_count += 1;
                            configured_steps.push((order as i32, step.clone()));
                        }
                    }
                }
            }
        }
    }

    let request_id = Uuid::new_v4();

    if step_count == 0 {
        // ----------------------------------------------------
        // Auto-approval (0 steps configured)
        // ----------------------------------------------------
        let mut tx = state.db.begin().await?;

        // 1. Insert approval_request with status = 'APPROVED'
        sqlx::query(
            r#"
            INSERT INTO approval_request (
                id, target_id, target_type, status, requester_id,
                reason, changes, node_id, current_step_order, version, created_at, updated_at
            ) VALUES (
                $1, $2, 'RECORD_UPDATE', 'APPROVED', $3,
                $4, $5, $6, 0, 1, NOW(), NOW()
            )
            "#,
        )
        .bind(request_id)
        .bind(record.id)
        .bind(&requester)
        .bind(&reason)
        .bind(&changes_payload)
        .bind(record.node_id)
        .execute(&mut *tx)
        .await?;

        // 2. Insert approval_step for DRAFT with status = 'APPROVED'
        let draft_step_id = Uuid::new_v4();
        sqlx::query(
            r#"
            INSERT INTO approval_step (
                id, request_id, step_order, step_type, assignee_id,
                status, comment, sla_hours, sla_due_at, is_escalated, version, created_at, updated_at
            ) VALUES (
                $1, $2, 0, 'DRAFT', $3,
                'APPROVED', '시스템 자동 승인 (결재선 미설정)', 48, NOW() + INTERVAL '48 hours', false, 1, NOW(), NOW()
            )
            "#
        )
        .bind(draft_step_id)
        .bind(request_id)
        .bind(&requester)
        .execute(&mut *tx)
        .await?;

        // 3. Update record data, searchable_data, and version
        let new_version = record.version + 1;
        sqlx::query(
            r#"
            UPDATE record
            SET data = $1, searchable_data = $1, version = $2, updated_at = NOW()
            WHERE id = $3
            "#,
        )
        .bind(&processed_data)
        .bind(new_version)
        .bind(record.id)
        .execute(&mut *tx)
        .await?;

        // 4. Insert record_history
        sqlx::query(
            r#"
            INSERT INTO record_history (
                id, record_id, version, change_type, changed_by,
                previous_data, new_data, source_system, approval_request_id, changed_at
            ) VALUES (
                gen_random_uuid(), $1, $2, 'UPDATE', $3,
                $4, $5, $6, $7, NOW()
            )
            "#,
        )
        .bind(record.id)
        .bind(new_version)
        .bind(&requester)
        .bind(&prev_data)
        .bind(&processed_data)
        .bind(record.source_system.as_deref())
        .bind(request_id)
        .execute(&mut *tx)
        .await?;

        tx.commit().await?;

        // 5. Asynchronously dispatch outbound webhook
        let pool_clone = state.db.clone();
        let rec_id = record.id;
        let node_id = record.node_id;
        let d_clone = processed_data.clone();
        tokio::spawn(async move {
            let _ = crate::services::outbound_service::OutboundService::dispatch_record_change(
                &pool_clone,
                rec_id,
                node_id,
                "UPDATE",
                &d_clone,
            )
            .await;
        });

        tracing::info!(
            "✅ Record {} auto-approved & merged via workflow (0 steps). Request ID: {}",
            record.id,
            request_id
        );
    } else {
        // ----------------------------------------------------
        // Multi-step approval workflow
        // ----------------------------------------------------
        let mut tx = state.db.begin().await?;

        // 1. Insert approval_request with status = 'PENDING'
        sqlx::query(
            r#"
            INSERT INTO approval_request (
                id, target_id, target_type, status, requester_id,
                reason, changes, node_id, current_step_order, version, created_at, updated_at
            ) VALUES (
                $1, $2, 'RECORD_UPDATE', 'PENDING', $3,
                $4, $5, $6, 1, 0, NOW(), NOW()
            )
            "#,
        )
        .bind(request_id)
        .bind(record.id)
        .bind(&requester)
        .bind(&reason)
        .bind(&changes_payload)
        .bind(record.node_id)
        .execute(&mut *tx)
        .await?;

        // 2. Insert DRAFT step (stepOrder = 0)
        let draft_step_id = Uuid::new_v4();
        sqlx::query(
            r#"
            INSERT INTO approval_step (
                id, request_id, step_order, step_type, assignee_id,
                status, comment, sla_hours, sla_due_at, is_escalated, version, created_at, updated_at
            ) VALUES (
                $1, $2, 0, 'DRAFT', $3,
                'SUBMITTED', $4, 48, NOW() + INTERVAL '48 hours', false, 0, NOW(), NOW()
            )
            "#
        )
        .bind(draft_step_id)
        .bind(request_id)
        .bind(&requester)
        .bind(&reason)
        .execute(&mut *tx)
        .await?;

        // 3. Insert configured approval steps
        for (order, step_node) in configured_steps {
            let step_id = Uuid::new_v4();
            let step_type = step_node
                .get("stepType")
                .and_then(|v| v.as_str())
                .unwrap_or("APPROVAL");
            let assignee_id = step_node
                .get("assigneeId")
                .and_then(|v| v.as_str())
                .filter(|s| !s.is_empty() && *s != "null");
            let assignee_role = step_node
                .get("assigneeRole")
                .and_then(|v| v.as_str())
                .filter(|s| !s.is_empty() && *s != "null");
            let initial_status = if order == 1 { "PENDING" } else { "WAITING" };

            sqlx::query(
                r#"
                INSERT INTO approval_step (
                    id, request_id, step_order, step_type, assignee_id, assignee_role,
                    status, is_escalated, version, created_at, updated_at
                ) VALUES (
                    $1, $2, $3, $4, $5, $6,
                    $7, false, 0, NOW(), NOW()
                )
                "#,
            )
            .bind(step_id)
            .bind(request_id)
            .bind(order)
            .bind(step_type)
            .bind(assignee_id)
            .bind(assignee_role)
            .bind(initial_status)
            .execute(&mut *tx)
            .await?;
        }

        tx.commit().await?;
        tracing::info!(
            "⏳ Record {} update approval request submitted with {} steps. Request ID: {}",
            record.id,
            step_count,
            request_id
        );
    }

    Ok(Json(
        serde_json::json!({ "success": true, "requestId": request_id }),
    ))
}

pub async fn request_record_delete(
    State(state): State<AppState>,
    Path(id): Path<Uuid>,
    auth: AuthUser,
    Json(payload): Json<RecordChangeRequestDto>,
) -> Result<Json<serde_json::Value>, AppError> {
    let new_id = Uuid::new_v4();
    let requester = payload.get_requester(&auth.claims.sub);
    let reason = payload.get_reason();

    sqlx::query(
        r#"
        INSERT INTO approval_request (
            id, target_id, target_type, status, requester_id,
            reason, changes, version, created_at, updated_at
        ) VALUES (
            $1, $2, 'RECORD_DELETE', 'PENDING', $3,
            $4, '{}'::jsonb, 0, NOW(), NOW()
        )
        "#,
    )
    .bind(new_id)
    .bind(id)
    .bind(&requester)
    .bind(reason)
    .execute(&state.db)
    .await?;

    Ok(Json(
        serde_json::json!({ "success": true, "requestId": new_id }),
    ))
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
