#!/usr/bin/env bash
# Chỉ iPhone USB; Appium trên 127.0.0.1:4723
set -euo pipefail
cd "$(dirname "$0")/.."
mvn -B test -Pios-audit
