use crate::error::AppError;
use crate::models::organization::{Department, Organization, Role, Team};
use crate::repositories::organization_repo::OrganizationRepository;
use sqlx::PgPool;
use uuid::Uuid;

pub struct OrganizationService;

impl OrganizationService {
    pub async fn get_all_organizations(pool: &PgPool) -> Result<Vec<Organization>, AppError> {
        OrganizationRepository::find_all(pool).await
    }

    pub async fn get_organization_by_id(
        pool: &PgPool,
        id: Uuid,
    ) -> Result<Option<Organization>, AppError> {
        OrganizationRepository::find_by_id(pool, id).await
    }

    pub async fn get_all_roles(pool: &PgPool) -> Result<Vec<Role>, AppError> {
        OrganizationRepository::find_roles(pool).await
    }

    pub async fn get_roles_by_org(pool: &PgPool, org_id: Uuid) -> Result<Vec<Role>, AppError> {
        OrganizationRepository::find_roles_by_org(pool, org_id).await
    }

    pub async fn get_departments_by_org(
        pool: &PgPool,
        org_id: Uuid,
    ) -> Result<Vec<Department>, AppError> {
        OrganizationRepository::find_departments_by_org(pool, org_id).await
    }

    pub async fn get_teams_by_org(pool: &PgPool, org_id: Uuid) -> Result<Vec<Team>, AppError> {
        OrganizationRepository::find_teams_by_org(pool, org_id).await
    }
}
