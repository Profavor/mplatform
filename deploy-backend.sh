#!/usr/bin/env bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# 1. 태그 결정 (인자 우선, 없으면 pom.xml 기준)
if [ -n "$1" ]; then
  TAG="$1"
  # pom.xml의 domain-system 프로젝트 버전만 정확히 동기화
  sed -i "/<artifactId>domain-system<\/artifactId>/{n;s/<version>.*<\/version>/<version>$TAG<\/version>/}" "$SCRIPT_DIR/backend/pom.xml" 2>/dev/null || true
else
  POM_VERSION=$(grep -A1 '<artifactId>domain-system</artifactId>' "$SCRIPT_DIR/backend/pom.xml" 2>/dev/null | grep '<version>' | awk -F'[><]' '{print $3}')
  TAG="${IMAGE_TAG:-${POM_VERSION:-latest}}"
fi

# k8s/30-backend.yaml 이미지 태그 및 버전 라벨 동기화
if [ -f "$SCRIPT_DIR/k8s/30-backend.yaml" ] && [ "$TAG" != "latest" ]; then
  sed -i "s|image: profavor2/mplatform-backend:.*|image: profavor2/mplatform-backend:$TAG|g" "$SCRIPT_DIR/k8s/30-backend.yaml"
  sed -i "s|version: \".*\"|version: \"$TAG\"|g" "$SCRIPT_DIR/k8s/30-backend.yaml"
fi

echo "================================================================"
echo "  [Quick Deploy] Backend -> Kubernetes"
echo "================================================================"
echo " Tag: $TAG"
echo "================================================================"

echo "==> 1. Building JAR on host (Fast with local cache)..."
(cd "$SCRIPT_DIR/backend" && mvn clean package -DskipTests)

echo "==> 2. Packaging Docker image..."
(cd "$SCRIPT_DIR/backend" && docker build -t "profavor2/mplatform-backend:$TAG" .)

echo "==> 3. Loading image into Minikube..."
minikube image load "profavor2/mplatform-backend:$TAG"

echo "==> 4. Applying K8s backend manifest..."
kubectl apply -f "$SCRIPT_DIR/k8s/30-backend.yaml"

echo "==> 5. Restarting backend deployment..."
kubectl rollout restart deployment backend -n mdm-system
kubectl rollout status deployment backend -n mdm-system --timeout=60s

echo "==> 6. Current Cluster Pod Status:"
kubectl get pods -n mdm-system

echo "================================================================"
echo " [SUCCESS] Backend v$TAG deployed successfully!"
echo "================================================================"
