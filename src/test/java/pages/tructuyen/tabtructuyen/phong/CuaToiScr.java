package pages.tructuyen.tabtructuyen.phong;

import org.openqa.selenium.By;

import base.BaseScr;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;

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

    public CuaToiScr(AppiumDriver driver) {
        super(driver);
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

    // ─── Checks ───────────────────────────────────────────────────────────────

    /**
     * Kiểm tra tên phòng có hiển thị trong danh sách hay không.
     * Thực hiện scroll tìm kiếm nếu cần.
     *
     * @param expectedName tên phòng cần tìm (sẽ tự truncate cho an toàn)
     */
    public boolean isRoomNameDisplayed(String expectedName) {
        try {
            By locator = roomLocator(expectedName);
            // Scroll tìm phòng nếu chưa thấy
            scrollToElement(locator);
            return isDisplayed(locator);
        } catch (Exception e) {
            return false;
        }
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
