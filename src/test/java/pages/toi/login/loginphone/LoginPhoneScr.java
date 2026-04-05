package pages.toi.login.loginphone;

import base.BaseScr;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.By;

public class LoginPhoneScr extends BaseScr {

    private final By phoneInput = By.xpath("//android.widget.EditText");
    private final By btnNext = AppiumBy.accessibilityId("Tiếp theo");

    public LoginPhoneScr(AppiumDriver driver){
        super(driver);
    }

    public boolean isDisplayedPage() {
        return isDisplayed(btnNext);
    }

    public OtpVerificationScr goToOtpPage(String phone){

        type(phoneInput, phone);
        click(btnNext);

        return new OtpVerificationScr(driver).waitForPageDisplayed();
    }
}

