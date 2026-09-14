use crate::error::AppError;
use crate::middleware::auth::AuthUser;
use crate::models::user_mgmt::{AdminUserUpdateDto, ResetPasswordRequest, SelfUserUpdateDto, UserDto};
use crate::services::user_service::UserService;
use crate::state::AppState;
use axum::http::StatusCode;
use axum::{
    extract::{Path, State},
    Json,
};

pub async fn get_all_users(
    State(state): State<AppState>,
    _auth: AuthUser,
) -> Result<Json<Vec<UserDto>>, AppError> {
    let users = UserService::get_all_users(&state.db).await?;
    Ok(Json(users))
}

pub async fn update_self(
    State(state): State<AppState>,
    auth: AuthUser,
    Json(dto): Json<SelfUserUpdateDto>,
) -> Result<Json<UserDto>, AppError> {
    let user = UserService::update_self_user(&state.db, &auth.claims.sub, dto).await?;
    Ok(Json(user))
}

pub async fn update_user(
    State(state): State<AppState>,
    Path(id): Path<String>,
    _auth: AuthUser,
    Json(dto): Json<AdminUserUpdateDto>,
) -> Result<Json<UserDto>, AppError> {
    let user = UserService::update_admin_user(&state.db, &id, dto).await?;
    Ok(Json(user))
}

pub async fn reset_password(
    State(state): State<AppState>,
    Path(id): Path<String>,
    _auth: AuthUser,
    Json(req): Json<ResetPasswordRequest>,
) -> Result<Json<serde_json::Value>, AppError> {
    UserService::reset_password(&state.db, &id, req).await?;
    Ok(Json(serde_json::json!({
        "status": "SUCCESS",
        "message": "Password has been successfully reset"
    })))
}

pub async fn delete_user(
    State(state): State<AppState>,
    Path(id): Path<String>,
    _auth: AuthUser,
) -> Result<Json<serde_json::Value>, AppError> {
    UserService::deactivate_user(&state.db, &id).await?;
    Ok(Json(serde_json::json!({
        "status": "SUCCESS",
        "message": "User has been deactivated"
    })))
}

pub async fn get_user_map(
    State(state): State<AppState>,
    _auth: AuthUser,
) -> Result<Json<std::collections::HashMap<String, String>>, AppError> {
    let users = UserService::get_all_users(&state.db).await?;
    let mut map = std::collections::HashMap::new();
    for u in users {
        let name = u.username.clone().unwrap_or_else(|| u.id.clone());
        map.insert(u.id.clone(), name.clone());
        if let Some(un) = u.username {
            map.insert(un, name);
        }
    }
    Ok(Json(map))
}

#[derive(serde::Deserialize)]
pub struct ChangePasswordDto {
    #[serde(alias = "oldPassword")]
    pub old_password: Option<String>,
    #[serde(alias = "newPassword")]
    pub new_password: Option<String>,
}

pub async fn change_my_password(
    State(state): State<AppState>,
    auth: AuthUser,
    Json(dto): Json<ChangePasswordDto>,
) -> Result<Json<serde_json::Value>, AppError> {
    let new_pass = dto.new_password.ok_or_else(|| AppError::BadRequest("newPassword is required".to_string()))?;
    let hashed = bcrypt::hash(&new_pass, 10)
        .map_err(|e| AppError::Internal(format!("Hashing failed: {e}")))?;

    sqlx::query("UPDATE users SET password = $1, must_change_password = false, updated_at = NOW() WHERE username = $2")
        .bind(&hashed)
        .bind(&auth.username)
        .execute(&state.db)
        .await?;

    Ok(Json(serde_json::json!({ "status": "SUCCESS" })))
}

#[derive(serde::Deserialize)]
pub struct TimezoneRequest {
    pub timezone: String,
}

pub async fn update_timezone(
    State(state): State<AppState>,
    auth: AuthUser,
    Json(req): Json<TimezoneRequest>,
) -> Result<Json<serde_json::Value>, AppError> {
    sqlx::query("UPDATE users SET timezone = $1, updated_at = NOW() WHERE username = $2")
        .bind(&req.timezone)
        .bind(&auth.username)
        .execute(&state.db)
        .await?;

    Ok(Json(serde_json::json!({ "status": "SUCCESS" })))
}

pub async fn get_user_org_history(
    State(state): State<AppState>,
    Path(id): Path<String>,
    _auth: AuthUser,
) -> Result<Json<Vec<serde_json::Value>>, AppError> {
    let rows: Vec<(i64, String, chrono::NaiveDateTime, Option<String>)> = sqlx::query_as(
        r#"
        SELECT id, user_id, changed_at, changed_by
        FROM user_org_history
        WHERE user_id = $1
        ORDER BY changed_at DESC
        "#,
    )
    .bind(&id)
    .fetch_all(&state.db)
    .await
    .unwrap_or_default();

    let list = rows
        .into_iter()
        .map(|(hid, uid, at, by)| {
            serde_json::json!({
                "id": hid,
                "userId": uid,
                "changedAt": at,
                "changedBy": by
            })
        })
        .collect();

    Ok(Json(list))
}

#[derive(Debug, serde::Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct CreateAdminUserRequest {
    pub username: String,
    pub email: Option<String>,
    pub role: Option<String>,
    pub organization_id: Option<uuid::Uuid>,
    pub department_id: Option<uuid::Uuid>,
    pub team_id: Option<uuid::Uuid>,
    pub timezone: Option<String>,
}

pub async fn create_user(
    State(state): State<AppState>,
    _auth: AuthUser,
    Json(req): Json<CreateAdminUserRequest>,
) -> Result<Json<serde_json::Value>, AppError> {
    use rand::Rng;
    let temp_password: String = rand::thread_rng()
        .sample_iter(&rand::distributions::Alphanumeric)
        .take(10)
        .map(char::from)
        .collect();

    let user_id = uuid::Uuid::new_v4().to_string();
    let hashed = bcrypt::hash(&temp_password, 10)
        .map_err(|e| AppError::Internal(format!("Password hash failed: {e}")))?;

    let role_val = req.role.unwrap_or_else(|| "ROLE_USER".to_string());

    sqlx::query(
        r#"
        INSERT INTO users (
            id, username, password, email, role, organization_id, department_id, team_id,
            is_active, must_change_password, encrypted_temp_password, timezone
        ) VALUES (
            $1, $2, $3, $4, $5, $6, $7, $8,
            true, true, $9, $10
        )
        "#
    )
    .bind(&user_id)
    .bind(&req.username)
    .bind(&hashed)
    .bind(&req.email)
    .bind(&role_val)
    .bind(req.organization_id)
    .bind(req.department_id)
    .bind(req.team_id)
    .bind(&temp_password)
    .bind(req.timezone.as_deref().unwrap_or("Asia/Seoul"))
    .execute(&state.db)
    .await?;

    Ok(Json(serde_json::json!({
        "username": req.username,
        "tempPassword": temp_password
    })))
}

pub async fn get_temp_password(
    State(state): State<AppState>,
    Path(id): Path<String>,
    _auth: AuthUser,
) -> Result<Json<serde_json::Value>, AppError> {
    let row: Option<(Option<String>, Option<bool>)> = sqlx::query_as(
        "SELECT encrypted_temp_password, must_change_password FROM users WHERE id = $1 OR username = $1"
    )
    .bind(&id)
    .fetch_optional(&state.db)
    .await?;

    match row {
        Some((Some(pwd), Some(true))) => Ok(Json(serde_json::json!({ "tempPassword": pwd }))),
        _ => Err(AppError::BadRequest("Temporary password not available".to_string())),
    }
}
