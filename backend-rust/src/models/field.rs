use chrono::NaiveDateTime;
use serde::{Deserialize, Serialize};
use sqlx::FromRow;
use uuid::Uuid;

#[derive(Debug, Clone, FromRow, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct FieldDefinition {
    pub id: Uuid,
    pub defined_at_node_id: Option<Uuid>,
    pub domain_id: Option<Uuid>,
    pub field_group_id: Option<Uuid>,
    pub field_key: String,
    pub name: Option<serde_json::Value>,
    pub hint: Option<serde_json::Value>,
    #[serde(rename = "type")]
    #[sqlx(rename = "type")]
    pub field_type: String,
    pub required: bool,
    pub is_searchable: bool,
    pub is_multi_value: bool,
    pub is_encrypted: bool,
    pub is_immutable: Option<bool>,
    pub is_read_only: Option<bool>,
    pub is_hidden: Option<bool>,
    pub is_highlighted: Option<bool>,
    pub is_removed: bool,
    pub is_table: bool,
    pub is_indexed: Option<bool>,
    pub options: Option<serde_json::Value>,
    pub default_value: Option<serde_json::Value>,
    pub unit: Option<String>,
    pub masking_pattern: Option<String>,
    pub field_order: i32,
    pub grid_width: Option<i32>,
    pub table_column_width: Option<i32>,
    pub created_at: Option<NaiveDateTime>,
    pub updated_at: Option<NaiveDateTime>,
}

#[derive(Debug, Clone, FromRow, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct FieldGroup {
    pub id: Uuid,
    pub domain_id: Option<Uuid>,
    pub name: Option<serde_json::Value>,
    pub group_order: i32,
    pub is_collapsed: Option<bool>,
    pub created_at: Option<NaiveDateTime>,
    pub updated_at: Option<NaiveDateTime>,
}

#[derive(Debug, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct FieldDefinitionRequest {
    pub field_key: Option<String>,
    pub name: Option<serde_json::Value>,
    pub hint: Option<serde_json::Value>,
    #[serde(rename = "type")]
    pub field_type: Option<String>,
    pub required: Option<bool>,
    pub is_searchable: Option<bool>,
    pub is_multi_value: Option<bool>,
    pub is_encrypted: Option<bool>,
    pub is_immutable: Option<bool>,
    pub is_read_only: Option<bool>,
    pub is_hidden: Option<bool>,
    pub is_highlighted: Option<bool>,
    pub is_removed: Option<bool>,
    pub is_table: Option<bool>,
    pub is_indexed: Option<bool>,
    pub options: Option<serde_json::Value>,
    pub default_value: Option<serde_json::Value>,
    pub unit: Option<String>,
    pub masking_pattern: Option<String>,
    pub field_order: Option<i32>,
    pub grid_width: Option<i32>,
    pub table_column_width: Option<i32>,
    pub field_group_id: Option<Uuid>,
    pub domain_id: Option<Uuid>,
}
