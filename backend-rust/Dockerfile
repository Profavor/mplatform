# ------------------------------------------------------------------------------
# Build Stage
# ------------------------------------------------------------------------------
FROM rust:slim-bookworm AS builder

WORKDIR /usr/src/backend-rust

# Pre-install dependencies for faster build cache
RUN apt-get update && apt-get install -y pkg-config libssl-dev && rm -rf /var/lib/apt/lists/*

COPY Cargo.toml Cargo.lock ./
RUN mkdir src && echo "fn main() {}" > src/main.rs && cargo build --release && rm -rf src

# Copy real source code and build
COPY src ./src
COPY data ./data
RUN touch src/main.rs && cargo build --release

# ------------------------------------------------------------------------------
# Runtime Stage (Ultra-lightweight ~30MB)
# ------------------------------------------------------------------------------
FROM debian:bookworm-slim

RUN apt-get update && apt-get install -y ca-certificates libssl3 curl && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY --from=builder /usr/src/backend-rust/target/release/backend-rust /app/backend-rust
COPY data /app/data

ENV PORT=8082
EXPOSE 8082

HEALTHCHECK --interval=10s --timeout=3s --retries=3 \
  CMD curl -f http://localhost:8082/health || exit 1

ENTRYPOINT ["/app/backend-rust"]
