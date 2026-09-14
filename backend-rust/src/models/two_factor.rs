use chrono::{DateTime, Utc};
use serde::{Deserialize, Serialize};

#[derive(Debug, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct TwoFactorStatusResponse {
    pub two_factor_enabled: bool,
    pub two_factor_type: Option<String>,
    pub has_backup_codes: bool,
    pub remaining_backup_codes_count: usize,
    pub role: Option<String>,
    pub mandatory: bool,
    pub grace_until: Option<chrono::NaiveDateTime>,
    pub grace_period_remaining_days: Option<i64>,
    pub masked_email: Option<String>,
}

#[derive(Debug, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct TwoFactorSetupResponse {
    pub secret: String,
    pub qr_code_url: String,
    pub backup_codes: Vec<String>,
}

#[derive(Debug, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct TwoFactorEnableRequest {
    pub secret: String,
    pub code: String,
    #[serde(rename = "type")]
    pub auth_type: Option<String>,
}

#[derive(Debug, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct TwoFactorEnableResponse {
    pub success: bool,
    pub backup_codes: Vec<String>,
    pub message: String,
}

#[derive(Debug, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct TwoFactorVerifyRequest {
    pub username: String,
    pub temp_token: String,
    pub code: String,
    #[serde(rename = "type")]
    pub auth_type: Option<String>,
}

#[derive(Debug, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct TwoFactorSendEmailRequest {
    pub username: Option<String>,
    pub temp_token: Option<String>,
}
