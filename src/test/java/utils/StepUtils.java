//Xu ly them cu the tung attachment fail vao tung steps sau khi fail
package utils;

import base.BaseDriver;
import io.qameta.allure.Allure;
import io.qameta.allure.Allure.ThrowableRunnable;
import io.qameta.allure.Allure.ThrowableRunnableVoid;

public class StepUtils {

    public static <T> T step(String name, ThrowableRunnable<T> action) {

        return Allure.step(name, () -> {
            StepContext.setStep(name);
            try {
                return action.run();
            } catch (Throwable e) {

                ScreenshotUtils.attachScreenshot(
                        BaseDriver.getDriver(),
                        "FAILED STEP - " + name
                );

                Allure.addAttachment(
                        "Page Source - " + name,
                        BaseDriver.getDriver().getPageSource()
                );

                throw e;
            }
        });
    }

    public static void step(String name, ThrowableRunnableVoid action) {

        Allure.step(name, () -> {
            StepContext.setStep(name);
            try {
                action.run();
            } catch (Throwable e) {

                ScreenshotUtils.attachScreenshot(
                        BaseDriver.getDriver(),
                        "FAILED STEP - " + name
                );

                Allure.addAttachment(
                        "Page Source - " + name,
                        BaseDriver.getDriver().getPageSource()
                );

                throw e;
            }
        });
    }
}