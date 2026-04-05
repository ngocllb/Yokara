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
 * Tab Song ca (trong mục Hát) — Android + iOS.
 */
public class SongCaScr extends BaseScr {

    private final By lblBaiHat;
    private final By lblSongCa;
    private final By btnSearchHeader;
    private final By txtSearchBox;

    private final BottomNav bottomNav;

    public SongCaScr(AppiumDriver driver) {
        super(driver);
        this.bottomNav = new BottomNav(driver);
        this.lblBaiHat = byLabeledText("Bài hát");
        this.lblSongCa = byLabeledText("Song ca");
        this.btnSearchHeader = buildSearchHeader(driver);
        this.txtSearchBox = searchPlaceholder();
    }

    private static By byLabeledText(String label) {
        return AppiumBy.xpath(
                "//*[contains(@content-desc, '" + label + "')"
                        + " or contains(@name, '" + label + "')"
                        + " or contains(@label, '" + label + "')"
                        + " or contains(@value, '" + label + "')]"
        );
    }

    private static By buildSearchHeader(AppiumDriver driver) {
        String android = "//android.view.View[@content-desc='Bài hát']/parent::*//android.widget.ImageView[last()]";
        if (driver instanceof IOSDriver) {
            return AppiumBy.xpath(
                    android
                            + " | //XCUIElementTypeNavigationBar//XCUIElementTypeButton[last()]"
                            + " | //XCUIElementTypeNavigationBar//XCUIElementTypeImage[last()]"
                            + " | //XCUIElementTypeOther[.//XCUIElementTypeStaticText[contains(@name,'Bài hát')]]"
                            + "//XCUIElementTypeButton[1]"
            );
        }
        return AppiumBy.xpath(android);
    }

    /** Placeholder ô tìm (Song ca). */
    private static By searchPlaceholder() {
        String hint = "Tìm kiếm bài hát";
        return AppiumBy.xpath(
                "//*[contains(@content-desc,'" + hint + "') or contains(@name,'" + hint + "') or contains(@label,'" + hint + "')]"
                        + " | //*[@content-desc='Tìm kiếm bài hát chờ song ca' or @name='Tìm kiếm bài hát chờ song ca']"
        );
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

    private static String firstNonBlank(String... parts) {
        for (String p : parts) {
            if (p != null && !p.isBlank()) {
                return p.trim();
            }
        }
        return null;
    }

    public BottomNav nav() {
        return bottomNav;
    }

    public boolean isLoaded() {
        return isDisplayed(txtSearchBox) || isDisplayed(lblSongCa);
    }

    public HatScr clickBaiHatTab() {
        click(lblBaiHat);
        return new HatScr(driver);
    }

    public void clickSearchHeader() {
        click(btnSearchHeader);
    }

    public void clickSearchBox() {
        click(txtSearchBox);
    }

    /**
     * Tìm bài chờ song ca — thu thập text khớp.
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
     * Chọn bài để song ca — Android: View content-desc; iOS: name/label.
     */
    public void selectSong(String songName) {
        String xpath;
        if (driver instanceof IOSDriver) {
            if (songName == null || songName.trim().isEmpty()) {
                xpath = "(//*[contains(@name,'Song ca') or contains(@label,'Song ca')])[1]";
            } else {
                String es = escXPath(songName.trim());
                xpath = String.format(
                        "(//*[contains(@content-desc,'%s') or contains(@name,'%s') or contains(@label,'%s')]"
                                + "//*[contains(@name,'Song ca') or contains(@label,'Song ca')])[1]",
                        es, es, es
                );
            }
        } else {
            if (songName == null || songName.trim().isEmpty()) {
                xpath = "(//android.view.View[.//android.view.View[@content-desc='Song ca']])[1]"
                        + "//android.view.View[@content-desc='Song ca']";
            } else {
                xpath = String.format(
                        "(//*[contains(@content-desc, '%s')]//android.view.View[@content-desc='Song ca'])[1]",
                        escXPath(songName.trim())
                );
            }
        }
        click(AppiumBy.xpath(xpath));
    }
}
