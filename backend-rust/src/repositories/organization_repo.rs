use crate::error::AppError;
use crate::models::organization::{Department, Organization, Role, Team};
use sqlx::PgPool;
use uuid::Uuid;

pub struct OrganizationRepository;

impl OrganizationRepository {
    pub async fn find_all(pool: &PgPool) -> Result<Vec<Organization>, AppError> {
        let orgs = sqlx::query_as::<_, Organization>(
            r#"
            SELECT id, name, display_name, description, icon, email_domain, is_active, created_at, updated_at
            FROM organization
            WHERE is_active = true
            ORDER BY name ASC
            "#,
        )
        .fetch_all(pool)
        .await?;

        Ok(orgs)
    }

    pub async fn find_by_id(pool: &PgPool, id: Uuid) -> Result<Option<Organization>, AppError> {
        let org = sqlx::query_as::<_, Organization>(
            r#"
            SELECT id, name, display_name, description, icon, email_domain, is_active, created_at, updated_at
            FROM organization
            WHERE id = $1
            "#,
        )
        .bind(id)
        .fetch_optional(pool)
        .await?;

        Ok(org)
    }

    pub async fn find_roles(pool: &PgPool) -> Result<Vec<Role>, AppError> {
        let mut roles = sqlx::query_as::<_, Role>(
            r#"
            SELECT id, organization_id, name, display_name, description, is_system_role, created_at, updated_at
            FROM role
            ORDER BY name ASC
            "#,
        )
        .fetch_all(pool)
        .await?;

        for role in &mut roles {
            let perm_rows: Vec<(String,)> =
                sqlx::query_as("SELECT permission FROM role_permissions WHERE role_id = $1")
                    .bind(role.id)
                    .fetch_all(pool)
                    .await
                    .unwrap_or_default();

            role.permissions = Some(perm_rows.into_iter().map(|(p,)| p).collect());
        }

        Ok(roles)
    }

    pub async fn find_roles_by_org(pool: &PgPool, org_id: Uuid) -> Result<Vec<Role>, AppError> {
        let mut roles = sqlx::query_as::<_, Role>(
            r#"
            SELECT id, organization_id, name, display_name, description, is_system_role, created_at, updated_at
            FROM role
            WHERE organization_id = $1
            ORDER BY name ASC
            "#,
        )
        .bind(org_id)
        .fetch_all(pool)
        .await?;

        for role in &mut roles {
            let perm_rows: Vec<(String,)> =
                sqlx::query_as("SELECT permission FROM role_permissions WHERE role_id = $1")
                    .bind(role.id)
                    .fetch_all(pool)
                    .await
                    .unwrap_or_default();

            role.permissions = Some(perm_rows.into_iter().map(|(p,)| p).collect());
        }

        Ok(roles)
    }

    pub async fn find_departments_by_org(
        pool: &PgPool,
        org_id: Uuid,
    ) -> Result<Vec<Department>, AppError> {
        let mut depts = sqlx::query_as::<_, Department>(
            r#"
            SELECT id, organization_id, name, parent_department_id, description, icon, is_active, created_at, updated_at
            FROM department
            WHERE organization_id = $1
            ORDER BY name ASC
            "#,
        )
        .bind(org_id)
        .fetch_all(pool)
        .await?;

        for dept in &mut depts {
            let role_rows: Vec<(Option<String>,)> =
                sqlx::query_as("SELECT role_name FROM department_roles WHERE department_id = $1")
                    .bind(dept.id)
                    .fetch_all(pool)
                    .await
                    .unwrap_or_default();

            dept.roles = Some(role_rows.into_iter().filter_map(|(r,)| r).collect());
        }

        Ok(depts)
    }

    pub async fn find_teams_by_org(pool: &PgPool, org_id: Uuid) -> Result<Vec<Team>, AppError> {
        let teams = sqlx::query_as::<_, Team>(
            r#"
            SELECT id, organization_id, department_id, name, description, is_active, created_at, updated_at
            FROM team
            WHERE organization_id = $1
            ORDER BY name ASC
            "#,
        )
        .bind(org_id)
        .fetch_all(pool)
        .await?;

        Ok(teams)
    }
}
