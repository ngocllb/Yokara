package tests;

import base.BaseDriver;
import base.BaseScr;
import base.BottomNav;
import flows.AuthFlow;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.toi.ToiGuestScr;
import pages.toi.ToiProfileScr;
import pages.toi.caidat.CaiDatScr;
import pages.toi.caidat.LogoutPopup;
import pages.toi.login.AccountScr;
import pages.toi.login.LoginMethodScr;
import pages.toi.login.loginphone.LoginPhoneScr;
import pages.toi.login.loginphone.OtpVerificationScr;
import testdata.LoginTestData;
import utils.StepUtils;

public class LoginMethodTest extends BaseDriver {

    private final LoginTestData data = LoginTestData.load();

    @Test(priority = 1)
    public void loginByUIDTest() {

        BottomNav bottomNav = new BottomNav(driver);
        AuthFlow auth = new AuthFlow(driver);
        ToiGuestScr guestPage = new ToiGuestScr(driver);

        StepUtils.step("Launch app và navigate tới tab Tôi", bottomNav::goToToi);
        StepUtils.step("Verify đang ở trạng thái Guest",
                () -> Assert.assertTrue(guestPage.isGuest(),
                        "Không tìm thấy button Đăng nhập"
                )
        );
        ToiProfileScr profilePage = StepUtils.step(
                "Login bằng UID",
                () -> auth.login("uid", data.uid(), data.password())
        );
        StepUtils.step("Verify Login thành công và UID chính xác sau khi login",
                () -> Assert.assertTrue(profilePage.isUserIdDisplayed(data.uid()),
                        "UID hiển thị không đúng sau khi login"
                )
        );
        logoutAndVerify(profilePage);
    }
    @Test(priority = 2)
    public void loginByPhoneTest() {

        BottomNav bottomNav = new BottomNav(driver);
        ToiGuestScr guestPage = new ToiGuestScr(driver);

        StepUtils.step("Navigate tới tab Tôi", bottomNav::goToToi);
        StepUtils.step("Verify đang ở trạng thái Guest",
                () -> Assert.assertTrue(guestPage.isGuest(),
                        "Không tìm thấy button Đăng nhập"
                )
        );
        BaseScr page = StepUtils.step(
                "Mở màn hình chọn phương thức đăng nhập",
                guestPage::clickLogin
        );
        LoginMethodScr loginMethodPage = StepUtils.step(
                "Đi tới màn Login Method",
                () -> {
                    if (page instanceof AccountScr accountScr) {
                        accountScr.selectAnotherMethodLogin();
                        LoginMethodScr.waitForLoginMethodScreen(driver);
                        return new LoginMethodScr(driver);
                    }
                    return (LoginMethodScr) page;
                }
        );
        LoginPhoneScr LoginPhoneScr = StepUtils.step(
                "Mở màn hình đăng nhập bằng số điện thoại",
                () -> (LoginPhoneScr) loginMethodPage.loginWith("phone")
        );
        OtpVerificationScr otpPage = StepUtils.step(
                "Nhập số điện thoại và chuyển sang màn OTP",
                () -> LoginPhoneScr.goToOtpPage(data.phone())
        );
        StepUtils.step("Nhập OTP sai và verify hiển thị lỗi", () -> {
            otpPage.submitInvalidOtp(data.otpInvalid());

            Assert.assertTrue(otpPage.isOtpErrorDisplayed(),
                    "Không hiển thị lỗi khi nhập OTP sai"
            );
            Assert.assertTrue(otpPage.isStillOnOtpPage(),
                    "Nhập OTP sai nhưng không còn ở màn OTP"
            );
        });
        ToiProfileScr profilePage = StepUtils.step(
                "Nhập OTP đúng và đăng nhập thành công",
                () -> otpPage.submitValidOtp(data.otpValid())
        );
        StepUtils.step("Verify Login thành công với đúng OTP",
                () -> Assert.assertTrue(profilePage.isUserIdDisplayed(data.uid()),
                        "UID hiển thị không đúng sau khi login"
                )
        );
        logoutAndVerify(profilePage);
    }

    private void logoutAndVerify(ToiProfileScr profilePage) {
        CaiDatScr CaiDatScr = StepUtils.step("Mở Cài đặt", profilePage::openSetting);
        LogoutPopup popup = StepUtils.step("Click Đăng xuất", CaiDatScr::clickDangXuat);
        ToiGuestScr guestAfterLogout = StepUtils.step("Xác nhận đăng xuất", popup::confirmLogout);

        StepUtils.step("Verify đã logout",
                () -> Assert.assertTrue(guestAfterLogout.isGuest(),
                        "Logout thất bại"
                )
        );
    }
}

