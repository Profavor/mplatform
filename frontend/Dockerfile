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

# Fix nuxt-oidc-auth module typescript reference push bug with Nuxt 3.21+
RUN node -e "const fs=require('fs');const f='node_modules/nuxt-oidc-auth/dist/module.mjs';let c=fs.readFileSync(f,'utf8');c=c.split('.push(').join('?.push(');fs.writeFileSync(f,c);" || true

# Build Nuxt 3 SSR application
ENV OAUTH2_ISSUER_URI="http://mplatform.local/auth/realms/mplatform"
ENV KEYCLOAK_REDIRECT_URI="http://mplatform.local/auth/keycloak/callback"
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
COPY --from=builder /app/node_modules /app/node_modules

# Fix Nitro static asset resolution in Docker multi-stage (resolve symlink issues)
RUN mkdir -p /app/.output/server/chunks/public && \
    cp -rn /app/.output/public/* /app/.output/server/chunks/public/ 2>/dev/null || true

# Fix Nitro incomplete symlink for Vue
RUN rm -rf /app/.output/server/node_modules/vue && \
    cp -a /app/node_modules/vue /app/.output/server/node_modules/vue

EXPOSE 3000

CMD ["node", "/app/.output/server/index.mjs"]
