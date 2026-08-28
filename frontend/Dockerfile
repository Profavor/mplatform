# Runtime stage (Pre-built standalone output from host npm run build)
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

# Copy built standalone output bundle and node_modules from host (No npm install inside docker!)
COPY .output /app/.output
COPY node_modules /app/node_modules

# Symlink host build path to container node_modules for ESM absolute path compatibility
RUN mkdir -p /home/profavor/dev/mplatform/frontend && \
    ln -s /app/node_modules /home/profavor/dev/mplatform/frontend/node_modules

# Fix Nitro static asset resolution
RUN mkdir -p /app/.output/server/chunks/public && \
    cp -rn /app/.output/public/* /app/.output/server/chunks/public/ 2>/dev/null || true

EXPOSE 3000

CMD ["node", "/app/.output/server/index.mjs"]

