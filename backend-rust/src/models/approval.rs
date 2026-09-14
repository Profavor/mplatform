use chrono::NaiveDateTime;
use serde::{Deserialize, Serialize};
use sqlx::FromRow;
use uuid::Uuid;

#[derive(Debug, Clone, FromRow, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct ApprovalRequest {
    pub id: Uuid,
    pub target_id: Uuid,
    pub target_type: String,
    pub status: String,
    pub requester_id: String,
    pub reason: Option<String>,
    pub changes: serde_json::Value,
    pub observer_ids: Option<serde_json::Value>,
    pub node_id: Option<Uuid>,
    pub current_step_order: Option<i32>,
    pub version: i64,
    pub created_at: Option<NaiveDateTime>,
    pub updated_at: Option<NaiveDateTime>,
}

#[derive(Debug, Clone, FromRow, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct ApprovalStep {
    pub id: Uuid,
    pub request_id: Uuid,
    pub step_order: i32,
    pub step_type: String,
    pub status: String,
    pub assignee_id: Option<String>,
    pub assignee_role: Option<String>,
    pub comment: Option<String>,
    pub sla_hours: Option<i32>,
    pub sla_due_at: Option<NaiveDateTime>,
    pub is_escalated: bool,
    pub escalated_from_user_id: Option<String>,
    pub escalated_at: Option<NaiveDateTime>,
    pub version: i64,
    pub created_at: Option<NaiveDateTime>,
    pub updated_at: Option<NaiveDateTime>,
}

#[derive(Debug, Clone, FromRow, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct ApprovalDelegation {
    pub id: Uuid,
    pub delegator_user_id: String,
    pub delegatee_user_id: String,
    pub reason: Option<String>,
    pub start_date: NaiveDateTime,
    pub end_date: NaiveDateTime,
    pub is_active: bool,
    pub created_at: NaiveDateTime,
    pub updated_at: Option<NaiveDateTime>,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct ApprovalDelegationDto {
    pub id: Uuid,
    pub delegator_user_id: String,
    pub delegator_user_name: Option<String>,
    pub delegatee_user_id: String,
    pub delegatee_user_name: Option<String>,
    pub reason: Option<String>,
    pub start_date: NaiveDateTime,
    pub end_date: NaiveDateTime,
    pub is_active: bool,
    pub created_at: NaiveDateTime,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct MyDelegationsResponse {
    pub delegated_by_me: Vec<ApprovalDelegationDto>,
    pub delegated_to_me: Vec<ApprovalDelegationDto>,
    pub by_me: Vec<ApprovalDelegationDto>,
    pub to_me: Vec<ApprovalDelegationDto>,
}

#[derive(Debug, Clone, FromRow, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct ApprovalRoutingTemplate {
    pub id: Uuid,
    pub domain_id: Option<Uuid>,
    pub template_name: String,
    pub condition_field: Option<String>,
    pub condition_operator: Option<String>,
    pub condition_value: Option<String>,
    pub steps_json: String,
    pub created_at: Option<NaiveDateTime>,
}

#[derive(Debug, Clone, FromRow, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct WorkflowConfig {
    pub id: Uuid,
    pub domain_id: Option<Uuid>,
    pub node_id: Option<Uuid>,
    pub name: Option<String>,
    pub action_type: String,
    pub description: Option<String>,
    pub is_active: bool,
    pub is_default: bool,
    pub steps_config: Option<String>,
    pub created_at: Option<NaiveDateTime>,
    pub updated_at: Option<NaiveDateTime>,
}

#[derive(Debug, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct CreateApprovalRequestDto {
    pub target_id: Uuid,
    pub target_type: String,
    pub reason: Option<String>,
    pub changes: serde_json::Value,
    pub node_id: Option<Uuid>,
    pub assignee_id: Option<String>,
}

#[derive(Debug, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct StepActionRequest {
    pub comment: Option<String>,
    pub action_by: Option<String>,
}

#[derive(Debug, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct MemoRequest {
    pub request_id: Uuid,
    pub comment: String,
}

#[derive(Debug, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct CreateDelegationDto {
    pub delegatee_user_id: String,
    pub reason: Option<String>,
    pub start_date: Option<String>,
    pub end_date: Option<String>,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct ApprovalDetailResponse {
    pub request: ApprovalRequest,
    pub steps: Vec<ApprovalStep>,
}

pub type CreateApprovalRequest = CreateApprovalRequestDto;

#[derive(Debug, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct ApproveStepRequest {
    pub comment: Option<String>,
}

#[derive(Debug, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct RejectStepRequest {
    pub reason: Option<String>,
}
