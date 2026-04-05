from appium import webdriver
from appium.options.android import UiAutomator2Options
import time

options = UiAutomator2Options()
options.platform_name = "Android"
options.no_reset = True
options.auto_grant_permissions = True
driver = webdriver.Remote("http://127.0.0.1:4723", options=options)

print("Dumping Room screen...")
time.sleep(2)
with open("room_actual_dump.xml", "w", encoding="utf-8") as f:
    f.write(driver.page_source)

print("Room dump completed!")
driver.quit()
