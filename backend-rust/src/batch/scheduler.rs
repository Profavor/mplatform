use crate::batch::approval_escalation::ApprovalEscalationJob;
use crate::batch::chat_cleanup::ChatCleanupJob;
use crate::batch::dq_scan::DqScanJob;
use crate::batch::integration_retry::IntegrationRetryJob;
use crate::batch::lock::AdvisoryLock;
use crate::batch::stock_ingestion::StockDataIngestionJob;
use chrono::Timelike;
use cron::Schedule;
use sqlx::PgPool;
use std::str::FromStr;
use std::time::Duration;
use tokio::time::interval;
use uuid::Uuid;

fn normalize_cron(expr: &str) -> String {
    let parts: Vec<&str> = expr.split_whitespace().collect();
    match parts.len() {
        5 => format!("0 {} *", parts.join(" ")),
        6 => format!("{} *", parts.join(" ")),
        7 => parts.join(" "),
        _ => expr.to_string(),
    }
}

fn is_cron_due(cron_str: &str, now_kst: chrono::DateTime<chrono::FixedOffset>) -> bool {
    let normalized = normalize_cron(cron_str.trim());
    let schedule = match Schedule::from_str(&normalized) {
        Ok(s) => s,
        Err(e) => {
            tracing::warn!("⚠️ Invalid cron expression '{}': {}", cron_str, e);
            return false;
        }
    };

    let start_of_minute = match now_kst.with_second(0).and_then(|t| t.with_nanosecond(0)) {
        Some(s) => s,
        None => return false,
    };
    let prev_sec = start_of_minute - chrono::Duration::seconds(1);

    if let Some(next_fire) = schedule.after(&prev_sec).next() {
        next_fire >= start_of_minute && next_fire < start_of_minute + chrono::Duration::seconds(60)
    } else {
        false
    }
}

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

        // 5. Dynamic Inbound Batch Scheduler (evaluates DB-configured cron expressions dynamically)
        {
            let pool = pool.clone();
            tokio::spawn(async move {
                let mut ticker = interval(Duration::from_secs(30));
                // Initial short delay on startup
                tokio::time::sleep(Duration::from_secs(5)).await;
                loop {
                    ticker.tick().await;

                    // KST is UTC + 9 hours
                    let kst_offset = chrono::FixedOffset::east_opt(9 * 3600).unwrap();
                    let now_kst = chrono::Utc::now().with_timezone(&kst_offset);

                    // Fetch active inbound batch channels dynamically from DB
                    let active_batch_channels: Vec<(Uuid, String, Option<String>)> = sqlx::query_as(
                        r#"
                        SELECT id, channel_code, config_json
                        FROM integration_channels
                        WHERE is_active = true
                          AND (type = 'SPRING_BATCH' OR type = 'SYSTEM_BATCH' OR channel_code = 'CH-KRX-INBOUND-001')
                        "#
                    )
                    .fetch_all(&pool)
                    .await
                    .unwrap_or_default();

                    for (channel_id, channel_code, config_json) in active_batch_channels {
                        let config: serde_json::Value = config_json
                            .as_deref()
                            .and_then(|s| serde_json::from_str(s).ok())
                            .unwrap_or_else(|| serde_json::json!({}));

                        let cron_expr = config.get("cron").and_then(|v| v.as_str()).unwrap_or("0 0 16 * * MON-FRI");

                        if is_cron_due(cron_expr, now_kst) {
                            let start_of_minute = now_kst.with_second(0).and_then(|t| t.with_nanosecond(0)).unwrap_or(now_kst);
                            let start_utc = start_of_minute.naive_utc();

                            let already_run: bool = sqlx::query_scalar(
                                r#"
                                SELECT EXISTS(
                                    SELECT 1 FROM integration_logs
                                    WHERE channel_id = $1
                                      AND created_at >= $2
                                )
                                "#
                            )
                            .bind(channel_id)
                            .bind(start_utc)
                            .fetch_one(&pool)
                            .await
                            .unwrap_or(true);

                            if !already_run {
                                if AdvisoryLock::try_acquire(&pool, StockDataIngestionJob::LOCK_ID).await {
                                    let already_run_locked: bool = sqlx::query_scalar(
                                        r#"
                                        SELECT EXISTS(
                                            SELECT 1 FROM integration_logs
                                            WHERE channel_id = $1
                                              AND created_at >= $2
                                        )
                                        "#
                                    )
                                    .bind(channel_id)
                                    .bind(start_utc)
                                    .fetch_one(&pool)
                                    .await
                                    .unwrap_or(true);

                                    if !already_run_locked {
                                        tracing::info!(
                                            "⏰ [Dynamic Batch Scheduler] Triggering batch for channel {} (cron: '{}', KST: {})...",
                                            channel_code, cron_expr, now_kst.format("%Y-%m-%d %H:%M:%S")
                                        );

                                        let markets_vec: Option<Vec<String>> = config.get("batchParams")
                                            .and_then(|v| v.as_str())
                                            .and_then(|p| serde_json::from_str::<serde_json::Value>(p).ok())
                                            .and_then(|v| v.get("markets").and_then(|m| m.as_str()).map(|s| s.split(',').map(|x| x.trim().to_string()).filter(|x| !x.is_empty()).collect()));

                                        let clear_existing = config.get("clearExisting").and_then(|v| v.as_bool());

                                        let seed_req = crate::batch::stock_ingestion::StockSeedRequest {
                                            clear_existing,
                                            markets: markets_vec,
                                            limit_per_market: None,
                                            custom_rows: None,
                                        };

                                        match StockDataIngestionJob::run_ingestion(&pool, seed_req, "SCHEDULED_CRON").await {
                                            Ok(res) => {
                                                tracing::info!("✅ Scheduled batch completed for {}: {}", channel_code, res.message);
                                            }
                                            Err(e) => {
                                                tracing::error!("❌ Scheduled batch error for {}: {}", channel_code, e);
                                            }
                                        }
                                    }
                                    AdvisoryLock::release(&pool, StockDataIngestionJob::LOCK_ID).await;
                                }
                            }
                        }
                    }
                }
            });
        }

        tracing::info!("✅ All MDM Batch Schedulers initialized successfully!");
    }
}
