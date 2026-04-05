package utils;

import io.appium.java_client.AppiumDriver;
import io.qameta.allure.Allure;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import java.io.ByteArrayInputStream;

public class ScreenshotUtils {

    public static void attachScreenshot(AppiumDriver driver, String name) {

        byte[] screenshot = ((TakesScreenshot) driver)
                .getScreenshotAs(OutputType.BYTES);

        Allure.addAttachment(
                name,
                "image/png",
                new ByteArrayInputStream(screenshot),
                ".png"
        );
    }
}