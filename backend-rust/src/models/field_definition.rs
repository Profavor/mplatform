use chrono::NaiveDateTime;
use serde::{Deserialize, Serialize};
use sqlx::FromRow;
use uuid::Uuid;

#[derive(Debug, Clone, FromRow, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct FieldDefinition {
    pub id: Uuid,
    pub domain_id: Option<Uuid>,
    pub defined_at_node_id: Option<Uuid>,
    pub field_group_id: Option<Uuid>,
    #[serde(alias = "key", alias = "field_key")]
    pub field_key: String,
    pub name: Option<serde_json::Value>,
    #[sqlx(rename = "type")]
    #[serde(rename = "type")]
    pub field_type: Option<String>,
    pub required: Option<bool>,
    pub is_searchable: Option<bool>,
    pub is_read_only: Option<bool>,
    pub is_hidden: Option<bool>,
    pub is_encrypted: Option<bool>,
    pub is_immutable: Option<bool>,
    pub is_highlighted: Option<bool>,
    pub is_multi_value: Option<bool>,
    pub is_table: Option<bool>,
    pub is_removed: Option<bool>,
    pub is_indexed: Option<bool>,
    pub default_value: Option<serde_json::Value>,
    pub options: Option<serde_json::Value>,
    pub hint: Option<serde_json::Value>,
    pub unit: Option<String>,
    pub masking_pattern: Option<String>,
    #[serde(alias = "order")]
    pub field_order: Option<i32>,
    pub grid_width: Option<i32>,
    pub table_column_width: Option<i32>,
    pub created_at: Option<NaiveDateTime>,
    pub updated_at: Option<NaiveDateTime>,
    #[sqlx(default)]
    pub field_group: Option<serde_json::Value>,
}

impl Serialize for FieldDefinition {
    fn serialize<S>(&self, serializer: S) -> Result<S::Ok, S::Error>
    where
        S: serde::Serializer,
    {
        use serde::ser::SerializeMap;
        let mut map = serializer.serialize_map(None)?;
        map.serialize_entry("id", &self.id)?;
        map.serialize_entry("domainId", &self.domain_id)?;
        map.serialize_entry("definedAtNodeId", &self.defined_at_node_id)?;

        let fg_id = self.field_group_id.or_else(|| {
            self.field_group.as_ref().and_then(|fg| {
                fg.get("id")
                    .and_then(|v| v.as_str())
                    .and_then(|s| Uuid::parse_str(s).ok())
            })
        });
        map.serialize_entry("fieldGroupId", &fg_id)?;
        map.serialize_entry("fieldGroup", &self.field_group)?;

        // Send key, fieldKey, and field_key for 100% frontend compatibility
        map.serialize_entry("key", &self.field_key)?;
        map.serialize_entry("fieldKey", &self.field_key)?;
        map.serialize_entry("field_key", &self.field_key)?;
        map.serialize_entry("name", &self.name)?;
        map.serialize_entry("type", &self.field_type)?;
        map.serialize_entry("required", &self.required.unwrap_or(false))?;
        map.serialize_entry("isSearchable", &self.is_searchable.unwrap_or(true))?;
        map.serialize_entry("isReadOnly", &self.is_read_only.unwrap_or(false))?;
        map.serialize_entry("isHidden", &self.is_hidden.unwrap_or(false))?;
        map.serialize_entry("isEncrypted", &self.is_encrypted.unwrap_or(false))?;
        map.serialize_entry("isImmutable", &self.is_immutable.unwrap_or(false))?;
        map.serialize_entry("isHighlighted", &self.is_highlighted.unwrap_or(false))?;
        map.serialize_entry("isMultiValue", &self.is_multi_value.unwrap_or(false))?;
        map.serialize_entry("isTable", &self.is_table.unwrap_or(false))?;
        map.serialize_entry("isRemoved", &self.is_removed.unwrap_or(false))?;
        map.serialize_entry("isIndexed", &self.is_indexed.unwrap_or(false))?;
        map.serialize_entry("defaultValue", &self.default_value)?;
        map.serialize_entry("default_value", &self.default_value)?;
        map.serialize_entry("options", &self.options)?;
        map.serialize_entry("hint", &self.hint)?;
        map.serialize_entry("unit", &self.unit)?;
        map.serialize_entry("maskingPattern", &self.masking_pattern)?;
        // Send both order and fieldOrder
        map.serialize_entry("order", &self.field_order)?;
        map.serialize_entry("fieldOrder", &self.field_order)?;
        map.serialize_entry("gridWidth", &self.grid_width)?;
        map.serialize_entry("tableColumnWidth", &self.table_column_width)?;
        map.serialize_entry("createdAt", &self.created_at)?;
        map.serialize_entry("updatedAt", &self.updated_at)?;
        map.end()
    }
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
    #[serde(alias = "key", alias = "fieldKey")]
    pub field_key: Option<String>,
    pub name: Option<serde_json::Value>,
    pub hint: Option<serde_json::Value>,
    #[serde(rename = "type")]
    pub field_type: Option<String>,
    pub required: Option<bool>,
    pub is_searchable: Option<bool>,
    pub is_read_only: Option<bool>,
    pub is_hidden: Option<bool>,
    pub is_encrypted: Option<bool>,
    pub is_immutable: Option<bool>,
    pub is_highlighted: Option<bool>,
    pub is_multi_value: Option<bool>,
    pub is_table: Option<bool>,
    pub is_removed: Option<bool>,
    pub is_indexed: Option<bool>,
    pub default_value: Option<serde_json::Value>,
    pub options: Option<serde_json::Value>,
    pub unit: Option<String>,
    pub masking_pattern: Option<String>,
    #[serde(alias = "order", alias = "fieldOrder")]
    pub field_order: Option<i32>,
    pub grid_width: Option<i32>,
    pub table_column_width: Option<i32>,
    #[serde(alias = "fieldGroupId", alias = "field_group_id")]
    pub field_group_id: Option<Uuid>,
    #[serde(alias = "fieldGroup")]
    pub field_group: Option<serde_json::Value>,
    pub domain_id: Option<Uuid>,
}

impl FieldDefinitionRequest {
    pub fn resolved_group_id(&self) -> Option<Uuid> {
        self.field_group_id.or_else(|| {
            self.field_group.as_ref().and_then(|g| {
                g.get("id")
                    .and_then(|id_val| id_val.as_str())
                    .and_then(|s| Uuid::parse_str(s).ok())
            })
        })
    }
}
