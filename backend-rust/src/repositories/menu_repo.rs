use crate::error::AppError;
use crate::models::menu::Menu;
use sqlx::PgPool;
use std::collections::HashMap;

pub struct MenuRepository;

impl MenuRepository {
    pub async fn find_all(pool: &PgPool, include_inactive: bool) -> Result<Vec<Menu>, AppError> {
        let sql = if include_inactive {
            r#"
            SELECT id, parent_id, name, path, icon, sort_order, is_active, created_at, updated_at
            FROM menu
            ORDER BY sort_order ASC, id ASC
            "#
        } else {
            r#"
            SELECT id, parent_id, name, path, icon, sort_order, is_active, created_at, updated_at
            FROM menu
            WHERE is_active IS NULL OR is_active = true
            ORDER BY sort_order ASC, id ASC
            "#
        };

        let menus = sqlx::query_as::<_, Menu>(sql).fetch_all(pool).await?;
        Ok(menus)
    }

    pub async fn get_menu_roles(pool: &PgPool) -> Result<HashMap<i64, Vec<String>>, AppError> {
        let rows: Vec<(i64, String)> = sqlx::query_as("SELECT menu_id, role_name FROM menu_roles")
            .fetch_all(pool)
            .await
            .unwrap_or_default();

        let mut map: HashMap<i64, Vec<String>> = HashMap::new();
        for (menu_id, role) in rows {
            map.entry(menu_id).or_default().push(role);
        }
        Ok(map)
    }

    pub async fn log_access(
        pool: &PgPool,
        menu_id: Option<i64>,
        menu_path: Option<&str>,
        user_id: &str,
        user_agent: Option<&str>,
        client_ip: Option<&str>,
    ) -> Result<(), AppError> {
        let _ = sqlx::query(
            r#"
            INSERT INTO menu_access_log (menu_id, menu_path, user_id, user_agent, client_ip, accessed_at)
            VALUES ($1, $2, $3, $4, $5, NOW())
            "#,
        )
        .bind(menu_id)
        .bind(menu_path)
        .bind(user_id)
        .bind(user_agent)
        .bind(client_ip)
        .execute(pool)
        .await;

        Ok(())
    }

    pub async fn find_recent_access_logs(
        pool: &PgPool,
        user_id: &str,
    ) -> Result<Vec<serde_json::Value>, AppError> {
        let rows: Vec<(
            i64,
            Option<i64>,
            Option<String>,
            String,
            Option<String>,
            Option<String>,
            chrono::NaiveDateTime,
        )> = sqlx::query_as(
            r#"
            SELECT id, menu_id, menu_path, user_id, user_agent, client_ip, accessed_at
            FROM menu_access_log
            WHERE user_id = $1
            ORDER BY accessed_at DESC
            LIMIT 20
            "#,
        )
        .bind(user_id)
        .fetch_all(pool)
        .await
        .unwrap_or_default();

        Ok(rows
            .into_iter()
            .map(|(id, m_id, path, uid, ua, ip, at)| {
                serde_json::json!({
                    "id": id,
                    "menuId": m_id,
                    "menuPath": path,
                    "userId": uid,
                    "userAgent": ua,
                    "clientIp": ip,
                    "accessedAt": at
                })
            })
            .collect())
    }

    pub async fn find_access_logs(
        pool: &PgPool,
        page: i64,
        size: i64,
    ) -> Result<serde_json::Value, AppError> {
        let offset = page * size;
        let rows: Vec<(
            i64,
            Option<i64>,
            Option<String>,
            String,
            Option<String>,
            Option<String>,
            chrono::NaiveDateTime,
        )> = sqlx::query_as(
            r#"
            SELECT id, menu_id, menu_path, user_id, user_agent, client_ip, accessed_at
            FROM menu_access_log
            ORDER BY accessed_at DESC
            LIMIT $1 OFFSET $2
            "#,
        )
        .bind(size)
        .bind(offset)
        .fetch_all(pool)
        .await
        .unwrap_or_default();

        let total: (i64,) = sqlx::query_as("SELECT COUNT(*) FROM menu_access_log")
            .fetch_one(pool)
            .await
            .unwrap_or((0,));

        let content: Vec<serde_json::Value> = rows
            .into_iter()
            .map(|(id, m_id, path, uid, ua, ip, at)| {
                serde_json::json!({
                    "id": id,
                    "menuId": m_id,
                    "menuPath": path,
                    "userId": uid,
                    "userAgent": ua,
                    "clientIp": ip,
                    "accessedAt": at
                })
            })
            .collect();

        Ok(serde_json::json!({
            "content": content,
            "totalElements": total.0,
            "totalPages": (total.0 + size - 1) / size.max(1),
            "size": size,
            "number": page
        }))
    }
}
