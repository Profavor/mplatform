use aes_gcm::aead::{Aead, KeyInit};
use aes_gcm::{Aes256Gcm, Nonce};
use base64::engine::general_purpose::STANDARD as BASE64;
use base64::Engine;
use hmac::{Hmac, Mac};
use rand::RngCore;
use sha2::{Digest, Sha256};
use std::sync::Arc;
use std::time::{Duration, Instant};
use tokio::sync::RwLock;
use tracing::{debug, error, info, warn};

use crate::error::AppError;

type HmacSha256 = Hmac<Sha256>;

pub struct FieldEncryptionService {
    http_client: reqwest::Client,
    vault_uri: String,
    vault_auth_method: String,
    vault_token: Option<String>,
    vault_k8s_role: String,
    vault_k8s_jwt_path: String,
    vault_key_name: String,
    cached_token: Arc<RwLock<Option<(String, Instant)>>>,
    encryption_type: String,
    aes_key: [u8; 32],
    hmac_key: [u8; 32],
}

impl FieldEncryptionService {
    pub fn new() -> Self {
        let vault_uri = std::env::var("VAULT_URI")
            .unwrap_or_else(|_| "http://vault:8200".to_string())
            .trim_end_matches('/')
            .to_string();
        let vault_auth_method = std::env::var("VAULT_AUTH_METHOD")
            .unwrap_or_else(|_| "KUBERNETES".to_string())
            .to_uppercase();
        let vault_token = std::env::var("VAULT_TOKEN").ok();
        let vault_k8s_role =
            std::env::var("VAULT_K8S_ROLE").unwrap_or_else(|_| "mdm-role".to_string());
        let vault_k8s_jwt_path = std::env::var("VAULT_K8S_JWT_PATH")
            .unwrap_or_else(|_| "/var/run/secrets/kubernetes.io/serviceaccount/token".to_string());
        let vault_key_name =
            std::env::var("VAULT_KEY_NAME").unwrap_or_else(|_| "mdm-field-key".to_string());
        let encryption_type = std::env::var("ENCRYPTION_TYPE")
            .unwrap_or_else(|_| "VAULT".to_string())
            .to_uppercase();

        let secret_key = std::env::var("ENCRYPTION_SECRET_KEY")
            .unwrap_or_else(|_| "default-vault-local-fallback-key-32bytes".to_string());

        // Prepare 32-byte AES key
        let mut aes_key = [0u8; 32];
        let bytes = secret_key.as_bytes();
        if bytes.len() <= 32 {
            aes_key[..bytes.len()].copy_from_slice(bytes);
        } else {
            aes_key.copy_from_slice(&bytes[..32]);
        }

        // Prepare HMAC key: SHA-256("HMAC-BLIND-INDEX-KEY:" + secret_key)
        let mut hasher = Sha256::new();
        hasher.update(format!("HMAC-BLIND-INDEX-KEY:{}", secret_key).as_bytes());
        let hmac_hash = hasher.finalize();
        let mut hmac_key = [0u8; 32];
        hmac_key.copy_from_slice(&hmac_hash);

        Self {
            http_client: reqwest::Client::builder()
                .timeout(Duration::from_secs(5))
                .build()
                .unwrap_or_default(),
            vault_uri,
            vault_auth_method,
            vault_token,
            vault_k8s_role,
            vault_k8s_jwt_path,
            vault_key_name,
            cached_token: Arc::new(RwLock::new(None)),
            encryption_type,
            aes_key,
            hmac_key,
        }
    }

    pub fn is_vault_encrypted(cipher_text: &str) -> bool {
        cipher_text.starts_with("vault:")
    }

    pub async fn get_vault_token(&self) -> Result<String, AppError> {
        if self.vault_auth_method == "TOKEN" {
            if let Some(token) = &self.vault_token {
                if !token.trim().is_empty() {
                    return Ok(token.clone());
                }
            }
        }

        // Check cached token
        {
            let guard = self.cached_token.read().await;
            if let Some((token, exp)) = guard.as_ref() {
                if Instant::now() < *exp {
                    return Ok(token.clone());
                }
            }
        }

        // Authenticate using Kubernetes Auth
        if self.vault_auth_method == "KUBERNETES" {
            let mut guard = self.cached_token.write().await;
            // Double check
            if let Some((token, exp)) = guard.as_ref() {
                if Instant::now() < *exp {
                    return Ok(token.clone());
                }
            }

            match tokio::fs::read_to_string(&self.vault_k8s_jwt_path).await {
                Ok(jwt) => {
                    let jwt = jwt.trim();
                    let login_url = format!("{}/v1/auth/kubernetes/login", self.vault_uri);
                    let body = serde_json::json!({
                        "role": self.vault_k8s_role,
                        "jwt": jwt
                    });

                    match self.http_client.post(&login_url).json(&body).send().await {
                        Ok(res) if res.status().is_success() => {
                            if let Ok(json) = res.json::<serde_json::Value>().await {
                                if let Some(client_token) =
                                    json.pointer("/auth/client_token").and_then(|t| t.as_str())
                                {
                                    let lease_duration = json
                                        .pointer("/auth/lease_duration")
                                        .and_then(|d| d.as_u64())
                                        .unwrap_or(3600);
                                    let valid_sec =
                                        std::cmp::max(60, lease_duration.saturating_sub(60));
                                    let expiration =
                                        Instant::now() + Duration::from_secs(valid_sec);
                                    let token_str = client_token.to_string();
                                    *guard = Some((token_str.clone(), expiration));
                                    info!("Authenticated with Vault via Kubernetes Auth. Valid for {}s", valid_sec);
                                    return Ok(token_str);
                                }
                            }
                        }
                        Ok(res) => {
                            warn!(
                                "Vault k8s login failed with status {}: {:?}",
                                res.status(),
                                res.text().await
                            );
                        }
                        Err(e) => {
                            warn!("Failed to communicate with Vault k8s login: {}", e);
                        }
                    }
                }
                Err(e) => {
                    warn!(
                        "ServiceAccount token file not readable at {}: {}",
                        self.vault_k8s_jwt_path, e
                    );
                }
            }
        }

        // Fallback to configured token or root
        if let Some(t) = &self.vault_token {
            if !t.trim().is_empty() {
                return Ok(t.clone());
            }
        }
        Ok("root".to_string())
    }

    pub async fn decrypt(&self, cipher_text: &str) -> Result<String, AppError> {
        let trimmed = cipher_text.trim();
        if trimmed.is_empty() {
            return Ok(cipher_text.to_string());
        }

        // 1. Vault Transit Decrypt
        if Self::is_vault_encrypted(trimmed) {
            match self.get_vault_token().await {
                Ok(token) => {
                    let url = format!(
                        "{}/v1/transit/decrypt/{}",
                        self.vault_uri, self.vault_key_name
                    );
                    let body = serde_json::json!({
                        "ciphertext": trimmed
                    });

                    match self
                        .http_client
                        .post(&url)
                        .header("X-Vault-Token", &token)
                        .json(&body)
                        .send()
                        .await
                    {
                        Ok(res) if res.status().is_success() => {
                            if let Ok(json) = res.json::<serde_json::Value>().await {
                                if let Some(b64) =
                                    json.pointer("/data/plaintext").and_then(|p| p.as_str())
                                {
                                    if let Ok(bytes) = BASE64.decode(b64) {
                                        if let Ok(plain) = String::from_utf8(bytes) {
                                            return Ok(plain);
                                        }
                                    }
                                }
                            }
                            warn!("Vault Transit decrypt returned unexpected body structure");
                        }
                        Ok(res) => {
                            let status = res.status();
                            let text = res.text().await.unwrap_or_default();
                            warn!(
                                "Vault Transit decrypt failed with status {}: {}",
                                status, text
                            );
                        }
                        Err(e) => {
                            error!("Vault Transit decrypt request failed: {}", e);
                        }
                    }
                }
                Err(e) => {
                    error!("Failed to obtain Vault token for decryption: {}", e);
                }
            }
            return Ok(cipher_text.to_string());
        }

        // 2. Legacy Local AES-GCM Decrypt (12-byte IV + ciphertext + 16-byte tag)
        if let Ok(combined) = BASE64.decode(trimmed) {
            if combined.len() >= 28 {
                let (iv, ct) = combined.split_at(12);
                let cipher = Aes256Gcm::new_from_slice(&self.aes_key)
                    .map_err(|e| AppError::Internal(format!("AES key init failed: {}", e)))?;
                let nonce = Nonce::from_slice(iv);
                if let Ok(plaintext_bytes) = cipher.decrypt(nonce, ct) {
                    if let Ok(plain) = String::from_utf8(plaintext_bytes) {
                        return Ok(plain);
                    }
                }
            }
        }

        Ok(cipher_text.to_string())
    }

    pub async fn decrypt_until_plaintext(&self, val: &str) -> String {
        let mut current = val.to_string();
        for _ in 0..5 {
            match self.decrypt(&current).await {
                Ok(next) if next != current => {
                    current = next;
                }
                _ => break,
            }
        }
        current
    }

    pub async fn encrypt(&self, plain_text: &str) -> Result<String, AppError> {
        if plain_text.is_empty() {
            return Ok(plain_text.to_string());
        }

        if self.encryption_type == "VAULT" {
            let token = self.get_vault_token().await?;
            let url = format!(
                "{}/v1/transit/encrypt/{}",
                self.vault_uri, self.vault_key_name
            );
            let b64_plain = BASE64.encode(plain_text.as_bytes());
            let body = serde_json::json!({
                "plaintext": b64_plain
            });

            let res = self
                .http_client
                .post(&url)
                .header("X-Vault-Token", token)
                .json(&body)
                .send()
                .await?;

            if res.status().is_success() {
                let json: serde_json::Value = res.json().await?;
                if let Some(ct) = json.pointer("/data/ciphertext").and_then(|c| c.as_str()) {
                    return Ok(ct.to_string());
                }
            }
            warn!("Vault Transit encrypt failed, falling back to local AES");
        }

        // Local AES-GCM
        let mut iv = [0u8; 12];
        rand::thread_rng().fill_bytes(&mut iv);
        let cipher = Aes256Gcm::new_from_slice(&self.aes_key)
            .map_err(|e| AppError::Internal(format!("AES key init failed: {}", e)))?;
        let nonce = Nonce::from_slice(&iv);
        let ciphertext = cipher
            .encrypt(nonce, plain_text.as_bytes())
            .map_err(|e| AppError::Internal(format!("AES encryption failed: {}", e)))?;

        let mut combined = Vec::with_capacity(12 + ciphertext.len());
        combined.extend_from_slice(&iv);
        combined.extend_from_slice(&ciphertext);

        Ok(BASE64.encode(&combined))
    }

    pub async fn generate_blind_index(&self, plain_text: &str) -> Result<String, AppError> {
        if plain_text.is_empty() {
            return Ok(plain_text.to_string());
        }

        if self.encryption_type == "VAULT" {
            if let Ok(token) = self.get_vault_token().await {
                let url = format!("{}/v1/transit/hmac/{}", self.vault_uri, self.vault_key_name);
                let b64_plain = BASE64.encode(plain_text.as_bytes());
                let body = serde_json::json!({
                    "input": b64_plain
                });

                if let Ok(res) = self
                    .http_client
                    .post(&url)
                    .header("X-Vault-Token", token)
                    .json(&body)
                    .send()
                    .await
                {
                    if res.status().is_success() {
                        if let Ok(json) = res.json::<serde_json::Value>().await {
                            if let Some(hmac) = json.pointer("/data/hmac").and_then(|h| h.as_str())
                            {
                                return Ok(hmac.to_string());
                            }
                        }
                    }
                }
            }
        }

        // Local HMAC-SHA256
        let mut mac = <HmacSha256 as Mac>::new_from_slice(&self.hmac_key)
            .map_err(|e| AppError::Internal(format!("HMAC init failed: {}", e)))?;
        mac.update(plain_text.as_bytes());
        let result = mac.finalize();
        Ok(hex::encode(result.into_bytes()))
    }
}
