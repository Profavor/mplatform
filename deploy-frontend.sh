#!/usr/bin/env bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# 1. 태그 결정 (인자 우선, 없으면 package.json 기준)
if [ -n "$1" ]; then
  TAG="$1"
  # frontend/package.json 버전 자동 동기화
  sed -i "s/\"version\": \".*\"/\"version\": \"$TAG\"/" "$SCRIPT_DIR/frontend/package.json" 2>/dev/null || true
else
  PKG_VERSION=$(grep -m1 '"version"' "$SCRIPT_DIR/frontend/package.json" 2>/dev/null | awk -F'"' '{print $4}')
  TAG="${IMAGE_TAG:-${PKG_VERSION:-latest}}"
fi

# k8s/31-frontend.yaml 이미지 태그 및 버전 라벨 동기화
if [ -f "$SCRIPT_DIR/k8s/31-frontend.yaml" ] && [ "$TAG" != "latest" ]; then
  sed -i "s|image: profavor2/mplatform-frontend:.*|image: profavor2/mplatform-frontend:$TAG|g" "$SCRIPT_DIR/k8s/31-frontend.yaml"
  sed -i "s|version: \".*\"|version: \"$TAG\"|g" "$SCRIPT_DIR/k8s/31-frontend.yaml"
fi

echo "================================================================"
echo "  [Quick Deploy] Frontend -> Kubernetes"
echo "================================================================"
echo " Tag: $TAG"
echo "================================================================"

echo "==> 1. Building Nuxt on host (Fast)..."
(cd "$SCRIPT_DIR/frontend" && NODE_OPTIONS="--max-old-space-size=4096" npm run build)

echo "==> 2. Packaging Docker image..."
(cd "$SCRIPT_DIR/frontend" && docker build -t "profavor2/mplatform-frontend:$TAG" .)

echo "==> 3. Loading image into Minikube..."
minikube image load "profavor2/mplatform-frontend:$TAG"

echo "==> 4. Applying K8s frontend manifest..."
kubectl apply -f "$SCRIPT_DIR/k8s/31-frontend.yaml"

echo "==> 5. Restarting frontend deployment..."
kubectl rollout restart deployment frontend -n mdm-system
kubectl rollout status deployment frontend -n mdm-system --timeout=60s

echo "==> 6. Current Cluster Pod Status:"
kubectl get pods -n mdm-system

echo "================================================================"
echo " [SUCCESS] Frontend v$TAG deployed successfully!"
echo "================================================================"
