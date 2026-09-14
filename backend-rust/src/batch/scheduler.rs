use crate::batch::approval_escalation::ApprovalEscalationJob;
use crate::batch::chat_cleanup::ChatCleanupJob;
use crate::batch::dq_scan::DqScanJob;
use crate::batch::integration_retry::IntegrationRetryJob;
use crate::batch::lock::AdvisoryLock;
use crate::batch::stock_ingestion::StockDataIngestionJob;
use chrono::{Datelike, Timelike};
use sqlx::PgPool;
use std::time::Duration;
use tokio::time::interval;

pub struct BatchScheduler;

impl BatchScheduler {
    /// Spawns background worker loops for all periodic batch jobs.
    pub fn start(pool: PgPool) {
        tracing::info!("⏰ Starting MDM Asynchronous Batch Schedulers...");

        // 1. Approval SLA Escalation Scheduler (every 10 minutes)
        {
            let pool = pool.clone();
            tokio::spawn(async move {
                let mut ticker = interval(Duration::from_secs(600));
                // Tick once immediately after 10s initial delay
                tokio::time::sleep(Duration::from_secs(10)).await;
                loop {
                    ticker.tick().await;
                    if AdvisoryLock::try_acquire(&pool, ApprovalEscalationJob::LOCK_ID).await {
                        let _ = ApprovalEscalationJob::run(&pool).await;
                        AdvisoryLock::release(&pool, ApprovalEscalationJob::LOCK_ID).await;
                    }
                }
            });
        }

        // 2. Integration DLQ Retry Scheduler (every 60 seconds)
        {
            let pool = pool.clone();
            tokio::spawn(async move {
                let mut ticker = interval(Duration::from_secs(60));
                tokio::time::sleep(Duration::from_secs(15)).await;
                loop {
                    ticker.tick().await;
                    if AdvisoryLock::try_acquire(&pool, IntegrationRetryJob::LOCK_ID).await {
                        let _ = IntegrationRetryJob::run(&pool).await;
                        AdvisoryLock::release(&pool, IntegrationRetryJob::LOCK_ID).await;
                    }
                }
            });
        }

        // 3. Nightly Data Quality (DQ) Scan (every 24 hours)
        {
            let pool = pool.clone();
            tokio::spawn(async move {
                let mut ticker = interval(Duration::from_secs(86400));
                tokio::time::sleep(Duration::from_secs(60)).await;
                loop {
                    ticker.tick().await;
                    if AdvisoryLock::try_acquire(&pool, DqScanJob::LOCK_ID).await {
                        let _ = DqScanJob::run(&pool).await;
                        AdvisoryLock::release(&pool, DqScanJob::LOCK_ID).await;
                    }
                }
            });
        }

        // 4. Nightly Chat / Ephemeral Message Cleanup (every 24 hours)
        {
            let pool = pool.clone();
            tokio::spawn(async move {
                let mut ticker = interval(Duration::from_secs(86400));
                tokio::time::sleep(Duration::from_secs(120)).await;
                loop {
                    ticker.tick().await;
                    if AdvisoryLock::try_acquire(&pool, ChatCleanupJob::LOCK_ID).await {
                        let _ = ChatCleanupJob::run(&pool).await;
                        AdvisoryLock::release(&pool, ChatCleanupJob::LOCK_ID).await;
                    }
                }
            });
        }

        // 5. Stock Market Inbound Batch Scheduler (every weekday Mon-Fri at 16:00 KST)
        {
            let pool = pool.clone();
            tokio::spawn(async move {
                let mut ticker = interval(Duration::from_secs(30));
                // Initial short delay on startup
                tokio::time::sleep(Duration::from_secs(5)).await;
                loop {
                    ticker.tick().await;

                    // KST is UTC + 9 hours
                    let now_kst = chrono::Utc::now() + chrono::Duration::hours(9);
                    let weekday = now_kst.weekday().num_days_from_monday(); // 0 = Mon, 4 = Fri
                    let is_weekday = weekday <= 4;
                    let is_after_16 = now_kst.hour() >= 16;

                    if is_weekday && is_after_16 {
                        // Check if stock batch has already run today in DB (KST date)
                        let already_run: bool = sqlx::query_scalar(
                            r#"
                            SELECT EXISTS(
                                SELECT 1 FROM integration_logs 
                                WHERE event_type = 'SPRING_BATCH_STOCK_INGESTION' 
                                  AND created_at >= (NOW() AT TIME ZONE 'Asia/Seoul')::date
                            )
                            "#
                        )
                        .fetch_one(&pool)
                        .await
                        .unwrap_or(true);

                        if !already_run {
                            tracing::info!("⏰ Triggering daily scheduled stock batch ingestion for today (KST: {})...", now_kst.format("%Y-%m-%d %H:%M:%S"));
                            if AdvisoryLock::try_acquire(&pool, StockDataIngestionJob::LOCK_ID).await {
                                // Double check after acquiring lock
                                let already_run_locked: bool = sqlx::query_scalar(
                                    r#"
                                    SELECT EXISTS(
                                        SELECT 1 FROM integration_logs 
                                        WHERE event_type = 'SPRING_BATCH_STOCK_INGESTION' 
                                          AND created_at >= (NOW() AT TIME ZONE 'Asia/Seoul')::date
                                    )
                                    "#
                                )
                                .fetch_one(&pool)
                                .await
                                .unwrap_or(true);

                                if !already_run_locked {
                                    match StockDataIngestionJob::run_ingestion(&pool, Default::default(), "SCHEDULED_CRON").await {
                                        Ok(res) => {
                                            tracing::info!("✅ Scheduled stock ingestion completed successfully: {}", res.message);
                                        }
                                        Err(e) => {
                                            tracing::error!("❌ Scheduled stock ingestion error: {}", e);
                                        }
                                    }
                                }
                                AdvisoryLock::release(&pool, StockDataIngestionJob::LOCK_ID).await;
                            }
                        }
                    }
                }
            });
        }

        tracing::info!("✅ All MDM Batch Schedulers initialized successfully!");
    }
}
