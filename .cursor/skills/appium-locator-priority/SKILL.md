---
name: appium-locator-priority
description: Prioritizes Appium locators (accessibility id → id → xpath) for Yokara Android/iOS. Requires not assuming element locations or inventing user flows; on flow/screen blockers, stop checks and report for user-assisted inspection. Follow existing project code style; do not reorganize folders without instruction. Use for Page Objects, locators, flow debugging, or Appium discipline.
---

# Nguyên tắc bắt element (Android + iOS)

## Hành vi bắt buộc (không chỉ locator)

- **Không giả định location**: Không đoán xpath, tọa độ, hierarchy hoặc “chắc là nút ở đây” khi chưa có bằng chứng từ inspector, log, hoặc xác nhận từ người có quyền truy cập app/thiết bị. Nếu thiếu thông tin — hỏi hoặc dừng và báo cáo (xem dưới).
- **Không tưởng tượng luồng**: Không bịa bước màn hình, thứ tự điều hướng, điều kiện hiển thị, hay nhánh A/B khi chưa được mô tả trong spec, testcase, hoặc code/test hiện có. Chỉ mô phỏng luồng đã được xác định rõ.
- **Khi vướng trong một luồng hoặc một màn hình**: **Dừng ngay** việc thêm/sửa phương thức check (assert/click/wait…) một cách mù quáng. **Đưa báo cáo ngắn gọn**: màn hình/luồng, bước đang làm, lỗi hoặc không tìm thấy element, đã thử gì — để người dùng có thể **truy cập** (thiết bị, inspector, build đúng) và **bắt locator / xác nhận hành vi** cho chính xác rồi mới tiếp tục.
- **Tuân theo nguyên tắc code của dự án hiện tại**: Đặt tên, package, Page Object, Base, wait/implicit, annotation Appium — bám theo mẫu đã có trong repo; không áp pattern ngoài khi chưa thống nhất.
- **Không tự tiện sắp xếp lại thư mục**: Không đổi cấu trúc `src`, package, nhóm page object trừ khi được yêu cầu rõ ràng.

## Thứ tự bắt buộc (locator)

1. **Accessibility ID** — ưu tiên cao nhất  
2. **ID** — khi không đủ hoặc không có accessibility id phù hợp  
3. **XPath và các phương thức khác** — chỉ khi hai cách trên không khả thi; xem là phương án cuối

## 1. Accessibility ID (ưu tiên 1)

- **Khi dùng**: Dev đã gán `content-desc` (Android) / `accessibilityIdentifier` hoặc label phù hợp (iOS) — locator ổn định, gần với hành vi người dùng và accessibility.
- **Appium**: `By.accessibilityId(...)` hoặc `@AndroidFindBy(accessibility = "...")` / `@iOSXCUITFindBy(accessibility = "...")` tùy framework Page Factory.
- **Lưu ý**: Ưu tiên **một** giá trị dùng được trên cả hai nền tảng khi có thể; nếu khác nhau, tách theo platform trong page object (không nhảy sang xpath sớm).

## 2. ID (ưu tiên 2)

- **Android**: `resource-id` (ví dụ `com.yokara.app:id/...`) — ổn định hơn xpath khi id do app định nghĩa rõ.
- **iOS**: thường gắn với thuộc tính `name` / định danh phần tử trong XCUITest (theo convention dự án); dùng khi không có accessibility id hoặc cần phân biệt phần tử trùng label.
- **Appium**: `By.id(...)` hoặc annotation `id` tương ứng driver — **không** nhầm với accessibility id.

## 3. XPath và phương thức khác (ưu tiên cuối)

- **XPath**: dễ gãy khi đổi layout, hierarchy; thường chậm hơn — chỉ dùng khi không có id/accessibility id, hoặc cần quan hệ cha-con đặc biệt.
- **Khác**: class name, tag, image name, predicate string, UIAutomator chain… — áp dụng tương tự: **chỉ khi không còn lựa chọn tốt hơn**.

## Quy tắc khi implement

- Thêm element mới: thử **accessibility id** trước → **id** → mới tới xpath/cách khác.
- Refactor: nếu có thể thay xpath bằng accessibility id hoặc id (sau khi confirm với build), nên thay.
- Ghi chú trong code (ngắn) nếu bắt buộc dùng xpath: lý do (ví dụ dynamic list, không có id).

## Checklist nhanh

**Locator**

```
- [ ] Đã có accessibility id (cả nền hoặc tách platform)?
- [ ] Nếu không — đã thử id (resource-id Android / name iOS)?
- [ ] Chỉ dùng xpath hoặc phương thức khác khi hai bước trên thất bại?
```

**Khi vướng (luồng / màn hình / element)**

```
- [ ] Đã dừng thêm check mù quáng; đã báo cáo đủ để user inspect?
- [ ] Không giả định location hay bịa thêm bước luồng?
- [ ] Thay đổi code vẫn bám pattern file/Page Object lân cận trong dự án?
```

## Tài liệu thêm

- Chi tiết convention annotation/driver của dự án: xem code Page Object hiện có trong `src/test/java/pages/`.
