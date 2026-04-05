package utils;

import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

/**
 * Tiện ích waiting chuẩn cho toàn bộ automation framework.
 *
 * <p><b>Nguyên tắc:</b> TUYỆT ĐỐI không dùng {@code Thread.sleep()} hay
 * {@code implicitlyWait()}. Tất cả chờ đợi phải thông qua
 * {@link WebDriverWait} + {@link ExpectedConditions}.</p>
 *
 * <h3>Cách dùng điển hình:</h3>
 * <pre>
 * // Chờ element hiển thị
 * WaitUtils.waitForVisible(driver, By.id("btn_login"));
 *
 * // Chờ element biến mất (ví dụ loading spinner)
 * WaitUtils.waitForInvisible(driver, By.id("loading_spinner"));
 *
 * // Chờ có ít nhất 1 kết quả tìm kiếm
 * WaitUtils.waitForPresence(driver, By.id("search_result"));
 *
 * // Kiểm tra element xuất hiện trong thời gian ngắn (không throw)
 * boolean shown = WaitUtils.isVisibleWithin(driver, By.id("toast_msg"), 3);
 * </pre>
 */
public class WaitUtils {

    /** Timeout mặc định (giây) cho hầu hết thao tác UI */
    public static final int DEFAULT_TIMEOUT  = 20;

    /** Timeout ngắn để kiểm tra nhanh sự tồn tại của element */
    public static final int SHORT_TIMEOUT    = 5;

    /** Timeout dài cho các màn hình load nặng (video, mạng chậm, v.v.) */
    public static final int LONG_TIMEOUT     = 45;

    private WaitUtils() { /* utility class */ }

    // ================================================================
    //  Factory helpers
    // ================================================================

    private static WebDriverWait wait(AppiumDriver driver, int timeoutSeconds) {
        return new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
    }

    // ================================================================
    //  Visibility
    // ================================================================

    /**
     * Chờ element hiển thị với timeout mặc định.
     *
     * @return WebElement sau khi hiển thị
     */
    public static WebElement waitForVisible(AppiumDriver driver, By locator) {
        return wait(driver, DEFAULT_TIMEOUT)
                .until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    /**
     * Chờ element hiển thị với timeout tùy chỉnh.
     */
    public static WebElement waitForVisible(AppiumDriver driver, By locator, int timeoutSeconds) {
        return wait(driver, timeoutSeconds)
                .until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    /**
     * Chờ element BIẾN MẤT (ví dụ loading spinner).
     */
    public static void waitForInvisible(AppiumDriver driver, By locator) {
        wait(driver, DEFAULT_TIMEOUT)
                .until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }

    /**
     * Chờ element BIẾN MẤT với timeout tùy chỉnh.
     */
    public static void waitForInvisible(AppiumDriver driver, By locator, int timeoutSeconds) {
        wait(driver, timeoutSeconds)
                .until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }

    // ================================================================
    //  Clickable
    // ================================================================

    /**
     * Chờ element có thể click được.
     *
     * @return WebElement sẵn sàng click
     */
    public static WebElement waitForClickable(AppiumDriver driver, By locator) {
        return wait(driver, DEFAULT_TIMEOUT)
                .until(ExpectedConditions.elementToBeClickable(locator));
    }

    /**
     * Chờ element có thể click với timeout tùy chỉnh.
     */
    public static WebElement waitForClickable(AppiumDriver driver, By locator, int timeoutSeconds) {
        return wait(driver, timeoutSeconds)
                .until(ExpectedConditions.elementToBeClickable(locator));
    }

    // ================================================================
    //  Presence (DOM, chưa cần hiển thị)
    // ================================================================

    /**
     * Chờ element hiện diện trong DOM (không nhất thiết visible).
     */
    public static WebElement waitForPresence(AppiumDriver driver, By locator) {
        return wait(driver, DEFAULT_TIMEOUT)
                .until(ExpectedConditions.presenceOfElementLocated(locator));
    }

    /**
     * Chờ có ít nhất 1 element trong DOM.
     */
    public static List<WebElement> waitForPresenceOfAll(AppiumDriver driver, By locator) {
        return wait(driver, DEFAULT_TIMEOUT)
                .until(ExpectedConditions.presenceOfAllElementsLocatedBy(locator));
    }

    // ================================================================
    //  Text
    // ================================================================

    /**
     * Chờ text của element khớp với giá trị kỳ vọng.
     */
    public static void waitForText(AppiumDriver driver, By locator, String expectedText) {
        wait(driver, DEFAULT_TIMEOUT)
                .until(ExpectedConditions.textToBe(locator, expectedText));
    }

    /**
     * Chờ text của element CHỨA chuỗi con kỳ vọng.
     */
    public static void waitForTextContains(AppiumDriver driver, By locator, String partialText) {
        wait(driver, DEFAULT_TIMEOUT)
                .until(ExpectedConditions.textToBePresentInElementLocated(locator, partialText));
    }

    // ================================================================
    //  Custom condition
    // ================================================================

    /**
     * Chờ theo điều kiện tùy chỉnh (dùng lambda cho trường hợp đặc biệt).
     *
     * <pre>
     * WaitUtils.waitFor(driver, d -> d.findElements(By.id("x")).size() > 2, 10);
     * </pre>
     */
    public static <T> T waitFor(AppiumDriver driver,
                                ExpectedCondition<T> condition,
                                int timeoutSeconds) {
        return wait(driver, timeoutSeconds).until(condition);
    }

    // ================================================================
    //  Safe checks (không throw exception)
    // ================================================================

    /**
     * Kiểm tra element có hiển thị trong khoảng thời gian {@code timeoutSeconds} không.
     * Trả về {@code false} thay vì throw nếu timeout.
     *
     * <p>Dùng cho các assertion kiểu "nếu có thì tốt, không có cũng không fail".</p>
     */
    public static boolean isVisibleWithin(AppiumDriver driver, By locator, int timeoutSeconds) {
        try {
            waitForVisible(driver, locator, timeoutSeconds);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Kiểm tra nhanh element có hiển thị không (dùng SHORT_TIMEOUT).
     */
    public static boolean isPresent(AppiumDriver driver, By locator) {
        return isVisibleWithin(driver, locator, SHORT_TIMEOUT);
    }
}
