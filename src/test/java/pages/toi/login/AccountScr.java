package pages.toi.login;

import base.BaseScr;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.ios.IOSDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebElement;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import utils.GestureUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Màn chọn tài khoản đã lưu (sau Đăng nhập khi có TK lưu).
 *
 * Android: giữ nguyên locator/flow hiện tại.
 * iOS (WDA): ưu tiên resolve đúng node có thể điều hướng UI được cho action
 * "Đăng nhập bằng tài khoản khác".
 */
public class AccountScr extends BaseScr {

    private static final String TEXT_TAI_KHOAN_KHAC = "Đăng nhập bằng tài khoản khác";
    private static final String ID_PATTERN_XPATH =
            "//*[contains(@name,'ID: ') or contains(@label,'ID: ') or contains(@content-desc,'ID: ')]";

    /**
     * Chung Android + iOS — vẫn giữ để không đổi flow hiện tại.
     */
    public static final By BTN_TAI_KHOAN_KHAC = AppiumBy.accessibilityId(TEXT_TAI_KHOAN_KHAC);

    /**
     * iOS exact node theo XML:
     * XCUIElementTypeStaticText value/name/label đều bằng đúng full text,
     * visible=true, accessible=true.
     */
    private static final By IOS_BTN_TAI_KHOAN_KHAC_EXACT_PREDICATE =
            AppiumBy.iOSNsPredicateString(
                    "type == 'XCUIElementTypeStaticText' AND visible == 1 AND enabled == 1 AND ("
                            + "name == '" + TEXT_TAI_KHOAN_KHAC + "' OR "
                            + "label == '" + TEXT_TAI_KHOAN_KHAC + "' OR "
                            + "value == '" + TEXT_TAI_KHOAN_KHAC + "')"
            );

    /**
     * Fallback exact XPath cho đúng node StaticText.
     */
    private static final By IOS_BTN_TAI_KHOAN_KHAC_EXACT_XPATH =
            AppiumBy.xpath(
                    "//XCUIElementTypeStaticText[@visible='true' and ("
                            + "@name='" + TEXT_TAI_KHOAN_KHAC + "' or "
                            + "@label='" + TEXT_TAI_KHOAN_KHAC + "' or "
                            + "@value='" + TEXT_TAI_KHOAN_KHAC + "')]"
            );

    /**
     * Fallback rộng hơn nếu build thay đổi type nhưng vẫn là element able để điều hướng.
     */
    private static final By IOS_BTN_TAI_KHOAN_KHAC_FALLBACK_PREDICATE =
            AppiumBy.iOSNsPredicateString(
                    "visible == 1 AND enabled == 1 AND "
                            + "(name CONTAINS[c] 'tài khoản khác' OR "
                            + " label CONTAINS[c] 'tài khoản khác' OR "
                            + " value CONTAINS[c] 'tài khoản khác') AND "
                            + "(type == 'XCUIElementTypeStaticText' OR "
                            + " type == 'XCUIElementTypeButton' OR "
                            + " type == 'XCUIElementTypeLink' OR "
                            + " type == 'XCUIElementTypeOther')"
            );

    /**
     * Fallback cuối cùng: bắt mọi node có text tương ứng.
     */
    private static final By IOS_BTN_TAI_KHOAN_KHAC_ANY =
            AppiumBy.xpath(
                    "//*[contains(@name,'" + TEXT_TAI_KHOAN_KHAC + "') "
                            + "or contains(@label,'" + TEXT_TAI_KHOAN_KHAC + "') "
                            + "or contains(@value,'" + TEXT_TAI_KHOAN_KHAC + "')]"
            );

    private static final List<By> IOS_TAI_KHOAN_KHAC_LOCATORS = List.of(
            IOS_BTN_TAI_KHOAN_KHAC_EXACT_PREDICATE,
            IOS_BTN_TAI_KHOAN_KHAC_EXACT_XPATH,
            BTN_TAI_KHOAN_KHAC,
            IOS_BTN_TAI_KHOAN_KHAC_FALLBACK_PREDICATE,
            IOS_BTN_TAI_KHOAN_KHAC_ANY
    );

    public AccountScr(AppiumDriver driver) {
        super(driver);
    }

    /**
     * mvn test -Daccount.ios.debug=true
     */
    private static boolean accountIosDebug() {
        return "true".equalsIgnoreCase(System.getProperty("account.ios.debug", "false"));
    }

    private void iosDbg(String fmt, Object... args) {
        if (accountIosDebug()) {
            System.err.println("[AccountScr-iOS] " + String.format(fmt, args));
        }
    }

    private void dumpAccountPageSource(String reason) {
        try {
            Path out = Path.of("target/account-ios-taikhoankhac.xml");
            Files.createDirectories(out.getParent());
            String src = driver.getPageSource();
            Files.writeString(out, src, StandardCharsets.UTF_8);
            iosDbg("Đã ghi page source (%d ký tự) → %s (%s)", src.length(), out.toAbsolutePath(), reason);
        } catch (IOException e) {
            iosDbg("Không ghi dump: %s", e.getMessage());
        }
    }

    public static boolean isAccountScr(AppiumDriver driver) {
        return isAccountScr(driver, 0);
    }

    public static boolean isAccountScr(AppiumDriver driver, int timeoutSec) {
        if (timeoutSec <= 0) {
            boolean found = !driver.findElements(AppiumBy.xpath(ID_PATTERN_XPATH)).isEmpty();
            if (found) {
                System.out.println("[DEBUG] AccountScr detected via ID pattern (fast)");
            }
            return found;
        }
        try {
            new WebDriverWait(driver, Duration.ofSeconds(timeoutSec))
                    .until(ExpectedConditions.presenceOfElementLocated(AppiumBy.xpath(ID_PATTERN_XPATH)));
            System.out.println("[DEBUG] AccountScr detected via ID pattern (wait)");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Chọn account đã lưu theo UID — giữ nguyên flow Android/iOS cũ.
     */
    public void selectAccountByUID(String uid) {
        if (uid == null || uid.isBlank()) {
            throw new IllegalArgumentException("uid không được rỗng");
        }
        String e = escXPath(uid.trim());
        By row = byPlatform(
                driver,
                AppiumBy.xpath("//android.view.View[contains(@content-desc, 'ID: " + e + "')]"),
                AppiumBy.xpath("//*[contains(@name,'ID: " + e + "') or contains(@label,'ID: " + e + "')]")
        );
        select(row);
    }

    /**
     * Nút “Đăng nhập bằng tài khoản khác”.
     */
    public void selectAnotherMethodLogin() {
        if (driver instanceof IOSDriver) {
            iosTapTaiKhoanKhac();
            waitForIosAccountNavigationAway();
        } else {
            select(BTN_TAI_KHOAN_KHAC);
        }
    }

    /**
     * Sau tap, DOM có thể giữ node cũ trong chốc lát.
     * Không đổi flow: chỉ wait mềm để màn sau có cơ hội lên.
     */
    private void waitForIosAccountNavigationAway() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(12)).until(d -> {
                if (isLoginMethodScreenHintPresent()) {
                    return true;
                }
                for (By by : IOS_TAI_KHOAN_KHAC_LOCATORS) {
                    List<WebElement> els = d.findElements(by);
                    if (els.isEmpty()) {
                        return true;
                    }
                    boolean anyVisible = false;
                    for (WebElement el : els) {
                        if (iosIntersectsViewport(el)) {
                            anyVisible = true;
                            break;
                        }
                    }
                    if (!anyVisible) {
                        return true;
                    }
                }
                return false;
            });
        } catch (TimeoutException ignored) {
            // Giữ nguyên behavior: flow sau tự wait màn mới
        }
    }

    /**
     * Hint nhẹ cho màn sau, không phụ thuộc cứng.
     */
    private boolean isLoginMethodScreenHintPresent() {
        List<By> hints = List.of(
                AppiumBy.accessibilityId("Đăng nhập"),
                AppiumBy.accessibilityId("Đăng ký"),
                AppiumBy.accessibilityId("Số điện thoại"),
                AppiumBy.accessibilityId("Facebook"),
                AppiumBy.accessibilityId("Google"),
                AppiumBy.accessibilityId("Apple")
        );
        for (By by : hints) {
            try {
                if (!driver.findElements(by).isEmpty()) {
                    return true;
                }
            } catch (Exception ignored) {
                // ignore
            }
        }
        return false;
    }

    /**
     * iOS/WDA:
     * - ưu tiên exact element theo XML
     * - chỉ tap element đang ở viewport và có khả năng điều hướng UI
     * - không đổi luồng business
     */
    private void iosTapTaiKhoanKhac() {
        iosDbg("Bắt đầu tìm '%s', số locator=%d", TEXT_TAI_KHOAN_KHAC, IOS_TAI_KHOAN_KHAC_LOCATORS.size());
        iosLogLocatorSnapshot(IOS_TAI_KHOAN_KHAC_LOCATORS, "trước resolve");

        try {
            new WebDriverWait(driver, Duration.ofSeconds(8)).until(d -> findBestIosTaiKhoanKhacElement() != null);
        } catch (TimeoutException ignored) {
            iosDbg("Chưa resolve được element ngay — tiếp tục scroll/resolve");
        }

        WebElement el = findBestIosTaiKhoanKhacElement();
        if (el == null) {
            iosScrollUntilTaiKhoanKhacVisible();
            el = findBestIosTaiKhoanKhacElement();
        }

        if (el == null) {
            iosLogLocatorSnapshot(IOS_TAI_KHOAN_KHAC_LOCATORS, "resolve fail");
            dumpAccountPageSource("không resolve được element tài khoản khác");
            throw new RuntimeException("iOS: không resolve được 'Đăng nhập bằng tài khoản khác'");
        }

        iosLogElementBrief(el, "tap target");
        iosTapWithFallbacks(el);
    }

    /**
     * Resolve element tốt nhất để tap:
     * 1. exact visible node
     * 2. exact node sau khi scroll to visible
     * 3. fallback visible node
     */
    private WebElement findBestIosTaiKhoanKhacElement() {
        for (By by : IOS_TAI_KHOAN_KHAC_LOCATORS) {
            List<WebElement> els = safeFindElements(by);
            if (els.isEmpty()) {
                continue;
            }

            // ưu tiên node đang nằm trong viewport
            for (WebElement el : els) {
                if (iosIsGoodTapTarget(el)) {
                    iosDbg("Resolve tốt: %s", by);
                    return el;
                }
            }

            // nếu có node nhưng chưa nằm gọn viewport, thử kéo đúng node đó ra
            for (WebElement el : els) {
                if (iosLooksLikeTaiKhoanKhacNode(el)) {
                    iosScrollElementToVisible(el);
                    if (iosIsGoodTapTarget(el)) {
                        iosDbg("Resolve tốt sau scroll to visible: %s", by);
                        return el;
                    }
                }
            }

            // refetch sau scroll
            List<WebElement> refetched = safeFindElements(by);
            for (WebElement el : refetched) {
                if (iosIsGoodTapTarget(el)) {
                    iosDbg("Resolve tốt sau refetch: %s", by);
                    return el;
                }
            }
        }
        return null;
    }

    private void iosScrollUntilTaiKhoanKhacVisible() {
        for (int i = 0; i < 10; i++) {
            WebElement el = findBestIosTaiKhoanKhacElement();
            if (el != null) {
                return;
            }
            iosDbg("Scroll vòng %d/10 để tìm '%s'", i + 1, TEXT_TAI_KHOAN_KHAC);
            swipeUp();
        }
        dumpAccountPageSource("scroll hết vòng mà chưa thấy tài khoản khác");
    }

    private List<WebElement> safeFindElements(By by) {
        try {
            return driver.findElements(by);
        } catch (Exception e) {
            iosDbg("findElements lỗi với locator %s: %s", by, e.getMessage());
            return List.of();
        }
    }

    private boolean iosLooksLikeTaiKhoanKhacNode(WebElement el) {
        String joined = (safeAttr(el, "name") + " | " + safeAttr(el, "label") + " | " + safeAttr(el, "value"))
                .toLowerCase();
        return joined.contains("đăng nhập bằng tài khoản khác") || joined.contains("tài khoản khác");
    }

    /**
     * Tap target chuẩn cho WDA:
     * - visible / nằm trong viewport
     * - enabled
     * - đúng text cần tìm
     */
    private boolean iosIsGoodTapTarget(WebElement el) {
        if (el == null) {
            return false;
        }
        if (!iosLooksLikeTaiKhoanKhacNode(el)) {
            return false;
        }
        if (!iosIntersectsViewport(el)) {
            return false;
        }
        try {
            String enabled = safeAttr(el, "enabled");
            if ("false".equalsIgnoreCase(enabled) || "0".equals(enabled)) {
                return false;
            }
        } catch (Exception ignored) {
            // ignore
        }
        return true;
    }

    private void iosLogLocatorSnapshot(List<By> locators, String phase) {
        for (int i = 0; i < locators.size(); i++) {
            By by = locators.get(i);
            List<WebElement> els = safeFindElements(by);
            iosDbg("[%s] locator[%d] %s → count=%d", phase, i, by, els.size());
            for (int j = 0; j < Math.min(els.size(), 3); j++) {
                iosLogElementBrief(els.get(j), "  [" + j + "]");
            }
        }
    }

    private void iosLogElementBrief(WebElement e, String label) {
        try {
            org.openqa.selenium.Rectangle r = e.getRect();
            iosDbg(
                    "%s rect=(%d,%d %dx%d) displayed=%s enabled=%s name=%s labelText=%s value=%s tag=%s",
                    label,
                    r.getX(), r.getY(), r.getWidth(), r.getHeight(),
                    safeDisplayed(e),
                    safeAttr(e, "enabled"),
                    safeAttr(e, "name"),
                    safeAttr(e, "label"),
                    safeAttr(e, "value"),
                    e.getClass().getSimpleName()
            );
        } catch (Exception ex) {
            iosDbg("%s (không đọc được rect/info: %s)", label, ex.getMessage());
        }
    }

    /**
     * Viewport check an toàn hơn cho iOS.
     */
    private boolean iosIntersectsViewport(WebElement e) {
        try {
            if (e.isDisplayed()) {
                org.openqa.selenium.Rectangle r = e.getRect();
                if (r.getHeight() <= 0 || r.getWidth() <= 0) {
                    return false;
                }
                Dimension win = driver.manage().window().getSize();
                int top = 20;
                int bottom = win.getHeight() - 20;
                int left = 0;
                int right = win.getWidth();
                int ex1 = r.getX();
                int ey1 = r.getY();
                int ex2 = r.getX() + r.getWidth();
                int ey2 = r.getY() + r.getHeight();
                return ex2 > left && ex1 < right && ey2 > top && ey1 < bottom;
            }
        } catch (Exception ignored) {
            // fallback theo rect
        }

        try {
            org.openqa.selenium.Rectangle r = e.getRect();
            if (r.getHeight() <= 0 || r.getWidth() <= 0) {
                return false;
            }
            Dimension win = driver.manage().window().getSize();
            int top = 20;
            int bottom = win.getHeight() - 20;
            int ry2 = r.getY() + r.getHeight();
            return ry2 > top && r.getY() < bottom;
        } catch (Exception ex) {
            return false;
        }
    }

    private boolean safeDisplayed(WebElement e) {
        try {
            return e.isDisplayed();
        } catch (Exception ignored) {
            return false;
        }
    }

    private String safeAttr(WebElement e, String attr) {
        try {
            String v = e.getAttribute(attr);
            return v == null ? "" : v;
        } catch (Exception ignored) {
            return "";
        }
    }

    /**
     * Một số build WDA click StaticText không ổn định.
     * Ưu tiên:
     * 1. WebElement.click()
     * 2. mobile: clickGesture với elementId
     * 3. W3C tap tâm phần tử
     * 4. mobile: clickGesture theo tọa độ
     */
    private void iosTapWithFallbacks(WebElement el) {
        Exception last = null;

        try {
            el.click();
            iosDbg("Tap OK: WebElement.click()");
            return;
        } catch (Exception ex) {
            iosDbg("WebElement.click() lỗi: %s", ex.getMessage());
            last = ex;
        }

        try {
            iosClickGestureOnElement(el);
            iosDbg("Tap OK: mobile: clickGesture (elementId)");
            return;
        } catch (Exception ex) {
            iosDbg("clickGesture elementId lỗi: %s", ex.getMessage());
            last = ex;
        }

        try {
            GestureUtils.tapElementCenter(driver, el);
            iosDbg("Tap OK: W3C pointer tại tâm phần tử");
            return;
        } catch (Exception ex) {
            iosDbg("W3C tapElementCenter lỗi: %s", ex.getMessage());
            last = ex;
        }

        try {
            org.openqa.selenium.Rectangle r = el.getRect();
            int x = r.getX() + Math.max(1, r.getWidth()) / 2;
            int y = r.getY() + Math.max(1, r.getHeight()) / 2;
            Map<String, Object> p = new HashMap<>();
            p.put("x", x);
            p.put("y", y);
            driver.executeScript("mobile: clickGesture", p);
            iosDbg("Tap OK: mobile: clickGesture xy=(%d,%d)", x, y);
            return;
        } catch (Exception ex) {
            iosDbg("clickGesture tọa độ lỗi: %s", ex.getMessage());
            last = ex;
        }

        dumpAccountPageSource("iosTapWithFallbacks thất bại");
        throw new RuntimeException("iOS: không tap được 'Đăng nhập bằng tài khoản khác'", last);
    }

    private void iosClickGestureOnElement(WebElement el) {
        Map<String, Object> p = new HashMap<>();
        p.put("elementId", ((RemoteWebElement) el).getId());
        driver.executeScript("mobile: clickGesture", p);
    }

    private void iosScrollElementToVisible(WebElement el) {
        try {
            Map<String, Object> p = new HashMap<>();
            p.put("elementId", ((RemoteWebElement) el).getId());
            p.put("toVisible", true);
            driver.executeScript("mobile: scroll", p);
            iosDbg("Đã gọi mobile: scroll toVisible cho element");
        } catch (Exception ex) {
            iosDbg("mobile: scroll toVisible lỗi: %s", ex.getMessage());
        }
    }

    /**
     * Chọn account đầu tiên trong danh sách.
     * Giữ nguyên flow.
     */
    public void selectFirstAccount() {
        By first = byPlatform(
                driver,
                AppiumBy.xpath("(//android.view.View[contains(@content-desc,'ID: ')])[1]"),
                AppiumBy.xpath("(//*[contains(@name,'ID: ') or contains(@label,'ID: ')])[1]")
        );
        if (driver.findElements(first).isEmpty()) {
            throw new RuntimeException("Không thấy account đã lưu (pattern ID: ) trên màn Account");
        }
        select(first);
    }

    /**
     * Danh sách các dòng account.
     */
    public List<WebElement> findSavedAccountRows() {
        if (driver instanceof IOSDriver) {
            return driver.findElements(
                    AppiumBy.xpath("//*[contains(@name,'ID: ') or contains(@label,'ID: ')]")
            );
        }
        return driver.findElements(
                AppiumBy.xpath("//android.view.View[contains(@content-desc,'ID: ')]")
        );
    }

    private static String escXPath(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.replace("'", "''");
    }
}