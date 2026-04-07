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
 * Tab Tôi khi đã đăng nhập — profile (UID, Tác phẩm, Cài đặt, …).
 * Không phải màn chọn phương thức đăng nhập — xem {@link pages.toi.login.LoginMethodScr}.
 */
public class ToiProfileScr extends BaseScr {

    /** Android: {@code ImageView} UID (content-desc số); iOS: {@code XCUIElementTypeImage} có {@code name}/{@code label} là UID (dump {@code ToiProfileScr_ios.xml}). */
    private final By userIdAnchor;
    /** Android: {@code ImageView[2]} header; iOS: icon phải header — hai {@code Image} không {@code name}/{@code label} (trái = back, phải = Cài đặt). */
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

    /** Khách: cùng semantics {@code content-desc} / label với {@link pages.toi.ToiGuestScr}. */
    private final By btnDangNhapKhach = AppiumBy.accessibilityId("Đăng nhập");

    private final BottomNav bottomNav;

    public ToiProfileScr(AppiumDriver driver) {
        super(driver);
        this.bottomNav = new BottomNav(driver);
        this.userIdAnchor = buildUserIdAnchor(driver);
        this.btnSetting = buildBtnSetting(driver);
        this.tabTacPham = byPlatform(
                driver,
                AppiumBy.xpath("//android.view.View[contains(@content-desc,'Tác phẩm')]"),
                AppiumBy.xpath("//XCUIElementTypeOther[contains(@name,'Tác phẩm')]"));
        this.tabMoiSongCa = byPlatform(
                driver,
                AppiumBy.xpath("//android.view.View[contains(@content-desc,'Mời song ca')]"),
                AppiumBy.xpath("//XCUIElementTypeStaticText[contains(@name,'Mời song ca')]"));
        this.btnBanNhap = AppiumBy.accessibilityId("Bản nháp");
        this.btnGioQua = AppiumBy.xpath("//android.widget.ImageView[contains(@content-desc,'Giỏ quà')]");
        this.btnNhiemVu = AppiumBy.accessibilityId("Nhiệm vụ");
    }

    /**
     * Android: {@code ToiProfileScr_android.txt} — UID là {@code ImageView} có {@code content-desc} chỉ chữ số.
     * iOS: {@code scripts/xml_dumps/ios/ToiProfileScr_ios.xml} — UID là {@code ImageView} {@code name=label} số (vd. 5955400), không phải {@code StaticText} tên hiển thị.
     */
    private static By buildUserIdAnchor(AppiumDriver driver) {
        if (driver instanceof IOSDriver) {
            return AppiumBy.xpath(
                    "//XCUIElementTypeImage[@name and @label and @name = @label "
                            + "and string-length(@name) >= 4 "
                            + "and string-length(translate(@name, '0123456789', '')) = 0]");
        }
        return AppiumBy.xpath(
                "//android.widget.ImageView["
                        + "@content-desc and string-length(@content-desc) >= 4 "
                        + "and string-length(translate(@content-desc, '0123456789', '')) = 0]");
    }

    /**
     * Android: icon Cài đặt góc phải header — {@code ancestor[3]} của tab Tác phẩm → {@code ImageView[2]}.
     * iOS: hai {@code XCUIElementTypeImage} không {@code name}/{@code label} trên cùng hàng (trái back, phải Cài đặt) — dump thực tế, không có {@code NavigationBar}.
     */
    private static By buildBtnSetting(AppiumDriver driver) {
        By android = AppiumBy.xpath(
                "//android.view.View[contains(@content-desc,'Tác phẩm')]/ancestor::android.view.View[3]"
                        + "/android.widget.ImageView[2]");
        if (driver instanceof IOSDriver) {
            return AppiumBy.xpath(
                    "//XCUIElementTypeImage[not(@name) and not(@label)]"
                            + "[preceding-sibling::XCUIElementTypeImage[not(@name) and not(@label)]]");
        }
        return android;
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
