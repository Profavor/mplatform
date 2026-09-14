use crate::error::AppError;
use crate::models::user::User;
use sqlx::PgPool;

pub struct UserRepository;

impl UserRepository {
    pub async fn find_by_username(pool: &PgPool, username: &str) -> Result<Option<User>, AppError> {
        let user = sqlx::query_as::<_, User>(
            r#"
            SELECT id, username, password, role, organization_id, department_id, team_id,
                   timezone, is_active, must_change_password, email, failed_login_count,
                   locked_until, last_login_epoch_sec, two_factor_enabled, two_factor_secret,
                   two_factor_type, backup_codes, two_factor_grace_until
            FROM users
            WHERE username = $1 OR id = $1
            "#,
        )
        .bind(username)
        .fetch_optional(pool)
        .await?;

        Ok(user)
    }

    pub async fn find_by_id(pool: &PgPool, id: &str) -> Result<Option<User>, AppError> {
        let user = sqlx::query_as::<_, User>(
            r#"
            SELECT id, username, password, role, organization_id, department_id, team_id,
                   timezone, is_active, must_change_password, email, failed_login_count,
                   locked_until, last_login_epoch_sec, two_factor_enabled, two_factor_secret,
                   two_factor_type, backup_codes, two_factor_grace_until
            FROM users
            WHERE id = $1
            "#,
        )
        .bind(id)
        .fetch_optional(pool)
        .await?;

        Ok(user)
    }

    pub async fn update_last_login(pool: &PgPool, id: &str, epoch_sec: i64) -> Result<(), AppError> {
        sqlx::query("UPDATE users SET last_login_epoch_sec = $1, failed_login_count = 0 WHERE id = $2")
            .bind(epoch_sec)
            .bind(id)
            .execute(pool)
            .await?;

        Ok(())
    }
}
