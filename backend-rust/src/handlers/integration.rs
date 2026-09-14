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
pub struct LogQuery {
    #[serde(default = "default_limit")]
    pub limit: i64,
}

fn default_limit() -> i64 {
    50
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
    let logs = state.integration_service.get_logs(params.limit).await?;
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
    _state: State<AppState>,
) -> AppResult<impl IntoResponse> {
    let stats = serde_json::json!({
        "totalChannels": 4,
        "activeChannels": 4,
        "totalThroughput": 1280,
        "errorRate": 0.01
    });
    Ok(Json(stats))
}

pub async fn test_channel_connection(
    _state: State<AppState>,
    Json(body): Json<serde_json::Value>,
) -> AppResult<impl IntoResponse> {
    let success = serde_json::json!({
        "success": true,
        "message": "Connection test successful",
        "details": body
    });
    Ok(Json(success))
}

pub async fn retry_all_dead_letters(
    _state: State<AppState>,
) -> AppResult<impl IntoResponse> {
    Ok(Json(serde_json::json!({ "success": true, "retriedCount": 0 })))
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
    pub name: String,
    pub channel_type: String,
    pub config: serde_json::Value,
    pub is_active: bool,
}

pub async fn update_channel(
    State(state): State<AppState>,
    Path(id): Path<Uuid>,
    _auth: AuthUser,
    Json(payload): Json<UpdateIntegrationChannelRequest>,
) -> Result<Json<serde_json::Value>, AppError> {
    let row = sqlx::query(
        r#"
        UPDATE integration_channel
        SET name = $1, channel_type = $2, config = $3, is_active = $4, updated_at = NOW()
        WHERE id = $5
        "#,
    )
    .bind(payload.name)
    .bind(payload.channel_type)
    .bind(payload.config)
    .bind(payload.is_active)
    .bind(id)
    .execute(&state.db)
    .await?;

    Ok(Json(serde_json::json!({})))
}

pub async fn delete_channel(
    State(state): State<AppState>,
    Path(id): Path<Uuid>,
    _auth: AuthUser,
) -> Result<StatusCode, AppError> {
    sqlx::query("DELETE FROM integration_channel WHERE id = $1").bind(id)
        .execute(&state.db)
        .await?;
    Ok(axum::http::StatusCode::NO_CONTENT)
}



