package pages.trangchu;

import base.BaseScr;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.By;

/**
 * Trang chủ — mỗi control một cặp locator Android/iOS bắt từ hierarchy thật (không chuỗi OR/dự phòng trong từng nền).
 * <p>Tiêu đề header tách khỏi tab dưới: Android {@code View} không clickable; iOS {@code StaticText} (tab là {@code Image}).</p>
 */
public class TrangChuScr extends BaseScr {

    private final By lblTitle;
    private final By btnTopRightAction;

    private final By btnSuKien;
    private final By btnQuanhDay;

    private final By lblMvNoiBat;

    private final base.BottomNav bottomNav;

    public TrangChuScr(AppiumDriver driver) {
        super(driver);
        bottomNav = new base.BottomNav(driver);

        this.lblTitle = byPlatform(driver, androidTitleTrangChu(), iosTitleTrangChu());
        this.btnTopRightAction = byPlatform(driver, androidHeaderSearch(), iosHeaderSearch());
        this.btnSuKien = byPlatform(driver, androidSuKien(), iosSuKien());
        this.btnQuanhDay = byPlatform(driver, androidQuanhDay(), iosQuanhDay());
        this.lblMvNoiBat = byPlatform(driver, androidMvNoiBat(), iosMvNoiBat());
    }

    private static By androidTitleTrangChu() {
        return AppiumBy.xpath(
                "//android.view.View[@content-desc='Trang chủ' and @clickable='false']");
    }

    private static By iosTitleTrangChu() {
        return AppiumBy.xpath("//XCUIElementTypeStaticText[@name='Trang chủ']");
    }

    private static By androidHeaderSearch() {
        return AppiumBy.xpath(
                "//android.view.View[@content-desc='Trang chủ' and @clickable='false']"
                        + "/following-sibling::android.widget.ImageView[1]");
    }

    /** Một Image 30×30 sau StaticText tiêu đề (dump iOS Trang chủ). */
    private static By iosHeaderSearch() {
        return AppiumBy.xpath(
                "//XCUIElementTypeStaticText[@name='Trang chủ']/following-sibling::XCUIElementTypeImage[1]");
    }

    private static By androidSuKien() {
        return AppiumBy.accessibilityId("Sự kiện");
    }

    private static By iosSuKien() {
        return AppiumBy.accessibilityId("Sự kiện");
    }

    private static By androidQuanhDay() {
        return AppiumBy.accessibilityId("Quanh đây");
    }

    private static By iosQuanhDay() {
        return AppiumBy.accessibilityId("Quanh đây");
    }

    private static By androidMvNoiBat() {
        return AppiumBy.accessibilityId("MV nổi bật");
    }

    private static By iosMvNoiBat() {
        return AppiumBy.accessibilityId("MV nổi bật");
    }

    public base.BottomNav nav() {
        return bottomNav;
    }

    public boolean isLoaded() {
        return isDisplayed(lblTitle);
    }

    public void clickTopRightAction() {
        click(btnTopRightAction);
    }

    public void clickSuKien() {
        click(btnSuKien);
    }

    public void clickQuanhDay() {
        click(btnQuanhDay);
    }

    public boolean isMvNoiBatDisplayed() {
        return isDisplayed(lblMvNoiBat);
    }
}
