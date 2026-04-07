package pages.tructuyen.tabtructuyen.phong;

import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;

import base.BaseScr;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;

public class TaoPhongScr extends BaseScr {

    // Vùng đặt tên phòng
    private final By txtTenPhong = AppiumBy.accessibilityId("Đặt tên phòng");
    
    // Chế độ Công khai / Riêng tư — Android: ImageView; iOS: name/label
    private final By btnModeSelector = AppiumBy.xpath(
            "//*[contains(@content-desc, 'Công khai') or contains(@content-desc, 'Riêng tư')"
                    + " or contains(@name, 'Công khai') or contains(@name, 'Riêng tư')"
                    + " or contains(@label, 'Công khai') or contains(@label, 'Riêng tư')]");

    private final By optRiengTu = AppiumBy.xpath(
            "//*[@content-desc='Riêng tư' or @name='Riêng tư' or @label='Riêng tư']");

    // Nút submit — Android: Button; iOS: XCUIElementTypeButton
    private final By btnTaoPhongSubmit = AppiumBy.xpath(
            "//android.widget.Button[@content-desc='Tạo phòng']"
                    + " | //XCUIElementTypeButton[contains(@name,'Tạo phòng') or contains(@label,'Tạo phòng')]");

    // Nút Xác nhận (popup mật khẩu khi tạo phòng Riêng tư)
    private final By btnXacNhan = AppiumBy.accessibilityId("Xác nhận");

    // Label popup nhập mật khẩu
    private final By lblNhapMatKhau = AppiumBy.xpath(
            "//*[contains(@content-desc,'mật khẩu') or contains(@text,'mật khẩu')"
                    + " or contains(@name,'mật khẩu') or contains(@label,'mật khẩu')]");

    public TaoPhongScr(AppiumDriver driver) {
        super(driver);
    }

    public CuaToiScr createRoom(String roomName, String privateStatus, String password) {
        // 1. Nhập tên phòng
        select(txtTenPhong);
        By lblSuaTen = AppiumBy.accessibilityId("Sửa tên");
        if (isDisplayed(lblSuaTen)) {
            By txtEditName = byPlatform(driver, 
                    AppiumBy.className("android.widget.EditText"), 
                    AppiumBy.xpath("//XCUIElementTypeTextField | //XCUIElementTypeSearchField"));
            By btnLuu = AppiumBy.accessibilityId("Lưu");
            type(txtEditName, roomName);
            click(btnLuu);
        } else {
            type(txtTenPhong, roomName);
        }

        // 2. Chọn chế độ phòng
        if ("PRIVATE".equalsIgnoreCase(privateStatus)) {
            // Bấm vào ImageView chế độ hiện tại (đang hiện "Công khai") để mở dropdown
            click(btnModeSelector);
            // Chọn Riêng tư
            click(optRiengTu);

            // Bấm nút Tạo phòng (Button, không phải parent View)
            click(btnTaoPhongSubmit);

            // 3. Xử lý popup nhập mật khẩu 4 số
            if (password != null && password.length() == 4) {
                // Đợi popup "Vui lòng đặt lại một mật khẩu gồm 4 số" xuất hiện
                wait.until(ExpectedConditions.visibilityOfElementLocated(lblNhapMatKhau));

                By txtPassword = byPlatform(driver,
                        AppiumBy.className("android.widget.EditText"),
                        AppiumBy.xpath("(//XCUIElementTypeSecureTextField)[1] | (//XCUIElementTypeTextField)[1]"));
                type(txtPassword, password);

                // Bấm Xác nhận để hoàn tất tạo phòng
                click(btnXacNhan);
            }
        } else {
            // Mặc định là Công khai, chỉ cần nhấn Tạo phòng
            click(btnTaoPhongSubmit);
        }

        return new CuaToiScr(driver);
    }
}

