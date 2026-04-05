#!/usr/bin/env bash
# Daily HC BOX: phát hiện mọi máy USB → chạy song song → tách Allure theo thiết bị.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

echo "[run-hc-box-daily] Project: $ROOT"
mvn -B test -Phc-box

python3 "$ROOT/scripts/split-allure-by-device.py"

echo ""
echo "Allure gốp (có filter theo label device):  allure serve allure-results"
echo "Allure từng máy:  allure serve allure-results-by-device/<tên_thư_mục>"
