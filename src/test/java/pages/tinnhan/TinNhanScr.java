package pages.tinnhan;

import base.BaseScr;
import base.BottomNav;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.By;

/**
 * Tab Tin nhắn — ưu tiên {@code accessibilityId} (trùng {@code content-desc} Android / dump iOS).
 */
public class TinNhanScr extends BaseScr {

    private final By lblTinNhanHeader = AppiumBy.accessibilityId("Tin nhắn");
    private final By btnSearchHeader;

    private final By itemThongBao = AppiumBy.xpath("//*[contains(@name, 'Thông báo') or contains(@label, 'Thông báo')]");
    private final By itemNghe = AppiumBy.xpath("//*[contains(@name, 'Nghe') or contains(@label, 'Nghe')]");
    private final By itemThich = AppiumBy.xpath("//*[contains(@name, 'Thích') or contains(@label, 'Thích')]");
    private final By itemBinhLuan = AppiumBy.xpath("//*[contains(@name, 'Bình luận') or contains(@label, 'Bình luận')]");
    private final By itemQuaTang = AppiumBy.xpath("//*[contains(@name, 'Quà tặng') or contains(@label, 'Quà tặng')]");
    private final By itemHoTro = AppiumBy.xpath("//*[contains(@name, 'Hỗ trợ') or contains(@label, 'Hỗ trợ')]");

    private final By itemTinNhanNguoiLa = AppiumBy.xpath("//*[contains(@name, 'Tin nhắn từ người lạ') or contains(@label, 'Tin nhắn từ người lạ')]");

    private final BottomNav bottomNav;

    public TinNhanScr(AppiumDriver driver) {
        super(driver);
        this.bottomNav = new BottomNav(driver);
        this.btnSearchHeader = byPlatform(driver, androidSearchHeader(), iosSearchHeader());
    }

    /** Icon tìm: không có content-desc cố định trên Android (scripts/xml_dumps); iOS: sibling sau tiêu đề. */
    private static By androidSearchHeader() {
        return AppiumBy.xpath(
                "//android.view.View[@content-desc='Tin nhắn']/parent::*//android.widget.ImageView[last()]");
    }

    private static By iosSearchHeader() {
        return AppiumBy.xpath(
                "(//XCUIElementTypeStaticText[@name='Tin nhắn'])[1]/following-sibling::XCUIElementTypeImage[1]");
    }

    private static String escXPath(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.replace("'", "''");
    }

    public BottomNav nav() {
        return bottomNav;
    }

    public boolean isLoaded() {
        return isDisplayed(lblTinNhanHeader);
    }

    public void clickSearchHeader() {
        click(btnSearchHeader);
    }

    public void openThongBao() {
        click(itemThongBao);
    }

    public void openNghe() {
        click(itemNghe);
    }

    public void openThich() {
        click(itemThich);
    }

    public void openBinhLuan() {
        click(itemBinhLuan);
    }

    public void openQuaTang() {
        click(itemQuaTang);
    }

    public void openHoTro() {
        click(itemHoTro);
    }

    public void openTinNhanNguoiLa() {
        click(itemTinNhanNguoiLa);
    }

    /**
     * Mở một cuộc hội thoại theo tên user
     */
    public void openChatWithUser(String userName) {
        String esc = escXPath(userName);
        click(AppiumBy.xpath(
                "//*[contains(@content-desc, '" + esc + "') or contains(@name, '" + esc + "') "
                        + "or contains(@label, '" + esc + "') or contains(@value, '" + esc + "')]"));
    }
}
