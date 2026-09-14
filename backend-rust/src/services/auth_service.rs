use crate::config::Config;
use crate::error::AppError;
use crate::models::user::{Claims, LoginRequest, LoginResponse, User};
use crate::repositories::user_repo::UserRepository;
use jsonwebtoken::{decode, encode, DecodingKey, EncodingKey, Header, Validation};
use sqlx::PgPool;
use std::time::{SystemTime, UNIX_EPOCH};

pub struct AuthService;

use crate::services::two_factor_service::TwoFactorService;

impl AuthService {
    pub async fn login(
        pool: &PgPool,
        config: &Config,
        two_factor_service: &TwoFactorService,
        req: LoginRequest,
    ) -> Result<LoginResponse, AppError> {
        let user = UserRepository::find_by_username(pool, &req.username)
            .await?
            .ok_or_else(|| AppError::Unauthorized("Invalid username or password".to_string()))?;

        if let Some(is_active) = user.is_active {
            if !is_active {
                return Err(AppError::Unauthorized("Account is deactivated".to_string()));
            }
        }

        let password_hash = user
            .password
            .as_deref()
            .ok_or_else(|| AppError::Unauthorized("Invalid credentials".to_string()))?;

        let password_valid = bcrypt::verify(&req.password, password_hash)
            .unwrap_or(false);

        if !password_valid {
            return Err(AppError::Unauthorized("Invalid username or password".to_string()));
        }

        let now_sec = SystemTime::now()
            .duration_since(UNIX_EPOCH)
            .unwrap()
            .as_secs() as i64;

        let _ = UserRepository::update_last_login(pool, &user.id, now_sec).await;

        let username = user.username.clone().unwrap_or_else(|| user.id.clone());

        // Check if 2FA is required
        if TwoFactorService::is_two_factor_required(&user) {
            let temp_token = two_factor_service.create_temp_token(&username).await;
            return Ok(LoginResponse {
                token: None,
                refresh_token: None,
                username: Some(username),
                role: user.role.clone(),
                id: Some(user.id.clone()),
                user_id: Some(user.id.clone()),
                organization_id: user.organization_id,
                department_id: user.department_id,
                timezone: user.timezone.clone(),
                server_offset: Some("+09:00".to_string()),
                permissions: vec![],
                must_change_password: user.must_change_password,
                two_factor_required: Some(true),
                temp_token: Some(temp_token),
            });
        }

        // Record login log
        let _ = sqlx::query(
            r#"
            INSERT INTO login_log (username, user_id, login_at, two_factor_status)
            VALUES ($1, $2, NOW(), 'NONE')
            "#
        )
        .bind(&user.username)
        .bind(&user.id)
        .execute(pool)
        .await;

        let permissions = Self::get_user_permissions(pool, &user).await;
        let (access_token, refresh_token) = Self::generate_tokens_with_permissions(&user, Some(permissions.clone()), config)?;

        let server_offset = "+09:00".to_string();

        Ok(LoginResponse {
            token: Some(access_token),
            refresh_token: Some(refresh_token),
            username: user.username.clone(),
            role: user.role.clone(),
            id: Some(user.id.clone()),
            user_id: Some(user.id.clone()),
            organization_id: user.organization_id,
            department_id: user.department_id,
            timezone: user.timezone.clone(),
            server_offset: Some(server_offset),
            permissions,
            must_change_password: user.must_change_password,
            two_factor_required: Some(false),
            temp_token: None,
        })
    }

    pub fn generate_tokens(user: &User, config: &Config) -> Result<(String, String), AppError> {
        Self::generate_tokens_with_permissions(user, None, config)
    }

    pub fn generate_tokens_with_permissions(
        user: &User,
        permissions: Option<Vec<String>>,
        config: &Config,
    ) -> Result<(String, String), AppError> {
        let now = SystemTime::now()
            .duration_since(UNIX_EPOCH)
            .unwrap()
            .as_secs() as usize;

        let access_exp = now + config.jwt_access_expiration_sec as usize;
        let refresh_exp = now + config.jwt_refresh_expiration_sec as usize;

        let username = user.username.clone().unwrap_or_else(|| user.id.clone());

        let access_claims = Claims {
            sub: username.clone(),
            role: user.role.clone(),
            user_id: Some(user.id.clone()),
            uuid: Some(user.id.clone()),
            session_id: None,
            token_type: None,
            permissions,
            iat: now,
            exp: access_exp,
        };

        let refresh_claims = Claims {
            sub: username,
            role: user.role.clone(),
            user_id: Some(user.id.clone()),
            uuid: Some(user.id.clone()),
            session_id: None,
            token_type: Some("REFRESH".to_string()),
            permissions: None,
            iat: now,
            exp: refresh_exp,
        };

        let encoding_key = EncodingKey::from_secret(config.jwt_secret.as_bytes());

        let access_token = encode(&Header::default(), &access_claims, &encoding_key)
            .map_err(|e| AppError::Internal(format!("Failed to generate JWT: {e}")))?;

        let refresh_token = encode(&Header::default(), &refresh_claims, &encoding_key)
            .map_err(|e| AppError::Internal(format!("Failed to generate refresh JWT: {e}")))?;

        Ok((access_token, refresh_token))
    }

    pub async fn refresh_tokens(
        pool: &PgPool,
        config: &Config,
        refresh_token: &str,
    ) -> Result<(String, String), AppError> {
        // 1. Try Keycloak refresh token exchange first
        if let Ok(tokens) = Self::refresh_keycloak_tokens(refresh_token).await {
            return Ok(tokens);
        }

        // 2. Fallback to local HS256 validation
        let key = DecodingKey::from_secret(config.jwt_secret.as_bytes());
        let mut validation = Validation::default();
        validation.validate_exp = true;

        let token_data = decode::<Claims>(refresh_token, &key, &validation)
            .map_err(|e| AppError::Unauthorized(format!("Invalid refresh token: {e}")))?;

        let username = token_data.claims.sub;
        let user = UserRepository::find_by_username(pool, &username)
            .await?
            .ok_or_else(|| AppError::Unauthorized("User not found".to_string()))?;

        Self::generate_tokens(&user, config)
    }

    async fn refresh_keycloak_tokens(
        refresh_token: &str,
    ) -> Result<(String, String), AppError> {
        let token_uri = std::env::var("KEYCLOAK_TOKEN_URI").unwrap_or_else(|_| {
            let server = std::env::var("KEYCLOAK_SERVER_URL")
                .unwrap_or_else(|_| "http://keycloak:8080/auth".to_string());
            let realm = std::env::var("KEYCLOAK_REALM").unwrap_or_else(|_| "mplatform".to_string());
            format!(
                "{}/realms/{}/protocol/openid-connect/token",
                server.trim_end_matches('/'),
                realm
            )
        });
        let client_id =
            std::env::var("KEYCLOAK_CLIENT_ID").unwrap_or_else(|_| "mdm-frontend".to_string());
        let client_secret =
            std::env::var("KEYCLOAK_CLIENT_SECRET").unwrap_or_else(|_| "secret".to_string());

        let client = reqwest::Client::builder()
            .timeout(std::time::Duration::from_secs(5))
            .build()
            .unwrap_or_default();

        let mut form: Vec<(&str, &str)> = vec![
            ("grant_type", "refresh_token"),
            ("refresh_token", refresh_token),
            ("client_id", &client_id),
        ];
        if !client_secret.trim().is_empty() {
            form.push(("client_secret", &client_secret));
        }

        let body = form
            .iter()
            .map(|(k, v)| format!("{}={}", urlencoding::encode(k), urlencoding::encode(v)))
            .collect::<Vec<_>>()
            .join("&");

        let resp = client
            .post(&token_uri)
            .header(reqwest::header::CONTENT_TYPE, "application/x-www-form-urlencoded")
            .body(body)
            .send()
            .await
            .map_err(|e| AppError::Unauthorized(format!("Keycloak refresh unreachable: {e}")))?;

        if !resp.status().is_success() {
            let err_body = resp.text().await.unwrap_or_default();
            return Err(AppError::Unauthorized(format!(
                "Keycloak refresh rejected: {err_body}"
            )));
        }

        let body: serde_json::Value = resp
            .json()
            .await
            .map_err(|e| AppError::Unauthorized(format!("Invalid Keycloak JSON: {e}")))?;

        let access_token = body
            .get("access_token")
            .and_then(|v| v.as_str())
            .ok_or_else(|| {
                AppError::Unauthorized("Missing access_token in Keycloak refresh response".to_string())
            })?
            .to_string();

        let new_refresh_token = body
            .get("refresh_token")
            .and_then(|v| v.as_str())
            .unwrap_or(refresh_token)
            .to_string();

        Ok((access_token, new_refresh_token))
    }

    pub async fn get_permissions_for_role_name(pool: &PgPool, role_name: &str) -> Vec<String> {
        let perms: Vec<String> = sqlx::query_scalar(
            r#"
            SELECT rp.permission
            FROM role r
            JOIN role_permissions rp ON r.id = rp.role_id
            WHERE r.name = $1
               OR r.name = ('ROLE_' || $1)
               OR ('ROLE_' || r.name) = $1
            "#
        )
        .bind(role_name)
        .fetch_all(pool)
        .await
        .unwrap_or_default();

        perms
    }

    pub async fn get_user_permissions(pool: &PgPool, user: &User) -> Vec<String> {
        let mut perms_set: std::collections::HashSet<String> = std::collections::HashSet::new();

        // 1. Check user_role assignments that are NOT expired (expires_at IS NULL OR expires_at > NOW())
        let assigned_perms: Vec<String> = sqlx::query_scalar(
            r#"
            SELECT rp.permission
            FROM user_role ur
            JOIN role_permissions rp ON ur.role_id = rp.role_id
            WHERE ur.user_id = $1
              AND (ur.expires_at IS NULL OR ur.expires_at > NOW())
            "#
        )
        .bind(&user.id)
        .fetch_all(pool)
        .await
        .unwrap_or_default();

        for p in assigned_perms {
            let trimmed = p.trim();
            if !trimmed.is_empty() {
                perms_set.insert(trimmed.to_string());
            }
        }

        // 2. Reflect permissions for user's assigned role column if present
        if let Some(ref r) = user.role {
            let role_perms = Self::get_permissions_for_role_name(pool, r).await;
            for p in role_perms {
                let trimmed = p.trim();
                if !trimmed.is_empty() {
                    perms_set.insert(trimmed.to_string());
                }
            }
        }

        // 3. Reflect department roles if department_id is set
        if let Some(dept_id) = user.department_id {
            let dept_perms: Vec<String> = sqlx::query_scalar(
                r#"
                SELECT rp.permission
                FROM department_roles dr
                JOIN role r ON (r.name = dr.role_name OR r.name = ('ROLE_' || dr.role_name) OR ('ROLE_' || r.name) = dr.role_name)
                JOIN role_permissions rp ON r.id = rp.role_id
                WHERE dr.department_id = $1
                "#
            )
            .bind(dept_id)
            .fetch_all(pool)
            .await
            .unwrap_or_default();

            for p in dept_perms {
                let trimmed = p.trim();
                if !trimmed.is_empty() {
                    perms_set.insert(trimmed.to_string());
                }
            }
        }

        let mut result: Vec<String> = perms_set.into_iter().collect();
        result.sort();
        result
    }
}
