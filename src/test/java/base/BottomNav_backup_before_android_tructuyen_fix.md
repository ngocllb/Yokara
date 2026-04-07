# Backup `BottomNav` before Android fix

Luu ban backup truoc khi tang do on dinh tab `Truc tuyen` tren Android.

```java
package base;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.ios.IOSDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import pages.trangchu.TrangChuScr;
import pages.tructuyen.tabtructuyen.TrucTuyenScr;
import pages.hat.HatScr;
import pages.tinnhan.TinNhanScr;
import pages.toi.ToiProfileScr;

import utils.GestureUtils;

import java.time.Duration;

public class BottomNav extends BaseScr {
    private final By tabTrangChu = AppiumBy.accessibilityId("Trang chủ");
    private final By tabTrucTuyen = AppiumBy.accessibilityId("Trực tuyến");
    private final By tabTrucTuyenIosFallback = AppiumBy.xpath(
            "//XCUIElementTypeImage[@name='Trang chủ']/following-sibling::XCUIElementTypeImage[1]");
    private final By tabTrucTuyenAfterTrangChuAny = AppiumBy.xpath(
            "//*[@name='Trang chủ']/following-sibling::*[1]");
    private final By tabHat;
    private final By tabTinNhan;
    private final By tabToi = AppiumBy.accessibilityId("Tôi");
    private final By tabToiIosFallback = AppiumBy.xpath(
            "//XCUIElementTypeImage[@name='Trực tuyến']/following-sibling::XCUIElementTypeImage[3]");

    public BottomNav(AppiumDriver driver) {
        super(driver);
        tabHat = byPlatform(driver, androidTabHat(), iosTabHat());
        tabTinNhan = byPlatform(driver, androidTabTinNhan(), iosTabTinNhan());
    }

    private static By androidTabHat() {
        return AppiumBy.xpath("//android.widget.ImageView[@content-desc='Trực tuyến']/following-sibling::android.widget.ImageView[1]");
    }

    private static By iosTabHat() {
        return AppiumBy.xpath("//XCUIElementTypeImage[@name='Trực tuyến']/following-sibling::XCUIElementTypeImage[1]");
    }

    private static By androidTabTinNhan() {
        return AppiumBy.xpath("//android.widget.ImageView[@content-desc='Trực tuyến']/following-sibling::android.widget.ImageView[2]");
    }

    private static By iosTabTinNhan() {
        return AppiumBy.xpath("//XCUIElementTypeImage[@name='Trực tuyến']/following-sibling::*[2]");
    }

    // ... backup đầy đủ lưu tại commit này
}
```
