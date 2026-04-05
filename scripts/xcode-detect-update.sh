#!/usr/bin/env bash
# Nhận diện Xcode đang dùng + (tùy chọn) tự cập nhật qua xcodes hoặc mas.
# Chỉ detect:        ./scripts/xcode-detect-update.sh
# Thử update:        ./scripts/xcode-detect-update.sh --update
#                    hoặc YOKARA_AUTO_UPDATE_XCODE=1 ./scripts/run-local.sh
set -u
dev=""
dev="$(xcode-select -p 2>/dev/null)" || true

echo "========== Xcode trên máy này =========="
if [[ -n "$dev" ]]; then
  echo "xcode-select -p: $dev"
else
  echo "xcode-select: (lỗi — cài Xcode, rồi: sudo xcode-select -s /Applications/Xcode.app/Contents/Developer)"
fi

echo ""
echo "xcodebuild -version:"
xcodebuild -version 2>&1 || echo "(lỗi)"

ds=""
if [[ -n "${dev:-}" ]]; then
  ds="${dev%/}/Platforms/iPhoneOS.platform/DeviceSupport"
  echo ""
  echo "DeviceSupport: $ds"
  if [[ -d "$ds" ]]; then
    ls -1 "$ds" 2>/dev/null | head -30 || true
    cnt="$(find "$ds" -maxdepth 1 -type d 2>/dev/null | wc -l | tr -d ' ')"
    echo "(thư mục con: khoảng $((cnt - 1)))"
  else
    echo "(không có thư mục)"
  fi
fi

if [[ "${1:-}" != "--update" ]] && [[ "${YOKARA_AUTO_UPDATE_XCODE:-}" != "1" ]]; then
  echo ""
  echo "Để thử cập nhật Xcode tự động: $0 --update"
  echo "hoặc: brew install xcodesorg/made/xcodes  rồi  xcodes install --latest"
  exit 0
fi

echo ""
echo "========== Thử cập nhật / cài Xcode mới =========="

if command -v xcodes >/dev/null 2>&1; then
  echo "Dùng xcodes (cần Apple ID; có thể lưu trong Keychain sau lần đăng nhập đầu)."
  set +e
  xcodes install --latest
  rc=$?
  set -e
  if [[ "$rc" -eq 0 ]]; then
    echo "xcodes install --latest: xong (kiểm tra /Applications và xcode-select)."
  else
    echo "xcodes install --latest: thoát $rc — xem hướng dẫn: https://github.com/XcodesOrg/xcodes"
  fi
  exit "$rc"
fi

if command -v mas >/dev/null 2>&1; then
  echo "Dùng mas (App Store): nâng/cài Xcode (ID 497799835)."
  set +e
  mas upgrade 497799835 || mas install 497799835
  rc=$?
  set -e
  exit "$rc"
fi

if brew list --cask 2>/dev/null | grep -q '^xcode$'; then
  echo "Dùng Homebrew cask xcode."
  brew upgrade --cask xcode
  exit $?
fi

echo "Không có xcodes / mas / brew cask xcode."
echo "Cài một trong các cách:"
echo "  brew install xcodesorg/made/xcodes && xcodes install --latest"
echo "  brew install mas && mas signin && mas install 497799835"
exit 1
