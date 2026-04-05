package pages.tructuyen.tabtructuyen.phong;

import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;

import base.BaseScr;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;

/**
 * ThongTinPhongScr – Màn hình Thông tin phòng.
 *
 * <p>Xuất hiện khi người dùng mở phần thông tin của phòng đang vào.
 * Hiển thị: tên phòng, quyền riêng tư (PUBLIC / PRIVATE),
 * đổi mật khẩu (chỉ khi PRIVATE), và nội dung thông báo.</p>
 *
 * <pre>
 * Luồng điển hình:
 *   PhongScr → openThongTinPhong() → ThongTinPhongScr
 *   ThongTinPhongScr.changePassword("newPass")   // chỉ khi PRIVATE
 *   ThongTinPhongScr.changeNotification("text")  // đổi nội dung thông báo
 * </pre>
 */
public class ThongTinPhongScr extends BaseScr {

    // ─── Header ───────────────────────────────────────────────────────────────

    /** Nút Quay lại từ màn hình Thông tin phòng → PhongScr */
    private final By btnBack = AppiumBy.xpath(
            "//*[@content-desc='Quay lại' or @content-desc='Back']");

    // ─── Tên phòng ────────────────────────────────────────────────────────────

    /**
     * Label tên phòng (dynamic – khởi tạo bằng roomName lúc runtime,
     * xem {@link #isRoomNameDisplayed(String)}).
     */
    private By roomNameLabel(String roomName) {
        return AppiumBy.xpath(
                "//*[contains(@content-desc,'" + roomName + "') " +
                "or contains(@text,'" + roomName + "')]");
    }

    // ─── Quyền riêng tư ───────────────────────────────────────────────────────

    /** Badge / nhãn "Công khai" */
    private final By lblCongKhai = AppiumBy.xpath(
            "//*[@content-desc='Công khai' or @text='Công khai']");

    /** Badge / nhãn "Riêng tư" */
    private final By lblRiengTu = AppiumBy.xpath(
            "//*[@content-desc='Riêng tư' or @text='Riêng tư']");

    // ─── Đổi mật khẩu (chỉ khi PRIVATE) ─────────────────────────────────────

    /** Row "Đổi mật khẩu phòng" (chỉ hiện khi phòng Riêng tư) */
    private final By rowDoiMatKhau = AppiumBy.xpath(
            "//*[contains(@content-desc,'Đổi mật khẩu') or contains(@text,'Đổi mật khẩu')]");

    /** Ô nhập mật khẩu mới (xuất hiện sau khi bấm vào row Đổi mật khẩu) */
    private final By txtMatKhauMoi = AppiumBy.className("android.widget.EditText");

    /** Nút Lưu / Xác nhận đổi mật khẩu */
    private final By btnLuuMatKhau = AppiumBy.xpath(
            "//*[@content-desc='Lưu' or @text='Lưu' " +
            "or @content-desc='Xác nhận' or @text='Xác nhận' " +
            "or @content-desc='OK' or @text='OK']");

    // ─── Thông báo ────────────────────────────────────────────────────────────

    /** Vùng Thông báo (có thể click để đổi nội dung) */
    private final By rowThongBao = AppiumBy.xpath(
            "//*[contains(@content-desc,'Thông báo') or contains(@text,'Thông báo')]");

    /** Ô nhập nội dung thông báo mới */
    private final By txtThongBao = AppiumBy.className("android.widget.EditText");

    /** Nút Lưu nội dung thông báo */
    private final By btnLuuThongBao = AppiumBy.xpath(
            "//*[@content-desc='Lưu' or @text='Lưu' " +
            "or @content-desc='Xác nhận' or @text='Xác nhận' " +
            "or @content-desc='OK' or @text='OK']");

    // ─── Constructor ──────────────────────────────────────────────────────────

    public ThongTinPhongScr(AppiumDriver driver) {
        super(driver);
    }

    // ─── Checks ───────────────────────────────────────────────────────────────

    /**
     * Verify tên phòng hiển thị đúng trên màn hình Thông tin phòng.
     *
     * @param roomName tên phòng cần kiểm tra
     */
    public boolean isRoomNameDisplayed(String roomName) {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(roomNameLabel(roomName)));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Kiểm tra phòng đang ở chế độ Công khai (PUBLIC).
     */
    public boolean isPublic() {
        return isDisplayed(lblCongKhai);
    }

    /**
     * Kiểm tra phòng đang ở chế độ Riêng tư (PRIVATE).
     */
    public boolean isPrivate() {
        return isDisplayed(lblRiengTu);
    }

    /**
     * Kiểm tra row "Đổi mật khẩu phòng" có hiển thị không.
     * Chỉ hiện khi phòng là PRIVATE.
     */
    public boolean isChangePasswordVisible() {
        return isDisplayed(rowDoiMatKhau);
    }

    // ─── Actions: Đổi mật khẩu (PRIVATE only) ────────────────────────────────

    /**
     * Đổi mật khẩu phòng.
     *
     * <p>Chỉ gọi khi phòng đang ở chế độ Riêng tư (PRIVATE).
     * Flow: bấm vào row "Đổi mật khẩu" → xoá mật khẩu cũ → nhập mật khẩu mới → bấm Lưu.</p>
     *
     * @param newPassword mật khẩu mới (thường 4 chữ số)
     * @throws RuntimeException nếu phòng đang PUBLIC (không có row đổi mật khẩu)
     */
    public void changePassword(String newPassword) {
        if (!isChangePasswordVisible()) {
            throw new RuntimeException(
                "Không tìm thấy 'Đổi mật khẩu phòng'. " +
                "Chức năng này chỉ khả dụng khi phòng ở chế độ Riêng tư (PRIVATE)."
            );
        }

        // Bấm vào row để mở ô nhập liệu
        click(rowDoiMatKhau);

        // Nhập mật khẩu mới vào EditText
        type(txtMatKhauMoi, newPassword);

        // Lưu
        click(btnLuuMatKhau);
    }

    // ─── Actions: Thông báo ───────────────────────────────────────────────────

    /**
     * Đổi nội dung thông báo của phòng.
     *
     * <p>Flow: bấm vào vùng Thông báo → xoá nội dung cũ → nhập nội dung mới → bấm Lưu.</p>
     *
     * @param newContent nội dung thông báo mới
     */
    public void changeNotification(String newContent) {
        // Bấm vào vùng thông báo để mở ô nhập liệu
        click(rowThongBao);

        // Nhập nội dung mới
        type(txtThongBao, newContent);

        // Lưu
        click(btnLuuThongBao);
    }

    // ─── Navigation ───────────────────────────────────────────────────────────

    /**
     * Quay lại màn hình phòng {@link PhongScr}.
     */
    public PhongScr goBack() {
        click(btnBack);
        return new PhongScr(driver);
    }
}
