#!/usr/bin/env sh
set -eu

project_root=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
env_file="$project_root/.env"

if [ ! -f "$env_file" ]; then
    printf '%s\n' "Missing .env; run 'make init' first." >&2
    exit 1
fi

set -a
# shellcheck disable=SC1090
. "$env_file"
set +a

base_url="${SMOKE_BASE_URL:-http://${APP_BIND_ADDRESS:-127.0.0.1}:${APP_PORT:-8080}}"
health_url="$base_url/internal/actuator/health"
login_url="$base_url/login"

curl --fail --silent --show-error "$health_url" >/dev/null
headers=$(curl --silent --show-error --dump-header - --output /dev/null "$login_url")

printf '%s' "$headers" | grep -qi '^Content-Security-Policy:'
printf '%s' "$headers" | grep -qi '^Permissions-Policy:'
printf '%s' "$headers" | grep -qi '^Referrer-Policy:'
printf '%s' "$headers" | grep -qi '^X-Content-Type-Options: nosniff'
printf '%s' "$headers" | grep -qi '^X-Frame-Options: DENY'
printf '%s' "$headers" | grep -qi '^Cache-Control:.*no-store'

printf '%s\n' "Smoke test passed: health endpoint and baseline security headers are available."
