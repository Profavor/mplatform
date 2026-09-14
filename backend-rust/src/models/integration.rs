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
    #[sqlx(default)]
    #[serde(default)]
    pub direction: Option<String>,
    #[sqlx(default)]
    #[serde(default)]
    pub channel_name: Option<String>,
    #[sqlx(default)]
    #[serde(default)]
    pub channel_code: Option<String>,
}

fn default_direction() -> String { "OUTBOUND".to_string() }
fn default_true() -> bool { true }
fn default_three() -> i32 { 3 }
fn default_backoff() -> i64 { 1000 }

#[derive(Debug, Clone, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct IntegrationMetricsDto {
    pub channel_id: Uuid,
    pub channel_name: String,
    pub channel_type: String,
    pub health_status: String, // HEALTHY, DEGRADED, UNHEALTHY
    pub total_requests: i64,
    pub success_count: i64,
    pub fail_count: i64,
    pub dlq_count: i64,
    pub success_rate: f64,
    pub avg_latency_ms: i64,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub last_ping_latency_ms: Option<i64>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub last_ping_at: Option<chrono::DateTime<chrono::Utc>>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub last_ping_message: Option<String>,
    pub hourly_stats: Vec<HourlyStat>,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct HourlyStat {
    pub time_slot: String,
    pub success_count: i64,
    pub fail_count: i64,
    pub dlq_count: i64,
}

#[derive(Debug, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct SmartMappingRecommendRequest {
    pub domain_id: Uuid,
    pub sample_payload: String,
}

#[derive(Debug, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct SmartMappingRecommendationDto {
    pub source_field: String,
    pub target_field_key: String,
    pub target_field_name: String,
    pub confidence_score: i32,
    pub match_reason: String,
    pub recommended_spel: String,
}
