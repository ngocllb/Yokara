package base;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.By;

import pages.trangchu.TrangChuScr;
import pages.tructuyen.tabtructuyen.TrucTuyenScr;
import pages.hat.HatScr;
import pages.tinnhan.TinNhanScr;
import pages.toi.ToiProfileScr;

/**
 * Thanh điều hướng dưới — mỗi tab: một XPath Android và một XPath iOS (dump thật, không OR trong cùng nền).
 * <p>Tab “Hát” / badge “Tin nhắn” không có {@code content-desc} cố định trên Android — dùng thứ tự anh em sau tab “Trực tuyến”.
 * iOS “Tin nhắn” bọc trong {@code Other} — dùng anh em thứ hai sau “Trực tuyến”; “Tôi” là {@code Image} thứ hai sau “Trực tuyến”.</p>
 */
public class BottomNav extends BaseScr {

    private final By tabTrangChu;
    private final By tabTrucTuyen;
    private final By tabHat;
    private final By tabTinNhan;
    private final By tabToi;

    public BottomNav(AppiumDriver driver) {
        super(driver);
        tabTrangChu = byPlatform(driver, androidTabTrangChu(), iosTabTrangChu());
        tabTrucTuyen = byPlatform(driver, androidTabTrucTuyen(), iosTabTrucTuyen());
        tabHat = byPlatform(driver, androidTabHat(), iosTabHat());
        tabTinNhan = byPlatform(driver, androidTabTinNhan(), iosTabTinNhan());
        tabToi = byPlatform(driver, androidTabToi(), iosTabToi());
    }

    private static By androidTabTrangChu() {
        return AppiumBy.xpath("//android.widget.ImageView[@content-desc='Trang chủ']");
    }

    private static By iosTabTrangChu() {
        return AppiumBy.xpath("//XCUIElementTypeImage[@name='Trang chủ']");
    }

    private static By androidTabTrucTuyen() {
        return AppiumBy.xpath("//android.widget.ImageView[@content-desc='Trực tuyến']");
    }

    private static By iosTabTrucTuyen() {
        return AppiumBy.xpath("//XCUIElementTypeImage[@name='Trực tuyến']");
    }

    /** Image không có content-desc — vị trí ngay sau tab Trực tuyến (dump Android). */
    private static By androidTabHat() {
        return AppiumBy.xpath(
                "//android.widget.ImageView[@content-desc='Trực tuyến']"
                        + "/following-sibling::android.widget.ImageView[1]");
    }

    private static By iosTabHat() {
        return AppiumBy.xpath(
                "//XCUIElementTypeImage[@name='Trực tuyến']/following-sibling::XCUIElementTypeImage[1]");
    }

    /** Badge đổi số — không bám chuỗi Tin nhắn\\nN; chỉ vị trí cột thứ 4. */
    private static By androidTabTinNhan() {
        return AppiumBy.xpath(
                "//android.widget.ImageView[@content-desc='Trực tuyến']"
                        + "/following-sibling::android.widget.ImageView[2]");
    }

    /** Khối Other bọc icon + badge (dump iOS). */
    private static By iosTabTinNhan() {
        return AppiumBy.xpath(
                "//XCUIElementTypeImage[@name='Trực tuyến']/following-sibling::*[2]");
    }

    private static By androidTabToi() {
        return AppiumBy.xpath(
                "//android.widget.ImageView[@content-desc='Trực tuyến']"
                        + "/following-sibling::android.widget.ImageView[3]");
    }

    /** Image “Tôi” là Image thứ hai sau “Trực tuyến” (sau Image Hát). */
    private static By iosTabToi() {
        return AppiumBy.xpath(
                "//XCUIElementTypeImage[@name='Trực tuyến']/following-sibling::XCUIElementTypeImage[2]");
    }

    public TrangChuScr goToTrangChu() {

        click(tabTrangChu);
        return new TrangChuScr(driver);
    }

    public TrucTuyenScr goToTrucTuyen() {

        click(tabTrucTuyen);
        return new TrucTuyenScr(driver);
    }

    public HatScr goToHat() {

        click(tabHat);
        return new HatScr(driver);
    }

    public TinNhanScr goToTinNhan() {

        click(tabTinNhan);
        return new TinNhanScr(driver);
    }

    public ToiProfileScr goToToi() {

        click(tabToi);
        return new ToiProfileScr(driver);
    }
}
