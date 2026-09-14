use crate::error::AppError;
use crate::middleware::auth::AuthUser;
use crate::models::classification::{ClassificationAxis, ClassificationNode};
use crate::repositories::classification_repo::ClassificationRepository;
use crate::state::AppState;
use axum::http::StatusCode;
use axum::{
    extract::{Query, State},
    Json,
};
use serde::Deserialize;
use uuid::Uuid;

#[derive(Debug, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct ClassificationQuery {
    pub domain_id: Option<Uuid>,
}

pub async fn get_axes(
    State(state): State<AppState>,
    Query(query): Query<ClassificationQuery>,
    _auth: AuthUser,
) -> Result<Json<Vec<ClassificationAxis>>, AppError> {
    let axes = ClassificationRepository::find_axes(&state.db, query.domain_id).await?;
    Ok(Json(axes))
}

pub async fn get_nodes(
    State(state): State<AppState>,
    Query(query): Query<ClassificationQuery>,
    _auth: AuthUser,
) -> Result<Json<Vec<ClassificationNode>>, AppError> {
    let nodes = ClassificationRepository::find_nodes(&state.db, query.domain_id).await?;
    Ok(Json(nodes))
}
