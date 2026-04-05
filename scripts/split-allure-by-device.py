#!/usr/bin/env python3
"""
Tách kết quả Allure theo label 'device' (gắn bởi BaseDriver khi chạy -Phc-box).
Sao chép từng *-result.json + file attachment được tham chiếu vào allure-results-by-device/<tên_thiết_bị>/

Sau đó xem từng thiết bị:
  allure serve allure-results-by-device/android_Pixel_xxx

Hoặc: allure generate allure-results-by-device/<folder> -o report-<folder>
"""
from __future__ import annotations

import glob
import json
import os
import re
import shutil
import sys

BASE = os.environ.get("ALLURE_RESULTS_DIR", "allure-results")
OUT_ROOT = os.environ.get("ALLURE_SPLIT_ROOT", "allure-results-by-device")


def device_from_result(data: dict) -> str | None:
    for lab in data.get("labels") or []:
        if lab.get("name") == "device" and lab.get("value"):
            return str(lab["value"]).strip()
    return None


def attachment_sources_from_raw(text: str) -> set[str]:
    return set(re.findall(r'"source"\s*:\s*"([^"]+)"', text))


def main() -> int:
    if not os.path.isdir(BASE):
        print(f"[split-allure] Không thấy thư mục: {BASE}", file=sys.stderr)
        return 1
    os.makedirs(OUT_ROOT, exist_ok=True)
    n = 0
    for path in glob.glob(os.path.join(BASE, "*-result.json")):
        try:
            raw = open(path, encoding="utf-8").read()
            data = json.loads(raw)
            dev = device_from_result(data)
            if not dev:
                dev = "_unknown_device"
            dest_dir = os.path.join(OUT_ROOT, dev)
            os.makedirs(dest_dir, exist_ok=True)
            base = os.path.basename(path)
            shutil.copy2(path, os.path.join(dest_dir, base))
            for src in attachment_sources_from_raw(raw):
                s = os.path.join(BASE, src)
                if os.path.isfile(s):
                    shutil.copy2(s, os.path.join(dest_dir, os.path.basename(src)))
            n += 1
        except Exception as e:
            print(f"[split-allure] Bỏ qua {path}: {e}", file=sys.stderr)
    # Sao chép file meta chung (nếu có) vào từng thư mục để generate ổn định hơn
    for meta in ("categories.json", "executor.json", "environment.properties"):
        mp = os.path.join(BASE, meta)
        if os.path.isfile(mp):
            for sub in glob.glob(os.path.join(OUT_ROOT, "*")):
                if os.path.isdir(sub):
                    shutil.copy2(mp, os.path.join(sub, meta))
    print(f"[split-allure] Đã phân loại {n} bài test → {OUT_ROOT}/<device>/")
    return 0


if __name__ == "__main__":
    sys.exit(main())
