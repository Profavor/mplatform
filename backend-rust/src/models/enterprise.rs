use chrono::NaiveDateTime;
use serde::{Deserialize, Serialize};
use sqlx::FromRow;
use uuid::Uuid;

#[derive(Debug, Clone, FromRow, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct SensitiveDataAccessLog {
    pub id: Uuid,
    pub user_id: String,
    pub username: Option<String>,
    pub target_type: String,
    pub target_id: Uuid,
    pub field_keys: Option<String>,
    pub access_reason: Option<String>,
    pub ip_address: Option<String>,
    pub accessed_at: NaiveDateTime,
}

#[derive(Debug, Clone, FromRow, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct WebhookSubscription {
    pub id: Uuid,
    pub name: String,
    pub target_url: String,
    pub events_csv: String,
    pub secret_key: Option<String>,
    pub is_active: bool,
    pub created_at: Option<NaiveDateTime>,
}

#[derive(Debug, Clone, FromRow, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct UserYoutubeConfig {
    pub id: Uuid,
    pub user_id: String,
    pub youtube_channel_url: Option<String>,
    pub playlist_id: Option<String>,
    pub playlist_title: Option<String>,
    pub api_key: Option<String>,
    pub updated_at: Option<NaiveDateTime>,
}

#[derive(Debug, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct CreateWebhookDto {
    pub name: String,
    pub target_url: String,
    pub events_csv: String,
    pub secret_key: Option<String>,
}

#[derive(Debug, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct YoutubeConfigDto {
    pub channel_url: Option<String>,
    pub playlist_id: Option<String>,
    pub playlist_title: Option<String>,
    pub api_key: Option<String>,
}
