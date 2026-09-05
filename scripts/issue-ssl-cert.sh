#!/usr/bin/env bash
set -e

DOMAIN="${1:-mdm.mplat.store}"
CERT_DIR="/tmp/letsencrypt"
mkdir -p "$CERT_DIR"

echo "================================================================"
echo "  🔒 Let's Encrypt Public SSL Certificate Issuer for $DOMAIN"
echo "================================================================"
echo " Mode: Standalone HTTP-01 Challenge (Port 80)"
echo " Automated: Zero DNS configuration required."
echo "================================================================"

echo "==> Temporarily pausing mdm-proxy-80..."
docker stop mdm-proxy-80 >/dev/null 2>&1 || true

echo "==> Running Certbot standalone..."
docker run --rm \
  -v "$CERT_DIR:/etc/letsencrypt" \
  --network host \
  certbot/certbot certonly \
  --standalone \
  --agree-tos \
  -m profavor@naver.com \
  --no-eff-email \
  -d "$DOMAIN"

echo "==> Resuming mdm-proxy-80..."
docker start mdm-proxy-80 >/dev/null 2>&1 || true

docker run --rm -v "$CERT_DIR:/etc/letsencrypt" alpine chmod -R 755 /etc/letsencrypt >/dev/null 2>&1 || true

CERT_PATH="$CERT_DIR/live/$DOMAIN/fullchain.pem"
KEY_PATH="$CERT_DIR/live/$DOMAIN/privkey.pem"

if [ -f "$CERT_PATH" ] && [ -f "$KEY_PATH" ]; then
  echo ""
  echo "==> Updating Kubernetes Secret (mdm-tls-secret)..."
  kubectl create secret tls mdm-tls-secret \
    --cert="$CERT_PATH" \
    --key="$KEY_PATH" \
    -n mdm-system \
    --dry-run=client -o yaml | kubectl apply -f -

  echo "==> Reloading Ingress Nginx..."
  kubectl rollout restart deployment ingress-nginx-controller -n ingress-nginx 2>/dev/null || true

  echo "================================================================"
  echo "  🎉 Official Let's Encrypt SSL applied successfully for $DOMAIN!"
  echo "================================================================"
else
  echo "❌ Certificate files not found at $CERT_DIR/live/$DOMAIN"
  exit 1
fi
