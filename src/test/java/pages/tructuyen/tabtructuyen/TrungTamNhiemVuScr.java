package pages.tructuyen.tabtructuyen;

import base.BaseScr;
import base.BottomNav;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * Trung tâm nhiệm vụ (từ tab Trực tuyến) — Android + iOS.
 */
public class TrungTamNhiemVuScr extends BaseScr {

    /** Một số bản build dùng tiêu đề đầy đủ, một số chỉ “Nhiệm vụ”. */
    private final By lblTrungTamNhiemVu;
    private final By lblNhiemVu;

    private final BottomNav bottomNav;

    public TrungTamNhiemVuScr(AppiumDriver driver) {
        super(driver);
        this.bottomNav = new BottomNav(driver);
        this.lblTrungTamNhiemVu = AppiumBy.accessibilityId("Trung tâm nhiệm vụ");
        this.lblNhiemVu = AppiumBy.accessibilityId("Nhiệm vụ");
    }

    public BottomNav nav() {
        return bottomNav;
    }

    public boolean isLoaded() {
        try {
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.visibilityOfElementLocated(lblTrungTamNhiemVu),
                    ExpectedConditions.visibilityOfElementLocated(lblNhiemVu)
            ));
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
