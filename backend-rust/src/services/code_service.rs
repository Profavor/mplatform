use crate::error::AppError;
use crate::models::code::{CodeDetail, CodeDetailRequest, CodeGroup, CodeGroupRequest};
use crate::repositories::code_repo::CodeRepository;
use sqlx::PgPool;
use uuid::Uuid;

pub struct CodeService;

impl CodeService {
    pub async fn get_all_groups(pool: &PgPool) -> Result<Vec<CodeGroup>, AppError> {
        CodeRepository::find_groups(pool).await
    }

    pub async fn get_group_by_code(pool: &PgPool, code: &str) -> Result<CodeGroup, AppError> {
        CodeRepository::find_group_by_code(pool, code)
            .await?
            .ok_or_else(|| AppError::NotFound(format!("Code group '{}' not found", code)))
    }

    pub async fn get_group_by_id(pool: &PgPool, id: Uuid) -> Result<CodeGroup, AppError> {
        CodeRepository::find_group_by_id(pool, id)
            .await?
            .ok_or_else(|| AppError::NotFound(format!("Code group id '{}' not found", id)))
    }

    pub async fn get_details_by_group(
        pool: &PgPool,
        group_code: &str,
    ) -> Result<Vec<CodeDetail>, AppError> {
        CodeRepository::find_details_by_group_code(pool, group_code).await
    }

    pub async fn get_details_by_group_id(
        pool: &PgPool,
        group_id: Uuid,
    ) -> Result<Vec<CodeDetail>, AppError> {
        CodeRepository::find_details_by_group_id(pool, group_id).await
    }

    pub async fn create_group(pool: &PgPool, req: CodeGroupRequest) -> Result<CodeGroup, AppError> {
        CodeRepository::create_group(pool, req).await
    }

    pub async fn update_group(pool: &PgPool, id: Uuid, req: CodeGroupRequest) -> Result<CodeGroup, AppError> {
        CodeRepository::update_group(pool, id, req).await
    }

    pub async fn delete_group(pool: &PgPool, id: Uuid) -> Result<(), AppError> {
        CodeRepository::delete_group(pool, id).await
    }

    pub async fn create_detail(pool: &PgPool, group_id: Uuid, req: CodeDetailRequest) -> Result<CodeDetail, AppError> {
        CodeRepository::create_detail(pool, group_id, req).await
    }

    pub async fn update_detail(pool: &PgPool, detail_id: Uuid, req: CodeDetailRequest) -> Result<CodeDetail, AppError> {
        CodeRepository::update_detail(pool, detail_id, req).await
    }

    pub async fn delete_detail(pool: &PgPool, detail_id: Uuid) -> Result<(), AppError> {
        CodeRepository::delete_detail(pool, detail_id).await
    }
}
