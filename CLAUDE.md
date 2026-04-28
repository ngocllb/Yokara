# CLAUDE.md — Yokara Mobile Automation

> File hướng dẫn cho Claude Code khi làm việc trong repo này. Đọc trước khi sinh code / sửa code.

## 1. Bối cảnh dự án

- **Mục tiêu**: Bộ automation Appium chạy được cho **cả Android & iOS**, **tự nhận diện thiết bị**, **chạy parallel** trên nhiều device cắm cùng lúc.
- **CI/CD**: Jenkins tại `192.168.10.130:8080`. Pipeline mặc định gọi profile `hc-box` (sinh suite động theo device đang cắm).
- **Vai trò mặc định khi hỗ trợ**: Senior Automation Tester cho app mobile Yokara (Android + iOS).
- **Ngôn ngữ trả lời**: **Tiếng Việt** cho mọi giải thích / gợi ý. Comment trong code có thể bằng tiếng Việt nếu khu vực xung quanh đang dùng tiếng Việt; còn lại giữ tiếng Anh kỹ thuật.

## 2. Stack & cấu trúc

| Lớp | Đường dẫn | Vai trò |
|---|---|---|
| Build | [pom.xml](pom.xml) | Java 17, Appium 9.0, TestNG 7.9, Allure 2.24, 6 Maven profile |
| Suite | [testng.xml](testng.xml), [testng-multidevice.xml](testng-multidevice.xml), [testng-jenkins.xml](testng-jenkins.xml) | Cấu hình suite TestNG |
| Pipeline | [Jenkinsfile](Jenkinsfile) | Detect device → parallel → Allure split per-device |
| Config | [config/config.properties](config/config.properties) | Platform, port base, app id, override qua `-D…` hoặc `YOKARA_*` env |
| Multi-device core | [src/test/java/core/](src/test/java/core/) | `DriverFactory`, `DeviceManager`, `ConfigManager`, iOS helpers |
| Base | [src/test/java/base/](src/test/java/base/) | `BaseDriver` (TestNG hooks), `BaseScr` (PageFactory), `BottomNav` |
| Page Objects | [src/test/java/pages/](src/test/java/pages/) | 22 màn hình theo tab: `tructuyen/`, `toi/`, `hat/`, `trangchu/`, `tinnhan/` |
| Flow nghiệp vụ | [src/test/java/flows/](src/test/java/flows/) | `AuthFlow`, `CreateRoomEntryFlow`, `SocialLoginHandler` |
| Listener / utils | [src/test/java/listeners/](src/test/java/listeners/), [src/test/java/utils/](src/test/java/utils/) | Allure hook, screenshot, wait, gesture, step |
| Test cases | [src/test/java/tests/](src/test/java/tests/) | Hiện có `RoomTest.java` |
| Tools sinh suite | [src/test/java/tools/GenerateHcBoxSuite.java](src/test/java/tools/GenerateHcBoxSuite.java) | Sinh `target/testng-hc-box.xml` từ device đang cắm |

## 3. Lệnh hay dùng

```bash
# Chạy suite mặc định (multidevice)
mvn clean test

# Chạy theo profile
mvn clean test -Psmoke-parallel
mvn clean test -Phc-box                # sinh suite theo device đang cắm
mvn clean test -Ploginmethod-ios-allure

# Override config bằng -D
mvn test -Dplatform=android -Dandroid.udid=4c039a0d
mvn test -DappiumServer=http://127.0.0.1:4723

# Override bằng env var (ưu tiên thấp hơn -D)
YOKARA_ANDROID_UDID=4c039a0d YOKARA_PLATFORM=android mvn test

# Mở Allure
allure serve allure-results
```

## 4. Nguyên tắc bắt element (BẮT BUỘC)

Tham khảo đầy đủ: [.cursor/skills/appium-locator-priority/SKILL.md](.cursor/skills/appium-locator-priority/SKILL.md)

**Thứ tự ưu tiên:** `accessibility id` → `id` (resource-id Android / name iOS) → `xpath` / khác.

- Trước khi viết locator mới: kiểm tra dump XML trong [scripts/xml_dumps/](scripts/xml_dumps/) hoặc nhờ user dump lại; **không đoán** content-desc, resource-id, hierarchy.
- Khi bắt buộc dùng XPath: ghi chú lý do ngắn gọn ngay trên locator (vd: `// dynamic list, no id`).
- Khác platform: tách `@AndroidFindBy` / `@iOSXCUITFindBy` trong cùng PO; **không** fallback xpath sớm chỉ vì lười tách.
- Tham khảo mẫu: [src/test/java/base/BaseScr.java](src/test/java/base/BaseScr.java) (`byPlatform`, `byIdThenFallback`).

## 5. Quy tắc viết code

### Convention
- Bám pattern **Page Object** + **Flow** đã có. Page Object **không** chứa assertion phức tạp; assert ở `tests/`.
- Wait: dùng [utils/WaitUtils.java](src/test/java/utils/WaitUtils.java) hoặc `WebDriverWait`. **Không** thêm `Thread.sleep` mới (các chỗ tồn tại như `BottomNav.java:79` là nợ kỹ thuật, đang được dọn dần).
- Step report: bọc hành động trong `StepUtils.step("…", () -> { … })` để Allure có timeline đẹp.
- Screenshot khi fail: đã có ở [listeners/AllureListener.java](src/test/java/listeners/AllureListener.java) — không tự gọi lại trong test.
- Đặt tên test: `test<Tính_năng><Hành_vi>` (vd `testCreatePrivateRoom`).

### Không tự ý
- **Không reorganize thư mục / package** nếu chưa được yêu cầu rõ.
- **Không thêm dependency** mới nếu chưa thống nhất.
- **Không sửa Jenkinsfile** trừ khi yêu cầu cụ thể (file 775 dòng, dễ vỡ pipeline).
- **Không commit** `.idea/`, `.venv/`, `.DS_Store`, `allure-results/`, `target/`. Nếu thấy file này đã bị commit từ trước → báo lại, đừng tự `git rm`.

## 6. Khi bị chặn (BẮT BUỘC)

Nếu vướng ở 1 màn hình / 1 luồng (không tìm được element, app điều hướng khác mong đợi, build sai version…):

1. **Dừng ngay** việc thêm `assert` / `click` / `wait` mù.
2. **Báo cáo ngắn** theo format:
   - Màn hình / luồng đang ở.
   - Bước vừa làm + log/lỗi cụ thể.
   - Đã thử gì (locator nào, wait bao lâu).
   - Cần user làm gì để gỡ (dump lại XML, mở Inspector, cấp build, xác nhận thiết kế).
3. **Không bịa luồng** chưa được mô tả trong spec / testcase / code hiện có.

## 7. Quy trình ưu tiên

Khi user đưa task không rõ scope, phân loại theo workflow tại [docs/WORKFLOW.md](docs/WORKFLOW.md):

- **P0** — Hotfix CI / blocker / repo hygiene gây vỡ build.
- **P1** — Mở rộng coverage, ổn định flaky test, locator hardening.
- **P2** — Refactor, DX, cleanup tooling.

Mặc định ưu tiên P0 trước, không bắt đầu P2 khi còn P0.

## 8. Trước khi báo "xong"

- [ ] `mvn clean test -DskipTests` (compile sạch) hoặc chạy lại đúng profile bị ảnh hưởng.
- [ ] Không thêm `Thread.sleep`, không thêm xpath text-based khi có thể tránh.
- [ ] PO mới có cả Android & iOS locator (hoặc giải thích lý do thiếu).
- [ ] Có cập nhật / không phá `testng*.xml`, `pom.xml` profiles, `Jenkinsfile`.
- [ ] Diff không lẫn file build/IDE (`.DS_Store`, `target/`, `allure-results/`).
