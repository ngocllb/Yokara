import time
from appium import webdriver
from appium.options.android import UiAutomator2Options
from appium.webdriver.common.appiumby import AppiumBy

options = UiAutomator2Options()
options.platform_name = "Android"
options.no_reset = True
options.auto_grant_permissions = True
driver = webdriver.Remote("http://127.0.0.1:4723", options=options)
time.sleep(2)

print("Navigating to Hat tab...")
try:
    # Try finding Hat element in BottomNav and click
    driver.find_element(AppiumBy.XPATH, "//*[@content-desc='Hát']").click()
except Exception as e:
    print("Could not click Hat tab directly:", e)

time.sleep(4)
with open("hat_baihat_dump.xml", "w", encoding="utf-8") as f:
    f.write(driver.page_source)
print("Dumped hat_baihat_dump.xml")

print("Navigating to Song ca sub-tab...")
try:
    # Try finding Song ca element in top nav and click
    driver.find_element(AppiumBy.XPATH, "//*[@content-desc='Song ca']").click()
except Exception as e:
    print("Could not click Song ca tab directly:", e)

time.sleep(4)
with open("hat_songca_dump.xml", "w", encoding="utf-8") as f:
    f.write(driver.page_source)
print("Dumped hat_songca_dump.xml")

driver.quit()
