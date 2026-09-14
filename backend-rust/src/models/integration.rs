use chrono::NaiveDateTime;
use serde::{Deserialize, Serialize};
use sqlx::FromRow;
use uuid::Uuid;

#[derive(Debug, Clone, FromRow, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct IntegrationChannel {
    pub id: Uuid,
    pub name: String,
    pub channel_code: Option<String>,
    pub r#type: String,
    pub direction: String,
    pub config_json: Option<String>,
    pub mapping_config_json: Option<String>,
    pub is_active: bool,
    pub requires_approval: bool,
    pub max_retries: i32,
    pub retry_backoff_ms: i64,
    pub use_exponential_backoff: bool,
    pub node_id: Option<Uuid>,
    pub created_at: Option<NaiveDateTime>,
    pub updated_at: Option<NaiveDateTime>,
}

#[derive(Debug, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct CreateChannelRequest {
    pub name: String,
    pub channel_code: Option<String>,
    pub r#type: String,
    #[serde(default = "default_direction")]
    pub direction: String,
    pub config_json: Option<String>,
    pub mapping_config_json: Option<String>,
    #[serde(default = "default_true")]
    pub is_active: bool,
    #[serde(default)]
    pub requires_approval: bool,
    #[serde(default = "default_three")]
    pub max_retries: i32,
    #[serde(default = "default_backoff")]
    pub retry_backoff_ms: i64,
    #[serde(default = "default_true")]
    pub use_exponential_backoff: bool,
    pub node_id: Option<Uuid>,
}

#[derive(Debug, Clone, FromRow, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct IntegrationLog {
    pub id: Uuid,
    pub channel_id: Uuid,
    pub record_id: Option<Uuid>,
    pub event_type: String,
    pub status: String,
    pub retry_count: i32,
    pub error_message: Option<String>,
    pub original_payload: Option<String>,
    pub mapped_payload: Option<String>,
    pub created_at: Option<NaiveDateTime>,
}

fn default_direction() -> String { "OUTBOUND".to_string() }
fn default_true() -> bool { true }
fn default_three() -> i32 { 3 }
fn default_backoff() -> i64 { 1000 }
