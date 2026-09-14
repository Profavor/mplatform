use crate::error::{AppError, AppResult};
use crate::models::two_factor::*;
use crate::models::user::User;
use crate::repositories::user_repo::UserRepository;
use chrono::{DateTime, Duration, Utc};
use hmac::{Hmac, Mac};
use rand::Rng;
use sha1::Sha1;
use sqlx::PgPool;
use std::collections::HashMap;
use std::sync::Arc;
use std::time::{SystemTime, UNIX_EPOCH};
use tokio::sync::RwLock;

type HmacSha1 = Hmac<Sha1>;

const BASE32_CHARS: &[u8] = b"ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
const BACKUP_CODE_CHARS: &[u8] = b"ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

#[derive(Clone, Debug)]
pub struct TempTokenInfo {
    pub username: String,
    pub created_at: i64,
}

#[derive(Clone)]
pub struct TwoFactorService {
    pool: PgPool,
    temp_tokens: Arc<RwLock<HashMap<String, TempTokenInfo>>>,
    email_otps: Arc<RwLock<HashMap<String, (String, i64)>>>,
}

impl TwoFactorService {
    pub fn new(pool: PgPool) -> Self {
        Self {
            pool,
            temp_tokens: Arc::new(RwLock::new(HashMap::new())),
            email_otps: Arc::new(RwLock::new(HashMap::new())),
        }
    }

    pub fn is_mandatory_role(role: Option<&str>) -> bool {
        match role {
            Some("ROLE_ADMIN") | Some("ADMIN") | Some("ROLE_SYSTEM_ADMIN") | Some("SYSTEM_ADMIN")
            | Some("ROLE_ORG_ADMIN") | Some("ORG_ADMIN") | Some("DATA_STEWARD") | Some("ROLE_DATA_STEWARD") => true,
            _ => false,
        }
    }

    pub fn is_two_factor_required(user: &User) -> bool {
        if user.two_factor_enabled.unwrap_or(false) {
            return true;
        }

        if Self::is_mandatory_role(user.role.as_deref()) {
            if let Some(grace_until) = user.two_factor_grace_until {
                if Utc::now().naive_utc() < grace_until {
                    return false;
                }
            }
            return true;
        }

        false
    }

    pub async fn create_temp_token(&self, username: &str) -> String {
        let token = uuid::Uuid::new_v4().to_string();
        let now = SystemTime::now().duration_since(UNIX_EPOCH).unwrap().as_secs() as i64;
        let mut map = self.temp_tokens.write().await;
        // Purge expired tokens > 300s
        map.retain(|_, v| now - v.created_at < 300);
        map.insert(token.clone(), TempTokenInfo {
            username: username.to_string(),
            created_at: now,
        });
        token
    }

    pub async fn validate_and_consume_temp_token(&self, token: &str, username: &str) -> bool {
        let mut map = self.temp_tokens.write().await;
        let now = SystemTime::now().duration_since(UNIX_EPOCH).unwrap().as_secs() as i64;
        if let Some(info) = map.get(token) {
            if info.username == username && now - info.created_at < 300 {
                map.remove(token);
                return true;
            }
        }
        false
    }

    pub fn generate_base32_secret() -> String {
        let mut rng = rand::thread_rng();
        let mut bytes = [0u8; 20];
        rng.fill(&mut bytes);
        Self::encode_base32(&bytes)
    }

    pub fn generate_backup_codes(count: usize) -> Vec<String> {
        let mut rng = rand::thread_rng();
        let mut codes = Vec::new();
        while codes.len() < count {
            let mut s = String::new();
            for _ in 0..8 {
                let idx = rng.gen_range(0..BACKUP_CODE_CHARS.len());
                s.push(BACKUP_CODE_CHARS[idx] as char);
            }
            if !codes.contains(&s) {
                codes.push(s);
            }
        }
        codes
    }

    pub fn verify_totp(&self, secret_base32: &str, code_input: &str) -> bool {
        let secret_bytes = match Self::decode_base32(secret_base32) {
            Ok(b) => b,
            Err(_) => return false,
        };

        let now_sec = SystemTime::now().duration_since(UNIX_EPOCH).unwrap().as_secs();
        let current_step = (now_sec / 30) as i64;

        for step_offset in [-1, 0, 1] {
            let step = (current_step + step_offset) as u64;
            let code = Self::generate_code_for_step(&secret_bytes, step);
            if code == code_input.trim() {
                return true;
            }
        }

        false
    }

    fn generate_code_for_step(secret: &[u8], step: u64) -> String {
        let mut mac = HmacSha1::new_from_slice(secret).expect("HMAC can take key of any size");
        mac.update(&step.to_be_bytes());
        let result = mac.finalize().into_bytes();

        let offset = (result[19] & 0x0f) as usize;
        let binary = ((result[offset] & 0x7f) as u32) << 24
            | ((result[offset + 1] & 0xff) as u32) << 16
            | ((result[offset + 2] & 0xff) as u32) << 8
            | ((result[offset + 3] & 0xff) as u32);

        let otp = binary % 1_000_000;
        format!("{:06}", otp)
    }

    pub fn encode_base32(data: &[u8]) -> String {
        let mut result = String::new();
        let mut buffer: u32 = 0;
        let mut bits_left = 0;

        for &byte in data {
            buffer = (buffer << 8) | (byte as u32);
            bits_left += 8;
            while bits_left >= 5 {
                let index = ((buffer >> (bits_left - 5)) & 0x1F) as usize;
                result.push(BASE32_CHARS[index] as char);
                bits_left -= 5;
            }
        }

        if bits_left > 0 {
            let index = ((buffer << (5 - bits_left)) & 0x1F) as usize;
            result.push(BASE32_CHARS[index] as char);
        }

        result
    }

    pub fn decode_base32(s: &str) -> Result<Vec<u8>, AppError> {
        let clean = s.trim().to_uppercase().replace(['=', ' '], "");
        let mut buffer: u32 = 0;
        let mut bits_left = 0;
        let mut out = Vec::new();

        for c in clean.chars() {
            let val = match c {
                'A'..='Z' => (c as u8 - b'A') as u32,
                '2'..='7' => (c as u8 - b'2' + 26) as u32,
                _ => return Err(AppError::BadRequest(format!("Invalid base32 char: {c}"))),
            };
            buffer = (buffer << 5) | val;
            bits_left += 5;
            if bits_left >= 8 {
                out.push(((buffer >> (bits_left - 8)) & 0xFF) as u8);
                bits_left -= 8;
            }
        }

        Ok(out)
    }

    pub async fn get_status(&self, username: &str) -> AppResult<TwoFactorStatusResponse> {
        let user = UserRepository::find_by_username(&self.pool, username)
            .await?
            .ok_or_else(|| AppError::NotFound(format!("User {username} not found")))?;

        let remaining_codes = match &user.backup_codes {
            Some(codes) if !codes.trim().is_empty() => codes.split(',').count(),
            _ => 0,
        };

        let now = Utc::now().naive_utc();
        let grace_days = user.two_factor_grace_until.map(|g| (g - now).num_days());
        let masked_email = user.email.as_ref().map(|e| {
            if let Some((local, domain)) = e.split_once('@') {
                let mask_prefix = if local.len() <= 2 { local.to_string() } else { format!("{}***", &local[..2]) };
                format!("{mask_prefix}@{domain}")
            } else {
                e.clone()
            }
        });

        let mandatory = Self::is_two_factor_required(&user);

        Ok(TwoFactorStatusResponse {
            two_factor_enabled: user.two_factor_enabled.unwrap_or(false),
            two_factor_type: user.two_factor_type,
            has_backup_codes: remaining_codes > 0,
            remaining_backup_codes_count: remaining_codes,
            role: user.role,
            mandatory,
            grace_until: user.two_factor_grace_until,
            grace_period_remaining_days: grace_days,
            masked_email,
        })
    }

    pub async fn setup(&self, username: &str) -> AppResult<TwoFactorSetupResponse> {
        let secret = Self::generate_base32_secret();
        let qr_code_url = format!("otpauth://totp/MDM%20Platform:{}?secret={}&issuer=MDM%20Platform", username, secret);
        let backup_codes = Self::generate_backup_codes(10);

        Ok(TwoFactorSetupResponse {
            secret,
            qr_code_url,
            backup_codes,
        })
    }

    pub async fn enable(&self, username: &str, req: TwoFactorEnableRequest) -> AppResult<TwoFactorEnableResponse> {
        let user = UserRepository::find_by_username(&self.pool, username)
            .await?
            .ok_or_else(|| AppError::NotFound(format!("User {username} not found")))?;

        if !self.verify_totp(&req.secret, &req.code) {
            return Err(AppError::BadRequest("Invalid 2FA verification code".to_string()));
        }

        let backup_codes = Self::generate_backup_codes(10);
        let mut hashed_codes = Vec::new();
        for c in &backup_codes {
            let hash = bcrypt::hash(c, bcrypt::DEFAULT_COST)
                .map_err(|e| AppError::Internal(e.to_string()))?;
            hashed_codes.push(hash);
        }
        let backup_codes_str = hashed_codes.join(",");
        let auth_type = req.auth_type.unwrap_or_else(|| "TOTP".to_string());

        sqlx::query(
            r#"
            UPDATE users
            SET two_factor_secret = $1,
                two_factor_enabled = true,
                two_factor_type = $2,
                backup_codes = $3,
                two_factor_grace_until = NULL
            WHERE id = $4
            "#
        )
        .bind(&req.secret)
        .bind(&auth_type)
        .bind(&backup_codes_str)
        .bind(&user.id)
        .execute(&self.pool)
        .await?;

        Ok(TwoFactorEnableResponse {
            success: true,
            backup_codes,
            message: "Two-factor authentication enabled successfully".to_string(),
        })
    }

    pub async fn disable(&self, username: &str) -> AppResult<()> {
        let user = UserRepository::find_by_username(&self.pool, username)
            .await?
            .ok_or_else(|| AppError::NotFound(format!("User {username} not found")))?;

        sqlx::query(
            r#"
            UPDATE users
            SET two_factor_secret = NULL,
                two_factor_enabled = false,
                two_factor_type = NULL,
                backup_codes = NULL
            WHERE id = $1
            "#
        )
        .bind(&user.id)
        .execute(&self.pool)
        .await?;

        Ok(())
    }

    pub async fn verify_backup_code(&self, user: &User, code: &str) -> AppResult<bool> {
        let raw_codes = match &user.backup_codes {
            Some(c) if !c.trim().is_empty() => c,
            _ => return Ok(false),
        };

        let mut list: Vec<&str> = raw_codes.split(',').collect();
        let target = code.trim().to_uppercase();

        for (i, &hash) in list.iter().enumerate() {
            if bcrypt::verify(&target, hash).unwrap_or(false) {
                list.remove(i);
                let remaining = list.join(",");
                let _ = sqlx::query("UPDATE users SET backup_codes = $1 WHERE id = $2")
                    .bind(if remaining.is_empty() { None } else { Some(remaining) })
                    .bind(&user.id)
                    .execute(&self.pool)
                    .await;
                return Ok(true);
            }
        }

        Ok(false)
    }

    pub async fn send_email_otp(&self, username: &str) -> AppResult<String> {
        let code = {
            let mut rng = rand::thread_rng();
            let num: u32 = rng.gen_range(100_000..=999_999);
            num.to_string()
        };
        let now = SystemTime::now().duration_since(UNIX_EPOCH).unwrap().as_secs() as i64;

        let mut map = self.email_otps.write().await;
        map.insert(username.to_string(), (code.clone(), now));
        tracing::info!("[2FA Email OTP] Code for {}: {}", username, code);
        Ok(code)
    }

    pub async fn verify_email_otp(&self, username: &str, code: &str) -> bool {
        let mut map = self.email_otps.write().await;
        let now = SystemTime::now().duration_since(UNIX_EPOCH).unwrap().as_secs() as i64;
        if let Some((stored_code, created_at)) = map.get(username) {
            if stored_code == code.trim() && now - created_at < 300 {
                map.remove(username);
                return true;
            }
        }
        false
    }
}
