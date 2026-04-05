package core;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.ios.options.XCUITestOptions;
import io.appium.java_client.ios.options.wda.XcodeCertificate;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Factory khởi tạo AppiumDriver cho Android hoặc iOS.
 * Hỗ trợ cấu hình tường minh (platform=android/ios)
 * và tự động phát hiện (platform=auto).
 */
public class DriverFactory {

    /** Mỗi session Android tăng systemPort — tránh trùng khi song song nhiều máy/session. */
    private static final AtomicInteger ANDROID_SESSION_SLOT = new AtomicInteger(0);
    /** Mỗi session iOS tăng wdaLocalPort / mjpegServerPort. */
    private static final AtomicInteger IOS_SESSION_SLOT = new AtomicInteger(0);

    public static AppiumDriver createDriver() {
        return createDriver(null, null);
    }

    /**
     * Khởi tạo driver với platform và UDID tùy chọn (phục vụ parallel/multi-device).
     */
    public static AppiumDriver createDriver(String platformParam, String udidParam) {
        try {
            String server = ConfigManager.getRequired("appiumServer");
            String platform = (platformParam != null && !platformParam.isBlank())
                    ? platformParam.trim().toLowerCase()
                    : resolvePlatform();

            System.out.println("[DriverFactory] Đang khởi tạo driver cho nền tảng: " + platform.toUpperCase()
                    + (udidParam != null ? " (UDID: " + udidParam + ")" : ""));

            if ("android".equalsIgnoreCase(platform)) {
                return createAndroidDriver(server, udidParam);
            } else if ("ios".equalsIgnoreCase(platform)) {
                return createIOSDriver(server, udidParam);
            } else {
                throw new RuntimeException(
                        "[DriverFactory] Nền tảng không được hỗ trợ: " + platform
                                + ". Giá trị hợp lệ: android, ios, auto");
            }

        } catch (Exception e) {
            throw new RuntimeException("[DriverFactory] Khởi tạo driver thất bại: " + e.getMessage(), e);
        }
    }

    // ================================================================
    //  ANDROID
    // ================================================================

    private static AppiumDriver createAndroidDriver(String server, String udidParam) throws Exception {
        // Ưu tiên: Parameter -> Config -> Auto detect
        String udid = (udidParam != null && !udidParam.isBlank()) ? udidParam : ConfigManager.get("android.udid");
        if (udid == null || udid.isBlank()) {
            udid = DeviceManager.getAndroidUDID();
        } else {
            System.out.println("[DriverFactory] Sử dụng Android UDID: " + udid);
        }

        String deviceName = ConfigManager.get("android.deviceName");
        if (deviceName == null || deviceName.isBlank()) {
            deviceName = udid;
        }

        System.out.println("[DriverFactory] Android UDID=" + udid + " | DeviceName=" + deviceName);

        UiAutomator2Options options = new UiAutomator2Options();
        options.setAutomationName("UiAutomator2");
        options.setPlatformName("Android");
        options.setDeviceName(deviceName);
        options.setUdid(udid);
        options.setAppPackage(ConfigManager.getRequired("android.appPackage"));
        options.setAppActivity(ConfigManager.getRequired("android.appActivity"));
        options.setNoReset(false);

        // Ổn định trên Android 13/14
        options.setAutoGrantPermissions(true);
        options.setIgnoreHiddenApiPolicyError(true);
        options.setDisableWindowAnimation(true);
        options.setSkipDeviceInitialization(true);

        int systemPort = nextAndroidSystemPort();
        options.setSystemPort(systemPort);
        System.out.println("[DriverFactory] Android systemPort=" + systemPort + " (tránh conflict session song song)");

        return new AndroidDriver(new URL(server), options);
    }

    // ================================================================
    //  iOS
    // ================================================================

    private static AppiumDriver createIOSDriver(String server, String udidParam) throws Exception {
        XcodeProbe.logSummaryIfEnabled();
        // Ưu tiên: Parameter -> Config -> Auto detect
        String udid = (udidParam != null && !udidParam.isBlank()) ? udidParam : ConfigManager.get("ios.udid");
        if (udid == null || udid.isBlank()) {
            String detected = DeviceManager.resolveIosUdid(false);
            if (detected == null) {
                throw new RuntimeException("[DriverFactory] Không tìm thấy thiết bị iOS (USB/Simulator). "
                        + "Cài Xcode, bật Simulator hoặc kết nối iPhone; có thể chỉ định ios.udid / ios.target=simulator.");
            }
            udid = detected;
        } else {
            System.out.println("[DriverFactory] Sử dụng iOS UDID: " + udid);
        }

        boolean sim = isLikelySimulatorUdid(udid);
        String deviceName = ConfigManager.get("ios.deviceName");
        if (deviceName == null || deviceName.isBlank()) {
            if (!sim) {
                deviceName = IosDeviceInfo.queryDeviceName(udid);
            }
            if (deviceName == null || deviceName.isBlank()) {
                deviceName = sim ? "iPhone Simulator" : "iPhone";
            }
        }

        String platformVersion = ConfigManager.get("ios.platformVersion");
        if (!sim) {
            String fromDevice = IosDeviceInfo.queryProductVersionNormalized(udid);
            if (fromDevice != null) {
                platformVersion = fromDevice;
            }
        }

        System.out.println("[DriverFactory] iOS UDID=" + udid + " | DeviceName=" + deviceName
                + (sim ? " | loại=Simulator" : " | loại=Thiết bị thật")
                + (platformVersion != null && !platformVersion.isBlank()
                        ? " | iOS=" + platformVersion : ""));

        XCUITestOptions options = new XCUITestOptions();
        options.setAutomationName("XCUITest");
        options.setPlatformName("iOS");
        options.setUdid(udid);
        options.setDeviceName(deviceName);
        if (platformVersion != null && !platformVersion.isBlank()) {
            options.setPlatformVersion(platformVersion);
        }
        applyIosAppOrBundle(options);
        options.setNoReset(parseBool(ConfigManager.get("ios.noReset"), false));
        if (!parseBool(ConfigManager.get("ios.forceAppLaunch"), true)) {
            options.setForceAppLaunch(false);
        }

        if (!sim) {
            options.setAllowProvisioningDeviceRegistration(true);
        }

        options.setWdaLaunchTimeout(Duration.ofSeconds(180));
        options.setWdaConnectionTimeout(Duration.ofSeconds(180));
        if ("true".equalsIgnoreCase(ConfigManager.get("ios.showXcodeLog"))) {
            options.setShowXcodeLog(true);
        }

        String team = ConfigManager.get("ios.xcodeOrgId");
        if (team == null || team.isBlank()) {
            team = IosEnvHelper.detectFirstAppleTeamId();
        }
        if (team != null && !team.isBlank()) {
            String signingId = ConfigManager.get("ios.xcodeSigningId");
            if (signingId == null || signingId.isBlank()) {
                signingId = "iPhone Developer";
            }
            options.setXcodeCertificate(new XcodeCertificate(team, signingId));
            String wdaBundle = ConfigManager.get("ios.wda.bundleId");
            if (wdaBundle == null || wdaBundle.isBlank()) {
                wdaBundle = "com.yokara.WebDriverAgentRunner";
            }
            options.setUpdatedWdaBundleId(wdaBundle);
        }

        int wdaPort = nextIosWdaPort();
        int mjpegPort = wdaPort + 2000;
        options.setWdaLocalPort(wdaPort);
        options.setMjpegServerPort(mjpegPort);
        System.out.println("[DriverFactory] iOS wdaLocalPort=" + wdaPort + " mjpegServerPort=" + mjpegPort
                + " (tránh conflict WDA song song)");

        return new IOSDriver(new URL(server), options);
    }

    // ================================================================
    //  HELPER: xác định platform
    // ================================================================

    /**
     * Xác định nền tảng từ cấu hình hoặc tự động phát hiện.
     * - "android" → dùng Android
     * - "ios"     → dùng iOS
     * - "auto" hoặc không cấu hình → tự động phát hiện qua DeviceManager
     */
    private static String resolvePlatform() {
        String platform = ConfigManager.get("platform");

        if (platform == null || platform.isBlank() || "auto".equalsIgnoreCase(platform)) {
            System.out.println("[DriverFactory] platform=auto → Đang tự động phát hiện thiết bị...");
            return DeviceManager.detectPlatform();
        }

        return platform.trim().toLowerCase();
    }

    private static boolean isLikelySimulatorUdid(String udid) {
        if (udid == null) {
            return false;
        }
        return udid.matches(
                "[0-9A-Fa-f]{8}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{12}");
    }

    private static boolean parseBool(String v, boolean def) {
        if (v == null || v.isBlank()) {
            return def;
        }
        return "true".equalsIgnoreCase(v.trim()) || "1".equals(v.trim());
    }

    private static void applyIosAppOrBundle(XCUITestOptions options) {
        String raw = ConfigManager.get("ios.app");
        if (raw != null && !raw.isBlank()) {
            Path app = Paths.get(expandUserHome(raw.trim()));
            if (Files.isDirectory(app) && app.getFileName().toString().endsWith(".app")) {
                options.setApp(app.toAbsolutePath().toString());
                System.out.println("[DriverFactory] Dùng ios.app: " + app.toAbsolutePath());
                return;
            }
            System.out.println("[DriverFactory] ios.app không hợp lệ hoặc chưa build: " + app);
        }
        options.setBundleId(ConfigManager.getRequired("ios.bundleId"));
    }

    private static String expandUserHome(String path) {
        if (path.startsWith("~/")) {
            return System.getProperty("user.home") + path.substring(1);
        }
        return path;
    }

    private static int nextAndroidSystemPort() {
        int base = parseInt(ConfigManager.get("android.systemPort.base"), 8300);
        int slot = ANDROID_SESSION_SLOT.incrementAndGet();
        return base + slot;
    }

    private static int nextIosWdaPort() {
        int base = parseInt(ConfigManager.get("ios.wdaLocalPort.base"), 8100);
        int slot = IOS_SESSION_SLOT.incrementAndGet();
        return base + slot * 10;
    }

    private static int parseInt(String raw, int defaultValue) {
        if (raw == null || raw.isBlank()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}