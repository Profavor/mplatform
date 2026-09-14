use crate::error::AppError;
use crate::models::user::Claims;
use crate::state::AppState;
use axum::{async_trait, extract::FromRequestParts, http::request::Parts};
use jsonwebtoken::{decode, decode_header, Algorithm, DecodingKey, Validation};
use std::collections::HashMap;
use std::sync::{OnceLock, RwLock};

#[derive(Debug, Clone)]
pub struct AuthUser {
    pub username: String,
    pub user_id: String,
    pub role: Option<String>,
    pub permissions: Vec<String>,
    pub claims: Claims,
}

impl AuthUser {
    /// Checks whether user has the required permission.
    /// Matches the frontend usePermission.ts algorithm:
    /// 1. Global wildcard: '*', '*:*', '*:read' etc.
    /// 2. Exact match: 'domain:write' === 'domain:write'
    /// 3. Resource prefix wildcard: 'domain:*' matches 'domain:write', 'domain:read', etc.
    pub fn has_permission(&self, required_permission: &str) -> bool {
        if self.permissions.is_empty() {
            return false;
        }

        let normalize = |p: &str| p.trim().to_lowercase();
        let norm_required = normalize(required_permission);

        // 1. Global wildcard
        for p in &self.permissions {
            let norm_p = normalize(p);
            if norm_p == "*" || norm_p == "*:*" || norm_p.starts_with("*:") {
                return true;
            }
        }

        // 2. Exact match
        for p in &self.permissions {
            if normalize(p) == norm_required {
                return true;
            }
        }

        // 3. Domain/Resource prefix wildcard ('domain:*' matches 'domain:write')
        if norm_required.contains(':') {
            if let Some((prefix, _)) = norm_required.split_once(':') {
                let domain_wildcard = format!("{}:*", prefix);
                for p in &self.permissions {
                    if normalize(p) == domain_wildcard {
                        return true;
                    }
                }
            }
        }

        false
    }

    /// Requires the user to have the specified permission, returning Forbidden error if not.
    pub fn require_permission(&self, required_permission: &str) -> Result<(), AppError> {
        if self.has_permission(required_permission) {
            Ok(())
        } else {
            Err(AppError::Forbidden(format!(
                "필요한 권한이 없습니다: '{}'",
                required_permission
            )))
        }
    }

    /// Checks whether user has ANY of the specified permissions.
    pub fn has_any_permission(&self, required_permissions: &[&str]) -> bool {
        required_permissions
            .iter()
            .any(|&perm| self.has_permission(perm))
    }

    /// Requires the user to have ANY of the specified permissions.
    pub fn require_any_permission(&self, required_permissions: &[&str]) -> Result<(), AppError> {
        if self.has_any_permission(required_permissions) {
            Ok(())
        } else {
            Err(AppError::Forbidden(format!(
                "필요한 권한이 없습니다. 다음 중 하나 이상의 권한이 필요합니다: {:?}",
                required_permissions
            )))
        }
    }
}

pub type AuthenticatedUser = AuthUser;

static JWKS_CACHE: OnceLock<RwLock<HashMap<String, DecodingKey>>> = OnceLock::new();

fn get_jwks_cache() -> &'static RwLock<HashMap<String, DecodingKey>> {
    JWKS_CACHE.get_or_init(|| RwLock::new(HashMap::new()))
}

#[derive(serde::Deserialize)]
struct JwkKey {
    kid: Option<String>,
    n: String,
    e: String,
}

#[derive(serde::Deserialize)]
struct JwkSet {
    keys: Vec<JwkKey>,
}

async fn fetch_keycloak_jwks() -> Result<HashMap<String, DecodingKey>, AppError> {
    let jwk_set_uri = std::env::var("JWK_SET_URI").unwrap_or_else(|_| {
        let server = std::env::var("KEYCLOAK_SERVER_URL")
            .unwrap_or_else(|_| "http://keycloak:8080/auth".to_string());
        let realm = std::env::var("KEYCLOAK_REALM").unwrap_or_else(|_| "mplatform".to_string());
        format!(
            "{}/realms/{}/protocol/openid-connect/certs",
            server.trim_end_matches('/'),
            realm
        )
    });

    let client = reqwest::Client::builder()
        .timeout(std::time::Duration::from_secs(5))
        .build()
        .unwrap_or_default();

    let resp = client
        .get(&jwk_set_uri)
        .send()
        .await
        .map_err(|e| AppError::Unauthorized(format!("Failed to reach Keycloak JWKS: {e}")))?;

    let jwk_set: JwkSet = resp
        .json()
        .await
        .map_err(|e| AppError::Unauthorized(format!("Failed to parse Keycloak JWKS: {e}")))?;

    let mut map = HashMap::new();
    for key in jwk_set.keys {
        if let Ok(decoding_key) = DecodingKey::from_rsa_components(&key.n, &key.e) {
            if let Some(kid) = key.kid {
                map.insert(kid, decoding_key);
            }
        }
    }
    Ok(map)
}

fn extract_token(parts: &Parts) -> Option<String> {
    // 1. Authorization: Bearer <token>
    if let Some(auth_header) = parts
        .headers
        .get(axum::http::header::AUTHORIZATION)
        .and_then(|value| value.to_str().ok())
    {
        if auth_header.starts_with("Bearer ") {
            let t = auth_header[7..].trim();
            if !t.is_empty() {
                return Some(t.to_string());
            }
        }
    }

    // 2. Cookies: auth_token=..., access_token=..., jwt=...
    if let Some(cookie_header) = parts
        .headers
        .get(axum::http::header::COOKIE)
        .and_then(|value| value.to_str().ok())
    {
        for cookie in cookie_header.split(';') {
            let mut kv = cookie.trim().splitn(2, '=');
            if let (Some(k), Some(v)) = (kv.next(), kv.next()) {
                let k_trim = k.trim();
                if k_trim == "auth_token" || k_trim == "access_token" || k_trim == "jwt" {
                    let v_trim = v.trim();
                    if !v_trim.is_empty() {
                        return Some(v_trim.to_string());
                    }
                }
            }
        }
    }

    None
}

fn extract_username_from_payload(payload: &serde_json::Value) -> Option<String> {
    for key in &["preferred_username", "clientId", "client_id", "azp", "sub"] {
        if let Some(val) = payload.get(*key).and_then(|v| v.as_str()) {
            let s = val.trim();
            if !s.is_empty() {
                return Some(s.to_string());
            }
        }
    }
    None
}

#[async_trait]
impl FromRequestParts<AppState> for AuthUser {
    type Rejection = AppError;

    async fn from_request_parts(
        parts: &mut Parts,
        state: &AppState,
    ) -> Result<Self, Self::Rejection> {
        let token = extract_token(parts).ok_or_else(|| {
            AppError::Unauthorized("Missing Authorization header or auth token cookie".to_string())
        })?;

        // 1. Check if token is Keycloak RS256 token
        if let Ok(header) = decode_header(&token) {
            if header.alg == Algorithm::RS256 {
                let kid = header.kid.clone();
                let cached_key = {
                    let cache = get_jwks_cache().read().unwrap();
                    kid.as_ref().and_then(|k| cache.get(k).cloned())
                };

                let decoding_key = if let Some(k) = cached_key {
                    k
                } else {
                    let new_keys = fetch_keycloak_jwks().await?;
                    let mut cache = get_jwks_cache().write().unwrap();
                    *cache = new_keys;
                    if let Some(ref k) = kid {
                        cache.get(k).cloned()
                    } else {
                        cache.values().next().cloned()
                    }
                    .ok_or_else(|| {
                        AppError::Unauthorized("No matching Keycloak public key found".to_string())
                    })?
                };

                let mut validation = Validation::new(Algorithm::RS256);
                validation.validate_exp = true;
                validation.validate_aud = false;

                if let Ok(token_data) =
                    decode::<serde_json::Value>(&token, &decoding_key, &validation)
                {
                    let payload = token_data.claims;
                    let username = extract_username_from_payload(&payload)
                        .unwrap_or_else(|| "unknown".to_string());

                    let user_opt =
                        crate::repositories::user_repo::UserRepository::find_by_username(
                            &state.db, &username,
                        )
                        .await
                        .ok()
                        .flatten();

                    let (user_id, role, permissions) = if let Some(u) = user_opt {
                        let perms =
                            crate::services::auth_service::AuthService::get_user_permissions(
                                &state.db, &u,
                            )
                            .await;
                        (
                            u.id,
                            u.role.unwrap_or_else(|| "ROLE_USER".to_string()),
                            perms,
                        )
                    } else {
                        let is_admin = username == "admin"
                            || username == "superadmin"
                            || payload
                                .get("realm_access")
                                .and_then(|ra| ra.get("roles"))
                                .and_then(|r| r.as_array())
                                .map(|roles| {
                                    roles.iter().any(|role| {
                                        role.as_str() == Some("SYSTEM_ADMIN")
                                            || role.as_str() == Some("ROLE_ADMIN")
                                    })
                                })
                                .unwrap_or(false);

                        let r = if is_admin {
                            "ROLE_ADMIN".to_string()
                        } else {
                            "ROLE_USER".to_string()
                        };
                        let sub = payload
                            .get("sub")
                            .and_then(|v| v.as_str())
                            .unwrap_or(&username)
                            .to_string();

                        let mut perms = crate::services::auth_service::AuthService::get_permissions_for_role_name(&state.db, &r).await;
                        if is_admin && !perms.iter().any(|p| p == "*") {
                            perms.insert(0, "*".to_string());
                        }

                        (sub, r, perms)
                    };

                    let session_id = payload
                        .get("session_state")
                        .or_else(|| payload.get("sid"))
                        .and_then(|v| v.as_str())
                        .map(|s| s.to_string());

                    let iat = payload.get("iat").and_then(|v| v.as_u64()).unwrap_or(0) as usize;
                    let exp = payload.get("exp").and_then(|v| v.as_u64()).unwrap_or(0) as usize;

                    let claims = Claims {
                        sub: username.clone(),
                        role: Some(role.clone()),
                        user_id: Some(user_id.clone()),
                        uuid: Some(user_id.clone()),
                        session_id,
                        token_type: Some("Bearer".to_string()),
                        permissions: Some(permissions.clone()),
                        iat,
                        exp,
                    };

                    return Ok(AuthUser {
                        username,
                        user_id,
                        role: Some(role),
                        permissions,
                        claims,
                    });
                }
            }
        }

        // 2. Fallback to local HS256 verification
        let key = DecodingKey::from_secret(state.config.jwt_secret.as_bytes());
        let mut validation = Validation::new(Algorithm::HS256);
        validation.validate_exp = true;
        validation.validate_aud = false;

        let token_data = decode::<Claims>(&token, &key, &validation)
            .map_err(|e| AppError::Unauthorized(format!("Invalid token: {e}")))?;

        let claims = token_data.claims;
        let username = claims.sub.clone();
        let user_id = claims.user_id.clone().unwrap_or_else(|| claims.sub.clone());
        let role = claims.role.clone();

        let permissions = if let Some(p) = claims.permissions.clone() {
            p
        } else {
            let u_opt = crate::repositories::user_repo::UserRepository::find_by_username(
                &state.db, &username,
            )
            .await
            .ok()
            .flatten();
            if let Some(u) = u_opt {
                crate::services::auth_service::AuthService::get_user_permissions(&state.db, &u)
                    .await
            } else if let Some(ref r) = role {
                let mut perms =
                    crate::services::auth_service::AuthService::get_permissions_for_role_name(
                        &state.db, r,
                    )
                    .await;
                if r == "ROLE_ADMIN"
                    || r == "ADMIN"
                    || username == "admin"
                    || username == "superadmin"
                {
                    if !perms.iter().any(|p| p == "*") {
                        perms.insert(0, "*".to_string());
                    }
                }
                perms
            } else {
                Vec::new()
            }
        };

        Ok(AuthUser {
            username,
            user_id,
            role,
            permissions,
            claims,
        })
    }
}

#[derive(Debug, Clone)]
pub struct OptionalAuthUser(pub Option<AuthUser>);

#[async_trait]
impl FromRequestParts<AppState> for OptionalAuthUser {
    type Rejection = std::convert::Infallible;

    async fn from_request_parts(
        parts: &mut Parts,
        state: &AppState,
    ) -> Result<Self, Self::Rejection> {
        let auth = AuthUser::from_request_parts(parts, state).await.ok();
        Ok(OptionalAuthUser(auth))
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    fn create_test_user(perms: Vec<&str>) -> AuthUser {
        AuthUser {
            username: "tester".to_string(),
            user_id: "usr-123".to_string(),
            role: Some("ROLE_USER".to_string()),
            permissions: perms.into_iter().map(|s| s.to_string()).collect(),
            claims: Claims {
                sub: "tester".to_string(),
                role: Some("ROLE_USER".to_string()),
                user_id: Some("usr-123".to_string()),
                uuid: Some("usr-123".to_string()),
                session_id: None,
                token_type: None,
                permissions: None,
                iat: 0,
                exp: 0,
            },
        }
    }

    #[test]
    fn test_exact_permission_match() {
        let user = create_test_user(vec!["domain:read", "domain:write"]);
        assert!(user.has_permission("domain:read"));
        assert!(user.has_permission("domain:write"));
        assert!(!user.has_permission("domain:delete"));
        assert!(!user.has_permission("record:read"));
    }

    #[test]
    fn test_global_wildcard() {
        let admin = create_test_user(vec!["*"]);
        assert!(admin.has_permission("domain:read"));
        assert!(admin.has_permission("record:write"));
        assert!(admin.has_permission("anything:really"));
        assert!(admin.require_permission("system:admin").is_ok());

        let colon_wildcard = create_test_user(vec!["*:*"]);
        assert!(colon_wildcard.has_permission("domain:write"));
    }

    #[test]
    fn test_domain_prefix_wildcard() {
        let domain_manager = create_test_user(vec!["domain:*"]);
        assert!(domain_manager.has_permission("domain:read"));
        assert!(domain_manager.has_permission("domain:write"));
        assert!(domain_manager.has_permission("domain:delete"));
        assert!(!domain_manager.has_permission("record:write"));
        assert!(!domain_manager.has_permission("field:read"));
    }

    #[test]
    fn test_case_and_whitespace_insensitivity() {
        let user = create_test_user(vec!["  DOMAIN:Write  "]);
        assert!(user.has_permission("domain:write"));
        assert!(user.has_permission("  DOMAIN:WRITE  "));
    }

    #[test]
    fn test_has_any_and_require_permission() {
        let user = create_test_user(vec!["record:read"]);
        assert!(user.has_any_permission(&["domain:write", "record:read"]));
        assert!(!user.has_any_permission(&["domain:write", "integration:write"]));

        assert!(user.require_permission("record:read").is_ok());
        let err = user.require_permission("record:write").unwrap_err();
        match err {
            AppError::Forbidden(msg) => assert!(msg.contains("record:write")),
            _ => panic!("Expected Forbidden error"),
        }
    }
}
