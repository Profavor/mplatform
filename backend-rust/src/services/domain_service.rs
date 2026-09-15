use crate::error::AppError;
use crate::middleware::auth::AuthUser;
use crate::models::domain::{DomainRequest, DomainResponse};
use crate::repositories::domain_repo::DomainRepository;
use sqlx::PgPool;
use uuid::Uuid;

pub struct DomainService;

impl DomainService {
    pub async fn get_all_domains(pool: &PgPool) -> Result<Vec<DomainResponse>, AppError> {
        let domains = DomainRepository::find_all(pool).await?;
        Ok(domains.into_iter().map(DomainResponse::from).collect())
    }

    pub async fn get_domains_for_user(
        pool: &PgPool,
        auth: &AuthUser,
    ) -> Result<Vec<DomainResponse>, AppError> {
        if let Some(org_id) = auth.organization_id {
            let domains = DomainRepository::find_by_organization(pool, org_id).await?;
            Ok(domains.into_iter().map(DomainResponse::from).collect())
        } else {
            Ok(Vec::new())
        }
    }

    pub async fn get_domain_by_id(pool: &PgPool, id: Uuid) -> Result<DomainResponse, AppError> {
        let domain = DomainRepository::find_by_id(pool, id)
            .await?
            .ok_or_else(|| AppError::NotFound(format!("Domain not found: {id}")))?;
        Ok(DomainResponse::from(domain))
    }

    pub async fn get_domain_by_id_for_user(
        pool: &PgPool,
        id: Uuid,
        auth: &AuthUser,
    ) -> Result<DomainResponse, AppError> {
        let domain = Self::get_domain_by_id(pool, id).await?;
        if domain.organization_id != auth.organization_id {
            return Err(AppError::Forbidden("접근 권한이 없는 도메인입니다".to_string()));
        }
        Ok(domain)
    }

    pub async fn create_domain(
        pool: &PgPool,
        req: DomainRequest,
    ) -> Result<DomainResponse, AppError> {
        let domain = DomainRepository::create(pool, req).await?;
        Ok(DomainResponse::from(domain))
    }

    pub async fn update_domain(
        pool: &PgPool,
        id: Uuid,
        req: DomainRequest,
    ) -> Result<DomainResponse, AppError> {
        let domain = DomainRepository::update(pool, id, req).await?;
        Ok(DomainResponse::from(domain))
    }
}
