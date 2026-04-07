package tests;

import base.BaseDriver;
import base.BottomNav;
import io.qameta.allure.Description;
import io.qameta.allure.Step;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.toi.ToiProfileScr;

/**
 * Điều hướng bottom nav → tab Tôi; locator và assertion nằm trong {@link BottomNav} / {@link pages.toi.ToiProfileScr}.
 */
public class NavigationTest extends BaseDriver {

    @Test
    @Description("Navigate to Tôi page — VIP (logged-in), tab Tác phẩm, hoặc màn khách (Đăng nhập)")
    public void testNavigateToToi() {
        ToiProfileScr profilePage = openToiTab();
        verifyToiTabLoaded(profilePage);
    }

    @Step("Mở tab Tôi từ bottom navigation")
    private ToiProfileScr openToiTab() {
        BottomNav bottomNav = new BottomNav(driver);
        return bottomNav.goToToi();
    }

    @Step("Verify tab Tôi hiển thị nội dung hợp lệ (VIP / Tác phẩm / Đăng nhập)")
    private void verifyToiTabLoaded(ToiProfileScr profilePage) {
        Assert.assertTrue(
                profilePage.isToiTabContentRecognized(),
                "Tab Tôi: không nhận diện được VIP, Tác phẩm hoặc Đăng nhập"
        );
    }
}

