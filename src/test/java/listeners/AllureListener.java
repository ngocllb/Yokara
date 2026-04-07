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
        if (stepName == null || stepName.isBlank()) {
            stepName = "Unknown Step";
        }

        AppiumDriver driver = BaseDriver.getDriver();
        try {
            captureScreenshot(stepName, driver);
            attachFailureMeta(result, driver, stepName);
            attachLocatorDebugContext(driver);
        } finally {
            StepContext.clear();
        }
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        StepContext.clear();
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        StepContext.clear();
    }

    @Attachment(value = "Failure Screenshot - {stepName}", type = "image/png")
    public byte[] captureScreenshot(String stepName, AppiumDriver driver) {
        try {
            if (driver == null) return new byte[0];
            return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
        } catch (Exception e) {
            return new byte[0];
        }
    }

    @Attachment(value = "Failure Meta", type = "text/plain")
    public String attachFailureMeta(ITestResult result, AppiumDriver driver, String stepName) {
        try {
            String threadId = String.valueOf(Thread.currentThread().getId());
            String sessionId = driver != null && driver.getSessionId() != null
                    ? driver.getSessionId().toString()
                    : "null";

            String platform = driver != null && driver.getCapabilities().getPlatformName() != null
                    ? driver.getCapabilities().getPlatformName().toString()
                    : "unknown";

            Object deviceName = driver != null ? driver.getCapabilities().getCapability("appium:deviceName") : null;
            Object udid = driver != null ? driver.getCapabilities().getCapability("appium:udid") : null;

            return "test=" + result.getMethod().getMethodName() + "\n"
                    + "class=" + result.getTestClass().getName() + "\n"
                    + "step=" + stepName + "\n"
                    + "threadId=" + threadId + "\n"
                    + "sessionId=" + sessionId + "\n"
                    + "platform=" + platform + "\n"
                    + "deviceName=" + deviceName + "\n"
                    + "udid=" + udid;
        } catch (Exception e) {
            return "Không đính kèm meta: " + e.getMessage();
        }
    }

    /**
     * Gợi ý mở lại đúng màn: capabilities + activity (Android) + page source (cắt bớt).
     */
    @Attachment(value = "Locator — màn hình & cây UI", type = "text/plain")
    public String attachLocatorDebugContext(AppiumDriver d) {
        try {
            if (d == null) {
                return "Không đính kèm context: driver = null";
            }
            StringBuilder sb = new StringBuilder();
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