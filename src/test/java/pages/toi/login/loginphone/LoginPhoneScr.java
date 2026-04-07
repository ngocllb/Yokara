package pages.toi.login.loginphone;

import base.BaseScr;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.By;

/**
 * Nhập SĐT trước OTP — hint Android / label iOS trùng {@code  Nhập số điện thoại} (có khoảng đầu, xem
 * {@code XML android Screen locator/LoginPhoneScr_android.txt}).
 */
public class LoginPhoneScr extends BaseScr {

    private final By phoneInput;
    private final By btnNext = AppiumBy.accessibilityId("Tiếp theo");

    public LoginPhoneScr(AppiumDriver driver) {
        super(driver);
        // iOS: Chính xác Accessibility ID từ XML (có khoảng trắng ở đầu)
        this.phoneInput = byPlatform(
                driver,
                AppiumBy.xpath("//android.view.View[@content-desc=' Nhập số điện thoại\nĐiền số điện thoại di động của bạn']//android.widget.EditText"),
                AppiumBy.accessibilityId(" Nhập số điện thoại"));
    }

    public LoginPhoneScr waitForPageDisplayed() {
        waitForVisible(phoneInput);
        waitForVisible(btnNext);
        return this;
    }

    public boolean isDisplayedPage() {
        return isDisplayed(btnNext);
    }

    public OtpVerificationScr goToOtpPage(String phone) {
        waitForPageDisplayed();
        click(phoneInput); // iOS: Cần click để focus trước khi type
        type(phoneInput, phone);
        click(btnNext);
        return new OtpVerificationScr(driver).waitForPageDisplayed();
    }
}
