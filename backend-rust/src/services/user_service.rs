use crate::error::AppError;
use crate::models::user::User;
use crate::models::user_mgmt::{
    AdminUserUpdateDto, ResetPasswordRequest, SelfUserUpdateDto, UserDto,
};
use crate::repositories::user_repo::UserRepository;
use sqlx::PgPool;

pub struct UserService;

impl UserService {
    pub async fn get_all_users(pool: &PgPool) -> Result<Vec<UserDto>, AppError> {
        let users = sqlx::query_as::<_, User>(
            r#"
            SELECT id, username, password, role, organization_id, department_id, team_id,
                   timezone, is_active, must_change_password, email, failed_login_count,
                   locked_until, last_login_epoch_sec, two_factor_enabled, two_factor_secret,
                   two_factor_type, backup_codes, two_factor_grace_until
            FROM users
            ORDER BY username ASC
            "#,
        )
        .fetch_all(pool)
        .await?;

        Ok(users
            .into_iter()
            .map(|u| UserDto {
                id: u.id,
                username: u.username,
                role: u.role,
                email: u.email,
                organization_id: u.organization_id,
                department_id: u.department_id,
                team_id: u.team_id,
                timezone: u.timezone,
                is_active: u.is_active,
                must_change_password: u.must_change_password,
            })
            .collect())
    }

    pub async fn get_users_for_admin(
        pool: &PgPool,
        auth: &crate::middleware::auth::AuthUser,
    ) -> Result<Vec<UserDto>, AppError> {
        if let Some(org_id) = auth.organization_id {
            let users = sqlx::query_as::<_, User>(
                r#"
                SELECT id, username, password, role, organization_id, department_id, team_id,
                       timezone, is_active, must_change_password, email, failed_login_count,
                       locked_until, last_login_epoch_sec, two_factor_enabled, two_factor_secret,
                       two_factor_type, backup_codes, two_factor_grace_until
                FROM users
                WHERE organization_id = $1 AND is_active = true
                ORDER BY username ASC
                "#,
            )
            .bind(org_id)
            .fetch_all(pool)
            .await?;

            Ok(users
                .into_iter()
                .map(|u| UserDto {
                    id: u.id,
                    username: u.username,
                    role: u.role,
                    email: u.email,
                    organization_id: u.organization_id,
                    department_id: u.department_id,
                    team_id: u.team_id,
                    timezone: u.timezone,
                    is_active: u.is_active,
                    must_change_password: u.must_change_password,
                })
                .collect())
        } else {
            Ok(Vec::new())
        }
    }

    pub async fn validate_user_access(
        pool: &PgPool,
        id: &str,
        auth: &crate::middleware::auth::AuthUser,
    ) -> Result<User, AppError> {
        let user = UserRepository::find_by_id(pool, id)
            .await?
            .ok_or_else(|| AppError::NotFound(format!("User not found: {id}")))?;

        if user.organization_id != auth.organization_id {
            return Err(AppError::Forbidden("접근 권한이 없는 사용자입니다".to_string()));
        }

        Ok(user)
    }

    pub async fn update_admin_user(
        pool: &PgPool,
        id: &str,
        dto: AdminUserUpdateDto,
    ) -> Result<UserDto, AppError> {
        let user = sqlx::query_as::<_, User>(
            r#"
            UPDATE users
            SET role = COALESCE($2, role),
                organization_id = COALESCE($3, organization_id),
                department_id = COALESCE($4, department_id),
                team_id = COALESCE($5, team_id),
                is_active = COALESCE($6, is_active),
                timezone = COALESCE($7, timezone),
                email = COALESCE($8, email)
            WHERE id = $1
            RETURNING id, username, password, role, organization_id, department_id, team_id,
                      timezone, is_active, must_change_password, email, failed_login_count,
                      locked_until, last_login_epoch_sec, two_factor_enabled, two_factor_secret,
                      two_factor_type, backup_codes, two_factor_grace_until
            "#,
        )
        .bind(id)
        .bind(dto.role)
        .bind(dto.organization_id)
        .bind(dto.department_id)
        .bind(dto.team_id)
        .bind(dto.is_active)
        .bind(dto.timezone)
        .bind(dto.email)
        .fetch_optional(pool)
        .await?
        .ok_or_else(|| AppError::NotFound(format!("User not found: {id}")))?;

        Ok(UserDto {
            id: user.id,
            username: user.username,
            role: user.role,
            email: user.email,
            organization_id: user.organization_id,
            department_id: user.department_id,
            team_id: user.team_id,
            timezone: user.timezone,
            is_active: user.is_active,
            must_change_password: user.must_change_password,
        })
    }

    pub async fn update_self_user(
        pool: &PgPool,
        username: &str,
        dto: SelfUserUpdateDto,
    ) -> Result<UserDto, AppError> {
        let user = UserRepository::find_by_username(pool, username)
            .await?
            .ok_or_else(|| AppError::NotFound(format!("User not found: {username}")))?;

        let mut password_hash = user.password.clone();

        if let (Some(cur_pw), Some(new_pw)) = (dto.current_password, dto.new_password) {
            let valid = user
                .password
                .as_deref()
                .map(|h| bcrypt::verify(&cur_pw, h).unwrap_or(false))
                .unwrap_or(false);

            if !valid {
                return Err(AppError::BadRequest(
                    "Current password does not match".to_string(),
                ));
            }

            let new_hash = bcrypt::hash(new_pw, 10)
                .map_err(|e| AppError::Internal(format!("Failed to hash password: {e}")))?;
            password_hash = Some(new_hash);
        }

        let updated = sqlx::query_as::<_, User>(
            r#"
            UPDATE users
            SET timezone = COALESCE($2, timezone),
                email = COALESCE($3, email),
                password = COALESCE($4, password)
            WHERE id = $1
            RETURNING id, username, password, role, organization_id, department_id, team_id,
                      timezone, is_active, must_change_password, email, failed_login_count,
                      locked_until, last_login_epoch_sec, two_factor_enabled, two_factor_secret,
                      two_factor_type, backup_codes, two_factor_grace_until
            "#,
        )
        .bind(&user.id)
        .bind(dto.timezone)
        .bind(dto.email)
        .bind(password_hash)
        .fetch_one(pool)
        .await?;

        Ok(UserDto {
            id: updated.id,
            username: updated.username,
            role: updated.role,
            email: updated.email,
            organization_id: updated.organization_id,
            department_id: updated.department_id,
            team_id: updated.team_id,
            timezone: updated.timezone,
            is_active: updated.is_active,
            must_change_password: updated.must_change_password,
        })
    }

    pub async fn reset_password(
        pool: &PgPool,
        id: &str,
        req: ResetPasswordRequest,
    ) -> Result<(), AppError> {
        let hash = bcrypt::hash(req.new_password, 10)
            .map_err(|e| AppError::Internal(format!("Failed to hash password: {e}")))?;

        sqlx::query("UPDATE users SET password = $1, must_change_password = true WHERE id = $2")
            .bind(hash)
            .bind(id)
            .execute(pool)
            .await?;

        Ok(())
    }

    pub async fn deactivate_user(pool: &PgPool, id: &str) -> Result<(), AppError> {
        sqlx::query("UPDATE users SET is_active = false WHERE id = $1")
            .bind(id)
            .execute(pool)
            .await?;

        Ok(())
    }

    pub async fn delete_user_permanently(pool: &PgPool, id: &str) -> Result<(), AppError> {
        let user = UserRepository::find_by_id(pool, id).await?;
        let username = user.as_ref().and_then(|u| u.username.as_deref());

        let mut tx = pool.begin().await?;

        sqlx::query("DELETE FROM data_scope_permission WHERE user_id = $1")
            .bind(id)
            .execute(&mut *tx)
            .await?;

        sqlx::query("DELETE FROM domain_permission WHERE user_id = $1")
            .bind(id)
            .execute(&mut *tx)
            .await?;

        sqlx::query("DELETE FROM domain_access_request WHERE user_id = $1")
            .bind(id)
            .execute(&mut *tx)
            .await?;

        sqlx::query("DELETE FROM user_role WHERE user_id = $1")
            .bind(id)
            .execute(&mut *tx)
            .await?;

        if let Some(un) = username {
            sqlx::query("DELETE FROM login_log WHERE user_id = $1 OR username = $2")
                .bind(id)
                .bind(un)
                .execute(&mut *tx)
                .await?;

            let _ = sqlx::query("DELETE FROM keycloak.credential WHERE user_id IN (SELECT id FROM keycloak.user_entity WHERE username = $1)")
                .bind(un)
                .execute(&mut *tx)
                .await;

            let _ = sqlx::query("DELETE FROM keycloak.user_role_mapping WHERE user_id IN (SELECT id FROM keycloak.user_entity WHERE username = $1)")
                .bind(un)
                .execute(&mut *tx)
                .await;

            let _ = sqlx::query("DELETE FROM keycloak.user_attribute WHERE user_id IN (SELECT id FROM keycloak.user_entity WHERE username = $1)")
                .bind(un)
                .execute(&mut *tx)
                .await;

            let _ = sqlx::query("DELETE FROM keycloak.federated_identity WHERE user_id IN (SELECT id FROM keycloak.user_entity WHERE username = $1)")
                .bind(un)
                .execute(&mut *tx)
                .await;

            let _ = sqlx::query("DELETE FROM keycloak.user_entity WHERE username = $1")
                .bind(un)
                .execute(&mut *tx)
                .await;
        } else {
            sqlx::query("DELETE FROM login_log WHERE user_id = $1")
                .bind(id)
                .execute(&mut *tx)
                .await?;
        }

        sqlx::query("DELETE FROM users WHERE id = $1")
            .bind(id)
            .execute(&mut *tx)
            .await?;

        tx.commit().await?;

        Ok(())
    }
}
