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
    #[sqlx(default)]
    pub reason: Option<String>,
    pub changes: serde_json::Value,
    #[sqlx(default)]
    pub observer_ids: Option<serde_json::Value>,
    #[sqlx(default)]
    pub node_id: Option<Uuid>,
    #[sqlx(default)]
    pub current_step_order: Option<i32>,
    #[sqlx(default)]
    pub version: i64,
    #[sqlx(default)]
    pub created_at: Option<NaiveDateTime>,
    #[sqlx(default)]
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
    #[sqlx(default)]
    pub assignee_id: Option<String>,
    #[sqlx(default)]
    pub assignee_role: Option<String>,
    #[sqlx(default)]
    pub comment: Option<String>,
    #[sqlx(default)]
    pub sla_hours: Option<i32>,
    #[sqlx(default)]
    pub sla_due_at: Option<NaiveDateTime>,
    #[sqlx(default)]
    pub is_escalated: bool,
    #[sqlx(default)]
    pub escalated_from_user_id: Option<String>,
    #[sqlx(default)]
    pub escalated_at: Option<NaiveDateTime>,
    #[sqlx(default)]
    pub version: i64,
    #[sqlx(default)]
    pub created_at: Option<NaiveDateTime>,
    #[sqlx(default)]
    pub updated_at: Option<NaiveDateTime>,
    #[sqlx(skip)]
    pub approval_request: Option<ApprovalRequest>,
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

#[derive(Debug, Clone, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct ApprovalDetailResponse {
    pub request: ApprovalRequest,
    pub steps: Vec<ApprovalStep>,
}

impl Serialize for ApprovalDetailResponse {
    fn serialize<S>(&self, serializer: S) -> Result<S::Ok, S::Error>
    where
        S: serde::Serializer,
    {
        use serde::ser::SerializeMap;
        let mut map = serializer.serialize_map(None)?;

        // Top-level fields expected by frontend enrichRequest and detail modal
        map.serialize_entry("id", &self.request.id)?;
        map.serialize_entry("targetId", &self.request.target_id)?;
        map.serialize_entry("targetType", &self.request.target_type)?;
        map.serialize_entry("status", &self.request.status)?;
        map.serialize_entry("requesterId", &self.request.requester_id)?;
        map.serialize_entry("reason", &self.request.reason)?;
        map.serialize_entry("changes", &self.request.changes)?;
        map.serialize_entry("observerIds", &self.request.observer_ids)?;
        map.serialize_entry("nodeId", &self.request.node_id)?;
        map.serialize_entry("classificationNodeId", &self.request.node_id)?;
        map.serialize_entry("currentStepOrder", &self.request.current_step_order)?;
        map.serialize_entry("version", &self.request.version)?;
        map.serialize_entry("createdAt", &self.request.created_at)?;
        map.serialize_entry("updatedAt", &self.request.updated_at)?;

        // Array of approval steps
        map.serialize_entry("steps", &self.steps)?;

        // Nested request object for backward compatibility
        map.serialize_entry("request", &self.request)?;

        map.end()
    }
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
