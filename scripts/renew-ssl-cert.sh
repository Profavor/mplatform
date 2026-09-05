#!/usr/bin/env bash
set -e

CERT_DIR="/home/profavor/.letsencrypt"
DOMAIN="mdm.mplat.store"

# Check if certificate expires within 30 days (2592000 seconds)
if openssl x509 -checkend 2592000 -noout -in "$CERT_DIR/live/$DOMAIN/cert.pem" >/dev/null 2>&1; then
  echo "✅ SSL Certificate for $DOMAIN is valid for more than 30 days. No renewal needed."
  exit 0
fi

echo "==> Certificate expires within 30 days. Starting automatic renewal..."

# 1. Temporarily pause port 80 proxy so Certbot standalone can bind port 80
docker stop mdm-proxy-80 >/dev/null 2>&1 || true

# 2. Run certbot renew standalone
docker run --rm \
  -v "$CERT_DIR:/etc/letsencrypt" \
  --network host \
  certbot/certbot renew --standalone

# 3. Resume port 80 proxy immediately
docker start mdm-proxy-80 >/dev/null 2>&1 || true

# 4. Ensure permissions
docker run --rm -v "$CERT_DIR:/etc/letsencrypt" alpine chmod -R 755 /etc/letsencrypt >/dev/null 2>&1 || true

# 5. Update Kubernetes Secret
kubectl create secret tls mdm-tls-secret \
  --cert="$CERT_DIR/live/$DOMAIN/fullchain.pem" \
  --key="$CERT_DIR/live/$DOMAIN/privkey.pem" \
  -n mdm-system \
  --dry-run=client -o yaml | kubectl apply -f -

# 6. Reload Ingress NGINX
kubectl rollout restart deployment ingress-nginx-controller -n ingress-nginx 2>/dev/null || true

echo "🎉 SSL Certificate renewed and applied to Kubernetes successfully!"
