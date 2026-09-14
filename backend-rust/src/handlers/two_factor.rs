use crate::error::AppError;
use crate::middleware::auth::AuthUser;
use crate::models::two_factor::*;
use crate::models::user::LoginResponse;
use crate::repositories::user_repo::UserRepository;
use crate::services::auth_service::AuthService;
use crate::state::AppState;
use axum::http::StatusCode;
use axum::{
    extract::{Query, State},
    Json,
};
use serde::Deserialize;

#[derive(Debug, Deserialize)]
pub struct UsernameQuery {
    pub username: Option<String>,
}

pub async fn get_status(
    State(state): State<AppState>,
    Query(params): Query<UsernameQuery>,
) -> Result<Json<TwoFactorStatusResponse>, AppError> {
    let username = params
        .username
        .ok_or_else(|| AppError::BadRequest("Username query parameter is required".to_string()))?;

    let status = state.two_factor_service.get_status(&username).await?;
    Ok(Json(status))
}

pub async fn setup(
    State(state): State<AppState>,
    auth: AuthUser,
) -> Result<Json<TwoFactorSetupResponse>, AppError> {
    let resp = state.two_factor_service.setup(&auth.username).await?;
    Ok(Json(resp))
}

pub async fn enable(
    State(state): State<AppState>,
    auth: AuthUser,
    Json(req): Json<TwoFactorEnableRequest>,
) -> Result<Json<TwoFactorEnableResponse>, AppError> {
    let resp = state.two_factor_service.enable(&auth.username, req).await?;
    Ok(Json(resp))
}

pub async fn disable(
    State(state): State<AppState>,
    Query(params): Query<UsernameQuery>,
) -> Result<Json<serde_json::Value>, AppError> {
    let username = params
        .username
        .ok_or_else(|| AppError::BadRequest("Username query parameter is required".to_string()))?;

    state.two_factor_service.disable(&username).await?;
    Ok(Json(serde_json::json!({
        "success": true,
        "message": "Two-factor authentication disabled"
    })))
}

pub async fn verify(
    State(state): State<AppState>,
    Json(req): Json<TwoFactorVerifyRequest>,
) -> Result<Json<LoginResponse>, AppError> {
    if !state
        .two_factor_service
        .validate_and_consume_temp_token(&req.temp_token, &req.username)
        .await
    {
        return Err(AppError::Unauthorized("Invalid or expired temporary 2FA token".to_string()));
    }

    let user = UserRepository::find_by_username(&state.pool, &req.username)
        .await?
        .ok_or_else(|| AppError::Unauthorized("User not found".to_string()))?;

    let auth_type = req.auth_type.unwrap_or_else(|| "TOTP".to_string()).to_uppercase();
    let is_valid = match auth_type.as_str() {
        "TOTP" => {
            let secret = user.two_factor_secret.as_deref().unwrap_or("");
            state.two_factor_service.verify_totp(secret, &req.code)
        }
        "EMAIL" => {
            state.two_factor_service.verify_email_otp(&user.username.clone().unwrap_or_default(), &req.code).await
        }
        "BACKUP_CODE" => {
            state.two_factor_service.verify_backup_code(&user, &req.code).await?
        }
        _ => false,
    };

    if !is_valid {
        return Err(AppError::Unauthorized("Invalid 2FA verification code".to_string()));
    }

    let (access_token, refresh_token) = AuthService::generate_tokens(&user, &state.config)?;
    let permissions = AuthService::get_permissions_for_role(user.role.as_deref());

    // Record login log
    let _ = sqlx::query(
        r#"
        INSERT INTO login_log (username, user_id, login_at, two_factor_status, two_factor_type)
        VALUES ($1, $2, NOW(), 'SUCCESS', $3)
        "#
    )
    .bind(&user.username)
    .bind(&user.id)
    .bind(&auth_type)
    .execute(&state.pool)
    .await;

    Ok(Json(LoginResponse {
        token: Some(access_token),
        refresh_token: Some(refresh_token),
        username: user.username.clone(),
        role: user.role.clone(),
        id: Some(user.id.clone()),
        user_id: Some(user.id.clone()),
        organization_id: user.organization_id,
        department_id: user.department_id,
        timezone: user.timezone.clone(),
        server_offset: Some("+09:00".to_string()),
        permissions,
        must_change_password: user.must_change_password,
        two_factor_required: Some(false),
        temp_token: None,
    }))
}

pub async fn send_email_otp(
    State(state): State<AppState>,
    Json(req): Json<TwoFactorSendEmailRequest>,
) -> Result<Json<serde_json::Value>, AppError> {
    let username = req.username.ok_or_else(|| AppError::BadRequest("Username is required".to_string()))?;
    state.two_factor_service.send_email_otp(&username).await?;

    Ok(Json(serde_json::json!({
        "success": true,
        "message": "Verification code sent to email"
    })))
}
