use chrono::NaiveDateTime;
use serde::{Deserialize, Serialize};
use sqlx::FromRow;
use uuid::Uuid;

#[derive(Debug, Clone, FromRow, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct MatchingRule {
    pub id: Uuid,
    pub domain_id: Uuid,
    pub node_id: Option<Uuid>,
    pub rule_name: String,
    pub match_type: String,
    pub target_field_keys: serde_json::Value,
    pub similarity_threshold: Option<f64>,
    pub is_active: bool,
    pub created_at: Option<NaiveDateTime>,
    pub updated_at: Option<NaiveDateTime>,
}

#[derive(Debug, Clone, FromRow, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct MatchCandidate {
    pub id: Uuid,
    pub domain_id: Option<Uuid>,
    pub node_id: Uuid,
    pub existing_record_id: Uuid,
    pub matched_rule_id: Option<Uuid>,
    pub score: f64,
    pub source: String,
    pub status: String,
    pub incoming_data_json: String,
    pub matched_field_details: Option<String>,
    pub reviewed_by: Option<String>,
    pub reviewed_at: Option<NaiveDateTime>,
    pub created_at: Option<NaiveDateTime>,
}

#[derive(Debug, Clone, FromRow, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct SurvivorshipRule {
    pub id: Uuid,
    pub domain_id: Uuid,
    pub field_key: Option<String>,
    pub strategy: String,
    pub priority: i32,
}

#[derive(Debug, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct MergeRequest {
    pub survivor_record_id: Uuid,
    pub merged_record_ids: Vec<Uuid>,
    pub custom_overrides: Option<serde_json::Value>,
    pub reason: Option<String>,
}

#[derive(Debug, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct MergeResult {
    pub golden_record_id: Uuid,
    pub merged_count: usize,
    pub status: String,
}
