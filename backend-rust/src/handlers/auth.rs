use crate::error::AppError;
use crate::middleware::auth::AuthUser;
use crate::models::user::{LoginRequest, LoginResponse};
use crate::repositories::user_repo::UserRepository;
use crate::services::auth_service::AuthService;
use crate::state::AppState;
use axum::http::StatusCode;
use axum::{
    extract::{Query, State},
    http::HeaderMap,
    Json,
};

pub async fn login(
    State(state): State<AppState>,
    Json(req): Json<LoginRequest>,
) -> Result<Json<LoginResponse>, AppError> {
    let response =
        AuthService::login(&state.db, &state.config, &state.two_factor_service, req).await?;
    Ok(Json(response))
}

pub async fn get_current_user(
    State(state): State<AppState>,
    auth: AuthUser,
) -> Result<Json<LoginResponse>, AppError> {
    let user = UserRepository::find_by_username(&state.db, &auth.claims.sub)
        .await?
        .ok_or_else(|| AppError::NotFound("User not found".to_string()))?;

    let permissions = AuthService::get_user_permissions(&state.db, &user).await;

    Ok(Json(LoginResponse {
        token: None,
        refresh_token: None,
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

#[derive(serde::Deserialize)]
pub struct RefreshTokenRequest {
    #[serde(alias = "refreshToken", alias = "refresh_token")]
    pub refresh_token: Option<String>,
}

pub async fn refresh_token(
    State(state): State<AppState>,
    Json(req): Json<RefreshTokenRequest>,
) -> Result<Json<serde_json::Value>, AppError> {
    let token_str = req
        .refresh_token
        .ok_or_else(|| AppError::BadRequest("refreshToken is required".to_string()))?;

    let (access_token, new_refresh_token) =
        AuthService::refresh_tokens(&state.db, &state.config, &token_str).await?;

    Ok(Json(serde_json::json!({
        "token": access_token.clone(),
        "accessToken": access_token,
        "refreshToken": new_refresh_token
    })))
}

#[derive(serde::Deserialize)]
pub struct RecordLoginRequest {
    pub username: Option<String>,
    #[serde(alias = "clientIp")]
    pub client_ip: Option<String>,
    #[serde(alias = "userAgent")]
    pub user_agent: Option<String>,
}

pub async fn record_login(
    State(state): State<AppState>,
    headers: HeaderMap,
    Json(req): Json<RecordLoginRequest>,
) -> Result<Json<serde_json::Value>, AppError> {
    let username = req.username.unwrap_or_else(|| "anonymous".to_string());
    let ip = req.client_ip.filter(|s| !s.is_empty()).unwrap_or_else(|| {
        if let Some(xff) = headers.get("x-forwarded-for").and_then(|v| v.to_str().ok()) {
            if let Some(first) = xff.split(',').next() {
                let s = first.trim();
                if !s.is_empty() {
                    return s.to_string();
                }
            }
        }
        if let Some(rip) = headers.get("x-real-ip").and_then(|v| v.to_str().ok()) {
            let s = rip.trim();
            if !s.is_empty() {
                return s.to_string();
            }
        }
        "127.0.0.1".to_string()
    });
    let ua = req.user_agent.filter(|s| !s.is_empty()).or_else(|| {
        headers
            .get(axum::http::header::USER_AGENT)
            .and_then(|v| v.to_str().ok())
            .map(|s| s.to_string())
    });

    let user_id = UserRepository::find_by_username(&state.db, &username)
        .await
        .ok()
        .flatten()
        .map(|u| u.id);

    let _ = sqlx::query(
        r#"
        INSERT INTO login_log (username, user_id, client_ip, user_agent, login_at)
        VALUES ($1, $2, $3, $4, NOW())
        "#,
    )
    .bind(username)
    .bind(user_id)
    .bind(ip)
    .bind(ua)
    .execute(&state.db)
    .await;

    Ok(Json(serde_json::json!({ "status": "success" })))
}

#[derive(Debug, serde::Deserialize)]
pub struct LoginLogQuery {
    #[serde(default)]
    pub page: Option<i64>,
    #[serde(default)]
    pub size: Option<i64>,
}

pub async fn get_login_logs(
    State(state): State<AppState>,
    Query(params): Query<LoginLogQuery>,
    _auth: AuthUser,
) -> Result<Json<serde_json::Value>, AppError> {
    let page = params.page.unwrap_or(0);
    let size = params.size.unwrap_or(20);
    let offset = page * size;

    let rows: Vec<serde_json::Value> = sqlx::query_as::<
        _,
        (
            i64,
            Option<String>,
            String,
            Option<String>,
            Option<String>,
            chrono::NaiveDateTime,
        ),
    >(
        r#"
        SELECT id, user_id, username, client_ip, user_agent, login_at
        FROM login_log
        ORDER BY login_at DESC
        LIMIT $1 OFFSET $2
        "#,
    )
    .bind(size)
    .bind(offset)
    .fetch_all(&state.db)
    .await
    .unwrap_or_default()
    .into_iter()
    .map(|(id, uid, username, ip, ua, at)| {
        serde_json::json!({
            "id": id,
            "userId": uid,
            "username": username,
            "clientIp": ip,
            "userAgent": ua,
            "loginAt": at
        })
    })
    .collect();

    let total: (i64,) = sqlx::query_as("SELECT COUNT(*) FROM login_log")
        .fetch_one(&state.db)
        .await
        .unwrap_or((0,));

    let total_pages = if size > 0 {
        (total.0 + size - 1) / size
    } else {
        0
    };

    Ok(Json(serde_json::json!({
        "content": rows,
        "totalElements": total.0,
        "totalPages": total_pages,
        "size": size,
        "number": page
    })))
}

#[derive(serde::Deserialize)]
pub struct SelfRegisterRequest {
    pub username: String,
    pub password: String,
    pub email: Option<String>,
    pub timezone: Option<String>,
}

pub async fn self_register(
    State(state): State<AppState>,
    Json(req): Json<SelfRegisterRequest>,
) -> Result<Json<serde_json::Value>, AppError> {
    let user_id = uuid::Uuid::new_v4().to_string();
    let hashed = bcrypt::hash(&req.password, 10)
        .map_err(|e| AppError::Internal(format!("Password hashing failed: {e}")))?;

    sqlx::query(
        r#"
        INSERT INTO users (id, username, password, email, role, is_active, timezone, created_at, updated_at)
        VALUES ($1, $2, $3, $4, 'ROLE_USER', true, $5, NOW(), NOW())
        "#,
    )
    .bind(&user_id)
    .bind(&req.username)
    .bind(&hashed)
    .bind(&req.email)
    .bind(req.timezone.as_deref().unwrap_or("Asia/Seoul"))
    .execute(&state.db)
    .await?;

    Ok(Json(serde_json::json!({
        "status": "success",
        "message": "User registered successfully"
    })))
}

pub async fn logout() -> Result<Json<serde_json::Value>, AppError> {
    Ok(Json(serde_json::json!({
        "status": "success",
        "message": "Logged out successfully"
    })))
}

#[derive(Debug, serde::Deserialize)]
pub struct CheckUsernameQuery {
    pub username: Option<String>,
}

pub async fn check_username(
    State(state): State<AppState>,
    Query(query): Query<CheckUsernameQuery>,
) -> Result<Json<serde_json::Value>, AppError> {
    let username = query.username.unwrap_or_default();
    let exists: bool = sqlx::query_scalar("SELECT EXISTS(SELECT 1 FROM users WHERE username = $1)")
        .bind(&username)
        .fetch_one(&state.db)
        .await
        .unwrap_or(false);

    Ok(Json(serde_json::json!({
        "available": !exists,
        "exists": exists
    })))
}
