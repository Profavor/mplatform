use crate::error::{AppError, AppResult};
use crate::models::excel::*;
use crate::models::record::CreateRecordRequest;
use crate::repositories::record_repo::RecordRepository;
use chrono::Utc;
use rust_xlsxwriter::Workbook;
use sqlx::{PgPool, Row};
use std::collections::HashMap;
use uuid::Uuid;

#[derive(Clone)]
pub struct ExcelService {
    pool: PgPool,
}

#[derive(Debug, sqlx::FromRow)]
struct FieldInfo {
    field_key: String,
    name: serde_json::Value,
    required: bool,
    r#type: String,
}

impl ExcelService {
    pub fn new(pool: PgPool) -> Self {
        Self { pool }
    }

    async fn get_domain_fields(
        &self,
        domain_id: Uuid,
        node_id: Option<Uuid>,
    ) -> AppResult<Vec<FieldInfo>> {
        let fields = if let Some(nid) = node_id {
            sqlx::query_as::<_, FieldInfo>(
                r#"
                SELECT field_key, name, required, type
                FROM field_definition
                WHERE (domain_id = $1 OR defined_at_node_id = $2) AND is_removed = false
                ORDER BY field_order ASC
                "#,
            )
            .bind(domain_id)
            .bind(nid)
            .fetch_all(&self.pool)
            .await?
        } else {
            sqlx::query_as::<_, FieldInfo>(
                r#"
                SELECT field_key, name, required, type
                FROM field_definition
                WHERE domain_id = $1 AND is_removed = false
                ORDER BY field_order ASC
                "#,
            )
            .bind(domain_id)
            .fetch_all(&self.pool)
            .await?
        };

        Ok(fields)
    }

    pub async fn generate_template(
        &self,
        domain_id: Uuid,
        node_id: Option<Uuid>,
        lang: &str,
    ) -> AppResult<Vec<u8>> {
        let fields = self.get_domain_fields(domain_id, node_id).await?;

        let mut out = String::from("\u{FEFF}"); // UTF-8 BOM

        // Row 1: Field display names
        let row1: Vec<String> = fields
            .iter()
            .map(|f| {
                let name = f
                    .name
                    .get(lang)
                    .and_then(|v| v.as_str())
                    .unwrap_or(&f.field_key);
                let label = if f.required {
                    format!("{}*", name)
                } else {
                    name.to_string()
                };
                Self::escape_csv(&label)
            })
            .collect();
        out.push_str(&row1.join(","));
        out.push_str("\r\n");

        // Row 2: Field Keys
        let row2: Vec<String> = fields
            .iter()
            .map(|f| Self::escape_csv(&f.field_key))
            .collect();
        out.push_str(&row2.join(","));
        out.push_str("\r\n");

        // Row 3: Samples
        let row3: Vec<String> = fields
            .iter()
            .map(|f| {
                let sample = match f.r#type.to_uppercase().as_str() {
                    "NUMBER" => "1000",
                    "DATE" => "2026-08-15",
                    "BOOLEAN" => "true",
                    "ENUM" => "OPTION_A",
                    _ => "예시 텍스트",
                };
                Self::escape_csv(sample)
            })
            .collect();
        out.push_str(&row3.join(","));
        out.push_str("\r\n");

        Ok(out.into_bytes())
    }

    pub async fn export_records_csv(
        &self,
        domain_id: Uuid,
        node_id: Option<Uuid>,
        lang: &str,
    ) -> AppResult<Vec<u8>> {
        let fields = self.get_domain_fields(domain_id, node_id).await?;

        let rows = if let Some(nid) = node_id {
            sqlx::query("SELECT data FROM record WHERE node_id = $1 ORDER BY created_at DESC")
                .bind(nid)
                .fetch_all(&self.pool)
                .await?
        } else {
            sqlx::query(
                r#"
                SELECT r.data
                FROM record r
                JOIN classification_node n ON r.node_id = n.id
                JOIN classification_axis a ON n.axis_id = a.id
                WHERE a.domain_id = $1
                ORDER BY r.created_at DESC
                "#,
            )
            .bind(domain_id)
            .fetch_all(&self.pool)
            .await?
        };

        let mut out = String::from("\u{FEFF}");

        // Header Row
        let header: Vec<String> = fields
            .iter()
            .map(|f| {
                let name = f
                    .name
                    .get(lang)
                    .and_then(|v| v.as_str())
                    .unwrap_or(&f.field_key);
                Self::escape_csv(name)
            })
            .collect();
        out.push_str(&header.join(","));
        out.push_str("\r\n");

        // Data Rows
        for r in rows {
            let data_val: serde_json::Value = r.get("data");
            let row_values: Vec<String> = fields
                .iter()
                .map(|f| {
                    let v = data_val.get(&f.field_key);
                    let val_str = match v {
                        Some(serde_json::Value::String(s)) => s.clone(),
                        Some(serde_json::Value::Number(n)) => n.to_string(),
                        Some(serde_json::Value::Bool(b)) => b.to_string(),
                        Some(serde_json::Value::Object(o)) => o
                            .get(lang)
                            .and_then(|x| x.as_str())
                            .unwrap_or("")
                            .to_string(),
                        _ => "".to_string(),
                    };
                    Self::escape_csv(&val_str)
                })
                .collect();
            out.push_str(&row_values.join(","));
            out.push_str("\r\n");
        }

        Ok(out.into_bytes())
    }

    pub async fn export_records_xlsx(
        &self,
        domain_id: Uuid,
        node_id: Option<Uuid>,
        lang: &str,
    ) -> AppResult<Vec<u8>> {
        let fields = self.get_domain_fields(domain_id, node_id).await?;

        let rows = if let Some(nid) = node_id {
            sqlx::query("SELECT data FROM record WHERE node_id = $1 ORDER BY created_at DESC")
                .bind(nid)
                .fetch_all(&self.pool)
                .await?
        } else {
            sqlx::query(
                r#"
                SELECT r.data
                FROM record r
                JOIN classification_node n ON r.node_id = n.id
                JOIN classification_axis a ON n.axis_id = a.id
                WHERE a.domain_id = $1
                ORDER BY r.created_at DESC
                "#,
            )
            .bind(domain_id)
            .fetch_all(&self.pool)
            .await?
        };

        let mut workbook = Workbook::new();
        let worksheet = workbook.add_worksheet();

        // Write Headers
        for (col, f) in fields.iter().enumerate() {
            let name = f
                .name
                .get(lang)
                .and_then(|v| v.as_str())
                .unwrap_or(&f.field_key);
            worksheet
                .write_string(0, col as u16, name)
                .map_err(|e| AppError::Internal(e.to_string()))?;
        }

        // Write Rows
        for (row_idx, r) in rows.iter().enumerate() {
            let data_val: serde_json::Value = r.get("data");
            for (col, f) in fields.iter().enumerate() {
                let v = data_val.get(&f.field_key);
                let val_str = match v {
                    Some(serde_json::Value::String(s)) => s.clone(),
                    Some(serde_json::Value::Number(n)) => n.to_string(),
                    Some(serde_json::Value::Bool(b)) => b.to_string(),
                    Some(serde_json::Value::Object(o)) => o
                        .get(lang)
                        .and_then(|x| x.as_str())
                        .unwrap_or("")
                        .to_string(),
                    _ => "".to_string(),
                };
                worksheet
                    .write_string((row_idx + 1) as u32, col as u16, &val_str)
                    .map_err(|e| AppError::Internal(e.to_string()))?;
            }
        }

        let buf = workbook
            .save_to_buffer()
            .map_err(|e| AppError::Internal(e.to_string()))?;
        Ok(buf)
    }

    pub async fn start_bulk_import_job(
        &self,
        req: BulkImportRequest,
        created_by: &str,
    ) -> AppResult<BulkImportProgress> {
        let job_id = Uuid::new_v4();
        let total_rows = req.rows.len() as i32;
        let file_name = req
            .file_name
            .unwrap_or_else(|| "import_data.csv".to_string());
        let now = Utc::now();

        let mut success_count = 0;
        let mut error_count = 0;
        let mut error_details = Vec::new();

        let effective_node_id: Uuid = match req.node_id {
            Some(nid) => nid,
            None => {
                let opt_nid: Option<Uuid> = sqlx::query_scalar(
                    "SELECT id FROM classification_node WHERE axis_id IN (SELECT id FROM classification_axis WHERE domain_id = $1) LIMIT 1"
                )
                .bind(req.domain_id)
                .fetch_optional(&self.pool)
                .await?;
                opt_nid.unwrap_or_else(Uuid::nil)
            }
        };

        for (i, row) in req.rows.iter().enumerate() {
            let create_req = CreateRecordRequest {
                node_id: Some(effective_node_id),
                data: row.clone(),
                source_system: Some("BULK_IMPORT".to_string()),
            };

            match crate::services::record_service::RecordService::create_record(
                &self.pool, create_req, created_by,
            )
            .await
            {
                Ok(_) => {
                    success_count += 1;
                }
                Err(e) => {
                    error_count += 1;
                    error_details.push(BulkImportErrorDetail {
                        row_number: (i + 1) as i32,
                        record_key: None,
                        error_message: e.to_string(),
                    });
                }
            }
        }

        let status = if error_count == 0 {
            "COMPLETED"
        } else if success_count == 0 {
            "FAILED"
        } else {
            "PARTIAL_SUCCESS"
        };
        let progress_pct = 100.0;
        let err_json = serde_json::to_string(&error_details).unwrap_or_default();

        let _ = sqlx::query(
            r#"
            INSERT INTO bulk_import_job (
                id, domain_id, file_name, status, total_rows, processed_rows,
                success_count, error_count, error_details_json, created_by,
                created_at, completed_at
            )
            VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12)
            "#,
        )
        .bind(job_id)
        .bind(req.domain_id)
        .bind(&file_name)
        .bind(status)
        .bind(total_rows)
        .bind(total_rows)
        .bind(success_count)
        .bind(error_count)
        .bind(&err_json)
        .bind(created_by)
        .bind(now)
        .bind(now)
        .execute(&self.pool)
        .await;

        Ok(BulkImportProgress {
            job_id,
            domain_id: req.domain_id,
            file_name,
            status: status.to_string(),
            total_rows,
            processed_rows: total_rows,
            success_count,
            error_count,
            progress_percentage: progress_pct,
            error_details,
            created_at: Some(now.naive_utc()),
            completed_at: Some(now.naive_utc()),
        })
    }

    pub async fn get_job_progress(&self, job_id: Uuid) -> AppResult<BulkImportProgress> {
        let row = sqlx::query(
            r#"
            SELECT id, domain_id, file_name, status, total_rows, processed_rows,
                   success_count, error_count, error_details_json, created_at, completed_at
            FROM bulk_import_job
            WHERE id = $1
            "#,
        )
        .bind(job_id)
        .fetch_optional(&self.pool)
        .await?
        .ok_or_else(|| AppError::NotFound(format!("Job {job_id} not found")))?;

        let total_rows: i32 = row.get("total_rows");
        let processed_rows: i32 = row.get("processed_rows");
        let progress_percentage = if total_rows > 0 {
            (processed_rows as f64 / total_rows as f64) * 100.0
        } else {
            100.0
        };

        let err_json: Option<String> = row.get("error_details_json");
        let error_details: Vec<BulkImportErrorDetail> = err_json
            .and_then(|s| serde_json::from_str(&s).ok())
            .unwrap_or_default();

        Ok(BulkImportProgress {
            job_id: row.get("id"),
            domain_id: row.get("domain_id"),
            file_name: row.get("file_name"),
            status: row.get("status"),
            total_rows,
            processed_rows,
            success_count: row.get("success_count"),
            error_count: row.get("error_count"),
            progress_percentage,
            error_details,
            created_at: row.get("created_at"),
            completed_at: row.get("completed_at"),
        })
    }

    fn escape_csv(value: &str) -> String {
        let escaped = value.replace('"', "\"\"");
        if escaped.contains(',')
            || escaped.contains('\n')
            || escaped.contains('\r')
            || escaped.contains('"')
        {
            format!("\"{}\"", escaped)
        } else {
            escaped
        }
    }
}
