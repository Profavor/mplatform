#!/bin/sh
set -e
export VAULT_ADDR="http://127.0.0.1:8200"

UNSEAL_KEY=$(grep -A 1 'unseal_keys_b64' /vault/data/init.json | tail -n 1 | tr -d ' ",\t\r\n')
ROOT_TOKEN=$(grep 'root_token' /vault/data/init.json | cut -d'"' -f4 | tr -d '\r\n ')

echo "Extracted UNSEAL_KEY: $UNSEAL_KEY"
echo "Extracted ROOT_TOKEN: $ROOT_TOKEN"

echo "$UNSEAL_KEY" > /vault/data/unseal_key
echo "$ROOT_TOKEN" > /vault/data/root_token
chmod 600 /vault/data/unseal_key /vault/data/root_token

echo "Unsealing Vault..."
vault operator unseal "$UNSEAL_KEY"

export VAULT_TOKEN="$ROOT_TOKEN"

echo "Enabling Transit Secrets Engine..."
vault secrets enable transit || true

echo "Creating AES-256-GCM encryption key (mdm-field-key)..."
vault write -f transit/keys/mdm-field-key type=aes256-gcm96 || true

echo "Enabling Kubernetes Auth..."
vault auth enable kubernetes || true
vault write auth/kubernetes/config \
    kubernetes_host="https://kubernetes.default.svc:443" || true

echo "Creating MDM Transit Policy..."
vault policy write mdm-policy - <<EOF
path "transit/encrypt/mdm-field-key" {
  capabilities = ["create", "update"]
}
path "transit/decrypt/mdm-field-key" {
  capabilities = ["create", "update"]
}
path "transit/hmac/mdm-field-key" {
  capabilities = ["create", "update"]
}
path "transit/rewrap/mdm-field-key" {
  capabilities = ["create", "update"]
}
EOF

echo "Binding MDM Role for Kubernetes Auth..."
vault write auth/kubernetes/role/mdm-role \
    bound_service_account_names=default,vault-sa,backend \
    bound_service_account_namespaces=mdm-system \
    policies=mdm-policy \
    ttl=24h || true

echo "=== VAULT_SETUP_FINISHED_SUCCESSFULLY ==="
