package flows;

import base.BottomNav;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.ios.IOSDriver;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.WebDriverWait;
import pages.toi.ToiGuestScr;
import pages.toi.login.AccountScr;
import pages.toi.login.LoginMethodScr;
import pages.tructuyen.tabtructuyen.TrucTuyenScr;
import pages.tructuyen.tabtructuyen.phong.TaoPhongScr;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Điều phối luồng: từ tab Trực tuyến bấm <strong>Tạo phòng</strong> → có thể vào màn Tạo phòng,
 * hoặc màn đăng nhập (Guest / Account đã lưu / trực tiếp chọn phương thức).
 * <p>Sau khi đăng nhập xong luôn quay lại Trực tuyến và bấm Tạo phòng lần nữa để vào {@link TaoPhongScr}.</p>
 */
public final class CreateRoomEntryFlow {

    private final AppiumDriver driver;
    private final AuthFlow authFlow;

    public CreateRoomEntryFlow(AppiumDriver driver, AuthFlow authFlow) {
        this.driver = driver;
        this.authFlow = authFlow;
    }

    /**
     * Bấm Tạo phòng; nếu cần đăng nhập thì dùng {@code method} + {@code loginArgs} (vd. {@code "uid"}, uid, password).
     */
    public TaoPhongScr openTaoPhongWithOptionalLogin(TrucTuyenScr trucTuyen, String method, String... loginArgs) {
        trucTuyen.tapCreateRoomButton();
        PostTapScreen screen = detectScreenAfterCreateRoomTap(Duration.ofSeconds(22));
        return switch (screen) {
            case TAO_PHONG -> new TaoPhongScr(driver);
            case ACCOUNT -> {
                authFlow.loginFromAccountScreen(new AccountScr(driver), method, loginArgs);
                yield afterLoginReturnToTaoPhong(trucTuyen);
            }
            case GUEST -> {
                authFlow.loginFromGuestPage(new ToiGuestScr(driver), method, loginArgs);
                yield afterLoginReturnToTaoPhong(trucTuyen);
            }
            case LOGIN_METHOD -> {
                authFlow.loginWhenLoginMethodScreenVisible(method, loginArgs);
                yield afterLoginReturnToTaoPhong(trucTuyen);
            }
            case UNKNOWN -> throw new IllegalStateException(
                    "Không xác định màn hình sau khi bấm Tạo phòng (không thấy Tạo phòng / Account / Guest / LoginMethod).");
        };
    }

    private TaoPhongScr afterLoginReturnToTaoPhong(TrucTuyenScr trucTuyen) {
        new BottomNav(driver).goToTrucTuyen();
        Duration settle = driver instanceof IOSDriver
                ? Duration.ofSeconds(28)
                : Duration.ofSeconds(18);
        if (!trucTuyen.waitForScreenReady(settle)) {
            throw new IllegalStateException("Sau đăng nhập, không quay lại được tab Trực tuyến ổn định.");
        }
        trucTuyen.tapCreateRoomButton();
        if (!TaoPhongScr.waitUntilPresent(driver, Duration.ofSeconds(22))) {
            throw new IllegalStateException("Sau đăng nhập và bấm lại Tạo phòng, không vào được màn đặt tên phòng.");
        }
        return new TaoPhongScr(driver);
    }

    /**
     * Phát hiện màn hiện tại sau tap Tạo phòng (ưu tiên: màn Tạo phòng → Account → Guest → LoginMethod).
     */
    private PostTapScreen detectScreenAfterCreateRoomTap(Duration maxWait) {
        AtomicReference<PostTapScreen> found = new AtomicReference<>();
        try {
            new WebDriverWait(driver, maxWait).until(d -> {
                AppiumDriver ad = (AppiumDriver) d;
                if (TaoPhongScr.isPresent(ad)) {
                    found.set(PostTapScreen.TAO_PHONG);
                    return true;
                }
                if (AccountScr.isAccountScr(ad, 0)) {
                    found.set(PostTapScreen.ACCOUNT);
                    return true;
                }
                ToiGuestScr guest = new ToiGuestScr(ad);
                if (guest.isGuestPageDisplayed()) {
                    found.set(PostTapScreen.GUEST);
                    return true;
                }
                if (LoginMethodScr.isLoginMethodScreenPresent(ad)) {
                    found.set(PostTapScreen.LOGIN_METHOD);
                    return true;
                }
                return false;
            });
        } catch (TimeoutException e) {
            return PostTapScreen.UNKNOWN;
        }
        PostTapScreen r = found.get();
        return r != null ? r : PostTapScreen.UNKNOWN;
    }

    private enum PostTapScreen {
        TAO_PHONG,
        ACCOUNT,
        GUEST,
        LOGIN_METHOD,
        UNKNOWN
    }
}
