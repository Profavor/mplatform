use chrono::NaiveDateTime;
use serde::{Deserialize, Serialize};
use sqlx::FromRow;
use uuid::Uuid;

#[derive(Debug, Clone, FromRow, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct ClassificationAxis {
    pub id: Uuid,
    pub domain_id: Uuid,
    pub axis_code: Option<String>,
    pub name: serde_json::Value,
    pub description: Option<String>,
    pub is_default: Option<bool>,
    pub sort_order: Option<i32>,
    pub created_at: Option<NaiveDateTime>,
    pub updated_at: Option<NaiveDateTime>,
}

#[derive(Debug, Clone, FromRow, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct ClassificationNode {
    pub id: Uuid,
    pub domain_id: Uuid,
    pub axis_id: Option<Uuid>,
    pub parent_id: Option<Uuid>,
    pub name: serde_json::Value,
    pub depth: Option<i32>,
    pub node_order: Option<i32>,
    pub path: Option<String>,
    pub icon: Option<String>,
    pub is_deleted: Option<bool>,
    pub detail_layout_config: Option<serde_json::Value>,
    pub created_at: Option<NaiveDateTime>,
    pub updated_at: Option<NaiveDateTime>,
    pub deleted_at: Option<NaiveDateTime>,
}

#[derive(Debug, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct CreateAxisRequest {
    pub domain_id: Uuid,
    pub axis_code: String,
    pub name: serde_json::Value,
    pub description: Option<String>,
    pub is_default: Option<bool>,
    pub sort_order: Option<i32>,
}

#[derive(Debug, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct CreateNodeRequest {
    pub domain_id: Uuid,
    pub axis_id: Option<Uuid>,
    pub parent_id: Option<Uuid>,
    pub name: serde_json::Value,
    pub icon: Option<String>,
    pub node_order: Option<i32>,
}
