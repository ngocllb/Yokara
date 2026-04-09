package pages.tructuyen.tabtructuyen.phong;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.ExpectedConditions;

import base.BaseScr;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.ios.IOSDriver;

/**
 * CuaToiScr – Danh sách phòng "Của tôi" (sub-tab trong Trực tuyến).
 *
 * <p>Lưu ý: content-desc của card phòng có dạng multi-line:
 * {@code "Talk\n0\n<tên phòng bị cắt ngắn>"}
 * Tên phòng dài sẽ bị truncate trong content-desc, nên cần dùng
 * prefix ngắn hơn khi tìm kiếm.</p>
 */
public class CuaToiScr extends BaseScr {

    /** Số ký tự tối đa dùng để tìm phòng (iOS/Android có thể truncate tên dài trong card). */
    private static final int SEARCH_NAME_MAX_LEN = 24;
    private final By tabCuaToi = AppiumBy.accessibilityId("Của tôi");
    private final By roomCardCandidates = AppiumBy.xpath(
            "//*[contains(@content-desc,'Talk') or contains(@name,'Talk') or contains(@label,'Talk')]");
    private final By btnSearch;
    private final By txtSearchInput;
    private final By lblKhongCoPhong = AppiumBy.accessibilityId("Không có phòng");

    public CuaToiScr(AppiumDriver driver) {
        super(driver);
        this.btnSearch = byPlatform(driver, androidBtnSearch(), iosBtnSearch());
        this.txtSearchInput = byPlatform(driver, AppiumBy.className("android.widget.EditText"),
                AppiumBy.xpath("//XCUIElementTypeSearchField"));
    }

    private static By androidBtnSearch() {
        // Theo dump Android tab Trực tuyến/Của tôi: icon search nằm cạnh "Khám phá".
        return AppiumBy.xpath(
                "//android.view.View[@content-desc='Khám phá']/following-sibling::android.widget.ImageView[1]");
    }

    private static By iosBtnSearch() {
        // Ưu tiên xpath tương đối theo text "Khám phá"; absolute xpath làm fallback cuối.
        return AppiumBy.xpath(
                "(//XCUIElementTypeStaticText[@name='Khám phá']/following-sibling::XCUIElementTypeImage[1])"
                        + " | (//XCUIElementTypeWindow/XCUIElementTypeOther/XCUIElementTypeOther/"
                        + "XCUIElementTypeOther/XCUIElementTypeOther[1]/XCUIElementTypeOther/"
                        + "XCUIElementTypeOther/XCUIElementTypeOther/XCUIElementTypeOther[2]/"
                        + "XCUIElementTypeOther[2]/XCUIElementTypeOther[2]/XCUIElementTypeImage[1])");
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    /**
     * Content-desc của card phòng bị truncate tên dài.
     * Lấy prefix ngắn để contains() vẫn match được.
     */
    private String safeSearchName(String roomName) {
        if (roomName == null) return "";
        return roomName.length() > SEARCH_NAME_MAX_LEN
                ? roomName.substring(0, SEARCH_NAME_MAX_LEN)
                : roomName;
    }

    private String escXPath(String raw) {
        return raw == null ? "" : raw.replace("'", "''");
    }

    /** Key nhận diện ổn định đặt ở đầu tên phòng (vd: rabc123). */
    private String roomKey(String roomName) {
        if (roomName == null) {
            return "";
        }
        String t = roomName.trim();
        if (t.isEmpty()) {
            return "";
        }
        int space = t.indexOf(' ');
        return space > 0 ? t.substring(0, space) : t;
    }

    /**
     * Build XPath locator tìm element có content-desc hoặc name chứa tên phòng (truncated).
     */
    private By roomLocator(String roomName) {
        String searchName = escXPath(safeSearchName(roomName));
        String key = escXPath(roomKey(roomName));
        return AppiumBy.xpath(
                "//*[contains(@content-desc, '" + searchName + "') "
                        + "or contains(@name, '" + searchName + "') "
                        + "or contains(@label, '" + searchName + "') "
                        + "or contains(@content-desc, '" + key + "') "
                        + "or contains(@name, '" + key + "') "
                        + "or contains(@label, '" + key + "') "
                        + "or (self::XCUIElementTypeOther and contains(@name,'Talk') and contains(@name, '" + searchName + "')) "
                        + "or (self::XCUIElementTypeOther and contains(@label,'Talk') and contains(@label, '" + searchName + "')) "
                        + "or (self::XCUIElementTypeOther and contains(@name,'Talk') and contains(@name, '" + key + "')) "
                        + "or (self::XCUIElementTypeOther and contains(@label,'Talk') and contains(@label, '" + key + "'))]");
    }

    private String attr(org.openqa.selenium.WebElement e, String name) {
        try {
            String v = e.getAttribute(name);
            return v == null ? "" : v.trim();
        } catch (Exception ignored) {
            return "";
        }
    }

    private void logRoomCardsSnapshot(String expectedName, int attempt) {
        try {
            java.util.List<org.openqa.selenium.WebElement> cards = finds(roomCardCandidates);
            String key = roomKey(expectedName);
            String searchName = safeSearchName(expectedName);
            System.out.println("[CuaToiScr] attempt=" + attempt
                    + " | expected=" + expectedName
                    + " | key=" + key
                    + " | searchName=" + searchName
                    + " | cards=" + cards.size());
            int max = Math.min(cards.size(), 8);
            for (int i = 0; i < max; i++) {
                org.openqa.selenium.WebElement c = cards.get(i);
                String line = attr(c, "content-desc");
                if (line.isEmpty()) {
                    line = attr(c, "name");
                }
                if (line.isEmpty()) {
                    line = attr(c, "label");
                }
                System.out.println("[CuaToiScr] card[" + i + "]=" + line.replace("\n", " | "));
            }
        } catch (Exception e) {
            System.out.println("[CuaToiScr] snapshot failed: " + e.getMessage());
        }
    }

    // ─── Checks ───────────────────────────────────────────────────────────────

    /**
     * Kiểm tra tên phòng có hiển thị trong danh sách hay không.
     * Thực hiện scroll tìm kiếm nếu cần.
     *
     * @param expectedName tên phòng cần tìm (sẽ tự truncate cho an toàn)
     */
    public boolean isRoomNameDisplayed(String expectedName) {
        By locator = roomLocator(expectedName);

        // Đảm bảo đang đứng đúng tab "Của tôi".
        try {
            click(tabCuaToi);
        } catch (Exception ignored) {
            // Một số build đã ở sẵn tab, không cần fail vì thao tác này.
        }

        // Sau khi tạo phòng, danh sách thường cập nhật chậm vài giây.
        // Poll trước, KHÔNG scroll sớm để tránh cuộn mất card mới tạo.
        for (int attempt = 0; attempt < 16; attempt++) {
            if (isDisplayed(locator)) {
                return true;
            }

            if (attempt == 3 || attempt == 7 || attempt == 11 || attempt == 15) {
                logRoomCardsSnapshot(expectedName, attempt);
            }

            try {
                Thread.sleep(700);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        // Fallback cuối cùng: dùng flow scroll chuẩn đang có.
        try {
            // Nếu card mới đã bị đẩy xuống, thử cuộn tìm một lần.
            scrollToElement(locator);
            return isDisplayed(locator);
        } catch (Exception ignored) {
            logRoomCardsSnapshot(expectedName, 999);
            return false;
        }
    }

    private void submitSearch() {
        if (driver instanceof IOSDriver) {
            find(txtSearchInput).sendKeys("\n");
        } else {
            driver.executeScript("mobile: performEditorAction", java.util.Map.of("action", "search"));
        }
    }

    /**
     * Verify phòng bằng search trong tab Của tôi để tránh nhiễu do danh sách nhiều card.
     */
    public boolean isRoomNameDisplayedBySearch(String expectedName) {
        By locator = roomLocator(expectedName);
        try {
            click(tabCuaToi);
        } catch (Exception ignored) {
        }

        click(btnSearch);
        type(txtSearchInput, expectedName);
        submitSearch();

        try {
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.visibilityOfElementLocated(locator),
                    ExpectedConditions.visibilityOfElementLocated(lblKhongCoPhong)
            ));
        } catch (TimeoutException ignored) {
        }
        return isDisplayed(locator) && !isDisplayed(lblKhongCoPhong);
    }

    // ─── Actions ──────────────────────────────────────────────────────────────

    /**
     * Bấm vào card phòng trong danh sách để vào màn hình phòng {@link PhongScr}.
     *
     * <p>Card phòng là element clickable chứa tên phòng.</p>
     *
     * @param roomName tên phòng cần click (sẽ tự truncate cho an toàn)
     * @return PhongScr – màn hình bên trong phòng
     */
    public PhongScr clickRoom(String roomName) {
        String searchName = escXPath(safeSearchName(roomName));
        String key = escXPath(roomKey(roomName));
        // Target element chứa tên phòng - generic XPath to work on both platforms
        By clickableRoom = AppiumBy.xpath(
                "//*[contains(@content-desc, '" + searchName + "') "
                        + "or contains(@name, '" + searchName + "') "
                        + "or contains(@label, '" + searchName + "') "
                        + "or contains(@content-desc, '" + key + "') "
                        + "or contains(@name, '" + key + "') "
                        + "or contains(@label, '" + key + "') "
                        + "or (self::XCUIElementTypeOther and contains(@name,'Talk') and contains(@name, '" + searchName + "')) "
                        + "or (self::XCUIElementTypeOther and contains(@label,'Talk') and contains(@label, '" + searchName + "')) "
                        + "or (self::XCUIElementTypeOther and contains(@name,'Talk') and contains(@name, '" + key + "')) "
                        + "or (self::XCUIElementTypeOther and contains(@label,'Talk') and contains(@label, '" + key + "'))]");
        click(clickableRoom);
        return new PhongScr(driver);
    }
}
