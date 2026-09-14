use crate::error::AppResult;
use crate::models::dashboard::*;
use crate::repositories::dashboard_repo::DashboardRepository;
use sqlx::PgPool;
use uuid::Uuid;

#[derive(Clone)]
pub struct DashboardService {
    pool: PgPool,
}

impl DashboardService {
    pub fn new(pool: PgPool) -> Self {
        Self { pool }
    }

    pub async fn get_stats(&self) -> AppResult<DashboardStats> {
        let stats = DashboardRepository::get_stats(&self.pool).await?;
        Ok(stats)
    }

    pub async fn get_approval_trends(&self) -> AppResult<Vec<TrendItem>> {
        let trends = DashboardRepository::get_approval_trends(&self.pool).await?;
        Ok(trends)
    }

    pub async fn get_dq_trends(&self) -> AppResult<Vec<TrendItem>> {
        let trends = DashboardRepository::get_dq_trends(&self.pool).await?;
        Ok(trends)
    }

    pub async fn get_domain_distribution(&self) -> AppResult<Vec<DomainDistributionItem>> {
        let items = DashboardRepository::get_domain_distribution(&self.pool).await?;
        Ok(items)
    }

    pub async fn get_dq_severity_distribution(&self) -> AppResult<Vec<DqSeverityItem>> {
        let items = DashboardRepository::get_dq_severity_distribution(&self.pool).await?;
        Ok(items)
    }

    pub async fn get_lease_summary(&self, org_id: Option<Uuid>) -> AppResult<LeaseSummaryDto> {
        let summary = DashboardRepository::get_lease_summary(&self.pool, org_id).await?;
        Ok(summary)
    }
}
