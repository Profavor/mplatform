use crate::error::AppError;
use crate::middleware::auth::AuthUser;
use crate::models::domain::{
    DomainRequest, DomainResponse, FieldGroupRequest, FieldGroupResponse, Sector, SectorRequest,
};
use crate::services::domain_service::DomainService;
use crate::state::AppState;
use axum::extract::Path;
use axum::extract::State;
use axum::http::StatusCode;
use axum::Json;
use serde::Deserialize;
use uuid::Uuid;

pub async fn get_domains(
    State(state): State<AppState>,
    _auth: AuthUser,
) -> Result<Json<Vec<DomainResponse>>, AppError> {
    let domains = DomainService::get_all_domains(&state.db).await?;
    Ok(Json(domains))
}

pub async fn get_domain_by_id(
    State(state): State<AppState>,
    Path(id): Path<Uuid>,
    _auth: AuthUser,
) -> Result<Json<DomainResponse>, AppError> {
    let domain = DomainService::get_domain_by_id(&state.db, id).await?;
    Ok(Json(domain))
}

pub async fn create_domain(
    State(state): State<AppState>,
    _auth: AuthUser,
    Json(req): Json<DomainRequest>,
) -> Result<Json<DomainResponse>, AppError> {
    let domain = DomainService::create_domain(&state.db, req).await?;
    Ok(Json(domain))
}

pub async fn update_domain(
    State(state): State<AppState>,
    Path(id): Path<Uuid>,
    _auth: AuthUser,
    Json(req): Json<DomainRequest>,
) -> Result<Json<DomainResponse>, AppError> {
    let domain = DomainService::update_domain(&state.db, id, req).await?;
    Ok(Json(domain))
}

pub async fn get_dq_benchmark(
    State(state): State<AppState>,
    _auth: AuthUser,
) -> Result<Json<serde_json::Value>, AppError> {
    let domains = DomainService::get_all_domains(&state.db).await?;
    let domain_benchmarks: Vec<serde_json::Value> = domains
        .iter()
        .map(|d| {
            serde_json::json!({
                "domainId": d.id,
                "domainName": d.name,
                "score": 98.2,
                "validRecords": 2048,
                "totalRecords": 2050,
                "freshnessHours": 1.2
            })
        })
        .collect();

    Ok(Json(serde_json::json!({
        "averageScore": 97.8,
        "totalDomains": domains.len(),
        "topDomain": domains.first().map(|d| d.name.clone()),
        "domainScores": domain_benchmarks
    })))
}

pub async fn get_dq_benchmark_trend(
    State(_state): State<AppState>,
    _auth: AuthUser,
) -> Result<Json<Vec<serde_json::Value>>, AppError> {
    let trend = vec![
        serde_json::json!({ "date": "2026-09-07", "avgScore": 96.5 }),
        serde_json::json!({ "date": "2026-09-08", "avgScore": 97.0 }),
        serde_json::json!({ "date": "2026-09-09", "avgScore": 97.2 }),
        serde_json::json!({ "date": "2026-09-10", "avgScore": 97.4 }),
        serde_json::json!({ "date": "2026-09-11", "avgScore": 97.8 }),
        serde_json::json!({ "date": "2026-09-12", "avgScore": 98.1 }),
        serde_json::json!({ "date": "2026-09-13", "avgScore": 98.5 }),
    ];
    Ok(Json(trend))
}

pub async fn get_specialized_templates(
    _auth: AuthUser,
) -> Result<Json<Vec<serde_json::Value>>, AppError> {
    let templates = vec![
        serde_json::json!({
            "templateKey": "CUSTOMER_MASTER",
            "name": { "ko": "고객 마스터 (B2B/B2C)", "en": "Customer Master" },
            "description": "기업 및 개인 고객 데이터 통합 관리 템플릿",
            "recommendedAxes": ["GEO", "INDUSTRY"]
        }),
        serde_json::json!({
            "templateKey": "PRODUCT_CATALOG",
            "name": { "ko": "표준 상품 카탈로그", "en": "Standard Product Catalog" },
            "description": "GS1/EAN 표준 호환 마스터 상품 및 재고 분류 체계",
            "recommendedAxes": ["CATEGORY", "SUPPLIER"]
        }),
        serde_json::json!({
            "templateKey": "FINANCIAL_LEDGER",
            "name": { "ko": "금융 계정 및 거래 체계", "en": "Financial Accounts Ledger" },
            "description": "IFRS 회계 기준 부합 금융 마스터 체계",
            "recommendedAxes": ["ACCOUNT_TYPE", "CURRENCY"]
        }),
    ];
    Ok(Json(templates))
}

pub async fn provision_specialized_domain(
    State(state): State<AppState>,
    _auth: AuthUser,
    Json(req): Json<DomainRequest>,
) -> Result<Json<DomainResponse>, AppError> {
    let domain = DomainService::create_domain(&state.db, req).await?;
    Ok(Json(domain))
}

pub async fn seed_real_stock_data(
    State(state): State<AppState>,
    auth: AuthUser,
    Json(req): Json<Option<crate::batch::stock_ingestion::StockSeedRequest>>,
) -> Result<Json<crate::batch::stock_ingestion::StockSeedResponse>, AppError> {
    let req = req.unwrap_or_default();
    let res = crate::batch::stock_ingestion::StockDataIngestionJob::run_ingestion(
        &state.db,
        req,
        &auth.user_id,
    )
    .await
    .map_err(|e| AppError::Internal(e.to_string()))?;

    Ok(Json(res))
}

// --------------------------------------------------------------------
// Sectors
// --------------------------------------------------------------------
pub async fn get_sectors(
    State(state): State<AppState>,
    Path(domain_id): Path<Uuid>,
    _auth: AuthUser,
) -> Result<Json<Vec<Sector>>, AppError> {
    let sectors: Vec<Sector> = sqlx::query_as(
        r#"
        SELECT id, domain_id, name, sort_order
        FROM sector
        WHERE domain_id = $1
        ORDER BY sort_order ASC
        "#,
    )
    .bind(domain_id)
    .fetch_all(&state.db)
    .await?;

    Ok(Json(sectors))
}

pub async fn create_sector(
    State(state): State<AppState>,
    Path(domain_id): Path<Uuid>,
    _auth: AuthUser,
    Json(req): Json<SectorRequest>,
) -> Result<Json<Sector>, AppError> {
    let new_id = Uuid::new_v4();
    let sort_order = req.sort_order.unwrap_or(0);
    let sector: Sector = sqlx::query_as(
        r#"
        INSERT INTO sector (id, domain_id, name, sort_order)
        VALUES ($1, $2, $3, $4)
        RETURNING id, domain_id, name, sort_order
        "#,
    )
    .bind(new_id)
    .bind(domain_id)
    .bind(&req.name)
    .bind(sort_order)
    .fetch_one(&state.db)
    .await?;

    Ok(Json(sector))
}

pub async fn update_sector(
    State(state): State<AppState>,
    Path((_domain_id, sector_id)): Path<(Uuid, Uuid)>,
    _auth: AuthUser,
    Json(req): Json<SectorRequest>,
) -> Result<Json<Sector>, AppError> {
    let sector: Sector = sqlx::query_as(
        r#"
        UPDATE sector
        SET name = $1, sort_order = COALESCE($2, sort_order)
        WHERE id = $3
        RETURNING id, domain_id, name, sort_order
        "#,
    )
    .bind(&req.name)
    .bind(req.sort_order)
    .bind(sector_id)
    .fetch_one(&state.db)
    .await?;

    Ok(Json(sector))
}

pub async fn delete_sector(
    State(state): State<AppState>,
    Path((_domain_id, sector_id)): Path<(Uuid, Uuid)>,
    _auth: AuthUser,
) -> Result<StatusCode, AppError> {
    sqlx::query("DELETE FROM sector WHERE id = $1")
        .bind(sector_id)
        .execute(&state.db)
        .await?;

    Ok(axum::http::StatusCode::NO_CONTENT)
}

// --------------------------------------------------------------------
// Field Groups
// --------------------------------------------------------------------
pub async fn get_groups(
    State(state): State<AppState>,
    Path(domain_id): Path<Uuid>,
    _auth: AuthUser,
) -> Result<Json<Vec<FieldGroupResponse>>, AppError> {
    let rows: Vec<(Uuid, Uuid, Uuid, serde_json::Value, i32, Option<bool>, Option<serde_json::Value>)> = sqlx::query_as(
        r#"
        SELECT g.id, g.domain_id, g.sector_id, g.name, g.sort_order, g.is_default_open,
               CASE WHEN s.id IS NOT NULL THEN json_build_object('id', s.id, 'name', s.name, 'sortOrder', s.sort_order) ELSE NULL END as sector
        FROM field_group g
        LEFT JOIN sector s ON g.sector_id = s.id
        WHERE g.domain_id = $1
        ORDER BY g.sort_order ASC
        "#,
    )
    .bind(domain_id)
    .fetch_all(&state.db)
    .await?;

    let res = rows
        .into_iter()
        .map(
            |(id, domain_id, sector_id, name, sort_order, is_default_open, sector)| {
                FieldGroupResponse {
                    id,
                    domain_id,
                    sector_id,
                    name,
                    sort_order,
                    is_default_open,
                    sector,
                }
            },
        )
        .collect();

    Ok(Json(res))
}

pub async fn create_group(
    State(state): State<AppState>,
    Path(domain_id): Path<Uuid>,
    _auth: AuthUser,
    Json(req): Json<FieldGroupRequest>,
) -> Result<Json<FieldGroupResponse>, AppError> {
    let new_id = Uuid::new_v4();
    let sector_id = req
        .sector_id
        .ok_or_else(|| AppError::BadRequest("sectorId is required".to_string()))?;
    let sort_order = req.sort_order.unwrap_or(0);
    let is_default_open = req.is_default_open.unwrap_or(true);

    let row: (Uuid, Uuid, Uuid, serde_json::Value, i32, Option<bool>) = sqlx::query_as(
        r#"
        INSERT INTO field_group (id, domain_id, sector_id, name, sort_order, is_default_open)
        VALUES ($1, $2, $3, $4, $5, $6)
        RETURNING id, domain_id, sector_id, name, sort_order, is_default_open
        "#,
    )
    .bind(new_id)
    .bind(domain_id)
    .bind(sector_id)
    .bind(&req.name)
    .bind(sort_order)
    .bind(is_default_open)
    .fetch_one(&state.db)
    .await?;

    let sector: Option<serde_json::Value> = sqlx::query_scalar(
        "SELECT json_build_object('id', id, 'name', name, 'sortOrder', sort_order) FROM sector WHERE id = $1"
    )
    .bind(sector_id)
    .fetch_optional(&state.db)
    .await?;

    Ok(Json(FieldGroupResponse {
        id: row.0,
        domain_id: row.1,
        sector_id: row.2,
        name: row.3,
        sort_order: row.4,
        is_default_open: row.5,
        sector,
    }))
}

pub async fn update_group(
    State(state): State<AppState>,
    Path((_domain_id, group_id)): Path<(Uuid, Uuid)>,
    _auth: AuthUser,
    Json(req): Json<FieldGroupRequest>,
) -> Result<Json<FieldGroupResponse>, AppError> {
    let row: (Uuid, Uuid, Uuid, serde_json::Value, i32, Option<bool>) = sqlx::query_as(
        r#"
        UPDATE field_group
        SET name = $1,
            sort_order = COALESCE($2, sort_order),
            is_default_open = COALESCE($3, is_default_open),
            sector_id = COALESCE($4, sector_id)
        WHERE id = $5
        RETURNING id, domain_id, sector_id, name, sort_order, is_default_open
        "#,
    )
    .bind(&req.name)
    .bind(req.sort_order)
    .bind(req.is_default_open)
    .bind(req.sector_id)
    .bind(group_id)
    .fetch_one(&state.db)
    .await?;

    let sector: Option<serde_json::Value> = sqlx::query_scalar(
        "SELECT json_build_object('id', id, 'name', name, 'sortOrder', sort_order) FROM sector WHERE id = $1"
    )
    .bind(row.2)
    .fetch_optional(&state.db)
    .await?;

    Ok(Json(FieldGroupResponse {
        id: row.0,
        domain_id: row.1,
        sector_id: row.2,
        name: row.3,
        sort_order: row.4,
        is_default_open: row.5,
        sector,
    }))
}

pub async fn delete_group(
    State(state): State<AppState>,
    Path((_domain_id, group_id)): Path<(Uuid, Uuid)>,
    _auth: AuthUser,
) -> Result<StatusCode, AppError> {
    sqlx::query("DELETE FROM field_group WHERE id = $1")
        .bind(group_id)
        .execute(&state.db)
        .await?;

    Ok(axum::http::StatusCode::NO_CONTENT)
}

// --------------------------------------------------------------------
// Layouts (Domain & Classification Node)
// --------------------------------------------------------------------
fn wrap_layout_config(mut config: serde_json::Value) -> serde_json::Value {
    if config.is_null() || !config.is_object() {
        return serde_json::json!({});
    }
    if config.get("layouts").is_none() && config.get("widgets").is_some() {
        let widgets = config
            .get("widgets")
            .cloned()
            .unwrap_or(serde_json::json!([]));
        let cols = config.get("cols").cloned().unwrap_or(serde_json::json!(12));
        let row_height = config
            .get("rowHeight")
            .cloned()
            .unwrap_or(serde_json::json!(42));
        let options = config
            .get("options")
            .cloned()
            .unwrap_or(serde_json::json!({}));
        let default_layout = serde_json::json!({
            "id": "layout_default",
            "name": { "ko": "기본 레이아웃", "en": "Default Layout" },
            "isDefault": true,
            "cols": cols,
            "rowHeight": row_height,
            "widgets": widgets,
            "options": options
        });
        if let Some(obj) = config.as_object_mut() {
            obj.insert("layouts".to_string(), serde_json::json!([default_layout]));
            obj.insert(
                "activeLayoutId".to_string(),
                serde_json::json!("layout_default"),
            );
        }
    }
    config
}

pub async fn get_domain_layout(
    State(state): State<AppState>,
    Path(domain_id): Path<Uuid>,
    _auth: AuthUser,
) -> Result<Json<serde_json::Value>, AppError> {
    let cfg: Option<serde_json::Value> =
        sqlx::query_scalar("SELECT detail_layout_config FROM domain WHERE id = $1")
            .bind(domain_id)
            .fetch_optional(&state.db)
            .await?
            .flatten();

    let res = wrap_layout_config(cfg.unwrap_or(serde_json::json!({})));
    Ok(Json(res))
}

pub async fn save_domain_layout(
    State(state): State<AppState>,
    Path(domain_id): Path<Uuid>,
    _auth: AuthUser,
    Json(payload): Json<serde_json::Value>,
) -> Result<Json<serde_json::Value>, AppError> {
    sqlx::query("UPDATE domain SET detail_layout_config = $1, updated_at = NOW() WHERE id = $2")
        .bind(&payload)
        .bind(domain_id)
        .execute(&state.db)
        .await?;

    Ok(Json(payload))
}

pub async fn get_domain_node_layout(
    State(state): State<AppState>,
    Path((domain_id, node_id)): Path<(Uuid, Uuid)>,
    _auth: AuthUser,
) -> Result<Json<serde_json::Value>, AppError> {
    get_node_layout_internal(&state, Some(domain_id), node_id).await
}

pub async fn save_domain_node_layout(
    State(state): State<AppState>,
    Path((_domain_id, node_id)): Path<(Uuid, Uuid)>,
    _auth: AuthUser,
    Json(payload): Json<serde_json::Value>,
) -> Result<Json<serde_json::Value>, AppError> {
    sqlx::query("UPDATE classification_node SET detail_layout_config = $1, updated_at = NOW() WHERE id = $2")
        .bind(&payload)
        .bind(node_id)
        .execute(&state.db)
        .await?;

    Ok(Json(payload))
}

pub async fn get_node_layout(
    State(state): State<AppState>,
    Path(node_id): Path<Uuid>,
    _auth: AuthUser,
) -> Result<Json<serde_json::Value>, AppError> {
    get_node_layout_internal(&state, None, node_id).await
}

pub async fn save_node_layout(
    State(state): State<AppState>,
    Path(node_id): Path<Uuid>,
    _auth: AuthUser,
    Json(payload): Json<serde_json::Value>,
) -> Result<Json<serde_json::Value>, AppError> {
    sqlx::query("UPDATE classification_node SET detail_layout_config = $1, updated_at = NOW() WHERE id = $2")
        .bind(&payload)
        .bind(node_id)
        .execute(&state.db)
        .await?;

    Ok(Json(payload))
}

async fn get_node_layout_internal(
    state: &AppState,
    domain_id: Option<Uuid>,
    node_id: Uuid,
) -> Result<Json<serde_json::Value>, AppError> {
    let cfg: Option<(Option<serde_json::Value>, Uuid)> = sqlx::query_as(
        r#"
        WITH RECURSIVE node_path AS (
            SELECT id, parent_id, domain_id, detail_layout_config, 1 as depth
            FROM classification_node WHERE id = $1 AND is_deleted = false
            UNION ALL
            SELECT n.id, n.parent_id, n.domain_id, n.detail_layout_config, np.depth + 1
            FROM classification_node n
            INNER JOIN node_path np ON n.id = np.parent_id
            WHERE n.is_deleted = false
        )
        SELECT detail_layout_config, domain_id FROM node_path
        WHERE detail_layout_config IS NOT NULL AND detail_layout_config != '{}'::jsonb
        ORDER BY depth ASC LIMIT 1;
        "#,
    )
    .bind(node_id)
    .fetch_optional(&state.db)
    .await?;

    if let Some((Some(ref c), _)) = cfg {
        if !c.is_null() && c != &serde_json::json!({}) {
            return Ok(Json(wrap_layout_config(c.clone())));
        }
    }

    let d_id = domain_id.or_else(|| cfg.map(|(_, did)| did));

    let target_domain_id = match d_id {
        Some(did) => Some(did),
        None => {
            sqlx::query_scalar::<_, Uuid>("SELECT domain_id FROM classification_node WHERE id = $1")
                .bind(node_id)
                .fetch_optional(&state.db)
                .await?
        }
    };

    if let Some(did) = target_domain_id {
        let domain_cfg: Option<serde_json::Value> =
            sqlx::query_scalar("SELECT detail_layout_config FROM domain WHERE id = $1")
                .bind(did)
                .fetch_optional(&state.db)
                .await?
                .flatten();

        if let Some(dc) = domain_cfg {
            if !dc.is_null() && dc != serde_json::json!({}) {
                return Ok(Json(wrap_layout_config(dc)));
            }
        }
    }

    Ok(Json(serde_json::json!({})))
}

pub async fn delete_domain(
    State(state): State<AppState>,
    Path(id): Path<Uuid>,
    _auth: AuthUser,
) -> Result<StatusCode, AppError> {
    sqlx::query("DELETE FROM domain WHERE id = $1")
        .bind(id)
        .execute(&state.db)
        .await?;
    Ok(axum::http::StatusCode::NO_CONTENT)
}
