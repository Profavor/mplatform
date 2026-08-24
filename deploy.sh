#!/usr/bin/env bash
set -e

# ==============================================================================
# MPlatform Kubernetes Deployment Script (Bash)
# ==============================================================================

REGISTRY="${1:-${DOCKER_REGISTRY:-}}"
TAG="${2:-${IMAGE_TAG:-latest}}"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
K8S_DIR="$SCRIPT_DIR/k8s"

echo "================================================================"
echo "  Deploying MPlatform to Kubernetes"
echo "================================================================"
echo " Namespace: mdm-system"
echo " Tag:       $TAG"
echo " Registry:  ${REGISTRY:-'(Default / Local)'}"
echo "================================================================"

echo "==> Applying Kubernetes Namespace, Configs, and Storage..."
kubectl apply -f "$K8S_DIR/00-namespace.yaml"
kubectl apply -f "$K8S_DIR/01-config.yaml"
kubectl apply -f "$K8S_DIR/02-pvc.yaml"

echo "==> Applying Application Deployments (Backend, Frontend, Mobile)..."
kubectl apply -f "$K8S_DIR/30-backend.yaml"
kubectl apply -f "$K8S_DIR/31-frontend.yaml"
kubectl apply -f "$K8S_DIR/32-mobile.yaml"
kubectl apply -f "$K8S_DIR/40-ingress.yaml"

if [ -n "$REGISTRY" ]; then
    REGISTRY="${REGISTRY%/}"
    echo "==> Setting remote registry image paths..."
    kubectl set image deployment/backend backend="$REGISTRY/mplatform-backend:$TAG" -n mdm-system
    kubectl set image deployment/frontend frontend="$REGISTRY/mplatform-frontend:$Tag" -n mdm-system
    kubectl set image deployment/mobile mobile="$REGISTRY/mplatform-mobile:$Tag" -n mdm-system
fi

echo "==> Restarting Deployments to pick up new images..."
kubectl rollout restart deployment backend -n mdm-system
kubectl rollout restart deployment frontend -n mdm-system
kubectl rollout restart deployment mobile -n mdm-system

echo "==> Waiting for rollout to complete..."
kubectl rollout status deployment backend -n mdm-system --timeout=180s
kubectl rollout status deployment frontend -n mdm-system --timeout=180s
kubectl rollout status deployment mobile -n mdm-system --timeout=180s

echo "[SUCCESS] All MPlatform deployments rolled out successfully!"
