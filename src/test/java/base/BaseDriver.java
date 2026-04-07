package base;

import core.ConfigManager;
import core.DeviceManager;
import core.DriverFactory;
import flows.AuthFlow;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.InteractsWithApps;
import io.appium.java_client.ios.IOSDriver;
import io.qameta.allure.Allure;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import utils.StepContext;

import java.time.Duration;
import java.util.List;

public class BaseDriver {

    private static final ThreadLocal<AppiumDriver> DRIVER = new ThreadLocal<>();

    protected AppiumDriver driver;
    protected WebDriverWait wait;
    protected AuthFlow auth;

    @BeforeMethod(alwaysRun = true)
    @Parameters({"suitePlatform", "suiteUdid", "suiteDeviceLabel", "suiteDeviceFolder"})
    public void setup(@Optional String suitePlatform,
                      @Optional String suiteUdid,
                      @Optional String suiteDeviceLabel,
                      @Optional String suiteDeviceFolder) {
        String runPlatform = normalizePlatform(System.getProperty("platform"));
        String runAndroidUdid = normalizeRaw(System.getProperty("android.udid"));
        String runIosUdid = normalizeRaw(System.getProperty("ios.udid"));

        String requestedPlatform = normalizePlatform(suitePlatform);
        String requestedUdid = normalizeRaw(suiteUdid);
        if (requestedPlatform == null) {
            requestedPlatform = runPlatform;
        }
        if (runPlatform != null && requestedPlatform != null && !runPlatform.equals(requestedPlatform)) {
            throw new SkipException(String.format(
                    "[Skip] Invocation platform=%s không thuộc branch hiện tại platform=%s",
                    requestedPlatform, runPlatform
            ));
        }
        if (requestedUdid == null) {
            if ("android".equals(requestedPlatform)) {
                requestedUdid = runAndroidUdid;
            } else if ("ios".equals(requestedPlatform)) {
                requestedUdid = runIosUdid;
            }
        }

        if (requestedPlatform == null) {
            throw new SkipException("[Skip] Không xác định được platform cho test invocation hiện tại.");
        }
        if ("android".equals(requestedPlatform)) {
            List<String> online = DeviceManager.getAndroidPhysicalDevices();
            if (online.isEmpty()) {
                throw new SkipException("[Skip] Không có thiết bị Android nào kết nối – bỏ qua test này.");
            }
            if (requestedUdid != null && !containsIgnoreCase(online, requestedUdid)) {
                throw new SkipException("[Skip] Android UDID không còn online: " + requestedUdid);
            }
        } else if ("ios".equals(requestedPlatform)) {
            List<String> online = DeviceManager.getIOSDevices();
            if (online.isEmpty()) {
                throw new SkipException("[Skip] Không có thiết bị iOS nào kết nối – bỏ qua test này.");
            }
            if (requestedUdid != null && !containsIgnoreCase(online, requestedUdid)) {
                throw new SkipException("[Skip] iOS UDID không còn online: " + requestedUdid);
            }
        } else {
            throw new SkipException("[Skip] Platform không hợp lệ: " + requestedPlatform);
        }

        if (suiteDeviceFolder != null && !suiteDeviceFolder.isBlank()) {
            Allure.label("device", suiteDeviceFolder.trim());
        }
        if (suiteDeviceLabel != null && !suiteDeviceLabel.isBlank()) {
            Allure.parameter("Thiết bị", suiteDeviceLabel.trim());
        }

        Allure.parameter("Run Platform", requestedPlatform);
        if (requestedUdid != null) {
            Allure.parameter("Run UDID", requestedUdid);
        }

        try {
            driver = DriverFactory.createDriver(requestedPlatform, requestedUdid);
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

        new BaseScr(driver).handleStartupPopups();
        handleIosLaunchBannerIfPresent();
        ensureIosMainTabBarVisible();
    }

    private String normalizePlatform(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed.toLowerCase();
    }

    private String normalizeRaw(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private boolean containsIgnoreCase(List<String> values, String target) {
        if (target == null) {
            return false;
        }
        for (String value : values) {
            if (value != null && value.equalsIgnoreCase(target)) {
                return true;
            }
        }
        return false;
    }

    private void ensureIosMainTabBarVisible() {
        if (!(driver instanceof IOSDriver)) {
            return;
        }
        By trangChu = AppiumBy.accessibilityId("Trang chủ");
        By toi = AppiumBy.accessibilityId("Tôi");
        WebDriverWait tabWait = new WebDriverWait(driver, Duration.ofSeconds(22));
        try {
            tabWait.until(d -> !d.findElements(trangChu).isEmpty() || !d.findElements(toi).isEmpty());
            waitIosTabBarSettled();
            return;
        } catch (TimeoutException e) {
            System.out.println("[BaseDriver] iOS: chưa thấy thanh tab sau activateApp — thử terminate + activate");
        }
        try {
            String bid = ConfigManager.getRequired("ios.bundleId");
            InteractsWithApps app = (InteractsWithApps) driver;
            app.terminateApp(bid);
            app.activateApp(bid);
            new BaseScr(driver).handleStartupPopups();
            handleIosLaunchBannerIfPresent();
            WebDriverWait afterRelaunch = new WebDriverWait(driver, Duration.ofSeconds(35));
            afterRelaunch.until(d -> !d.findElements(trangChu).isEmpty() || !d.findElements(toi).isEmpty());
            waitIosTabBarSettled();
        } catch (Exception ex) {
            throw new RuntimeException(
                    "[BaseDriver] iOS: không đưa app về màn có bottom bar (Trang chủ / Tôi): " + ex.getMessage(), ex);
        }
    }

    private void handleIosLaunchBannerIfPresent() {
        if (!(driver instanceof IOSDriver)) {
            return;
        }
        By btnBoQua = AppiumBy.accessibilityId("Bỏ qua");
        try {
            List<org.openqa.selenium.WebElement> skips = driver.findElements(btnBoQua);
            if (skips.isEmpty()) {
                return;
            }
            org.openqa.selenium.WebElement skip = skips.get(0);
            try {
                skip.click();
            } catch (Exception clickEx) {
                driver.executeScript(
                        "mobile: clickGesture",
                        java.util.Map.of("elementId", ((org.openqa.selenium.remote.RemoteWebElement) skip).getId())
                );
            }
            System.out.println("[BaseDriver] iOS: đã dismiss banner bằng nút 'Bỏ qua'");
            new WebDriverWait(driver, Duration.ofSeconds(8)).until(d ->
                    d.findElements(btnBoQua).isEmpty()
                            || !d.findElements(AppiumBy.accessibilityId("Bài hát")).isEmpty()
                            || !d.findElements(AppiumBy.accessibilityId("Song ca")).isEmpty());
        } catch (Exception e) {
            System.out.println("[BaseDriver] iOS: xử lý banner 'Bỏ qua' lỗi: " + e.getMessage());
        }
    }

    private void waitIosTabBarSettled() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(15)).until(d ->
                    !d.findElements(AppiumBy.xpath("//*[contains(@name,'Trực tuyến')]")).isEmpty()
                            || !d.findElements(AppiumBy.accessibilityId("Trực tuyến")).isEmpty());
        } catch (TimeoutException ignored) {
        }
    }

    public static AppiumDriver getDriver() {
        AppiumDriver d = DRIVER.get();
        if (d == null) {
            throw new IllegalStateException("Không có AppiumDriver trong luồng hiện tại.");
        }
        return d;
    }

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