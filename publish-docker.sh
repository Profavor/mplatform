#!/usr/bin/env bash
set -e

# ==============================================================================
# MPlatform Docker Build & Publish Automation Script (Bash)
# ==============================================================================

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PKG_VERSION=$(grep -m1 '"version"' "$SCRIPT_DIR/frontend/package.json" 2>/dev/null | awk -F'"' '{print $4}')

REGISTRY="${1:-${DOCKER_REGISTRY:-mplatform}}"
TAG="${2:-${IMAGE_TAG:-${PKG_VERSION:-latest}}}"
TARGET="${3:-all}"
PUSH="${PUSH:-true}"
INCLUDE_LATEST="${INCLUDE_LATEST:-true}"
NO_CACHE="${NO_CACHE:-false}"

REGISTRY="${REGISTRY%/}"

echo "================================================================"
echo "  MPlatform Docker Build & Publish Pipeline"
echo "================================================================"
echo " Registry:         $REGISTRY"
echo " Version/Tag:      $TAG (Package: ${PKG_VERSION:-N/A})"
echo " Target:           $TARGET"
echo " Push to Registry: $PUSH"
echo " Include Latest:   $INCLUDE_LATEST"
echo " No Cache:         $NO_CACHE"
echo "================================================================"

build_and_push() {
    local SERVICE_NAME="$1"
    local IMAGE_NAME="$2"
    local CONTEXT_DIR="$3"
    local DOCKERFILE="$4"
    
    local FULL_TAG="${REGISTRY}/${IMAGE_NAME}:${TAG}"
    local LATEST_TAG="${REGISTRY}/${IMAGE_NAME}:latest"
    
    echo ""
    echo "----------------------------------------------------------------"
    echo " Processing Service: $SERVICE_NAME"
    echo "----------------------------------------------------------------"
    
    local CACHE_FLAG=""
    if [ "$NO_CACHE" = "true" ]; then
        CACHE_FLAG="--no-cache"
    fi
    
    echo "==> Building Docker Image: $FULL_TAG"
    docker build $CACHE_FLAG -t "$FULL_TAG" -f "$DOCKERFILE" "$CONTEXT_DIR"
    
    if [ "$INCLUDE_LATEST" = "true" ] && [ "$TAG" != "latest" ]; then
        echo "==> Tagging as latest: $LATEST_TAG"
        docker tag "$FULL_TAG" "$LATEST_TAG"
    fi
    
    if [ "$PUSH" = "true" ]; then
        echo "==> Pushing image to registry: $FULL_TAG"
        docker push "$FULL_TAG"
        
        if [ "$INCLUDE_LATEST" = "true" ] && [ "$TAG" != "latest" ]; then
            echo "==> Pushing latest image: $LATEST_TAG"
            docker push "$LATEST_TAG"
        fi
        echo "[SUCCESS] Pushed $SERVICE_NAME successfully!"
    else
        echo "[SUCCESS] Built $SERVICE_NAME (Push Skipped)"
    fi
}

if [ "$TARGET" = "all" ] || [ "$TARGET" = "backend" ]; then
    build_and_push "backend" "mplatform-backend" "$SCRIPT_DIR/backend" "$SCRIPT_DIR/backend/Dockerfile"
fi

if [ "$TARGET" = "all" ] || [ "$TARGET" = "frontend" ]; then
    build_and_push "frontend" "mplatform-frontend" "$SCRIPT_DIR/frontend" "$SCRIPT_DIR/frontend/Dockerfile"
fi

if [ "$TARGET" = "all" ] || [ "$TARGET" = "mobile" ]; then
    build_and_push "mobile" "mplatform-mobile" "$SCRIPT_DIR/mobile" "$SCRIPT_DIR/mobile/Dockerfile"
fi

echo ""
echo "================================================================"
echo "  All operations completed successfully!"
echo "================================================================"
