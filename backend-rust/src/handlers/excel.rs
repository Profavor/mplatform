use crate::error::AppResult;
use crate::middleware::auth::AuthUser;
use crate::models::excel::*;
use crate::state::AppState;
use axum::http::StatusCode;
use axum::{
    body::Body,
    extract::{Path, Query, State},
    http::header,
    response::{IntoResponse, Response},
    Json,
};
use uuid::Uuid;

pub async fn download_template(
    State(state): State<AppState>,
    Path(domain_id): Path<Uuid>,
    Query(params): Query<ExportTemplateQuery>,
    _auth: AuthUser,
) -> AppResult<impl IntoResponse> {
    let bytes = state
        .excel_service
        .generate_template(domain_id, params.node_id, &params.lang)
        .await?;

    let filename = format!("template_{}.csv", &domain_id.to_string()[..8]);

    let response = Response::builder()
        .header(header::CONTENT_TYPE, "text/csv; charset=utf-8")
        .header(
            header::CONTENT_DISPOSITION,
            format!("attachment; filename=\"{filename}\""),
        )
        .body(Body::from(bytes))
        .unwrap();

    Ok(response)
}

pub async fn export_records(
    State(state): State<AppState>,
    Path(domain_id): Path<Uuid>,
    Query(params): Query<ExportRecordsQuery>,
    _auth: AuthUser,
) -> AppResult<impl IntoResponse> {
    let format = params.format.as_deref().unwrap_or("csv").to_lowercase();

    if format == "xlsx" {
        let bytes = state
            .excel_service
            .export_records_xlsx(domain_id, params.node_id, &params.lang)
            .await?;

        let filename = format!("records_{}.xlsx", &domain_id.to_string()[..8]);

        let response = Response::builder()
            .header(
                header::CONTENT_TYPE,
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            )
            .header(
                header::CONTENT_DISPOSITION,
                format!("attachment; filename=\"{filename}\""),
            )
            .body(Body::from(bytes))
            .unwrap();

        Ok(response)
    } else {
        let bytes = state
            .excel_service
            .export_records_csv(domain_id, params.node_id, &params.lang)
            .await?;

        let filename = format!("records_{}.csv", &domain_id.to_string()[..8]);

        let response = Response::builder()
            .header(header::CONTENT_TYPE, "text/csv; charset=utf-8")
            .header(
                header::CONTENT_DISPOSITION,
                format!("attachment; filename=\"{filename}\""),
            )
            .body(Body::from(bytes))
            .unwrap();

        Ok(response)
    }
}

pub async fn start_import_job(
    State(state): State<AppState>,
    auth: AuthUser,
    Json(req): Json<BulkImportRequest>,
) -> AppResult<impl IntoResponse> {
    let progress = state
        .excel_service
        .start_bulk_import_job(req, &auth.user_id)
        .await?;

    Ok(Json(progress))
}

pub async fn get_job_progress(
    State(state): State<AppState>,
    Path(job_id): Path<Uuid>,
    _auth: AuthUser,
) -> AppResult<impl IntoResponse> {
    let progress = state.excel_service.get_job_progress(job_id).await?;
    Ok(Json(progress))
}
