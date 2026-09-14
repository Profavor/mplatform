use crate::error::AppError;
use crate::middleware::auth::AuthUser;
use crate::models::hash_chain::LedgerVerificationResponse;
use crate::services::hash_chain_service::HashChainService;
use crate::state::AppState;
use axum::http::StatusCode;
use axum::{
    extract::{Path, State},
    Json,
};
use uuid::Uuid;

pub async fn verify_ledger(
    State(state): State<AppState>,
    Path(record_id): Path<Uuid>,
    _auth: AuthUser,
) -> Result<Json<LedgerVerificationResponse>, AppError> {
    let response = HashChainService::verify_record_ledger(&state.db, record_id).await?;
    Ok(Json(response))
}
