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
    auth: AuthUser,
) -> Result<Json<Vec<ClassificationAxis>>, AppError> {
    if let Some(domain_id) = query.domain_id {
        if let Some(org_id) = auth.organization_id {
            let domain_org: Option<Uuid> = sqlx::query_scalar("SELECT organization_id FROM domain WHERE id = $1")
                .bind(domain_id)
                .fetch_optional(&state.db)
                .await?;
            if domain_org != Some(org_id) {
                return Err(AppError::Forbidden("접근 권한이 없는 도메인입니다".to_string()));
            }
        }
        let axes = ClassificationRepository::find_axes(&state.db, Some(domain_id)).await?;
        Ok(Json(axes))
    } else {
        let org_filter = auth.organization_id;
        let axes = sqlx::query_as::<_, ClassificationAxis>(
            r#"
            SELECT ca.* FROM classification_axis ca
            JOIN domain d ON ca.domain_id = d.id
            WHERE ($1::uuid IS NULL OR d.organization_id = $1)
            ORDER BY ca.sort_order ASC, ca.created_at ASC
            "#
        )
        .bind(org_filter)
        .fetch_all(&state.db)
        .await?;
        Ok(Json(axes))
    }
}

pub async fn get_nodes(
    State(state): State<AppState>,
    Query(query): Query<ClassificationQuery>,
    auth: AuthUser,
) -> Result<Json<Vec<ClassificationNode>>, AppError> {
    if let Some(domain_id) = query.domain_id {
        if let Some(org_id) = auth.organization_id {
            let domain_org: Option<Uuid> = sqlx::query_scalar("SELECT organization_id FROM domain WHERE id = $1")
                .bind(domain_id)
                .fetch_optional(&state.db)
                .await?;
            if domain_org != Some(org_id) {
                return Err(AppError::Forbidden("접근 권한이 없는 도메인입니다".to_string()));
            }
        }
        let nodes = ClassificationRepository::find_nodes(&state.db, Some(domain_id)).await?;
        Ok(Json(nodes))
    } else {
        let org_filter = auth.organization_id;
        let nodes = sqlx::query_as::<_, ClassificationNode>(
            r#"
            SELECT cn.* FROM classification_node cn
            JOIN domain d ON cn.domain_id = d.id
            WHERE cn.is_deleted = false
              AND ($1::uuid IS NULL OR d.organization_id = $1)
            ORDER BY cn.depth ASC, cn.node_order ASC
            "#
        )
        .bind(org_filter)
        .fetch_all(&state.db)
        .await?;
        Ok(Json(nodes))
    }
}
