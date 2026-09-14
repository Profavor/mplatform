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

#[derive(Debug, Clone, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct CreateRecordRequest {
    pub node_id: Option<Uuid>,
    pub data: serde_json::Value,
    pub source_system: Option<String>,
}

impl<'de> Deserialize<'de> for CreateRecordRequest {
    fn deserialize<D>(deserializer: D) -> Result<Self, D::Error>
    where
        D: serde::Deserializer<'de>,
    {
        let value = serde_json::Value::deserialize(deserializer)?;
        let node_id = value
            .get("nodeId")
            .or_else(|| value.get("node_id"))
            .and_then(|v| {
                if let Some(s) = v.as_str() {
                    Uuid::parse_str(s).ok()
                } else {
                    None
                }
            });
        let data = value
            .get("data")
            .cloned()
            .unwrap_or(serde_json::Value::Null);
        let source_system = value
            .get("sourceSystem")
            .or_else(|| value.get("source_system"))
            .and_then(|v| v.as_str().map(String::from));

        Ok(CreateRecordRequest {
            node_id,
            data,
            source_system,
        })
    }
}

#[derive(Debug, Clone, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct RecordChangeRequestDto {
    pub data: Option<serde_json::Value>,
    pub changes: Option<serde_json::Value>,
    pub comment: Option<String>,
    pub reason: Option<String>,
    #[serde(alias = "requesterId")]
    pub requester_id: Option<String>,
    #[serde(alias = "workflowConfigId")]
    pub workflow_config_id: Option<Uuid>,
    pub upsert: Option<bool>,
}

impl RecordChangeRequestDto {
    pub fn get_patch_data(&self) -> serde_json::Value {
        if let Some(ref d) = self.data {
            match d {
                serde_json::Value::String(s) => {
                    serde_json::from_str(s).unwrap_or_else(|_| serde_json::json!({}))
                }
                other => other.clone(),
            }
        } else if let Some(ref c) = self.changes {
            match c {
                serde_json::Value::String(s) => {
                    if let Ok(val) = serde_json::from_str::<serde_json::Value>(s) {
                        if val.get("after").is_some() {
                            val.get("after").cloned().unwrap_or(val)
                        } else {
                            val
                        }
                    } else {
                        serde_json::json!({})
                    }
                }
                serde_json::Value::Object(map) => {
                    if let Some(after) = map.get("after") {
                        after.clone()
                    } else {
                        serde_json::Value::Object(map.clone())
                    }
                }
                other => other.clone(),
            }
        } else {
            serde_json::json!({})
        }
    }

    pub fn get_requester(&self, default: &str) -> String {
        self.requester_id
            .as_deref()
            .filter(|s| !s.trim().is_empty())
            .unwrap_or(default)
            .to_string()
    }

    pub fn get_reason(&self) -> String {
        self.comment
            .as_deref()
            .or_else(|| self.reason.as_deref())
            .filter(|s| !s.trim().is_empty())
            .unwrap_or("Record update request")
            .to_string()
    }
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
    pub empty: bool,
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
        let empty = content.is_empty();

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
            empty,
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

    #[test]
    fn test_create_record_request_deserialization() {
        // 1. Without nodeId in body
        let json1 = r#"{"data": {"item_code": "123"}}"#;
        let req1: CreateRecordRequest = serde_json::from_str(json1).unwrap();
        assert!(req1.node_id.is_none());
        assert_eq!(req1.data["item_code"], "123");

        // 2. With nodeId (camelCase)
        let json2 = r#"{"nodeId": "1617e6ad-4f04-4baf-8957-6689be1d1a80", "data": {"foo": "bar"}}"#;
        let req2: CreateRecordRequest = serde_json::from_str(json2).unwrap();
        assert_eq!(
            req2.node_id,
            Some(Uuid::parse_str("1617e6ad-4f04-4baf-8957-6689be1d1a80").unwrap())
        );

        // 3. With node_id (snake_case)
        let json3 = r#"{"node_id": "1617e6ad-4f04-4baf-8957-6689be1d1a80", "data": {}}"#;
        let req3: CreateRecordRequest = serde_json::from_str(json3).unwrap();
        assert_eq!(
            req3.node_id,
            Some(Uuid::parse_str("1617e6ad-4f04-4baf-8957-6689be1d1a80").unwrap())
        );

        // 4. With both nodeId and node_id (no duplicate field error!)
        let json4 = r#"{"nodeId": "1617e6ad-4f04-4baf-8957-6689be1d1a80", "node_id": "1617e6ad-4f04-4baf-8957-6689be1d1a80", "data": {}}"#;
        let req4: CreateRecordRequest = serde_json::from_str(json4).unwrap();
        assert_eq!(
            req4.node_id,
            Some(Uuid::parse_str("1617e6ad-4f04-4baf-8957-6689be1d1a80").unwrap())
        );
    }

    #[test]
    fn test_record_change_request_dto_parsing() {
        // Stringified data from client
        let json1 = r#"{"data": "{\"ITEM_PRICE\": 45000}", "requesterId": "siseon-sync", "comment": "Price update"}"#;
        let dto1: RecordChangeRequestDto = serde_json::from_str(json1).unwrap();
        let patch1 = dto1.get_patch_data();
        assert_eq!(patch1["ITEM_PRICE"], 45000);
        assert_eq!(dto1.get_requester("default-user"), "siseon-sync");
        assert_eq!(dto1.get_reason(), "Price update");

        // Object changes format
        let json2 = r#"{"changes": {"after": {"ITEM_NAME": "사과"}}, "reason": "Name fix"}"#;
        let dto2: RecordChangeRequestDto = serde_json::from_str(json2).unwrap();
        let patch2 = dto2.get_patch_data();
        assert_eq!(patch2["ITEM_NAME"], "사과");
        assert_eq!(dto2.get_reason(), "Name fix");
    }
}
