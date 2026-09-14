use crate::error::AppResult;
use crate::models::dashboard::*;
use crate::state::AppState;
use axum::http::StatusCode;
use axum::{
    extract::{Query, State},
    response::IntoResponse,
    Json,
};
use serde::Deserialize;
use uuid::Uuid;

#[derive(Debug, Deserialize)]
pub struct LeaseSummaryQuery {
    pub organization_id: Option<Uuid>,
}

pub async fn get_stats(State(state): State<AppState>) -> AppResult<impl IntoResponse> {
    let stats = state.dashboard_service.get_stats().await?;
    Ok(Json(stats))
}

pub async fn get_approval_trends(State(state): State<AppState>) -> AppResult<impl IntoResponse> {
    let trends = state.dashboard_service.get_approval_trends().await?;
    Ok(Json(trends))
}

pub async fn get_dq_trends(State(state): State<AppState>) -> AppResult<impl IntoResponse> {
    let trends = state.dashboard_service.get_dq_trends().await?;
    Ok(Json(trends))
}

pub async fn get_domain_distribution(
    State(state): State<AppState>,
) -> AppResult<impl IntoResponse> {
    let dist = state.dashboard_service.get_domain_distribution().await?;
    Ok(Json(dist))
}

pub async fn get_dq_severity_distribution(
    State(state): State<AppState>,
) -> AppResult<impl IntoResponse> {
    let dist = state
        .dashboard_service
        .get_dq_severity_distribution()
        .await?;
    Ok(Json(dist))
}

pub async fn get_lease_summary(
    State(state): State<AppState>,
    Query(params): Query<LeaseSummaryQuery>,
) -> AppResult<impl IntoResponse> {
    let summary = state
        .dashboard_service
        .get_lease_summary(params.organization_id)
        .await?;
    Ok(Json(summary))
}
