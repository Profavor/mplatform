mod batch;
mod config;
mod error;
mod handlers;
mod middleware;
mod models;
mod repositories;
mod routes;
mod services;
mod state;

use config::Config;
use sqlx::postgres::PgPoolOptions;
use state::AppState;
use std::time::Duration;
use tracing_subscriber::{layer::SubscriberExt, util::SubscriberInitExt};

#[tokio::main]
async fn main() -> anyhow::Result<()> {
    // 1. Initialize logging
    tracing_subscriber::registry()
        .with(
            tracing_subscriber::EnvFilter::try_from_default_env()
                .unwrap_or_else(|_| "backend_rust=debug,tower_http=debug,axum=trace".into()),
        )
        .with(tracing_subscriber::fmt::layer())
        .init();

    // 2. Load configuration
    let config = Config::from_env();
    tracing::info!(
        "🚀 Starting MDM Platform Rust Backend v{}",
        env!("CARGO_PKG_VERSION")
    );
    tracing::info!("📡 Configured Port: {}", config.port);

    // 3. Setup PostgreSQL Connection Pool
    let max_connections: u32 = std::env::var("DATABASE_POOL_MAX")
        .ok()
        .and_then(|v| v.parse().ok())
        .unwrap_or(50);
    let min_connections: u32 = std::env::var("DATABASE_POOL_MIN")
        .ok()
        .and_then(|v| v.parse().ok())
        .unwrap_or(10);
    let acquire_timeout_secs: u64 = std::env::var("DATABASE_POOL_TIMEOUT_SECS")
        .ok()
        .and_then(|v| v.parse().ok())
        .unwrap_or(15);

    tracing::info!(
        "🔌 Connecting to PostgreSQL (pool: {}-{}, timeout: {}s)...",
        min_connections,
        max_connections,
        acquire_timeout_secs
    );
    let pool = PgPoolOptions::new()
        .max_connections(max_connections)
        .min_connections(min_connections)
        .acquire_timeout(Duration::from_secs(acquire_timeout_secs))
        .idle_timeout(Duration::from_secs(300))
        .after_connect(|conn, _meta| {
            Box::pin(async move {
                sqlx::query("SET timezone = 'Asia/Seoul';")
                    .execute(conn)
                    .await?;
                Ok(())
            })
        })
        .connect(&config.database_url)
        .await
        .map_err(|e| {
            tracing::error!("❌ Failed to connect to database: {:?}", e);
            e
        })?;

    tracing::info!("✅ PostgreSQL connection established successfully!");

    // 4. Create AppState and Router
    let state = AppState::new(pool, config.clone());

    // 4-1. Start Asynchronous Batch Schedulers
    batch::scheduler::BatchScheduler::start(state.db.clone());

    let app = routes::create_router(state);

    // 5. Bind Server
    let addr = std::net::SocketAddr::from(([0, 0, 0, 0], config.port));
    let listener = tokio::net::TcpListener::bind(addr).await?;
    tracing::info!("🌟 MDM Rust Backend listening on http://{}", addr);

    // 6. Run Server with Graceful Shutdown
    axum::serve(listener, app)
        .with_graceful_shutdown(shutdown_signal())
        .await?;

    tracing::info!("👋 Server shutdown completed cleanly");
    Ok(())
}

async fn shutdown_signal() {
    let ctrl_c = async {
        tokio::signal::ctrl_c()
            .await
            .expect("failed to install Ctrl+C handler");
    };

    #[cfg(unix)]
    let terminate = async {
        tokio::signal::unix::signal(tokio::signal::unix::SignalKind::terminate())
            .expect("failed to install signal handler")
            .recv()
            .await;
    };

    #[cfg(not(unix))]
    let terminate = std::future::pending::<()>();

    tokio::select! {
        _ = ctrl_c => {
            tracing::info!("🛑 Received Ctrl+C, initiating graceful shutdown...");
        },
        _ = terminate => {
            tracing::info!("🛑 Received SIGTERM, initiating graceful shutdown...");
        },
    }
}
