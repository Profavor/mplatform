use axum::http::StatusCode;
use axum::{
    extract::{Path, Query, State},
    response::IntoResponse,
    Json,
};
use serde::{Deserialize, Serialize};
use uuid::Uuid;

use crate::{
    error::AppError,
    models::{
        field_definition::{FieldDefinition, FieldDefinitionRequest},
        record::PageResponse,
    },
    state::AppState,
};

#[derive(Debug, Deserialize)]
pub struct PageQuery {
    pub page: Option<i64>,
    pub size: Option<i64>,
    pub as_of: Option<String>,
    pub reason: Option<String>,
}

const FIELD_SELECT_SQL: &str = r#"
    SELECT 
        fd.*,
        CASE 
            WHEN fg.id IS NOT NULL THEN json_build_object(
                'id', fg.id,
                'domainId', fg.domain_id,
                'sectorId', fg.sector_id,
                'name', fg.name,
                'sortOrder', fg.sort_order,
                'isDefaultOpen', fg.is_default_open,
                'sector', CASE 
                    WHEN s.id IS NOT NULL THEN json_build_object(
                        'id', s.id,
                        'domainId', s.domain_id,
                        'name', s.name,
                        'sortOrder', s.sort_order
                    )
                    ELSE NULL
                END
            )
            ELSE NULL
        END as field_group
    FROM field_definition fd
    LEFT JOIN field_group fg ON fd.field_group_id = fg.id
    LEFT JOIN sector s ON fg.sector_id = s.id
"#;

async fn fetch_field_by_id(
    db: &sqlx::PgPool,
    id: Uuid,
) -> Result<Option<FieldDefinition>, AppError> {
    let query_str = format!("{} WHERE fd.id = $1", FIELD_SELECT_SQL);
    let field = sqlx::query_as::<_, FieldDefinition>(&query_str)
        .bind(id)
        .fetch_optional(db)
        .await?;
    Ok(field)
}

pub async fn fetch_effective_fields(
    pool: &sqlx::PgPool,
    node_id: Uuid,
) -> Result<Vec<FieldDefinition>, AppError> {
    // 1. Traverse node and ancestors
    let ancestors: Vec<(Uuid, Option<Uuid>, Uuid)> = sqlx::query_as(
        r#"
        WITH RECURSIVE node_path AS (
            SELECT id, parent_id, domain_id FROM classification_node WHERE id = $1 AND is_deleted = false
            UNION ALL
            SELECT n.id, n.parent_id, n.domain_id FROM classification_node n
            INNER JOIN node_path np ON n.id = np.parent_id
            WHERE n.is_deleted = false
        )
        SELECT id, parent_id, domain_id FROM node_path
        "#
    )
    .bind(node_id)
    .fetch_all(pool)
    .await?;

    if ancestors.is_empty() {
        return Ok(vec![]);
    }

    let domain_id = ancestors[0].2;
    let node_ids: Vec<Uuid> = ancestors.iter().map(|a| a.0).collect();

    let query_str = format!(
        r#"
        {}
        WHERE (fd.domain_id = $1 OR fd.defined_at_node_id = ANY($2))
          AND (fd.is_removed = false OR fd.is_removed IS NULL)
        ORDER BY 
            CASE WHEN fd.defined_at_node_id IS NULL THEN 0 ELSE 1 END ASC,
            COALESCE(s.sort_order, 9999) ASC,
            COALESCE(fg.sort_order, 9999) ASC,
            fd.field_order ASC,
            fd.created_at ASC
        "#,
        FIELD_SELECT_SQL
    );

    let fields: Vec<FieldDefinition> = sqlx::query_as(&query_str)
        .bind(domain_id)
        .bind(&node_ids)
        .fetch_all(pool)
        .await?;

    // Deduplicate by field_key preserving order (node overrides domain if duplicate)
    let mut seen = std::collections::HashSet::new();
    let mut deduped = Vec::new();
    for f in fields {
        if seen.insert(f.field_key.clone()) {
            deduped.push(f);
        }
    }

    Ok(deduped)
}

pub async fn get_effective_fields(
    State(state): State<AppState>,
    Path(node_id): Path<Uuid>,
) -> Result<Json<Vec<FieldDefinition>>, AppError> {
    let fields = fetch_effective_fields(&state.db, node_id).await?;
    Ok(Json(fields))
}

pub async fn get_effective_fields_page(
    State(state): State<AppState>,
    Path(node_id): Path<Uuid>,
    Query(params): Query<PageQuery>,
) -> Result<Json<PageResponse<FieldDefinition>>, AppError> {
    let all = get_effective_fields(State(state), Path(node_id)).await?.0;
    let page = params.page.unwrap_or(0);
    let size = params.size.unwrap_or(100);
    let total = all.len() as i64;
    let start = (page * size) as usize;
    let content = if start < all.len() {
        all.into_iter().skip(start).take(size as usize).collect()
    } else {
        vec![]
    };

    Ok(Json(PageResponse::new(content, total, page, size)))
}

pub async fn get_effective_fields_as_of(
    State(state): State<AppState>,
    Path(node_id): Path<Uuid>,
    Query(_params): Query<PageQuery>,
) -> Result<Json<Vec<FieldDefinition>>, AppError> {
    get_effective_fields(State(state), Path(node_id)).await
}

pub async fn add_field(
    State(state): State<AppState>,
    Path(node_id): Path<Uuid>,
    Json(payload): Json<FieldDefinitionRequest>,
) -> Result<Json<FieldDefinition>, AppError> {
    let new_id = Uuid::new_v4();
    let key = payload
        .field_key
        .clone()
        .unwrap_or_else(|| format!("field_{}", &new_id.to_string()[..8]));
    let ftype = payload
        .field_type
        .clone()
        .unwrap_or_else(|| "STRING".to_string());
    let req = payload.required.unwrap_or(false);
    let searchable = payload.is_searchable.unwrap_or(true);
    let multi = payload.is_multi_value.unwrap_or(false);
    let encrypted = payload.is_encrypted.unwrap_or(false);
    let order = payload.field_order.unwrap_or(1);
    let is_table = payload.is_table.unwrap_or(false);
    let grid_w = payload.grid_width.unwrap_or(3);
    let col_w = payload.table_column_width.unwrap_or(150);
    let group_id = payload.resolved_group_id();

    let domain_id: Option<Uuid> =
        sqlx::query_scalar("SELECT domain_id FROM classification_node WHERE id = $1")
            .bind(node_id)
            .fetch_optional(&state.db)
            .await?;

    sqlx::query(
        r#"
        INSERT INTO field_definition (
            id, domain_id, defined_at_node_id, field_group_id, field_key, name, hint, type,
            required, is_searchable, is_multi_value, is_encrypted,
            is_removed, is_table, field_order, grid_width, table_column_width,
            created_at, updated_at
        ) VALUES (
            $1, $2, $3, $4, $5, $6, $7, $8,
            $9, $10, $11, $12,
            false, $13, $14, $15, $16,
            NOW(), NOW()
        )
        "#,
    )
    .bind(new_id)
    .bind(domain_id)
    .bind(node_id)
    .bind(group_id)
    .bind(key)
    .bind(payload.name)
    .bind(payload.hint)
    .bind(ftype)
    .bind(req)
    .bind(searchable)
    .bind(multi)
    .bind(encrypted)
    .bind(is_table)
    .bind(order)
    .bind(grid_w)
    .bind(col_w)
    .execute(&state.db)
    .await?;

    let field = fetch_field_by_id(&state.db, new_id)
        .await?
        .ok_or_else(|| AppError::Internal("Failed to fetch inserted field".to_string()))?;

    Ok(Json(field))
}

async fn do_update_field(
    db: &sqlx::PgPool,
    field_id: Uuid,
    payload: FieldDefinitionRequest,
) -> Result<FieldDefinition, AppError> {
    let group_id = payload.resolved_group_id();

    sqlx::query(
        r#"
        UPDATE field_definition
        SET name = COALESCE($1, name),
            hint = COALESCE($2, hint),
            type = COALESCE($3, type),
            required = COALESCE($4, required),
            is_searchable = COALESCE($5, is_searchable),
            is_read_only = COALESCE($6, is_read_only),
            is_hidden = COALESCE($7, is_hidden),
            is_indexed = COALESCE($8, is_indexed),
            field_order = COALESCE($9, field_order),
            grid_width = COALESCE($10, grid_width),
            table_column_width = COALESCE($11, table_column_width),
            field_group_id = COALESCE($12, field_group_id),
            updated_at = NOW()
        WHERE id = $13
        "#,
    )
    .bind(payload.name)
    .bind(payload.hint)
    .bind(payload.field_type)
    .bind(payload.required)
    .bind(payload.is_searchable)
    .bind(payload.is_read_only)
    .bind(payload.is_hidden)
    .bind(payload.is_indexed)
    .bind(payload.field_order)
    .bind(payload.grid_width)
    .bind(payload.table_column_width)
    .bind(group_id)
    .bind(field_id)
    .execute(db)
    .await?;

    let field = fetch_field_by_id(db, field_id)
        .await?
        .ok_or_else(|| AppError::NotFound("Field not found".to_string()))?;

    Ok(field)
}

pub async fn update_field(
    State(state): State<AppState>,
    Path((_node_id, field_id)): Path<(Uuid, Uuid)>,
    Json(payload): Json<FieldDefinitionRequest>,
) -> Result<Json<FieldDefinition>, AppError> {
    let field = do_update_field(&state.db, field_id, payload).await?;
    Ok(Json(field))
}

pub async fn update_domain_field(
    State(state): State<AppState>,
    Path((_domain_id, field_id)): Path<(Uuid, Uuid)>,
    Json(payload): Json<FieldDefinitionRequest>,
) -> Result<Json<FieldDefinition>, AppError> {
    let field = do_update_field(&state.db, field_id, payload).await?;
    Ok(Json(field))
}

pub async fn delete_field(
    State(state): State<AppState>,
    Path((_node_id, field_id)): Path<(Uuid, Uuid)>,
    Query(_params): Query<PageQuery>,
) -> Result<StatusCode, AppError> {
    sqlx::query("UPDATE field_definition SET is_removed = true, updated_at = NOW() WHERE id = $1")
        .bind(field_id)
        .execute(&state.db)
        .await?;

    Ok(axum::http::StatusCode::NO_CONTENT)
}

// Domain-level fields
pub async fn get_domain_fields(
    State(state): State<AppState>,
    Path(domain_id): Path<Uuid>,
) -> Result<Json<Vec<FieldDefinition>>, AppError> {
    let query_str = format!(
        r#"
        {}
        WHERE fd.domain_id = $1 AND (fd.is_removed = false OR fd.is_removed IS NULL)
        ORDER BY 
            COALESCE(s.sort_order, 9999) ASC,
            COALESCE(fg.sort_order, 9999) ASC,
            fd.field_order ASC,
            fd.created_at ASC
        "#,
        FIELD_SELECT_SQL
    );

    let fields = sqlx::query_as::<_, FieldDefinition>(&query_str)
        .bind(domain_id)
        .fetch_all(&state.db)
        .await?;

    Ok(Json(fields))
}

pub async fn get_domain_fields_page(
    State(state): State<AppState>,
    Path(domain_id): Path<Uuid>,
    Query(params): Query<PageQuery>,
) -> Result<Json<PageResponse<FieldDefinition>>, AppError> {
    let page = params.page.unwrap_or(0);
    let size = params.size.unwrap_or(100);
    let offset = page * size;

    let total: (i64,) = sqlx::query_as(
        "SELECT COUNT(*) FROM field_definition WHERE domain_id = $1 AND (is_removed = false OR is_removed IS NULL)"
    )
    .bind(domain_id)
    .fetch_one(&state.db)
    .await?;

    let query_str = format!(
        r#"
        {}
        WHERE fd.domain_id = $1 AND (fd.is_removed = false OR fd.is_removed IS NULL)
        ORDER BY 
            COALESCE(s.sort_order, 9999) ASC,
            COALESCE(fg.sort_order, 9999) ASC,
            fd.field_order ASC,
            fd.created_at ASC
        LIMIT $2 OFFSET $3
        "#,
        FIELD_SELECT_SQL
    );

    let content = sqlx::query_as::<_, FieldDefinition>(&query_str)
        .bind(domain_id)
        .bind(size)
        .bind(offset)
        .fetch_all(&state.db)
        .await?;

    Ok(Json(PageResponse::new(content, total.0, page, size)))
}

pub async fn add_domain_field(
    State(state): State<AppState>,
    Path(domain_id): Path<Uuid>,
    Json(payload): Json<FieldDefinitionRequest>,
) -> Result<Json<FieldDefinition>, AppError> {
    let new_id = Uuid::new_v4();
    let key = payload
        .field_key
        .clone()
        .unwrap_or_else(|| format!("field_{}", &new_id.to_string()[..8]));
    let ftype = payload
        .field_type
        .clone()
        .unwrap_or_else(|| "STRING".to_string());
    let req = payload.required.unwrap_or(false);
    let searchable = payload.is_searchable.unwrap_or(true);
    let multi = payload.is_multi_value.unwrap_or(false);
    let encrypted = payload.is_encrypted.unwrap_or(false);
    let order = payload.field_order.unwrap_or(1);
    let is_table = payload.is_table.unwrap_or(false);
    let grid_w = payload.grid_width.unwrap_or(3);
    let col_w = payload.table_column_width.unwrap_or(150);
    let group_id = payload.resolved_group_id();

    sqlx::query(
        r#"
        INSERT INTO field_definition (
            id, domain_id, defined_at_node_id, field_group_id, field_key, name, hint, type,
            required, is_searchable, is_multi_value, is_encrypted,
            is_removed, is_table, field_order, grid_width, table_column_width,
            created_at, updated_at
        ) VALUES (
            $1, $2, NULL, $3, $4, $5, $6, $7,
            $8, $9, $10, $11,
            false, $12, $13, $14, $15,
            NOW(), NOW()
        )
        "#,
    )
    .bind(new_id)
    .bind(domain_id)
    .bind(group_id)
    .bind(key)
    .bind(payload.name)
    .bind(payload.hint)
    .bind(ftype)
    .bind(req)
    .bind(searchable)
    .bind(multi)
    .bind(encrypted)
    .bind(is_table)
    .bind(order)
    .bind(grid_w)
    .bind(col_w)
    .execute(&state.db)
    .await?;

    let field = fetch_field_by_id(&state.db, new_id)
        .await?
        .ok_or_else(|| AppError::Internal("Failed to fetch inserted field".to_string()))?;

    Ok(Json(field))
}

pub async fn delete_domain_field(
    State(state): State<AppState>,
    Path((_domain_id, field_id)): Path<(Uuid, Uuid)>,
    Query(_params): Query<PageQuery>,
) -> Result<StatusCode, AppError> {
    sqlx::query("UPDATE field_definition SET is_removed = true, updated_at = NOW() WHERE id = $1")
        .bind(field_id)
        .execute(&state.db)
        .await?;

    Ok(axum::http::StatusCode::NO_CONTENT)
}

// Node metadata
#[derive(Debug, Serialize, sqlx::FromRow)]
#[serde(rename_all = "camelCase")]
pub struct NodeDetail {
    pub id: Uuid,
    pub domain_id: Uuid,
    pub parent_id: Option<Uuid>,
    pub name: serde_json::Value,
    pub path: String,
    pub depth: i32,
    pub node_order: i32,
    pub icon: Option<String>,
}

pub async fn get_node_by_id(
    State(state): State<AppState>,
    Path(node_id): Path<Uuid>,
) -> Result<Json<NodeDetail>, AppError> {
    let node = sqlx::query_as::<_, NodeDetail>(
        r#"
        SELECT id, domain_id, parent_id, name, path, depth, node_order, icon
        FROM classification_node
        WHERE id = $1 AND is_deleted = false
        "#,
    )
    .bind(node_id)
    .fetch_optional(&state.db)
    .await?
    .ok_or_else(|| AppError::NotFound("Node not found".to_string()))?;

    Ok(Json(node))
}

pub async fn get_node_info(
    State(state): State<AppState>,
    Path(node_id): Path<Uuid>,
) -> Result<Json<serde_json::Value>, AppError> {
    let node = get_node_by_id(State(state.clone()), Path(node_id)).await?.0;
    let field_count: (i64,) = sqlx::query_as(
        "SELECT COUNT(*) FROM field_definition WHERE defined_at_node_id = $1 AND is_removed = false"
    )
    .bind(node_id)
    .fetch_one(&state.db)
    .await?;

    let record_count: (i64,) =
        sqlx::query_as("SELECT COUNT(*) FROM record WHERE node_id = $1 AND status != 'DELETED'")
            .bind(node_id)
            .fetch_one(&state.db)
            .await?;

    Ok(Json(serde_json::json!({
        "node": node,
        "fieldCount": field_count.0,
        "recordCount": record_count.0
    })))
}

#[derive(Debug, Deserialize)]
pub struct TreeQuery {
    #[serde(alias = "axisId")]
    pub axis_id: Option<Uuid>,
}

#[derive(Debug, Clone, sqlx::FromRow)]
pub struct NodeRow {
    pub id: Uuid,
    pub domain_id: Uuid,
    pub parent_id: Option<Uuid>,
    pub axis_id: Option<Uuid>,
    pub name: serde_json::Value,
    pub path: String,
    pub depth: i32,
    pub node_order: i32,
    pub icon: Option<String>,
    pub detail_layout_config: Option<serde_json::Value>,
}

#[derive(Debug, Clone, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct NodeTreeNode {
    pub id: Uuid,
    pub domain_id: Uuid,
    pub parent_id: Option<Uuid>,
    pub axis_id: Option<Uuid>,
    pub name: serde_json::Value,
    pub path: String,
    pub depth: i32,
    pub order: i32,
    pub node_order: i32,
    pub icon: Option<String>,
    pub detail_layout_config: Option<serde_json::Value>,
    pub children: Vec<NodeTreeNode>,
}

fn build_node_tree(nodes: &[NodeRow], target_root_ids: &[Uuid]) -> Vec<NodeTreeNode> {
    use std::collections::HashMap;

    let mut children_map: HashMap<Uuid, Vec<&NodeRow>> = HashMap::new();
    for node in nodes {
        if let Some(parent_id) = node.parent_id {
            children_map.entry(parent_id).or_default().push(node);
        }
    }

    for children in children_map.values_mut() {
        children.sort_by_key(|n| n.node_order);
    }

    fn attach(node: &NodeRow, children_map: &HashMap<Uuid, Vec<&NodeRow>>) -> NodeTreeNode {
        let child_nodes = children_map
            .get(&node.id)
            .map(|list| {
                list.iter()
                    .map(|child| attach(child, children_map))
                    .collect()
            })
            .unwrap_or_default();

        NodeTreeNode {
            id: node.id,
            domain_id: node.domain_id,
            parent_id: node.parent_id,
            axis_id: node.axis_id,
            name: node.name.clone(),
            path: node.path.clone(),
            depth: node.depth,
            order: node.node_order,
            node_order: node.node_order,
            icon: node.icon.clone(),
            detail_layout_config: node.detail_layout_config.clone(),
            children: child_nodes,
        }
    }

    let node_map: HashMap<Uuid, &NodeRow> = nodes.iter().map(|n| (n.id, n)).collect();

    target_root_ids
        .iter()
        .filter_map(|id| node_map.get(id))
        .map(|root| attach(root, &children_map))
        .collect()
}

pub async fn get_domain_nodes_tree(
    State(state): State<AppState>,
    Path(domain_id): Path<Uuid>,
    Query(query): Query<TreeQuery>,
) -> Result<Json<Vec<NodeTreeNode>>, AppError> {
    let all_nodes = sqlx::query_as::<_, NodeRow>(
        r#"
        SELECT id, domain_id, parent_id, axis_id, name, path, depth, node_order, icon, detail_layout_config
        FROM classification_node
        WHERE domain_id = $1 AND is_deleted = false
        ORDER BY depth ASC, node_order ASC
        "#
    )
    .bind(domain_id)
    .fetch_all(&state.db)
    .await?;

    if all_nodes.is_empty() {
        return Ok(Json(Vec::new()));
    }

    let root_candidates: Vec<&NodeRow> = if let Some(axis_id) = query.axis_id {
        let roots: Vec<&NodeRow> = all_nodes
            .iter()
            .filter(|n| n.parent_id.is_none() && n.axis_id == Some(axis_id))
            .collect();
        if roots.is_empty() {
            all_nodes.iter().filter(|n| n.parent_id.is_none()).collect()
        } else {
            roots
        }
    } else {
        let default_axis: Option<(Uuid,)> = sqlx::query_as(
            "SELECT id FROM classification_axis WHERE domain_id = $1 AND is_default = true LIMIT 1",
        )
        .bind(domain_id)
        .fetch_optional(&state.db)
        .await?;

        let mut roots = Vec::new();
        if let Some((default_axis_id,)) = default_axis {
            roots = all_nodes
                .iter()
                .filter(|n| n.parent_id.is_none() && n.axis_id == Some(default_axis_id))
                .collect();
        }
        if roots.is_empty() {
            roots = all_nodes
                .iter()
                .filter(|n| n.parent_id.is_none() && n.axis_id.is_none())
                .collect();
        }
        if roots.is_empty() {
            roots = all_nodes.iter().filter(|n| n.parent_id.is_none()).collect();
        }
        roots
    };

    let mut sorted_roots = root_candidates;
    sorted_roots.sort_by_key(|n| n.node_order);
    let root_ids: Vec<Uuid> = sorted_roots.iter().map(|n| n.id).collect();

    let tree = if root_ids.is_empty() {
        let min_depth = all_nodes.iter().map(|n| n.depth).min().unwrap_or(0);
        let fallback_roots: Vec<Uuid> = all_nodes
            .iter()
            .filter(|n| n.depth == min_depth)
            .map(|n| n.id)
            .collect();
        build_node_tree(&all_nodes, &fallback_roots)
    } else {
        build_node_tree(&all_nodes, &root_ids)
    };

    Ok(Json(tree))
}

pub async fn get_all_nodes_tree(
    State(state): State<AppState>,
) -> Result<Json<Vec<NodeTreeNode>>, AppError> {
    let all_nodes = sqlx::query_as::<_, NodeRow>(
        r#"
        SELECT id, domain_id, parent_id, axis_id, name, path, depth, node_order, icon, detail_layout_config
        FROM classification_node
        WHERE is_deleted = false
        ORDER BY depth ASC, node_order ASC
        "#
    )
    .fetch_all(&state.db)
    .await?;

    if all_nodes.is_empty() {
        return Ok(Json(Vec::new()));
    }

    let mut root_nodes: Vec<&NodeRow> =
        all_nodes.iter().filter(|n| n.parent_id.is_none()).collect();
    root_nodes.sort_by_key(|n| (n.domain_id, n.node_order));
    let root_ids: Vec<Uuid> = root_nodes.iter().map(|n| n.id).collect();

    let tree = build_node_tree(&all_nodes, &root_ids);
    Ok(Json(tree))
}

/// Batch endpoint: returns all domains with their axes and classification node trees.
/// Replaces N+1 per-domain fetching with 3 SQL queries total.
pub async fn get_domains_batch_trees(
    State(state): State<AppState>,
) -> Result<Json<Vec<serde_json::Value>>, AppError> {
    use std::collections::HashMap;

    // 1. Fetch all domains
    let domains: Vec<(Uuid, serde_json::Value, Option<String>, Option<serde_json::Value>, i32)> =
        sqlx::query_as(
            r#"SELECT id, name, icon, description, sort_order
               FROM domain
               ORDER BY sort_order ASC, created_at ASC"#,
        )
        .fetch_all(&state.db)
        .await?;

    // 2. Fetch all classification axes
    let axes: Vec<(Uuid, Uuid, serde_json::Value, Option<String>, bool)> = sqlx::query_as(
        r#"SELECT id, domain_id, name, axis_code, is_default
           FROM classification_axis
           ORDER BY domain_id, sort_order ASC, created_at ASC"#,
    )
    .fetch_all(&state.db)
    .await?;

    // 3. Fetch all classification nodes
    let all_nodes = sqlx::query_as::<_, NodeRow>(
        r#"SELECT id, domain_id, parent_id, axis_id, name, path, depth, node_order, icon, detail_layout_config
           FROM classification_node
           WHERE is_deleted = false
           ORDER BY depth ASC, node_order ASC"#,
    )
    .fetch_all(&state.db)
    .await?;

    // Group axes by domain_id
    let mut axes_by_domain: HashMap<Uuid, Vec<&(Uuid, Uuid, serde_json::Value, Option<String>, bool)>> =
        HashMap::new();
    for axis in &axes {
        axes_by_domain.entry(axis.1).or_default().push(axis);
    }

    // Group nodes by domain_id
    let mut nodes_by_domain: HashMap<Uuid, Vec<&NodeRow>> = HashMap::new();
    for node in &all_nodes {
        nodes_by_domain.entry(node.domain_id).or_default().push(node);
    }

    // Build response
    let mut result = Vec::with_capacity(domains.len());
    for (domain_id, domain_name, domain_icon, domain_desc, sort_order) in &domains {
        let domain_axes = axes_by_domain.get(domain_id).map(|a| a.as_slice()).unwrap_or(&[]);

        // Build axes JSON array
        let axes_json: Vec<serde_json::Value> = domain_axes
            .iter()
            .map(|(id, _, name, code, is_default)| {
                serde_json::json!({
                    "id": id,
                    "name": name,
                    "axisCode": code,
                    "isDefault": is_default
                })
            })
            .collect();

        // Build tree for this domain
        let tree = if let Some(domain_nodes) = nodes_by_domain.get(domain_id) {
            // Collect owned NodeRow copies for build_node_tree
            let owned_nodes: Vec<NodeRow> = domain_nodes.iter().map(|n| (*n).clone()).collect();

            // Find default axis for root selection
            let default_axis_id = domain_axes
                .iter()
                .find(|(_, _, _, _, is_default)| *is_default)
                .map(|(id, _, _, _, _)| *id);

            // Select root nodes (same logic as get_domain_nodes_tree)
            let mut roots: Vec<&NodeRow> = if let Some(axis_id) = default_axis_id {
                let axis_roots: Vec<&NodeRow> = owned_nodes
                    .iter()
                    .filter(|n| n.parent_id.is_none() && n.axis_id == Some(axis_id))
                    .collect();
                if axis_roots.is_empty() {
                    owned_nodes.iter().filter(|n| n.parent_id.is_none()).collect()
                } else {
                    axis_roots
                }
            } else {
                // No default axis — try nodes without axis_id first
                let no_axis_roots: Vec<&NodeRow> = owned_nodes
                    .iter()
                    .filter(|n| n.parent_id.is_none() && n.axis_id.is_none())
                    .collect();
                if no_axis_roots.is_empty() {
                    owned_nodes.iter().filter(|n| n.parent_id.is_none()).collect()
                } else {
                    no_axis_roots
                }
            };

            roots.sort_by_key(|n| n.node_order);
            let root_ids: Vec<Uuid> = roots.iter().map(|n| n.id).collect();

            if root_ids.is_empty() && !owned_nodes.is_empty() {
                let min_depth = owned_nodes.iter().map(|n| n.depth).min().unwrap_or(0);
                let fallback: Vec<Uuid> = owned_nodes
                    .iter()
                    .filter(|n| n.depth == min_depth)
                    .map(|n| n.id)
                    .collect();
                build_node_tree(&owned_nodes, &fallback)
            } else {
                build_node_tree(&owned_nodes, &root_ids)
            }
        } else {
            Vec::new()
        };

        result.push(serde_json::json!({
            "domain": {
                "id": domain_id,
                "name": domain_name,
                "icon": domain_icon,
                "description": domain_desc,
                "sortOrder": sort_order,
                "domainOrder": sort_order
            },
            "axes": axes_json,
            "tree": tree
        }));
    }

    Ok(Json(result))
}

// Field DQ Rules
pub async fn get_field_dq_rules(
    State(state): State<AppState>,
    Path(field_id): Path<Uuid>,
) -> Result<Json<Vec<serde_json::Value>>, AppError> {
    let rules: Vec<(Uuid, String, String, serde_json::Value)> = sqlx::query_as(
        r#"
        SELECT id, rule_name, rule_type, rule_value
        FROM dq_rule
        WHERE field_definition_id = $1
        "#,
    )
    .bind(field_id)
    .fetch_all(&state.db)
    .await?;

    let res: Vec<serde_json::Value> = rules
        .into_iter()
        .map(|(id, name, rtype, val)| {
            serde_json::json!({
                "id": id,
                "ruleName": name,
                "ruleType": rtype,
                "ruleValue": val
            })
        })
        .collect();

    Ok(Json(res))
}

pub async fn save_field_dq_rules(
    State(state): State<AppState>,
    Path(field_id): Path<Uuid>,
    Json(payload): Json<Vec<serde_json::Value>>,
) -> Result<Json<serde_json::Value>, AppError> {
    // Delete existing and insert new
    sqlx::query("DELETE FROM dq_rule WHERE field_definition_id = $1")
        .bind(field_id)
        .execute(&state.db)
        .await?;

    for rule in &payload {
        let name = rule
            .get("ruleName")
            .and_then(|v| v.as_str())
            .unwrap_or("Custom Rule");
        let rtype = rule
            .get("ruleType")
            .and_then(|v| v.as_str())
            .unwrap_or("REGEX");
        let val = rule
            .get("ruleValue")
            .cloned()
            .unwrap_or(serde_json::json!({}));
        let new_id = Uuid::new_v4();

        sqlx::query(
            r#"
            INSERT INTO dq_rule (id, field_definition_id, rule_name, rule_type, rule_value, severity, is_active, created_at, updated_at)
            VALUES ($1, $2, $3, $4, $5, 'WARNING', true, NOW(), NOW())
            "#
        )
        .bind(new_id)
        .bind(field_id)
        .bind(name)
        .bind(ftype_or_default(rtype))
        .bind(val)
        .execute(&state.db)
        .await?;
    }

    Ok(Json(
        serde_json::json!({ "success": true, "count": payload.len() }),
    ))
}

fn ftype_or_default(rtype: &str) -> &str {
    match rtype {
        "NOT_NULL" | "RANGE" | "ENUM" | "REGEX" | "LENGTH" => rtype,
        _ => "REGEX",
    }
}
