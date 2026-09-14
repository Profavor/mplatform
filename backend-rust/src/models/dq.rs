use chrono::NaiveDateTime;
use serde::{Deserialize, Serialize};
use sqlx::FromRow;
use uuid::Uuid;

#[derive(Debug, Clone, FromRow, Serialize, Deserialize)]
pub struct DqRule {
    pub id: Uuid,
    pub domain_id: Option<Uuid>,
    pub node_id: Option<Uuid>,
    pub field_definition_id: Uuid,
    pub rule_type: String,
    pub severity: String,
    pub is_active: bool,
    pub sort_order: i32,
    pub params: Option<serde_json::Value>,
    pub message: Option<serde_json::Value>,
    pub created_at: Option<NaiveDateTime>,
    pub updated_at: Option<NaiveDateTime>,
}

#[derive(Debug, Clone, FromRow, Serialize, Deserialize)]
pub struct RuleWithField {
    pub id: Uuid,
    pub domain_id: Option<Uuid>,
    pub node_id: Option<Uuid>,
    pub field_definition_id: Uuid,
    pub field_key: String,
    pub rule_type: String,
    pub severity: String,
    pub is_active: bool,
    pub sort_order: i32,
    pub params: Option<serde_json::Value>,
    pub message: Option<serde_json::Value>,
}

#[derive(Debug, Clone, FromRow, Serialize, Deserialize)]
pub struct DqViolation {
    pub id: Uuid,
    pub record_id: Uuid,
    pub dq_rule_id: Option<Uuid>,
    pub field_key: String,
    pub severity: String,
    pub message: Option<serde_json::Value>,
    pub actual_value: Option<String>,
    pub resolved: bool,
    pub checked_at: NaiveDateTime,
}

#[derive(Debug, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct CreateDqRuleRequest {
    pub domain_id: Option<Uuid>,
    pub node_id: Option<Uuid>,
    pub field_definition_id: Uuid,
    pub rule_type: String,
    pub severity: String,
    pub is_active: Option<bool>,
    pub sort_order: Option<i32>,
    pub params: Option<serde_json::Value>,
    pub message: Option<serde_json::Value>,
}

#[derive(Debug, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct DqViolationItem {
    pub record_index: usize,
    pub field_key: String,
    pub rule_type: String,
    pub severity: String,
    pub message: String,
    pub actual_value: Option<String>,
}

#[derive(Debug, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct BatchValidateRequest {
    pub domain_id: Uuid,
    pub node_id: Option<Uuid>,
    pub records: Vec<serde_json::Value>,
}

#[derive(Debug, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct BatchValidateResponse {
    pub total_records: usize,
    pub valid_records: usize,
    pub invalid_records: usize,
    pub violations: Vec<DqViolationItem>,
}

#[derive(Debug, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct DqScanResult {
    pub scanned_records: usize,
    pub violation_count: usize,
    pub total_rules: usize,
    pub status: String,
}
