package tests;

import org.testng.Assert;
import org.testng.annotations.Test;

import base.BaseDriver;
import base.BottomNav;
import pages.tructuyen.tabtructuyen.TrucTuyenScr;
import pages.tructuyen.tabtructuyen.phong.TaoPhongScr;
import pages.tructuyen.tabtructuyen.phong.CuaToiScr;
import pages.tructuyen.tabtructuyen.phong.PhongScr;

import utils.StepUtils;

public class RoomTest extends BaseDriver {

    /**
     * iOS card phòng thường truncate phần cuối; đặt mã unique ở đầu để locator luôn còn lại sau truncate.
     */
    private static String buildUniqueRoomName() {
        String key = "r" + Long.toString(System.currentTimeMillis(), 36);
        return key + " ngoc lele bao";
    }

    @Test
    public void testCreatePrivateRoom() {
        BottomNav bottomNav = new BottomNav(driver);

        StepUtils.step("Navigate tới tab Trực tuyến", bottomNav::goToTrucTuyen);

        TrucTuyenScr trucTuyenScr = new TrucTuyenScr(driver);
        StepUtils.step("Verify đang ở tab Trực tuyến",
            () -> Assert.assertTrue(trucTuyenScr.isLoaded(), "Không ở tab Trực tuyến")
        );

        TaoPhongScr taoPhongScr = StepUtils.step("Mở màn hình Tạo phòng (Guest / Account / đã vào Tạo phòng)", () ->
                new flows.CreateRoomEntryFlow(driver, auth).openTaoPhongWithOptionalLogin(
                        trucTuyenScr, "uid", "6026833", "Abcd1234")
        );

        String roomName = buildUniqueRoomName();
        CuaToiScr roomPage = StepUtils.step("Tạo phòng riêng tư",
            () -> taoPhongScr.createRoom(roomName, "PRIVATE", "1212")
        );

        StepUtils.step("Verify phòng đã được tạo thành công",
            () -> Assert.assertTrue(roomPage.isRoomNameDisplayed(roomName), "Tên phòng không khớp: " + roomName)
        );

        PhongScr phongScr = StepUtils.step("Tìm và vào phòng vừa tạo từ Search",
            () -> trucTuyenScr.enterRoom(roomName)
        );

        StepUtils.step("Verify màn hình phòng hiển thị đúng tên",
            () -> Assert.assertTrue(phongScr.isLoaded(roomName), "Không vào được màn hình phòng: " + roomName)
        );

        StepUtils.step("Xóa phòng vừa tạo từ Thông tin phòng",
            () -> phongScr.openThongTinPhong().deleteRoom(roomName)
        );
    }
}
