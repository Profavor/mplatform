use crate::batch::approval_escalation::ApprovalEscalationJob;
use crate::batch::chat_cleanup::ChatCleanupJob;
use crate::batch::dq_scan::DqScanJob;
use crate::batch::integration_retry::IntegrationRetryJob;
use crate::batch::lock::AdvisoryLock;
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

        tracing::info!("✅ All MDM Batch Schedulers initialized successfully!");
    }
}
