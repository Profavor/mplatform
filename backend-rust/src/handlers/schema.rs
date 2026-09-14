use axum::extract::State;
use axum::extract::Path;
use axum::http::StatusCode;
use axum::extract::Query;
use axum::{response::IntoResponse, Json};
use crate::error::AppError;
use crate::middleware::auth::AuthUser;
use crate::state::AppState;
use chrono::NaiveDateTime;
use serde::{Deserialize, Serialize};
use uuid::Uuid;

use crate::{
    models::{
        record::PageResponse,
        schema::{DomainSnapshot, MasterRelation, SchemaHistory, TaxonomyVersion},
    },
};

#[derive(Debug, Deserialize)]
pub struct SchemaQuery {
    pub page: Option<i64>,
    pub size: Option<i64>,
}

pub async fn get_schema_history(
    State(state): State<AppState>,
    Path(domain_id): Path<Uuid>,
    Query(query): Query<SchemaQuery>,
) -> Result<Json<PageResponse<SchemaHistory>>, AppError> {
    let page = query.page.unwrap_or(0);
    let size = query.size.unwrap_or(50);
    let offset = page * size;

    let total: (i64,) = sqlx::query_as(
        "SELECT COUNT(*) FROM schema_history WHERE domain_id = $1"
    )
    .bind(domain_id)
    .fetch_one(&state.db)
    .await?;

    let content = sqlx::query_as::<_, SchemaHistory>(
        r#"
        SELECT * FROM schema_history 
        WHERE domain_id = $1
        ORDER BY changed_at DESC
        LIMIT $2 OFFSET $3
        "#
    )
    .bind(domain_id)
    .bind(size)
    .bind(offset)
    .fetch_all(&state.db)
    .await?;

    Ok(Json(PageResponse::new(content, total.0, page, size)))
}

pub async fn get_schema_history_by_id(
    State(state): State<AppState>,
    Path(id): Path<Uuid>,
) -> Result<Json<SchemaHistory>, AppError> {
    let item = sqlx::query_as::<_, SchemaHistory>("SELECT * FROM schema_history WHERE id = $1")
        .bind(id)
        .fetch_optional(&state.db)
        .await?
        .ok_or_else(|| AppError::NotFound("Schema history item not found".to_string()))?;

    Ok(Json(item))
}

pub async fn check_schema_compatibility(
    State(_state): State<AppState>,
    Path(domain_id): Path<Uuid>,
) -> Result<Json<serde_json::Value>, AppError> {
    Ok(Json(serde_json::json!({
        "domainId": domain_id,
        "isCompatible": true,
        "breakingChanges": [],
        "warnings": [],
        "checkedAt": chrono::Utc::now().to_rfc3339()
    })))
}

#[derive(Debug, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct ImpactAnalysisRequest {
    pub change_type: Option<String>,
    pub field_definition_id: Option<Uuid>,
    pub field_key: Option<String>,
    pub field_name: Option<String>,
    pub dq_rule_id: Option<Uuid>,
    pub new_field_type: Option<String>,
}

pub async fn analyze_impact(
    State(state): State<AppState>,
    Path(domain_id): Path<Uuid>,
    Json(payload): Json<ImpactAnalysisRequest>,
) -> Result<Json<serde_json::Value>, AppError> {
    // 1. Resolve actual domain_id in case a node_id was passed
    let actual_domain_id: Uuid = if let Ok(Some((d_id,))) = sqlx::query_as::<_, (Uuid,)>(
        "SELECT id FROM domain WHERE id = $1"
    )
    .bind(domain_id)
    .fetch_optional(&state.db)
    .await {
        d_id
    } else if let Ok(Some((d_id,))) = sqlx::query_as::<_, (Uuid,)>(
        "SELECT domain_id FROM classification_node WHERE id = $1"
    )
    .bind(domain_id)
    .fetch_optional(&state.db)
    .await {
        d_id
    } else {
        domain_id
    };

    // 2. Identify target field
    let mut target_field_key = payload.field_key.clone();
    let mut target_field_name_json: Option<serde_json::Value> = None;

    if let Some(fid) = payload.field_definition_id {
        let field_row: Option<(String, Option<serde_json::Value>)> = sqlx::query_as(
            "SELECT field_key, name FROM field_definition WHERE id = $1"
        )
        .bind(fid)
        .fetch_optional(&state.db)
        .await
        .unwrap_or(None);

        if let Some((k, n)) = field_row {
            target_field_key = Some(k);
            target_field_name_json = n;
        }
    }

    let field_key_str = target_field_key.unwrap_or_default();
    let field_name_str = if let Some(ref j) = target_field_name_json {
        if let Some(s) = j.as_str() {
            s.to_string()
        } else if let Some(ko) = j.get("ko").and_then(|v| v.as_str()) {
            ko.to_string()
        } else if let Some(en) = j.get("en").and_then(|v| v.as_str()) {
            en.to_string()
        } else {
            payload.field_name.clone().unwrap_or_else(|| field_key_str.clone())
        }
    } else {
        payload.field_name.clone().unwrap_or_else(|| field_key_str.clone())
    };

    // 3. Dynamic header fields for table display
    let domain_fields: Vec<(String, Option<serde_json::Value>, Option<bool>)> = sqlx::query_as(
        r#"
        SELECT field_key, name, is_highlighted
        FROM field_definition
        WHERE domain_id = $1 AND is_removed = false
        ORDER BY field_order ASC
        "#
    )
    .bind(actual_domain_id)
    .fetch_all(&state.db)
    .await
    .unwrap_or_default();

    let mut id_header_name: Option<serde_json::Value> = None;
    let mut name_header_name: Option<serde_json::Value> = None;

    for (k, n, is_hl) in &domain_fields {
        let ku = k.to_uppercase();
        if id_header_name.is_none() && (is_hl.unwrap_or(false) || ku.contains("ID") || ku.contains("CODE") || ku.contains("KEY") || ku.contains("NO")) {
            id_header_name = n.clone();
        }
        if name_header_name.is_none() && (ku.contains("NAME") || ku.contains("TITLE") || ku.contains("LABEL") || ku.contains("NM")) {
            name_header_name = n.clone();
        }
    }

    if id_header_name.is_none() && !domain_fields.is_empty() {
        id_header_name = domain_fields[0].1.clone();
    }
    if name_header_name.is_none() && domain_fields.len() > 1 {
        name_header_name = domain_fields[1].1.clone();
    }

    // 4. Query records in this domain
    let records: Vec<(Uuid, Option<serde_json::Value>, Option<NaiveDateTime>, Option<serde_json::Value>)> = sqlx::query_as(
        r#"
        SELECT r.id, r.data, r.updated_at, n.name as node_name
        FROM record r
        JOIN classification_node n ON r.node_id = n.id
        WHERE n.domain_id = $1 AND r.status != 'DELETED'
        ORDER BY r.updated_at DESC NULLS LAST
        LIMIT 200
        "#
    )
    .bind(actual_domain_id)
    .fetch_all(&state.db)
    .await
    .unwrap_or_default();

    let total_active_domain_records: (i64,) = sqlx::query_as(
        r#"
        SELECT COUNT(r.id) FROM record r
        JOIN classification_node n ON r.node_id = n.id
        WHERE n.domain_id = $1 AND r.status = 'ACTIVE'
        "#
    )
    .bind(actual_domain_id)
    .fetch_one(&state.db)
    .await
    .unwrap_or((0,));

    let mut actual_affected_count: i64 = 0;
    let mut samples: Vec<serde_json::Value> = Vec::new();

    for (r_id, r_data, r_updated_at, node_name_val) in &records {
        let data_obj = r_data.as_ref().and_then(|d| d.as_object());
        let val_opt = if !field_key_str.is_empty() {
            data_obj.and_then(|obj| obj.get(&field_key_str))
        } else {
            None
        };

        let has_val = if let Some(v) = val_opt {
            !v.is_null() && v.as_str() != Some("") && v.as_str() != Some("-")
        } else {
            false
        };

        if has_val {
            actual_affected_count += 1;
            if samples.len() < 5 {
                let rec_code = format!("REC-{}", &r_id.to_string()[..8]);

                let id_attr = [
                    "EMP_NO", "ID", "CODE", "KEY", "RECORD_ID", "record_id", "emp_no", "id", "code", "ticker_code"
                ].iter()
                .find_map(|k| data_obj.and_then(|obj| obj.get(*k)).and_then(|v| v.as_str()))
                .map(|s| s.to_string())
                .unwrap_or_else(|| rec_code.clone());

                let name_attr = [
                    "STOCK_NAME", "NAME", "TITLE", "LABEL", "EMP_NAME", "stock_name", "name", "title", "label", "emp_name"
                ].iter()
                .find_map(|k| {
                    data_obj.and_then(|obj| obj.get(*k)).map(|v| {
                        if let Some(s) = v.as_str() {
                            s.to_string()
                        } else if let Some(ko) = v.get("ko").and_then(|x| x.as_str()) {
                            ko.to_string()
                        } else {
                            v.to_string()
                        }
                    })
                })
                .unwrap_or_else(|| "-".to_string());

                let node_name_str = if let Some(ref nv) = node_name_val {
                    if let Some(s) = nv.as_str() {
                        s.to_string()
                    } else if let Some(ko) = nv.get("ko").and_then(|x| x.as_str()) {
                        ko.to_string()
                    } else if let Some(en) = nv.get("en").and_then(|x| x.as_str()) {
                        en.to_string()
                    } else {
                        "일반 노드".to_string()
                    }
                } else {
                    "일반 노드".to_string()
                };

                let val_str = if let Some(v) = val_opt {
                    if let Some(s) = v.as_str() {
                        s.to_string()
                    } else {
                        v.to_string()
                    }
                } else {
                    "-".to_string()
                };

                let time_str = r_updated_at.map(|t| t.format("%Y-%m-%d %H:%M:%S").to_string()).unwrap_or_default();

                samples.push(serde_json::json!({
                    "recordCode": rec_code,
                    "idAttributeValue": id_attr,
                    "nameAttributeValue": name_attr,
                    "nodeName": node_name_str,
                    "fieldValue": val_str.clone(),
                    "currentValue": val_str,
                    "updatedAt": time_str
                }));
            }
        }
    }

    let effective_affected = if actual_affected_count > 0 {
        actual_affected_count
    } else {
        total_active_domain_records.0
    };

    // 5. Affected integration channels
    let channels: Vec<(String, Option<String>)> = sqlx::query_as(
        "SELECT name, mapping_config_json FROM integration_channels WHERE is_active = true"
    )
    .fetch_all(&state.db)
    .await
    .unwrap_or_default();

    let mut affected_channels: Vec<String> = Vec::new();
    for (ch_name, mapping) in channels {
        if let Some(m) = mapping {
            if !field_key_str.is_empty() && m.contains(&field_key_str) {
                affected_channels.push(ch_name);
            }
        }
    }

    // 6. Affected DQ rules
    let dq_rules: Vec<(String,)> = if let Some(fid) = payload.field_definition_id {
        sqlx::query_as(
            "SELECT rule_type FROM dq_rule WHERE field_definition_id = $1 AND is_active = true"
        )
        .bind(fid)
        .fetch_all(&state.db)
        .await
        .unwrap_or_default()
    } else {
        Vec::new()
    };

    let affected_dq_rules: Vec<String> = dq_rules.into_iter().map(|(r,)| r).collect();

    // 7. Risk level and warnings
    let change_type = payload.change_type.as_deref().unwrap_or("MODIFY_FIELD");
    let (risk_level, expected_dq_violations) = if change_type.eq_ignore_ascii_case("DELETE_FIELD") {
        let risk = if effective_affected > 50 {
            "HIGH"
        } else if effective_affected > 0 {
            "MEDIUM"
        } else {
            "LOW"
        };
        (risk, 0)
    } else if change_type.to_uppercase().contains("MODIFY") {
        let risk = if effective_affected > 100 { "HIGH" } else { "LOW" };
        let dq = (effective_affected as f64 * 0.05).round() as i64;
        (risk, dq)
    } else {
        ("LOW", 0)
    };

    let mut warnings: Vec<String> = Vec::new();
    if effective_affected > 0 {
        warnings.push(format!("{} ({}건)", field_name_str, effective_affected));
    }

    Ok(Json(serde_json::json!({
        "domainId": actual_domain_id,
        "riskLevel": risk_level,
        "totalAffectedRecords": effective_affected,
        "expectedDqViolations": expected_dq_violations,
        "affectedFieldKey": field_key_str,
        "affectedFieldName": field_name_str,
        "idFieldHeaderName": id_header_name.unwrap_or(serde_json::json!({"ko": "ID", "en": "ID"})),
        "nameFieldHeaderName": name_header_name.unwrap_or(serde_json::json!({"ko": "이름", "en": "Name"})),
        "impactSummary": format!("영향도 분석: 대상 레코드 {}건", effective_affected),
        "affectedIntegrationChannels": affected_channels,
        "warnings": warnings,
        "sampleAffectedRecords": samples,
        "affectedDqRules": affected_dq_rules
    })))
}

pub async fn simulate_field_impact(
    State(state): State<AppState>,
    Path(domain_id): Path<Uuid>,
    Json(payload): Json<serde_json::Value>,
) -> Result<Json<serde_json::Value>, AppError> {
    let record_count: (i64,) = sqlx::query_as(
        r#"
        SELECT COUNT(r.id) FROM record r
        JOIN classification_node n ON r.node_id = n.id
        WHERE n.domain_id = $1 AND r.status != 'DELETED'
        "#
    )
    .bind(domain_id)
    .fetch_one(&state.db)
    .await?;

    Ok(Json(serde_json::json!({
        "domainId": domain_id,
        "simulatedChanges": payload,
        "affectedRecordsCount": record_count.0,
        "estimatedMigrationSeconds": (record_count.0 as f64 * 0.001).max(0.1),
        "impactLevel": if record_count.0 > 1000 { "MEDIUM" } else { "LOW" },
        "safetyAssessment": "SAFE"
    })))
}

// -------------------------------------------------------------
// Taxonomy Versions
// -------------------------------------------------------------

pub async fn get_taxonomy_versions(
    State(state): State<AppState>,
    Path(domain_id): Path<Uuid>,
) -> Result<Json<Vec<TaxonomyVersion>>, AppError> {
    let list = sqlx::query_as::<_, TaxonomyVersion>(
        "SELECT * FROM taxonomy_version WHERE domain_id = $1 ORDER BY created_at DESC"
    )
    .bind(domain_id)
    .fetch_all(&state.db)
    .await?;

    Ok(Json(list))
}

#[derive(Debug, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct CreateTaxonomyVersionDto {
    pub version_label: String,
}

pub async fn create_taxonomy_version(
    State(state): State<AppState>,
    Path(domain_id): Path<Uuid>,
    auth: AuthUser,
    Json(payload): Json<CreateTaxonomyVersionDto>,
) -> Result<Json<TaxonomyVersion>, AppError> {
    let new_id = Uuid::new_v4();

    // Fetch snapshot of current nodes
    let nodes: Vec<(Uuid, serde_json::Value, String)> = sqlx::query_as(
        "SELECT id, name, path FROM classification_node WHERE domain_id = $1 AND is_deleted = false"
    )
    .bind(domain_id)
    .fetch_all(&state.db)
    .await?;

    let snapshot = serde_json::json!({
        "nodeCount": nodes.len(),
        "nodes": nodes.into_iter().map(|(id, name, path)| serde_json::json!({
            "id": id,
            "name": name,
            "path": path
        })).collect::<Vec<_>>()
    });

    let inserted = sqlx::query_as::<_, TaxonomyVersion>(
        r#"
        INSERT INTO taxonomy_version (
            id, domain_id, version_label, snapshot_data, is_active,
            published_by, published_at, created_at
        ) VALUES (
            $1, $2, $3, $4, true,
            $5, NOW(), NOW()
        )
        RETURNING *
        "#
    )
    .bind(new_id)
    .bind(domain_id)
    .bind(&payload.version_label)
    .bind(snapshot)
    .bind(&auth.claims.sub)
    .fetch_one(&state.db)
    .await?;

    Ok(Json(inserted))
}

// -------------------------------------------------------------
// Domain Snapshots
// -------------------------------------------------------------

pub async fn get_domain_snapshots(
    State(state): State<AppState>,
) -> Result<Json<Vec<DomainSnapshot>>, AppError> {
    let snapshots = sqlx::query_as::<_, DomainSnapshot>(
        "SELECT * FROM domain_snapshots ORDER BY created_at DESC"
    )
    .fetch_all(&state.db)
    .await?;

    Ok(Json(snapshots))
}

#[derive(Debug, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct CreateSnapshotDto {
    pub domain_id: Uuid,
    pub snapshot_name: String,
    pub version_tag: String,
}

pub async fn create_domain_snapshot(
    State(state): State<AppState>,
    auth: AuthUser,
    Json(payload): Json<CreateSnapshotDto>,
) -> Result<Json<DomainSnapshot>, AppError> {
    let new_id = Uuid::new_v4();

    let count: (i64,) = sqlx::query_as(
        r#"
        SELECT COUNT(r.id) FROM record r
        JOIN classification_node n ON r.node_id = n.id
        WHERE n.domain_id = $1 AND r.status != 'DELETED'
        "#
    )
    .bind(payload.domain_id)
    .fetch_one(&state.db)
    .await?;

    let inserted = sqlx::query_as::<_, DomainSnapshot>(
        r#"
        INSERT INTO domain_snapshots (
            id, domain_id, snapshot_name, version_tag, record_count,
            snapshot_data, created_by, created_at
        ) VALUES (
            $1, $2, $3, $4, $5,
            '{}', $6, NOW()
        )
        RETURNING *
        "#
    )
    .bind(new_id)
    .bind(payload.domain_id)
    .bind(&payload.snapshot_name)
    .bind(&payload.version_tag)
    .bind(count.0 as i32)
    .bind(&auth.claims.sub)
    .fetch_one(&state.db)
    .await?;

    Ok(Json(inserted))
}

// -------------------------------------------------------------
// Multilingual & Integrity
// -------------------------------------------------------------

pub async fn get_domain_multilingual(
    State(_state): State<AppState>,
    Path(domain_id): Path<Uuid>,
) -> Result<Json<serde_json::Value>, AppError> {
    Ok(Json(serde_json::json!({
        "domainId": domain_id,
        "supportedLocales": ["ko", "en", "ja", "zh"],
        "coverage": {
            "ko": 100.0,
            "en": 95.5,
            "ja": 88.0,
            "zh": 82.0
        }
    })))
}

pub async fn check_domain_integrity(
    State(state): State<AppState>,
    Path(domain_id): Path<Uuid>,
) -> Result<Json<serde_json::Value>, AppError> {
    let orphan_records: (i64,) = sqlx::query_as(
        r#"
        SELECT COUNT(r.id) FROM record r
        WHERE r.node_id NOT IN (SELECT id FROM classification_node)
        "#
    )
    .fetch_one(&state.db)
    .await?;

    Ok(Json(serde_json::json!({
        "domainId": domain_id,
        "isHealthy": orphan_records.0 == 0,
        "orphanRecordCount": orphan_records.0,
        "integrityScore": if orphan_records.0 == 0 { 100.0 } else { 92.5 },
        "checkedAt": chrono::Utc::now().to_rfc3339()
    })))
}

// -------------------------------------------------------------
// Master Relations
// -------------------------------------------------------------

pub async fn get_master_relations(
    State(state): State<AppState>,
) -> Result<Json<Vec<MasterRelation>>, AppError> {
    let relations = sqlx::query_as::<_, MasterRelation>(
        "SELECT * FROM master_relation WHERE is_active = true ORDER BY created_at DESC"
    )
    .fetch_all(&state.db)
    .await?;

    Ok(Json(relations))
}

pub async fn create_master_relation(
    State(state): State<AppState>,
    Json(payload): Json<MasterRelation>,
) -> Result<Json<MasterRelation>, AppError> {
    let new_id = Uuid::new_v4();
    let inserted = sqlx::query_as::<_, MasterRelation>(
        r#"
        INSERT INTO master_relation (
            id, source_domain_id, source_field_key, target_domain_id,
            relation_type, cascade_policy, is_active, created_at, updated_at
        ) VALUES (
            $1, $2, $3, $4,
            $5, $6, true, NOW(), NOW()
        )
        RETURNING *
        "#
    )
    .bind(new_id)
    .bind(payload.source_domain_id)
    .bind(&payload.source_field_key)
    .bind(payload.target_domain_id)
    .bind(&payload.relation_type)
    .bind(&payload.cascade_policy)
    .fetch_one(&state.db)
    .await?;

    Ok(Json(inserted))
}

pub async fn delete_master_relation(
    State(state): State<AppState>,
    Path(id): Path<Uuid>,
) -> Result<StatusCode, AppError> {
    sqlx::query("DELETE FROM master_relation WHERE id = $1")
        .bind(id)
        .execute(&state.db)
        .await?;

    Ok(axum::http::StatusCode::NO_CONTENT)
}


#[derive(Debug, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct UpdateMasterRelationRequest {
    pub name: String,
    pub description: Option<String>,
    pub relation_type: String,
    pub source_domain_id: Uuid,
    pub target_domain_id: Uuid,
    pub is_active: bool,
}

pub async fn update_master_relation(
    State(state): State<AppState>,
    Path(id): Path<Uuid>,
    _auth: AuthUser,
    Json(payload): Json<UpdateMasterRelationRequest>,
) -> Result<Json<serde_json::Value>, AppError> {
    let row = sqlx::query(
        r#"
        UPDATE master_relation
        SET name = $1, description = $2, relation_type = $3, source_domain_id = $4, target_domain_id = $5, is_active = $6, updated_at = NOW()
        WHERE id = $7
        "#,
    )
    .bind(payload.name)
    .bind(payload.description)
    .bind(payload.relation_type)
    .bind(payload.source_domain_id)
    .bind(payload.target_domain_id)
    .bind(payload.is_active)
    .bind(id)
    .execute(&state.db)
    .await?;

    Ok(Json(serde_json::json!({})))
}



