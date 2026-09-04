#!/usr/bin/env bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PID_FILE="/tmp/mplatform_direct.pid"
LOG_FILE="/tmp/mplatform_direct.log"
PORT_FILE="/tmp/mplatform_direct.port"

DEFAULT_PORT=8080
LISTEN_PORT="${2:-$DEFAULT_PORT}"

# socat 확인
if ! command -v socat >/dev/null 2>&1; then
  echo "❌ Error: 'socat'이 설치되어 있지 않습니다."
  echo "👉 설치: sudo apt-get update && sudo apt-get install -y socat"
  exit 1
fi

get_minikube_ip() {
  minikube ip 2>/dev/null || echo "192.168.49.2"
}

get_local_ip() {
  ip route get 8.8.8.8 2>/dev/null | awk '{print $7; exit}' || ip addr show wlo1 2>/dev/null | grep -oP '(?<=inet\s)\d+(\.\d+){3}' | head -n 1 || echo "127.0.0.1"
}

get_public_ip() {
  curl -s --max-time 3 https://ifconfig.me 2>/dev/null || curl -s --max-time 3 https://api.ipify.org 2>/dev/null || echo "알수없음"
}

get_gateway_ip() {
  ip route show default 2>/dev/null | awk '{print $3; exit}' || echo "192.168.219.1"
}

show_info() {
  local port="$1"
  local local_ip="$(get_local_ip)"
  local public_ip="$(get_public_ip)"
  local gateway_ip="$(get_gateway_ip)"
  local minikube_ip="$(get_minikube_ip)"

  local local_url="http://${local_ip}:${port}"
  local public_url=""
  if [ "$public_ip" != "알수없음" ]; then
    public_url="http://${public_ip}:${port}"
  fi

  echo "================================================================"
  echo "  🚀 MDM Platform 직접 외부 접속 (Direct Access) 활성화"
  echo "================================================================"
  echo "  📍 Minikube Ingress Target : http://${minikube_ip}:80"
  echo "  🔌 Local Listening Port    : ${port}"
  echo "----------------------------------------------------------------"
  echo "  📱 [동일 Wi-Fi/사내망 접속]  : ${local_url}"
  if [ -n "$public_url" ]; then
    echo "  🌐 [외부 인터넷/LTE 직접 접속] : ${public_url}"
  fi
  echo "================================================================"
  echo "  💡 공유기 설정 안내 (외부 접속 시 1회 설정):"
  echo "     1. 공유기 관리자 페이지: http://${gateway_ip}"
  echo "     2. [포트포워딩] 외부 ${port} -> 내부 IP ${local_ip} / 내부 포트 ${port} (TCP)"
  echo "        (또는 [DMZ] 대상 IP를 ${local_ip} 로 설정)"
  echo "================================================================"

  if [ -n "$public_url" ] && command -v qrencode >/dev/null 2>&1; then
    echo ""
    echo "📱 외부 접속 QR 코드 (스마트폰 카메라로 스캔):"
    qrencode -t ANSIUTF8 "${public_url}"
    echo ""
  elif command -v qrencode >/dev/null 2>&1; then
    echo ""
    echo "📱 Wi-Fi 내부 접속 QR 코드 (스마트폰 카메라로 스캔):"
    qrencode -t ANSIUTF8 "${local_url}"
    echo ""
  fi
}

start_direct() {
  local port="${1:-$DEFAULT_PORT}"

  if [ -f "$PID_FILE" ] && kill -0 "$(cat "$PID_FILE")" 2>/dev/null; then
    local running_port="$(cat "$PORT_FILE" 2>/dev/null || echo "$DEFAULT_PORT")"
    echo "⚠️ Direct access is already RUNNING (PID: $(cat "$PID_FILE"), Port: ${running_port})"
    show_info "$running_port"
    return 0
  fi

  # 1024 미만 포트 바인딩 시 root 권한 체크
  if [ "$port" -lt 1024 ] && [ "$EUID" -ne 0 ]; then
    echo "⚠️ 포트 ${port}는 1024 미만이므로 관리자(sudo) 권한이 필요합니다."
    echo "👉 'sudo ./direct.sh start ${port}' 또는 1024 이상 포트(예: 8080)를 사용하세요."
    exit 1
  fi

  local minikube_ip="$(get_minikube_ip)"

  echo "==> 포워딩 시작: 0.0.0.0:${port} -> ${minikube_ip}:80..."
  rm -f "$LOG_FILE"

  setsid socat TCP-LISTEN:${port},fork,reuseaddr TCP:${minikube_ip}:80 </dev/null > "$LOG_FILE" 2>&1 &
  local pid=$!
  sleep 1

  # setsid로 인해 실제 socat PID 찾기
  local socat_pid="$(pgrep -f "socat TCP-LISTEN:${port},fork,reuseaddr TCP:${minikube_ip}:80" | head -n 1 || echo "")"
  if [ -z "$socat_pid" ]; then
    socat_pid="$pid"
  fi

  if ! kill -0 "$socat_pid" 2>/dev/null; then
    echo "❌ 포워더 시작 실패. 로그를 확인하세요:"
    cat "$LOG_FILE"
    rm -f "$PID_FILE" "$PORT_FILE"
    exit 1
  fi

  echo "$socat_pid" > "$PID_FILE"
  echo "$port" > "$PORT_FILE"

  show_info "$port"
  echo "💡 백그라운드 프로세스 PID: $socat_pid"
  echo "💡 중지 명령: ./direct.sh stop"
  echo "💡 상태 확인: ./direct.sh status"
}

stop_direct() {
  local stopped=false
  if [ -f "$PID_FILE" ]; then
    local pid="$(cat "$PID_FILE")"
    if kill -0 "$pid" 2>/dev/null; then
      echo "==> 직접 접속 포워더 중지 중 (PID: $pid)..."
      kill "$pid" 2>/dev/null || true
      sleep 1
      stopped=true
    fi
    rm -f "$PID_FILE" "$PORT_FILE"
  fi
  # 잔여 socat 정리
  pkill -f "socat TCP-LISTEN:.*TCP:.*:80" 2>/dev/null || true
  if [ "$stopped" = true ]; then
    echo "✅ 직접 접속 포워더가 중지되었습니다."
  else
    echo "⚠️ 실행 중인 포워더가 없거나 이미 종료되었습니다."
  fi
}

status_direct() {
  if [ -f "$PID_FILE" ] && kill -0 "$(cat "$PID_FILE")" 2>/dev/null; then
    local port="$(cat "$PORT_FILE" 2>/dev/null || echo "$DEFAULT_PORT")"
    echo "✅ Direct access is RUNNING (PID: $(cat "$PID_FILE"), Port: ${port})"
    show_info "$port"
  else
    echo "❌ Direct access is STOPPED"
  fi
}

case "$1" in
  start)
    start_direct "$LISTEN_PORT"
    ;;
  stop)
    stop_direct
    ;;
  restart)
    stop_direct
    sleep 1
    start_direct "$LISTEN_PORT"
    ;;
  status)
    status_direct
    ;;
  *)
    start_direct "$LISTEN_PORT"
    ;;
esac
