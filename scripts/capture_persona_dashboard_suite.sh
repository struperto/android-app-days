#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ADB_BIN="${ADB_BIN:-$HOME/Library/Android/sdk/platform-tools/adb}"
PACKAGE="com.struperto.androidappdays"
ACTIVITY="${PACKAGE}/.MainActivity"
OUT_DIR="${1:-$ROOT_DIR/tmp/persona-visual-suite}"

PERSONAS=(
  "early-athlete"
  "busy-parent"
  "night-owl-creative"
  "desk-pm"
  "recovery-week"
  "nutrition-optimizer"
  "freelance-switcher"
  "shift-worker"
  "minimal-manual"
  "overloaded-lead"
)

WINDOWS=("vormittag" "mittag" "abend")

mkdir -p "$OUT_DIR"
rm -f "$OUT_DIR"/*.png

cd "$ROOT_DIR"
./gradlew installDebug >/dev/null

"$ADB_BIN" wait-for-device
for persona in "${PERSONAS[@]}"; do
  for window in "${WINDOWS[@]}"; do
    "$ADB_BIN" shell am start -S -W \
      -n "$ACTIVITY" \
      --es debug_persona_id "$persona" \
      --es debug_window_id "$window" >/dev/null
    sleep 2
    "$ADB_BIN" exec-out screencap -p >"$OUT_DIR/${persona}_${window}.png"
  done
done

last_persona="${PERSONAS[${#PERSONAS[@]}-1]}"
"$ADB_BIN" shell am start -W \
  -n "$ACTIVITY" \
  --es debug_persona_id "$last_persona" \
  --es debug_window_id "abend" >/dev/null

echo "Saved screenshots to $OUT_DIR"
