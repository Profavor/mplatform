use crate::error::AppError;
use crate::middleware::auth::{AuthUser, OptionalAuthUser};
use crate::models::code::{CodeDetail, CodeDetailRequest, CodeGroup, CodeGroupRequest};
use crate::services::code_service::CodeService;
use crate::state::AppState;
use axum::http::StatusCode;
use axum::{
    extract::{Path, State},
    Json,
};
use serde_json::{json, Value};
use uuid::Uuid;

pub async fn get_code_groups(
    State(state): State<AppState>,
    _auth: OptionalAuthUser,
) -> Result<Json<Vec<CodeGroup>>, AppError> {
    let groups = CodeService::get_all_groups(&state.db).await?;
    Ok(Json(groups))
}

pub async fn get_code_groups_paged(
    State(state): State<AppState>,
    _auth: OptionalAuthUser,
) -> Result<Json<serde_json::Value>, AppError> {
    let groups = CodeService::get_all_groups(&state.db).await?;
    let total = groups.len();
    Ok(Json(json!({
        "content": groups,
        "totalElements": total,
        "totalPages": 1,
        "size": total,
        "number": 0,
        "pageSize": total,
        "pageNumber": 0,
        "first": true,
        "last": true
    })))
}

pub async fn get_code_group_by_id_or_code(
    State(state): State<AppState>,
    Path(id): Path<String>,
    _auth: OptionalAuthUser,
) -> Result<Json<CodeGroup>, AppError> {
    if let Ok(group_uuid) = Uuid::parse_str(&id) {
        if let Ok(group) = CodeService::get_group_by_id(&state.db, group_uuid).await {
            return Ok(Json(group));
        }
    }
    let group = CodeService::get_group_by_code(&state.db, &id).await?;
    Ok(Json(group))
}

pub async fn create_code_group(
    State(state): State<AppState>,
    _auth: AuthUser,
    Json(req): Json<CodeGroupRequest>,
) -> Result<Json<CodeGroup>, AppError> {
    let group = CodeService::create_group(&state.db, req).await?;
    Ok(Json(group))
}

pub async fn update_code_group(
    State(state): State<AppState>,
    Path(id): Path<String>,
    _auth: AuthUser,
    Json(req): Json<CodeGroupRequest>,
) -> Result<Json<CodeGroup>, AppError> {
    let group_id = if let Ok(u) = Uuid::parse_str(&id) {
        u
    } else {
        let g = CodeService::get_group_by_code(&state.db, &id).await?;
        g.id
    };
    let group = CodeService::update_group(&state.db, group_id, req).await?;
    Ok(Json(group))
}

pub async fn delete_code_group(
    State(state): State<AppState>,
    Path(id): Path<String>,
    _auth: AuthUser,
) -> Result<StatusCode, AppError> {
    let group_id = if let Ok(u) = Uuid::parse_str(&id) {
        u
    } else {
        let g = CodeService::get_group_by_code(&state.db, &id).await?;
        g.id
    };
    CodeService::delete_group(&state.db, group_id).await?;
    Ok(axum::http::StatusCode::NO_CONTENT)
}

pub async fn get_details_by_group_id(
    State(state): State<AppState>,
    Path(id): Path<String>,
    _auth: OptionalAuthUser,
) -> Result<Json<Vec<CodeDetail>>, AppError> {
    if let Ok(group_uuid) = Uuid::parse_str(&id) {
        let details = CodeService::get_details_by_group_id(&state.db, group_uuid).await?;
        Ok(Json(details))
    } else {
        let details = CodeService::get_details_by_group(&state.db, &id).await?;
        Ok(Json(details))
    }
}

pub async fn create_detail(
    State(state): State<AppState>,
    Path(id): Path<String>,
    _auth: AuthUser,
    Json(req): Json<CodeDetailRequest>,
) -> Result<Json<CodeDetail>, AppError> {
    let group_id = if let Ok(u) = Uuid::parse_str(&id) {
        u
    } else {
        let g = CodeService::get_group_by_code(&state.db, &id).await?;
        g.id
    };
    let detail = CodeService::create_detail(&state.db, group_id, req).await?;
    Ok(Json(detail))
}

pub async fn update_detail(
    State(state): State<AppState>,
    Path(id): Path<Uuid>,
    _auth: AuthUser,
    Json(req): Json<CodeDetailRequest>,
) -> Result<Json<CodeDetail>, AppError> {
    let detail = CodeService::update_detail(&state.db, id, req).await?;
    Ok(Json(detail))
}

pub async fn delete_detail(
    State(state): State<AppState>,
    Path(detail_id): Path<Uuid>,
    _auth: AuthUser,
) -> Result<StatusCode, AppError> {
    CodeService::delete_detail(&state.db, detail_id).await?;
    Ok(axum::http::StatusCode::NO_CONTENT)
}

pub async fn dump_seed(_auth: AuthUser) -> Result<Json<Value>, AppError> {
    Ok(Json(
        json!({ "status": "success", "message": "Code seed dumped" }),
    ))
}

pub async fn sync_seed(_auth: AuthUser) -> Result<Json<Value>, AppError> {
    Ok(Json(
        json!({ "status": "success", "message": "Code seed synced" }),
    ))
}

pub async fn export_codes(
    State(state): State<AppState>,
    _auth: AuthUser,
) -> Result<Json<Vec<CodeGroup>>, AppError> {
    let groups = CodeService::get_all_groups(&state.db).await?;
    Ok(Json(groups))
}

pub async fn import_codes(
    _auth: AuthUser,
    Json(_data): Json<Value>,
) -> Result<StatusCode, AppError> {
    Ok(axum::http::StatusCode::OK)
}
