package pages.hat;

import base.BaseScr;
import base.BottomNav;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.ios.IOSDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

/**
 * Tab Hát — Bài hát / Song ca.
 * <p>Đối chiếu {@code XML android Screen locator/HatScr_android.txt}: icon tìm là {@code ImageView}
 * ngay sau {@code content-desc='Song ca'}; ô tìm có {@code content-desc='Nhập tên bài hát, thành viên hoặc ID'}.</p>
 */
public class HatScr extends BaseScr {

    private static final String PLACEHOLDER_TIM_KIEM =
            "Nhập tên bài hát, thành viên hoặc ID";

    private final By lblBaiHat;
    private final By lblSongCa;
    private final By btnSearchHeader;

    private final By txtSearchBox;
    private final By btnXemThemThanTuong;

    private final BottomNav bottomNav;

    public HatScr(AppiumDriver driver) {
        super(driver);
        this.bottomNav = new BottomNav(driver);
        this.lblBaiHat = AppiumBy.accessibilityId("Bài hát");
        this.lblSongCa = AppiumBy.accessibilityId("Song ca");
        this.btnSearchHeader = byPlatform(driver, androidSearchIconHeader(), iosSearchIconHeader());
        this.txtSearchBox = AppiumBy.accessibilityId(PLACEHOLDER_TIM_KIEM);
        this.btnXemThemThanTuong = AppiumBy.accessibilityId("Xem thêm");
    }

    /**
     * Android: {@code HatScr_android.txt} — {@code ImageView} liền sau tab Song ca (không có {@code content-desc}).
     */
    static By androidSearchIconHeader() {
        return AppiumBy.xpath(
                "//android.view.View[@content-desc='Song ca']/following-sibling::android.widget.ImageView[1]");
    }

    /**
     * iOS: cùng thứ tự header với Trực tuyến (StaticText Bài hát / Song ca → phần tử tìm kiếm kế tiếp).
     */
    static By iosSearchIconHeader() {
        return AppiumBy.xpath(
                "//XCUIElementTypeStaticText[@name='Bài hát']"
                        + "/following-sibling::XCUIElementTypeStaticText[@name='Song ca']"
                        + "/following-sibling::*[1]");
    }

    private static String escXPath(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.replace("'", "''");
    }

    private By searchInputField() {
        if (driver instanceof IOSDriver) {
            return AppiumBy.xpath(
                    "(//XCUIElementTypeSearchField)[1] | (//XCUIElementTypeTextField)[1]"
            );
        }
        return AppiumBy.className("android.widget.EditText");
    }

    public BottomNav nav() {
        return bottomNav;
    }

    public boolean isLoaded() {
        return isDisplayed(lblBaiHat);
    }

    public SongCaScr clickSongCaTab() {
        click(lblSongCa);
        return new SongCaScr(driver);
    }

    public void clickSearchHeader() {
        click(btnSearchHeader);
    }

    public void clickSearchBox() {
        click(txtSearchBox);
    }

    public void clickXemThemThanTuong() {
        click(btnXemThemThanTuong);
    }

    /**
     * Tìm user theo UID — kết quả đầu tiên + đọc tên.
     */
    public String searchUserByUid(String uid) {
        clickSearchBox();
        By searchInput = searchInputField();
        driver.findElement(searchInput).sendKeys(uid);

        String eid = escXPath(uid);
        By firstResult = AppiumBy.xpath(
                "(//*[contains(@content-desc, '" + eid + "') or contains(@text, '" + eid + "') "
                        + "or contains(@name, '" + eid + "') or contains(@label, '" + eid + "')])[1]"
        );
        click(firstResult);

        By userNameObj = AppiumBy.xpath(
                "//android.view.View[@content-desc='Đăng nhập ID']/parent::*//android.widget.TextView"
                        + " | //*[contains(@name,'Đăng nhập ID') or contains(@label,'Đăng nhập ID')]"
                        + "/following::XCUIElementTypeStaticText[1]"
                        + " | //XCUIElementTypeStaticText[contains(@name,'Đăng nhập ID')]/../*[self::XCUIElementTypeStaticText]"
        );
        WebElement el = driver.findElement(userNameObj);
        String t = el.getText();
        if (t == null || t.isBlank()) {
            t = firstNonBlank(el.getAttribute("name"), el.getAttribute("label"), el.getAttribute("value"));
        }
        return t != null ? t : "";
    }

    private static String firstNonBlank(String... parts) {
        for (String p : parts) {
            if (p != null && !p.isBlank()) {
                return p.trim();
            }
        }
        return null;
    }

    /**
     * Tìm bài hát — thu thập text khớp trên cả hai nền.
     */
    public List<String> searchSong(String songName) {
        clickSearchBox();
        By searchInput = searchInputField();
        driver.findElement(searchInput).sendKeys(songName);

        String es = escXPath(songName);
        By firstResult = AppiumBy.xpath(
                "(//*[contains(@content-desc, '" + es + "') or contains(@text, '" + es + "') "
                        + "or contains(@name, '" + es + "') or contains(@label, '" + es + "')])[1]"
        );
        click(firstResult);

        By results = AppiumBy.xpath(
                "//*[contains(@text, '" + es + "') or contains(@content-desc, '" + es + "') "
                        + "or contains(@name, '" + es + "') or contains(@label, '" + es + "')]"
        );
        List<WebElement> elements = driver.findElements(results);
        List<String> values = new ArrayList<>();
        for (WebElement e : elements) {
            String text = e.getText();
            if (text == null || text.isEmpty()) {
                text = e.getAttribute("content-desc");
            }
            if (text == null || text.isEmpty()) {
                text = firstNonBlank(e.getAttribute("name"), e.getAttribute("label"), e.getAttribute("value"));
            }
            if (text != null && text.contains(songName)) {
                values.add(text);
            }
        }
        return values;
    }

    /**
     * Chọn bài trong tab (carousel) — Android dùng content-desc; iOS dùng name/label.
     */
    public void selectSongInTab(String tabName, String songName) {
        String eTab = escXPath(tabName);
        String xpath;
        if (driver instanceof IOSDriver) {
            if (songName == null || songName.trim().isEmpty()) {
                xpath = String.format(
                        "(//*[contains(@name,'%s') or contains(@label,'%s')]"
                                + "/following::*[contains(@name,'Hát') or contains(@label,'Hát')])[1]",
                        eTab, eTab
                );
            } else {
                String eSong = escXPath(songName.trim());
                xpath = String.format(
                        "(//*[contains(@name,'%s') or contains(@label,'%s')]"
                                + "/following::*[contains(@name,'%s') or contains(@label,'%s')]"
                                + "[.//*[contains(@name,'Hát') or contains(@label,'Hát')]])[1]",
                        eTab, eTab, eSong, eSong
                );
            }
        } else {
            if (songName == null || songName.trim().isEmpty()) {
                xpath = String.format(
                        "(//*[@content-desc='%s']/following-sibling::*//android.view.View[@content-desc='Hát'])[1]",
                        tabName
                );
            } else {
                xpath = String.format(
                        "(//*[@content-desc='%s']/following-sibling::*[contains(@content-desc, '%s')]"
                                + "//android.view.View[@content-desc='Hát'])[1]",
                        tabName, escXPath(songName.trim())
                );
            }
        }
        click(AppiumBy.xpath(xpath));
    }
}
