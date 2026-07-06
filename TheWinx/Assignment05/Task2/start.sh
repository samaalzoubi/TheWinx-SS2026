#!/usr/bin/env bash
# Start/stop/status for the full Task 2 integrated system: Eureka, Config
# Server, the 5 bounded-context services, and the combined web portal.
# Usage: ./start.sh [start|stop|status|urls]  (default: start)
set -uo pipefail
cd "$(dirname "$0")"

export JAVA_HOME="${JAVA_HOME:-/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home}"

INFRA=(
  "infra-eureka-server:8761"
  "infra-config-server:8888"
)
SERVICES=(
  "bc01-identity-access:8081"
  "bc02-fleet-management:8082"
  "bc03-booking:8083"
  "bc04-payment:8084"
  "bc05-rating:8085"
  "infra-api-gateway:8080"
)
ALL=("${INFRA[@]}" "${SERVICES[@]}")

urls() {
  cat <<'EOF'

  *** Start here ***
  Combined web portal        http://localhost:8080          (register, log in, search & book, end ride, rate - one UI)

  Infrastructure
  Eureka dashboard           http://localhost:8761
  Config server              http://localhost:8888/bc03-booking/default   (example: shows resolved config for a service)

  Per-service API/UI (still available individually)
  Service                 UI                             API docs                              Circuit breakers
  ---------------------------------------------------------------------------------------------------------------------
  bc01 Identity & Access   http://localhost:8081/ui       http://localhost:8081/swagger-ui.html  -
  bc02 Fleet Management    http://localhost:8082/ui       http://localhost:8082/swagger-ui.html  -
  bc03 Booking             http://localhost:8083/ui       http://localhost:8083/swagger-ui.html  http://localhost:8083/actuator/circuitbreakers
  bc04 Payment             http://localhost:8084/ui       http://localhost:8084/swagger-ui.html  http://localhost:8084/actuator/circuitbreakers
  bc05 Rating              http://localhost:8085/ui       http://localhost:8085/swagger-ui.html  http://localhost:8085/actuator/circuitbreakers

  H2 console for any bc0X service: http://localhost:<port>/h2-console
EOF
}

# infra-config-server serves its own /{application}/{profile} API on its main
# port, which shadows /actuator/health there - its actuator lives on 8889 instead.
health_port() {
  if [ "$1" = "infra-config-server" ]; then echo 8889; else echo "$2"; fi
}

wait_healthy() {
  local name="$1" port="$2" hport
  hport="$(health_port "$name" "$port")"
  for _ in $(seq 1 40); do
    if curl -s "http://localhost:$hport/actuator/health" 2>/dev/null | grep -q '"status":"UP"'; then
      echo "  $name (:$port) is UP"
      return 0
    fi
    sleep 3
  done
  echo "  $name (:$port) did NOT come up in time - check logs/$name.log"
  return 1
}

# Each service's own /actuator/health only proves its JVM is up - it says
# nothing about whether it has *discovered its peers* yet via Eureka. Right
# after a synchronized restart, a service's local registry cache can still
# be empty for a few seconds, during which every cross-service call falls
# back to mock data. Wait for Eureka's own registry to list every expected
# service name before calling the stack actually ready.
wait_discovery() {
  local expected=("BC01-IDENTITY-ACCESS" "BC02-FLEET-MANAGEMENT" "BC03-BOOKING" "BC04-PAYMENT" "BC05-RATING" "INFRA-API-GATEWAY")
  echo "Waiting for services to discover each other via Eureka..."
  for _ in $(seq 1 20); do
    registered=$(curl -s -H "Accept: application/json" http://localhost:8761/eureka/apps 2>/dev/null)
    missing=0
    for name in "${expected[@]}"; do
      echo "$registered" | grep -q "\"$name\"" || missing=$((missing + 1))
    done
    if [ "$missing" -eq 0 ]; then
      echo "  All services registered with Eureka."
      sleep 6  # give clients one more registry-fetch cycle (5s interval) to pick up the full set
      return 0
    fi
    sleep 2
  done
  echo "  Warning: not all services showed up in Eureka's registry in time - cross-service calls may briefly use fallback data."
}

start() {
  for entry in "${ALL[@]}"; do
    port="${entry##*:}"
    if lsof -ti:"$port" >/dev/null 2>&1; then
      echo "Port $port is already in use - run './start.sh stop' first if these are stale processes."
      exit 1
    fi
  done

  echo "Building all modules (infra + 5 services + gateway)..."
  ./mvnw -q clean install -DskipTests || { echo "Build failed."; exit 1; }

  mkdir -p logs

  echo "Starting infrastructure (Eureka, Config Server) first..."
  for entry in "${INFRA[@]}"; do
    name="${entry%%:*}"
    nohup ./mvnw -q -pl "$name" spring-boot:run > "logs/$name.log" 2>&1 &
    disown
  done
  for entry in "${INFRA[@]}"; do
    wait_healthy "${entry%%:*}" "${entry##*:}"
  done

  echo "Starting the 5 bounded-context services + portal..."
  for entry in "${SERVICES[@]}"; do
    name="${entry%%:*}"
    nohup ./mvnw -q -pl "$name" spring-boot:run > "logs/$name.log" 2>&1 &
    disown
  done
  for entry in "${SERVICES[@]}"; do
    wait_healthy "${entry%%:*}" "${entry##*:}"
  done

  wait_discovery

  urls
}

stop() {
  for entry in "${ALL[@]}"; do
    name="${entry%%:*}"
    port="${entry##*:}"
    pids=$(lsof -ti:"$port" 2>/dev/null || true)
    if [ -n "$pids" ]; then
      echo "Stopping $name (:$port)..."
      kill $pids 2>/dev/null || true
    fi
  done
  sleep 2
  for entry in "${ALL[@]}"; do
    port="${entry##*:}"
    pids=$(lsof -ti:"$port" 2>/dev/null || true)
    [ -n "$pids" ] && kill -9 $pids 2>/dev/null || true
  done
  echo "Stopped."
}

status() {
  for entry in "${ALL[@]}"; do
    name="${entry%%:*}"
    port="${entry##*:}"
    hport="$(health_port "$name" "$port")"
    if curl -s "http://localhost:$hport/actuator/health" 2>/dev/null | grep -q '"status":"UP"'; then
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
