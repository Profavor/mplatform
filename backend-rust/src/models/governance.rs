use chrono::NaiveDateTime;
use serde::{Deserialize, Serialize};
use sqlx::FromRow;
use uuid::Uuid;

#[derive(Debug, Clone, FromRow, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct BusinessTerm {
    pub id: Uuid,
    pub term_code: String,
    pub term_name: serde_json::Value,
    pub description: Option<String>,
    pub domain_id: Option<Uuid>,
    pub data_type: Option<String>,
    pub synonyms: Option<String>,
    pub abbreviation: Option<String>,
    pub sensitivity_level: Option<String>,
    pub created_at: NaiveDateTime,
}

#[derive(Debug, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct CreateBusinessTermRequest {
    pub term_code: String,
    pub term_name: serde_json::Value,
    pub description: Option<String>,
    pub domain_id: Option<Uuid>,
    pub data_type: Option<String>,
    pub synonyms: Option<String>,
    pub abbreviation: Option<String>,
    pub sensitivity_level: Option<String>,
}

#[derive(Debug, Clone, FromRow, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct ColumnMaskingPolicy {
    pub id: Uuid,
    pub domain_id: Option<Uuid>,
    pub field_key: String,
    pub target_type: String,
    pub target_id: String,
    pub masking_action: String,
    pub is_active: bool,
    pub description: Option<String>,
    pub created_by: Option<String>,
    pub created_at: Option<NaiveDateTime>,
    pub updated_at: Option<NaiveDateTime>,
}

#[derive(Debug, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct CreateMaskingPolicyRequest {
    pub domain_id: Option<Uuid>,
    pub field_key: String,
    pub target_type: String,
    pub target_id: String,
    pub masking_action: String,
    #[serde(default = "default_true")]
    pub is_active: bool,
    pub description: Option<String>,
}

#[derive(Debug, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct CopilotChatRequest {
    pub message: String,
    pub domain_id: Option<Uuid>,
}

#[derive(Debug, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct CopilotChatResponse {
    pub reply: String,
    pub suggested_actions: Vec<String>,
    pub related_domains: Vec<Uuid>,
}

fn default_true() -> bool {
    true
}
