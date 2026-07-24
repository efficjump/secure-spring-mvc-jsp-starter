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
smoke_locale="${APP_I18N_DEFAULT_LOCALE:-en}"
locale_cookie_name="${APP_I18N_COOKIE_NAME:-APP_LOCALE}"
locale_url="$base_url/locale?lang=$smoke_locale&returnTo=%2Flogin"
cookie_jar=$(mktemp)
trap 'rm -f -- "$cookie_jar"' EXIT

curl --fail --silent --show-error "$health_url" >/dev/null
headers=$(curl --silent --show-error --dump-header - --output /dev/null "$login_url")

printf '%s' "$headers" | grep -qi '^Content-Security-Policy:'
printf '%s' "$headers" | grep -qi '^Permissions-Policy:'
printf '%s' "$headers" | grep -qi '^Referrer-Policy:'
printf '%s' "$headers" | grep -qi '^X-Content-Type-Options: nosniff'
printf '%s' "$headers" | grep -qi '^X-Frame-Options: SAMEORIGIN'
printf '%s' "$headers" | grep -qi '^Cache-Control:.*no-store'

locale_headers=$(curl --silent --show-error --cookie-jar "$cookie_jar" --dump-header - --output /dev/null "$locale_url")
printf '%s' "$locale_headers" | grep -qi '^Location: /login'
printf '%s' "$locale_headers" | grep -qi "^Set-Cookie: $locale_cookie_name=$smoke_locale;.*HttpOnly;.*SameSite="

localized_login=$(curl --fail --silent --show-error --cookie "$cookie_jar" "$login_url")
printf '%s' "$localized_login" | grep -Fq "<html lang=\"$smoke_locale\">"

printf '%s\n' "Smoke test passed: health, security headers, JSP rendering, and locale persistence are available."
