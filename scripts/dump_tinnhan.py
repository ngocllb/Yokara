from appium import webdriver
from appium.options.android import UiAutomator2Options
from appium.webdriver.common.appiumby import AppiumBy
import time

options = UiAutomator2Options()
options.platform_name = "Android"
options.no_reset = True
options.auto_grant_permissions = True
driver = webdriver.Remote("http://127.0.0.1:4723", options=options)

print("Navigating to Tin Nhắn tab...")
try:
    # Safer XPath to handle cases like "Tin nhắn\n5"
    tab_tin_nhan = driver.find_element(AppiumBy.XPATH, "//*[contains(@content-desc, 'Tin nhắn')]")
    tab_tin_nhan.click()
    time.sleep(3)
    
    print("Dumping Tin Nhắn screen...")
    with open("tinnhan_dump.xml", "w", encoding="utf-8") as f:
        f.write(driver.page_source)
    print("Dump successful: tinnhan_dump.xml")
except Exception as e:
    print(f"Error: {e}")

driver.quit()
