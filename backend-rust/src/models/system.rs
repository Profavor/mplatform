use chrono::NaiveDateTime;
use serde::{Deserialize, Serialize};
use sqlx::FromRow;

#[derive(Debug, Clone, FromRow, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct SystemFeature {
    pub id: i64,
    pub feature_no: i32,
    pub feature_name_key: String,
    pub category: String,
    pub icon_name: String,
    pub color_theme: String,
    pub bean_name: String,
    pub is_governance_core: bool,
}

#[derive(Debug, Clone, FromRow, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct SystemConfigItem {
    pub config_key: String,
    pub config_value: Option<String>,
    pub updated_at: Option<NaiveDateTime>,
}

#[derive(Debug, Clone, FromRow, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct ErrorLogItem {
    pub id: i64,
    pub user_id: Option<String>,
    pub request_uri: Option<String>,
    pub error_message: Option<String>,
    pub stack_trace: Option<String>,
    pub logged_at: NaiveDateTime,
}

#[derive(Debug, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct DiagnosticsReport {
    pub engine: String,
    pub version: String,
    pub status: String,
    pub memory_rss_mb: f64,
    pub db_pool_active: u32,
    pub db_pool_idle: u32,
    pub db_latency_ms: f64,
    pub uptime_seconds: u64,
}
