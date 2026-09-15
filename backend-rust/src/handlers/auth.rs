use crate::error::AppError;
use crate::middleware::auth::{AuthUser, OptionalAuthUser};
use crate::models::user::{LoginRequest, LoginResponse};
use crate::repositories::user_repo::UserRepository;
use crate::services::auth_service::AuthService;
use crate::state::AppState;
use axum::http::{header, HeaderMap, StatusCode};
use axum::response::Response;
use axum::{
    extract::{Query, State},
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
    auth: AuthUser,
) -> Result<Json<serde_json::Value>, AppError> {
    let page = params.page.unwrap_or(0);
    let size = params.size.unwrap_or(20);
    let offset = page * size;

    let (rows_raw, total_count): (
        Vec<(
            i64,
            Option<String>,
            String,
            Option<String>,
            Option<String>,
            chrono::NaiveDateTime,
        )>,
        i64,
    ) = if let Some(org_id) = auth.organization_id {
        let rows = sqlx::query_as::<
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
            WHERE (
                user_id IN (SELECT id::text FROM users WHERE organization_id = $1)
                OR username IN (SELECT username FROM users WHERE organization_id = $1)
            )
            ORDER BY login_at DESC
            LIMIT $2 OFFSET $3
            "#,
        )
        .bind(org_id)
        .bind(size)
        .bind(offset)
        .fetch_all(&state.db)
        .await
        .unwrap_or_default();

        let total: (i64,) = sqlx::query_as(
            r#"
            SELECT COUNT(*) FROM login_log
            WHERE (
                user_id IN (SELECT id::text FROM users WHERE organization_id = $1)
                OR username IN (SELECT username FROM users WHERE organization_id = $1)
            )
            "#,
        )
        .bind(org_id)
        .fetch_one(&state.db)
        .await
        .unwrap_or((0,));

        (rows, total.0)
    } else {
        let rows = sqlx::query_as::<
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
        .unwrap_or_default();

        let total: (i64,) = sqlx::query_as("SELECT COUNT(*) FROM login_log")
            .fetch_one(&state.db)
            .await
            .unwrap_or((0,));

        (rows, total.0)
    };

    let rows: Vec<serde_json::Value> = rows_raw
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

    let total_pages = if size > 0 {
        (total_count + size - 1) / size
    } else {
        0
    };

    Ok(Json(serde_json::json!({
        "content": rows,
        "totalElements": total_count,
        "totalPages": total_pages,
        "size": size,
        "number": page
    })))
}

#[derive(serde::Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct SelfRegisterRequest {
    pub username: String,
    pub password: String,
    pub email: Option<String>,
    #[serde(alias = "company_name")]
    pub company_name: Option<String>,
    #[serde(alias = "template_category")]
    pub template_category: Option<String>,
    #[serde(alias = "terms_agreed")]
    pub terms_agreed: Option<bool>,
    pub timezone: Option<String>,
}

pub async fn self_register(
    State(state): State<AppState>,
    Json(req): Json<SelfRegisterRequest>,
) -> Result<Json<serde_json::Value>, AppError> {
    let username = req.username.trim();
    if username.is_empty() {
        return Err(AppError::BadRequest("Username is required".to_string()));
    }
    if req.password.is_empty() {
        return Err(AppError::BadRequest("Password is required".to_string()));
    }

    let exists: bool = sqlx::query_scalar("SELECT EXISTS(SELECT 1 FROM users WHERE username = $1)")
        .bind(username)
        .fetch_one(&state.db)
        .await
        .unwrap_or(false);

    if exists {
        return Err(AppError::BadRequest("Username already exists".to_string()));
    }

    let org_name = req
        .company_name
        .as_deref()
        .map(|s| s.trim())
        .filter(|s| !s.is_empty())
        .unwrap_or("Default Organization");

    let org_id = uuid::Uuid::new_v4();
    let org_result = sqlx::query_scalar::<_, uuid::Uuid>(
        r#"
        INSERT INTO organization (id, name, display_name, is_active, created_at, updated_at)
        VALUES ($1, $2, $2, true, NOW(), NOW())
        ON CONFLICT (name) DO UPDATE SET updated_at = NOW()
        RETURNING id
        "#,
    )
    .bind(org_id)
    .bind(org_name)
    .fetch_one(&state.db)
    .await;

    let final_org_id = match org_result {
        Ok(id) => id,
        Err(_) => {
            let unique_name = format!("{} ({})", org_name, &org_id.to_string()[..8]);
            sqlx::query_scalar::<_, uuid::Uuid>(
                r#"
                INSERT INTO organization (id, name, display_name, is_active, created_at, updated_at)
                VALUES ($1, $2, $2, true, NOW(), NOW())
                RETURNING id
                "#,
            )
            .bind(org_id)
            .bind(&unique_name)
            .fetch_one(&state.db)
            .await
            .unwrap_or(org_id)
        }
    };

    let user_id = uuid::Uuid::new_v4().to_string();
    let hashed = bcrypt::hash(&req.password, 10)
        .map_err(|e| AppError::Internal(format!("Password hashing failed: {e}")))?;
    let role = "ROLE_ADMIN,ORG_ADMIN".to_string();
    let tz = req.timezone.as_deref().unwrap_or("Asia/Seoul");

    sqlx::query(
        r#"
        INSERT INTO users (
            id, username, password, email, role, is_active, timezone, organization_id, must_change_password, failed_login_count, two_factor_enabled
        )
        VALUES ($1, $2, $3, $4, $5, true, $6, $7, false, 0, false)
        "#,
    )
    .bind(&user_id)
    .bind(username)
    .bind(&hashed)
    .bind(&req.email)
    .bind(&role)
    .bind(tz)
    .bind(final_org_id)
    .execute(&state.db)
    .await?;

    // 3. Provision Company Domain, Axis, Root Node, and Permission
    let category = req
        .template_category
        .as_deref()
        .map(|s| s.trim().to_uppercase())
        .unwrap_or_else(|| "CUSTOMER".to_string());

    let (domain_name, icon, numbering_pattern, axis_code, axis_name, root_node_name) = match category.as_str() {
        "LEASE_CONTRACT" => (
            serde_json::json!({
                "ko": format!("{org_name} 임대차 계약 마스터"),
                "en": format!("{org_name} Lease Contract Master")
            }),
            "apartment",
            "LC-{YYYY}-{SEQ:6}",
            "CONTRACT_TYPE",
            serde_json::json!({ "ko": "계약 구분", "en": "Contract Type" }),
            serde_json::json!({ "ko": "전체 계약", "en": "All Contracts" })
        ),
        "PRODUCT" => (
            serde_json::json!({
                "ko": format!("{org_name} 상품 마스터"),
                "en": format!("{org_name} Product Master")
            }),
            "inventory_2",
            "PROD-{YYYY}-{SEQ:6}",
            "PRODUCT_CATEGORY",
            serde_json::json!({ "ko": "상품 분류", "en": "Product Category" }),
            serde_json::json!({ "ko": "전체 상품", "en": "All Products" })
        ),
        _ => (
            serde_json::json!({
                "ko": format!("{org_name} 고객 마스터"),
                "en": format!("{org_name} Customer Master")
            }),
            "person_pin",
            "CUST-{YYYY}-{SEQ:6}",
            "CUSTOMER_TYPE",
            serde_json::json!({ "ko": "고객 구분", "en": "Customer Type" }),
            serde_json::json!({ "ko": "전체 고객", "en": "All Customers" })
        ),
    };

    let domain_id = uuid::Uuid::new_v4();
    let domain_desc = serde_json::json!({
        "ko": format!("{org_name} 전용 마스터 데이터 도메인"),
        "en": format!("{org_name} Dedicated Master Data Domain")
    });

    let _ = sqlx::query(
        r#"
        INSERT INTO domain (
            id, name, description, icon, domain_type, specialized_category,
            organization_id, auto_dq_scan_enabled, current_sequence, sort_order, numbering_pattern,
            created_at, updated_at
        )
        VALUES ($1, $2, $3, $4, 'SPECIALIZED', $5, $6, true, 0, 1, $7, NOW(), NOW())
        "#,
    )
    .bind(domain_id)
    .bind(domain_name)
    .bind(domain_desc)
    .bind(icon)
    .bind(&category)
    .bind(final_org_id)
    .bind(numbering_pattern)
    .execute(&state.db)
    .await;

    // Create Classification Axis
    let axis_id = uuid::Uuid::new_v4();
    let _ = sqlx::query(
        r#"
        INSERT INTO classification_axis (
            id, domain_id, name, axis_code, is_default, sort_order, created_at, updated_at
        )
        VALUES ($1, $2, $3, $4, true, 1, NOW(), NOW())
        "#,
    )
    .bind(axis_id)
    .bind(domain_id)
    .bind(axis_name)
    .bind(axis_code)
    .execute(&state.db)
    .await;

    // Create Root Node (Depth 0)
    let root_node_id = uuid::Uuid::new_v4();
    let root_path = format!("/{}", axis_code.to_lowercase());
    let _ = sqlx::query(
        r#"
        INSERT INTO classification_node (
            id, domain_id, axis_id, parent_id, name, path, depth, node_order, is_deleted, icon, created_at, updated_at
        )
        VALUES ($1, $2, $3, NULL, $4, $5, 0, 1, false, $6, NOW(), NOW())
        "#,
    )
    .bind(root_node_id)
    .bind(domain_id)
    .bind(axis_id)
    .bind(root_node_name)
    .bind(root_path)
    .bind(icon)
    .execute(&state.db)
    .await;

    // Create Domain Permission for the new user
    let perm_id = uuid::Uuid::new_v4();
    let _ = sqlx::query(
        r#"
        INSERT INTO domain_permission (id, domain_id, user_id, created_at)
        VALUES ($1, $2, $3, NOW())
        "#,
    )
    .bind(perm_id)
    .bind(domain_id)
    .bind(&user_id)
    .execute(&state.db)
    .await;

    let user = UserRepository::find_by_username(&state.db, username)
        .await?
        .ok_or_else(|| AppError::Internal("Failed to retrieve created user".to_string()))?;

    let permissions = AuthService::get_user_permissions(&state.db, &user).await;
    let (access_token, refresh_token) =
        AuthService::generate_tokens_with_permissions(&user, Some(permissions.clone()), &state.config)?;

    let server_offset = "+09:00".to_string();

    Ok(Json(serde_json::json!({
        "status": "success",
        "message": "User registered successfully",
        "token": access_token,
        "accessToken": access_token,
        "refreshToken": refresh_token,
        "username": user.username,
        "role": user.role,
        "id": user.id,
        "userId": user.id,
        "organizationId": user.organization_id,
        "departmentId": user.department_id,
        "timezone": user.timezone,
        "serverOffset": server_offset,
        "permissions": permissions,
        "mustChangePassword": false,
        "twoFactorRequired": false
    })))
}

#[derive(Debug, serde::Deserialize, Default)]
pub struct LogoutRequest {
    #[serde(alias = "refreshToken", default)]
    pub refresh_token: Option<String>,
}

pub async fn logout(
    State(state): State<AppState>,
    headers: HeaderMap,
    OptionalAuthUser(auth): OptionalAuthUser,
    body_bytes: axum::body::Bytes,
) -> Result<Response, AppError> {
    let req: Option<LogoutRequest> = serde_json::from_slice(&body_bytes).ok();

    // 1. Extract refresh_token from body or cookie
    let refresh_token = req
        .and_then(|r| r.refresh_token)
        .filter(|rt| !rt.trim().is_empty())
        .or_else(|| {
            headers
                .get(header::COOKIE)
                .and_then(|v| v.to_str().ok())
                .and_then(|cookie_header| {
                    for cookie in cookie_header.split(';') {
                        let mut kv = cookie.trim().splitn(2, '=');
                        if let (Some(name), Some(val)) = (kv.next(), kv.next()) {
                            if name == "refresh_token" && !val.is_empty() {
                                return Some(val.to_string());
                            }
                        }
                    }
                    None
                })
        });

    // 2. Keycloak backchannel logout if refresh_token is present
    if let Some(ref rt) = refresh_token {
        let server = std::env::var("KEYCLOAK_SERVER_URL")
            .unwrap_or_else(|_| "http://keycloak:8080/auth".to_string());
        let realm = std::env::var("KEYCLOAK_REALM").unwrap_or_else(|_| "mplatform".to_string());
        let logout_uri = format!(
            "{}/realms/{}/protocol/openid-connect/logout",
            server.trim_end_matches('/'),
            realm
        );
        let client_id =
            std::env::var("KEYCLOAK_CLIENT_ID").unwrap_or_else(|_| "mdm-frontend".to_string());
        let client_secret =
            std::env::var("KEYCLOAK_CLIENT_SECRET").unwrap_or_else(|_| "secret".to_string());

        let client = reqwest::Client::builder()
            .timeout(std::time::Duration::from_secs(5))
            .build()
            .unwrap_or_default();

        let mut form: Vec<(&str, &str)> = vec![
            ("client_id", &client_id),
            ("refresh_token", rt),
        ];
        if !client_secret.trim().is_empty() {
            form.push(("client_secret", &client_secret));
        }

        let body = form
            .iter()
            .map(|(k, v)| format!("{}={}", urlencoding::encode(k), urlencoding::encode(v)))
            .collect::<Vec<_>>()
            .join("&");

        match client
            .post(&logout_uri)
            .header(
                reqwest::header::CONTENT_TYPE,
                "application/x-www-form-urlencoded",
            )
            .body(body)
            .send()
            .await
        {
            Ok(resp) => {
                tracing::info!(
                    "[Logout] Keycloak backchannel logout HTTP {}: {}",
                    resp.status(),
                    logout_uri
                );
            }
            Err(e) => {
                tracing::warn!(
                    "[Logout] Keycloak backchannel logout request failed: {}",
                    e
                );
            }
        }
    }

    // 3. Invalidate DB active session if user was identified
    if let Some(user) = auth {
        let _ = sqlx::query(
            "UPDATE users SET active_session_id = NULL WHERE username = $1 OR id = $2",
        )
        .bind(&user.username)
        .bind(&user.user_id)
        .execute(&state.db)
        .await;
    }

    // 4. Clear cookies in response
    let is_https = headers
        .get("x-forwarded-proto")
        .and_then(|v| v.to_str().ok())
        .map(|s| s.starts_with("https"))
        .unwrap_or(false);
    let secure_flag = if is_https { "; Secure" } else { "" };

    let body_json = serde_json::json!({
        "status": "success",
        "message": "Logged out successfully"
    });

    let mut resp = Response::builder()
        .status(StatusCode::OK)
        .header(header::CONTENT_TYPE, "application/json")
        .body(axum::body::Body::from(serde_json::to_vec(&body_json).unwrap_or_default()))
        .unwrap_or_else(|_| Response::default());

    for cookie_name in &["auth_token", "token", "refresh_token", "id_token", "user_data"] {
        if let Ok(c) = format!(
            "{}=; Path=/; Max-Age=0; Expires=Thu, 01 Jan 1970 00:00:00 GMT; SameSite=Lax{}",
            cookie_name, secure_flag
        )
        .parse::<axum::http::HeaderValue>()
        {
            resp.headers_mut().append(header::SET_COOKIE, c);
        }
    }

    Ok(resp)
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
