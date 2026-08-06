# Stage 1: Build stage
FROM node:24-alpine AS builder
WORKDIR /app

# Copy package files and install dependencies
COPY package*.json ./
RUN npm ci

ARG API_BASE_URL=http://localhost:8080
ENV API_BASE_URL=$API_BASE_URL
ENV NUXT_PUBLIC_API_BASE=$API_BASE_URL

# Copy application source code
COPY . .

# Build Nuxt 3 SSR application
RUN npm run build

# Stage 2: Runtime stage
FROM node:24-alpine AS runner
WORKDIR /app

ARG API_BASE_URL=http://localhost:8080
ENV API_BASE_URL=$API_BASE_URL
ENV NUXT_PUBLIC_API_BASE=$API_BASE_URL
ENV NODE_ENV=production
ENV PORT=3000
ENV HOST=0.0.0.0

# Copy built output bundle from builder
COPY --from=builder /app/.output /app/.output

# Fix Nitro static asset resolution in Docker multi-stage (resolve symlink issues)
RUN mkdir -p /app/.output/server/chunks/public && \
    cp -rn /app/.output/public/* /app/.output/server/chunks/public/ 2>/dev/null || true

EXPOSE 3000

CMD ["node", "/app/.output/server/index.mjs"]
