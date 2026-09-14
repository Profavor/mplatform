use axum::extract::Query;
use axum::http::{header, HeaderMap, StatusCode};
use crate::error::AppError;
use crate::middleware::auth::{AuthUser, OptionalAuthUser};
use crate::models::file::{FileInfoResponse, FileUploadResponse};
use crate::services::file_service::FileService;
use crate::state::AppState;
use axum::{
    extract::{Multipart, Path, State},
    response::IntoResponse,
    Json,
};
use serde::Deserialize;

#[derive(Debug, Deserialize)]
pub struct DownloadQuery {
    pub name: Option<String>,
    pub size: Option<u64>,
}

fn sanitize_filename(name: &str) -> Result<String, AppError> {
    let decoded = urlencoding::decode(name).unwrap_or(std::borrow::Cow::Borrowed(name));
    let clean = decoded
        .trim()
        .trim_matches(|c| c == '[' || c == ']' || c == '"' || c == '\'');
    if clean.contains("..") || clean.contains('/') || clean.contains('\\') {
        return Err(AppError::BadRequest("Invalid file name".to_string()));
    }
    if clean.is_empty() {
        return Err(AppError::BadRequest("File name cannot be empty".to_string()));
    }
    Ok(clean.to_string())
}

pub async fn upload_file(
    State(state): State<AppState>,
    _auth: AuthUser,
    mut multipart: Multipart,
) -> Result<Json<FileUploadResponse>, AppError> {
    let mut original_name = "unknown_file".to_string();
    let mut file_bytes = Vec::new();
    let mut detected_content_type: Option<String> = None;

    while let Some(field) = multipart
        .next_field()
        .await
        .map_err(|e| AppError::BadRequest(format!("Multipart error: {e}")))?
    {
        if let Some(name) = field.file_name() {
            original_name = name.to_string();
        }
        if let Some(ct) = field.content_type() {
            detected_content_type = Some(ct.to_string());
        }

        let data = field
            .bytes()
            .await
            .map_err(|e| AppError::BadRequest(format!("Failed to read file field: {e}")))?;

        file_bytes.extend_from_slice(&data);
    }

    if file_bytes.is_empty() {
        return Err(AppError::BadRequest("Uploaded file is empty".to_string()));
    }

    let (saved_name, size) = FileService::save_file(
        &state.config.upload_dir,
        &state.config.minio_url,
        &state.config.minio_bucket,
        &original_name,
        &file_bytes,
        detected_content_type.as_deref(),
    )
    .await?;

    let encoded_name = urlencoding::encode(&original_name);
    let download_url = format!("/api/files/download/{saved_name}?name={encoded_name}&size={size}");

    Ok(Json(FileUploadResponse {
        file_name: original_name,
        url: download_url,
        size: size.to_string(),
        file_size: size.to_string(),
    }))
}

pub async fn download_file(
    State(state): State<AppState>,
    Path(file_name): Path<String>,
    Query(query): Query<DownloadQuery>,
    _auth: OptionalAuthUser,
) -> Result<impl IntoResponse, AppError> {
    let clean_file_name = sanitize_filename(&file_name)?;
    let (bytes, minio_ct) = FileService::read_file(
        &state.config.upload_dir,
        &state.config.minio_url,
        &state.config.minio_bucket,
        &clean_file_name,
    )
    .await?;

    let raw_download_name = query.name.as_deref().unwrap_or(&clean_file_name);
    let decoded_download_name = urlencoding::decode(raw_download_name)
        .unwrap_or(std::borrow::Cow::Borrowed(raw_download_name))
        .trim_matches(|c| c == '[' || c == ']' || c == '"' || c == '\'')
        .to_string();
    let download_name = if decoded_download_name.is_empty() {
        "file".to_string()
    } else {
        decoded_download_name
    };

    let safe_ascii_name: String = download_name
        .chars()
        .map(|c| {
            if c.is_ascii_alphanumeric() || c == '.' || c == '_' || c == '-' {
                c
            } else {
                '_'
            }
        })
        .collect();
    let safe_ascii = if safe_ascii_name.is_empty() {
        "file".to_string()
    } else {
        safe_ascii_name
    };
    let encoded_utf8 = urlencoding::encode(&download_name);

    let content_type =
        minio_ct.unwrap_or_else(|| FileService::guess_mime_type(&download_name).to_string());

    let mut headers = HeaderMap::new();
    headers.insert(
        header::CONTENT_DISPOSITION,
        format!(
            "attachment; filename=\"{safe_ascii}\"; filename*=UTF-8''{encoded_utf8}"
        )
        .parse()
        .map_err(|e| AppError::Internal(format!("Invalid Content-Disposition header: {e}")))?,
    );
    headers.insert(
        header::CONTENT_TYPE,
        content_type
            .parse()
            .unwrap_or_else(|_| "application/octet-stream".parse().unwrap()),
    );

    Ok((StatusCode::OK, headers, bytes))
}

pub async fn get_file_info(
    State(state): State<AppState>,
    Path(file_name): Path<String>,
    _auth: OptionalAuthUser,
) -> Result<Json<FileInfoResponse>, AppError> {
    let clean_file_name = sanitize_filename(&file_name)?;
    let (size, minio_ct) = FileService::get_file_metadata(
        &state.config.upload_dir,
        &state.config.minio_url,
        &state.config.minio_bucket,
        &clean_file_name,
    )
    .await?;

    let content_type =
        minio_ct.unwrap_or_else(|| FileService::guess_mime_type(&clean_file_name).to_string());
    let download_url = format!("/api/files/download/{clean_file_name}");

    Ok(Json(FileInfoResponse {
        file_name: clean_file_name,
        size,
        content_type,
        download_url,
    }))
}
