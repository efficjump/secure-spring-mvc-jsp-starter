#!/bin/sh
set -eu

mode=${1:-repository}

if ! repo_root=$(git rev-parse --show-toplevel 2>/dev/null); then
    echo "[public-check] Git 저장소에서 실행해 주세요." >&2
    exit 2
fi

cd "$repo_root"

case "$mode" in
    staged)
        grep_scope="--cached"
        ;;
    repository)
        grep_scope=""
        ;;
    *)
        echo "사용법: $0 {staged|repository}" >&2
        exit 2
        ;;
esac

paths_file=$(mktemp "${TMPDIR:-/tmp}/public-release-paths.XXXXXX")
messages_file=$(mktemp "${TMPDIR:-/tmp}/public-release-messages.XXXXXX")
trap 'rm -f "$paths_file" "$messages_file"' EXIT HUP INT TERM

if [ "$mode" = "staged" ]; then
    git diff --cached --name-only --diff-filter=ACMR > "$paths_file"
else
    git ls-files > "$paths_file"
fi

failed=0

while IFS= read -r path; do
    [ -n "$path" ] || continue

    case "$path" in
        .env|.env.*)
            if [ "$path" != ".env.example" ]; then
                echo "[public-check] 환경 비밀 파일은 커밋할 수 없습니다: $path" >&2
                failed=1
            fi
            ;;
        .DS_Store|*/.DS_Store|target/*|outputs/*|logs/*|work/*|.m2-cache/*|*.war|*.jar|*.tar|*.tar.gz|*.zip|*.sql.gz|*.dump|*.pem|*.key|*.p12|*.pfx|*.jks)
            echo "[public-check] 로컬·비밀·생성 파일은 커밋할 수 없습니다: $path" >&2
            failed=1
            ;;
    esac

    if git cat-file -e ":$path" 2>/dev/null; then
        size=$(git cat-file -s ":$path")
        if [ "$size" -gt 5242880 ]; then
            echo "[public-check] 5 MiB를 넘는 파일은 별도 검토가 필요합니다: $path" >&2
            failed=1
        fi
    fi
done < "$paths_file"

if [ -n "$grep_scope" ]; then
    if git grep "$grep_scope" -nI -E '(/Users/[^/[:space:]]+|/home/[^/[:space:]]+|[A-Za-z]:\\Users\\[^\\[:space:]]+)' -- . ':(exclude)scripts/public-release-check.sh' >/dev/null 2>&1; then
        echo "[public-check] 개인 워크스테이션 절대 경로가 포함되어 있습니다." >&2
        failed=1
    fi
    if git grep "$grep_scope" -nI -E '(-----BEGIN [A-Z0-9 ]*PRIVATE KEY-----|AKIA[0-9A-Z]{16}|gh[pousr]_[A-Za-z0-9_]{20,}|github_pat_[A-Za-z0-9_]{20,})' -- . ':(exclude)scripts/public-release-check.sh' >/dev/null 2>&1; then
        echo "[public-check] 비밀값으로 보이는 문자열이 포함되어 있습니다." >&2
        failed=1
    fi
else
    if git grep -nI -E '(/Users/[^/[:space:]]+|/home/[^/[:space:]]+|[A-Za-z]:\\Users\\[^\\[:space:]]+)' -- . ':(exclude)scripts/public-release-check.sh' >/dev/null 2>&1; then
        echo "[public-check] 개인 워크스테이션 절대 경로가 포함되어 있습니다." >&2
        failed=1
    fi
    if git grep -nI -E '(-----BEGIN [A-Z0-9 ]*PRIVATE KEY-----|AKIA[0-9A-Z]{16}|gh[pousr]_[A-Za-z0-9_]{20,}|github_pat_[A-Za-z0-9_]{20,})' -- . ':(exclude)scripts/public-release-check.sh' >/dev/null 2>&1; then
        echo "[public-check] 비밀값으로 보이는 문자열이 포함되어 있습니다." >&2
        failed=1
    fi
fi

deny_file=$(git rev-parse --git-path info/public-release-deny-patterns)
if [ -s "$deny_file" ]; then
    if [ -n "$grep_scope" ]; then
        if git grep "$grep_scope" -nI -i -F -f "$deny_file" -- . >/dev/null 2>&1; then
            echo "[public-check] 비공개 차단 목록의 문자열이 포함되어 있습니다." >&2
            failed=1
        fi
    elif git grep -nI -i -F -f "$deny_file" -- . >/dev/null 2>&1; then
        echo "[public-check] 비공개 차단 목록의 문자열이 포함되어 있습니다." >&2
        failed=1
    fi

    git log --format=%B > "$messages_file" 2>/dev/null || :
    if grep -iF -f "$deny_file" "$messages_file" >/dev/null 2>&1; then
        echo "[public-check] 커밋 메시지에 비공개 차단 목록의 문자열이 포함되어 있습니다." >&2
        failed=1
    fi
fi

if ! git -c core.whitespace=-blank-at-eof diff --cached --check >/dev/null; then
    echo "[public-check] 스테이징 내용에 공백 오류가 있습니다." >&2
    failed=1
fi

if [ "$failed" -ne 0 ]; then
    echo "[public-check] 공개 안전성 검사가 실패했습니다." >&2
    exit 1
fi

echo "[public-check] 공개 안전성 검사를 통과했습니다 ($mode)."
