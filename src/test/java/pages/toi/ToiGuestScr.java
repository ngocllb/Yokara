package pages.toi;

import base.BaseScr;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import pages.toi.login.AccountScr;
import pages.toi.login.LoginMethodScr;

public class ToiGuestScr extends BaseScr {

    private By btnDangNhap = AppiumBy.xpath(
            "//*[contains(@content-desc, 'Đăng nhập') or contains(@content-desc, 'ĐĂNG NHẬP')"
                    + " or contains(@name, 'Đăng nhập') or contains(@label, 'Đăng nhập')]");

    public ToiGuestScr(AppiumDriver driver){
        super(driver);
    }

    public boolean isGuest(){
        // Nếu tìm thấy nút Đăng nhập thì là Guest
        return isDisplayed(btnDangNhap);
    }

    public BaseScr clickLogin(){

        click(btnDangNhap);

        // nếu có account lưu
        if(isDisplayed(AppiumBy.accessibilityId("Đăng nhập bằng tài khoản khác"))){
            return new AccountScr(driver);
        }

        // nếu không có account lưu
        return new LoginMethodScr(driver);
    }
    public void waitForGuestPage(){

        wait.until(
                ExpectedConditions.visibilityOfElementLocated(btnDangNhap)
        );
    }
    public boolean isGuestPageDisplayed() {

        return isDisplayed(btnDangNhap);
    }
}

