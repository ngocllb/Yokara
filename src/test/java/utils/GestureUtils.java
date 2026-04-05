//Xử lý gesture của mobile ví dụ Swipe, scrool, zoom, tap, drag

package utils;

import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;

import java.time.Duration;
import java.util.Collections;

public class GestureUtils {

    public static void swipeUp(AndroidDriver driver) {

        Dimension size = driver.manage().window().getSize();

        int startX = size.width / 2;
        int startY = (int) (size.height * 0.8);
        int endY = (int) (size.height * 0.2);

        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");

        Sequence swipe = new Sequence(finger, 1);

        swipe.addAction(finger.createPointerMove(Duration.ZERO,
                PointerInput.Origin.viewport(), startX, startY));

        swipe.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));

        swipe.addAction(finger.createPointerMove(Duration.ofMillis(600),
                PointerInput.Origin.viewport(), startX, endY));

        swipe.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));

        driver.perform(Collections.singletonList(swipe));
    }

    public static void longPress(AndroidDriver driver, int x, int y) {

        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");

        Sequence longPress = new Sequence(finger, 1);

        longPress.addAction(finger.createPointerMove(Duration.ZERO,
                PointerInput.Origin.viewport(), x, y));

        longPress.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));

        longPress.addAction(new org.openqa.selenium.interactions.Pause(finger, Duration.ofSeconds(2)));

        longPress.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));

        driver.perform(Collections.singletonList(longPress));
    }

}