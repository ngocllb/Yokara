# Yokara Mobile Automation

Automation Appium + TestNG + Maven cho **Android** và **iOS** (đa thiết bị, Jenkins).

## Tài liệu

| Đối tượng | File |
|-----------|------|
| Người có nền kỹ thuật (Java/Maven/Appium/CI) | [docs/README-technical.md](docs/README-technical.md) |
| Người chưa chuyên sâu tech / low-code | [docs/README-non-technical.md](docs/README-non-technical.md) |

---

## Ghi chú nhanh (công cụ & chạy thử)

### Công cụ cần thiết

- Java 17
- Apache Maven 3.8+
- Appium (`npm install -g appium`) — khuyến nghị Appium 2
- Node.js
- Appium drivers: `appium driver install uiautomator2`, `appium driver install xcuitest`
- Android SDK (`adb devices`), Android Studio (tùy chọn)
- Appium Inspector
- Allure Commandline (`npm install -g allure-commandline`) để xem report
- IDE: IntelliJ IDEA

### Ví dụ capability Android (Appium Inspector / JSON)

```json
{
  "platformName": "Android",
  "automationName": "UiAutomator2",
  "deviceName": "Android Device",
  "udid": "4c039a0d",
  "appPackage": "com.yokara.v3",
  "appActivity": "com.yokara.v3.MainActivity",
  "noReset": true,
  "autoGrantPermissions": true,
  "ignoreHiddenApiPolicyError": true
}
```

- `udid`: lấy từ `adb devices`
- `appPackage` / activity: có thể tra khi app đang mở (tùy môi trường Windows/macOS)
- `noReset: true`: không gỡ app mỗi lần chạy, giữ trạng thái đăng nhập
- `autoGrantPermissions`: tự cấp quyền khi cần
- `ignoreHiddenApiPolicyError`: giảm lỗi policy trên Android 10+

### Các bước chạy nhanh

1. Bật Appium server (ví dụ):

   ```bash
   appium --address 127.0.0.1 --port 4723
   ```

2. Chạy test:

   ```bash
   mvn clean test
   ```

3. Xem Allure (thư mục results theo `pom` / `-Dallure.results.directory`):

   ```bash
   allure serve allure-results
   ```

Chi tiết suite TestNG, Jenkins, cấu hình: xem [docs/README-technical.md](docs/README-technical.md).
