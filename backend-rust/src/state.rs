use crate::config::Config;
use crate::services::chat_service::ChatService;
use crate::services::dashboard_service::DashboardService;
use crate::services::excel_service::ExcelService;
use crate::services::field_encryption_service::FieldEncryptionService;
use crate::services::governance_service::GovernanceService;
use crate::services::integration_service::IntegrationService;
use crate::services::stock_bot_service::StockBotService;
use crate::services::system_service::SystemService;
use crate::services::two_factor_service::TwoFactorService;
use sqlx::PgPool;
use std::sync::Arc;
use tokio::sync::broadcast;

#[derive(Clone)]
pub struct AppState {
    pub instance_id: String,
    pub db: PgPool,
    pub pool: PgPool,
    pub config: Arc<Config>,
    pub broadcast_tx: broadcast::Sender<String>,
    pub two_factor_service: TwoFactorService,
    pub dashboard_service: DashboardService,
    pub chat_service: ChatService,
    pub stock_bot_service: Arc<StockBotService>,
    pub excel_service: ExcelService,
    pub governance_service: GovernanceService,
    pub system_service: SystemService,
    pub integration_service: IntegrationService,
    pub field_encryption_service: Arc<FieldEncryptionService>,
}

impl AppState {
    pub fn new(db: PgPool, config: Config) -> Self {
        let instance_id = uuid::Uuid::new_v4().to_string();
        let (broadcast_tx, _) = broadcast::channel(1024);
        let two_factor_service = TwoFactorService::new(db.clone());
        let dashboard_service = DashboardService::new(db.clone());
        let chat_service = ChatService::new(db.clone(), broadcast_tx.clone(), instance_id.clone());
        let stock_bot_service = Arc::new(StockBotService::new(db.clone(), broadcast_tx.clone()));
        let excel_service = ExcelService::new(db.clone());
        let governance_service = GovernanceService::new(db.clone());
        let system_service = SystemService::new(db.clone());
        let integration_service = IntegrationService::new(db.clone());
        let field_encryption_service = Arc::new(FieldEncryptionService::new());

        Self {
            instance_id,
            db: db.clone(),
            pool: db,
            config: Arc::new(config),
            broadcast_tx,
            two_factor_service,
            dashboard_service,
            chat_service,
            stock_bot_service,
            excel_service,
            governance_service,
            system_service,
            integration_service,
            field_encryption_service,
        }
    }
}
