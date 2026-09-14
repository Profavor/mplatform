use serde::{Deserialize, Serialize};

#[derive(Debug, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct FileUploadResponse {
    pub file_name: String,
    pub url: String,
    pub size: String,
    pub file_size: String,
}

#[derive(Debug, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct FileInfoResponse {
    pub file_name: String,
    pub size: u64,
    pub content_type: String,
    pub download_url: String,
}
