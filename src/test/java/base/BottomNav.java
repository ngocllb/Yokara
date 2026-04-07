package base;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.ios.IOSDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.ExpectedConditions;

import pages.trangchu.TrangChuScr;
import pages.tructuyen.tabtructuyen.TrucTuyenScr;
import pages.hat.HatScr;
import pages.tinnhan.TinNhanScr;
import pages.toi.ToiProfileScr;

/**
 * Thanh điều hướng dưới — các tab có {@code content-desc} / {@code name} trùng chuỗi dùng chung
 * {@link AppiumBy#accessibilityId} (đối chiếu {@code XML android Screen locator/HatScr_android.txt} dòng tab bar).
 * <p>Riêng <strong>Hát</strong> và <strong>Tin nhắn</strong> (badge đổi số): Android không có desc cố định / chuỗi khác iOS → {@link #byPlatform}.</p>
 */
public class BottomNav extends BaseScr {

    private final By tabTrangChu = AppiumBy.accessibilityId("Trang chủ");
    private final By tabTrucTuyen = AppiumBy.accessibilityId("Trực tuyến");
    private final By tabHat;
    private final By tabTinNhan;
    private final By tabToi = AppiumBy.accessibilityId("Tôi");
    /** iOS: cùng hàng tab với dump {@code scripts/xml_dumps/ios/ToiProfileScr_ios.xml} (Hát → Tin nhắn → Tôi). */
    private final By tabToiIosFallback = AppiumBy.xpath(
            "//XCUIElementTypeImage[@name='Trực tuyến']/following-sibling::XCUIElementTypeImage[3]");

    public BottomNav(AppiumDriver driver) {
        super(driver);
        tabHat = byPlatform(driver, androidTabHat(), iosTabHat());
        tabTinNhan = byPlatform(driver, androidTabTinNhan(), iosTabTinNhan());
    }

    private static By androidTabHat() {
        return AppiumBy.xpath(
                "//android.widget.ImageView[@content-desc='Trực tuyến']"
                        + "/following-sibling::android.widget.ImageView[1]");
    }

    private static By iosTabHat() {
        return AppiumBy.xpath(
                "//XCUIElementTypeImage[@name='Trực tuyến']/following-sibling::XCUIElementTypeImage[1]");
    }

    private static By androidTabTinNhan() {
        return AppiumBy.xpath(
                "//android.widget.ImageView[@content-desc='Trực tuyến']"
                        + "/following-sibling::android.widget.ImageView[2]");
    }

    private static By iosTabTinNhan() {
        return AppiumBy.xpath(
                "//XCUIElementTypeImage[@name='Trực tuyến']/following-sibling::*[2]");
    }

    /** iOS: icon tab đôi khi {@code visible=false} nhưng vẫn tap — dùng presence + click. */
    private void tapBottomTab(By tab) {
        if (driver instanceof IOSDriver) {
            // iOS: tap bằng presence và đợi transition
            wait.until(ExpectedConditions.presenceOfElementLocated(tab)).click();
            try { Thread.sleep(500); } catch (InterruptedException ignored) {} // Minimal stable pause for iOS
        } else {
            click(tab);
        }
    }

    public TrangChuScr goToTrangChu() {
        tapBottomTab(tabTrangChu);
        return new TrangChuScr(driver);
    }

    public TrucTuyenScr goToTrucTuyen() {
        tapBottomTab(tabTrucTuyen);
        return new TrucTuyenScr(driver);
    }

    public HatScr goToHat() {
        tapBottomTab(tabHat);
        return new HatScr(driver);
    }

    public TinNhanScr goToTinNhan() {
        tapBottomTab(tabTinNhan);
        return new TinNhanScr(driver);
    }

    public ToiProfileScr goToToi() {
        if (driver instanceof IOSDriver) {
            try {
                tapBottomTab(tabToi);
            } catch (TimeoutException e) {
                tapBottomTab(tabToiIosFallback);
            }
        } else {
            tapBottomTab(tabToi);
        }
        return new ToiProfileScr(driver);
    }
}
