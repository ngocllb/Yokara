
1. Phân chia cấu trúc POM

2. Các công cụ cần thiết
- Java 17
- Apache Maven 3.11.0
- Appium (npm install -g appium) - verison (9.0.0)
- NodeJS
- Appium Driver Android UiAutomator2 (appium driver install uiautomator2)
- Adroid SDK (adb devices)
- Android Studio
- Appium Inspector 
- Allure Commandline (npm install -g allure-commandline) để gene report
- IDE - IntelliJ
- 

3. Cách lấy thông số để chạy Appium Spector (JSON Representation)

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

- udid -> adb devices
- package -> adb shell dumpsys window | findstr mCurrentFocus (mở app đã rồi chạy)
- "noReset": true => không reset app, không uninstall, giữ login state
- "autoGrantPermissions": true => tự động grant permission khi install app
- "ignoreHiddenApiPolicyError": true => android 10+ có các lỗi về policy, nên cần thêm phần này để có thể access được khi spec app

hoặc search chatGPT 1 script CMD giúp tự động generate toàn bộ Appium capability của device

4. Các bước chạy
Cmd : Appium => lấy server (vd:  http://127.0.0.1:4723)
cmd: appium --address 127.0.0.1 --port 4723
Bật Selenium inspector sau  chạy Json
cmd: mvn clean test để test thử bộ testcase
cmd: allure serve allure-results => gene report Allure
