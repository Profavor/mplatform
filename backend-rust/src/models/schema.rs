use chrono::NaiveDateTime;
use serde::{Deserialize, Serialize};
use sqlx::FromRow;
use uuid::Uuid;

#[derive(Debug, Clone, FromRow, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct SchemaHistory {
    pub id: Uuid,
    pub domain_id: Option<Uuid>,
    pub target_type: String,
    pub target_id: Uuid,
    pub action: String,
    pub before_data: Option<String>,
    pub after_data: Option<String>,
    pub changed_by: Option<String>,
    pub changed_at: Option<NaiveDateTime>,
}

#[derive(Debug, Clone, FromRow, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct TaxonomyVersion {
    pub id: Uuid,
    pub domain_id: Uuid,
    pub version_label: String,
    pub snapshot_data: Option<serde_json::Value>,
    pub is_active: bool,
    pub published_by: Option<String>,
    pub published_at: Option<NaiveDateTime>,
    pub created_at: Option<NaiveDateTime>,
}

#[derive(Debug, Clone, FromRow, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct DomainSnapshot {
    pub id: Uuid,
    pub domain_id: Uuid,
    pub snapshot_name: String,
    pub version_tag: String,
    pub record_count: i32,
    pub snapshot_data: String,
    pub created_by: Option<String>,
    pub created_at: Option<NaiveDateTime>,
}

#[derive(Debug, Clone, FromRow, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct MasterRelation {
    pub id: Uuid,
    pub source_domain_id: Uuid,
    pub source_field_key: String,
    pub target_domain_id: Uuid,
    pub relation_type: String,
    pub cascade_policy: String,
    pub is_active: bool,
    pub created_at: Option<NaiveDateTime>,
    pub updated_at: Option<NaiveDateTime>,
}
