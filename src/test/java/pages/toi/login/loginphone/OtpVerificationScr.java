package pages.toi.login.loginphone;

import base.BaseScr;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import pages.toi.ToiProfileScr;

/**
 * Màn nhập OTP sau khi nhập SĐT.
 * <p>Android (logic cũ): {@code View} có {@code content-desc} chứa {@code Nhập mã OTP}, {@code EditText} bên trong.</p>
 * <p>iOS: {@code scripts/xml_dumps/otp-ios.xml} — khối title động (timer); ô OTP là {@code TextField} không label trong dump; nút dùng semantics cố định.</p>
 * <p>{@code otpTitle}/{@code otpInput} giữ {@code byPlatform}: cùng chuỗi con {@code Nhập mã OTP} nhưng cây Android vs iOS khác (View+EditText vs Other+TextField) — XPath tách an toàn hơn một {@code accessibilityId} duy nhất.</p>
 */
public class OtpVerificationScr extends BaseScr {

    private final By otpTitle;
    private final By otpInput;

    private final By btnConfirm = AppiumBy.accessibilityId("Xác nhận");
    private final By btnResendCode = AppiumBy.accessibilityId("Gửi lại mã");

    private final By otpErrorMessage;

    public OtpVerificationScr(AppiumDriver driver) {
        super(driver);
        this.otpTitle = byPlatform(
                driver,
                AppiumBy.xpath("//android.view.View[contains(@content-desc,'Nhập mã OTP')]"),
                AppiumBy.xpath("//XCUIElementTypeOther[contains(@name,'Nhập mã OTP')]"));
        this.otpInput = byPlatform(
                driver,
                AppiumBy.xpath(
                        "//android.view.View[contains(@content-desc,'Nhập mã OTP')]//android.widget.EditText"),
                AppiumBy.xpath(
                        "//XCUIElementTypeOther[contains(@name,'Nhập mã OTP')]/following::XCUIElementTypeTextField[1]"));
        this.otpErrorMessage = byPlatform(
                driver,
                AppiumBy.xpath(
                        "//*[contains(@content-desc,'Mã kích hoạt không đúng. Vui lòng kiểm tra lại')]"),
                AppiumBy.xpath(
                        "//*[contains(@name,'Mã kích hoạt không đúng') or contains(@label,'Mã kích hoạt không đúng') "
                                + "or contains(@value,'Mã kích hoạt không đúng')]"));
    }

    public OtpVerificationScr waitForPageDisplayed() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(otpTitle));
        wait.until(ExpectedConditions.visibilityOfElementLocated(otpInput));
        wait.until(ExpectedConditions.visibilityOfElementLocated(btnConfirm));
        return this;
    }

    public boolean isOtpPageDisplayed() {
        try {
            waitForPageDisplayed();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void enterOtp(String otp) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(otpInput));
        type(otpInput, otp);
    }

    public void clickConfirm() {
        wait.until(ExpectedConditions.elementToBeClickable(btnConfirm));
        click(btnConfirm);
    }

    public void clickResendCode() {
        wait.until(ExpectedConditions.elementToBeClickable(btnResendCode));
        click(btnResendCode);
    }

    public boolean isOtpErrorDisplayed() {
        return isDisplayed(otpErrorMessage);
    }

    public void waitForOtpError() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(otpErrorMessage));
    }

    public ToiProfileScr submitValidOtp(String otp) {
        waitForPageDisplayed();
        enterOtp(otp);
        clickConfirm();
        return new ToiProfileScr(driver);
    }

    public OtpVerificationScr submitInvalidOtp(String otp) {
        waitForPageDisplayed();
        enterOtp(otp);
        clickConfirm();
        waitForOtpError();
        return this;
    }

    public boolean isLoginSuccess(String expectedUid) {
        try {
            return new ToiProfileScr(driver).isUserIdDisplayed(expectedUid);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isStillOnOtpPage() {
        return isDisplayed(otpTitle) && isDisplayed(btnConfirm);
    }
}
