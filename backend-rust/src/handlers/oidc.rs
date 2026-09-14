use axum::http::{header, HeaderMap, StatusCode};
use axum::{
    extract::{Query, State},
    response::{IntoResponse, Response},
};
use base64::{engine::general_purpose::URL_SAFE_NO_PAD, Engine as _};
use serde::{Deserialize, Serialize};
use std::collections::HashMap;
use tracing::{error, info, warn};
use uuid::Uuid;

use crate::{error::AppError, state::AppState};

#[derive(Debug, Deserialize)]
pub struct OidcLoginQuery {
    pub client: Option<String>,
    pub redirect: Option<String>,
}

#[derive(Debug, Deserialize)]
pub struct OidcCallbackQuery {
    pub code: Option<String>,
    pub state: Option<String>,
    pub error: Option<String>,
    pub error_description: Option<String>,
}

#[derive(Debug, Serialize, Deserialize)]
struct OidcState {
    client: String,
    redirect: String,
    nonce: String,
}

#[derive(Debug, Deserialize)]
struct KeycloakTokenResponse {
    access_token: String,
    refresh_token: Option<String>,
    expires_in: Option<i64>,
    session_state: Option<String>,
}

fn resolve_scheme(headers: &HeaderMap) -> String {
    if let Some(proto) = headers
        .get("x-forwarded-proto")
        .and_then(|v| v.to_str().ok())
    {
        if let Some(first) = proto.split(',').next() {
            let s = first.trim();
            if !s.is_empty() {
                return s.to_string();
            }
        }
    }
    if let Ok(p) = std::env::var("KEYCLOAK_PUBLIC_PROTO") {
        if !p.trim().is_empty() {
            return p.trim().to_string();
        }
    }
    if let Some(host) = headers
        .get("x-forwarded-host")
        .or_else(|| headers.get(header::HOST))
        .and_then(|v| v.to_str().ok())
    {
        if host.contains("mplat.store") {
            return "https".to_string();
        }
    }
    "http".to_string()
}

fn resolve_host(headers: &HeaderMap) -> String {
    if let Some(h) = headers
        .get("x-forwarded-host")
        .and_then(|v| v.to_str().ok())
    {
        if let Some(first) = h.split(',').next() {
            let s = first.trim();
            if !s.is_empty() {
                return s.to_string();
            }
        }
    }
    if let Some(h) = headers.get(header::HOST).and_then(|v| v.to_str().ok()) {
        let s = h.trim();
        if !s.is_empty() {
            return s.to_string();
        }
    }
    std::env::var("KEYCLOAK_PUBLIC_HOST").unwrap_or_else(|_| "mdm.mplat.store".to_string())
}

fn build_callback_uri(headers: &HeaderMap) -> String {
    let scheme = resolve_scheme(headers);
    let host = resolve_host(headers);
    format!("{}://{}/api/auth/oidc/callback", scheme, host)
}

fn resolve_auth_endpoint(headers: &HeaderMap) -> String {
    if let Ok(uri) =
        std::env::var("KEYCLOAK_AUTH_URI").or_else(|_| std::env::var("SPRING_KEYCLOAK_AUTH_URI"))
    {
        let trimmed = uri.trim();
        if trimmed.starts_with("http://") || trimmed.starts_with("https://") {
            return trimmed.to_string();
        }
        let scheme = resolve_scheme(headers);
        let host = resolve_host(headers);
        let path = if trimmed.starts_with('/') {
            trimmed.to_string()
        } else {
            format!("/{}", trimmed)
        };
        return format!("{}://{}{}", scheme, host, path);
    }
    let scheme = resolve_scheme(headers);
    let host = resolve_host(headers);
    let realm = std::env::var("KEYCLOAK_REALM").unwrap_or_else(|_| "mplatform".to_string());
    format!(
        "{}://{}/auth/realms/{}/protocol/openid-connect/auth",
        scheme, host, realm
    )
}

fn build_state(client: &str, redirect: &str) -> String {
    let state_obj = OidcState {
        client: if client.trim().is_empty() {
            "web".to_string()
        } else {
            client.trim().to_string()
        },
        redirect: if redirect.trim().is_empty() {
            "/dashboard".to_string()
        } else {
            redirect.trim().to_string()
        },
        nonce: Uuid::new_v4().to_string(),
    };
    let json_bytes = serde_json::to_vec(&state_obj).unwrap_or_default();
    URL_SAFE_NO_PAD.encode(json_bytes)
}

fn parse_state(state: Option<&str>) -> (String, String) {
    let state_str = match state {
        Some(s) if !s.trim().is_empty() => s.trim(),
        _ => return ("web".to_string(), "/dashboard".to_string()),
    };

    let mut padded = state_str.to_string();
    while padded.len() % 4 != 0 {
        padded.push('=');
    }

    let decoded = base64::engine::general_purpose::URL_SAFE
        .decode(padded.as_bytes())
        .or_else(|_| URL_SAFE_NO_PAD.decode(state_str.as_bytes()))
        .or_else(|_| base64::engine::general_purpose::STANDARD.decode(padded.as_bytes()));

    if let Ok(bytes) = decoded {
        if let Ok(obj) = serde_json::from_slice::<HashMap<String, String>>(&bytes) {
            let client = obj
                .get("client")
                .cloned()
                .unwrap_or_else(|| "web".to_string());
            let redirect = obj
                .get("redirect")
                .cloned()
                .unwrap_or_else(|| "/dashboard".to_string());
            return (client, redirect);
        }
    }

    ("web".to_string(), "/dashboard".to_string())
}

fn extract_client_ip(headers: &HeaderMap) -> String {
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
}

fn decode_jwt_payload(token: &str) -> Option<serde_json::Value> {
    let parts: Vec<&str> = token.split('.').collect();
    if parts.len() < 2 {
        return None;
    }
    let mut payload_b64 = parts[1].to_string();
    while payload_b64.len() % 4 != 0 {
        payload_b64.push('=');
    }
    let decoded = base64::engine::general_purpose::URL_SAFE
        .decode(payload_b64.as_bytes())
        .or_else(|_| URL_SAFE_NO_PAD.decode(parts[1].as_bytes()))
        .or_else(|_| base64::engine::general_purpose::STANDARD.decode(payload_b64.as_bytes()))
        .ok()?;
    serde_json::from_slice(&decoded).ok()
}

fn extract_username(payload: &serde_json::Value) -> Option<String> {
    for key in &["preferred_username", "clientId", "client_id", "azp", "sub"] {
        if let Some(val) = payload.get(*key).and_then(|v| v.as_str()) {
            if !val.trim().is_empty() {
                return Some(val.trim().to_string());
            }
        }
    }
    None
}

/// GET /api/auth/oidc/login
pub async fn oidc_login(
    Query(query): Query<OidcLoginQuery>,
    headers: HeaderMap,
) -> impl IntoResponse {
    let client = query.client.unwrap_or_else(|| "web".to_string());
    let redirect = query.redirect.unwrap_or_else(|| "/dashboard".to_string());
    let callback_uri = build_callback_uri(&headers);
    let state = build_state(&client, &redirect);
    let auth_endpoint = resolve_auth_endpoint(&headers);
    let client_id =
        std::env::var("KEYCLOAK_CLIENT_ID").unwrap_or_else(|_| "mdm-frontend".to_string());

    let redirect_url = format!(
        "{}?client_id={}&response_type=code&redirect_uri={}&state={}&scope=openid%20profile%20email",
        auth_endpoint,
        urlencoding::encode(&client_id),
        urlencoding::encode(&callback_uri),
        urlencoding::encode(&state),
    );

    info!(
        "[OIDC Login] Initiating login for client: {}, target: {}, redirect_uri: {}",
        client, redirect, callback_uri
    );

    Response::builder()
        .status(StatusCode::FOUND)
        .header(header::LOCATION, redirect_url)
        .body(axum::body::Body::empty())
        .unwrap()
}

/// GET /api/auth/oidc/callback
pub async fn oidc_callback(
    State(state): State<AppState>,
    Query(query): Query<OidcCallbackQuery>,
    headers: HeaderMap,
) -> impl IntoResponse {
    let (client, redirect) = parse_state(query.state.as_deref());

    // 1. Check Keycloak error parameter
    if let Some(err) = query.error {
        let err_desc = query.error_description.as_deref().unwrap_or("");
        warn!(
            "[OIDC Callback] Error from Keycloak: {} - {}",
            err, err_desc
        );
        let encoded_err = urlencoding::encode(&err);
        let target = if client.eq_ignore_ascii_case("mobile") {
            format!("mplatform://oauth2redirect?error={}", encoded_err)
        } else if client.eq_ignore_ascii_case("mobile_web") {
            format!("/mobile/?error={}", encoded_err)
        } else {
            format!("/login?error={}", encoded_err)
        };
        return Response::builder()
            .status(StatusCode::FOUND)
            .header(header::LOCATION, target)
            .body(axum::body::Body::empty())
            .unwrap();
    }

    // 2. Check code parameter
    let code = match query.code {
        Some(c) if !c.trim().is_empty() => c.trim().to_string(),
        _ => {
            warn!("[OIDC Callback] Missing code parameter");
            return Response::builder()
                .status(StatusCode::FOUND)
                .header(header::LOCATION, "/login?error=missing_code")
                .body(axum::body::Body::empty())
                .unwrap();
        }
    };

    // 3. Exchange code for token with Keycloak
    let token_uri = std::env::var("KEYCLOAK_TOKEN_URI").unwrap_or_else(|_| {
        let realm = std::env::var("KEYCLOAK_REALM").unwrap_or_else(|_| "mplatform".to_string());
        format!(
            "http://keycloak:8080/auth/realms/{}/protocol/openid-connect/token",
            realm
        )
    });
    let client_id =
        std::env::var("KEYCLOAK_CLIENT_ID").unwrap_or_else(|_| "mdm-frontend".to_string());
    let client_secret =
        std::env::var("KEYCLOAK_CLIENT_SECRET").unwrap_or_else(|_| "secret".to_string());
    let callback_uri = build_callback_uri(&headers);

    let http_client = reqwest::Client::builder()
        .timeout(std::time::Duration::from_secs(10))
        .build()
        .unwrap_or_default();

    let mut form_params: Vec<(&str, &str)> = vec![
        ("grant_type", "authorization_code"),
        ("code", &code),
        ("redirect_uri", &callback_uri),
        ("client_id", &client_id),
    ];
    if !client_secret.trim().is_empty() {
        form_params.push(("client_secret", &client_secret));
    }

    let body = form_params
        .iter()
        .map(|(k, v)| format!("{}={}", urlencoding::encode(k), urlencoding::encode(v)))
        .collect::<Vec<_>>()
        .join("&");

    let resp = match http_client
        .post(&token_uri)
        .header(header::CONTENT_TYPE, "application/x-www-form-urlencoded")
        .body(body)
        .send()
        .await
    {
        Ok(r) => r,
        Err(e) => {
            error!("[OIDC Callback] Keycloak request failed: {:?}", e);
            let err_raw = format!("keycloak_unreachable: {}", e);
            let err_msg = urlencoding::encode(&err_raw);
            return Response::builder()
                .status(StatusCode::FOUND)
                .header(header::LOCATION, format!("/login?error={}", err_msg))
                .body(axum::body::Body::empty())
                .unwrap();
        }
    };

    if !resp.status().is_success() {
        let err_text = resp.text().await.unwrap_or_default();
        error!(
            "[OIDC Callback] Keycloak returned non-success status: {}",
            err_text
        );
        return Response::builder()
            .status(StatusCode::FOUND)
            .header(header::LOCATION, "/login?error=token_exchange_failed")
            .body(axum::body::Body::empty())
            .unwrap();
    }

    let token_data: KeycloakTokenResponse = match resp.json().await {
        Ok(d) => d,
        Err(e) => {
            error!("[OIDC Callback] Failed to parse Keycloak JSON: {:?}", e);
            return Response::builder()
                .status(StatusCode::FOUND)
                .header(header::LOCATION, "/login?error=invalid_token_response")
                .body(axum::body::Body::empty())
                .unwrap();
        }
    };

    // 4. Extract claims from access_token
    let payload = decode_jwt_payload(&token_data.access_token);
    let username = payload.as_ref().and_then(extract_username);

    let now_epoch = chrono::Utc::now().timestamp();
    let session_id = token_data
        .session_state
        .clone()
        .unwrap_or_else(|| Uuid::new_v4().to_string());

    let maybe_user = if let Some(ref uname) = username {
        let user = sqlx::query_as::<_, crate::models::user::User>(
            r#"
            SELECT id, username, password, role, organization_id, department_id, team_id,
                   timezone, is_active, must_change_password, email, failed_login_count,
                   locked_until, last_login_epoch_sec, two_factor_enabled, two_factor_secret,
                   two_factor_type, backup_codes, two_factor_grace_until
            FROM users
            WHERE username = $1
            "#,
        )
        .bind(uname)
        .fetch_optional(&state.db)
        .await
        .unwrap_or(None);

        if let Some(existing) = user {
            let _ = sqlx::query(
                r#"
                UPDATE users
                SET active_session_id = $1,
                    last_login_epoch_sec = $2,
                    failed_login_count = 0
                WHERE id = $3
                "#,
            )
            .bind(&session_id)
            .bind(now_epoch)
            .bind(&existing.id)
            .execute(&state.db)
            .await;
            Some(existing)
        } else {
            let new_id = Uuid::new_v4().to_string();
            let email = payload
                .as_ref()
                .and_then(|p| p.get("email"))
                .and_then(|e| e.as_str())
                .map(|s| s.to_string());

            let is_admin = uname == "admin"
                || uname == "superadmin"
                || payload
                    .as_ref()
                    .map(|p| {
                        p.get("realm_access")
                            .and_then(|ra| ra.get("roles"))
                            .and_then(|r| r.as_array())
                            .map(|roles| {
                                roles.iter().any(|role| {
                                    role.as_str() == Some("SYSTEM_ADMIN")
                                        || role.as_str() == Some("ROLE_ADMIN")
                                })
                            })
                            .unwrap_or(false)
                    })
                    .unwrap_or(false);

            let role = if is_admin {
                "ROLE_ADMIN".to_string()
            } else {
                "ROLE_USER".to_string()
            };

            let _ = sqlx::query(
                r#"
                INSERT INTO users (id, username, password, role, email, is_active, must_change_password, timezone, active_session_id, last_login_epoch_sec, failed_login_count)
                VALUES ($1, $2, $3, $4, $5, true, false, 'Asia/Seoul', $6, $7, 0)
                "#,
            )
            .bind(&new_id)
            .bind(uname)
            .bind(Uuid::new_v4().to_string())
            .bind(&role)
            .bind(&email)
            .bind(&session_id)
            .bind(now_epoch)
            .execute(&state.db)
            .await;

            Some(crate::models::user::User {
                id: new_id,
                username: Some(uname.clone()),
                password: None,
                role: Some(role),
                organization_id: None,
                department_id: None,
                team_id: None,
                timezone: Some("Asia/Seoul".to_string()),
                is_active: Some(true),
                must_change_password: Some(false),
                email,
                failed_login_count: Some(0),
                locked_until: None,
                last_login_epoch_sec: Some(now_epoch),
                two_factor_enabled: Some(false),
                two_factor_secret: None,
                two_factor_type: None,
                backup_codes: None,
                two_factor_grace_until: None,
            })
        }
    } else {
        None
    };

    // 5. Record login log with 30s deduplication
    if let Some(ref uname) = username {
        let is_recent = sqlx::query_scalar::<_, bool>(
            r#"
            SELECT EXISTS (
                SELECT 1 FROM login_log
                WHERE username = $1 AND login_at > NOW() - INTERVAL '30 seconds'
            )
            "#,
        )
        .bind(uname)
        .fetch_one(&state.db)
        .await
        .unwrap_or(false);

        if !is_recent {
            let client_ip = extract_client_ip(&headers);
            let user_agent = headers
                .get(header::USER_AGENT)
                .and_then(|v| v.to_str().ok())
                .unwrap_or("");
            let safe_ip = if client_ip.len() > 45 {
                &client_ip[..45]
            } else {
                &client_ip
            };
            let safe_ua = if user_agent.len() > 500 {
                &user_agent[..500]
            } else {
                user_agent
            };
            let user_id = maybe_user.as_ref().map(|u| u.id.as_str());

            let _ = sqlx::query(
                r#"
                INSERT INTO login_log (username, user_id, client_ip, user_agent, login_at)
                VALUES ($1, $2, $3, $4, NOW())
                "#,
            )
            .bind(uname)
            .bind(user_id)
            .bind(safe_ip)
            .bind(safe_ua)
            .execute(&state.db)
            .await;
        }
    }

    // 6. Branch redirect by client type
    if client.eq_ignore_ascii_case("mobile") {
        let target = format!(
            "mplatform://oauth2redirect?access_token={}&refresh_token={}",
            urlencoding::encode(&token_data.access_token),
            urlencoding::encode(token_data.refresh_token.as_deref().unwrap_or("")),
        );
        return Response::builder()
            .status(StatusCode::FOUND)
            .header(header::LOCATION, target)
            .body(axum::body::Body::empty())
            .unwrap();
    }

    if client.eq_ignore_ascii_case("mobile_web") {
        let target = format!(
            "/mobile/?access_token={}&refresh_token={}",
            urlencoding::encode(&token_data.access_token),
            urlencoding::encode(token_data.refresh_token.as_deref().unwrap_or("")),
        );
        return Response::builder()
            .status(StatusCode::FOUND)
            .header(header::LOCATION, target)
            .body(axum::body::Body::empty())
            .unwrap();
    }

    // Web client (Nuxt 3)
    let is_https = resolve_scheme(&headers) == "https";
    let secure_flag = if is_https { "; Secure" } else { "" };
    let expires_in = token_data.expires_in.unwrap_or(1800);

    let destination = if redirect.starts_with('/') && !redirect.starts_with("//") {
        redirect
    } else {
        "/dashboard".to_string()
    };

    let mut resp = Response::builder()
        .status(StatusCode::FOUND)
        .header(header::LOCATION, &destination)
        .body(axum::body::Body::empty())
        .unwrap();

    if let Ok(c) = format!(
        "auth_token={}; Path=/; Max-Age={}; SameSite=Lax{}",
        token_data.access_token, expires_in, secure_flag
    )
    .parse()
    {
        resp.headers_mut().append(header::SET_COOKIE, c);
    }

    if let Some(ref rt) = token_data.refresh_token {
        if let Ok(c) = format!(
            "refresh_token={}; Path=/; Max-Age=86400; SameSite=Lax{}",
            rt, secure_flag
        )
        .parse()
        {
            resp.headers_mut().append(header::SET_COOKIE, c);
        }
    }

    if let Some(ref user) = maybe_user {
        let user_data = serde_json::json!({
            "id": user.id,
            "username": user.username,
            "role": user.role,
            "organizationId": user.organization_id,
            "departmentId": user.department_id,
            "teamId": user.team_id,
            "timezone": user.timezone.as_deref().unwrap_or("Asia/Seoul"),
            "mustChangePassword": user.must_change_password.unwrap_or(false),
        });
        let user_json = serde_json::to_string(&user_data).unwrap_or_default();
        if let Ok(c) = format!(
            "user_data={}; Path=/; Max-Age={}; SameSite=Lax{}",
            urlencoding::encode(&user_json),
            expires_in,
            secure_flag
        )
        .parse()
        {
            resp.headers_mut().append(header::SET_COOKIE, c);
        }
    }

    info!(
        "[OIDC Callback] Login successful for user: {:?}, redirecting to: {}",
        username, destination
    );
    resp
}
