use chrono::NaiveDateTime;
use serde::{Deserialize, Serialize};
use uuid::Uuid;

#[derive(Debug, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct BulkImportRequest {
    pub domain_id: Uuid,
    pub node_id: Option<Uuid>,
    pub file_name: Option<String>,
    pub rows: Vec<serde_json::Value>,
}

#[derive(Debug, Serialize, Deserialize, Clone)]
#[serde(rename_all = "camelCase")]
pub struct BulkImportProgress {
    pub job_id: Uuid,
    pub domain_id: Uuid,
    pub file_name: String,
    pub status: String,
    pub total_rows: i32,
    pub processed_rows: i32,
    pub success_count: i32,
    pub error_count: i32,
    pub progress_percentage: f64,
    pub error_details: Vec<BulkImportErrorDetail>,
    pub created_at: Option<NaiveDateTime>,
    pub completed_at: Option<NaiveDateTime>,
}

#[derive(Debug, Serialize, Deserialize, Clone)]
#[serde(rename_all = "camelCase")]
pub struct BulkImportErrorDetail {
    pub row_number: i32,
    pub record_key: Option<String>,
    pub error_message: String,
}

#[derive(Debug, Deserialize)]
pub struct ExportTemplateQuery {
    pub node_id: Option<Uuid>,
    #[serde(default = "default_lang")]
    pub lang: String,
}

#[derive(Debug, Deserialize)]
pub struct ExportRecordsQuery {
    pub node_id: Option<Uuid>,
    #[serde(default = "default_lang")]
    pub lang: String,
    pub format: Option<String>,
}

fn default_lang() -> String {
    "ko".to_string()
}
