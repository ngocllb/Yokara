from appium import webdriver
from appium.options.android import UiAutomator2Options
from appium.webdriver.common.appiumby import AppiumBy
import time

options = UiAutomator2Options()
options.platform_name = "Android"
options.no_reset = True
options.auto_grant_permissions = True
driver = webdriver.Remote("http://127.0.0.1:4723", options=options)

print("Navigating to Tôi tab...")
try:
    # Safer XPath to handle potential badge counts or different naming
    tab_toi = driver.find_element(AppiumBy.XPATH, "//*[contains(@content-desc, 'Tôi')]")
    tab_toi.click()
    time.sleep(3)
    
    print("Dumping Tôi screen...")
    with open("toi_dump.xml", "w", encoding="utf-8") as f:
        f.write(driver.page_source)
    print("Dump successful: toi_dump.xml")
except Exception as e:
    print(f"Error: {e}")

driver.quit()
