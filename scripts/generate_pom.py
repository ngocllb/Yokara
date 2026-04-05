#!/usr/bin/env python3
"""
generate_pom.py
===============
Tự động kết nối Appium, điều hướng qua các tab của app Yokara,
dump XML page source, phân tích cấu trúc UI và sinh ra các
Page Object Java skeleton sẵn sàng dùng trong framework.

Cách dùng:
    python scripts/generate_pom.py

Yêu cầu:
    pip install Appium-Python-Client lxml
"""

import subprocess
import sys
import time
import os
import re
import xml.etree.ElementTree as ET
import configparser
from datetime import datetime
from pathlib import Path

if hasattr(sys.stdout, 'reconfigure') and sys.stdout.encoding.lower() != 'utf-8':
    sys.stdout.reconfigure(encoding='utf-8')

# ─── Auto-install dependencies ───────────────────────────────────────────────
def ensure_deps():
    required = ["appium", "lxml"]
    for pkg in required:
        try:
            __import__(pkg if pkg != "appium" else "appium.webdriver")
        except ImportError:
            print(f"[setup] Cài đặt {pkg}...")
            subprocess.check_call([sys.executable, "-m", "pip", "install",
                                   "Appium-Python-Client" if pkg == "appium" else pkg,
                                   "-q"])

ensure_deps()

from appium import webdriver
from appium.webdriver.common.appiumby import AppiumBy
from appium.options.android import UiAutomator2Options
from lxml import etree

# ─── Paths ───────────────────────────────────────────────────────────────────
SCRIPT_DIR   = Path(__file__).parent
PROJECT_ROOT = SCRIPT_DIR.parent
CONFIG_FILE  = PROJECT_ROOT / "config" / "config.properties"
OUTPUT_DIR   = PROJECT_ROOT / "src" / "test" / "java" / "pages"
DUMP_DIR     = SCRIPT_DIR / "xml_dumps"
DUMP_DIR.mkdir(parents=True, exist_ok=True)

# ─── Read config.properties ──────────────────────────────────────────────────
def read_config():
    text = CONFIG_FILE.read_text(encoding="utf-8")
    # Thêm dummy section để configparser hoạt động
    parser = configparser.ConfigParser()
    parser.read_string("[root]\n" + text)
    cfg = dict(parser["root"])
    return cfg

# ─── ADB helper: lấy UDID ────────────────────────────────────────────────────
def get_android_udid():
    try:
        result = subprocess.run(["adb", "devices"], capture_output=True, text=True, timeout=10)
        for line in result.stdout.splitlines():
            line = line.strip()
            if line and not line.startswith("List") and "\tdevice" in line:
                return line.split("\t")[0].strip()
    except Exception as e:
        print(f"[warn] adb error: {e}")
    return None

# ─── Appium session ──────────────────────────────────────────────────────────
def create_driver(cfg):
    udid = cfg.get("android.udid", "").strip() or get_android_udid()

    device_name = cfg.get("android.devicename", "").strip() or udid or "Android Device"
    server = cfg.get("appiumserver", "http://127.0.0.1:4723").strip()

    options = UiAutomator2Options()
    options.platform_name      = "Android"
    options.device_name        = device_name
    if udid:
        options.udid           = udid
    options.app_package        = cfg.get("android.apppackage", "com.yokara.v3").strip()
    options.app_activity       = cfg.get("android.appactivity", "com.yokara.v3.MainActivity").strip()
    options.no_reset           = True
    options.auto_grant_permissions     = True
    options.disable_window_animation   = True

    print(f"[appium] Kết nối tới {server} | device={udid or 'auto'}")
    driver = webdriver.Remote(server, options=options)
    time.sleep(2)
    return driver

# ─── UI dump và parse ────────────────────────────────────────────────────────
def dump_page_source(driver, screen_name):
    """Lấy XML page source và lưu file dump."""
    source = driver.page_source
    dump_path = DUMP_DIR / f"{screen_name}.xml"
    dump_path.write_text(source, encoding="utf-8")
    print(f"  [dump] Đã lưu: {dump_path.name}")
    return source

def parse_elements(xml_source):
    """
    Parse XML và trích xuất các element hữu ích.
    Ưu tiên theo thứ tự: resource-id > content-desc > text
    Loại bỏ các element không có identifier.
    """
    elements = []
    seen_ids = set()

    try:
        root = etree.fromstring(xml_source.encode("utf-8"))
    except etree.XMLSyntaxError:
        return elements

    for node in root.iter():
        res_id     = (node.get("resource-id") or "").strip()
        content    = (node.get("content-desc") or "").strip()
        text       = (node.get("text") or "").strip()
        cls        = (node.get("class") or "").strip()
        clickable  = node.get("clickable") == "true"
        scrollable = node.get("scrollable") == "true"

        # Bỏ qua nếu không có locator nào
        if not res_id and not content and not text:
            continue

        # Loại bỏ trùng lặp
        key = res_id or content or text
        if key in seen_ids:
            continue
        seen_ids.add(key)

        elements.append({
            "resource_id": res_id,
            "content_desc": content,
            "text": text,
            "class": cls,
            "clickable": clickable,
            "scrollable": scrollable,
        })

    return elements

# ─── Tên biến Java từ string tùy ý ──────────────────────────────────────────
def to_java_field_name(raw):
    """'Đăng nhập Facebook' → 'dangNhapFacebook'"""
    # Bỏ ký tự không hợp lệ, giữ chữ và số
    clean = re.sub(r"[^a-zA-Z0-9\s_À-ỹ]", "", raw)
    # Tách từ
    words = re.split(r"[\s_]+", clean.strip())

    # Camel case (bỏ dấu tiếng Việt)
    result = []
    for i, w in enumerate(words):
        w = _remove_vn_accent(w)
        if not w:
            continue
        if i == 0:
            result.append(w[0].lower() + w[1:].lower())
        else:
            result.append(w[0].upper() + w[1:].lower())
    camel = "".join(result) or "element"

    # Không bắt đầu bằng số
    if camel and camel[0].isdigit():
        camel = "e" + camel
    return camel

def _remove_vn_accent(s):
    table = str.maketrans(
        "àáạảãâầấậẩẫăặắẳẵằèéẹẻẽêềếệểễìíịỉĩòóọỏõôồốộổỗơờớợởỡùúụủũưừứựửữỳýỵỷỹđ"
        "ÀÁẠẢÃÂẦẤẬẨẪĂẶẮẲẴẰÈÉẸẺẼÊỀẾỆỂỄÌÍỊỈĨÒÓỌỎÕÔỒỐỘỔỖƠỜỚỢỞỠÙÚỤỦŨƯỪỨỰỬỮỲÝỴỶỸĐ",
        "aaaaaaaaaaaaaaaaaeeeeeeeeeeeiiiiiooooooooooooooooouuuuuuuuuuuyyyyyd"
        "AAAAAAAAAAAAAAAAAEEEEEEEEEEEIIIIIOOOOOOOOOOOOOOOOOUUUUUUUUUUUYYYYYD"
    )
    return s.translate(table)

def to_class_name(screen_name):
    words = re.split(r"[\s_-]+", screen_name)
    return "".join(w.capitalize() for w in words) + "Page"

# ─── Sinh Java Page Object ───────────────────────────────────────────────────
JAVA_TEMPLATE = '''\
package pages.{pkg};

import base.BasePage;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;

/**
 * Page Object: {class_name}
 * Auto-generated by generate_pom.py — {timestamp}
 * Màn hình: {screen_display}
 *
 * TODO: Kiểm tra lại locator, thêm logic nghiệp vụ cho từng method.
 */
public class {class_name} extends BasePage {{

{locator_fields}

    public {class_name}(AppiumDriver driver) {{
        super(driver);
    }}

    /** Kiểm tra màn hình đang hiển thị */
    public boolean isDisplayed() {{
        // TODO: Dùng locator phù hợp để verify màn hình
        return true;
    }}

{methods}
}}
'''

def build_locator_line(elem, field_name):
    """Sinh dòng khai báo `private By fieldName = ...;`"""
    
    def escape_java_str(s):
        return s.replace('\\', '\\\\').replace('"', '\\"').replace('\n', '\\n').replace('\r', '')

    if elem["resource_id"]:
        locator = f'By.id("{escape_java_str(elem["resource_id"])}")'
        comment = ""
    elif elem["content_desc"]:
        locator = f'AppiumBy.accessibilityId("{escape_java_str(elem["content_desc"])}")'
        comment = ""
    else:
        locator = f'AppiumBy.androidUIAutomator("new UiSelector().text(\\"{escape_java_str(elem["text"])}\\")")'
        comment = "  // fallback: text-based locator"

    return f'    private final By {field_name} = {locator};{comment}'

def build_method(elem, field_name):
    """Sinh method click / getText tuỳ loại element."""
    lines = []
    label = elem["content_desc"] or elem["text"] or elem["resource_id"].split("/")[-1]

    if elem["clickable"]:
        method_name = "click" + field_name[0].upper() + field_name[1:]
        lines.append(f'    /** Click vào: {label} */')
        lines.append(f'    public void {method_name}() {{')
        lines.append(f'        click({field_name});')
        lines.append(f'    }}')

    if elem["class"] in ("android.widget.EditText",):
        type_name  = "type" + field_name[0].upper() + field_name[1:]
        lines.append(f'    /** Nhập text vào: {label} */')
        lines.append(f'    public void {type_name}(String text) {{')
        lines.append(f'        type({field_name}, text);')
        lines.append(f'    }}')

    if elem["class"] in ("android.widget.TextView", "android.view.View"):
        get_name = "get" + field_name[0].upper() + field_name[1:] + "Text"
        lines.append(f'    /** Lấy text của: {label} */')
        lines.append(f'    public String {get_name}() {{')
        lines.append(f'        return find({field_name}).getText();')
        lines.append(f'    }}')

    # Mặc định: isDisplayed check
    vis_name = "is" + field_name[0].upper() + field_name[1:] + "Displayed"
    lines.append(f'    /** Kiểm tra hiển thị: {label} */')
    lines.append(f'    public boolean {vis_name}() {{')
    lines.append(f'        return isDisplayed({field_name});')
    lines.append(f'    }}')

    return "\n".join(lines)

def generate_page_class(screen_name, pkg, elements, output_dir):
    """Sinh file Java Page Object cho 1 màn hình."""
    class_name = to_class_name(screen_name)
    timestamp  = datetime.now().strftime("%Y-%m-%d %H:%M")

    field_lines   = []
    method_blocks = []
    seen_names    = {}

    for elem in elements:
        raw = elem["content_desc"] or elem["text"] or elem["resource_id"].split("/")[-1]
        field_name = to_java_field_name(raw)

        # Tránh trùng tên field
        if field_name in seen_names:
            seen_names[field_name] += 1
            field_name = f"{field_name}{seen_names[field_name]}"
        else:
            seen_names[field_name] = 0

        field_lines.append(build_locator_line(elem, field_name))
        method_blocks.append(build_method(elem, field_name))

    locator_section = "\n".join(field_lines)
    methods_section = "\n\n".join(method_blocks)

    java_code = JAVA_TEMPLATE.format(
        pkg             = pkg,
        class_name      = class_name,
        screen_display  = screen_name,
        timestamp       = timestamp,
        locator_fields  = locator_section,
        methods         = methods_section,
    )

    pkg_path = output_dir / pkg.replace(".", "/")
    pkg_path.mkdir(parents=True, exist_ok=True)
    out_file = pkg_path / f"{class_name}.java"
    out_file.write_text(java_code, encoding="utf-8")
    print(f"  [gen]  Sinh file: src/test/java/pages/{pkg}/{class_name}.java ({len(elements)} elements)")
    return out_file

# ─── Navigation plan ─────────────────────────────────────────────────────────
# Mỗi entry: (AppiumBy_type, locator_value, tên_màn_hình, java_package)
BOTTOM_NAV_TABS = [
    (AppiumBy.XPATH, "//*[@content-desc='Home']",   "trang_chu",  "trangchu"),
    (AppiumBy.XPATH, "//*[@content-desc='Online']", "truc_tuyen", "tructuyen"),
    (AppiumBy.XPATH, "//android.widget.ImageView[@bounds='[432,2096][648,2274]']", "hat", "hat"),
    (AppiumBy.XPATH, "//*[starts-with(@content-desc, 'Message')]", "tin_nhan", "tinnhan"),
    (AppiumBy.XPATH, "//*[@content-desc='Me']",     "toi",        "toi"),
]

# ─── Main ────────────────────────────────────────────────────────────────────
def main():
    print("=" * 60)
    print("  Auto POM Generator — Yokara Automation")
    print("=" * 60)

    cfg = read_config()
    try:
        driver = create_driver(cfg)
    except Exception as e:
        # The instruction "Dùng open file write" is already satisfied by the existing code.
        # The provided "Code Edit" snippet appears to be malformed.
        # Assuming the intent was to ensure the file is opened for writing,
        # and to keep the code syntactically correct, no change is needed here.
        with open("appium_error.log", "w", encoding="utf-8") as f:
            f.write(str(e))
        print(f"\n[ERROR] Lỗi Appium! Chi tiết đã được lưu vào appium_error.log")
        return

    generated = []

    try:
        for (by_type, loc_val, screen_name, pkg) in BOTTOM_NAV_TABS:
            print(f"\n>>> Tab: {loc_val}")

            # Navigate to tab
            try:
                els = driver.find_elements(by_type, loc_val)
                if els:
                    els[0].click()
                    time.sleep(2)  # Chờ animation tab (đây là wait cho gesture, không phải wait cho element)
                else:
                    print(f"  [skip] Không tìm thấy tab '{loc_val}', bỏ qua")
                    continue
            except Exception as e:
                print(f"  [warn] Không click được tab '{loc_val}': {e}")
                continue

            # Dump page source
            xml_src = dump_page_source(driver, screen_name)

            # Parse
            elements = parse_elements(xml_src)
            print(f"  [parse] Tìm thấy {len(elements)} elements")

            # Generate Java
            out = generate_page_class(screen_name, pkg, elements, OUTPUT_DIR)
            generated.append(out)

        # Màn hình hiện tại (bất kể đang ở đâu) — capture thêm
        print(f"\n>>> Màn hình hiện tại (backup dump)")
        xml_src  = dump_page_source(driver, "current_screen")
        elements = parse_elements(xml_src)
        print(f"  [parse] {len(elements)} elements")

    finally:
        driver.quit()
        print("\n[appium] Session đã đóng.")

    print("\n" + "=" * 60)
    print(f"  ✅ Đã sinh {len(generated)} Page Object file(s):")
    for f in generated:
        rel = f.relative_to(PROJECT_ROOT)
        print(f"     {rel}")
    print("\n  📝 Tiếp theo:")
    print("     1. Mở các file .java vừa sinh trong IDE")
    print("     2. Kiểm tra locator (có thể cần chỉnh cho chính xác)")
    print("     3. Thêm logic nghiệp vụ vào các method")
    print("     4. Các method click/type/isDisplayed đã sẵn sàng dùng")
    print("=" * 60)

if __name__ == "__main__":
    main()
