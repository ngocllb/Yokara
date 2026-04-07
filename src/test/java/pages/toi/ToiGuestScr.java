package pages.toi;

import base.BaseScr;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import pages.toi.login.AccountScr;
import pages.toi.login.LoginMethodScr;

import io.appium.java_client.ios.IOSDriver;

/**
 * Tab Tôi khi chưa đăng nhập — một locator {@code accessibilityId} cho nút Đăng nhập (đối chiếu dump).
 */
public class ToiGuestScr extends BaseScr {

    private final By btnDangNhap = AppiumBy.accessibilityId("Đăng nhập");

    public ToiGuestScr(AppiumDriver driver) {
        super(driver);
    }

    public boolean isGuest(){
        // Nếu tìm thấy nút Đăng nhập thì là Guest (chờ tối đa 3s cho transition)
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(btnDangNhap));
            return true;
        } catch (Exception e) {
            return isDisplayed(btnDangNhap);
        }
    }

    public BaseScr clickLogin() {
        if (driver instanceof IOSDriver) {
            select(btnDangNhap);
        } else {
            click(btnDangNhap);
        }

        // nếu có màn account lưu — check bằng marker ID: thay vì nút ở bottom (chờ tối đa 5s cho UI load)
        if (AccountScr.isAccountScr(driver, 5)) {
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

