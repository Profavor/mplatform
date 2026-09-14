use crate::error::AppError;
use crate::models::classification::{ClassificationAxis, ClassificationNode};
use sqlx::PgPool;
use uuid::Uuid;

pub struct ClassificationRepository;

impl ClassificationRepository {
    pub async fn find_axes(pool: &PgPool, domain_id: Option<Uuid>) -> Result<Vec<ClassificationAxis>, AppError> {
        let axes = if let Some(did) = domain_id {
            sqlx::query_as::<_, ClassificationAxis>(
                r#"
                SELECT id, domain_id, axis_code, name, description, is_default, sort_order, created_at, updated_at
                FROM classification_axis
                WHERE domain_id = $1
                ORDER BY sort_order ASC, created_at ASC
                "#,
            )
            .bind(did)
            .fetch_all(pool)
            .await?
        } else {
            sqlx::query_as::<_, ClassificationAxis>(
                r#"
                SELECT id, domain_id, axis_code, name, description, is_default, sort_order, created_at, updated_at
                FROM classification_axis
                ORDER BY sort_order ASC, created_at ASC
                "#,
            )
            .fetch_all(pool)
            .await?
        };

        Ok(axes)
    }

    pub async fn find_nodes(pool: &PgPool, domain_id: Option<Uuid>) -> Result<Vec<ClassificationNode>, AppError> {
        let nodes = if let Some(did) = domain_id {
            sqlx::query_as::<_, ClassificationNode>(
                r#"
                SELECT id, domain_id, axis_id, parent_id, name, depth, node_order,
                       path, icon, is_deleted, detail_layout_config, created_at, updated_at, deleted_at
                FROM classification_node
                WHERE domain_id = $1 AND (is_deleted IS NULL OR is_deleted = false)
                ORDER BY depth ASC, node_order ASC
                "#,
            )
            .bind(did)
            .fetch_all(pool)
            .await?
        } else {
            sqlx::query_as::<_, ClassificationNode>(
                r#"
                SELECT id, domain_id, axis_id, parent_id, name, depth, node_order,
                       path, icon, is_deleted, detail_layout_config, created_at, updated_at, deleted_at
                FROM classification_node
                WHERE (is_deleted IS NULL OR is_deleted = false)
                ORDER BY depth ASC, node_order ASC
                LIMIT 500
                "#,
            )
            .fetch_all(pool)
            .await?
        };

        Ok(nodes)
    }

    pub async fn find_node_by_id(pool: &PgPool, id: Uuid) -> Result<Option<ClassificationNode>, AppError> {
        let node = sqlx::query_as::<_, ClassificationNode>(
            r#"
            SELECT id, domain_id, axis_id, parent_id, name, depth, node_order,
                   path, icon, is_deleted, detail_layout_config, created_at, updated_at, deleted_at
            FROM classification_node
            WHERE id = $1
            "#,
        )
        .bind(id)
        .fetch_optional(pool)
        .await?;

        Ok(node)
    }
}
