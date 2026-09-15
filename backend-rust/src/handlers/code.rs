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
    auth: OptionalAuthUser,
) -> Result<Json<Vec<CodeGroup>>, AppError> {
    let groups = match auth.0 {
        Some(user) => CodeService::get_groups_for_admin(&state.db, &user).await?,
        None => {
            crate::repositories::code_repo::CodeRepository::find_groups_by_org(&state.db, None).await?
        }
    };
    Ok(Json(groups))
}

pub async fn get_code_groups_paged(
    State(state): State<AppState>,
    auth: OptionalAuthUser,
) -> Result<Json<serde_json::Value>, AppError> {
    let groups = match auth.0 {
        Some(user) => CodeService::get_groups_for_admin(&state.db, &user).await?,
        None => {
            crate::repositories::code_repo::CodeRepository::find_groups_by_org(&state.db, None).await?
        }
    };
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
    auth: OptionalAuthUser,
) -> Result<Json<CodeGroup>, AppError> {
    let group = if let Ok(group_uuid) = Uuid::parse_str(&id) {
        if let Ok(group) = CodeService::get_group_by_id(&state.db, group_uuid).await {
            group
        } else {
            CodeService::get_group_by_code(&state.db, &id).await?
        }
    } else {
        CodeService::get_group_by_code(&state.db, &id).await?
    };

    if let Some(user) = auth.0 {
        if let Some(org_id) = group.organization_id {
            if Some(org_id) != user.organization_id {
                return Err(AppError::Forbidden("접근 권한이 없는 코드 그룹입니다".to_string()));
            }
        }
    }

    Ok(Json(group))
}

pub async fn create_code_group(
    State(state): State<AppState>,
    auth: AuthUser,
    Json(mut req): Json<CodeGroupRequest>,
) -> Result<Json<CodeGroup>, AppError> {
    req.organization_id = auth.organization_id;
    let group = CodeService::create_group(&state.db, req).await?;
    Ok(Json(group))
}

pub async fn update_code_group(
    State(state): State<AppState>,
    Path(id): Path<String>,
    auth: AuthUser,
    Json(mut req): Json<CodeGroupRequest>,
) -> Result<Json<CodeGroup>, AppError> {
    let group = if let Ok(u) = Uuid::parse_str(&id) {
        CodeService::get_group_by_id(&state.db, u).await?
    } else {
        CodeService::get_group_by_code(&state.db, &id).await?
    };

    match group.organization_id {
        None => {
            return Err(AppError::Forbidden(
                "공통 시스템 코드 그룹은 수정할 수 없습니다".to_string(),
            ))
        }
        Some(org_id) => {
            if Some(org_id) != auth.organization_id {
                return Err(AppError::Forbidden(
                    "타 조직의 코드 그룹은 수정할 수 없습니다".to_string(),
                ));
            }
        }
    }
    req.organization_id = auth.organization_id;

    let group = CodeService::update_group(&state.db, group.id, req).await?;
    Ok(Json(group))
}

pub async fn delete_code_group(
    State(state): State<AppState>,
    Path(id): Path<String>,
    auth: AuthUser,
) -> Result<StatusCode, AppError> {
    let group = if let Ok(u) = Uuid::parse_str(&id) {
        CodeService::get_group_by_id(&state.db, u).await?
    } else {
        CodeService::get_group_by_code(&state.db, &id).await?
    };

    match group.organization_id {
        None => {
            return Err(AppError::Forbidden(
                "공통 시스템 코드 그룹은 삭제할 수 없습니다".to_string(),
            ))
        }
        Some(org_id) => {
            if Some(org_id) != auth.organization_id {
                return Err(AppError::Forbidden(
                    "타 조직의 코드 그룹은 삭제할 수 없습니다".to_string(),
                ));
            }
        }
    }

    CodeService::delete_group(&state.db, group.id).await?;
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
    auth: AuthUser,
    Json(req): Json<CodeDetailRequest>,
) -> Result<Json<CodeDetail>, AppError> {
    let group = if let Ok(u) = Uuid::parse_str(&id) {
        CodeService::get_group_by_id(&state.db, u).await?
    } else {
        CodeService::get_group_by_code(&state.db, &id).await?
    };

    match group.organization_id {
        None => {
            return Err(AppError::Forbidden(
                "공통 시스템 코드에는 상세 코드를 추가할 수 없습니다".to_string(),
            ))
        }
        Some(org_id) => {
            if Some(org_id) != auth.organization_id {
                return Err(AppError::Forbidden(
                    "타 조직의 코드 그룹에는 상세 코드를 추가할 수 없습니다".to_string(),
                ));
            }
        }
    }

    let detail = CodeService::create_detail(&state.db, group.id, req).await?;
    Ok(Json(detail))
}

pub async fn update_detail(
    State(state): State<AppState>,
    Path(id): Path<Uuid>,
    auth: AuthUser,
    Json(req): Json<CodeDetailRequest>,
) -> Result<Json<CodeDetail>, AppError> {
    let parent_org: Option<Option<Uuid>> = sqlx::query_scalar(
        "SELECT g.organization_id FROM code_group g JOIN code_detail d ON d.group_id = g.id WHERE d.id = $1"
    )
    .bind(id)
    .fetch_optional(&state.db)
    .await?;

    match parent_org {
        Some(Some(org_id)) => {
            if Some(org_id) != auth.organization_id {
                return Err(AppError::Forbidden("타 조직의 상세 코드는 수정할 수 없습니다".to_string()));
            }
        }
        Some(None) => {
            return Err(AppError::Forbidden("공통 시스템 상세 코드는 수정할 수 없습니다".to_string()));
        }
        None => return Err(AppError::NotFound("Code detail not found".to_string())),
    }

    let detail = CodeService::update_detail(&state.db, id, req).await?;
    Ok(Json(detail))
}

pub async fn delete_detail(
    State(state): State<AppState>,
    Path(detail_id): Path<Uuid>,
    auth: AuthUser,
) -> Result<StatusCode, AppError> {
    let parent_org: Option<Option<Uuid>> = sqlx::query_scalar(
        "SELECT g.organization_id FROM code_group g JOIN code_detail d ON d.group_id = g.id WHERE d.id = $1"
    )
    .bind(detail_id)
    .fetch_optional(&state.db)
    .await?;

    match parent_org {
        Some(Some(org_id)) => {
            if Some(org_id) != auth.organization_id {
                return Err(AppError::Forbidden("타 조직의 상세 코드는 삭제할 수 없습니다".to_string()));
            }
        }
        Some(None) => {
            return Err(AppError::Forbidden("공통 시스템 상세 코드는 삭제할 수 없습니다".to_string()));
        }
        None => return Err(AppError::NotFound("Code detail not found".to_string())),
    }

    CodeService::delete_detail(&state.db, detail_id).await?;
    Ok(axum::http::StatusCode::NO_CONTENT)
}

pub async fn dump_seed(auth: AuthUser) -> Result<Json<Value>, AppError> {
    if !auth.is_org_admin() {
        return Err(AppError::Forbidden("관리자만 코드 시드를 덤프할 수 있습니다".to_string()));
    }
    Ok(Json(
        json!({ "status": "success", "message": "Code seed dumped" }),
    ))
}

pub async fn sync_seed(auth: AuthUser) -> Result<Json<Value>, AppError> {
    if !auth.is_org_admin() {
        return Err(AppError::Forbidden("관리자만 코드 시드를 동기화할 수 있습니다".to_string()));
    }
    Ok(Json(
        json!({ "status": "success", "message": "Code seed synced" }),
    ))
}

pub async fn export_codes(
    State(state): State<AppState>,
    auth: AuthUser,
) -> Result<Json<Vec<CodeGroup>>, AppError> {
    let groups = CodeService::get_groups_for_admin(&state.db, &auth).await?;
    Ok(Json(groups))
}

pub async fn import_codes(
    auth: AuthUser,
    Json(_data): Json<Value>,
) -> Result<StatusCode, AppError> {
    if !auth.is_org_admin() {
        return Err(AppError::Forbidden("관리자만 코드를 일괄 임포트할 수 있습니다".to_string()));
    }
    Ok(axum::http::StatusCode::OK)
}
