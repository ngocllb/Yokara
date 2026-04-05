package tests.audit;

import base.BaseDriver;
import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Rà bottom tab (cùng pattern {@link base.BottomNav}) trên iOS và lưu page source để chỉnh locator.
 * Chạy: {@code mvn test -Dsurefire.suiteXmlFiles=testng-ios-audit.xml} (Appium đã bật, iPhone USB).
 */
public class IosUiLocatorAuditTest extends BaseDriver {

    private static final String[] BOTTOM_TAB_LABELS = {
            "Trang chủ", "Trực tuyến", "Hát", "Tin nhắn", "Tôi"
    };

    private static By byTabLikeBottomNav(String label) {
        return AppiumBy.xpath(
                "//*[contains(@content-desc, '" + label + "')"
                        + " or contains(@name, '" + label + "')"
                        + " or contains(@label, '" + label + "')"
                        + " or contains(@value, '" + label + "')]"
        );
    }

    @Test(description = "Lưu page source + assert bottom tab hiển thị theo locator chéo nền")
    public void auditBottomNavLocatorsOnIos() throws Exception {
        SoftAssert soft = new SoftAssert();
        for (String label : BOTTOM_TAB_LABELS) {
            boolean found = !driver.findElements(byTabLikeBottomNav(label)).isEmpty();
            soft.assertTrue(found, "Không thấy tab/control: " + label);
        }

        Path dir = Paths.get("target", "ios-audit");
        Files.createDirectories(dir);
        Path out = dir.resolve("page-source-" + System.currentTimeMillis() + ".xml");
        Files.writeString(out, driver.getPageSource(), StandardCharsets.UTF_8);
        System.out.println("[IosUiLocatorAuditTest] Đã ghi: " + out.toAbsolutePath());

        soft.assertAll();
    }
}
