use crate::error::AppResult;
use crate::state::AppState;
use axum::http::StatusCode;
use axum::{
    extract::{Query, State},
    response::IntoResponse,
    Json,
};
use serde::Deserialize;

#[derive(Debug, Deserialize)]
pub struct ErrorLogQuery {
    #[serde(default)]
    pub page: Option<i64>,
    #[serde(default)]
    pub size: Option<i64>,
    #[serde(default)]
    pub limit: Option<i64>,
}

pub async fn get_features(State(state): State<AppState>) -> AppResult<impl IntoResponse> {
    let features = state.system_service.get_features().await?;
    Ok(Json(features))
}

pub async fn get_config(State(state): State<AppState>) -> AppResult<impl IntoResponse> {
    let config = state.system_service.get_config().await?;
    Ok(Json(config))
}

pub async fn get_diagnostics(State(state): State<AppState>) -> AppResult<impl IntoResponse> {
    let report = state.system_service.get_diagnostics().await?;
    Ok(Json(report))
}

pub async fn get_freshness(State(state): State<AppState>) -> AppResult<impl IntoResponse> {
    let report = state.system_service.get_freshness().await?;
    Ok(Json(report))
}

pub async fn get_volume(State(state): State<AppState>) -> AppResult<impl IntoResponse> {
    let report = state.system_service.get_volume_anomalies().await?;
    Ok(Json(report))
}

pub async fn get_sla(State(state): State<AppState>) -> AppResult<impl IntoResponse> {
    let report = state.system_service.get_sla_contracts().await?;
    Ok(Json(report))
}

pub async fn get_error_logs(
    State(state): State<AppState>,
    Query(params): Query<ErrorLogQuery>,
) -> AppResult<impl IntoResponse> {
    let page = params.page.unwrap_or(0);
    let size = params.size.or(params.limit).unwrap_or(20);
    let (logs, total) = state.system_service.get_error_logs(page, size).await?;
    let total_pages = if size > 0 { (total + size - 1) / size } else { 0 };

    Ok(Json(serde_json::json!({
        "content": logs,
        "totalElements": total,
        "totalPages": total_pages,
        "size": size,
        "number": page
    })))
}
