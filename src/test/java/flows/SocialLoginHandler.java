package flows;

import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import utils.WaitUtils;

import java.util.List;

/**
 * Xử lý màn hình đăng nhập Social (Google, Facebook, Zalo)
 * khi hiện dialog chọn tài khoản.
 *
 * <p>Mỗi locator được kiểm tra bằng {@link WaitUtils#isPresent}
 * (ngắn, không throw) thay vì {@code driver.findElements} trực tiếp,
 * để tránh race condition khi dialog load chậm.</p>
 */
public class SocialLoginHandler {

    private final AppiumDriver driver;

    private static final By GOOGLE_ACCOUNT  = By.id("com.google.android.gms:id/account_name");
    private static final By FACEBOOK_BUTTON = By.id("com.facebook.katana:id/continue_button");
    private static final By ZALO_ACCOUNT    = By.id("com.zing.zalo:id/tv_name");

    public SocialLoginHandler(AppiumDriver driver) {
        this.driver = driver;
    }

    /**
     * Tự động xử lý dialog chọn tài khoản Social.
     * Thứ tự ưu tiên: Google → Facebook → Zalo.
     */
    public void handle() {
        if (clickFirstIfPresent(GOOGLE_ACCOUNT))  return;
        if (clickFirstIfPresent(FACEBOOK_BUTTON)) return;
        clickFirstIfPresent(ZALO_ACCOUNT);
    }

    /**
     * Click vào phần tử đầu tiên nếu hiện diện trong SHORT_TIMEOUT.
     * Dùng {@link WaitUtils#isPresent} để kiểm tra với timeout ngắn,
     * tránh chờ quá lâu khi element không tồn tại.
     */
    private boolean clickFirstIfPresent(By locator) {
        if (!WaitUtils.isPresent(driver, locator)) {
            return false;
        }
        List<WebElement> matches = driver.findElements(locator);
        if (matches.isEmpty()) {
            return false;
        }
        matches.get(0).click();
        return true;
    }
}