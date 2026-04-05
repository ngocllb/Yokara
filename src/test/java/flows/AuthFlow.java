package flows;

import base.BaseScr;
import io.appium.java_client.AppiumDriver;
import pages.toi.ToiGuestScr;
import pages.toi.ToiProfileScr;
import pages.toi.login.AccountScr;
import pages.toi.login.FbGoogleAccountScr;
import pages.toi.login.LoginMethodScr;
import pages.toi.login.LoginUIDScr;
import pages.toi.login.loginphone.LoginPhoneScr;
import pages.toi.login.loginphone.OtpVerificationScr;

public class AuthFlow {

    private final AppiumDriver driver;

    public AuthFlow(AppiumDriver driver) {
        this.driver = driver;
    }

    public ToiProfileScr login(String method, String... args) {

        try {
            ((io.appium.java_client.HidesKeyboard) driver).hideKeyboard();
        } catch (Exception e) {
            // Keyboard already hidden or not supported
        }
        // Xử lý popup nếu có che khuất tabbar
        new BaseScr(driver).handleStartupPopups();

        // Cưỡng bức về tab Tôi trước khi tìm nút Đăng nhập
        try {
            driver.findElement(io.appium.java_client.AppiumBy.accessibilityId("Tôi")).click();
        } catch (Exception e) {
            // Đã ở tab Tôi hoặc lỗi nhẹ, bỏ qua
        }

        ToiGuestScr guestPage = new ToiGuestScr(driver);
        // Thay thế sleep(5000) bằng dynamic wait tích hợp sẵn trong waitForGuestPage
        guestPage.waitForGuestPage(); 
        BaseScr page = guestPage.clickLogin();

        LoginMethodScr methodPage;

        if (page instanceof AccountScr) {
            AccountScr AccountScr = (AccountScr) page;
            AccountScr.selectAnotherMethodLogin();
            methodPage = new LoginMethodScr(driver);
        } else {
            methodPage = (LoginMethodScr) page;
        }

        switch (method.toLowerCase()) {

            case "uid":
                validateArgs(method, args, 2);

                LoginUIDScr uidPage =
                        (LoginUIDScr) methodPage.loginWith("uid");

                return uidPage.loginByUID(args[0], args[1]);

            case "phone":
                validateArgs(method, args, 2);

                LoginPhoneScr phonePage =
                        (LoginPhoneScr) methodPage.loginWith("phone");

                OtpVerificationScr otpPage =
                        phonePage.goToOtpPage(args[0]);

                return otpPage.submitValidOtp(args[1]);

            case "facebook":
                FbGoogleAccountScr socialPage =
                        (FbGoogleAccountScr) methodPage.loginWith("facebook");

                socialPage.selectFirstAccount();

                return new ToiProfileScr(driver);

            default:
                throw new RuntimeException("Unsupported login method: " + method);
        }
    }

    private void validateArgs(String method, String[] args, int expectedSize) {
        if (args == null || args.length < expectedSize) {
            throw new IllegalArgumentException(
                    "Login method '" + method + "' requires " + expectedSize + " arguments"
            );
        }
    }
}

