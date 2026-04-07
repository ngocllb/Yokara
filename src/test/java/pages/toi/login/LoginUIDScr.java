package pages.toi.login;

import base.BaseScr;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.By;
import pages.toi.ToiProfileScr;

/**
 * Đăng nhập bằng ID — cùng semantics chuỗi hint Android / label iOS ({@code Mời nhập ID}, {@code Vui lòng nhập mật khẩu});
 * {@link core.LocatorPolicy}: {@code byIdThenFallback} khi Android chỉ có hint trong XML nhưng accessibility vẫn resolve được trước.
 */
public class LoginUIDScr extends BaseScr {

    private final By inputUID;
    private final By inputPassword;
    private final By btnLogin = AppiumBy.accessibilityId("Đăng nhập");

    public LoginUIDScr(AppiumDriver driver) {
        super(driver);
        this.inputUID = byIdThenFallback(
                driver,
                null,
                "Mời nhập ID",
                AppiumBy.xpath("//android.widget.EditText[@hint='Mời nhập ID']"));
        this.inputPassword = byIdThenFallback(
                driver,
                null,
                "Vui lòng nhập mật khẩu",
                AppiumBy.xpath("//android.widget.EditText[@hint='Vui lòng nhập mật khẩu']"));
    }

    public ToiProfileScr loginByUID(String uid, String password) {
        type(inputUID, uid);
        type(inputPassword, password);
        click(btnLogin);
        return new ToiProfileScr(driver);
    }
}
