use chrono::NaiveDateTime;
use serde::{Deserialize, Serialize};
use sqlx::FromRow;
use uuid::Uuid;

#[derive(Debug, Clone, FromRow, Serialize, Deserialize)]
pub struct Notification {
    pub id: Uuid,
    pub user_id: String,
    #[sqlx(rename = "type")]
    pub notif_type: String,
    pub title: String,
    pub message: Option<String>,
    pub link_url: Option<String>,
    pub is_read: bool,
    pub created_at: NaiveDateTime,
}

#[derive(Debug, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct NotificationResponse {
    pub id: Uuid,
    pub user_id: String,
    pub r#type: String,
    pub title: String,
    pub message: Option<String>,
    pub link_url: Option<String>,
    pub is_read: bool,
    pub created_at: String,
}

impl From<Notification> for NotificationResponse {
    fn from(n: Notification) -> Self {
        Self {
            id: n.id,
            user_id: n.user_id,
            r#type: n.notif_type,
            title: n.title,
            message: n.message,
            link_url: n.link_url,
            is_read: n.is_read,
            created_at: n.created_at.to_string(),
        }
    }
}
