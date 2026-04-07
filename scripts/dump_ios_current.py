import time
import os
from appium import webdriver
from appium.options.ios import XCUITestOptions

# UDID của thiết bị iOS (từ idevice_id -l)
UDID = "00008110-00165D890AE0401E"

options = XCUITestOptions()
options.platform_name = "iOS"
options.udid = UDID
options.bundle_id = "com.yokara"
options.no_reset = True
options.auto_accept_alerts = True

# Appium server mặc định
driver = webdriver.Remote("http://127.0.0.1:4723", options=options)
time.sleep(3)

# Đặt tên file theo màn hình bạn đang đứng (vd: hat_ios.xml)
filename = "current_ios_dump.xml"

with open(filename, "w", encoding="utf-8") as f:
    f.write(driver.page_source)

driver.quit()
print(f"Dump thành công tại: {os.path.abspath(filename)}")
