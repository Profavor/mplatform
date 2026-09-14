use crate::error::AppError;
use sha2::{Digest, Sha256};
use std::path::{Path, PathBuf};
use tokio::fs;

pub struct FileService;

impl FileService {
    pub async fn ensure_upload_dir(base_dir: &str) -> Result<PathBuf, AppError> {
        let dir = PathBuf::from(base_dir);
        if !dir.exists() {
            fs::create_dir_all(&dir)
                .await
                .map_err(|e| AppError::Internal(format!("Failed to create upload dir: {e}")))?;
        }
        Ok(dir)
    }

    pub fn guess_mime_type(file_name: &str) -> &'static str {
        let lower = file_name.to_lowercase();
        if lower.ends_with(".jpg") || lower.ends_with(".jpeg") {
            "image/jpeg"
        } else if lower.ends_with(".png") {
            "image/png"
        } else if lower.ends_with(".gif") {
            "image/gif"
        } else if lower.ends_with(".webp") {
            "image/webp"
        } else if lower.ends_with(".svg") {
            "image/svg+xml"
        } else if lower.ends_with(".pdf") {
            "application/pdf"
        } else if lower.ends_with(".xlsx") {
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        } else if lower.ends_with(".xls") {
            "application/vnd.ms-excel"
        } else if lower.ends_with(".docx") {
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        } else if lower.ends_with(".doc") {
            "application/msword"
        } else if lower.ends_with(".pptx") {
            "application/vnd.openxmlformats-officedocument.presentationml.presentation"
        } else if lower.ends_with(".ppt") {
            "application/vnd.ms-powerpoint"
        } else if lower.ends_with(".csv") {
            "text/csv; charset=utf-8"
        } else if lower.ends_with(".txt") {
            "text/plain; charset=utf-8"
        } else if lower.ends_with(".json") {
            "application/json"
        } else if lower.ends_with(".zip") {
            "application/zip"
        } else if lower.ends_with(".tar") || lower.ends_with(".tar.gz") || lower.ends_with(".tgz") {
            "application/x-tar"
        } else {
            "application/octet-stream"
        }
    }

    pub async fn save_file(
        base_dir: &str,
        minio_url: &str,
        minio_bucket: &str,
        original_name: &str,
        data: &[u8],
        content_type: Option<&str>,
    ) -> Result<(String, usize), AppError> {
        let dir = Self::ensure_upload_dir(base_dir).await?;
        let ext = Path::new(original_name)
            .extension()
            .and_then(|e| e.to_str())
            .map(|e| format!(".{e}"))
            .unwrap_or_default();

        let mut hasher = Sha256::new();
        hasher.update(data);
        let hash_str = hex::encode(hasher.finalize());
        let saved_name = format!("{}{}", hash_str, ext);

        // 1. Write to local cache disk
        let dest = dir.join(&saved_name);
        let _ = fs::write(&dest, data).await;

        // 2. Upload to MinIO
        if !minio_url.is_empty() {
            let url = format!(
                "{}/{}/{}",
                minio_url.trim_end_matches('/'),
                minio_bucket,
                saved_name
            );
            let ct = content_type.unwrap_or_else(|| Self::guess_mime_type(original_name));
            let client = reqwest::Client::builder()
                .timeout(std::time::Duration::from_secs(15))
                .build()
                .unwrap_or_default();

            match client
                .put(&url)
                .header(reqwest::header::CONTENT_TYPE, ct)
                .body(data.to_vec())
                .send()
                .await
            {
                Ok(resp) => {
                    if !resp.status().is_success() {
                        tracing::warn!(
                            "MinIO upload returned status {}: {}",
                            resp.status(),
                            url
                        );
                    }
                }
                Err(err) => {
                    tracing::warn!("MinIO upload failed ({err}): {url}");
                }
            }
        }

        Ok((saved_name, data.len()))
    }

    pub async fn read_file(
        base_dir: &str,
        minio_url: &str,
        minio_bucket: &str,
        file_name: &str,
    ) -> Result<(Vec<u8>, Option<String>), AppError> {
        // 1. Check MinIO first
        if !minio_url.is_empty() {
            let url = format!(
                "{}/{}/{}",
                minio_url.trim_end_matches('/'),
                minio_bucket,
                file_name
            );
            let client = reqwest::Client::builder()
                .timeout(std::time::Duration::from_secs(15))
                .build()
                .unwrap_or_default();

            if let Ok(resp) = client.get(&url).send().await {
                if resp.status().is_success() {
                    let content_type = resp
                        .headers()
                        .get(reqwest::header::CONTENT_TYPE)
                        .and_then(|v| v.to_str().ok())
                        .map(|s| s.to_string());
                    if let Ok(bytes) = resp.bytes().await {
                        // Also cache to local disk asynchronously in background
                        let cache_dir = PathBuf::from(base_dir);
                        let cache_path = cache_dir.join(file_name);
                        let bytes_clone = bytes.clone();
                        tokio::spawn(async move {
                            let _ = fs::write(cache_path, bytes_clone).await;
                        });

                        return Ok((bytes.to_vec(), content_type));
                    }
                }
            }
        }

        // 2. Fallback to local storage
        let dir = PathBuf::from(base_dir);
        let file_path = dir.join(file_name);

        if file_path.exists() {
            let bytes = fs::read(&file_path)
                .await
                .map_err(|e| AppError::Internal(format!("Failed to read file: {e}")))?;
            return Ok((bytes, None));
        }

        Err(AppError::NotFound(format!("File not found: {file_name}")))
    }

    pub async fn get_file_metadata(
        base_dir: &str,
        minio_url: &str,
        minio_bucket: &str,
        file_name: &str,
    ) -> Result<(u64, Option<String>), AppError> {
        // 1. Check MinIO HEAD
        if !minio_url.is_empty() {
            let url = format!(
                "{}/{}/{}",
                minio_url.trim_end_matches('/'),
                minio_bucket,
                file_name
            );
            let client = reqwest::Client::builder()
                .timeout(std::time::Duration::from_secs(5))
                .build()
                .unwrap_or_default();

            if let Ok(resp) = client.head(&url).send().await {
                if resp.status().is_success() {
                    let size = resp
                        .headers()
                        .get(reqwest::header::CONTENT_LENGTH)
                        .and_then(|v| v.to_str().ok())
                        .and_then(|v| v.parse::<u64>().ok())
                        .unwrap_or(0);
                    let content_type = resp
                        .headers()
                        .get(reqwest::header::CONTENT_TYPE)
                        .and_then(|v| v.to_str().ok())
                        .map(|s| s.to_string());
                    return Ok((size, content_type));
                }
            }
        }

        // 2. Fallback to local disk
        let dir = PathBuf::from(base_dir);
        let file_path = dir.join(file_name);

        if file_path.exists() {
            let metadata = fs::metadata(&file_path)
                .await
                .map_err(|e| AppError::Internal(format!("Failed to get file metadata: {e}")))?;
            return Ok((metadata.len(), None));
        }

        Err(AppError::NotFound(format!("File not found: {file_name}")))
    }
}
