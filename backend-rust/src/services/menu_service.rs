use crate::error::AppError;
use crate::models::menu::Menu;
use crate::repositories::menu_repo::MenuRepository;
use sqlx::PgPool;
use std::collections::HashMap;

pub struct MenuService;

impl MenuService {
    pub async fn get_all_menus(pool: &PgPool, include_inactive: bool) -> Result<Vec<Menu>, AppError> {
        MenuRepository::find_all(pool, include_inactive).await
    }

    pub async fn get_menu_tree(pool: &PgPool, include_inactive: bool) -> Result<Vec<serde_json::Value>, AppError> {
        let menus = MenuRepository::find_all(pool, include_inactive).await?;
        let roles_map = MenuRepository::get_menu_roles(pool).await?;
        Ok(Self::build_tree_recursive(&menus, &roles_map, None))
    }

    fn build_tree_recursive(
        menus: &[Menu],
        roles_map: &HashMap<i64, Vec<String>>,
        parent_id: Option<i64>,
    ) -> Vec<serde_json::Value> {
        let mut result = Vec::new();
        for menu in menus.iter().filter(|m| m.parent_id == parent_id) {
            let children = Self::build_tree_recursive(menus, roles_map, Some(menu.id));
            let mut node_roles = roles_map.get(&menu.id).cloned().unwrap_or_default();
            for child in &children {
                if let Some(arr) = child.get("requiredRoles").and_then(|r| r.as_array()) {
                    for r in arr {
                        if let Some(s) = r.as_str() {
                            if !node_roles.iter().any(|existing| existing == s) {
                                node_roles.push(s.to_string());
                            }
                        }
                    }
                }
            }

            let mut node = serde_json::json!({
                "id": menu.id,
                "name": menu.name,
                "path": menu.path,
                "icon": menu.icon,
                "sortOrder": menu.sort_order,
                "parentId": menu.parent_id,
                "isActive": menu.is_active,
                "requiredRoles": node_roles,
            });

            if !children.is_empty() {
                node["children"] = serde_json::json!(children);
            }
            result.push(node);
        }
        result
    }

    pub async fn log_access(
        pool: &PgPool,
        menu_id: Option<i64>,
        menu_path: Option<&str>,
        user_id: &str,
        user_agent: Option<&str>,
        client_ip: Option<&str>,
    ) -> Result<(), AppError> {
        MenuRepository::log_access(pool, menu_id, menu_path, user_id, user_agent, client_ip).await
    }

    pub async fn get_my_recent_access(pool: &PgPool, user_id: &str) -> Result<Vec<serde_json::Value>, AppError> {
        MenuRepository::find_recent_access_logs(pool, user_id).await
    }

    pub async fn get_access_logs(pool: &PgPool, page: i64, size: i64) -> Result<serde_json::Value, AppError> {
        MenuRepository::find_access_logs(pool, page, size).await
    }
}
