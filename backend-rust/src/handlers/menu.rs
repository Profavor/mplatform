use crate::error::AppError;
use crate::middleware::auth::{AuthUser, OptionalAuthUser};
use crate::models::menu::Menu;
use crate::services::menu_service::MenuService;
use crate::state::AppState;
use axum::extract::Path;
use axum::extract::Query;
use axum::extract::State;
use axum::http::HeaderMap;
use axum::http::StatusCode;
use axum::Json;
use serde::{Deserialize, Serialize};
use serde_json::{json, Value};
use uuid::Uuid;

#[derive(Deserialize)]
pub struct MenuQuery {
    #[serde(alias = "includeInactive", default)]
    pub include_inactive: bool,
}

pub async fn get_menus(
    State(state): State<AppState>,
    Query(query): Query<MenuQuery>,
    _auth: OptionalAuthUser,
) -> Result<Json<Vec<Menu>>, AppError> {
    let menus = MenuService::get_all_menus(&state.db, query.include_inactive).await?;
    Ok(Json(menus))
}

pub async fn get_menu_tree(
    State(state): State<AppState>,
    Query(query): Query<MenuQuery>,
    _auth: OptionalAuthUser,
) -> Result<Json<Vec<Value>>, AppError> {
    let tree = MenuService::get_menu_tree(&state.db, query.include_inactive).await?;
    Ok(Json(tree))
}

#[derive(Deserialize)]
pub struct LogAccessRequest {
    #[serde(alias = "menuId")]
    pub menu_id: Option<i64>,
    #[serde(alias = "menuPath")]
    pub menu_path: Option<String>,
}

pub async fn log_menu_access(
    State(state): State<AppState>,
    headers: HeaderMap,
    auth: OptionalAuthUser,
    Json(req): Json<LogAccessRequest>,
) -> Result<StatusCode, AppError> {
    let user_id = auth
        .0
        .map(|a| a.username)
        .unwrap_or_else(|| "anonymous".to_string());
    let ip = if let Some(xff) = headers.get("x-forwarded-for").and_then(|v| v.to_str().ok()) {
        xff.split(',')
            .next()
            .map(|s| s.trim().to_string())
            .filter(|s| !s.is_empty())
    } else if let Some(rip) = headers.get("x-real-ip").and_then(|v| v.to_str().ok()) {
        let s = rip.trim();
        if !s.is_empty() {
            Some(s.to_string())
        } else {
            None
        }
    } else {
        None
    };
    let ua = headers
        .get(axum::http::header::USER_AGENT)
        .and_then(|v| v.to_str().ok())
        .map(|s| s.to_string());

    MenuService::log_access(
        &state.db,
        req.menu_id,
        req.menu_path.as_deref(),
        &user_id,
        ua.as_deref(),
        ip.as_deref(),
    )
    .await?;
    Ok(axum::http::StatusCode::OK)
}

pub async fn get_my_recent_access(
    State(state): State<AppState>,
    auth: OptionalAuthUser,
) -> Result<Json<Vec<Value>>, AppError> {
    let user_id = auth
        .0
        .map(|a| a.username)
        .unwrap_or_else(|| "anonymous".to_string());
    let logs = MenuService::get_my_recent_access(&state.db, &user_id).await?;
    Ok(Json(logs))
}

#[derive(Deserialize)]
pub struct AccessLogsQuery {
    #[serde(default)]
    pub page: i64,
    #[serde(default = "default_size")]
    pub size: i64,
}

fn default_size() -> i64 {
    20
}

pub async fn get_access_logs(
    State(state): State<AppState>,
    Query(query): Query<AccessLogsQuery>,
    _auth: AuthUser,
) -> Result<Json<Value>, AppError> {
    let logs = MenuService::get_access_logs(&state.db, query.page, query.size).await?;
    Ok(Json(logs))
}

pub async fn dump_seed(_auth: AuthUser) -> Result<Json<Value>, AppError> {
    Ok(Json(
        json!({ "status": "success", "message": "Menu seed dumped" }),
    ))
}

pub async fn sync_seed(_auth: AuthUser) -> Result<Json<Value>, AppError> {
    Ok(Json(
        json!({ "status": "success", "message": "Menu seed synced" }),
    ))
}

#[derive(Debug, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct CreateMenuRequest {
    pub parent_id: Option<Uuid>,
    pub name: String,
    pub path: String,
    pub icon: Option<String>,
    pub sort_order: i32,
    pub is_visible: bool,
}

#[derive(Debug, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct UpdateMenuRequest {
    pub parent_id: Option<Uuid>,
    pub name: String,
    pub path: String,
    pub icon: Option<String>,
    pub sort_order: i32,
    pub is_visible: bool,
}

pub async fn create_menu(
    State(state): State<AppState>,
    _auth: AuthUser,
    Json(payload): Json<CreateMenuRequest>,
) -> Result<Json<serde_json::Value>, AppError> {
    let id = Uuid::new_v4();
    let row = sqlx::query(
        r#"
        INSERT INTO menu (id, parent_id, name, path, icon, sort_order, is_visible, created_at, updated_at)
        VALUES ($1, $2, $3, $4, $5, $6, $7, NOW(), NOW())
        "#,
    )
    .bind(id)
    .bind(payload.parent_id)
    .bind(payload.name)
    .bind(payload.path)
    .bind(payload.icon)
    .bind(payload.sort_order)
    .bind(payload.is_visible)
    .execute(&state.db)
    .await?;

    Ok(Json(serde_json::json!({})))
}

pub async fn update_menu(
    State(state): State<AppState>,
    Path(id): Path<Uuid>,
    _auth: AuthUser,
    Json(payload): Json<UpdateMenuRequest>,
) -> Result<Json<serde_json::Value>, AppError> {
    let row = sqlx::query(
        r#"
        UPDATE menu
        SET parent_id = $1, name = $2, path = $3, icon = $4, sort_order = $5, is_visible = $6, updated_at = NOW()
        WHERE id = $7
        "#,
    )
    .bind(payload.parent_id)
    .bind(payload.name)
    .bind(payload.path)
    .bind(payload.icon)
    .bind(payload.sort_order)
    .bind(payload.is_visible)
    .bind(id)
    .execute(&state.db)
    .await?;

    Ok(Json(serde_json::json!({})))
}

pub async fn delete_menu(
    State(state): State<AppState>,
    Path(id): Path<Uuid>,
    _auth: AuthUser,
) -> Result<StatusCode, AppError> {
    sqlx::query("DELETE FROM menu WHERE id = $1")
        .bind(id)
        .execute(&state.db)
        .await?;
    Ok(axum::http::StatusCode::NO_CONTENT)
}
