use crate::error::AppError;
use crate::middleware::auth::AuthUser;
use crate::models::search::GlobalSearchResponse;
use crate::services::search_service::SearchService;
use crate::state::AppState;
use axum::http::StatusCode;
use axum::{
    extract::{Query, State},
    Json,
};
use serde::Deserialize;

#[derive(Debug, Deserialize)]
pub struct SearchQuery {
    pub q: Option<String>,
}

pub async fn search(
    State(state): State<AppState>,
    Query(query): Query<SearchQuery>,
    _auth: AuthUser,
) -> Result<Json<GlobalSearchResponse>, AppError> {
    let q = query.q.unwrap_or_default();
    let result = SearchService::global_search(&state.db, &q).await?;
    Ok(Json(result))
}
