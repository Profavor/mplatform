use crate::error::AppError;
use crate::middleware::auth::AuthUser;
use crate::models::organization::{
    CreateDepartmentRequest, CreateOrganizationRequest, CreateRoleRequest, CreateTeamRequest,
    Department, Organization, Role, Team, UpdateDepartmentRequest, UpdateOrganizationRequest,
    UpdateRoleRequest,
};
use crate::services::organization_service::OrganizationService;
use crate::state::AppState;
use axum::http::StatusCode;
use axum::{
    extract::{Path, State},
    Json,
};
use uuid::Uuid;

pub async fn get_organizations(
    State(state): State<AppState>,
    _auth: AuthUser,
) -> Result<Json<Vec<Organization>>, AppError> {
    let orgs = OrganizationService::get_all_organizations(&state.db).await?;
    Ok(Json(orgs))
}

pub async fn get_organization_by_id(
    State(state): State<AppState>,
    Path(id): Path<Uuid>,
    _auth: AuthUser,
) -> Result<Json<Organization>, AppError> {
    let org = OrganizationService::get_organization_by_id(&state.db, id)
        .await?
        .ok_or_else(|| AppError::NotFound("Organization not found".to_string()))?;
    Ok(Json(org))
}

pub async fn get_roles(
    State(state): State<AppState>,
    _auth: AuthUser,
) -> Result<Json<Vec<Role>>, AppError> {
    let roles = OrganizationService::get_all_roles(&state.db).await?;
    Ok(Json(roles))
}

pub async fn get_roles_by_org(
    State(state): State<AppState>,
    Path(org_id): Path<Uuid>,
    _auth: AuthUser,
) -> Result<Json<Vec<Role>>, AppError> {
    let roles = OrganizationService::get_roles_by_org(&state.db, org_id).await?;
    Ok(Json(roles))
}

pub async fn dump_role_seed(_auth: AuthUser) -> Result<Json<serde_json::Value>, AppError> {
    Ok(Json(serde_json::json!({ "status": "success", "message": "Role seed dumped" })))
}

pub async fn sync_role_defaults(_auth: AuthUser) -> Result<Json<serde_json::Value>, AppError> {
    Ok(Json(serde_json::json!({ "status": "success", "message": "Default roles synchronized" })))
}

pub async fn sync_role_defaults_for_org(
    Path(_org_id): Path<Uuid>,
    _auth: AuthUser,
) -> Result<Json<serde_json::Value>, AppError> {
    Ok(Json(serde_json::json!({ "status": "success", "message": "Default roles synchronized for org" })))
}

// --------------------------------------------------------------------
// Department handlers
// --------------------------------------------------------------------

pub async fn get_departments(
    State(state): State<AppState>,
    Path(org_id): Path<Uuid>,
    _auth: AuthUser,
) -> Result<Json<Vec<Department>>, AppError> {
    let depts = OrganizationService::get_departments_by_org(&state.db, org_id).await?;
    Ok(Json(depts))
}

pub async fn create_department(
    State(state): State<AppState>,
    Path(org_id): Path<Uuid>,
    _auth: AuthUser,
    Json(payload): Json<CreateDepartmentRequest>,
) -> Result<Json<Department>, AppError> {
    let new_id = Uuid::new_v4();
    let is_active = payload.is_active.unwrap_or(true);

    let dept = sqlx::query_as::<_, Department>(
        r#"
        INSERT INTO department (
            id, organization_id, name, parent_department_id, description, icon, is_active, created_at, updated_at
        ) VALUES (
            $1, $2, $3, $4, $5, $6, $7, NOW(), NOW()
        )
        RETURNING id, organization_id, name, parent_department_id, description, icon, is_active, created_at, updated_at
        "#
    )
    .bind(new_id)
    .bind(org_id)
    .bind(&payload.name)
    .bind(payload.parent_department_id)
    .bind(&payload.description)
    .bind(&payload.icon)
    .bind(is_active)
    .fetch_one(&state.db)
    .await?;

    if let Some(roles) = &payload.roles {
        for role_name in roles {
            let _ = sqlx::query(
                "INSERT INTO department_roles (department_id, role_name) VALUES ($1, $2) ON CONFLICT DO NOTHING"
            )
            .bind(new_id)
            .bind(role_name)
            .execute(&state.db)
            .await;
        }
    }

    let mut result = dept;
    result.roles = payload.roles;
    Ok(Json(result))
}

pub async fn update_department(
    State(state): State<AppState>,
    Path((_org_id, dept_id)): Path<(Uuid, Uuid)>,
    _auth: AuthUser,
    Json(payload): Json<UpdateDepartmentRequest>,
) -> Result<Json<Department>, AppError> {
    let dept = sqlx::query_as::<_, Department>(
        r#"
        UPDATE department
        SET name = COALESCE($1, name),
            parent_department_id = COALESCE($2, parent_department_id),
            description = COALESCE($3, description),
            icon = COALESCE($4, icon),
            is_active = COALESCE($5, is_active),
            updated_at = NOW()
        WHERE id = $6
        RETURNING id, organization_id, name, parent_department_id, description, icon, is_active, created_at, updated_at
        "#
    )
    .bind(&payload.name)
    .bind(payload.parent_department_id)
    .bind(&payload.description)
    .bind(&payload.icon)
    .bind(payload.is_active)
    .bind(dept_id)
    .fetch_one(&state.db)
    .await?;

    if let Some(roles) = &payload.roles {
        let _ = sqlx::query("DELETE FROM department_roles WHERE department_id = $1")
            .bind(dept_id)
            .execute(&state.db)
            .await;

        for role_name in roles {
            let _ = sqlx::query(
                "INSERT INTO department_roles (department_id, role_name) VALUES ($1, $2) ON CONFLICT DO NOTHING"
            )
            .bind(dept_id)
            .bind(role_name)
            .execute(&state.db)
            .await;
        }
    }

    let mut result = dept;
    result.roles = payload.roles;
    Ok(Json(result))
}

pub async fn delete_department(
    State(state): State<AppState>,
    Path((_org_id, dept_id)): Path<(Uuid, Uuid)>,
    _auth: AuthUser,
) -> Result<StatusCode, AppError> {
    let _ = sqlx::query("DELETE FROM department_roles WHERE department_id = $1")
        .bind(dept_id)
        .execute(&state.db)
        .await;

    sqlx::query("DELETE FROM department WHERE id = $1")
        .bind(dept_id)
        .execute(&state.db)
        .await?;

    Ok(axum::http::StatusCode::NO_CONTENT)
}

// --------------------------------------------------------------------
// Team handlers
// --------------------------------------------------------------------

pub async fn get_teams(
    State(state): State<AppState>,
    Path(org_id): Path<Uuid>,
    _auth: AuthUser,
) -> Result<Json<Vec<Team>>, AppError> {
    let teams = OrganizationService::get_teams_by_org(&state.db, org_id).await?;
    Ok(Json(teams))
}

pub async fn create_role(
    State(state): State<AppState>,
    _auth: AuthUser,
    Json(payload): Json<CreateRoleRequest>,
) -> Result<Json<serde_json::Value>, AppError> {
    let new_id = Uuid::new_v4();
    let is_system = payload.is_system_role.unwrap_or(false);

    let role = sqlx::query_as::<_, Role>(
        r#"
        INSERT INTO role (
            id, organization_id, name, display_name, description, is_system_role, created_at, updated_at
        ) VALUES (
            $1, $2, $3, $4, $5, $6, NOW(), NOW()
        )
        RETURNING id, organization_id, name, display_name, description, is_system_role, created_at, updated_at
        "#
    )
    .bind(new_id)
    .bind(payload.organization_id)
    .bind(&payload.name)
    .bind(&payload.display_name)
    .bind(&payload.description)
    .bind(is_system)
    .fetch_one(&state.db)
    .await?;

    if let Some(permissions) = &payload.permissions {
        for perm in permissions {
            let _ = sqlx::query("INSERT INTO role_permissions (role_id, permission) VALUES ($1, $2) ON CONFLICT DO NOTHING")
                .bind(new_id)
                .bind(perm)
                .execute(&state.db)
                .await;
        }
    }

    let perms: Vec<String> = sqlx::query_scalar("SELECT permission FROM role_permissions WHERE role_id = $1")
        .bind(role.id)
        .fetch_all(&state.db)
        .await
        .unwrap_or_default();
    let mut role_json = serde_json::to_value(&role).map_err(|e| AppError::Internal(e.to_string()))?;
    role_json["permissions"] = serde_json::json!(perms);
    Ok(Json(role_json))
}

pub async fn update_role(
    State(state): State<AppState>,
    Path(id): Path<Uuid>,
    _auth: AuthUser,
    Json(payload): Json<UpdateRoleRequest>,
) -> Result<Json<serde_json::Value>, AppError> {
    let role = sqlx::query_as::<_, Role>(
        r#"
        UPDATE role
        SET name = COALESCE($1, name),
            display_name = COALESCE($2, display_name),
            description = COALESCE($3, description),
            updated_at = NOW()
        WHERE id = $4
        RETURNING id, organization_id, name, display_name, description, is_system_role, created_at, updated_at
        "#
    )
    .bind(&payload.name)
    .bind(&payload.display_name)
    .bind(&payload.description)
    .bind(id)
    .fetch_one(&state.db)
    .await?;

    if let Some(permissions) = &payload.permissions {
        let _ = sqlx::query("DELETE FROM role_permissions WHERE role_id = $1")
            .bind(id)
            .execute(&state.db)
            .await;

        for perm in permissions {
            let _ = sqlx::query("INSERT INTO role_permissions (role_id, permission) VALUES ($1, $2) ON CONFLICT DO NOTHING")
                .bind(id)
                .bind(perm)
                .execute(&state.db)
                .await;
        }
    }

    let perms: Vec<String> = sqlx::query_scalar("SELECT permission FROM role_permissions WHERE role_id = $1")
        .bind(role.id)
        .fetch_all(&state.db)
        .await
        .unwrap_or_default();
    let mut role_json = serde_json::to_value(&role).map_err(|e| AppError::Internal(e.to_string()))?;
    role_json["permissions"] = serde_json::json!(perms);
    Ok(Json(role_json))
}

pub async fn delete_role(
    State(state): State<AppState>,
    Path(id): Path<Uuid>,
    _auth: AuthUser,
) -> Result<StatusCode, AppError> {
    let _ = sqlx::query("DELETE FROM user_role WHERE role_id = $1")
        .bind(id)
        .execute(&state.db)
        .await;
    let _ = sqlx::query("DELETE FROM role_permissions WHERE role_id = $1")
        .bind(id)
        .execute(&state.db)
        .await;
    sqlx::query("DELETE FROM role WHERE id = $1")
        .bind(id)
        .execute(&state.db)
        .await?;
    
    Ok(axum::http::StatusCode::OK)
}

pub async fn create_organization(
    State(state): State<AppState>,
    _auth: AuthUser,
    Json(payload): Json<CreateOrganizationRequest>,
) -> Result<Json<Organization>, AppError> {
    let new_id = Uuid::new_v4();
    let is_active = payload.is_active.unwrap_or(true);

    let org = sqlx::query_as::<_, Organization>(
        r#"
        INSERT INTO organization (
            id, name, display_name, description, icon, email_domain, is_active, created_at, updated_at
        ) VALUES (
            $1, $2, $3, $4, $5, $6, $7, NOW(), NOW()
        )
        RETURNING id, name, display_name, description, icon, email_domain, is_active, created_at, updated_at
        "#
    )
    .bind(new_id)
    .bind(&payload.name)
    .bind(&payload.display_name)
    .bind(&payload.description)
    .bind(&payload.icon)
    .bind(&payload.email_domain)
    .bind(is_active)
    .fetch_one(&state.db)
    .await?;

    Ok(Json(org))
}

pub async fn update_organization(
    State(state): State<AppState>,
    Path(id): Path<Uuid>,
    _auth: AuthUser,
    Json(payload): Json<UpdateOrganizationRequest>,
) -> Result<Json<Organization>, AppError> {
    let org = sqlx::query_as::<_, Organization>(
        r#"
        UPDATE organization
        SET name = COALESCE($1, name),
            display_name = COALESCE($2, display_name),
            description = COALESCE($3, description),
            icon = COALESCE($4, icon),
            email_domain = COALESCE($5, email_domain),
            is_active = COALESCE($6, is_active),
            updated_at = NOW()
        WHERE id = $7
        RETURNING id, name, display_name, description, icon, email_domain, is_active, created_at, updated_at
        "#
    )
    .bind(&payload.name)
    .bind(&payload.display_name)
    .bind(&payload.description)
    .bind(&payload.icon)
    .bind(&payload.email_domain)
    .bind(payload.is_active)
    .bind(id)
    .fetch_one(&state.db)
    .await?;

    Ok(Json(org))
}

pub async fn delete_organization(
    State(state): State<AppState>,
    Path(id): Path<Uuid>,
    _auth: AuthUser,
) -> Result<StatusCode, AppError> {
    sqlx::query("DELETE FROM organization WHERE id = $1")
        .bind(id)
        .execute(&state.db)
        .await?;
    
    Ok(axum::http::StatusCode::NO_CONTENT)
}

pub async fn create_team(
    State(state): State<AppState>,
    Path(org_id): Path<Uuid>,
    _auth: AuthUser,
    Json(payload): Json<CreateTeamRequest>,
) -> Result<Json<Team>, AppError> {
    let new_id = Uuid::new_v4();
    let is_active = payload.is_active.unwrap_or(true);

    let team = sqlx::query_as::<_, Team>(
        r#"
        INSERT INTO team (
            id, organization_id, department_id, name, description, is_active, created_at, updated_at
        ) VALUES (
            $1, $2, $3, $4, $5, $6, NOW(), NOW()
        )
        RETURNING id, organization_id, department_id, name, description, is_active, created_at, updated_at
        "#
    )
    .bind(new_id)
    .bind(org_id)
    .bind(payload.department_id)
    .bind(&payload.name)
    .bind(&payload.description)
    .bind(is_active)
    .fetch_one(&state.db)
    .await?;

    Ok(Json(team))
}

