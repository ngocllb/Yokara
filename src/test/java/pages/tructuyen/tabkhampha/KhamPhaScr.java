package pages.tructuyen.tabkhampha;

import base.BaseScr;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.ios.IOSDriver;
import org.openqa.selenium.By;

/**
 * Khám phá (con tab Trực tuyến) — Android (content-desc) + iOS (name, label, value).
 */
public class KhamPhaScr extends BaseScr {

    private final By lblTrucTuyen;
    private final By lblKhamPha;

    private final By btnSearch;

    private final By btnXuHuong;
    private final By btnSanhGame;
    private final By btnDanhCa;
    private final By btnSongCa;

    private final By lblTopDaiGia;
    private final By btnXemThemTopDaiGia;
    private final By lblTopBaiThu;

    private final base.BottomNav bottomNav;

    public KhamPhaScr(AppiumDriver driver) {
        super(driver);
        bottomNav = new base.BottomNav(driver);
        this.lblTrucTuyen = AppiumBy.accessibilityId("Trực tuyến");
        this.lblKhamPha = AppiumBy.accessibilityId("Khám phá");
        this.btnSearch = buildSearchButton(driver);
        this.btnXuHuong = AppiumBy.accessibilityId("Xu hướng");
        this.btnSanhGame = AppiumBy.accessibilityId("Sảnh game");
        this.btnDanhCa = AppiumBy.accessibilityId("Danh ca");
        this.btnSongCa = AppiumBy.accessibilityId("Song ca");
        this.lblTopDaiGia = AppiumBy.accessibilityId("Top đại gia");
        this.btnXemThemTopDaiGia = buildXemThemTopDaiGia(driver);
        this.lblTopBaiThu = buildTopBaiThuSection();
    }

    /** Cùng cấu trúc header {@link pages.tructuyen.tabtructuyen.TrucTuyenScr#iosBtnSearch()}. */
    private static By buildSearchButton(AppiumDriver driver) {
        String android = "//android.view.View[@content-desc='Khám phá']/following-sibling::android.widget.ImageView[last()]";
        if (driver instanceof IOSDriver) {
            return AppiumBy.xpath(
                    "//XCUIElementTypeStaticText[@name='Khám phá']/following-sibling::XCUIElementTypeImage[last()]");
        }
        return AppiumBy.xpath(android);
    }

    /** “Xem thêm” trong khối Top đại gia. */
    private static By buildXemThemTopDaiGia(AppiumDriver driver) {
        String android = "//android.view.View[@content-desc='Top đại gia']//android.view.View[@content-desc='Xem thêm']";
        if (driver instanceof IOSDriver) {
            return AppiumBy.xpath(
                    android
                            + " | //*[contains(@name,'Top đại gia') or contains(@label,'Top đại gia')]"
                            + "//*[contains(@name,'Xem thêm') or contains(@label,'Xem thêm')]"
                            + " | //XCUIElementTypeStaticText[contains(@name,'Top đại gia')]/following::*"
                            + "[contains(@name,'Xem thêm') or contains(@label,'Xem thêm')][1]"
            );
        }
        return AppiumBy.xpath(android);
    }

    /**
     * Khu “Top bài thu” / “Xem thêm” — bản cũ dùng accessibility một dòng có xuống dòng.
     */
    private static By buildTopBaiThuSection() {
        return AppiumBy.accessibilityId("Top bài thu\nXem thêm");
    }

    public base.BottomNav nav() {
        return bottomNav;
    }

    public boolean isLoaded() {
        return isDisplayed(lblKhamPha);
    }

    public void clickSearch() {
        click(btnSearch);
    }

    public void clickTrucTuyen() {
        click(lblTrucTuyen);
    }

    public void clickXuHuong() {
        click(btnXuHuong);
    }

    public void clickSanhGame() {
        click(btnSanhGame);
    }

    public void clickDanhCa() {
        click(btnDanhCa);
    }

    public void clickSongCa() {
        click(btnSongCa);
    }

    public void clickXemThemTopDaiGia() {
        click(btnXemThemTopDaiGia);
    }

    public boolean isTopDaiGiaDisplayed() {
        return isDisplayed(lblTopDaiGia);
    }

    public void clickTopBaiThuSection() {
        click(lblTopBaiThu);
    }
}
