from appium import webdriver
from appium.options.android import UiAutomator2Options
from appium.webdriver.common.appiumby import AppiumBy
import time

options = UiAutomator2Options()
options.platform_name = "Android"
options.no_reset = True
options.auto_grant_permissions = True
driver = webdriver.Remote("http://127.0.0.1:4723", options=options)

print("Dumping current screen (Bài hát)...")
time.sleep(2)
with open("hat_baihat_dump.xml", "w", encoding="utf-8") as f:
    f.write(driver.page_source)

try:
    print("Clicking Song ca tab...")
    driver.find_element(AppiumBy.XPATH, "//*[@content-desc='Song ca']").click()
    time.sleep(3)
    with open("hat_songca_dump.xml", "w", encoding="utf-8") as f:
        f.write(driver.page_source)
    print("Dumped Song ca")
except Exception as e:
    print("Could not click song ca:", e)

driver.quit()
