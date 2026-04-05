package pages.tructuyen.tabtructuyen;

import base.BaseScr;
import base.BottomNav;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.By;

/**
 * Màn Bảng xếp hạng (từ tab Trực tuyến).
 * <p>iOS: tiêu đề là {@code XCUIElementTypeStaticText} name {@code Bảng xếp hạng} (Appium source khi đang ở màn).
 * Android: cùng chuỗi semantics → {@code accessibilityId}; nếu build dùng toolbar native khác, bắt lại dump và chỉnh đúng một locator.</p>
 */
public class BangXepHangScr extends BaseScr {

    private final By lblBangXepHang;

    private final BottomNav bottomNav;

    public BangXepHangScr(AppiumDriver driver) {
        super(driver);
        this.bottomNav = new BottomNav(driver);
        this.lblBangXepHang = byPlatform(driver, androidLblBangXepHang(), iosLblBangXepHang());
    }

    private static By androidLblBangXepHang() {
        return AppiumBy.accessibilityId("Bảng xếp hạng");
    }

    private static By iosLblBangXepHang() {
        return AppiumBy.xpath("//XCUIElementTypeStaticText[@name='Bảng xếp hạng']");
    }

    public BottomNav nav() {
        return bottomNav;
    }

    public boolean isLoaded() {
        return isDisplayed(lblBangXepHang);
    }
}
