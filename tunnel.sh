#!/usr/bin/env bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PID_FILE="/tmp/mplatform_tunnel.pid"
LOG_FILE="/tmp/mplatform_tunnel.log"

# Find cloudflared executable
CLOUDFLARED_BIN="$(which cloudflared 2>/dev/null || echo "$HOME/.local/bin/cloudflared")"

if [ ! -x "$CLOUDFLARED_BIN" ]; then
  echo "==> cloudflared not found. Downloading standalone binary to ~/.local/bin/cloudflared..."
  mkdir -p "$HOME/.local/bin"
  curl -fsSL https://github.com/cloudflare/cloudflared/releases/latest/download/cloudflared-linux-amd64 -o "$HOME/.local/bin/cloudflared"
  chmod +x "$HOME/.local/bin/cloudflared"
  CLOUDFLARED_BIN="$HOME/.local/bin/cloudflared"
fi

# Detect Minikube IP
MINIKUBE_IP="$(minikube ip 2>/dev/null || echo "192.168.49.2")"

show_url() {
  if [ -f "$LOG_FILE" ]; then
    TUNNEL_URL=$(grep -o 'https://[-a-zA-Z0-9]*\.trycloudflare\.com' "$LOG_FILE" | head -n 1 || true)
    if [ -n "$TUNNEL_URL" ]; then
      echo "  🔗 Current URL: $TUNNEL_URL"
      if which qrencode >/dev/null 2>&1; then
        echo ""
        echo "  📱 Mobile QR Code:"
        qrencode -t ANSIUTF8 "$TUNNEL_URL"
      fi
    fi
  fi
}

start_tunnel() {
  if [ -f "$PID_FILE" ] && kill -0 "$(cat "$PID_FILE")" 2>/dev/null; then
    echo "⚠️ Tunnel is already running with PID $(cat "$PID_FILE")."
    show_url
    return 0
  fi

  echo "================================================================"
  echo "  🚀 Starting Cloudflare Tunnel for MDM Platform"
  echo "================================================================"
  echo " Target Ingress: http://$MINIKUBE_IP (Host: mplatform.local)"
  echo "================================================================"

  rm -f "$LOG_FILE"
  "$CLOUDFLARED_BIN" tunnel --url "http://$MINIKUBE_IP" --http-host-header "mplatform.local" > "$LOG_FILE" 2>&1 &
  TUNNEL_PID=$!
  echo "$TUNNEL_PID" > "$PID_FILE"

  echo "==> Waiting for public URL generation..."
  TUNNEL_URL=""
  for i in {1..30}; do
    if [ -f "$LOG_FILE" ]; then
      TUNNEL_URL=$(grep -o 'https://[-a-zA-Z0-9]*\.trycloudflare\.com' "$LOG_FILE" | head -n 1 || true)
      if [ -n "$TUNNEL_URL" ]; then
        break
      fi
    fi
    sleep 1
  done

  if [ -n "$TUNNEL_URL" ]; then
    echo ""
    echo "================================================================"
    echo "  🎉 Tunnel Connected Successfully!"
    echo "================================================================"
    echo "  🔗 Public URL: $TUNNEL_URL"
    echo "  📱 Mobile & External Access Ready (HTTPS)"
    echo "================================================================"
    echo ""
    if which qrencode >/dev/null 2>&1; then
      echo "📱 Scan QR Code to open on Mobile:"
      qrencode -t ANSIUTF8 "$TUNNEL_URL"
      echo ""
    fi
    echo "💡 Logs: $LOG_FILE"
    echo "💡 Stop command: ./tunnel.sh stop"
    echo "💡 Status command: ./tunnel.sh status"
    echo "================================================================"
  else
    echo "❌ Failed to obtain tunnel URL within 30s. Check log:"
    cat "$LOG_FILE"
    kill "$TUNNEL_PID" 2>/dev/null || true
    rm -f "$PID_FILE"
    exit 1
  fi
}

stop_tunnel() {
  if [ -f "$PID_FILE" ]; then
    PID="$(cat "$PID_FILE")"
    if kill -0 "$PID" 2>/dev/null; then
      echo "==> Stopping tunnel (PID: $PID)..."
      kill "$PID" 2>/dev/null || true
      sleep 1
    fi
    rm -f "$PID_FILE"
    echo "✅ Tunnel stopped."
  else
    echo "⚠️ No running tunnel found."
  fi
  # Clean up any leftover cloudflared process
  pkill -f "cloudflared tunnel --url" 2>/dev/null || true
}

status_tunnel() {
  if [ -f "$PID_FILE" ] && kill -0 "$(cat "$PID_FILE")" 2>/dev/null; then
    echo "✅ Tunnel is RUNNING (PID: $(cat "$PID_FILE"))"
    show_url
  else
    echo "❌ Tunnel is STOPPED"
  fi
}

case "$1" in
  start)
    start_tunnel
    ;;
  stop)
    stop_tunnel
    ;;
  restart)
    stop_tunnel
    sleep 1
    start_tunnel
    ;;
  status)
    status_tunnel
    ;;
  *)
    start_tunnel
    ;;
esac
