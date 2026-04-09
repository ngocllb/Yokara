package pages.tructuyen.tabtructuyen;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.WebDriverWait;

import base.BaseScr;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.ios.IOSDriver;

import java.time.Duration;

import pages.tructuyen.tabkhampha.KhamPhaScr;
import pages.tructuyen.tabtructuyen.phong.CuaToiScr;
import pages.tructuyen.tabtructuyen.phong.PhongScr;
import pages.tructuyen.tabtructuyen.phong.TaoPhongScr;

/**
 * Tab Trực tuyến — label/nút dùng {@code accessibilityId} khi semantics trùng Android/iOS;
 * nút tìm (icon) tách {@code byPlatform} theo dump.
 */
public class TrucTuyenScr extends BaseScr {
    private static final int SEARCH_NAME_MAX_LEN = 24;

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
        return AppiumBy.accessibilityId("Nhập ID, tên phòng");
    }

    private static String safeSearchName(String roomName) {
        if (roomName == null) {
            return "";
        }
        return roomName.length() > SEARCH_NAME_MAX_LEN
                ? roomName.substring(0, SEARCH_NAME_MAX_LEN)
                : roomName;
    }

    private static String roomKey(String roomName) {
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

    /** Một dòng phòng trong danh sách kết quả (Android). */
    private static By androidRoomRowByName(String roomName) {
        String searchName = escXPath(safeSearchName(roomName));
        String key = escXPath(roomKey(roomName));
        return AppiumBy.xpath(
                "//android.view.View[@clickable='true' and ("
                        + "contains(@content-desc, '" + searchName + "') "
                        + "or contains(@content-desc, '" + key + "') "
                        + "or (contains(@content-desc, 'Talk') and contains(@content-desc, '" + key + "'))"
                        + ")]");
    }

    /** Một dòng phòng trong danh sách kết quả (iOS). */
    private static By iosRoomRowByName(String roomName) {
        String searchName = escXPath(safeSearchName(roomName));
        String key = escXPath(roomKey(roomName));
        return AppiumBy.xpath(
                "//*[contains(@name, '" + searchName + "') "
                        + "or contains(@label, '" + searchName + "') "
                        + "or contains(@name, '" + key + "') "
                        + "or contains(@label, '" + key + "') "
                        + "or (self::XCUIElementTypeOther and contains(@name,'Talk') and contains(@name, '" + key + "')) "
                        + "or (self::XCUIElementTypeOther and contains(@label,'Talk') and contains(@label, '" + key + "'))]");
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

    /** iOS: coi là có trong tree (WDA hay {@code visible=false} với icon/text vẫn tap được). */
    private boolean isPresentInDom(By locator) {
        try {
            return !driver.findElements(locator).isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    private boolean trucTuyenMarkersOk(boolean usePresenceOnly) {
        if (usePresenceOnly) {
            boolean base = isPresentInDom(btnTaoPhong) || isPresentInDom(lblKhamPha) || isPresentInDom(tabDeCu)
                    || isPresentInDom(lblTrucTuyen) || isPresentInDom(btnBangXepHang);
            if (base) {
                return true;
            }
            // iOS: Flutter/WDA đôi khi chậm cập nhật tree sau activate — thêm predicate/xpath lỏng
            return iosLoosePresenceMarkers();
        }
        return isDisplayed(btnTaoPhong) || isDisplayed(lblKhamPha) || isDisplayed(tabDeCu)
                || isDisplayed(lblTrucTuyen);
    }

    private boolean iosLoosePresenceMarkers() {
        if (!(driver instanceof IOSDriver)) {
            return false;
        }
        try {
            if (!driver.findElements(AppiumBy.iOSNsPredicateString(
                    "name CONTAINS 'Khám phá' OR name CONTAINS 'Tạo phòng' OR name CONTAINS 'Đề cử'"))
                    .isEmpty()) {
                return true;
            }
        } catch (Exception ignored) {
        }
        return !driver.findElements(AppiumBy.xpath(
                "//*[contains(@name,'Khám phá') or contains(@name,'Tạo phòng') or contains(@name,'Đề cử')]"))
                .isEmpty();
    }

    /**
     * Chờ màn Trực tuyến (nội dung hoặc marker lỏng trên iOS) — dùng chung cho {@link #isLoaded()} và {@link base.BottomNav}.
     */
    public boolean waitForScreenReady(Duration timeout) {
        boolean ios = driver instanceof IOSDriver;
        try {
            WebDriverWait w = new WebDriverWait(driver, timeout);
            w.until(d -> trucTuyenMarkersOk(ios));
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    /**
     * Không chỉ dựa label tab bar "Trực tuyến" — trên iOS icon tab đôi khi {@code isDisplayed() == false}.
     */
    public boolean isLoaded() {
        Duration d = driver instanceof IOSDriver
                ? Duration.ofSeconds(28)
                : Duration.ofSeconds(18);
        return waitForScreenReady(d);
    }

    public void clickSearch() {
        click(btnSearch);
    }

    private void submitRoomSearch(By searchInput) {
        if (driver instanceof IOSDriver) {
            try {
                find(searchInput).sendKeys("\n");
            } catch (Exception ignored) {
            }
            By iosKeyboardSearch = AppiumBy.iOSNsPredicateString(
                    "type == 'XCUIElementTypeButton' AND (name == 'Search' OR name == 'Tìm kiếm')");
            if (isDisplayed(iosKeyboardSearch)) {
                click(iosKeyboardSearch);
            }
            return;
        }
        driver.executeScript("mobile: performEditorAction", java.util.Map.of("action", "search"));
    }

    private void waitRoomResultLoaded(By roomLoc, By noRoomLoc) {
        try {
            wait.until(org.openqa.selenium.support.ui.ExpectedConditions.or(
                    org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated(roomLoc),
                    org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated(noRoomLoc)
            ));
        } catch (Exception ignored) {
            // fallback check bên dưới
        }
    }

    /**
     * Enter phòng theo tên:
     * 1) click Search
     * 2) nhập roomName
     * 3) submit (Enter/Search) và đợi kết quả load
     * 4) click đúng dòng phòng để vào {@link PhongScr}
     */
    public PhongScr enterRoom(String roomName) {
        clickSearch();

        By searchInput = byPlatform(driver, androidSearchInput(), iosSearchInput());
        find(searchInput).clear();
        type(searchInput, roomName);

        submitRoomSearch(searchInput);

        By noRoomLoc = lblKhongCoPhong();
        By roomLoc = driver instanceof IOSDriver ? iosRoomRowByName(roomName) : androidRoomRowByName(roomName);
        waitRoomResultLoaded(roomLoc, noRoomLoc);

        if (isDisplayed(noRoomLoc)) {
            throw new RuntimeException("Không tìm thấy phòng: " + roomName);
        }

        click(roomLoc);
        PhongScr phongScr = new PhongScr(driver);
        if (!phongScr.isLoaded(roomName)) {
            throw new RuntimeException("Không vào được phòng sau khi tìm kiếm: " + roomName);
        }
        return phongScr;
    }

    public KhamPhaScr clickKhamPha() {
        click(lblKhamPha);
        return new KhamPhaScr(driver);
    }

    /** Chỉ tap nút Tạo phòng — luồng đăng nhập tùy chọn dùng {@link flows.CreateRoomEntryFlow}. */
    public void tapCreateRoomButton() {
        click(btnTaoPhong);
    }

    public TaoPhongScr clickTaoPhong() {
        tapCreateRoomButton();
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
