package pages.toi.login;

import base.BaseScr;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.By;
import pages.toi.login.loginphone.LoginPhoneScr;

import java.util.HashMap;
import java.util.Map;

public class LoginMethodScr extends BaseScr {

    private final By btnLoginWithID = AppiumBy.accessibilityId("Đăng nhập bằng ID");
    private final Map<String, By> loginMethods = new HashMap<>();

    public LoginMethodScr(AppiumDriver driver) {

        super(driver);

        loginMethods.put("facebook", AppiumBy.accessibilityId("Đăng nhập Facebook"));
        loginMethods.put("google", AppiumBy.accessibilityId("Đăng nhập bằng Google"));
        loginMethods.put("zalo", AppiumBy.accessibilityId("Đăng nhập Zalo"));
        loginMethods.put("phone", AppiumBy.accessibilityId("Đăng nhập số điện thoại"));
        loginMethods.put("uid", btnLoginWithID);
    }


    public BaseScr loginWith(String method){

        By locator = loginMethods.get(method.toLowerCase());

        if(locator == null){
            throw new RuntimeException("Unsupported login method: " + method);
        }

        click(locator);

        return switch (method.toLowerCase()) {
            case "uid" -> new LoginUIDScr(driver);
            case "phone" -> new LoginPhoneScr(driver);
            default -> new AccountScr(driver);
        };
    }
}

