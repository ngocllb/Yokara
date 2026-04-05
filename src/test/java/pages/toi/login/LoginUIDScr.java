package pages.toi.login;

import base.BaseScr;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.By;
import pages.toi.ToiProfileScr;

public class LoginUIDScr extends BaseScr {

    private final By inputUID = AppiumBy.xpath("//android.widget.EditText[@password='false']");
    private final By inputPassword = AppiumBy.xpath("//android.widget.EditText[@password='true']");
    private final By btnLogin = AppiumBy.accessibilityId("Đăng nhập");

    public LoginUIDScr(AppiumDriver driver){
        super(driver);
    }

    public ToiProfileScr loginByUID(String uid, String password){

        type(inputUID, uid);

        type(inputPassword, password);

        click(btnLogin);

        return new ToiProfileScr(driver);
    }
}
