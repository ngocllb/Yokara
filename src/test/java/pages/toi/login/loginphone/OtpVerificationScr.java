package pages.toi.login.loginphone;

import base.BaseScr;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import pages.toi.ToiProfileScr;

public class OtpVerificationScr extends BaseScr {

    private final By otpTitle = AppiumBy.xpath(
            "//android.view.View[contains(@content-desc,'Nhập mã OTP')]"
    );

    private final By otpInput = AppiumBy.xpath(
            "//android.view.View[contains(@content-desc,'Nhập mã OTP')]//android.widget.EditText"
    );

    private final By btnConfirm = AppiumBy.accessibilityId("Xác nhận");

    private final By btnResendCode = AppiumBy.accessibilityId("Gửi lại mã");

    private final By otpErrorMessage = AppiumBy.xpath(
            "//*[contains(@content-desc,'Mã kích hoạt không đúng. Vui lòng kiểm tra lại')]"
    );

    public OtpVerificationScr(AppiumDriver driver) {
        super(driver);
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
