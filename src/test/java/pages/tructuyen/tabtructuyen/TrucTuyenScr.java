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
 * Tab Trực tuyến — mỗi control: một locator Android và một locator iOS (bắt từ hierarchy thật),
 * không xâu OR/dự phòng trong cùng một nền tảng.
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
        this.lblTrucTuyen = byPlatform(driver, androidLblTrucTuyen(), iosLblTrucTuyen());
        this.lblKhamPha = byPlatform(driver, androidLblKhamPha(), iosLblKhamPha());
        this.btnSearch = byPlatform(driver, androidBtnSearch(), iosBtnSearch());
        this.btnTaoPhong = byPlatform(driver, androidTaoPhong(), iosTaoPhong());
        this.btnBangXepHang = byPlatform(driver, androidBangXepHang(), iosBangXepHang());
        this.btnNhiemVu = byPlatform(driver, androidNhiemVu(), iosNhiemVu());
        this.tabDeCu = byPlatform(driver, androidTabDeCu(), iosTabDeCu());
        this.tabGanDay = byPlatform(driver, androidTabGanDay(), iosTabGanDay());
        this.tabCuaToi = byPlatform(driver, androidTabCuaToi(), iosTabCuaToi());
    }

    private static By androidLblTrucTuyen() {
        return AppiumBy.xpath("//android.view.View[@content-desc='Trực tuyến']");
    }

    private static By iosLblTrucTuyen() {
        return AppiumBy.xpath("//XCUIElementTypeStaticText[@name='Trực tuyến']");
    }

    private static By androidLblKhamPha() {
        return AppiumBy.xpath("//android.view.View[@content-desc='Khám phá']");
    }

    private static By iosLblKhamPha() {
        return AppiumBy.xpath("//XCUIElementTypeStaticText[@name='Khám phá']");
    }

    private static By androidBtnSearch() {
        return AppiumBy.xpath(
                "//android.view.View[@content-desc='Khám phá']/following-sibling::android.widget.ImageView[last()]");
    }

    private static By iosBtnSearch() {
        return AppiumBy.xpath(
                "//XCUIElementTypeStaticText[@name='Khám phá']/following-sibling::XCUIElementTypeImage[last()]");
    }

    /** Android: content-desc từ dump; Appium map sang accessibilityId. */
    private static By androidTaoPhong() {
        return AppiumBy.accessibilityId("Tạo phòng");
    }

    private static By iosTaoPhong() {
        return AppiumBy.accessibilityId("Tạo phòng");
    }

    private static By androidBangXepHang() {
        return AppiumBy.accessibilityId("Bảng xếp hạng");
    }

    private static By iosBangXepHang() {
        return AppiumBy.accessibilityId("Bảng xếp hạng");
    }

    private static By androidNhiemVu() {
        return AppiumBy.accessibilityId("Nhiệm vụ");
    }

    private static By iosNhiemVu() {
        return AppiumBy.accessibilityId("Nhiệm vụ");
    }

    private static By androidTabDeCu() {
        return AppiumBy.xpath("//android.view.View[@content-desc='Đề cử']");
    }

    private static By iosTabDeCu() {
        return AppiumBy.xpath("//XCUIElementTypeStaticText[@name='Đề cử']");
    }

    private static By androidTabGanDay() {
        return AppiumBy.xpath("//android.view.View[@content-desc='Gần đây']");
    }

    private static By iosTabGanDay() {
        return AppiumBy.xpath("//XCUIElementTypeStaticText[@name='Gần đây']");
    }

    private static By androidTabCuaToi() {
        return AppiumBy.xpath("//android.view.View[@content-desc='Của tôi']");
    }

    private static By iosTabCuaToi() {
        return AppiumBy.xpath("//XCUIElementTypeStaticText[@name='Của tôi']");
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
        return AppiumBy.xpath("//XCUIElementTypeCell[contains(@name, '" + esc + "')]");
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
