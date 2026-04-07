package pages.tructuyen.tabtructuyen;

import org.openqa.selenium.By;

import base.BaseScr;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.ios.IOSDriver;
import pages.tructuyen.tabkhampha.KhamPhaScr;
import pages.tructuyen.tabtructuyen.phong.CuaToiScr;
import pages.tructuyen.tabtructuyen.phong.TaoPhongScr;

/**
 * Tab Trực tuyến — label/nút dùng {@code accessibilityId} khi semantics trùng Android/iOS;
 * nút tìm (icon) tách {@code byPlatform} theo dump.
 */
public class TrucTuyenScr extends BaseScr {

    private final By lblTrucTuyen;
    private final By lblKhamPha;

    private final By btnSearch;

    private final By btnTaoPhong;
    private final By btnBangXepHang;
    private final By btnNhiemVu;

    private final By tabDeCu;
    private final By tabGanDay;
    private final By tabCuaToi;

    private final base.BottomNav bottomNav;

    public TrucTuyenScr(AppiumDriver driver) {
        super(driver);
        bottomNav = new base.BottomNav(driver);
        this.lblTrucTuyen = AppiumBy.accessibilityId("Trực tuyến");
        this.lblKhamPha = AppiumBy.accessibilityId("Khám phá");
        this.btnSearch = byPlatform(driver, androidBtnSearch(), iosBtnSearch());
        this.btnTaoPhong = AppiumBy.accessibilityId("Tạo phòng");
        this.btnBangXepHang = AppiumBy.accessibilityId("Bảng xếp hạng");
        this.btnNhiemVu = AppiumBy.accessibilityId("Nhiệm vụ");
        this.tabDeCu = AppiumBy.accessibilityId("Đề cử");
        this.tabGanDay = AppiumBy.accessibilityId("Gần đây");
        this.tabCuaToi = AppiumBy.accessibilityId("Của tôi");
    }

    private static By androidBtnSearch() {
        return AppiumBy.xpath(
                "//android.view.View[@content-desc='Khám phá']/following-sibling::android.widget.ImageView[last()]");
    }

    private static By iosBtnSearch() {
        return AppiumBy.xpath(
                "//XCUIElementTypeStaticText[@name='Khám phá']/following-sibling::XCUIElementTypeImage[last()]");
    }

    /** Màn kết quả trống — một label accessibility (Flutter semantics) cho cả hai nền tảng. */
    private static By lblKhongCoPhong() {
        return AppiumBy.accessibilityId("Không có phòng");
    }

    private static By androidSearchInput() {
        return AppiumBy.className("android.widget.EditText");
    }

    /** Trường nhập sau khi mở tìm kiếm — một SearchField (dump iOS). */
    private static By iosSearchInput() {
        return AppiumBy.xpath("//XCUIElementTypeSearchField");
    }

    /** Một dòng phòng trong danh sách kết quả (Android). */
    private static By androidRoomRowById(String id) {
        String esc = escXPath(id);
        return AppiumBy.xpath(
                "//android.view.View[@clickable='true' and contains(@content-desc, '" + esc + "')]");
    }

    /** Một dòng phòng trong danh sách kết quả (iOS). */
    private static By iosRoomRowById(String id) {
        String esc = escXPath(id);
        return AppiumBy.xpath("//*[contains(@name, '" + esc + "')]");
    }

    private static String escXPath(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.replace("'", "''");
    }

    public base.BottomNav nav() {
        return bottomNav;
    }

    public boolean isLoaded() {
        return isDisplayed(lblTrucTuyen);
    }

    public void clickSearch() {
        click(btnSearch);
    }

    /**
     * Tìm phòng theo ID — ô nhập và dòng kết quả: một locator từng nền, không OR nội bộ.
     */
    public Object enterRoom(String id) {
        clickSearch();

        By searchInput = byPlatform(driver, androidSearchInput(), iosSearchInput());
        type(searchInput, id);

        driver.executeScript("mobile: performEditorAction", java.util.Map.of("action", "search"));

        By noRoomLoc = lblKhongCoPhong();
        By roomLoc = driver instanceof IOSDriver ? iosRoomRowById(id) : androidRoomRowById(id);

        try {
            wait.until(org.openqa.selenium.support.ui.ExpectedConditions.or(
                    org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated(roomLoc),
                    org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated(noRoomLoc)
            ));
        } catch (Exception e) {
            // chờ isDisplayed bên dưới
        }

        if (isDisplayed(noRoomLoc)) {
            return noRoomLoc;
        }

        click(roomLoc);
        return new pages.tructuyen.tabtructuyen.phong.PhongScr(driver);
    }

    public KhamPhaScr clickKhamPha() {
        click(lblKhamPha);
        return new KhamPhaScr(driver);
    }

    public TaoPhongScr clickTaoPhong() {
        click(btnTaoPhong);
        return new TaoPhongScr(driver);
    }
    
    public TaoPhongScr clickTaoPhong(java.util.function.Consumer<pages.toi.ToiGuestScr> guestHandler) {
        click(btnTaoPhong);
        pages.toi.ToiGuestScr guestPage = new pages.toi.ToiGuestScr(driver);
        if (guestPage.isGuestPageDisplayed() && guestHandler != null) {
            guestHandler.accept(guestPage);
            // Sau khi handler kết thúc (tức là đã đăng nhập xong), mình bấm lại Tạo Phòng
            click(btnTaoPhong);
        }
        return new TaoPhongScr(driver);
    }

    public BangXepHangScr clickBangXepHang() {
        click(btnBangXepHang);
        return new BangXepHangScr(driver);
    }

    public TrungTamNhiemVuScr clickNhiemVu() {
        click(btnNhiemVu);
        return new TrungTamNhiemVuScr(driver);
    }

    public CuaToiScr createRoom(String roomName, String privateStatus, String password) {
        return clickTaoPhong().createRoom(roomName, privateStatus, password);
    }

    public void switchTabDeCu() {
        click(tabDeCu);
    }

    public void switchTabGanDay() {
        click(tabGanDay);
    }

    public void switchTabCuaToi() {
        click(tabCuaToi);
    }
}
