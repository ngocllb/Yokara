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

    private static final String IOS_LOGIN_FACEBOOK = "Đăng nhập Facebook";
    private static final String IOS_LOGIN_GOOGLE = "Đăng nhập bằng Google";
    private static final String IOS_LOGIN_ZALO = "Đăng nhập Zalo";
    private static final String IOS_LOGIN_APPLE = "Đăng nhập bằng Apple";
    private static final String IOS_LOGIN_UID = "Đăng nhập bằng ID";
    private static final String IOS_LOGIN_PHONE = "Đăng nhập số điện thoại";
    // XPath tuyệt đối theo dump thực tế user cung cấp (ưu tiên dùng cho cụm Gần đây iOS hiện tại).
    private static final String IOS_XP_ZALO_EXACT =
            "//XCUIElementTypeWindow/XCUIElementTypeOther/XCUIElementTypeOther/XCUIElementTypeOther/"
                    + "XCUIElementTypeOther[1]/XCUIElementTypeOther/XCUIElementTypeOther/XCUIElementTypeOther/"
                    + "XCUIElementTypeOther[2]/XCUIElementTypeOther[2]/XCUIElementTypeOther[2]/XCUIElementTypeImage[5]";
    private static final String IOS_XP_PHONE_EXACT =
            "//XCUIElementTypeWindow/XCUIElementTypeOther/XCUIElementTypeOther/XCUIElementTypeOther/"
                    + "XCUIElementTypeOther[1]/XCUIElementTypeOther/XCUIElementTypeOther/XCUIElementTypeOther/"
                    + "XCUIElementTypeOther[2]/XCUIElementTypeOther[2]/XCUIElementTypeOther[2]/XCUIElementTypeImage[6]";
    private static final String IOS_XP_UID_EXACT =
            "//XCUIElementTypeWindow/XCUIElementTypeOther/XCUIElementTypeOther/XCUIElementTypeOther/"
                    + "XCUIElementTypeOther[1]/XCUIElementTypeOther/XCUIElementTypeOther/XCUIElementTypeOther/"
                    + "XCUIElementTypeOther[2]/XCUIElementTypeOther[2]/XCUIElementTypeOther[2]/XCUIElementTypeImage[7]";

    public LoginMethodScr(AppiumDriver driver) {
        super(driver);
    }

    /**
     * Sau {@link AccountScr#selectAnotherMethodLogin()} — chờ màn chọn phương thức thật sự (tránh tap UID/Phone quá sớm).
     */
    public static void waitForLoginMethodScreen(AppiumDriver driver) {
        WebDriverWait w = new WebDriverWait(driver, Duration.ofSeconds(25));
        w.until(d -> {
            AppiumDriver ad = (AppiumDriver) d;
            if (AccountScr.isAccountScr(ad, 0)) {
                return false;
            }
            return isLoginMethodScreenPresent(ad);
        });
    }

    /** Kiểm tra nhanh (không chờ) — đồng bộ điều kiện với {@link #waitForLoginMethodScreen}. */
    public static boolean isLoginMethodScreenPresent(AppiumDriver driver) {
        if (AccountScr.isAccountScr(driver, 0)) {
            return false;
        }
        if (!driver.findElements(AppiumBy.accessibilityId(IOS_LOGIN_FACEBOOK)).isEmpty()) {
            return true;
        }
        if (!driver.findElements(AppiumBy.accessibilityId(IOS_LOGIN_GOOGLE)).isEmpty()) {
            return true;
        }
        if (!driver.findElements(AppiumBy.accessibilityId("Gần đây")).isEmpty()) {
            return true;
        }
        if (!driver.findElements(AppiumBy.accessibilityId(IOS_LOGIN_UID)).isEmpty()) {
            return true;
        }
        return !driver.findElements(
                AppiumBy.accessibilityId("Đăng nhập để tận hưởng trải nghiệm âm nhạc tốt hơn")).isEmpty();
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
                        IOS_XP_UID_EXACT,
                        "//*[contains(@name,'" + IOS_LOGIN_UID + "') or contains(@label,'" + IOS_LOGIN_UID + "')]",
                        "//XCUIElementTypeImage[contains(@name,'" + IOS_LOGIN_UID + "') or contains(@label,'" + IOS_LOGIN_UID + "')]",
                        "//XCUIElementTypeStaticText[contains(@name,'" + IOS_LOGIN_UID + "') or contains(@label,'" + IOS_LOGIN_UID + "')]"
                };
                for (String xp : xpaths) {
                    List<WebElement> els = context.findElements(AppiumBy.xpath(xp));
                    if (!els.isEmpty()) {
                        return els;
                    }
                }
                List<WebElement> pred = context.findElements(AppiumBy.iOSNsPredicateString(
                        "(type == 'XCUIElementTypeImage' OR type == 'XCUIElementTypeStaticText') "
                                + "AND ((name CONTAINS[c] 'ID' OR label CONTAINS[c] 'ID') "
                                + "OR (name CONTAINS[c] 'đăng nhập bằng id' OR label CONTAINS[c] 'đăng nhập bằng id'))"));
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
                        IOS_XP_PHONE_EXACT,
                        "//*[contains(@name,'" + IOS_LOGIN_PHONE + "') or contains(@label,'" + IOS_LOGIN_PHONE + "')]",
                        "//XCUIElementTypeImage[contains(@name,'" + IOS_LOGIN_PHONE + "') "
                                + "or contains(@label,'" + IOS_LOGIN_PHONE + "')]",
                        "//XCUIElementTypeStaticText[contains(@name,'" + IOS_LOGIN_PHONE + "') "
                                + "or contains(@label,'" + IOS_LOGIN_PHONE + "')]",
                        iosRecentIconByOrder(1),
                        iosRecentIconByOrder(2)
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
                List<WebElement> byAcc = context.findElements(AppiumBy.accessibilityId(IOS_LOGIN_UID));
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
                List<WebElement> byAcc = context.findElements(AppiumBy.accessibilityId(IOS_LOGIN_PHONE));
                if (!byAcc.isEmpty()) {
                    return byAcc;
                }
                return iosPhoneFallbackChain().findElements(context);
            }

            @Override
            public String toString() {
                return "LoginMethodScr.iosPhoneCombined";
            }
        };
    }

    private static By iosGoogleCombined() {
        return new By() {
            @Override
            public List<WebElement> findElements(SearchContext context) {
                List<WebElement> byAcc = context.findElements(AppiumBy.accessibilityId(IOS_LOGIN_GOOGLE));
                if (!byAcc.isEmpty()) {
                    return byAcc;
                }
                String[] xpaths = {
                        "//*[contains(@name,'" + IOS_LOGIN_GOOGLE + "') or contains(@label,'" + IOS_LOGIN_GOOGLE + "')]",
                        iosRecentIconByOrder(1)
                };
                for (String xp : xpaths) {
                    List<WebElement> els = context.findElements(AppiumBy.xpath(xp));
                    if (!els.isEmpty()) {
                        return els;
                    }
                }
                return Collections.emptyList();
            }

            @Override
            public String toString() {
                return "LoginMethodScr.iosGoogleCombined";
            }
        };
    }

    private static By iosZaloCombined() {
        return new By() {
            @Override
            public List<WebElement> findElements(SearchContext context) {
                List<WebElement> byAcc = context.findElements(AppiumBy.accessibilityId(IOS_LOGIN_ZALO));
                if (!byAcc.isEmpty()) {
                    return byAcc;
                }
                String[] xpaths = {
                        IOS_XP_ZALO_EXACT,
                        "//*[contains(@name,'" + IOS_LOGIN_ZALO + "') or contains(@label,'" + IOS_LOGIN_ZALO + "')]",
                        iosRecentIconByOrder(3)
                };
                for (String xp : xpaths) {
                    List<WebElement> els = context.findElements(AppiumBy.xpath(xp));
                    if (!els.isEmpty()) {
                        return els;
                    }
                }
                return Collections.emptyList();
            }

            @Override
            public String toString() {
                return "LoginMethodScr.iosZaloCombined";
            }
        };
    }

    /**
     * iOS dump login-method: icon "Gần đây" là anchor và các method gần đây nằm ở sibling kế tiếp.
     */
    private static String iosRecentIconByOrder(int order) {
        return "//XCUIElementTypeImage[(contains(@name,'Gần đây') or contains(@label,'Gần đây'))]"
                + "/following-sibling::XCUIElementTypeImage[" + order + "]";
    }

    private By locatorFor(String key) {
        return switch (key) {
            case "facebook" -> AppiumBy.accessibilityId(IOS_LOGIN_FACEBOOK);
            case "google" -> byPlatform(driver,
                    AppiumBy.accessibilityId(IOS_LOGIN_GOOGLE),
                    iosGoogleCombined());
            case "zalo" -> byPlatform(driver,
                    AppiumBy.accessibilityId(IOS_LOGIN_ZALO),
                    iosZaloCombined());
            case "apple" -> AppiumBy.accessibilityId(IOS_LOGIN_APPLE);
            case "uid" -> byPlatform(driver,
                    AppiumBy.accessibilityId(IOS_LOGIN_UID),
                    iosUidCombined());
            case "phone" -> byPlatform(driver,
                    AppiumBy.accessibilityId("Đăng nhập số điện thoại\nGần đây"),
                    iosPhoneCombined());
            default -> throw new RuntimeException("Unsupported login method: " + key);
        };
    }
}
