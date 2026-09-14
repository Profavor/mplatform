use crate::models::domain::DomainResponse;
use crate::models::record::Record;
use serde::{Deserialize, Serialize};

#[derive(Debug, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct GlobalSearchResponse {
    pub query: String,
    pub total_hits: usize,
    pub total_elements: usize,
    pub domains: Vec<DomainResponse>,
    pub records: Vec<Record>,
    pub content: Vec<Record>,
}
