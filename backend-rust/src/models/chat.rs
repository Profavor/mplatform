use chrono::NaiveDateTime;
use serde::{Deserialize, Serialize};
use sqlx::FromRow;
use uuid::Uuid;

#[derive(Debug, Clone, FromRow, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct ChatMessageRoom {
    pub id: Uuid,
    pub name: String,
    pub is_group: bool,
    pub created_by: Option<String>,
    pub created_at: Option<NaiveDateTime>,
    pub last_message: Option<String>,
    pub last_message_at: Option<NaiveDateTime>,
    pub version: i64,
    #[sqlx(default)]
    pub unread_count: Option<i64>,
}

#[derive(Debug, Clone, FromRow, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct ChatMessage {
    pub id: Uuid,
    pub room_id: Uuid,
    pub sender_id: String,
    pub sender_name: Option<String>,
    pub message_type: String,
    pub content: Option<String>,
    pub file_url: Option<String>,
    pub file_name: Option<String>,
    pub file_size: Option<i64>,
    pub created_at: Option<NaiveDateTime>,
    #[sqlx(default)]
    pub unread_count: Option<i64>,
}

#[derive(Debug, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct CreateRoomRequest {
    pub room_name: Option<String>,
    #[serde(default)]
    pub is_group: bool,
    #[serde(default)]
    pub member_user_ids: Vec<String>,
}

#[derive(Debug, Deserialize, Serialize, Clone)]
#[serde(rename_all = "camelCase")]
pub struct SendMessageRequest {
    pub room_id: Option<Uuid>,
    pub message_type: Option<String>,
    pub content: Option<String>,
    pub file_url: Option<String>,
    pub file_name: Option<String>,
    pub file_size: Option<i64>,
}

#[derive(Debug, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct RoomMemberDto {
    pub user_id: String,
    pub username: Option<String>,
    pub role: Option<String>,
    pub joined_at: Option<NaiveDateTime>,
    pub last_read_at: Option<NaiveDateTime>,
}
