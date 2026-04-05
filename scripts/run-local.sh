#!/usr/bin/env bash
# Chạy automation local (macOS): PATH Homebrew + Node 20 + Appium 4723 + Maven test
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
export PATH="/opt/homebrew/bin:${PATH:-}"

bash "${ROOT}/scripts/xcode-detect-update.sh" || true
if [[ "${YOKARA_AUTO_UPDATE_XCODE:-}" == "1" ]]; then
  echo "[run-local] YOKARA_AUTO_UPDATE_XCODE=1 → thử cập nhật Xcode"
  bash "${ROOT}/scripts/xcode-detect-update.sh" --update || true
fi

if [[ -s "${HOME}/.nvm/nvm.sh" ]]; then
  # shellcheck source=/dev/null
  source "${HOME}/.nvm/nvm.sh"
  nvm use 20.20.2 2>/dev/null || nvm use 20 2>/dev/null || true
fi

if ! curl -sf "http://127.0.0.1:4723/status" >/dev/null; then
  echo "[run-local] Khởi động Appium..."
  ( lsof -ti :4723 | xargs kill -9 2>/dev/null ) || true
  appium --address 127.0.0.1 --port 4723 --log "${ROOT}/appium-ios-run.log" --log-level info &
  for _ in $(seq 1 20); do
    curl -sf "http://127.0.0.1:4723/status" >/dev/null && break
    sleep 1
  done
fi

cd "${ROOT}"
echo "[run-local] mvn clean test (platform theo config/config.properties)"
mvn -B clean test "$@"
