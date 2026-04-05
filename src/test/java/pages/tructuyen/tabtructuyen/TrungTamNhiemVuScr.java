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
        this.lblTrungTamNhiemVu = byLabeledText("Trung tâm nhiệm vụ");
        this.lblNhiemVu = byLabeledText("Nhiệm vụ");
    }

    private static By byLabeledText(String label) {
        return AppiumBy.xpath(
                "//*[contains(@content-desc, '" + label + "')"
                        + " or contains(@name, '" + label + "')"
                        + " or contains(@label, '" + label + "')"
                        + " or contains(@value, '" + label + "')]"
        );
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
