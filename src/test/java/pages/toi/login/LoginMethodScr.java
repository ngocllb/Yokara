package pages.toi.login;

import base.BaseScr;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.ios.IOSDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import pages.toi.login.loginphone.LoginPhoneScr;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Màn chọn phương thức đăng nhập (sau Đăng nhập ở guest / không có TK lưu).
 * <p>Android: {@code content-desc} — đối chiếu {@code XML android Screen locator/LoginMethodScr_android.txt}.
 * iOS: icon hàng “Gần đây” đôi khi {@code visible=false} trong tree — dùng {@code presence} + {@code clickGesture} khi cần.</p>
 * <p>Giữ {@code byPlatform} cho từng nút vì chuỗi accessibility Android (mô tả dài) không trùng label iOS — xem {@link core.LocatorPolicy}.</p>
 */
public class LoginMethodScr extends BaseScr {

    public LoginMethodScr(AppiumDriver driver) {
        super(driver);
    }

    /**
     * Sau {@link AccountScr#selectAnotherMethodLogin()} — chờ màn chọn phương thức thật sự (tránh tap UID/Phone quá sớm).
     */
    public static void waitForLoginMethodScreen(AppiumDriver driver) {
        WebDriverWait w = new WebDriverWait(driver, Duration.ofSeconds(25));
        w.until(d -> {
            boolean isAcc = AccountScr.isAccountScr(driver, 0);
            if (isAcc) {
                return false;
            }
            List<WebElement> fb = d.findElements(AppiumBy.accessibilityId("Đăng nhập Facebook"));
            if (!fb.isEmpty()) {
                System.out.println("[DEBUG] Found Facebook button");
                return true;
            }
            List<WebElement> gg = d.findElements(AppiumBy.accessibilityId("Đăng nhập bằng Google"));
            if (!gg.isEmpty()) {
                System.out.println("[DEBUG] Found Google button");
                return true;
            }
            List<WebElement> gd = d.findElements(AppiumBy.accessibilityId("Gần đây"));
            if (!gd.isEmpty()) {
                System.out.println("[DEBUG] Found 'Gan day' section");
                return true;
            }
            List<WebElement> id = d.findElements(AppiumBy.accessibilityId("Đăng nhập bằng ID"));
            if (!id.isEmpty()) {
                System.out.println("[DEBUG] Found 'Login by ID' button");
                return true;
            }
            List<WebElement> texts = d.findElements(AppiumBy.accessibilityId("Đăng nhập để tận hưởng trải nghiệm âm nhạc tốt hơn"));
            if (!texts.isEmpty()) {
                System.out.println("[DEBUG] Found 'Enjoy' text trigger");
                return true;
            }
            return false;
        });
    }

    /**
     * @param method {@code uid} | {@code phone} | {@code facebook} | {@code google} | {@code zalo} | {@code apple}
     */
    public BaseScr loginWith(String method) {
        String key = method.toLowerCase().trim();
        By locator = locatorFor(key);
        if (driver instanceof IOSDriver) {
            iosTapMethod(locator);
        } else {
            select(locator);
        }
        return switch (key) {
            case "uid" -> new LoginUIDScr(driver);
            case "phone" -> new LoginPhoneScr(driver);
            default -> new AccountScr(driver);
        };
    }

    /**
     * iOS: không dùng {@link #scrollToElement} (lỗi tương tự Account — {@code isDisplayed} trong DOM ≠ hiển thị).
     * Icon hàng “Gần đây” thường cần {@code clickGesture}.
     */
    private void iosTapMethod(By locator) {
        WebElement el;
        try {
            el = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
        } catch (TimeoutException e) {
            try {
                Files.writeString(Path.of("target/loginmethod-fail.xml"), driver.getPageSource());
            } catch (Exception ignored) {
                // ignore
            }
            throw e;
        }
        try {
            el.click();
        } catch (Exception ex) {
            Map<String, Object> p = new HashMap<>();
            p.put("elementId", ((RemoteWebElement) el).getId());
            driver.executeScript("mobile: clickGesture", p);
        }
    }

    /**
     * iOS: XCUITest đôi khi không khớp XPath union {@code |}; thử lần lượt từng biểu thức.
     * Build mới: nút có label; build cũ: hàng icon sau “Gần đây” ({@code scripts/xml_dumps/login-method-ios.xml}).
     */
    private static By iosUidFallbackChain() {
        return new By() {
            @Override
            public List<WebElement> findElements(SearchContext context) {
                String[] xpaths = {
                        "//*[contains(@name,'Đăng nhập bằng ID') or contains(@label,'Đăng nhập bằng ID')]",
                        "//XCUIElementTypeImage[contains(@name,'Đăng nhập bằng ID') or contains(@label,'Đăng nhập bằng ID')]",
                        "//XCUIElementTypeStaticText[contains(@name,'Đăng nhập bằng ID') or contains(@label,'Đăng nhập bằng ID')]",
                        "//XCUIElementTypeImage[(contains(@name,'Gần đây') or contains(@label,'Gần đây'))]"
                                + "/following-sibling::XCUIElementTypeImage[2]",
                        "//*[(contains(@name,'Gần đây') or contains(@label,'Gần đây'))]"
                                + "/following-sibling::XCUIElementTypeImage[2]"
                };
                for (String xp : xpaths) {
                    List<WebElement> els = context.findElements(AppiumBy.xpath(xp));
                    if (!els.isEmpty()) {
                        return els;
                    }
                }
                List<WebElement> pred = context.findElements(AppiumBy.iOSNsPredicateString(
                        "(type == 'XCUIElementTypeImage' OR type == 'XCUIElementTypeStaticText') "
                                + "AND (name CONTAINS[c] 'Đăng nhập' AND name CONTAINS[c] 'ID')"));
                return pred.isEmpty() ? Collections.emptyList() : pred;
            }

            @Override
            public String toString() {
                return "LoginMethodScr.iosUidFallbackChain";
            }
        };
    }

    private static By iosPhoneFallbackChain() {
        return new By() {
            @Override
            public List<WebElement> findElements(SearchContext context) {
                String[] xpaths = {
                        "//*[contains(@name,'Đăng nhập số điện thoại') or contains(@label,'Đăng nhập số điện thoại')]",
                        "//XCUIElementTypeImage[contains(@name,'Đăng nhập số điện thoại') "
                                + "or contains(@label,'Đăng nhập số điện thoại')]",
                        "//XCUIElementTypeStaticText[contains(@name,'Đăng nhập số điện thoại') "
                                + "or contains(@label,'Đăng nhập số điện thoại')]",
                        "//XCUIElementTypeImage[(contains(@name,'Gần đây') or contains(@label,'Gần đây'))]"
                                + "/following-sibling::XCUIElementTypeImage[1]",
                        "//*[(contains(@name,'Gần đây') or contains(@label,'Gần đây'))]"
                                + "/following-sibling::XCUIElementTypeImage[1]"
                };
                for (String xp : xpaths) {
                    List<WebElement> els = context.findElements(AppiumBy.xpath(xp));
                    if (!els.isEmpty()) {
                        return els;
                    }
                }
                List<WebElement> pred = context.findElements(AppiumBy.iOSNsPredicateString(
                        "(type == 'XCUIElementTypeImage' OR type == 'XCUIElementTypeStaticText') "
                                + "AND (name CONTAINS[c] 'điện thoại' OR label CONTAINS[c] 'điện thoại')"));
                return pred.isEmpty() ? Collections.emptyList() : pred;
            }

            @Override
            public String toString() {
                return "LoginMethodScr.iosPhoneFallbackChain";
            }
        };
    }

    /** Gộp accessibility + fallback (tránh lồng {@link #byIdThenFallback} — log Selenium hiển thị [unknown locator]). */
    private static By iosUidCombined() {
        return new By() {
            @Override
            public List<WebElement> findElements(SearchContext context) {
                List<WebElement> byAcc = context.findElements(AppiumBy.accessibilityId("Đăng nhập bằng ID"));
                if (!byAcc.isEmpty()) {
                    return byAcc;
                }
                return iosUidFallbackChain().findElements(context);
            }

            @Override
            public String toString() {
                return "LoginMethodScr.iosUidCombined";
            }
        };
    }

    private static By iosPhoneCombined() {
        return new By() {
            @Override
            public List<WebElement> findElements(SearchContext context) {
                return iosPhoneFallbackChain().findElements(context);
            }

            @Override
            public String toString() {
                return "LoginMethodScr.iosPhoneCombined";
            }
        };
    }

    private By locatorFor(String key) {
        return switch (key) {
            case "facebook" -> AppiumBy.accessibilityId("Đăng nhập Facebook");
            case "google" -> AppiumBy.accessibilityId("Đăng nhập bằng Google");
            case "zalo" -> AppiumBy.accessibilityId("Đăng nhập Zalo");
            case "apple" -> AppiumBy.accessibilityId("Đăng nhập bằng Apple");
            case "uid" -> byPlatform(driver,
                    AppiumBy.accessibilityId("Đăng nhập bằng ID"),
                    iosUidCombined());
            case "phone" -> byPlatform(driver,
                    AppiumBy.accessibilityId("Đăng nhập số điện thoại\nGần đây"),
                    iosPhoneCombined());
            default -> throw new RuntimeException("Unsupported login method: " + key);
        };
    }
}
