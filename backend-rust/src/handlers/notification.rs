use crate::error::AppError;
use crate::middleware::auth::AuthUser;
use crate::models::notification::NotificationResponse;
use crate::services::notification_service::NotificationService;
use crate::state::AppState;
use axum::extract::Path;
use axum::extract::State;
use axum::http::StatusCode;
use axum::Json;
use serde::Deserialize;
use uuid::Uuid;

pub async fn get_my_notifications(
    State(state): State<AppState>,
    auth: AuthUser,
) -> Result<Json<Vec<NotificationResponse>>, AppError> {
    let notifs = NotificationService::get_my_notifications(&state.db, &auth.claims.sub).await?;
    Ok(Json(notifs))
}

pub async fn get_unread_count(
    State(state): State<AppState>,
    auth: AuthUser,
) -> Result<Json<serde_json::Value>, AppError> {
    let count = NotificationService::get_unread_count(&state.db, &auth.claims.sub).await?;
    Ok(Json(serde_json::json!({ "unreadCount": count })))
}

pub async fn mark_as_read(
    State(state): State<AppState>,
    Path(id): Path<Uuid>,
    auth: AuthUser,
) -> Result<Json<serde_json::Value>, AppError> {
    let success = NotificationService::mark_as_read(&state.db, id, &auth.claims.sub).await?;
    Ok(Json(serde_json::json!({ "success": success })))
}

pub async fn mark_all_read(
    State(state): State<AppState>,
    auth: AuthUser,
) -> Result<Json<serde_json::Value>, AppError> {
    let _ = sqlx::query("UPDATE notification SET is_read = true WHERE user_id = $1")
        .bind(&auth.username)
        .execute(&state.db)
        .await;
    Ok(Json(serde_json::json!({ "success": true })))
}

pub async fn clear_all_notifications(
    State(state): State<AppState>,
    auth: AuthUser,
) -> Result<Json<serde_json::Value>, AppError> {
    let _ = sqlx::query("DELETE FROM notification WHERE user_id = $1")
        .bind(&auth.username)
        .execute(&state.db)
        .await;
    Ok(Json(serde_json::json!({ "success": true })))
}

#[derive(Debug, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct CreateNotificationRequest {
    pub user_id: String,
    pub title: String,
    pub message: String,
    pub link: Option<String>,
    pub notification_type: Option<String>,
}

pub async fn create_notification(
    State(state): State<AppState>,
    _auth: AuthUser,
    Json(payload): Json<CreateNotificationRequest>,
) -> Result<Json<serde_json::Value>, AppError> {
    let id = Uuid::new_v4();
    let row = sqlx::query(
        r#"
        INSERT INTO notification (id, user_id, title, message, link, notification_type, is_read, created_at)
        VALUES ($1, $2, $3, $4, $5, $6, false, NOW())
        "#,
    )
    .bind(id)
    .bind(payload.user_id)
    .bind(payload.title)
    .bind(payload.message)
    .bind(payload.link)
    .bind(payload.notification_type)
    .execute(&state.db)
    .await?;

    Ok(Json(serde_json::json!({})))
}

pub async fn delete_notification(
    State(state): State<AppState>,
    Path(id): Path<Uuid>,
    _auth: AuthUser,
) -> Result<StatusCode, AppError> {
    sqlx::query("DELETE FROM notification WHERE id = $1")
        .bind(id)
        .execute(&state.db)
        .await?;
    Ok(axum::http::StatusCode::NO_CONTENT)
}
