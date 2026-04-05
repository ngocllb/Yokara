package base;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.pagefactory.AppiumFieldDecorator;
import org.openqa.selenium.*;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Map;
import java.util.List;

/**
 * Lớp cơ sở cho tất cả Page Object.
 *
 * <p>Sử dụng {@link AppiumFieldDecorator} để các subclass có thể dùng
 * annotation {@code @AndroidFindBy} hoặc {@code @iOSXCUITFindBy}
 * cho locator đa nền tảng.</p>
 *
 * <pre>
 * Ví dụ trong Page class con:
 *
 * {@literal @}AndroidFindBy(id = "com.example:id/btn_login")
 * {@literal @}iOSXCUITFindBy(accessibility = "LoginButton")
 * private WebElement btnLogin;
 * </pre>
 */
public class BaseScr {

    protected AppiumDriver driver;
    protected WebDriverWait wait;

    private static final int DEFAULT_WAIT_SECONDS = 20;

    // Pattern chuẩn của Appium PageFactory: this đã fully initialized trước khi truyền vào
    @SuppressWarnings("LeakingThisInConstructor")
    public BaseScr(AppiumDriver driver) {
        this.driver = driver;
        this.wait   = new WebDriverWait(driver, Duration.ofSeconds(DEFAULT_WAIT_SECONDS));

        // Khởi tạo các field được đánh dấu @AndroidFindBy / @iOSXCUITFindBy
        PageFactory.initElements(new AppiumFieldDecorator(driver), this);
    }

    /**
     * Thứ tự trên <strong>mọi nền tảng</strong>: (1) Android {@code resource-id} nếu có,
     * (2) {@link AppiumBy#accessibilityId} (Android: thường khớp content-desc; iOS: accessibility identifier),
     * (3) {@code fallback} (XPath, iOS predicate, …).
     * <p>Truyền {@code null} hoặc chuỗi rỗng để bỏ qua bước tương ứng.</p>
     */
    protected static By byIdThenFallback(
            AppiumDriver driver,
            String androidResourceId,
            String accessibilityId,
            By fallback) {
        return new By() {
            @Override
            public List<WebElement> findElements(SearchContext context) {
                if (driver instanceof AndroidDriver
                        && androidResourceId != null
                        && !androidResourceId.isBlank()) {
                    List<WebElement> byId = context.findElements(AppiumBy.id(androidResourceId));
                    if (!byId.isEmpty()) {
                        return byId;
                    }
                }
                if (accessibilityId != null && !accessibilityId.isBlank()) {
                    List<WebElement> byAcc = context.findElements(AppiumBy.accessibilityId(accessibilityId));
                    if (!byAcc.isEmpty()) {
                        return byAcc;
                    }
                }
                return fallback.findElements(context);
            }
        };
    }

    /**
     * Chọn locator theo nền tảng (multi-device / parallel): Android vs iOS.
     * Dùng khi đã bắt riêng từng nền (resource-id / XPath / accessibility).
     */
    protected static By byPlatform(AppiumDriver driver, By androidLocator, By iosLocator) {
        return driver instanceof IOSDriver ? iosLocator : androidLocator;
    }

    /* ================= FIND ================= */

    protected WebElement find(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected List<WebElement> finds(By locator) {
        return driver.findElements(locator);
    }

    /* ================= CLICK ================= */

    protected void click(By locator) {
        for (int i = 0; i < 3; i++) {
            try {
                WebElement element = wait.until(ExpectedConditions.elementToBeClickable(locator));
                element.click();
                return;
            } catch (StaleElementReferenceException e) {
                if (i == 2) throw e;
            }
        }
    }

    /* ================= TYPE ================= */

    protected void type(By locator, String text) {
        for (int i = 0; i < 3; i++) {
            try {
                WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
                element.click();
                element.clear();
                element.sendKeys(text);
                return;
            } catch (StaleElementReferenceException e) {
                if (i == 2) throw e;
            }
        }
    }

    /* ================= DISPLAY ================= */

    protected boolean isDisplayed(By locator) {
        try {
            return !driver.findElements(locator).isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    /* ================= SWIPE ================= */

    public void swipeUp() {
        Dimension size = driver.manage().window().getSize();

        int startX = size.width / 2;
        int startY = (int) (size.height * 0.8);

        driver.executeScript("mobile: swipeGesture", Map.of(
                "left",      startX,
                "top",       startY,
                "width",     0,
                "height",    size.height,
                "direction", "up",
                "percent",   0.7
        ));
    }

    /* ================= SCROLL ================= */

    public void scrollToElement(By locator) {
        int maxScroll = 6;

        for (int i = 0; i < maxScroll; i++) {
            if (isDisplayed(locator)) {
                return;
            }
            swipeUp();
        }

        throw new RuntimeException("Không tìm thấy element sau khi scroll: " + locator);
    }

    /* ================= STARTUP POPUPS ================= */

    /**
     * Xử lý các popup hệ thống hoặc Welcome khi vừa mở app (EULA, Permission, v.v.)
     */
    public void handleStartupPopups() {
        try {
            // Danh sách các text nút thường gặp trong pop-up
            By btnCommon = AppiumBy.xpath(
                    "//*[contains(@text, 'Đồng ý') or contains(@name, 'Đồng ý') or contains(@label, 'Đồng ý')"
                            + " or contains(@text, 'Cho phép') or contains(@name, 'Cho phép') or contains(@label, 'Cho phép')"
                            + " or contains(@text, 'Allow') or contains(@name, 'Allow') or contains(@label, 'Allow')"
                            + " or contains(@text, 'AGREE') or contains(@name, 'AGREE')]"
            );
            if (isDisplayed(btnCommon)) {
                click(btnCommon);
            }
        } catch (Exception e) {
            // Bỏ qua nếu không thấy hoặc lỗi
        }
    }

    /* ================= MAP CLICK ================= */

    protected void clickByKey(Map<String, By> map, String key) {
        By locator = map.get(key.toLowerCase());

        if (locator == null) {
            throw new RuntimeException("Locator not found for key: " + key);
        }

        click(locator);
    }

    /**
     * Tìm và click vào button dựa trên text (hỗ trợ cả Android & iOS).
     */
    protected void clickByText(String text) {
        By locator = AppiumBy.xpath(
                "//*[(self::button or self::android.widget.Button or self::XCUIElementTypeButton)" +
                        " and (contains(@text, '" + text + "') or contains(@name, '" + text + "') or contains(@label, '" + text + "'))]"
        );
        click(locator);
    }
}
