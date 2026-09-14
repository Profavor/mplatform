use crate::error::AppError;
use crate::models::code::{CodeDetail, CodeDetailRequest, CodeGroup, CodeGroupRequest};
use sqlx::PgPool;
use uuid::Uuid;

pub struct CodeRepository;

impl CodeRepository {
    pub async fn find_groups(pool: &PgPool) -> Result<Vec<CodeGroup>, AppError> {
        let groups = sqlx::query_as::<_, CodeGroup>(
            r#"
            SELECT id, group_code, name, description, is_active, organization_id, created_at, updated_at
            FROM code_group
            WHERE is_active = true
            ORDER BY group_code ASC
            "#,
        )
        .fetch_all(pool)
        .await?;

        Ok(groups)
    }

    pub async fn find_all_groups(pool: &PgPool) -> Result<Vec<CodeGroup>, AppError> {
        let groups = sqlx::query_as::<_, CodeGroup>(
            r#"
            SELECT id, group_code, name, description, is_active, organization_id, created_at, updated_at
            FROM code_group
            ORDER BY group_code ASC
            "#,
        )
        .fetch_all(pool)
        .await?;

        Ok(groups)
    }

    pub async fn find_group_by_code(pool: &PgPool, code: &str) -> Result<Option<CodeGroup>, AppError> {
        let group = sqlx::query_as::<_, CodeGroup>(
            r#"
            SELECT id, group_code, name, description, is_active, organization_id, created_at, updated_at
            FROM code_group
            WHERE group_code = $1
            "#,
        )
        .bind(code)
        .fetch_optional(pool)
        .await?;

        Ok(group)
    }

    pub async fn find_group_by_id(pool: &PgPool, id: Uuid) -> Result<Option<CodeGroup>, AppError> {
        let group = sqlx::query_as::<_, CodeGroup>(
            r#"
            SELECT id, group_code, name, description, is_active, organization_id, created_at, updated_at
            FROM code_group
            WHERE id = $1
            "#,
        )
        .bind(id)
        .fetch_optional(pool)
        .await?;

        Ok(group)
    }

    pub async fn find_details_by_group_code(
        pool: &PgPool,
        group_code: &str,
    ) -> Result<Vec<CodeDetail>, AppError> {
        let details = sqlx::query_as::<_, CodeDetail>(
            r#"
            SELECT d.id, d.group_id, d.detail_code, d.name, d.sort_order, d.valid_from, d.valid_to, d.is_active, d.created_at, d.updated_at
            FROM code_detail d
            JOIN code_group g ON d.group_id = g.id
            WHERE g.group_code = $1 AND d.is_active = true
            ORDER BY d.sort_order ASC, d.detail_code ASC
            "#,
        )
        .bind(group_code)
        .fetch_all(pool)
        .await?;

        Ok(details)
    }

    pub async fn find_details_by_group_id(
        pool: &PgPool,
        group_id: Uuid,
    ) -> Result<Vec<CodeDetail>, AppError> {
        let details = sqlx::query_as::<_, CodeDetail>(
            r#"
            SELECT d.id, d.group_id, d.detail_code, d.name, d.sort_order, d.valid_from, d.valid_to, d.is_active, d.created_at, d.updated_at
            FROM code_detail d
            WHERE d.group_id = $1
            ORDER BY d.sort_order ASC, d.detail_code ASC
            "#,
        )
        .bind(group_id)
        .fetch_all(pool)
        .await?;

        Ok(details)
    }

    pub async fn create_group(pool: &PgPool, req: CodeGroupRequest) -> Result<CodeGroup, AppError> {
        let id = Uuid::new_v4();
        let is_active = req.is_active.unwrap_or(true);
        let group = sqlx::query_as::<_, CodeGroup>(
            r#"
            INSERT INTO code_group (id, group_code, name, description, is_active, organization_id, created_at, updated_at)
            VALUES ($1, $2, $3, $4, $5, $6, NOW(), NOW())
            RETURNING id, group_code, name, description, is_active, organization_id, created_at, updated_at
            "#,
        )
        .bind(id)
        .bind(&req.group_code)
        .bind(&req.name)
        .bind(&req.description)
        .bind(is_active)
        .bind(req.organization_id)
        .fetch_one(pool)
        .await?;

        Ok(group)
    }

    pub async fn update_group(pool: &PgPool, id: Uuid, req: CodeGroupRequest) -> Result<CodeGroup, AppError> {
        let is_active = req.is_active.unwrap_or(true);
        let group = sqlx::query_as::<_, CodeGroup>(
            r#"
            UPDATE code_group
            SET group_code = $2, name = $3, description = $4, is_active = $5, organization_id = $6, updated_at = NOW()
            WHERE id = $1
            RETURNING id, group_code, name, description, is_active, organization_id, created_at, updated_at
            "#,
        )
        .bind(id)
        .bind(&req.group_code)
        .bind(&req.name)
        .bind(&req.description)
        .bind(is_active)
        .bind(req.organization_id)
        .fetch_one(pool)
        .await?;

        Ok(group)
    }

    pub async fn delete_group(pool: &PgPool, id: Uuid) -> Result<(), AppError> {
        sqlx::query("DELETE FROM code_detail WHERE group_id = $1")
            .bind(id)
            .execute(pool)
            .await?;
        sqlx::query("DELETE FROM code_group WHERE id = $1")
            .bind(id)
            .execute(pool)
            .await?;
        Ok(())
    }

    pub async fn create_detail(pool: &PgPool, group_id: Uuid, req: CodeDetailRequest) -> Result<CodeDetail, AppError> {
        let id = Uuid::new_v4();
        let is_active = req.is_active.unwrap_or(true);
        let sort_order = req.sort_order.unwrap_or(1);
        let detail = sqlx::query_as::<_, CodeDetail>(
            r#"
            INSERT INTO code_detail (id, group_id, detail_code, name, sort_order, valid_from, valid_to, is_active, created_at, updated_at)
            VALUES ($1, $2, $3, $4, $5, $6, $7, $8, NOW(), NOW())
            RETURNING id, group_id, detail_code, name, sort_order, valid_from, valid_to, is_active, created_at, updated_at
            "#,
        )
        .bind(id)
        .bind(group_id)
        .bind(&req.detail_code)
        .bind(&req.name)
        .bind(sort_order)
        .bind(req.valid_from)
        .bind(req.valid_to)
        .bind(is_active)
        .fetch_one(pool)
        .await?;

        Ok(detail)
    }

    pub async fn update_detail(pool: &PgPool, detail_id: Uuid, req: CodeDetailRequest) -> Result<CodeDetail, AppError> {
        let is_active = req.is_active.unwrap_or(true);
        let detail = sqlx::query_as::<_, CodeDetail>(
            r#"
            UPDATE code_detail
            SET detail_code = $2, name = $3, sort_order = $4, valid_from = $5, valid_to = $6, is_active = $7, updated_at = NOW()
            WHERE id = $1
            RETURNING id, group_id, detail_code, name, sort_order, valid_from, valid_to, is_active, created_at, updated_at
            "#,
        )
        .bind(detail_id)
        .bind(&req.detail_code)
        .bind(&req.name)
        .bind(req.sort_order)
        .bind(req.valid_from)
        .bind(req.valid_to)
        .bind(is_active)
        .fetch_one(pool)
        .await?;

        Ok(detail)
    }

    pub async fn delete_detail(pool: &PgPool, detail_id: Uuid) -> Result<(), AppError> {
        sqlx::query("DELETE FROM code_detail WHERE id = $1")
            .bind(detail_id)
            .execute(pool)
            .await?;
        Ok(())
    }
}
