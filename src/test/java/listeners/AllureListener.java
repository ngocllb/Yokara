package listeners;

import io.qameta.allure.Allure;
import io.qameta.allure.AllureLifecycle;
import io.qameta.allure.model.Parameter;
import io.qameta.allure.model.Status;
import io.qameta.allure.model.Label;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.PrintWriter;
import java.io.StringWriter;

public class AllureListener implements ITestListener {

    private static final ThreadLocal<Boolean> METADATA_ATTACHED = ThreadLocal.withInitial(() -> false);

    @Override
    public void onStart(ITestContext context) {
        // no-op
    }

    @Override
    public void onFinish(ITestContext context) {
        // no-op
    }

    @Override
    public void onTestStart(ITestResult result) {
        METADATA_ATTACHED.set(false);
        attachExecutionMetadata(result);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        attachExecutionMetadata(result);
    }

    @Override
    public void onTestFailure(ITestResult result) {
        attachExecutionMetadata(result);

        Throwable throwable = result.getThrowable();
        if (throwable != null) {
            Allure.addAttachment("Failure Message", throwable.toString());
            Allure.addAttachment("Failure Stacktrace", asStackTrace(throwable));
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        attachExecutionMetadata(result);

        Throwable throwable = result.getThrowable();
        if (throwable != null) {
            Allure.addAttachment("Skip Reason", throwable.toString());
        }
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
        attachExecutionMetadata(result);
    }

    @Override
    public void onTestFailedWithTimeout(ITestResult result) {
        onTestFailure(result);
    }

    private void attachExecutionMetadata(ITestResult result) {
        if (Boolean.TRUE.equals(METADATA_ATTACHED.get())) {
            return;
        }

        String platform = firstNonBlank(
                System.getProperty("platform"),
                parameter(result, "suitePlatform"),
                "unknown"
        ).toLowerCase();

        String udid = "android".equals(platform)
                ? firstNonBlank(System.getProperty("android.udid"), parameter(result, "suiteUdid"))
                : firstNonBlank(System.getProperty("ios.udid"), parameter(result, "suiteUdid"));

        String appiumServer = firstNonBlank(System.getProperty("appiumServer"), "unknown");

        String deviceKey = buildDeviceKey(platform, udid);
        String originalTestName = result.getMethod().getMethodName();
        String displayName = originalTestName + " [" + deviceKey + "]";

        AllureLifecycle lifecycle = Allure.getLifecycle();

        try {
            lifecycle.updateTestCase(testResult -> {
                testResult.setName(displayName);

                upsertLabel(testResult, "platform", platform);
                upsertLabel(testResult, "device", deviceKey);
                upsertLabel(testResult, "framework", "testng");
                upsertLabel(testResult, "host", safe(System.getProperty("user.name")));

                upsertParameter(testResult, "platform", platform);
                upsertParameter(testResult, "device", deviceKey);

                if (udid != null && !udid.isBlank()) {
                    upsertParameter(testResult, "udid", udid);
                }
                if (appiumServer != null && !appiumServer.isBlank()) {
                    upsertParameter(testResult, "appiumServer", appiumServer);
                }
            });

            Allure.parameter("platform", platform);
            Allure.parameter("device", deviceKey);
            if (udid != null && !udid.isBlank()) {
                Allure.parameter("udid", udid);
            }
            if (appiumServer != null && !appiumServer.isBlank()) {
                Allure.parameter("appiumServer", appiumServer);
            }

            METADATA_ATTACHED.set(true);
        } catch (Exception ex) {
            // Không để listener làm hỏng test run
            System.out.println("[AllureListener] attachExecutionMetadata failed: " + ex.getMessage());
        }
    }

    private String parameter(ITestResult result, String key) {
        if (result == null || result.getTestContext() == null) {
            return null;
        }
        try {
            return result.getTestContext().getCurrentXmlTest().getParameter(key);
        } catch (Exception e) {
            return null;
        }
    }

    private void upsertLabel(io.qameta.allure.model.TestResult testResult, String name, String value) {
        if (value == null || value.isBlank()) {
            return;
        }

        for (Label label : testResult.getLabels()) {
            if (name.equals(label.getName())) {
                label.setValue(value);
                return;
            }
        }

        testResult.getLabels().add(new Label().setName(name).setValue(value));
    }

    private void upsertParameter(io.qameta.allure.model.TestResult testResult, String name, String value) {
        if (value == null || value.isBlank()) {
            return;
        }

        for (Parameter p : testResult.getParameters()) {
            if (name.equals(p.getName())) {
                p.setValue(value);
                return;
            }
        }

        testResult.getParameters().add(new Parameter().setName(name).setValue(value));
    }

    private String buildDeviceKey(String platform, String udid) {
        if (udid == null || udid.isBlank()) {
            return platform;
        }

        String shortUdid = udid.length() > 8 ? udid.substring(udid.length() - 8) : udid;
        return platform + "-" + shortUdid;
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String asStackTrace(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        pw.flush();
        return sw.toString();
    }
}