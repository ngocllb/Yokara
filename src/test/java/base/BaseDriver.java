package base;

import core.ConfigManager;
import core.DeviceManager;
import core.DriverFactory;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.InteractsWithApps;
import io.appium.java_client.ios.IOSDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import flows.AuthFlow;
import io.qameta.allure.Allure;
import utils.StepContext;

import java.time.Duration;
import java.util.List;

public class BaseDriver {

    /**
     * Mỗi luồng TestNG (parallel tests) có driver riêng — tránh ghi đè khi chạy Android + iOS cùng lúc.
     */
    private static final ThreadLocal<AppiumDriver> DRIVER = new ThreadLocal<>();

    protected AppiumDriver driver;
    protected WebDriverWait wait;
    protected AuthFlow auth;

    @BeforeMethod(alwaysRun = true)
    @Parameters({ "suitePlatform", "suiteUdid", "suiteDeviceLabel", "suiteDeviceFolder" })
    public void setup(@Optional String suitePlatform, @Optional String suiteUdid,
                      @Optional String suiteDeviceLabel, @Optional String suiteDeviceFolder) {

        // Pre-check: bỏ qua nếu platform được chỉ định nhưng không có thiết bị tương ứng
        if ("android".equalsIgnoreCase(suitePlatform)) {
            List<String> online = DeviceManager.getAndroidPhysicalDevices();
            if (online.isEmpty()) {
                throw new SkipException("[Skip] Không có thiết bị Android nào kết nối – bỏ qua test này.");
            }
            if (suiteUdid != null && !suiteUdid.isBlank() && !online.contains(suiteUdid.trim())) {
                throw new SkipException("[Skip] Android UDID không còn online: " + suiteUdid);
            }
        }
        if ("ios".equalsIgnoreCase(suitePlatform)) {
            List<String> online = DeviceManager.getIOSDevices();
            if (online.isEmpty()) {
                throw new SkipException("[Skip] Không có thiết bị iOS nào kết nối – bỏ qua test này.");
            }
            if (suiteUdid != null && !suiteUdid.isBlank() && !online.contains(suiteUdid.trim())) {
                throw new SkipException("[Skip] iOS UDID không còn online: " + suiteUdid);
            }
        }

        if (suiteDeviceFolder != null && !suiteDeviceFolder.isBlank()) {
            Allure.label("device", suiteDeviceFolder.trim());
        }
        if (suiteDeviceLabel != null && !suiteDeviceLabel.isBlank()) {
            Allure.parameter("Thiết bị", suiteDeviceLabel.trim());
        }

        try {
            driver = DriverFactory.createDriver(suitePlatform, suiteUdid);
            DRIVER.set(driver);
        } catch (RuntimeException e) {
            String msg = e.getMessage() != null ? e.getMessage() : "";
            if (msg.contains("Không tìm thấy thiết bị") || msg.contains("unknown")) {
                throw new SkipException("[Skip] Không thể khởi tạo driver: " + msg);
            }
            throw e;
        }

        if (driver == null) {
            throw new RuntimeException("Driver initialization failed!");
        }

        if (driver instanceof IOSDriver) {
            try {
                String bid = ConfigManager.getRequired("ios.bundleId");
                ((InteractsWithApps) driver).activateApp(bid);
            } catch (Exception e) {
                System.out.println("[BaseDriver] iOS activateApp: " + e.getMessage());
            }
        }

        wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        auth = new AuthFlow(driver);

        // Xử lý các popup khi vừa mở app (EULA, Permission)
        new BaseScr(driver).handleStartupPopups();
        ensureIosMainTabBarVisible();
    }

    /**
     * iOS + {@code noReset=true} / {@code forceAppLaunch=false}: session đôi khi mở không phải màn 5 tab
     * → không tìm thấy {@code accessibilityId("Tôi")}. Thử terminate + activate một lần để về root có bottom bar.
     */
    private void ensureIosMainTabBarVisible() {
        if (!(driver instanceof IOSDriver)) {
            return;
        }
        By trangChu = AppiumBy.accessibilityId("Trang chủ");
        By toi = AppiumBy.accessibilityId("Tôi");
        WebDriverWait tabWait = new WebDriverWait(driver, Duration.ofSeconds(12));
        try {
            tabWait.until(d -> !d.findElements(trangChu).isEmpty() || !d.findElements(toi).isEmpty());
            return;
        } catch (TimeoutException e) {
            System.out.println("[BaseDriver] iOS: chưa thấy thanh tab sau activateApp — thử terminate + activate");
        }
        try {
            String bid = ConfigManager.getRequired("ios.bundleId");
            InteractsWithApps app = (InteractsWithApps) driver;
            app.terminateApp(bid);
            // Thread.sleep(2000) removed to follow "No sleep" rule. 
            // Appium's activateApp usually handles the restart well.
            app.activateApp(bid);
            new BaseScr(driver).handleStartupPopups();
            WebDriverWait afterRelaunch = new WebDriverWait(driver, Duration.ofSeconds(25));
            afterRelaunch.until(d -> !d.findElements(trangChu).isEmpty() || !d.findElements(toi).isEmpty());
        } catch (Exception ex) {
            throw new RuntimeException(
                    "[BaseDriver] iOS: không đưa app về màn có bottom bar (Trang chủ / Tôi): " + ex.getMessage(), ex);
        }
    }

    public static AppiumDriver getDriver() {
        AppiumDriver d = DRIVER.get();
        if (d == null) {
            throw new IllegalStateException("Không có AppiumDriver trong luồng hiện tại (chưa @BeforeMethod hoặc đã teardown).");
        }
        return d;
    }

    /**
     * Screenshot khi fail: {@link listeners.AllureListener} + {@link utils.StepUtils}.
     * Ở đây chỉ đóng driver và xóa ngữ cảnh bước.
     */
    @AfterMethod(alwaysRun = true)
    public void teardown() {

        try {
            if (driver != null) {
                driver.quit();
            }
        } catch (Exception e) {
            System.out.println("Driver quit failed: " + e.getMessage());
        } finally {
            DRIVER.remove();
            driver = null;
            StepContext.clear();
        }
    }
}
