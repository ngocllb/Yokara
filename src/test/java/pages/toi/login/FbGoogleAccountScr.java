package pages.toi.login;

import base.BaseScr;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.By;

public class FbGoogleAccountScr extends BaseScr {

    // account đầu tiên trong list
    private By firstAccount = AppiumBy.xpath("//android.widget.TextView[@resource-id='com.google.android.gms:id/account_name' or @resource-id='com.facebook.katana:id/account_name' or string-length(@text)>0]");

    public FbGoogleAccountScr(AppiumDriver driver){
        super(driver);
    }

    public void selectFirstAccount(){

        click(firstAccount);
    }
}
