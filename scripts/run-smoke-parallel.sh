#!/usr/bin/env bash
# Cần Appium: appium --address 127.0.0.1 --port 4723
set -euo pipefail
cd "$(dirname "$0")/.."
mvn -B test -Psmoke-parallel
