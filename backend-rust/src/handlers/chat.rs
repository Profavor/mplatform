use crate::error::AppError;
use crate::error::AppResult;
use crate::middleware::auth::AuthUser;
use crate::models::chat::*;
use crate::state::AppState;
use axum::extract::Path;
use axum::extract::State;
use axum::http::StatusCode;
use axum::{response::IntoResponse, Json};
use serde::Deserialize;
use uuid::Uuid;

pub async fn get_rooms(
    State(state): State<AppState>,
    auth: AuthUser,
) -> AppResult<impl IntoResponse> {
    let rooms = state.chat_service.get_user_rooms(&auth.user_id).await?;
    Ok(Json(rooms))
}

pub async fn create_room(
    State(state): State<AppState>,
    auth: AuthUser,
    Json(req): Json<CreateRoomRequest>,
) -> AppResult<impl IntoResponse> {
    let name = req.room_name.unwrap_or_else(|| "대화방".to_string());
    let room = state
        .chat_service
        .create_room(&name, req.is_group, &auth.user_id, &req.member_user_ids)
        .await?;
    Ok(Json(room))
}

pub async fn get_room_members(
    State(state): State<AppState>,
    Path(room_id): Path<Uuid>,
    _auth: AuthUser,
) -> AppResult<impl IntoResponse> {
    let members = state.chat_service.get_room_members(room_id).await?;
    Ok(Json(members))
}

pub async fn get_room_messages(
    State(state): State<AppState>,
    Path(room_id): Path<Uuid>,
    auth: AuthUser,
) -> AppResult<impl IntoResponse> {
    let messages = state
        .chat_service
        .get_room_messages(room_id, &auth.user_id)
        .await?;
    Ok(Json(messages))
}

pub async fn send_room_message(
    State(state): State<AppState>,
    Path(room_id): Path<Uuid>,
    auth: AuthUser,
    Json(req): Json<SendMessageRequest>,
) -> AppResult<impl IntoResponse> {
    let user_content = req.content.clone().unwrap_or_default();
    let msg = state
        .chat_service
        .send_message(room_id, &auth.user_id, Some(&auth.username), req)
        .await?;

    // Check if Stock Bot should respond
    let stock_bot = state.stock_bot_service.clone();
    let chat_service = state.chat_service.clone();
    let user_id = auth.user_id.clone();

    if stock_bot.should_bot_respond(room_id, &user_id, &user_content).await {
        tokio::spawn(async move {
            if let Err(e) = stock_bot.respond_to_message(room_id, &user_content, &chat_service).await {
                tracing::error!("StockBot failed to respond: {:?}", e);
            }
        });
    }

    Ok(Json(msg))
}

pub async fn mark_room_as_read(
    State(state): State<AppState>,
    Path(room_id): Path<Uuid>,
    auth: AuthUser,
) -> AppResult<impl IntoResponse> {
    state
        .chat_service
        .mark_room_as_read(room_id, &auth.user_id)
        .await?;
    Ok(Json(serde_json::json!({ "success": true })))
}

pub async fn leave_room(
    State(state): State<AppState>,
    Path(room_id): Path<Uuid>,
    auth: AuthUser,
) -> AppResult<impl IntoResponse> {
    state
        .chat_service
        .leave_room(room_id, &auth.user_id)
        .await?;
    Ok(Json(serde_json::json!({ "success": true })))
}

pub async fn delete_room(
    State(state): State<AppState>,
    Path(room_id): Path<Uuid>,
    _auth: AuthUser,
) -> AppResult<impl IntoResponse> {
    state.chat_service.delete_room(room_id).await?;
    Ok(Json(serde_json::json!({ "success": true })))
}

pub async fn get_total_unread_count(
    State(state): State<AppState>,
    auth: AuthUser,
) -> AppResult<impl IntoResponse> {
    let count = state
        .chat_service
        .get_total_unread_count(&auth.user_id)
        .await?;
    Ok(Json(count))
}

pub async fn delete_message(
    State(state): State<AppState>,
    Path(message_id): Path<Uuid>,
    _auth: AuthUser,
) -> AppResult<impl IntoResponse> {
    state.chat_service.delete_message(message_id).await?;
    Ok(Json(serde_json::json!({ "success": true })))
}

pub async fn get_chat_presence(_auth: AuthUser) -> AppResult<impl IntoResponse> {
    let presence = serde_json::json!({
        "status": "ONLINE",
        "activeUsers": 1,
        "users": ["superadmin"]
    });
    Ok(Json(presence))
}

#[derive(serde::Deserialize)]
pub struct ChatTranslateRequest {
    pub text: String,
    #[serde(alias = "targetLang")]
    pub target_lang: Option<String>,
}

pub async fn translate_chat_message(
    _auth: AuthUser,
    Json(req): Json<ChatTranslateRequest>,
) -> AppResult<impl IntoResponse> {
    let translated = format!("[번역: {}]", req.text);
    Ok(Json(
        serde_json::json!({ "original": req.text, "translated": translated }),
    ))
}

pub async fn get_chat_users(
    State(state): State<AppState>,
    _auth: AuthUser,
) -> AppResult<impl IntoResponse> {
    let users = crate::services::user_service::UserService::get_all_users(&state.db).await?;
    let mut list: Vec<serde_json::Value> = users
        .into_iter()
        .map(|u| {
            serde_json::json!({
                "userId": u.id,
                "username": u.username,
                "email": u.email,
                "role": u.role,
                "online": true
            })
        })
        .collect();

    if !list.iter().any(|u| u.get("userId").and_then(|v| v.as_str()) == Some(crate::services::stock_bot_service::StockBotService::BOT_USER_ID)) {
        list.push(serde_json::json!({
            "userId": crate::services::stock_bot_service::StockBotService::BOT_USER_ID,
            "username": "AI 주식 비서",
            "email": "stockbot@mplatform.ai",
            "role": "BOT",
            "online": true
        }));
    }

    Ok(Json(list))
}

#[derive(Debug, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct InviteMembersRequest {
    #[serde(alias = "user_ids")]
    pub user_ids: Vec<String>,
    pub past_message_hours: Option<i64>,
}

pub async fn invite_members(
    State(state): State<AppState>,
    Path(room_id): Path<Uuid>,
    _auth: AuthUser,
    Json(payload): Json<InviteMembersRequest>,
) -> Result<StatusCode, AppError> {
    let hours = payload.past_message_hours.unwrap_or(0).clamp(0, 48);
    let joined_at = chrono::Utc::now() - chrono::Duration::hours(hours);
    let now = chrono::Utc::now().naive_utc();

    for raw_uid in payload.user_ids {
        let clean_uid = raw_uid.trim().to_string();
        if clean_uid.is_empty() {
            continue;
        }

        let canon_id: Option<String> =
            sqlx::query_scalar("SELECT id FROM users WHERE id = $1 OR username = $1")
                .bind(&clean_uid)
                .fetch_optional(&state.db)
                .await
                .unwrap_or(None);

        let final_uid = canon_id.clone().unwrap_or_else(|| clean_uid.clone());

        let exists: bool = sqlx::query_scalar(
            "SELECT EXISTS(SELECT 1 FROM chat_message_room_member WHERE room_id = $1 AND (user_id = $2 OR user_id = $3))"
        )
        .bind(room_id)
        .bind(&final_uid)
        .bind(&clean_uid)
        .fetch_one(&state.db)
        .await
        .unwrap_or(false);

        if !exists {
            let member_id = Uuid::new_v4();
            sqlx::query(
                r#"
                INSERT INTO chat_message_room_member (id, room_id, user_id, joined_at, last_read_at)
                VALUES ($1, $2, $3, $4, $5)
                "#,
            )
            .bind(member_id)
            .bind(room_id)
            .bind(&final_uid)
            .bind(joined_at.naive_utc())
            .bind(now)
            .execute(&state.db)
            .await?;

            let _ = state
                .chat_service
                .send_message(
                    room_id,
                    "SYSTEM",
                    Some("SYSTEM"),
                    SendMessageRequest {
                        room_id: Some(room_id),
                        message_type: Some("JOIN".to_string()),
                        content: Some(final_uid.clone()),
                        file_url: None,
                        file_name: None,
                        file_size: None,
                    },
                )
                .await;
        }
    }
    Ok(axum::http::StatusCode::OK)
}

pub async fn kick_member(
    State(state): State<AppState>,
    Path((room_id, target_user_id)): Path<(Uuid, String)>,
    _auth: AuthUser,
) -> Result<StatusCode, AppError> {
    let canon_id: Option<String> =
        sqlx::query_scalar("SELECT id FROM users WHERE id = $1 OR username = $1")
            .bind(&target_user_id)
            .fetch_optional(&state.db)
            .await
            .unwrap_or(None);

    let final_id = canon_id.unwrap_or_else(|| target_user_id.clone());

    sqlx::query(
        "DELETE FROM chat_message_room_member WHERE room_id = $1 AND (user_id = $2 OR user_id = $3)"
    )
    .bind(room_id)
    .bind(&target_user_id)
    .bind(&final_id)
    .execute(&state.db)
    .await?;
    Ok(axum::http::StatusCode::NO_CONTENT)
}

#[derive(Debug, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct DelegateCreatorRequest {
    pub new_creator_id: String,
}

pub async fn delegate_creator(
    State(state): State<AppState>,
    Path(room_id): Path<Uuid>,
    _auth: AuthUser,
    Json(payload): Json<DelegateCreatorRequest>,
) -> Result<Json<serde_json::Value>, AppError> {
    let row = sqlx::query(
        r#"
        UPDATE chat_message_room
        SET created_by = $1, updated_at = NOW()
        WHERE id = $2
        "#,
    )
    .bind(payload.new_creator_id)
    .bind(room_id)
    .execute(&state.db)
    .await?;

    Ok(Json(serde_json::json!({})))
}

#[derive(Debug, Deserialize)]
pub struct StockBotQueryRequest {
    pub message: String,
}

pub async fn query_stock_bot(
    State(state): State<AppState>,
    _auth: AuthUser,
    Json(req): Json<StockBotQueryRequest>,
) -> AppResult<impl IntoResponse> {
    let reply = state.stock_bot_service.process_query(&req.message).await;
    Ok(Json(serde_json::json!({
        "reply": reply
    })))
}

pub async fn get_or_create_stock_bot_room(
    State(state): State<AppState>,
    auth: AuthUser,
) -> AppResult<impl IntoResponse> {
    let room = state
        .stock_bot_service
        .get_or_create_stock_bot_room(&auth.user_id)
        .await?;
    Ok(Json(room))
}
