use serde::{Deserialize, Serialize};
use uuid::Uuid;

#[derive(Debug, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct UserDto {
    pub id: String,
    pub username: Option<String>,
    pub role: Option<String>,
    pub email: Option<String>,
    pub organization_id: Option<Uuid>,
    pub department_id: Option<Uuid>,
    pub team_id: Option<Uuid>,
    pub timezone: Option<String>,
    pub is_active: Option<bool>,
    pub must_change_password: Option<bool>,
}

#[derive(Debug, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct AdminUserUpdateDto {
    pub role: Option<String>,
    pub organization_id: Option<Uuid>,
    pub department_id: Option<Uuid>,
    pub team_id: Option<Uuid>,
    pub is_active: Option<bool>,
    pub timezone: Option<String>,
    pub email: Option<String>,
}

#[derive(Debug, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct SelfUserUpdateDto {
    pub timezone: Option<String>,
    pub email: Option<String>,
    pub current_password: Option<String>,
    pub new_password: Option<String>,
}

#[derive(Debug, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct ResetPasswordRequest {
    pub new_password: String,
}
