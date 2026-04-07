package tests;

import org.testng.Assert;
import org.testng.annotations.Test;

import base.BaseDriver;
import base.BottomNav;
import pages.tructuyen.tabtructuyen.TrucTuyenScr;
import pages.tructuyen.tabtructuyen.phong.TaoPhongScr;
import pages.tructuyen.tabtructuyen.phong.CuaToiScr;
import pages.tructuyen.tabtructuyen.phong.PhongScr;
import pages.toi.ToiGuestScr;
import pages.toi.login.AccountScr;
import pages.toi.login.LoginMethodScr;
import pages.toi.login.LoginUIDScr;
import utils.StepUtils;

public class RoomTest extends BaseDriver {

    @Test
    public void testCreatePrivateRoom() {
        BottomNav bottomNav = new BottomNav(driver);

        StepUtils.step("Navigate tới tab Trực tuyến", bottomNav::goToTrucTuyen);

        TrucTuyenScr trucTuyenScr = new TrucTuyenScr(driver);
        StepUtils.step("Verify đang ở tab Trực tuyến",
            () -> Assert.assertTrue(trucTuyenScr.isLoaded(), "Không ở tab Trực tuyến")
        );

        TaoPhongScr taoPhongScr = StepUtils.step("Mở màn hình Tạo phòng", () -> {
            trucTuyenScr.clickTaoPhong();
            ToiGuestScr guestPage = new ToiGuestScr(driver);
            if (guestPage.isGuestPageDisplayed()) {
                System.out.println("[RoomTest] Bấm Tạo Phòng yêu cầu Đăng Nhập. Bắt đầu luồng đăng nhập tự động...");
                base.BaseScr pageAfterClick = guestPage.clickLogin();
                LoginMethodScr methodPage;
                if (pageAfterClick instanceof AccountScr accountScr) {
                    accountScr.selectAnotherMethodLogin();
                    LoginMethodScr.waitForLoginMethodScreen(driver);
                    methodPage = new LoginMethodScr(driver);
                } else {
                    methodPage = (LoginMethodScr) pageAfterClick;
                }
                LoginUIDScr uidPage = (LoginUIDScr) methodPage.loginWith("uid");
                uidPage.loginByUID("6026833", "Abcd1234");
                
                System.out.println("[RoomTest] Login thành công, bấm Tạo phòng lần 2.");
                trucTuyenScr.clickTaoPhong();
            }
            return new TaoPhongScr(driver);
        });

        String roomName = "ngoc lele bao " + System.currentTimeMillis();
        CuaToiScr roomPage = StepUtils.step("Tạo phòng riêng tư",
            () -> taoPhongScr.createRoom(roomName, "PRIVATE", "1212")
        );

        StepUtils.step("Verify phòng đã được tạo thành công",
            () -> Assert.assertTrue(roomPage.isRoomNameDisplayed(roomName), "Tên phòng không khớp: " + roomName)
        );

        PhongScr phongScr = StepUtils.step("Bấm vào phòng vừa tạo",
            () -> roomPage.clickRoom(roomName)
        );

        StepUtils.step("Verify màn hình phòng hiển thị đúng tên",
            () -> Assert.assertTrue(phongScr.isLoaded(roomName), "Không vào được màn hình phòng: " + roomName)
        );
    }
}
