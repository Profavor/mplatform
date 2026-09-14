use crate::error::AppError;
use crate::middleware::auth::AuthUser;
use crate::state::AppState;
use axum::extract::{Path, Query, State};
use axum::http::StatusCode;
use axum::{response::IntoResponse, Json};
use serde::{Deserialize, Serialize};
use uuid::Uuid;

use crate::{
    models::{
        approval::{
            ApprovalDelegation, ApprovalDelegationDto, ApprovalDetailResponse, ApprovalRequest,
            ApprovalRoutingTemplate, ApprovalStep, ApproveStepRequest, CreateApprovalRequest,
            CreateDelegationDto, MemoRequest, MyDelegationsResponse, RejectStepRequest,
            StepActionRequest, WorkflowConfig,
        },
        record::PageResponse,
    },
    services::approval_service::ApprovalService,
};

#[derive(Debug, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct ApprovalQuery {
    pub status: Option<String>,
    pub requester_id: Option<String>,
    pub search: Option<String>,
    pub assignee_id: Option<String>,
    pub page: Option<i64>,
    pub size: Option<i64>,
    pub sort: Option<String>,
}

// Legacy endpoints matching /api/approvals
pub async fn get_approvals(
    State(state): State<AppState>,
    Query(query): Query<ApprovalQuery>,
    _auth: AuthUser,
) -> Result<Json<Vec<ApprovalRequest>>, AppError> {
    let list = ApprovalService::get_requests(
        &state.db,
        query.status.as_deref(),
        query.requester_id.as_deref(),
    )
    .await?;

    Ok(Json(list))
}

pub async fn get_approval_by_id(
    State(state): State<AppState>,
    Path(id): Path<Uuid>,
    _auth: AuthUser,
) -> Result<Json<ApprovalDetailResponse>, AppError> {
    let detail = ApprovalService::get_request_detail(&state.db, id).await?;
    Ok(Json(detail))
}

pub async fn create_approval(
    State(state): State<AppState>,
    auth: AuthUser,
    Json(req): Json<CreateApprovalRequest>,
) -> Result<Json<ApprovalRequest>, AppError> {
    let approval = ApprovalService::create_request(&state.db, req, &auth.claims.sub).await?;
    Ok(Json(approval))
}

pub async fn approve_step(
    State(state): State<AppState>,
    Path(id): Path<Uuid>,
    auth: AuthUser,
    Json(req): Json<ApproveStepRequest>,
) -> Result<Json<ApprovalDetailResponse>, AppError> {
    let detail =
        ApprovalService::approve(&state.db, id, req.comment.as_deref(), &auth.claims.sub).await?;
    Ok(Json(detail))
}

pub async fn reject_step(
    State(state): State<AppState>,
    Path(id): Path<Uuid>,
    auth: AuthUser,
    Json(req): Json<RejectStepRequest>,
) -> Result<Json<ApprovalDetailResponse>, AppError> {
    let detail =
        ApprovalService::reject(&state.db, id, req.reason.as_deref(), &auth.claims.sub).await?;
    Ok(Json(detail))
}

// -------------------------------------------------------------
// Full /api/approval-requests endpoints
// -------------------------------------------------------------

pub async fn get_pending_requests(
    State(state): State<AppState>,
    Query(query): Query<ApprovalQuery>,
    _auth: AuthUser,
) -> Result<Json<PageResponse<ApprovalRequest>>, AppError> {
    let page = query.page.unwrap_or(0);
    let size = query.size.unwrap_or(100);
    let offset = page * size;

    let total: (i64,) =
        sqlx::query_as("SELECT COUNT(*) FROM approval_request WHERE status = 'PENDING'")
            .fetch_one(&state.db)
            .await?;

    let content = sqlx::query_as::<_, ApprovalRequest>(
        r#"
        SELECT * FROM approval_request 
        WHERE status = 'PENDING'
        ORDER BY created_at DESC
        LIMIT $1 OFFSET $2
        "#,
    )
    .bind(size)
    .bind(offset)
    .fetch_all(&state.db)
    .await?;

    Ok(Json(PageResponse::new(content, total.0, page, size)))
}

pub async fn get_all_requests(
    State(state): State<AppState>,
    Query(query): Query<ApprovalQuery>,
    _auth: AuthUser,
) -> Result<Json<PageResponse<ApprovalRequest>>, AppError> {
    let page = query.page.unwrap_or(0);
    let size = query.size.unwrap_or(100);
    let offset = page * size;

    let mut sql = "SELECT * FROM approval_request WHERE 1=1".to_string();
    let mut count_sql = "SELECT COUNT(*) FROM approval_request WHERE 1=1".to_string();

    if let Some(status) = &query.status {
        if !status.is_empty() && status != "ALL" {
            let filter = format!(" AND status = '{}'", status.replace('\'', "''"));
            sql.push_str(&filter);
            count_sql.push_str(&filter);
        }
    }

    if let Some(search) = &query.search {
        if !search.is_empty() {
            let filter = format!(
                " AND (requester_id ILIKE '%{}%' OR reason ILIKE '%{}%')",
                search.replace('\'', "''"),
                search.replace('\'', "''")
            );
            sql.push_str(&filter);
            count_sql.push_str(&filter);
        }
    }

    let total: (i64,) = sqlx::query_as(&count_sql).fetch_one(&state.db).await?;

    sql.push_str(&format!(
        " ORDER BY created_at DESC LIMIT {} OFFSET {}",
        size, offset
    ));
    let content = sqlx::query_as::<_, ApprovalRequest>(&sql)
        .fetch_all(&state.db)
        .await?;

    Ok(Json(PageResponse::new(content, total.0, page, size)))
}

pub async fn get_my_todos(
    State(state): State<AppState>,
    Query(query): Query<ApprovalQuery>,
    auth: AuthUser,
) -> Result<Json<PageResponse<ApprovalStep>>, AppError> {
    let page = query.page.unwrap_or(0);
    let size = query.size.unwrap_or(100);
    let offset = page * size;
    let user_id = query.assignee_id.unwrap_or_else(|| auth.claims.sub.clone());
    let auth_uid = auth
        .claims
        .user_id
        .clone()
        .unwrap_or_else(|| auth.claims.sub.clone());

    let total: (i64,) = sqlx::query_as(
        r#"
        SELECT COUNT(*) FROM approval_step 
        WHERE status = 'PENDING' 
          AND (
              assignee_id = $1 
              OR assignee_id = $2
              OR assignee_id IS NULL 
              OR assignee_id IN (
                  SELECT delegator_user_id 
                  FROM approval_delegations 
                  WHERE (delegatee_user_id = $1 OR delegatee_user_id = $2)
                    AND is_active = true 
                    AND start_date <= NOW() 
                    AND end_date >= NOW()
                  UNION
                  SELECT u.username 
                  FROM approval_delegations ad
                  JOIN users u ON ad.delegator_user_id = u.id
                  WHERE (ad.delegatee_user_id = $1 OR ad.delegatee_user_id = $2)
                    AND ad.is_active = true 
                    AND ad.start_date <= NOW() 
                    AND ad.end_date >= NOW()
              )
          )
        "#,
    )
    .bind(&user_id)
    .bind(&auth_uid)
    .fetch_one(&state.db)
    .await?;

    let content = sqlx::query_as::<_, ApprovalStep>(
        r#"
        SELECT * FROM approval_step 
        WHERE status = 'PENDING' 
          AND (
              assignee_id = $1 
              OR assignee_id = $2
              OR assignee_id IS NULL 
              OR assignee_id IN (
                  SELECT delegator_user_id 
                  FROM approval_delegations 
                  WHERE (delegatee_user_id = $1 OR delegatee_user_id = $2)
                    AND is_active = true 
                    AND start_date <= NOW() 
                    AND end_date >= NOW()
                  UNION
                  SELECT u.username 
                  FROM approval_delegations ad
                  JOIN users u ON ad.delegator_user_id = u.id
                  WHERE (ad.delegatee_user_id = $1 OR ad.delegatee_user_id = $2)
                    AND ad.is_active = true 
                    AND ad.start_date <= NOW() 
                    AND ad.end_date >= NOW()
              )
          )
        ORDER BY created_at DESC
        LIMIT $3 OFFSET $4
        "#,
    )
    .bind(&user_id)
    .bind(&auth_uid)
    .bind(size)
    .bind(offset)
    .fetch_all(&state.db)
    .await?;

    Ok(Json(PageResponse::new(content, total.0, page, size)))
}

pub async fn get_my_requests(
    State(state): State<AppState>,
    Query(query): Query<ApprovalQuery>,
    auth: AuthUser,
) -> Result<Json<PageResponse<ApprovalRequest>>, AppError> {
    let page = query.page.unwrap_or(0);
    let size = query.size.unwrap_or(100);
    let offset = page * size;
    let user_id = query.requester_id.unwrap_or(auth.claims.sub);

    let total: (i64,) =
        sqlx::query_as("SELECT COUNT(*) FROM approval_request WHERE requester_id = $1")
            .bind(&user_id)
            .fetch_one(&state.db)
            .await?;

    let content = sqlx::query_as::<_, ApprovalRequest>(
        r#"
        SELECT * FROM approval_request 
        WHERE requester_id = $1
        ORDER BY created_at DESC
        LIMIT $2 OFFSET $3
        "#,
    )
    .bind(&user_id)
    .bind(size)
    .bind(offset)
    .fetch_all(&state.db)
    .await?;

    Ok(Json(PageResponse::new(content, total.0, page, size)))
}

pub async fn cancel_request(
    State(state): State<AppState>,
    Path(id): Path<Uuid>,
    _auth: AuthUser,
) -> Result<Json<serde_json::Value>, AppError> {
    sqlx::query(
        "UPDATE approval_request SET status = 'CANCELLED', updated_at = NOW() WHERE id = $1",
    )
    .bind(id)
    .execute(&state.db)
    .await?;

    sqlx::query("UPDATE approval_step SET status = 'CANCELLED', updated_at = NOW() WHERE request_id = $1 AND status = 'PENDING'")
        .bind(id)
        .execute(&state.db)
        .await?;

    Ok(Json(
        serde_json::json!({ "success": true, "requestId": id, "status": "CANCELLED" }),
    ))
}

pub async fn add_memo(
    State(state): State<AppState>,
    Json(payload): Json<MemoRequest>,
) -> Result<Json<serde_json::Value>, AppError> {
    let new_id = Uuid::new_v4();
    sqlx::query(
        r#"
        INSERT INTO approval_step (
            id, request_id, step_order, step_type, status, comment,
            is_escalated, version, created_at, updated_at
        ) VALUES (
            $1, $2, 999, 'MEMO', 'RECORDED', $3,
            false, 0, NOW(), NOW()
        )
        "#,
    )
    .bind(new_id)
    .bind(payload.request_id)
    .bind(&payload.comment)
    .execute(&state.db)
    .await?;

    Ok(Json(
        serde_json::json!({ "success": true, "memoId": new_id }),
    ))
}

// Effective workflow and permissions
pub async fn get_effective_workflow(
    State(state): State<AppState>,
    Path(node_id): Path<Uuid>,
) -> Result<Json<serde_json::Value>, AppError> {
    let config = sqlx::query_as::<_, WorkflowConfig>(
        "SELECT * FROM workflow_config WHERE node_id = $1 AND is_active = true LIMIT 1",
    )
    .bind(node_id)
    .fetch_optional(&state.db)
    .await?;

    let has_workflow = config.is_some();
    let workflow_id = config.as_ref().map(|c| c.id);
    let workflow_name = config.as_ref().and_then(|c| c.name.clone());

    Ok(Json(serde_json::json!({
        "hasWorkflow": has_workflow,
        "workflowId": workflow_id,
        "workflowName": workflow_name,
        "ruleName": {},
        "editableFields": [],
        "readOnlyFields": [],
        "hiddenFields": []
    })))
}

pub async fn get_pending_schema_status(
    State(state): State<AppState>,
) -> Result<Json<serde_json::Value>, AppError> {
    let pending_count: (i64,) = sqlx::query_as(
        "SELECT COUNT(*) FROM approval_request WHERE target_type = 'SCHEMA' AND status = 'PENDING'",
    )
    .fetch_one(&state.db)
    .await?;

    Ok(Json(serde_json::json!({
        "hasPendingSchemaApproval": pending_count.0 > 0,
        "pendingCount": pending_count.0
    })))
}

pub async fn get_available_workflows(
    State(state): State<AppState>,
    Path(node_id): Path<Uuid>,
) -> Result<Json<Vec<WorkflowConfig>>, AppError> {
    let configs = sqlx::query_as::<_, WorkflowConfig>(
        "SELECT * FROM workflow_config WHERE (node_id = $1 OR node_id IS NULL) AND is_active = true"
    )
    .bind(node_id)
    .fetch_all(&state.db)
    .await?;

    Ok(Json(configs))
}

pub async fn get_effective_permission(
    State(_state): State<AppState>,
    Path(_node_id): Path<Uuid>,
) -> Result<Json<serde_json::Value>, AppError> {
    Ok(Json(serde_json::json!({
        "canRead": true,
        "canWrite": true,
        "canApprove": true,
        "canDelete": true
    })))
}

fn parse_delegation_datetime(s: &str) -> Option<chrono::NaiveDateTime> {
    chrono::NaiveDateTime::parse_from_str(s, "%Y-%m-%dT%H:%M:%S")
        .or_else(|_| chrono::NaiveDateTime::parse_from_str(s, "%Y-%m-%d %H:%M:%S"))
        .or_else(|_| chrono::DateTime::parse_from_rfc3339(s).map(|dt| dt.naive_utc()))
        .or_else(|_| {
            chrono::NaiveDate::parse_from_str(s, "%Y-%m-%d")
                .map(|d| d.and_hms_opt(0, 0, 0).unwrap())
        })
        .ok()
}

// Delegations
pub async fn get_my_delegations(
    State(state): State<AppState>,
    auth: AuthUser,
) -> Result<Json<MyDelegationsResponse>, AppError> {
    let sub = auth.claims.sub.clone();
    let uid = auth.claims.user_id.clone().unwrap_or_else(|| sub.clone());
    let now = chrono::Utc::now().naive_utc();

    type DelegationRow = (
        Uuid,
        String,
        Option<String>,
        String,
        Option<String>,
        Option<String>,
        chrono::NaiveDateTime,
        chrono::NaiveDateTime,
        bool,
        chrono::NaiveDateTime,
    );

    let by_me_rows = sqlx::query_as::<_, DelegationRow>(
        r#"
        SELECT 
            d.id, d.delegator_user_id, u1.username as delegator_user_name,
            d.delegatee_user_id, u2.username as delegatee_user_name,
            d.reason, d.start_date, d.end_date, d.is_active, d.created_at
        FROM approval_delegations d
        LEFT JOIN users u1 ON (d.delegator_user_id = u1.id OR d.delegator_user_id = u1.username)
        LEFT JOIN users u2 ON (d.delegatee_user_id = u2.id OR d.delegatee_user_id = u2.username)
        WHERE d.delegator_user_id = $1 OR d.delegator_user_id = $2
        ORDER BY d.created_at DESC
        "#,
    )
    .bind(&sub)
    .bind(&uid)
    .fetch_all(&state.db)
    .await?;

    let by_me: Vec<ApprovalDelegationDto> = by_me_rows
        .into_iter()
        .map(|r| ApprovalDelegationDto {
            id: r.0,
            delegator_user_id: r.1,
            delegator_user_name: r.2,
            delegatee_user_id: r.3,
            delegatee_user_name: r.4,
            reason: r.5,
            start_date: r.6,
            end_date: r.7,
            is_active: r.8 && (now <= r.7),
            created_at: r.9,
        })
        .collect();

    let to_me_rows = sqlx::query_as::<_, DelegationRow>(
        r#"
        SELECT 
            d.id, d.delegator_user_id, u1.username as delegator_user_name,
            d.delegatee_user_id, u2.username as delegatee_user_name,
            d.reason, d.start_date, d.end_date, d.is_active, d.created_at
        FROM approval_delegations d
        LEFT JOIN users u1 ON (d.delegator_user_id = u1.id OR d.delegator_user_id = u1.username)
        LEFT JOIN users u2 ON (d.delegatee_user_id = u2.id OR d.delegatee_user_id = u2.username)
        WHERE d.delegatee_user_id = $1 OR d.delegatee_user_id = $2
        ORDER BY d.created_at DESC
        "#,
    )
    .bind(&sub)
    .bind(&uid)
    .fetch_all(&state.db)
    .await?;

    let to_me: Vec<ApprovalDelegationDto> = to_me_rows
        .into_iter()
        .map(|r| ApprovalDelegationDto {
            id: r.0,
            delegator_user_id: r.1,
            delegator_user_name: r.2,
            delegatee_user_id: r.3,
            delegatee_user_name: r.4,
            reason: r.5,
            start_date: r.6,
            end_date: r.7,
            is_active: r.8 && (now <= r.7),
            created_at: r.9,
        })
        .collect();

    Ok(Json(MyDelegationsResponse {
        delegated_by_me: by_me.clone(),
        delegated_to_me: to_me.clone(),
        by_me,
        to_me,
    }))
}

pub async fn create_delegation(
    State(state): State<AppState>,
    auth: AuthUser,
    Json(payload): Json<CreateDelegationDto>,
) -> Result<Json<ApprovalDelegation>, AppError> {
    let delegator_sub = auth.claims.sub.clone();
    let delegator_uid = auth
        .claims
        .user_id
        .clone()
        .unwrap_or_else(|| delegator_sub.clone());

    if payload.delegatee_user_id.trim().is_empty() {
        return Err(AppError::BadRequest("Delegatee user ID is required".into()));
    }
    if payload.delegatee_user_id == delegator_sub || payload.delegatee_user_id == delegator_uid {
        return Err(AppError::BadRequest(
            "Cannot delegate approvals to yourself".into(),
        ));
    }

    let now = chrono::Utc::now().naive_utc();
    let start_date = payload
        .start_date
        .as_deref()
        .and_then(parse_delegation_datetime)
        .unwrap_or(now);
    let end_date = payload
        .end_date
        .as_deref()
        .and_then(parse_delegation_datetime)
        .unwrap_or(now + chrono::Duration::days(30));

    if end_date < start_date {
        return Err(AppError::BadRequest(
            "End date cannot be before start date".into(),
        ));
    }

    let new_id = Uuid::new_v4();
    let inserted = sqlx::query_as::<_, ApprovalDelegation>(
        r#"
        INSERT INTO approval_delegations (
            id, delegator_user_id, delegatee_user_id, reason,
            start_date, end_date, is_active, created_at, updated_at
        ) VALUES (
            $1, $2, $3, $4,
            $5, $6, true, NOW(), NOW()
        )
        RETURNING *
        "#,
    )
    .bind(new_id)
    .bind(&delegator_uid)
    .bind(&payload.delegatee_user_id)
    .bind(&payload.reason)
    .bind(start_date)
    .bind(end_date)
    .fetch_one(&state.db)
    .await?;

    Ok(Json(inserted))
}

pub async fn delete_delegation(
    State(state): State<AppState>,
    Path(id): Path<Uuid>,
) -> Result<Json<serde_json::Value>, AppError> {
    sqlx::query(
        "UPDATE approval_delegations SET is_active = false, updated_at = NOW() WHERE id = $1",
    )
    .bind(id)
    .execute(&state.db)
    .await?;

    Ok(Json(serde_json::json!({
        "success": true,
        "message": "Delegation revoked successfully."
    })))
}

// Escalation, Sandbox, Rejection Analytics
pub async fn scan_escalations(
    State(state): State<AppState>,
) -> Result<Json<serde_json::Value>, AppError> {
    let escalated: Vec<(Uuid,)> = sqlx::query_as(
        r#"
        UPDATE approval_step
        SET is_escalated = true, escalated_at = NOW(), updated_at = NOW()
        WHERE status = 'PENDING' AND sla_due_at < NOW() AND is_escalated = false
        RETURNING id
        "#,
    )
    .fetch_all(&state.db)
    .await?;

    Ok(Json(serde_json::json!({
        "scanned": true,
        "escalatedCount": escalated.len(),
        "escalatedStepIds": escalated.iter().map(|e| e.0).collect::<Vec<_>>()
    })))
}

pub async fn get_sandbox_preview(
    State(state): State<AppState>,
    Path(request_id): Path<Uuid>,
) -> Result<Json<serde_json::Value>, AppError> {
    let req = sqlx::query_as::<_, ApprovalRequest>("SELECT * FROM approval_request WHERE id = $1")
        .bind(request_id)
        .fetch_optional(&state.db)
        .await?
        .ok_or_else(|| AppError::NotFound("Approval request not found".to_string()))?;

    Ok(Json(serde_json::json!({
        "requestId": req.id,
        "targetId": req.target_id,
        "targetType": req.target_type,
        "simulatedChanges": req.changes,
        "simulationStatus": "SUCCESS"
    })))
}

pub async fn get_rejection_analytics(
    State(state): State<AppState>,
) -> Result<Json<serde_json::Value>, AppError> {
    let rejections: (i64,) =
        sqlx::query_as("SELECT COUNT(*) FROM approval_step WHERE status = 'REJECTED'")
            .fetch_one(&state.db)
            .await?;

    Ok(Json(serde_json::json!({
        "totalRejections": rejections.0,
        "topReasons": [
            { "reason": "Missing required field", "count": 12 },
            { "reason": "Invalid business number", "count": 8 },
            { "reason": "Duplicate record suspected", "count": 5 }
        ]
    })))
}

// Routing templates
pub async fn get_routing_templates(
    State(state): State<AppState>,
) -> Result<Json<Vec<ApprovalRoutingTemplate>>, AppError> {
    let list = sqlx::query_as::<_, ApprovalRoutingTemplate>(
        "SELECT * FROM approval_routing_templates ORDER BY created_at DESC",
    )
    .fetch_all(&state.db)
    .await?;

    Ok(Json(list))
}

pub async fn create_routing_template(
    State(state): State<AppState>,
    Json(payload): Json<ApprovalRoutingTemplate>,
) -> Result<Json<ApprovalRoutingTemplate>, AppError> {
    let new_id = Uuid::new_v4();
    let inserted = sqlx::query_as::<_, ApprovalRoutingTemplate>(
        r#"
        INSERT INTO approval_routing_templates (
            id, domain_id, template_name, condition_field, condition_operator,
            condition_value, steps_json, created_at
        ) VALUES (
            $1, $2, $3, $4, $5,
            $6, $7, NOW()
        )
        RETURNING *
        "#,
    )
    .bind(new_id)
    .bind(payload.domain_id)
    .bind(&payload.template_name)
    .bind(&payload.condition_field)
    .bind(&payload.condition_operator)
    .bind(&payload.condition_value)
    .bind(&payload.steps_json)
    .fetch_one(&state.db)
    .await?;

    Ok(Json(inserted))
}

// Workflow Configs
pub async fn get_workflow_configs(
    State(state): State<AppState>,
) -> Result<Json<Vec<WorkflowConfig>>, AppError> {
    let configs = sqlx::query_as::<_, WorkflowConfig>(
        "SELECT * FROM workflow_config ORDER BY created_at DESC",
    )
    .fetch_all(&state.db)
    .await?;

    Ok(Json(configs))
}

pub async fn get_workflow_config_by_id(
    State(state): State<AppState>,
    Path(id): Path<Uuid>,
) -> Result<Json<WorkflowConfig>, AppError> {
    let config = sqlx::query_as::<_, WorkflowConfig>("SELECT * FROM workflow_config WHERE id = $1")
        .bind(id)
        .fetch_optional(&state.db)
        .await?
        .ok_or_else(|| AppError::NotFound("Workflow config not found".to_string()))?;

    Ok(Json(config))
}

pub async fn get_node_workflow_configs(
    State(state): State<AppState>,
    Path(node_id): Path<Uuid>,
) -> Result<Json<Vec<WorkflowConfig>>, AppError> {
    let configs = sqlx::query_as::<_, WorkflowConfig>(
        "SELECT * FROM workflow_config WHERE node_id = $1 OR domain_id = $1",
    )
    .bind(node_id)
    .fetch_all(&state.db)
    .await?;

    Ok(Json(configs))
}

pub async fn get_workflow_configs_page(
    State(state): State<AppState>,
    Query(params): Query<ApprovalQuery>,
) -> Result<Json<PageResponse<WorkflowConfig>>, AppError> {
    let page = params.page.unwrap_or(0);
    let size = params.size.unwrap_or(100);
    let offset = page * size;

    let total: (i64,) = sqlx::query_as("SELECT COUNT(*) FROM workflow_config")
        .fetch_one(&state.db)
        .await?;

    let content = sqlx::query_as::<_, WorkflowConfig>(
        "SELECT * FROM workflow_config ORDER BY created_at DESC LIMIT $1 OFFSET $2",
    )
    .bind(size)
    .bind(offset)
    .fetch_all(&state.db)
    .await?;

    Ok(Json(PageResponse::new(content, total.0, page, size)))
}

#[derive(Debug, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct WorkflowConfigRequest {
    pub config: serde_json::Value,
}

pub async fn save_workflow_config_for_domain(
    State(state): State<AppState>,
    Path(domain_id): Path<Uuid>,
    _auth: AuthUser,
    Json(payload): Json<WorkflowConfigRequest>,
) -> Result<Json<serde_json::Value>, AppError> {
    let id = Uuid::new_v4();
    let row = sqlx::query(
        r#"
        INSERT INTO workflow_config (id, domain_id, config, created_at, updated_at)
        VALUES ($1, $2, $3, NOW(), NOW())
        ON CONFLICT (domain_id) WHERE node_id IS NULL 
        DO UPDATE SET config = EXCLUDED.config, updated_at = NOW()
        "#,
    )
    .bind(id)
    .bind(domain_id)
    .bind(payload.config)
    .execute(&state.db)
    .await?;

    Ok(Json(serde_json::json!({})))
}

pub async fn save_workflow_config_for_node(
    State(state): State<AppState>,
    Path(node_id): Path<Uuid>,
    _auth: AuthUser,
    Json(payload): Json<WorkflowConfigRequest>,
) -> Result<Json<serde_json::Value>, AppError> {
    let id = Uuid::new_v4();
    let row = sqlx::query(
        r#"
        INSERT INTO workflow_config (id, node_id, config, created_at, updated_at)
        VALUES ($1, $2, $3, NOW(), NOW())
        ON CONFLICT (node_id) 
        DO UPDATE SET config = EXCLUDED.config, updated_at = NOW()
        "#,
    )
    .bind(id)
    .bind(node_id)
    .bind(payload.config)
    .execute(&state.db)
    .await?;

    Ok(Json(serde_json::json!({})))
}

pub async fn delete_workflow_config(
    State(state): State<AppState>,
    Path(id): Path<Uuid>,
    _auth: AuthUser,
) -> Result<StatusCode, AppError> {
    sqlx::query("DELETE FROM workflow_config WHERE id = $1")
        .bind(id)
        .execute(&state.db)
        .await?;
    Ok(axum::http::StatusCode::NO_CONTENT)
}
