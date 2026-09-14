use chrono::{NaiveDate, NaiveDateTime};
use serde::{Deserialize, Serialize};
use sqlx::FromRow;
use uuid::Uuid;

#[derive(Debug, Clone, FromRow, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct CodeGroup {
    pub id: Uuid,
    pub group_code: String,
    pub name: serde_json::Value,
    pub description: Option<serde_json::Value>,
    pub is_active: bool,
    pub organization_id: Option<Uuid>,
    pub created_at: Option<NaiveDateTime>,
    pub updated_at: Option<NaiveDateTime>,
}

#[derive(Debug, Clone, FromRow, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct CodeDetail {
    pub id: Uuid,
    pub group_id: Uuid,
    pub detail_code: String,
    pub name: serde_json::Value,
    pub sort_order: Option<i32>,
    pub valid_from: Option<NaiveDate>,
    pub valid_to: Option<NaiveDate>,
    pub is_active: bool,
    pub created_at: Option<NaiveDateTime>,
    pub updated_at: Option<NaiveDateTime>,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct CodeGroupRequest {
    pub group_code: String,
    pub name: serde_json::Value,
    pub description: Option<serde_json::Value>,
    pub organization_id: Option<Uuid>,
    pub is_active: Option<bool>,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct CodeDetailRequest {
    pub detail_code: String,
    pub name: serde_json::Value,
    pub sort_order: Option<i32>,
    pub valid_from: Option<NaiveDate>,
    pub valid_to: Option<NaiveDate>,
    pub is_active: Option<bool>,
}
