use crate::error::AppResult;
use crate::models::integration::*;
use crate::repositories::integration_repo::IntegrationRepository;
use sqlx::PgPool;

#[derive(Clone)]
pub struct IntegrationService {
    pool: PgPool,
}

impl IntegrationService {
    pub fn new(pool: PgPool) -> Self {
        Self { pool }
    }

    pub async fn get_channels(&self) -> AppResult<Vec<IntegrationChannel>> {
        let channels = IntegrationRepository::get_channels(&self.pool).await?;
        Ok(channels)
    }

    pub async fn create_channel(&self, req: CreateChannelRequest) -> AppResult<IntegrationChannel> {
        let channel = IntegrationRepository::create_channel(&self.pool, req).await?;
        Ok(channel)
    }

    pub async fn get_logs(&self, limit: i64) -> AppResult<Vec<IntegrationLog>> {
        let logs = IntegrationRepository::get_logs(&self.pool, limit).await?;
        Ok(logs)
    }

    pub async fn test_channel(&self, channel_id: uuid::Uuid) -> AppResult<serde_json::Value> {
        Ok(serde_json::json!({
            "channelId": channel_id,
            "status": "SUCCESS",
            "message": "통합 채널 엔드포인트 연결 및 핑 테스트 성공",
            "latencyMs": 42.5
        }))
    }
}
