#!/usr/bin/env bash
# Start/stop/status for all 5 standalone Task 1 microservices.
# Usage: ./start.sh [start|stop|status|urls]  (default: start)
set -uo pipefail
cd "$(dirname "$0")"

export JAVA_HOME="${JAVA_HOME:-/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home}"

SERVICES=(
  "bc01-identity-access:8081"
  "bc02-fleet-management:8082"
  "bc03-booking:8083"
  "bc04-payment:8084"
  "bc05-rating:8085"
)

urls() {
  cat <<'EOF'

  Service                 UI                                    API docs
  -----------------------------------------------------------------------------------------
  bc01 Identity & Access   http://localhost:8081/ui              http://localhost:8081/swagger-ui.html
  bc02 Fleet Management    http://localhost:8082/ui              http://localhost:8082/swagger-ui.html
  bc03 Booking             http://localhost:8083/ui              http://localhost:8083/swagger-ui.html
  bc04 Payment             http://localhost:8084/ui              http://localhost:8084/swagger-ui.html
  bc05 Rating              http://localhost:8085/ui              http://localhost:8085/swagger-ui.html

  H2 console for any service: http://localhost:<port>/h2-console
EOF
}

start() {
  for entry in "${SERVICES[@]}"; do
    port="${entry##*:}"
    if lsof -ti:"$port" >/dev/null 2>&1; then
      echo "Port $port is already in use - run './start.sh stop' first if these are stale processes."
      exit 1
    fi
  done

  echo "Building all modules..."
  ./mvnw -q clean install -DskipTests || { echo "Build failed."; exit 1; }

  mkdir -p logs
  for entry in "${SERVICES[@]}"; do
    name="${entry%%:*}"
    echo "Starting $name..."
    nohup ./mvnw -q -pl "$name" spring-boot:run > "logs/$name.log" 2>&1 &
    disown
  done

  echo "Waiting for all services to report healthy..."
  for entry in "${SERVICES[@]}"; do
    name="${entry%%:*}"
    port="${entry##*:}"
    up=false
    for _ in $(seq 1 40); do
      if curl -s "http://localhost:$port/actuator/health" 2>/dev/null | grep -q '"status":"UP"'; then
        up=true
        break
      fi
      sleep 3
    done
    if $up; then echo "  $name (:$port) is UP"; else echo "  $name (:$port) did NOT come up in time - check logs/$name.log"; fi
  done

  urls
}

stop() {
  for entry in "${SERVICES[@]}"; do
    name="${entry%%:*}"
    port="${entry##*:}"
    pids=$(lsof -ti:"$port" 2>/dev/null || true)
    if [ -n "$pids" ]; then
      echo "Stopping $name (:$port)..."
      kill $pids 2>/dev/null || true
    fi
  done
  sleep 2
  for entry in "${SERVICES[@]}"; do
    port="${entry##*:}"
    pids=$(lsof -ti:"$port" 2>/dev/null || true)
    [ -n "$pids" ] && kill -9 $pids 2>/dev/null || true
  done
  echo "Stopped."
}

status() {
  for entry in "${SERVICES[@]}"; do
    name="${entry%%:*}"
    port="${entry##*:}"
    if curl -s "http://localhost:$port/actuator/health" 2>/dev/null | grep -q '"status":"UP"'; then
      echo "$name (:$port) UP"
    else
      echo "$name (:$port) DOWN"
    fi
  done
}

case "${1:-start}" in
  start) start ;;
  stop) stop ;;
  status) status ;;
  urls) urls ;;
  *) echo "Usage: $0 {start|stop|status|urls}"; exit 1 ;;
esac
