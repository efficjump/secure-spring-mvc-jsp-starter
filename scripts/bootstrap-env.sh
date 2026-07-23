#!/usr/bin/env sh
set -eu

project_root=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
env_file="$project_root/.env"

if [ -f "$env_file" ]; then
    printf '%s\n' ".env already exists; leaving it unchanged."
    exit 0
fi

if ! command -v openssl >/dev/null 2>&1; then
    printf '%s\n' "openssl is required to generate local secrets." >&2
    exit 1
fi

random_secret() {
    openssl rand -hex 32
}

umask 077
db_password=$(random_secret)
db_root_password=$(random_secret)
admin_password=$(random_secret)

{
    printf 'COMPOSE_PROJECT_NAME=%s\n' "${COMPOSE_PROJECT_NAME:-secure-web-starter}"
    printf 'MARIADB_IMAGE=%s\n' "${MARIADB_IMAGE:-mariadb:12.3.2}"
    printf 'MAVEN_IMAGE=%s\n' "${MAVEN_IMAGE:-maven:3.9.11-eclipse-temurin-21}"
    printf 'RUNTIME_IMAGE=%s\n' "${RUNTIME_IMAGE:-eclipse-temurin:21-jre-noble}"
    printf 'DB_NAME=%s\n' "${DB_NAME:-webapp}"
    printf 'DB_USERNAME=%s\n' "${DB_USERNAME:-webapp}"
    printf 'DB_PASSWORD=%s\n' "$db_password"
    printf 'DB_ROOT_PASSWORD=%s\n' "$db_root_password"
    printf 'DB_BIND_ADDRESS=%s\n' "${DB_BIND_ADDRESS:-127.0.0.1}"
    printf 'DB_PORT=%s\n' "${DB_PORT:-3306}"
    printf 'DB_SLOW_QUERY_SECONDS=%s\n' "${DB_SLOW_QUERY_SECONDS:-1}"
    printf 'APP_BIND_ADDRESS=%s\n' "${APP_BIND_ADDRESS:-127.0.0.1}"
    printf 'APP_PORT=%s\n' "${APP_PORT:-8080}"
    printf 'APP_BOOTSTRAP_ADMIN_ENABLED=%s\n' "${APP_BOOTSTRAP_ADMIN_ENABLED:-true}"
    printf 'APP_BOOTSTRAP_ADMIN_USERNAME=%s\n' "${APP_BOOTSTRAP_ADMIN_USERNAME:-admin}"
    printf 'APP_BOOTSTRAP_ADMIN_EMAIL=%s\n' "${APP_BOOTSTRAP_ADMIN_EMAIL:-admin@localhost.invalid}"
    printf 'APP_BOOTSTRAP_ADMIN_PASSWORD=%s\n' "$admin_password"
    printf 'APP_REGISTRATION_ENABLED=%s\n' "${APP_REGISTRATION_ENABLED:-false}"
    printf 'APP_I18N_DEFAULT_LOCALE=%s\n' "${APP_I18N_DEFAULT_LOCALE:-ko}"
    printf 'APP_I18N_SUPPORTED_LOCALES=%s\n' "${APP_I18N_SUPPORTED_LOCALES:-ko,en}"
    printf 'APP_I18N_COOKIE_NAME=%s\n' "${APP_I18N_COOKIE_NAME:-APP_LOCALE}"
    printf 'APP_I18N_COOKIE_PATH=%s\n' "${APP_I18N_COOKIE_PATH:-/}"
    printf 'APP_I18N_COOKIE_MAX_AGE=%s\n' "${APP_I18N_COOKIE_MAX_AGE:-365d}"
    printf 'APP_I18N_COOKIE_SAME_SITE=%s\n' "${APP_I18N_COOKIE_SAME_SITE:-Lax}"
    printf 'APP_I18N_COOKIE_SECURE=%s\n' "${APP_I18N_COOKIE_SECURE:-false}"
    printf 'APP_I18N_RESPECT_ACCEPT_LANGUAGE=%s\n' "${APP_I18N_RESPECT_ACCEPT_LANGUAGE:-true}"
    printf 'APP_SQL_LOG_LEVEL=%s\n' "${APP_SQL_LOG_LEVEL:-DEBUG}"
    printf 'APP_SQL_BIND_LOG_LEVEL=%s\n' "${APP_SQL_BIND_LOG_LEVEL:-OFF}"
    printf 'APP_DB_DRIVER_LOG_LEVEL=%s\n' "${APP_DB_DRIVER_LOG_LEVEL:-OFF}"
} > "$env_file"

chmod 600 "$env_file"
printf '%s\n' "Created $env_file with generated database and bootstrap-admin secrets."
printf '%s\n' "Read the initial admin password from .env, change it after first login, then disable bootstrap."
