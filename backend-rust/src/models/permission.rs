use chrono::NaiveDateTime;
use serde::{Deserialize, Serialize};
use sqlx::FromRow;
use uuid::Uuid;

#[derive(Debug, Clone, FromRow, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct PermissionGroup {
    pub id: String,
    pub code: String,
    pub title_ko: String,
    pub title_en: Option<String>,
    pub icon: Option<String>,
    pub color: Option<String>,
    pub chip_class: Option<String>,
    pub sort_order: Option<i32>,
}

#[derive(Debug, Clone, FromRow, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct PermissionItem {
    pub id: Uuid,
    pub group_id: Option<String>,
    pub perm_value: String,
    pub label_ko: String,
    pub label_en: Option<String>,
    pub sort_order: Option<i32>,
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
    pub description: Option<String>,
    pub is_active: bool,
    pub created_by: Option<String>,
    pub created_at: Option<NaiveDateTime>,
    pub updated_at: Option<NaiveDateTime>,
}

#[derive(Debug, Clone, FromRow, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct DataScopePermission {
    pub id: Uuid,
    pub user_id: String,
    pub domain_id: Uuid,
    pub node_id: Option<Uuid>,
    pub permission_level: String,
    pub created_by: Option<String>,
    pub created_at: Option<NaiveDateTime>,
    pub updated_at: Option<NaiveDateTime>,
}

#[derive(Debug, Clone, FromRow, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct DomainPermission {
    pub id: Uuid,
    pub user_id: String,
    pub domain_id: Uuid,
    pub created_at: Option<NaiveDateTime>,
}

#[derive(Debug, Clone, FromRow, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct DomainAccessRequest {
    pub id: Uuid,
    pub user_id: String,
    pub domain_id: Uuid,
    pub status: String,
    pub created_at: Option<NaiveDateTime>,
    pub updated_at: Option<NaiveDateTime>,
}
