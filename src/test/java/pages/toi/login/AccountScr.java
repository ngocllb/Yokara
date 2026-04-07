package pages.toi.login;

import base.BaseScr;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.ios.IOSDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Màn chọn tài khoản đã lưu (sau Đăng nhập khi có TK lưu).
 * <p>Đối chiếu: {@code XML android Screen locator/AccountScr_android.txt} và {@code scripts/xml_dumps/ios/AccountScr_ios.xml}.</p>
 * <ul>
 *   <li><strong>Hàng tài khoản</strong>: Android {@code View} {@code content-desc} (tên + xuống dòng + {@code ID: &lt;uid&gt;});
 *       iOS {@code XCUIElementTypeOther} {@code name}/{@code label} cùng chuỗi (khác widget, cùng semantics).</li>
 *   <li><strong>Chọn theo UID</strong>: {@code contains(..., 'ID: &lt;uid&gt;')} trên {@code content-desc} (Android) và
 *       {@code name}/{@code label} (iOS) — đã khớp dump.</li>
 *   <li><strong>Đăng nhập bằng tài khoản khác</strong>: Android {@code content-desc}; iOS {@code StaticText} — dùng chung
 *       {@link AppiumBy#accessibilityId} {@code Đăng nhập bằng tài khoản khác}.</li>
 *   <li><strong>Nút Đăng nhập</strong> (xác nhận tài khoản đã chọn): Android {@code Button} + {@code content-desc};
 *       iOS {@code XCUIElementTypeButton} — cùng label; chưa bọc constant trong class này nếu flow chỉ dùng chọn account.</li>
 *   <li>Footer “chính sách / điều khoản”: iOS link chữ thường; Android {@code content-desc} viết hoa — khác chuỗi, không dùng chung locator.</li>
 * </ul>
 * <p>iOS: nếu nút “tài khoản khác” trong vùng cuộn → {@link #scrollToElement}; nếu {@code .click()} không ăn → {@code mobile: clickGesture}.</p>
 */
public class AccountScr extends BaseScr {

    /** Chung Android + iOS — Inspector/Appium map đúng accessibility. */
    public static final By BTN_TAI_KHOAN_KHAC = AppiumBy.accessibilityId("Đăng nhập bằng tài khoản khác");

    /**
     * iOS dump {@code AccountScr_ios.xml}: nút là {@code XCUIElementTypeStaticText} — dùng XPath khi {@code accessibilityId}
     * không tap được (WDA không đánh dấu clickable).
     */
    private static By iosBtnTaiKhoanKhac() {
        return AppiumBy.xpath(
                "//XCUIElementTypeStaticText[contains(@name,'Đăng nhập bằng tài khoản khác') "
                        + "or contains(@label,'Đăng nhập bằng tài khoản khác')]");
    }

    /** Fallback khi XPath/visibility lệch build — predicate trùng dump iOS. */
    private static By iosBtnTaiKhoanKhacPredicate() {
        return AppiumBy.iOSNsPredicateString(
                "type == 'XCUIElementTypeStaticText' AND "
                        + "(label CONTAINS 'Đăng nhập bằng tài khoản khác' OR name CONTAINS 'Đăng nhập bằng tài khoản khác')");
    }

    public AccountScr(AppiumDriver driver) {
        super(driver);
    }

    /**
     * Kiểm tra màn hình hiện tại có phải là AccountScr (màn list TK lưu) hay không.
     * Mặc định gọi với timeout 0 (check nhanh).
     */
    public static boolean isAccountScr(AppiumDriver driver) {
        return isAccountScr(driver, 0);
    }

    /**
     * Kiểm tra màn hình hiện tại với thời gian chờ xác định.
     */
    public static boolean isAccountScr(AppiumDriver driver, int timeoutSec) {
        String xpath = "//*[contains(@name,'ID: ') or contains(@label,'ID: ') or contains(@content-desc,'ID: ')]";
        if (timeoutSec <= 0) {
            boolean found = !driver.findElements(AppiumBy.xpath(xpath)).isEmpty();
            if (found) System.out.println("[DEBUG] AccountScr detected via ID pattern (fast)");
            return found;
        }
        try {
            new WebDriverWait(driver, Duration.ofSeconds(timeoutSec))
                    .until(ExpectedConditions.presenceOfElementLocated(AppiumBy.xpath(xpath)));
            System.out.println("[DEBUG] AccountScr detected via ID pattern (wait)");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Chọn account đã lưu theo UID (vd. 6069820) — content-desc / name chứa chuỗi {@code ID: &lt;uid&gt;}.
     */
    public void selectAccountByUID(String uid) {
        if (uid == null || uid.isBlank()) {
            throw new IllegalArgumentException("uid không được rỗng");
        }
        String e = escXPath(uid.trim());
        By row = byPlatform(
                driver,
                AppiumBy.xpath("//android.view.View[contains(@content-desc, 'ID: " + e + "')]"),
                AppiumBy.xpath(
                        "//*[contains(@name,'ID: " + e + "') or contains(@label,'ID: " + e + "')]"));
        select(row);
    }

    /** Nút “Đăng nhập bằng tài khoản khác”. */
    public void selectAnotherMethodLogin() {
        if (driver instanceof IOSDriver) {
            iosTapTaiKhoanKhac();
            // Đợi nút hiện tại biến mất thực sự (Dynamic Wait) thay vì hard sleep
            waitForInvisibility(iosBtnTaiKhoanKhac());
        } else {
            select(BTN_TAI_KHOAN_KHAC);
        }
    }

    /**
     * iOS: {@link #scrollToElement} cũ dùng {@link #isDisplayed} (có phần tử trong tree là đủ) nên không cuộn tới
     * vùng cuối màn Account; {@code StaticText} thường không clickable theo WDA — tap bằng {@code mobile: clickGesture}.
     * Locator tap: XPath {@link #iosBtnTaiKhoanKhac()} (dump); {@link #BTN_TAI_KHOAN_KHAC} vẫn dùng cho {@link pages.toi.ToiGuestScr#clickLogin}.
     */
    private void iosTapTaiKhoanKhac() {
        By xpath = iosBtnTaiKhoanKhac();
        By predicate = iosBtnTaiKhoanKhacPredicate();
        iosScrollUntilOnScreen(xpath, predicate);
        WebElement el = findIosTaiKhoanKhacElement(xpath, predicate);
        iosTapWithFallbacks(el);
    }

    /**
     * Cuộn tới khi text có trong tree và (hiển thị WDA hoặc rect nằm trong viewport).
     * Danh sách account dài có thể cần nhiều lần {@link #swipeUp}.
     */
    private void iosScrollUntilOnScreen(By xpathLoc, By predicateLoc) {
        WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(1));
        for (int i = 0; i < 22; i++) {
            if (iosTryAcceptScrollTarget(xpathLoc, shortWait) || iosTryAcceptScrollTarget(predicateLoc, shortWait)) {
                return;
            }
            swipeUp();
        }
        throw new RuntimeException(
                "iOS: không thấy 'Đăng nhập bằng tài khoản khác' sau khi cuộn (Account)");
    }

    private boolean iosTryAcceptScrollTarget(By locator, WebDriverWait shortWait) {
        try {
            shortWait.until(d -> {
                List<WebElement> list = d.findElements(locator);
                if (list.isEmpty()) {
                    return false;
                }
                WebElement e = list.get(0);
                return iosIsRoughlyOnScreen(e);
            });
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean iosIsRoughlyOnScreen(WebElement e) {
        try {
            if (e.isDisplayed()) {
                return true;
            }
        } catch (Exception ignored) {
            return false;
        }
        try {
            org.openqa.selenium.Rectangle r = e.getRect();
            if (r.getHeight() <= 0 || r.getWidth() <= 0) {
                return false;
            }
            Dimension win = driver.manage().window().getSize();
            int margin = 80;
            int cy = r.getY() + r.getHeight() / 2;
            return cy >= margin && cy <= win.getHeight() - margin;
        } catch (Exception ex) {
            return false;
        }
    }

    private WebElement findIosTaiKhoanKhacElement(By xpathLoc, By predicateLoc) {
        List<WebElement> x = driver.findElements(xpathLoc);
        if (!x.isEmpty()) {
            return wait.until(ExpectedConditions.presenceOfElementLocated(xpathLoc));
        }
        return wait.until(ExpectedConditions.presenceOfElementLocated(predicateLoc));
    }

    /**
     * WDA: gesture theo elementId → tọa độ trung tâm → {@code click} — Flutter {@code StaticText} đôi khi chỉ nhận một cách.
     */
    private void iosTapWithFallbacks(WebElement el) {
        Exception last = null;
        try {
            iosClickGestureOnElement(el);
            return;
        } catch (Exception ex) {
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
            return;
        } catch (Exception ex) {
            last = ex;
        }
        try {
            el.click();
            return;
        } catch (Exception ex) {
            last = ex;
        }
        throw new RuntimeException("iOS: không tap được 'Đăng nhập bằng tài khoản khác'", last);
    }

    private void iosClickGestureOnElement(WebElement el) {
        Map<String, Object> p = new HashMap<>();
        p.put("elementId", ((RemoteWebElement) el).getId());
        driver.executeScript("mobile: clickGesture", p);
    }

    /**
     * Chọn account đầu tiên trong danh sách (social / smoke).
     */
    public void selectFirstAccount() {
        By first = byPlatform(
                driver,
                AppiumBy.xpath("(//android.view.View[contains(@content-desc,'ID: ')])[1]"),
                AppiumBy.xpath("(//*[contains(@name,'ID: ') or contains(@label,'ID: ')])[1]"));
        if (driver.findElements(first).isEmpty()) {
            throw new RuntimeException("Không thấy account đã lưu (pattern ID: ) trên màn Account");
        }
        select(first);
    }

    /**
     * Danh sách các dòng account (debug / bước tùy chọn).
     */
    public List<WebElement> findSavedAccountRows() {
        if (driver instanceof IOSDriver) {
            return driver.findElements(
                    AppiumBy.xpath("//*[contains(@name,'ID: ') or contains(@label,'ID: ')]"));
        }
        return driver.findElements(
                AppiumBy.xpath("//android.view.View[contains(@content-desc,'ID: ')]"));
    }

    private static String escXPath(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.replace("'", "''");
    }
}
