package pages.tructuyen.tabtructuyen;

import base.BaseScr;
import base.BottomNav;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.By;

/** Màn Bảng xếp hạng — tiêu đề cùng semantics Android/iOS ({@code accessibilityId}). */
public class BangXepHangScr extends BaseScr {

    private final By lblBangXepHang = AppiumBy.accessibilityId("Bảng xếp hạng");

    private final BottomNav bottomNav;

    public BangXepHangScr(AppiumDriver driver) {
        super(driver);
        this.bottomNav = new BottomNav(driver);
    }

    public BottomNav nav() {
        return bottomNav;
    }

    public boolean isLoaded() {
        return isDisplayed(lblBangXepHang);
    }
}
