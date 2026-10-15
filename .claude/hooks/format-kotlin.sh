#!/usr/bin/env bash
# Stop hook: 이번 턴에 수정된 .kt 파일이 있을 때만 ./gradlew ktlintFormat 을 실행한다.
# - 포맷만 한다(위반 보고는 CI의 ktlintCheck 몫). 자동으로 못 고치는 위반은 그대로 남는다.
# - 실패해도 작업을 막지 않는다. Stop 훅에서 exit 2를 쓰면 Claude가 멈추지 못하므로 항상 exit 0.
# - 마지막 실행 시각을 스탬프 파일에 기록해, 그 이후 수정된 .kt가 있을 때만 다시 실행한다.
set -u

cd "${CLAUDE_PROJECT_DIR:-.}" || exit 0
stamp=".claude/format-kotlin.stamp"

[ -f ./gradlew ] || exit 0

if [ -f "$stamp" ]; then
  changed=$(find . \( -name .git -o -name .gradle -o -name build \) -prune -o -name '*.kt' -newer "$stamp" -print -quit)
else
  # 첫 실행: 스탬프가 없으므로 git이 보는 .kt 변경(수정·신규)을 기준으로 한다.
  changed=$(git status --porcelain -- '*.kt' 2>/dev/null | head -n 1)
fi

[ -n "$changed" ] || exit 0

if ! output=$(bash ./gradlew ktlintFormat -q --console=plain 2>&1); then
  echo "ktlintFormat 실패(작업은 계속됨). CI의 ktlintCheck가 최종 확인한다." >&2
  echo "$output" | tail -n 5 >&2
fi

# 포맷이 바꾼 파일이 다음 실행에서 '새 변경'으로 잡히지 않도록 실행 후에 기록한다.
touch "$stamp"
exit 0
