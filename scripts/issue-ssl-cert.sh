#!/usr/bin/env bash
set -e

DOMAIN="${1:-mdm.mplat.store}"
CERT_DIR="/tmp/letsencrypt"
mkdir -p "$CERT_DIR"

echo "================================================================"
echo "  🔒 Let's Encrypt Public SSL Certificate Issuer for $DOMAIN"
echo "================================================================"
echo " Mode: DNS-01 Challenge (Gabia DNS TXT Record)"
echo " No HTTP/80 port required. Works behind ISP/NAT/Routers."
echo "================================================================"

echo "==> Running Certbot via Docker..."
docker run -it --rm \
  -v "$CERT_DIR:/etc/letsencrypt" \
  certbot/certbot certonly \
  --manual \
  --preferred-challenges dns \
  --agree-tos \
  --manual-public-ip-logging-ok \
  -d "$DOMAIN"

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
