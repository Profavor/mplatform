use chrono::NaiveDateTime;
use serde::{Deserialize, Serialize};
use sqlx::FromRow;
use uuid::Uuid;

#[derive(Debug, Clone, FromRow, Deserialize)]
pub struct Record {
    pub id: Uuid,
    pub node_id: Uuid,
    pub data: Option<serde_json::Value>,
    pub searchable_data: Option<serde_json::Value>,
    pub status: String,
    pub version: i32,
    pub source_system: Option<String>,
    pub merged_into_record_id: Option<Uuid>,
    pub approval_request_id: Option<Uuid>,
    pub created_at: Option<NaiveDateTime>,
    pub updated_at: Option<NaiveDateTime>,
    #[sqlx(default)]
    pub node: Option<serde_json::Value>,
}

impl Serialize for Record {
    fn serialize<S>(&self, serializer: S) -> Result<S::Ok, S::Error>
    where
        S: serde::Serializer,
    {
        use serde::ser::SerializeMap;
        let mut map = serializer.serialize_map(None)?;
        map.serialize_entry("id", &self.id)?;
        map.serialize_entry("nodeId", &self.node_id)?;
        map.serialize_entry("node_id", &self.node_id)?;
        map.serialize_entry("data", &self.data)?;
        map.serialize_entry("searchableData", &self.searchable_data)?;
        map.serialize_entry("searchable_data", &self.searchable_data)?;
        map.serialize_entry("status", &self.status)?;
        map.serialize_entry("version", &self.version)?;
        map.serialize_entry("sourceSystem", &self.source_system)?;
        map.serialize_entry("source_system", &self.source_system)?;
        map.serialize_entry("mergedIntoRecordId", &self.merged_into_record_id)?;
        map.serialize_entry("merged_into_record_id", &self.merged_into_record_id)?;
        map.serialize_entry("approvalRequestId", &self.approval_request_id)?;
        map.serialize_entry("approval_request_id", &self.approval_request_id)?;
        map.serialize_entry("createdAt", &self.created_at)?;
        map.serialize_entry("created_at", &self.created_at)?;
        map.serialize_entry("updatedAt", &self.updated_at)?;
        map.serialize_entry("updated_at", &self.updated_at)?;

        let node_val = match &self.node {
            Some(n) => n.clone(),
            None => serde_json::json!({
                "id": self.node_id,
                "name": {}
            }),
        };
        map.serialize_entry("node", &node_val)?;

        map.end()
    }
}

#[derive(Debug, Clone, FromRow, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct RecordHistory {
    pub id: Uuid,
    pub record_id: Uuid,
    pub version: i32,
    pub change_type: Option<String>,
    pub changed_by: Option<String>,
    pub previous_data: Option<serde_json::Value>,
    pub new_data: Option<serde_json::Value>,
    pub source_system: Option<String>,
    pub approval_request_id: Option<Uuid>,
    pub changed_at: Option<NaiveDateTime>,
}

#[derive(Debug, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct CreateRecordRequest {
    #[serde(alias = "node_id")]
    pub node_id: Uuid,
    pub data: serde_json::Value,
    #[serde(alias = "source_system")]
    pub source_system: Option<String>,
}

#[derive(Debug, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct UpdateRecordRequest {
    pub data: serde_json::Value,
    #[serde(alias = "change_reason")]
    pub change_reason: Option<String>,
}

#[derive(Debug, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct PageResponse<T> {
    pub content: Vec<T>,
    pub total_elements: i64,
    pub total_pages: i64,
    pub page_number: i64,
    pub page_size: i64,
    pub size: i64,
    pub number: i64,
    pub first: bool,
    pub last: bool,
}

impl<T> PageResponse<T> {
    pub fn new(content: Vec<T>, total_elements: i64, page: i64, size: i64) -> Self {
        let total_pages = if size > 0 {
            (total_elements + size - 1) / size
        } else {
            0
        };
        let first = page == 0;
        let last = page >= total_pages - 1;

        Self {
            content,
            total_elements,
            total_pages,
            page_number: page,
            page_size: size,
            size,
            number: page,
            first,
            last,
        }
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_page_response_calculation() {
        let items = vec![1, 2, 3];
        let page = PageResponse::new(items, 25, 0, 10);
        assert_eq!(page.total_pages, 3);
        assert_eq!(page.first, true);
        assert_eq!(page.last, false);

        let last_page = PageResponse::new(vec![1], 25, 2, 10);
        assert_eq!(last_page.total_pages, 3);
        assert_eq!(last_page.first, false);
        assert_eq!(last_page.last, true);
    }
}
