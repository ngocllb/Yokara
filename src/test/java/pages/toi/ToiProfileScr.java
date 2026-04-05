package pages.toi;

import base.BaseScr;
import base.BottomNav;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.ios.IOSDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import pages.toi.caidat.CaiDatScr;

/**
 * Tab Tôi / profile — locator dùng chung Android (content-desc, resource-id) và iOS (name, label, value).
 */
public class ToiProfileScr extends BaseScr {

    /** Android: ImageView có content-desc (thường là UID); iOS: text hoặc ảnh header profile. */
    private final By userIdAnchor;
    /**
     * Android: ImageView cạnh dòng Tác phẩm.
     * iOS: nút Cài đặt / biểu tượng setting trong navigation hoặc header.
     */
    private final By btnSetting;

    private final By tabTacPham;
    private final By tabMoiSongCa;

    private final By btnBanNhap;
    private final By btnGioQua;
    private final By btnNhiemVu;

    /** Android + iOS: text VIP trên nhiều thuộc tính. */
    private final By menuVip = AppiumBy.xpath(
            "//*[contains(@content-desc, 'VIP') or contains(@name, 'VIP') or contains(@label, 'VIP') "
                    + "or contains(@value, 'VIP')]");

    /** Khách: nút đăng nhập. */
    private final By btnDangNhapKhach = AppiumBy.xpath(
            "//*[contains(@content-desc, 'Đăng nhập') or contains(@content-desc, 'ĐĂNG NHẬP')"
                    + " or contains(@name, 'Đăng nhập') or contains(@label, 'Đăng nhập') or contains(@value, 'Đăng nhập')]");

    private final BottomNav bottomNav;

    public ToiProfileScr(AppiumDriver driver) {
        super(driver);
        this.bottomNav = new BottomNav(driver);
        this.userIdAnchor = buildUserIdAnchor(driver);
        this.btnSetting = buildBtnSetting(driver);
        this.tabTacPham = byLabeledTab("Tác phẩm");
        this.tabMoiSongCa = byLabeledTab("Mời song ca");
        this.btnBanNhap = byLabeledControl("Bản nháp");
        this.btnGioQua = byLabeledControl("Giỏ quà");
        this.btnNhiemVu = byLabeledControl("Nhiệm vụ");
    }

    private static By buildUserIdAnchor(AppiumDriver driver) {
        if (driver instanceof IOSDriver) {
            return AppiumBy.xpath(
                    "//XCUIElementTypeStaticText["
                            + "string-length(@name)>3 and not(contains(@name,'VIP')) "
                            + "and not(contains(@name,'Tác phẩm')) and not(contains(@name,'Đăng nhập'))"
                            + "][1]"
                            + " | //XCUIElementTypeImage[(@name and string-length(@name)>0) or (@label and string-length(@label)>0)][1]");
        }
        return AppiumBy.xpath("//android.widget.ImageView[@content-desc]");
    }

    private static By buildBtnSetting(AppiumDriver driver) {
        String android =
                "//android.view.View[@content-desc='Tác phẩm']/parent::*//android.widget.ImageView[1]";
        if (driver instanceof IOSDriver) {
            return AppiumBy.xpath(
                    android
                            + " | //XCUIElementTypeButton[contains(@name,'Cài đặt') or contains(@label,'Cài đặt')]"
                            + " | //XCUIElementTypeButton[contains(@name,'Setting') or contains(@label,'Setting')]"
                            + " | //XCUIElementTypeNavigationBar//XCUIElementTypeButton[last()]"
                            + " | //XCUIElementTypeImage[contains(@name,'Cài') or contains(@label,'Cài') or contains(@name,'setting')]");
        }
        return AppiumBy.xpath(android);
    }

    /** Tab phụ (Tác phẩm / Mời song ca): đủ content-desc + name + label + value. */
    private static By byLabeledTab(String label) {
        return AppiumBy.xpath(
                "//*[contains(@content-desc, '" + label + "')"
                        + " or contains(@name, '" + label + "')"
                        + " or contains(@label, '" + label + "')"
                        + " or contains(@value, '" + label + "')]"
        );
    }

    /** Icon nhanh: accessibilityId thường trùng hai nền; thêm xpath dự phòng. */
    private static By byLabeledControl(String label) {
        return AppiumBy.xpath(
                "//*[@content-desc='" + label + "' or @name='" + label + "' or @label='" + label + "' or @value='" + label + "']"
                        + " | //*[contains(@content-desc, '" + label + "') or contains(@name, '" + label + "') "
                        + "or contains(@label, '" + label + "') or contains(@value, '" + label + "')]"
        );
    }

    public String getUserId() {
        WebElement el = find(userIdAnchor);
        if (driver instanceof IOSDriver) {
            String n = firstNonBlank(el.getAttribute("name"), el.getAttribute("label"), el.getAttribute("value"));
            return n != null ? n : "";
        }
        String cd = el.getAttribute("content-desc");
        return cd != null ? cd : "";
    }

    private static String firstNonBlank(String... parts) {
        for (String p : parts) {
            if (p != null && !p.isBlank()) {
                return p.trim();
            }
        }
        return null;
    }

    public boolean isUserIdDisplayed(String uid) {
        if (uid == null || uid.isBlank()) {
            return false;
        }
        String u = uid.trim();
        By userIdLocator;
        if (driver instanceof IOSDriver) {
            userIdLocator = AppiumBy.xpath(
                    "//*[@name='" + escAttr(u) + "' or @label='" + escAttr(u) + "' or @value='" + escAttr(u) + "']"
                            + " | //*[contains(@name,'" + escAttr(u) + "') or contains(@label,'" + escAttr(u) + "')]");
        } else {
            userIdLocator = AppiumBy.xpath(
                    "//android.widget.ImageView[@content-desc='" + escAttr(u) + "']"
                            + " | //*[contains(@content-desc,'" + escAttr(u) + "')]");
        }
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(userIdLocator));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /** Escape literal trong XPath 1.0 (nháy đơn trong chuỗi → lặp đôi). */
    private static String escAttr(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.replace("'", "''");
    }

    public CaiDatScr openSetting() {
        click(btnSetting);
        return new CaiDatScr(driver);
    }

    public void clickTacPhamTab() {
        click(tabTacPham);
    }

    public void clickMoiSongCaTab() {
        click(tabMoiSongCa);
    }

    public void openBanNhap() {
        click(btnBanNhap);
    }

    public void openGioQua() {
        click(btnGioQua);
    }

    public void openNhiemVu() {
        click(btnNhiemVu);
    }

    public BottomNav nav() {
        return bottomNav;
    }

    /** Menu VIP — cùng xpath cho Android và iOS (tránh NSPredicate lệch bản build). */
    public boolean isVipMenuDisplayed() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(menuVip)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Smoke: tab Tôi đã mở — VIP (đã login), tab Tác phẩm, hoặc màn khách (Đăng nhập).
     */
    public boolean isToiTabContentRecognized() {
        try {
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.visibilityOfElementLocated(menuVip),
                    ExpectedConditions.visibilityOfElementLocated(tabTacPham),
                    ExpectedConditions.visibilityOfElementLocated(btnDangNhapKhach)
            ));
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
