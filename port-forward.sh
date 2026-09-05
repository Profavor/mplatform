#!/usr/bin/env bash

MINIKUBE_IP="$(minikube ip 2>/dev/null || echo "192.168.49.2")"
PID_FILE="/tmp/mplatform_port_forward.pid"
LOG_FILE="/tmp/mplatform_port_forward.log"

stop() {
  docker rm -f mdm-proxy-443 mdm-proxy-80 mdm-proxy-8443 mdm-proxy-8080 >/dev/null 2>&1
  pkill -f "socat.*LISTEN:8443" 2>/dev/null
  pkill -f "socat.*LISTEN:8080" 2>/dev/null
  echo "✅ Port forwarding stopped."
}

start() {
  stop >/dev/null 2>&1

  echo "================================================================"
  echo "  🚀 Starting Multi-Port Forwarding -> Minikube ($MINIKUBE_IP)"
  echo "================================================================"
  echo "  - Standard HTTPS: 0.0.0.0:443  -> $MINIKUBE_IP:443"
  echo "  - Standard HTTP : 0.0.0.0:80   -> $MINIKUBE_IP:80"
  echo "  - Alt HTTPS     : 0.0.0.0:8443 -> $MINIKUBE_IP:443"
  echo "  - Alt HTTP      : 0.0.0.0:8080 -> $MINIKUBE_IP:80"
  echo "================================================================"

  docker run -d --name mdm-proxy-443  --network minikube -p 443:443   --restart always alpine/socat:latest TCP-LISTEN:443,fork,reuseaddr TCP:"$MINIKUBE_IP":443 >/dev/null 2>&1
  docker run -d --name mdm-proxy-80   --network minikube -p 80:80     --restart always alpine/socat:latest TCP-LISTEN:80,fork,reuseaddr TCP:"$MINIKUBE_IP":80 >/dev/null 2>&1
  docker run -d --name mdm-proxy-8443 --network minikube -p 8443:8443 --restart always alpine/socat:latest TCP-LISTEN:8443,fork,reuseaddr TCP:"$MINIKUBE_IP":443 >/dev/null 2>&1
  docker run -d --name mdm-proxy-8080 --network minikube -p 8080:8080 --restart always alpine/socat:latest TCP-LISTEN:8080,fork,reuseaddr TCP:"$MINIKUBE_IP":80 >/dev/null 2>&1

  echo "✅ All port forwards are active and listening!"
  echo "   Router Port Forwarding Guide (LG U+ 192.168.219.1):"
  echo "   - Internal IP: 192.168.219.132"
  echo "   - External 443 -> Internal 8443 (or 443) [TCP]"
  echo "   - External 80  -> Internal 8080 (or 80)  [TCP]"
}

status() {
  echo "=== Current Listening Ports on Host ==="
  ss -tln | grep -E ':443\b|:80\b|:8443\b|:8080\b' || echo "No ports active"
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
