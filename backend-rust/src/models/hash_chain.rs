use serde::{Deserialize, Serialize};
use uuid::Uuid;

#[derive(Debug, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct LedgerBlockItem {
    pub block_index: i64,
    pub record_id: Uuid,
    pub record_code: String,
    pub action_type: String,
    pub actor: String,
    pub prev_hash: String,
    pub block_hash: String,
    pub timestamp: Option<String>,
    pub valid: bool,
}

#[derive(Debug, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct LedgerVerificationResponse {
    pub total_blocks: usize,
    pub valid_blocks: usize,
    pub corrupted_blocks: usize,
    pub is_chain_intact: bool,
    pub blocks: Vec<LedgerBlockItem>,
    pub summary: String,
}
