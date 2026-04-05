package pages.toi.login;

import base.BaseScr;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

public class AccountScr extends BaseScr {

    public AccountScr(AppiumDriver driver){
        super(driver);
    }

    /**
     * Chọn account đã lưu theo UID
     * Ví dụ: 6069820
     */
    public void selectAccountByUID(String uid) {
        By accountByUid = AppiumBy.androidUIAutomator(
                "new UiSelector()" +
                        ".className(\"android.view.View\")" +
                        ".descriptionContains(\"ID: " + uid + "\")"
        );
        click(accountByUid);
    }

    /**
     * Chọn đăng nhập bằng tài khoản khác
     */
    public void selectAnotherMethodLogin() {
        click(AppiumBy.accessibilityId("Đăng nhập bằng tài khoản khác"));
    }

    /**
     * Chọn account đầu tiên (dùng cho social login)
     */
    public void selectFirstAccount(){
        List<WebElement> accounts = driver.findElements(
                AppiumBy.androidUIAutomator(
                        "new UiSelector().className(\"android.view.View\").descriptionContains(\"ID: \")"
                )
        );

        if (accounts.isEmpty()) {
            throw new RuntimeException("No saved account found on Account page");
        }

        accounts.get(0).click();
    }
}
