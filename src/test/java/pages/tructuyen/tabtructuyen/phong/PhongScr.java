package pages.tructuyen.tabtructuyen.phong;

import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;

import base.BaseScr;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;

/**
 * PhongScr – Màn hình bên trong phòng Live sau khi bấm vào tên phòng.
 *
 * <p>Xuất hiện ngay sau khi tạo phòng thành công và người dùng bấm vào
 * tên phòng đó trong danh sách "Của tôi".</p>
 */
public class PhongScr extends BaseScr {

    // ─── In-room common elements ──────────────────────────────────────────────

    /** Nút thoát / đóng phòng (góc trên trái) */
    private final By btnBack = AppiumBy.xpath(
            "//android.widget.ImageView[@content-desc='Quay lại'] " +
            "| //android.widget.Button[@content-desc='Quay lại'] " +
            "| //android.view.View[@content-desc='Quay lại']");

    /** Nút micro/mute trong phòng */
    private final By btnMicro = AppiumBy.xpath(
            "//*[contains(@content-desc,'Micro') or contains(@content-desc,'micro')]");

    /** Nút chat / bình luận trong phòng */
    private final By btnChat = AppiumBy.xpath(
            "//*[contains(@content-desc,'Bình luận') or contains(@content-desc,'Chat') or contains(@content-desc,'chat')]");

    /** Nút rời phòng / kết thúc */
    private final By btnLeave = AppiumBy.xpath(
            "//*[contains(@content-desc,'Rời phòng') or contains(@content-desc,'Kết thúc') or contains(@content-desc,'Thoát')]");

    /** Nút mở Thông tin phòng (ivRoomKara – góc trên header) */
    private final By btnThongTinPhong = AppiumBy.id("com.yokara.v3:id/ivRoomKara");

    public PhongScr(AppiumDriver driver) {
        super(driver);
    }

    // ─── Checks ───────────────────────────────────────────────────────────────

    /** Số ký tự tối đa dùng để tìm phòng (content-desc truncate tên dài) */
    private static final int SEARCH_NAME_MAX_LEN = 18;

    private String safeSearchName(String name) {
        if (name == null) return "";
        return name.length() > SEARCH_NAME_MAX_LEN
                ? name.substring(0, SEARCH_NAME_MAX_LEN)
                : name;
    }

    /**
     * Kiểm tra màn hình phòng đã load bằng cách verify tên phòng xuất hiện.
     *
     * @param roomName tên phòng cần verify (sẽ tự truncate cho an toàn)
     */
    public boolean isLoaded(String roomName) {
        String searchName = safeSearchName(roomName);
        By roomTitle = AppiumBy.xpath(
                "//*[contains(@content-desc,'" + searchName + "') " +
                "or contains(@text,'" + searchName + "')]");
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(roomTitle));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Kiểm tra tên phòng đang hiển thị trong header của màn hình phòng.
     */
    public boolean isRoomTitleDisplayed(String roomName) {
        return isLoaded(roomName);
    }

    // ─── Actions ──────────────────────────────────────────────────────────────

    /** Bấm nút Quay lại / Back để thoát khỏi phòng */
    public CuaToiScr goBack() {
        click(btnBack);
        return new CuaToiScr(driver);
    }

    /** Bấm nút Micro (mute / unmute) */
    public void toggleMicro() {
        click(btnMicro);
    }

    /** Bấm vào khu vực Chat / Bình luận */
    public void openChat() {
        click(btnChat);
    }

    /** Bấm Rời phòng / Kết thúc để thoát */
    public CuaToiScr leaveRoom() {
        click(btnLeave);
        return new CuaToiScr(driver);
    }

    /**
     * Mở màn hình Thông tin phòng {@link ThongTinPhongScr}.
     *
     * <p>Bấm vào nút/icon Thông tin (ⓘ) hoặc tên phòng trên header
     * để xem và chỉnh sửa thông tin phòng.</p>
     *
     * @return ThongTinPhongScr – màn hình thông tin phòng
     */
    public ThongTinPhongScr openThongTinPhong() {
        click(btnThongTinPhong);
        return new ThongTinPhongScr(driver);
    }
}
