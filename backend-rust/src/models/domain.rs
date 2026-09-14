use chrono::NaiveDateTime;
use serde::{Deserialize, Serialize};
use sqlx::FromRow;
use uuid::Uuid;

#[derive(Debug, Clone, FromRow, Serialize, Deserialize)]
pub struct Domain {
    pub id: Uuid,
    pub name: serde_json::Value,
    pub description: Option<serde_json::Value>,
    pub icon: Option<String>,
    pub domain_type: Option<String>,
    pub specialized_category: Option<String>,
    pub auto_dq_scan_enabled: bool,
    pub current_sequence: i64,
    pub description_field_id: Option<Uuid>,
    pub detail_layout_config: Option<serde_json::Value>,
    pub display_name_field_id: Option<Uuid>,
    pub identifier_field_id: Option<Uuid>,
    pub image_field_id: Option<Uuid>,
    pub numbering_pattern: Option<String>,
    pub organization_id: Option<Uuid>,
    pub sort_order: i32,
    pub created_at: Option<NaiveDateTime>,
    pub updated_at: Option<NaiveDateTime>,
}

#[derive(Debug, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct DomainRequest {
    pub name: serde_json::Value,
    pub description: Option<serde_json::Value>,
    pub icon: Option<String>,
    pub domain_type: Option<String>,
    pub specialized_category: Option<String>,
    pub auto_dq_scan_enabled: Option<bool>,
    pub sort_order: Option<i32>,
    pub numbering_pattern: Option<String>,
    pub identifier_field_id: Option<Uuid>,
    pub display_name_field_id: Option<Uuid>,
    pub description_field_id: Option<Uuid>,
    pub image_field_id: Option<Uuid>,
}

#[derive(Debug, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct DomainResponse {
    pub id: Uuid,
    pub organization_id: Option<Uuid>,
    pub name: serde_json::Value,
    pub description: Option<serde_json::Value>,
    pub icon: Option<String>,
    pub domain_type: Option<String>,
    pub specialized_category: Option<String>,
    pub auto_dq_scan_enabled: bool,
    pub sort_order: i32,
    pub current_sequence: i64,
    pub identifier_field_id: Option<Uuid>,
    pub display_name_field_id: Option<Uuid>,
    pub description_field_id: Option<Uuid>,
    pub image_field_id: Option<Uuid>,
    pub numbering_pattern: Option<String>,
    pub created_at: Option<String>,
    pub updated_at: Option<String>,
}

impl From<Domain> for DomainResponse {
    fn from(d: Domain) -> Self {
        Self {
            id: d.id,
            organization_id: d.organization_id,
            name: d.name,
            description: d.description,
            icon: d.icon,
            domain_type: d.domain_type,
            specialized_category: d.specialized_category,
            auto_dq_scan_enabled: d.auto_dq_scan_enabled,
            sort_order: d.sort_order,
            current_sequence: d.current_sequence,
            identifier_field_id: d.identifier_field_id,
            display_name_field_id: d.display_name_field_id,
            description_field_id: d.description_field_id,
            image_field_id: d.image_field_id,
            numbering_pattern: d.numbering_pattern,
            created_at: d.created_at.map(|t| t.to_string()),
            updated_at: d.updated_at.map(|t| t.to_string()),
        }
    }
}

#[derive(Debug, Clone, FromRow, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct Sector {
    pub id: Uuid,
    pub domain_id: Uuid,
    pub name: serde_json::Value,
    pub sort_order: i32,
}

#[derive(Debug, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct SectorRequest {
    pub name: serde_json::Value,
    #[serde(default)]
    pub sort_order: Option<i32>,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct FieldGroupResponse {
    pub id: Uuid,
    pub domain_id: Uuid,
    pub sector_id: Uuid,
    pub name: serde_json::Value,
    pub sort_order: i32,
    pub is_default_open: Option<bool>,
    pub sector: Option<serde_json::Value>,
}

#[derive(Debug, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct FieldGroupRequest {
    pub sector_id: Option<Uuid>,
    pub name: serde_json::Value,
    #[serde(default)]
    pub sort_order: Option<i32>,
    #[serde(default)]
    pub is_default_open: Option<bool>,
}

