package pages.tinnhan;

import base.BaseScr;
import base.BottomNav;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.By;

/**
 * Tab Tin nhắn — mọi nền tảng: resource-id (nếu có) → accessibility id → XPath.
 */
public class TinNhanScr extends BaseScr {

    private final By lblTinNhanHeader;
    private final By btnSearchHeader;

    private final By itemThongBao;
    private final By itemNghe;
    private final By itemThich;
    private final By itemBinhLuan;
    private final By itemQuaTang;
    private final By itemHoTro;

    private final By itemTinNhanNguoiLa;

    private final BottomNav bottomNav;

    public TinNhanScr(AppiumDriver driver) {
        super(driver);
        this.bottomNav = new BottomNav(driver);
        this.lblTinNhanHeader = byIdThenFallback(driver, null, "Tin nhắn", AppiumBy.xpath(labeledXPath("Tin nhắn")));
        this.btnSearchHeader = byIdThenFallback(driver, null, "Tìm kiếm", buildSearchHeaderFallback());
        this.itemThongBao = byIdThenFallback(driver, null, "Thông báo", AppiumBy.xpath(labeledXPath("Thông báo")));
        this.itemNghe = byIdThenFallback(driver, null, "Nghe", AppiumBy.xpath(labeledXPath("Nghe")));
        this.itemThich = byIdThenFallback(driver, null, "Thích", AppiumBy.xpath(labeledXPath("Thích")));
        this.itemBinhLuan = byIdThenFallback(driver, null, "Bình luận", AppiumBy.xpath(labeledXPath("Bình luận")));
        this.itemQuaTang = byIdThenFallback(driver, null, "Quà tặng", AppiumBy.xpath(labeledXPath("Quà tặng")));
        this.itemHoTro = byIdThenFallback(driver, null, "Hỗ trợ", AppiumBy.xpath(labeledXPath("Hỗ trợ")));
        this.itemTinNhanNguoiLa = byIdThenFallback(
                driver, null, "Tin nhắn từ người lạ", AppiumBy.xpath(labeledXPath("Tin nhắn từ người lạ")));
    }

    private static By buildSearchHeaderFallback() {
        return AppiumBy.xpath(
                "//android.widget.ImageView[@bounds='[965,124][1038,197]']"
                        + " | //XCUIElementTypeNavigationBar//XCUIElementTypeButton[last()]"
                        + " | //XCUIElementTypeNavigationBar//XCUIElementTypeImage[last()]"
                        + " | //XCUIElementTypeOther[.//XCUIElementTypeStaticText[contains(@name,'Tin nhắn')]]"
                        + "//XCUIElementTypeButton[1]"
        );
    }

    private static String labeledXPath(String fragment) {
        String esc = escXPath(fragment);
        return "//*[contains(@content-desc, '" + esc + "')"
                + " or contains(@name, '" + esc + "')"
                + " or contains(@label, '" + esc + "')"
                + " or contains(@value, '" + esc + "')]";
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
        click(byIdThenFallback(driver, null, userName, AppiumBy.xpath(labeledXPath(userName))));
    }
}
