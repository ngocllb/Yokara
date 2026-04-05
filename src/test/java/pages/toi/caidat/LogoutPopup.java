package pages.toi.caidat;

import base.BaseScr;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;

import pages.toi.ToiGuestScr;

public class LogoutPopup extends BaseScr {

    public LogoutPopup(AppiumDriver driver) {
        super(driver);
    }

    /* ================= LOCATORS ================= */

    private final By btnDong = AppiumBy.accessibilityId("Đóng");

    private final By btnXacNhan = AppiumBy.accessibilityId("Xác nhận");


    /* ================= ACTION ================= */

    public CaiDatScr closePopup() {

        click(btnDong);

        return new CaiDatScr(driver);
    }

    public ToiGuestScr confirmLogout() {

        click(btnXacNhan);

        ToiGuestScr guestPage = new ToiGuestScr(driver);

        // wait màn guest
        guestPage.waitForGuestPage();

        return guestPage;
    }


    /* ================= VERIFY ================= */

    public boolean isPopupDisplayed() {

        return isDisplayed(btnXacNhan);

    }


}
