#!/usr/bin/env bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
MINIKUBE_IP="$(minikube ip 2>/dev/null || echo "192.168.49.2")"

PID_FILE="/tmp/mplatform_port_forward.pid"
LOG_FILE="/tmp/mplatform_port_forward.log"

PORT_HTTPS="${PORT_HTTPS:-8443}"
PORT_HTTP="${PORT_HTTP:-8080}"

start() {
  # Clean up existing listeners on target ports
  pkill -f "socat.*LISTEN:$PORT_HTTPS" 2>/dev/null || true
  pkill -f "socat.*LISTEN:$PORT_HTTP" 2>/dev/null || true
  rm -f "$PID_FILE" "$LOG_FILE"

  echo "================================================================"
  echo "  🚀 Starting Host Port Forwarding -> Minikube ($MINIKUBE_IP)"
  echo "================================================================"
  echo "  - HTTPS: 0.0.0.0:$PORT_HTTPS -> $MINIKUBE_IP:443"
  echo "  - HTTP : 0.0.0.0:$PORT_HTTP  -> $MINIKUBE_IP:80"
  echo "================================================================"

  # Start socat as session daemon using setsid
  setsid socat TCP-LISTEN:"$PORT_HTTPS",fork,reuseaddr TCP:"$MINIKUBE_IP":443 </dev/null >> "$LOG_FILE" 2>&1 &
  PID_HTTPS=$!

  setsid socat TCP-LISTEN:"$PORT_HTTP",fork,reuseaddr TCP:"$MINIKUBE_IP":80 </dev/null >> "$LOG_FILE" 2>&1 &
  PID_HTTP=$!

  echo "$PID_HTTPS $PID_HTTP" > "$PID_FILE"
  sleep 1

  # Check if sockets are actually listening
  if ss -tln | grep -q ":$PORT_HTTPS\b" && ss -tln | grep -q ":$PORT_HTTP\b"; then
    echo "✅ Port forwarding started successfully and listening!"
    echo "   (HTTPS PID: $PID_HTTPS on port $PORT_HTTPS)"
    echo "   (HTTP  PID: $PID_HTTP  on port $PORT_HTTP)"
    echo ""
    echo "   Router Port Forwarding Guide (LG U+ 192.168.219.1):"
    echo "   - External 443 -> Host 192.168.219.132:$PORT_HTTPS (TCP)"
    echo "   - External 80  -> Host 192.168.219.132:$PORT_HTTP (TCP)"
  else
    echo "❌ Failed to bind sockets. Log:"
    cat "$LOG_FILE"
    exit 1
  fi
}

stop() {
  if [ -f "$PID_FILE" ]; then
    PIDS=$(cat "$PID_FILE")
    for pid in $PIDS; do
      kill "$pid" 2>/dev/null || true
    done
    rm -f "$PID_FILE"
  fi
  pkill -f "socat.*LISTEN:$PORT_HTTPS" 2>/dev/null || true
  pkill -f "socat.*LISTEN:$PORT_HTTP" 2>/dev/null || true
  echo "✅ Port forwarding stopped."
}

status() {
  HTTPS_OK=false
  HTTP_OK=false
  if ss -tln | grep -q ":$PORT_HTTPS\b"; then
    HTTPS_OK=true
  fi
  if ss -tln | grep -q ":$PORT_HTTP\b"; then
    HTTP_OK=true
  fi

  if [ "$HTTPS_OK" = true ] && [ "$HTTP_OK" = true ]; then
    echo "✅ Port forwarding is RUNNING and listening on ports:"
    echo "   HTTPS: 0.0.0.0:$PORT_HTTPS -> $MINIKUBE_IP:443"
    echo "   HTTP : 0.0.0.0:$PORT_HTTP  -> $MINIKUBE_IP:80"
    return 0
  else
    echo "❌ Port forwarding is STOPPED (HTTPS: $HTTPS_OK, HTTP: $HTTP_OK)"
    return 1
  fi
}

case "$1" in
  start)
    start
    ;;
  stop)
    stop
    ;;
  restart)
    stop
    sleep 1
    start
    ;;
  status)
    status
    ;;
  *)
    start
    ;;
esac
