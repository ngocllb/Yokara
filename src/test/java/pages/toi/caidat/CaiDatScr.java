package pages.toi.caidat;

import base.BaseScr;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.By;

public class CaiDatScr extends BaseScr {

    public CaiDatScr(AppiumDriver driver) {
        super(driver);
    }

    /* ================= LOCATORS ================= */

    private final By ungDungHay = AppiumBy.accessibilityId("Ứng dụng hay");
    private final By baoCaoLoi = AppiumBy.accessibilityId("Báo cáo lỗi");
    private final By donDep = AppiumBy.accessibilityId("Dọn dẹp");
    private final By nguoiBiChan = AppiumBy.accessibilityId("Người bị chặn");
    private final By quyenRiengTu = AppiumBy.accessibilityId("Quyền riêng tư");
    private final By chinhSach = AppiumBy.accessibilityId("Chính sách");
    private final By ngonNgu = AppiumBy.accessibilityId("Ngôn ngữ");
    private final By phienBan = AppiumBy.xpath("//android.widget.ImageView[contains(@content-desc,'Phiên bản')]");
    private final By huyTaiKhoan = AppiumBy.accessibilityId("Huỷ tài khoản");
    private final By chuyenTaiKhoan = AppiumBy.accessibilityId("Chuyển đổi tài khoản");
    private final By dangXuat = AppiumBy.accessibilityId("Đăng xuất");

    /* ================= VERIFY ================= */

    public boolean isSettingPageDisplayed() {
        return isDisplayed(quyenRiengTu);
    }

    /* ================= ACTION ================= */

    public void openUngDungHay() {
        click(ungDungHay);
    }

    public void openBaoCaoLoi() {
        click(baoCaoLoi);
    }

    public void openDonDep() {
        click(donDep);
    }

    public void openNguoiBiChan() {
        click(nguoiBiChan);
    }

    public void openQuyenRiengTu() {
        click(quyenRiengTu);
    }

    public void openChinhSach() {
        click(chinhSach);
    }

    public void openNgonNgu() {
        click(ngonNgu);
    }

    public void openPhienBan() {
        click(phienBan);
    }

    public void clickHuyTaiKhoan() {
        scrollToElement(huyTaiKhoan);
        click(huyTaiKhoan);
    }

    public void clickChuyenTaiKhoan() {
        scrollToElement(chuyenTaiKhoan);
        click(chuyenTaiKhoan);
    }

    public LogoutPopup clickDangXuat() {

        scrollToElement(dangXuat);

        click(dangXuat);

        return new LogoutPopup(driver);
    }


}
