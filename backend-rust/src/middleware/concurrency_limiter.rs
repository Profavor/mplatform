use axum::{
    body::Body,
    http::{Request, Response, StatusCode},
    middleware::Next,
};
use std::sync::atomic::{AtomicUsize, Ordering};
use std::sync::Arc;

/// Tracks the number of in-flight concurrent requests across the server.
/// When the count exceeds `max_concurrent`, returns 429 Too Many Requests
/// with a Retry-After header so clients can back off gracefully.
#[derive(Clone)]
pub struct ConcurrencyState {
    pub in_flight: Arc<AtomicUsize>,
    pub max_concurrent: usize,
}

impl ConcurrencyState {
    pub fn new(max_concurrent: usize) -> Self {
        Self {
            in_flight: Arc::new(AtomicUsize::new(0)),
            max_concurrent,
        }
    }

    pub fn current_load(&self) -> usize {
        self.in_flight.load(Ordering::Relaxed)
    }
}

/// Axum middleware that enforces concurrency limits.
/// Health check endpoints are always allowed through.
pub async fn concurrency_limiter(
    axum::extract::State(state): axum::extract::State<ConcurrencyState>,
    request: Request<Body>,
    next: Next,
) -> Response<Body> {
    let path = request.uri().path().to_string();

    // Always allow health checks and load monitoring through
    if path == "/health" || path == "/api/health" || path == "/api/system/load" {
        return next.run(request).await;
    }

    let current = state.in_flight.fetch_add(1, Ordering::SeqCst);

    if current >= state.max_concurrent {
        // Over limit — decrement immediately and return 429
        state.in_flight.fetch_sub(1, Ordering::SeqCst);

        tracing::warn!(
            "🚦 Concurrency limit reached: {}/{} — returning 429 for {}",
            current,
            state.max_concurrent,
            path
        );

        let body = serde_json::json!({
            "error": "서버가 일시적으로 혼잡합니다. 잠시 후 다시 시도해주세요.",
            "error_en": "Server is temporarily overloaded. Please retry shortly.",
            "retry_after": 5,
            "current_load": current,
            "max_capacity": state.max_concurrent
        });

        return Response::builder()
            .status(StatusCode::TOO_MANY_REQUESTS)
            .header("Content-Type", "application/json; charset=utf-8")
            .header("Retry-After", "5")
            .header("X-RateLimit-Limit", state.max_concurrent.to_string())
            .header("X-RateLimit-Remaining", "0")
            .body(Body::from(serde_json::to_string(&body).unwrap()))
            .unwrap();
    }

    // Process the request
    let response = next.run(request).await;

    // Decrement on completion
    state.in_flight.fetch_sub(1, Ordering::SeqCst);

    response
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_concurrency_state_new() {
        let state = ConcurrencyState::new(4000);
        assert_eq!(state.max_concurrent, 4000);
        assert_eq!(state.current_load(), 0);
    }

    #[test]
    fn test_concurrency_state_increment_decrement() {
        let state = ConcurrencyState::new(100);
        state.in_flight.fetch_add(1, Ordering::SeqCst);
        assert_eq!(state.current_load(), 1);
        state.in_flight.fetch_add(1, Ordering::SeqCst);
        assert_eq!(state.current_load(), 2);
        state.in_flight.fetch_sub(1, Ordering::SeqCst);
        assert_eq!(state.current_load(), 1);
        state.in_flight.fetch_sub(1, Ordering::SeqCst);
        assert_eq!(state.current_load(), 0);
    }

    #[test]
    fn test_over_limit_detection() {
        let state = ConcurrencyState::new(2);
        // Simulate 2 in-flight
        state.in_flight.fetch_add(1, Ordering::SeqCst);
        state.in_flight.fetch_add(1, Ordering::SeqCst);
        // Now at limit — next request should be rejected
        let current = state.in_flight.load(Ordering::SeqCst);
        assert!(current >= state.max_concurrent);
    }
}
