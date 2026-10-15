#!/usr/bin/env bash
# PR 크기 경고. 리뷰 대상 변경 라인 수(추가 + 삭제)를 세어 임계값을 넘으면 경고만 한다.
# 실패시키지 않는다(항상 exit 0). 경고가 뜨면 PR 템플릿의 "크기 경고 사유"에 쪼개지 못한 이유를 적는다.
#
# 사용: bash .github/scripts/pr-size.sh <base> <head> [threshold]
set -euo pipefail

base="${1:?base ref required}"
head="${2:?head ref required}"
threshold="${3:-500}"

# 집계에서 제외: 테스트(ArchUnit 스냅샷 포함), 생성 코드, lock 파일, gradle wrapper, 설계·규칙 문서
excludes=(
  ':(exclude,glob)**/src/test/**'
  ':(exclude,glob)**/generated/**'
  ':(exclude,glob)**/*.lock'
  ':(exclude,glob)gradle/wrapper/**'
  ':(exclude,glob)docs/**'
)

# 바이너리 파일은 numstat이 "-"로 표시하므로 0으로 센다.
count_lines() {
  git diff --numstat "$base...$head" -- "$@" | awk '$1 != "-" { n += $1 + $2 } END { print n + 0 }'
}

total=$(count_lines .)
reviewed=$(count_lines . "${excludes[@]}")
excluded=$((total - reviewed))

echo "리뷰 대상 ${reviewed}줄 (제외 ${excluded}줄, 전체 ${total}줄, 임계값 ${threshold}줄)"

if [ -n "${GITHUB_STEP_SUMMARY:-}" ]; then
  {
    echo "### PR 크기"
    echo
    echo "| 구분 | 줄 수 |"
    echo "|---|---|"
    echo "| 리뷰 대상 | ${reviewed} |"
    echo "| 제외 (테스트, 생성 코드, lock, gradle wrapper, docs) | ${excluded} |"
    echo "| 임계값 | ${threshold} |"
  } >> "$GITHUB_STEP_SUMMARY"
fi

if [ "$reviewed" -gt "$threshold" ]; then
  msg="리뷰 대상이 ${reviewed}줄로 임계값 ${threshold}줄을 넘었습니다. 슬라이스를 더 쪼갤 수 있는지 확인하고, 쪼개지 못했다면 PR 설명의 '크기 경고 사유'에 이유를 적어주세요."
  echo "::warning title=PR 크기 경고::${msg}"
  if [ -n "${GITHUB_STEP_SUMMARY:-}" ]; then
    printf '\n> **경고**: %s\n' "$msg" >> "$GITHUB_STEP_SUMMARY"
  fi
fi
