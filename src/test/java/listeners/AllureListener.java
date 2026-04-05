package listeners;

import base.BaseDriver;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.qameta.allure.Attachment;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.testng.ITestListener;
import org.testng.ITestResult;
import utils.StepContext;

public class AllureListener implements ITestListener {

    @Override
    public void onTestFailure(ITestResult result) {

        String stepName = StepContext.getStep();

        if (stepName == null) {
            stepName = "Unknown Step";
        }

        captureScreenshot(stepName);
        attachLocatorDebugContext();
    }

    @Attachment(value = "Failure Screenshot - {stepName}", type = "image/png")
    public byte[] captureScreenshot(String stepName) {

        return ((TakesScreenshot) BaseDriver.getDriver())
                .getScreenshotAs(OutputType.BYTES);
    }

    /**
     * Gợi ý mở lại đúng màn: capabilities + activity (Android) + page source (cắt bớt).
     */
    @Attachment(value = "Locator — màn hình & cây UI (mở Appium Inspector cùng session)", type = "text/plain")
    public String attachLocatorDebugContext() {
        try {
            AppiumDriver d = BaseDriver.getDriver();
            StringBuilder sb = new StringBuilder();
            sb.append("Dùng Appium Inspector / Xcode → kết nối cùng UDID và bundleId như dưới để bắt lại locator.\n\n");
            sb.append("Capabilities (rút gọn):\n").append(d.getCapabilities().asMap()).append("\n\n");
            if (d instanceof AndroidDriver ad) {
                try {
                    sb.append("currentActivity: ").append(ad.currentActivity()).append("\n");
                } catch (Exception ignored) {
                }
            }
            String ps = d.getPageSource();
            if (ps.length() > 60_000) {
                ps = ps.substring(0, 60_000) + "\n... [truncated]";
            }
            sb.append("\n--- PAGE SOURCE ---\n").append(ps);
            return sb.toString();
        } catch (Exception e) {
            return "Không đính kèm context (driver đã đóng?): " + e.getMessage();
        }
    }
}