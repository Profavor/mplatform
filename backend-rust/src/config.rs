use std::env;

#[derive(Clone, Debug)]
pub struct Config {
    pub port: u16,
    pub database_url: String,
    pub jwt_secret: String,
    pub jwt_access_expiration_sec: i64,
    pub jwt_refresh_expiration_sec: i64,
    pub cors_allowed_origins: Vec<String>,
    pub upload_dir: String,
    pub minio_url: String,
    pub minio_bucket: String,
}

impl Config {
    pub fn from_env() -> Self {
        dotenvy::dotenv().ok();

        let port = env::var("PORT")
            .ok()
            .and_then(|p| p.parse::<u16>().ok())
            .unwrap_or(8082); // Default to 8082 to avoid collision with Spring Boot on 8080

        let database_url = env::var("DATABASE_URL").unwrap_or_else(|_| {
            let host = env::var("DB_HOST").unwrap_or_else(|_| "localhost".to_string());
            let port = env::var("DB_PORT").unwrap_or_else(|_| "5432".to_string());
            let name = env::var("DB_NAME").unwrap_or_else(|_| "domain_system".to_string());
            let user = env::var("DB_USERNAME").unwrap_or_else(|_| "postgres".to_string());
            let pass = env::var("DB_PASSWORD").unwrap_or_else(|_| "password".to_string());
            format!("postgres://{user}:{pass}@{host}:{port}/{name}?sslmode=disable")
        });

        let jwt_secret = env::var("JWT_SECRET").unwrap_or_else(|_| {
            "YOUR_SUPER_SECRET_KEY_FOR_JWT_AUTHENTICATION_OVER_256_BITS".to_string()
        });

        let jwt_access_expiration_sec = env::var("JWT_ACCESS_EXPIRATION_SEC")
            .ok()
            .and_then(|v| v.parse::<i64>().ok())
            .unwrap_or(1800);

        let jwt_refresh_expiration_sec = env::var("JWT_REFRESH_EXPIRATION_SEC")
            .ok()
            .and_then(|v| v.parse::<i64>().ok())
            .unwrap_or(172800);

        let cors_allowed_origins = env::var("CORS_ALLOWED_ORIGINS")
            .unwrap_or_else(|_| "http://localhost:3000,http://localhost:8080".to_string())
            .split(',')
            .map(|s| s.trim().to_string())
            .filter(|s| !s.is_empty())
            .collect();

        let upload_dir = env::var("FILE_UPLOAD_DIR").unwrap_or_else(|_| "./uploads".to_string());
        let minio_url = env::var("MINIO_URL").unwrap_or_else(|_| "http://minio:9000".to_string());
        let minio_bucket = env::var("MINIO_BUCKET_NAME").unwrap_or_else(|_| "domain-system".to_string());

        Self {
            port,
            database_url,
            jwt_secret,
            jwt_access_expiration_sec,
            jwt_refresh_expiration_sec,
            cors_allowed_origins,
            upload_dir,
            minio_url,
            minio_bucket,
        }
    }
}
