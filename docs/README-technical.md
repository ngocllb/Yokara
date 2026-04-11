# Yokara Mobile Automation — Hướng dẫn kỹ thuật

Dành cho dev/QA automation đã quen Java, Maven, Appium, CI.

## Mục tiêu

- Framework **TestNG + Appium** (Java 17, Maven) cho **Android & iOS**.
- Chạy **local** (thiết bị USB) và **CI** (Jenkins, song song nhiều thiết bị).

## Kiến trúc (rút gọn)

| Thư mục / thành phần | Vai trò |
|---------------------|---------|
| `core/ConfigManager` | Đọc cấu hình: ưu tiên **`-D`** → **ENV** → `config/config.properties`. |
| `core/DeviceManager` | Phát hiện thiết bị Android (`adb`) / iOS (`idevice_id`, `tidevice`), Simulator. |
| `core/DriverFactory` | Tạo `AndroidDriver` / `IOSDriver`, gán **port** (systemPort, WDA, MJPEG) tránh xung đột. |
| `base/BaseDriver` | Setup/teardown session, `activateApp`, ổn định màn hình, recovery khi cần. |
| `pages/` | Page Object. |
| `tests/` | Test case TestNG. |
| `listeners/AllureListener` | Gắn metadata Allure (platform, UDID, slot/port Jenkins nếu có). |

## Yêu cầu công cụ

- **JDK 17**, **Maven** 3.8+ (`java -version`, `mvn -v`).
- **Appium 2** + drivers: `appium driver install uiautomator2`, `appium driver install xcuitest`.
- **Android:** Android SDK, `adb`, biến `ANDROID_HOME` / `ANDROID_SDK_ROOT`.
- **iOS thật:** Xcode, `idevice_id` (libimobiledevice), tùy chọn `tidevice`; Team ID / ký WDA khi cần.

## Cấu hình

- File chính: `config/config.properties`.
- Key thường dùng: `appiumServer`, `platform`, `android.appPackage`, `android.appActivity`, `ios.bundleId`, cổng base cho parallel.

## TestNG — file suite nào khi nào?

| File | Khi nào |
|------|---------|
| `testng-multidevice.xml` | **Mặc định** trong `pom.xml`: hai `<test>` Android rồi iOS (tuần tự). |
| `testng-jenkins.xml` | **Jenkins**: mỗi lần `mvn` một platform (`-Dplatform=android` hoặc `ios`). |
| `testng.xml` | Suite tối giản (tùy chọn). |

## Chạy local

```bash
# Appium (ví dụ)
appium --address 127.0.0.1 --port 4723

# Mặc định (multidevice)
mvn test

# Một platform, kiểu Jenkins
mvn test -DsuiteXmlFile=testng-jenkins.xml -Dplatform=android \
  -Dandroid.udid=<UDID> -DappiumServer=http://127.0.0.1:4723
```

## Jenkins

- Pipeline: `Jenkinsfile`.
- Discover thiết bị: `scripts/get_jenkins_devices.py`.
- Truyền `-DsuiteXmlFile=testng-jenkins.xml` và `-Dplatform`, UDID, Appium URL, port theo matrix.

## Allure

- Thư mục kết quả do Surefire + property `allure.results.directory` quyết định (xem `pom.xml`).
- Trên Jenkins có thể gộp nhiều máy và sinh HTML theo từng device (theo cấu hình pipeline).

## Lưu ý vận hành

- **Một UDID** không nên phục vụ hai session Appium đồng thời (local + Jenkins).
- Máy **vừa Jenkins vừa dev**: tránh chạy local trùng thiết bị/port với job đang chạy.

## Ghi chú Appium Inspector (Android) — ví dụ capability

```json
{
  "platformName": "Android",
  "automationName": "UiAutomator2",
  "deviceName": "Android Device",
  "udid": "<adb devices>",
  "appPackage": "com.yokara.v3",
  "appActivity": "com.yokara.v3.MainActivity",
  "noReset": true,
  "autoGrantPermissions": true,
  "ignoreHiddenApiPolicyError": true
}
```

- `udid`: `adb devices`
- `noReset: true`: giữ trạng thái đăng nhập, không gỡ app mỗi lần chạy.
