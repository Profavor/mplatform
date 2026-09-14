use axum::http::StatusCode;
use axum::{
    extract::{Path, Query, State},
    response::IntoResponse,
    Json,
};
use chrono::NaiveDateTime;
use serde::{Deserialize, Serialize};
use uuid::Uuid;

use crate::{
    error::AppError,
    middleware::auth::AuthUser,
    models::{
        permission::{
            ColumnMaskingPolicy, DataScopePermission, DomainAccessRequest, DomainPermission,
            PermissionGroup, PermissionItem,
        },
        record::PageResponse,
    },
    state::AppState,
};

#[derive(Debug, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct PermQuery {
    pub page: Option<i64>,
    pub size: Option<i64>,
    pub search: Option<String>,
}

// -------------------------------------------------------------
// Permission Groups & Items
// -------------------------------------------------------------

#[derive(Debug, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct PermissionGroupWithItems {
    #[serde(flatten)]
    pub group: PermissionGroup,
    pub items: Vec<PermissionItem>,
}

pub async fn get_permission_groups(
    State(state): State<AppState>,
) -> Result<Json<Vec<PermissionGroupWithItems>>, AppError> {
    let groups = sqlx::query_as::<_, PermissionGroup>(
        "SELECT * FROM permission_group ORDER BY sort_order ASC, code ASC",
    )
    .fetch_all(&state.db)
    .await?;

    let items = sqlx::query_as::<_, PermissionItem>(
        "SELECT * FROM permission_item ORDER BY sort_order ASC, perm_value ASC",
    )
    .fetch_all(&state.db)
    .await?;

    let mut result = Vec::new();
    for g in groups {
        let group_items = items
            .iter()
            .filter(|i| i.group_id.as_deref() == Some(&g.id))
            .cloned()
            .collect();
        result.push(PermissionGroupWithItems {
            group: g,
            items: group_items,
        });
    }

    Ok(Json(result))
}

#[derive(Debug, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct CreatePermissionGroupRequest {
    pub id: Option<String>,
    pub code: String,
    pub title_ko: String,
    pub title_en: Option<String>,
    pub icon: Option<String>,
    pub color: Option<String>,
    pub chip_class: Option<String>,
    pub sort_order: Option<i32>,
}

pub async fn create_permission_group(
    State(state): State<AppState>,
    Json(payload): Json<CreatePermissionGroupRequest>,
) -> Result<Json<PermissionGroup>, AppError> {
    let group_id = payload
        .id
        .clone()
        .filter(|s| !s.trim().is_empty())
        .unwrap_or_else(|| payload.code.to_lowercase().trim().to_string());
    let inserted = sqlx::query_as::<_, PermissionGroup>(
        r#"
        INSERT INTO permission_group (id, code, title_ko, title_en, icon, color, chip_class, sort_order)
        VALUES ($1, $2, $3, $4, $5, $6, $7, $8)
        RETURNING *
        "#
    )
    .bind(&group_id)
    .bind(&payload.code)
    .bind(&payload.title_ko)
    .bind(&payload.title_en)
    .bind(payload.icon.as_deref().unwrap_or("⚙️"))
    .bind(payload.color.as_deref().unwrap_or("#3b82f6"))
    .bind(payload.chip_class.as_deref().unwrap_or(""))
    .bind(payload.sort_order.unwrap_or(0))
    .fetch_one(&state.db)
    .await?;

    Ok(Json(inserted))
}

#[derive(Debug, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct UpdatePermissionGroupRequest {
    pub title_ko: Option<String>,
    pub title_en: Option<String>,
    pub icon: Option<String>,
    pub color: Option<String>,
    pub chip_class: Option<String>,
    pub sort_order: Option<i32>,
}

pub async fn update_permission_group(
    State(state): State<AppState>,
    Path(id): Path<String>,
    Json(payload): Json<UpdatePermissionGroupRequest>,
) -> Result<Json<PermissionGroup>, AppError> {
    let existing =
        sqlx::query_as::<_, PermissionGroup>("SELECT * FROM permission_group WHERE id = $1")
            .bind(&id)
            .fetch_optional(&state.db)
            .await?
            .ok_or_else(|| AppError::NotFound(format!("Permission group not found: {}", id)))?;

    let title_ko = payload.title_ko.unwrap_or(existing.title_ko);
    let title_en = payload.title_en.or(existing.title_en);
    let icon = payload.icon.or(existing.icon);
    let color = payload.color.or(existing.color);
    let chip_class = payload.chip_class.or(existing.chip_class);
    let sort_order = payload.sort_order.or(existing.sort_order);

    let updated = sqlx::query_as::<_, PermissionGroup>(
        r#"
        UPDATE permission_group
        SET title_ko = $1, title_en = $2, icon = $3, color = $4, chip_class = $5, sort_order = $6
        WHERE id = $7
        RETURNING *
        "#,
    )
    .bind(&title_ko)
    .bind(&title_en)
    .bind(&icon)
    .bind(&color)
    .bind(&chip_class)
    .bind(sort_order)
    .bind(&id)
    .fetch_one(&state.db)
    .await?;

    Ok(Json(updated))
}

pub async fn delete_permission_group(
    State(state): State<AppState>,
    Path(id): Path<String>,
) -> Result<StatusCode, AppError> {
    sqlx::query("DELETE FROM permission_item WHERE group_id = $1")
        .bind(&id)
        .execute(&state.db)
        .await?;

    sqlx::query("DELETE FROM permission_group WHERE id = $1")
        .bind(&id)
        .execute(&state.db)
        .await?;

    Ok(axum::http::StatusCode::NO_CONTENT)
}

#[derive(Debug, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct CreatePermissionItemRequest {
    pub perm_value: String,
    pub label_ko: String,
    pub label_en: Option<String>,
    pub sort_order: Option<i32>,
}

pub async fn add_permission_item(
    State(state): State<AppState>,
    Path(group_id): Path<String>,
    Json(payload): Json<CreatePermissionItemRequest>,
) -> Result<Json<PermissionItem>, AppError> {
    let new_id = Uuid::new_v4();
    let inserted = sqlx::query_as::<_, PermissionItem>(
        r#"
        INSERT INTO permission_item (id, group_id, perm_value, label_ko, label_en, sort_order)
        VALUES ($1, $2, $3, $4, $5, $6)
        RETURNING *
        "#,
    )
    .bind(new_id)
    .bind(&group_id)
    .bind(&payload.perm_value)
    .bind(&payload.label_ko)
    .bind(&payload.label_en)
    .bind(payload.sort_order.unwrap_or(0))
    .fetch_one(&state.db)
    .await?;

    Ok(Json(inserted))
}

pub async fn delete_permission_item(
    State(state): State<AppState>,
    Path(item_id): Path<Uuid>,
) -> Result<StatusCode, AppError> {
    sqlx::query("DELETE FROM permission_item WHERE id = $1")
        .bind(item_id)
        .execute(&state.db)
        .await?;

    Ok(axum::http::StatusCode::NO_CONTENT)
}

// -------------------------------------------------------------
// Masking Policies
// -------------------------------------------------------------

pub async fn get_masking_policies(
    State(state): State<AppState>,
) -> Result<Json<Vec<ColumnMaskingPolicy>>, AppError> {
    let policies = sqlx::query_as::<_, ColumnMaskingPolicy>(
        "SELECT * FROM column_masking_policy WHERE is_active = true ORDER BY created_at DESC",
    )
    .fetch_all(&state.db)
    .await?;

    Ok(Json(policies))
}

pub async fn create_masking_policy(
    State(state): State<AppState>,
    auth: AuthUser,
    Json(payload): Json<ColumnMaskingPolicy>,
) -> Result<Json<ColumnMaskingPolicy>, AppError> {
    let new_id = Uuid::new_v4();
    let inserted = sqlx::query_as::<_, ColumnMaskingPolicy>(
        r#"
        INSERT INTO column_masking_policy (
            id, domain_id, field_key, target_type, target_id,
            masking_action, description, is_active, created_by, created_at, updated_at
        ) VALUES (
            $1, $2, $3, $4, $5,
            $6, $7, true, $8, NOW(), NOW()
        )
        RETURNING *
        "#,
    )
    .bind(new_id)
    .bind(payload.domain_id)
    .bind(&payload.field_key)
    .bind(&payload.target_type)
    .bind(&payload.target_id)
    .bind(&payload.masking_action)
    .bind(&payload.description)
    .bind(&auth.claims.sub)
    .fetch_one(&state.db)
    .await?;

    Ok(Json(inserted))
}

pub async fn delete_masking_policy(
    State(state): State<AppState>,
    Path(policy_id): Path<Uuid>,
) -> Result<StatusCode, AppError> {
    sqlx::query("DELETE FROM column_masking_policy WHERE id = $1")
        .bind(policy_id)
        .execute(&state.db)
        .await?;

    Ok(axum::http::StatusCode::NO_CONTENT)
}

// -------------------------------------------------------------
// Data Scope Permissions
// -------------------------------------------------------------

pub async fn get_user_scopes(
    State(state): State<AppState>,
    Path(user_id): Path<String>,
) -> Result<Json<Vec<DataScopePermission>>, AppError> {
    let scopes = sqlx::query_as::<_, DataScopePermission>(
        "SELECT * FROM data_scope_permission WHERE user_id = $1",
    )
    .bind(user_id)
    .fetch_all(&state.db)
    .await?;

    Ok(Json(scopes))
}

pub async fn add_user_scope(
    State(state): State<AppState>,
    Path(user_id): Path<String>,
    auth: AuthUser,
    Json(payload): Json<DataScopePermission>,
) -> Result<Json<DataScopePermission>, AppError> {
    let new_id = Uuid::new_v4();
    let inserted = sqlx::query_as::<_, DataScopePermission>(
        r#"
        INSERT INTO data_scope_permission (
            id, user_id, domain_id, node_id, permission_level,
            created_by, created_at, updated_at
        ) VALUES (
            $1, $2, $3, $4, $5,
            $6, NOW(), NOW()
        )
        RETURNING *
        "#,
    )
    .bind(new_id)
    .bind(&user_id)
    .bind(payload.domain_id)
    .bind(payload.node_id)
    .bind(&payload.permission_level)
    .bind(&auth.claims.sub)
    .fetch_one(&state.db)
    .await?;

    Ok(Json(inserted))
}

pub async fn delete_user_scope(
    State(state): State<AppState>,
    Path(scope_id): Path<Uuid>,
) -> Result<StatusCode, AppError> {
    sqlx::query("DELETE FROM data_scope_permission WHERE id = $1")
        .bind(scope_id)
        .execute(&state.db)
        .await?;

    Ok(axum::http::StatusCode::NO_CONTENT)
}

// -------------------------------------------------------------
// Domain Permissions & Access Requests
// -------------------------------------------------------------

pub async fn get_permissions_users(
    State(state): State<AppState>,
) -> Result<Json<Vec<serde_json::Value>>, AppError> {
    let users: Vec<(String, Option<String>, Option<String>)> =
        sqlx::query_as("SELECT id, username, email FROM users ORDER BY username ASC")
            .fetch_all(&state.db)
            .await?;

    let res = users
        .into_iter()
        .map(|(id, username, email)| {
            serde_json::json!({
                "id": id,
                "username": username.clone(),
                "name": username,
                "email": email
            })
        })
        .collect();

    Ok(Json(res))
}

pub async fn get_user_domains(
    State(state): State<AppState>,
    Path(user_id): Path<String>,
) -> Result<Json<Vec<DomainPermission>>, AppError> {
    let list =
        sqlx::query_as::<_, DomainPermission>("SELECT * FROM domain_permission WHERE user_id = $1")
            .bind(user_id)
            .fetch_all(&state.db)
            .await?;

    Ok(Json(list))
}

pub async fn get_available_domains(
    State(state): State<AppState>,
) -> Result<Json<Vec<serde_json::Value>>, AppError> {
    let domains: Vec<(Uuid, serde_json::Value)> =
        sqlx::query_as("SELECT id, name FROM domain ORDER BY sort_order ASC")
            .fetch_all(&state.db)
            .await?;

    let res = domains
        .into_iter()
        .map(|(id, name)| {
            serde_json::json!({
                "id": id,
                "code": id.to_string()[..8].to_string(),
                "name": name
            })
        })
        .collect();

    Ok(Json(res))
}

pub async fn assign_user_domain(
    State(state): State<AppState>,
    Path((user_id, domain_id)): Path<(String, Uuid)>,
) -> Result<Json<DomainPermission>, AppError> {
    let new_id = Uuid::new_v4();
    let inserted = sqlx::query_as::<_, DomainPermission>(
        r#"
        INSERT INTO domain_permission (id, user_id, domain_id, created_at)
        VALUES ($1, $2, $3, NOW())
        ON CONFLICT DO NOTHING
        RETURNING *
        "#,
    )
    .bind(new_id)
    .bind(&user_id)
    .bind(domain_id)
    .fetch_optional(&state.db)
    .await?
    .unwrap_or(DomainPermission {
        id: new_id,
        user_id,
        domain_id,
        created_at: Some(chrono::Utc::now().naive_utc()),
    });

    Ok(Json(inserted))
}

pub async fn revoke_user_domain(
    State(state): State<AppState>,
    Path((user_id, domain_id)): Path<(String, Uuid)>,
) -> Result<StatusCode, AppError> {
    sqlx::query("DELETE FROM domain_permission WHERE user_id = $1 AND domain_id = $2")
        .bind(&user_id)
        .bind(domain_id)
        .execute(&state.db)
        .await?;

    Ok(axum::http::StatusCode::NO_CONTENT)
}

// Access Requests
pub async fn get_pending_access_requests(
    State(state): State<AppState>,
) -> Result<Json<Vec<DomainAccessRequest>>, AppError> {
    let requests = sqlx::query_as::<_, DomainAccessRequest>(
        "SELECT * FROM domain_access_request WHERE status = 'PENDING' ORDER BY created_at DESC",
    )
    .fetch_all(&state.db)
    .await?;

    Ok(Json(requests))
}

pub async fn submit_access_request(
    State(state): State<AppState>,
    auth: AuthUser,
    Json(payload): Json<DomainAccessRequest>,
) -> Result<Json<DomainAccessRequest>, AppError> {
    let new_id = Uuid::new_v4();
    let inserted = sqlx::query_as::<_, DomainAccessRequest>(
        r#"
        INSERT INTO domain_access_request (id, user_id, domain_id, status, created_at, updated_at)
        VALUES ($1, $2, $3, 'PENDING', NOW(), NOW())
        RETURNING *
        "#,
    )
    .bind(new_id)
    .bind(&auth.claims.sub)
    .bind(payload.domain_id)
    .fetch_one(&state.db)
    .await?;

    Ok(Json(inserted))
}

pub async fn approve_access_request(
    State(state): State<AppState>,
    Path(request_id): Path<Uuid>,
) -> Result<Json<serde_json::Value>, AppError> {
    let req = sqlx::query_as::<_, DomainAccessRequest>(
        "UPDATE domain_access_request SET status = 'APPROVED', updated_at = NOW() WHERE id = $1 RETURNING *"
    )
    .bind(request_id)
    .fetch_optional(&state.db)
    .await?
    .ok_or_else(|| AppError::NotFound("Access request not found".to_string()))?;

    // Grant domain permission
    let new_pid = Uuid::new_v4();
    let _ = sqlx::query(
        "INSERT INTO domain_permission (id, user_id, domain_id, created_at) VALUES ($1, $2, $3, NOW())"
    )
    .bind(new_pid)
    .bind(&req.user_id)
    .bind(req.domain_id)
    .execute(&state.db)
    .await;

    Ok(Json(
        serde_json::json!({ "success": true, "status": "APPROVED" }),
    ))
}

pub async fn reject_access_request(
    State(state): State<AppState>,
    Path(request_id): Path<Uuid>,
) -> Result<Json<serde_json::Value>, AppError> {
    sqlx::query(
        "UPDATE domain_access_request SET status = 'REJECTED', updated_at = NOW() WHERE id = $1",
    )
    .bind(request_id)
    .execute(&state.db)
    .await?;

    Ok(Json(
        serde_json::json!({ "success": true, "status": "REJECTED" }),
    ))
}

pub async fn delete_access_request(
    State(state): State<AppState>,
    Path(request_id): Path<Uuid>,
) -> Result<StatusCode, AppError> {
    sqlx::query("DELETE FROM domain_access_request WHERE id = $1")
        .bind(request_id)
        .execute(&state.db)
        .await?;

    Ok(axum::http::StatusCode::NO_CONTENT)
}

pub async fn update_user_tenant_info(
    State(_state): State<AppState>,
    Path(_user_id): Path<String>,
    Json(payload): Json<serde_json::Value>,
) -> Result<Json<serde_json::Value>, AppError> {
    Ok(Json(payload))
}

// -------------------------------------------------------------
// Permission Audit Logs
// -------------------------------------------------------------

#[derive(Debug, Clone, sqlx::FromRow, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct PermissionAuditLog {
    pub id: Uuid,
    pub resource_type: String,
    pub action_type: String,
    pub changed_by: String,
    pub target_user_id: Option<String>,
    pub target_username: Option<String>,
    pub target_resource_id: Option<String>,
    pub target_resource_name: Option<String>,
    pub before_value: Option<String>,
    pub after_value: Option<String>,
    pub client_ip: Option<String>,
    pub changed_at: NaiveDateTime,
}

pub async fn get_permission_audit_logs(
    State(state): State<AppState>,
    Query(params): Query<PermQuery>,
) -> Result<Json<PageResponse<PermissionAuditLog>>, AppError> {
    let page = params.page.unwrap_or(0);
    let size = params.size.unwrap_or(50);
    let offset = page * size;

    let total: (i64,) = sqlx::query_as("SELECT COUNT(*) FROM permission_audit_log")
        .fetch_one(&state.db)
        .await?;

    let content = sqlx::query_as::<_, PermissionAuditLog>(
        "SELECT * FROM permission_audit_log ORDER BY changed_at DESC LIMIT $1 OFFSET $2",
    )
    .bind(size)
    .bind(offset)
    .fetch_all(&state.db)
    .await?;

    Ok(Json(PageResponse::new(content, total.0, page, size)))
}
