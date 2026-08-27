# Stage 1: Build stage
FROM node:24-slim AS builder
WORKDIR /app

# Copy package files and install dependencies (cached unless package*.json changes)
COPY package*.json ./
COPY patch-oidc.js ./
RUN npm ci

# Cache bust for source code changes
ARG CACHEBUST=1
RUN echo "Cache bust: $CACHEBUST"

ARG API_BASE_URL=http://backend:8080
ENV API_BASE_URL=$API_BASE_URL
ENV NUXT_PUBLIC_API_BASE=""

# Copy application source code
COPY . .

# Build Nuxt 3 SSR application
ENV OAUTH2_ISSUER_URI="/auth/realms/mplatform"
ENV KEYCLOAK_REDIRECT_URI="/auth/keycloak/callback"
ENV NODE_OPTIONS="--max-old-space-size=4096"
RUN npm run build

# Stage 2: Runtime stage
FROM node:24-slim AS runner
WORKDIR /app

# Install tzdata for timezone support
RUN apt-get update && \
    apt-get install -y --no-install-recommends tzdata && \
    rm -rf /var/lib/apt/lists/*

ARG API_BASE_URL=http://backend:8080
ENV API_BASE_URL=$API_BASE_URL
ENV NUXT_PUBLIC_API_BASE=""
ENV NODE_ENV=production
ENV PORT=3000
ENV HOST=0.0.0.0

# Copy built output bundle from builder (standalone bundle)
COPY --from=builder /app/.output /app/.output

# Copy node_modules for SSR external dependencies (e.g. undio)
COPY --from=builder /app/node_modules /app/node_modules

# Fix Nitro static asset resolution in Docker multi-stage
RUN mkdir -p /app/.output/server/chunks/public && \
    cp -rn /app/.output/public/* /app/.output/server/chunks/public/ 2>/dev/null || true

EXPOSE 3000

CMD ["node", "/app/.output/server/index.mjs"]
