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
        this.lblTrucTuyen = byLabeledText("Trực tuyến");
        this.lblKhamPha = byLabeledText("Khám phá");
        this.btnSearch = buildSearchButton(driver);
        this.btnXuHuong = byLabeledControl("Xu hướng");
        this.btnSanhGame = byLabeledControl("Sảnh game");
        this.btnDanhCa = byLabeledControl("Danh ca");
        this.btnSongCa = byLabeledControl("Song ca");
        this.lblTopDaiGia = byLabeledText("Top đại gia");
        this.btnXemThemTopDaiGia = buildXemThemTopDaiGia(driver);
        this.lblTopBaiThu = buildTopBaiThuSection();
    }

    private static By byLabeledText(String label) {
        return AppiumBy.xpath(
                "//*[contains(@content-desc, '" + label + "')"
                        + " or contains(@name, '" + label + "')"
                        + " or contains(@label, '" + label + "')"
                        + " or contains(@value, '" + label + "')]"
        );
    }

    private static By byLabeledControl(String label) {
        return AppiumBy.xpath(
                "//*[@content-desc='" + label + "' or @name='" + label + "' or @label='" + label + "' or @value='" + label + "']"
                        + " | //*[contains(@content-desc, '" + label + "') or contains(@name, '" + label + "') "
                        + "or contains(@label, '" + label + "') or contains(@value, '" + label + "')]"
        );
    }

    private static By buildSearchButton(AppiumDriver driver) {
        String android = "//android.view.View[@content-desc='Khám phá']/parent::*//android.widget.ImageView[last()]";
        if (driver instanceof IOSDriver) {
            return AppiumBy.xpath(
                    android
                            + " | //XCUIElementTypeNavigationBar//XCUIElementTypeButton[last()]"
                            + " | //XCUIElementTypeNavigationBar//XCUIElementTypeImage[last()]"
                            + " | //XCUIElementTypeOther[.//XCUIElementTypeStaticText[contains(@name,'Khám phá')]]"
                            + "//XCUIElementTypeButton[1]"
            );
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
        String nl = "\n";
        return AppiumBy.xpath(
                "//*[contains(@content-desc,'Top bài thu') or contains(@name,'Top bài thu') or contains(@label,'Top bài thu')]"
                        + "[contains(@content-desc,'Xem thêm') or contains(@name,'Xem thêm') or contains(@label,'Xem thêm')]"
                        + " | //*[contains(@content-desc,'Top bài thu" + nl + "Xem thêm')]"
                        + " | //*[contains(@name,'Top bài thu')]"
        );
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
