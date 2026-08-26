# Stage 1: Build Flutter Web application
FROM ghcr.io/cirruslabs/flutter:stable AS builder

WORKDIR /app

# Enable web support
RUN flutter config --enable-web

# Install dependencies
COPY pubspec.yaml pubspec.lock ./
RUN flutter pub get

# Copy source and build web release
COPY . .
RUN flutter pub get
RUN flutter build web --release --base-href /mobile/ --pwa-strategy=none

# Stage 2: Production Nginx Server
FROM nginx:alpine

WORKDIR /usr/share/nginx/html

COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=builder /app/build/web /usr/share/nginx/html
COPY --from=builder /app/build/web /usr/share/nginx/html/mobile

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]

