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
        BaseScr base = new BaseScr(driver);
        
        // 1. Chuẩn bị: Ẩn bàn phím và xử lý popup khởi động
        hideKeyboardIfPossible();
        base.handleStartupPopups();

        // 2. Chuyển sang tab Tôi
        ensureToiTabVisible();

        // 3. Chờ màn hình Guest và click Đăng nhập
        ToiGuestScr guestPage = new ToiGuestScr(driver);
        guestPage.waitForGuestPage();
        BaseScr pageAfterClick = guestPage.clickLogin();

        // 4. Xử lý logic chuyển tiếp (AccountScr -> LoginMethodScr)
        LoginMethodScr methodPage;
        if (pageAfterClick instanceof AccountScr accountScr) {
            accountScr.selectAnotherMethodLogin();
            LoginMethodScr.waitForLoginMethodScreen(driver);
            methodPage = new LoginMethodScr(driver);
        } else {
            methodPage = (LoginMethodScr) pageAfterClick;
        }

        // 5. Thực hiện đăng nhập theo phương thức
        return performLoginAction(method, methodPage, args);
    }

    private void ensureToiTabVisible() {
        try {
            // Sử dụng BottomNav để có logic fallback và wait ổn định cho iOS
            new base.BottomNav(driver).goToToi();
        } catch (Exception e) {
            // Log nhẹ nhàng hoặc bỏ qua nếu đã ở đúng tab
            System.out.println("[AuthFlow] Tab 'Tôi' navigation issue: " + e.getMessage());
        }
    }

    private void hideKeyboardIfPossible() {
        try {
            if (driver instanceof io.appium.java_client.HidesKeyboard) {
                ((io.appium.java_client.HidesKeyboard) driver).hideKeyboard();
            }
        } catch (Exception ignored) {}
    }

    private ToiProfileScr performLoginAction(String method, LoginMethodScr methodPage, String[] args) {
        switch (method.toLowerCase()) {
            case "uid" -> {
                validateArgs(method, args, 2);
                LoginUIDScr uidPage = (LoginUIDScr) methodPage.loginWith("uid");
                return uidPage.loginByUID(args[0], args[1]);
            }
            case "phone" -> {
                validateArgs(method, args, 2);
                LoginPhoneScr phonePage = (LoginPhoneScr) methodPage.loginWith("phone");
                OtpVerificationScr otpPage = phonePage.goToOtpPage(args[0]);
                return otpPage.submitValidOtp(args[1]);
            }
            case "facebook" -> {
                FbGoogleAccountScr socialPage = (FbGoogleAccountScr) methodPage.loginWith("facebook");
                socialPage.selectFirstAccount();
                return new ToiProfileScr(driver);
            }
            default -> throw new RuntimeException("Unsupported login method: " + method);
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
