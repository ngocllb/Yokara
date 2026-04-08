#!/opt/homebrew/bin/python3
import subprocess
import re
import sys


def run_command(cmd):
    try:
        return subprocess.check_output(cmd, stderr=subprocess.STDOUT).decode("utf-8", errors="ignore")
    except Exception as e:
        print(f"[get_jenkins_devices] command failed: {' '.join(cmd)} => {e}", file=sys.stderr)
        return ""


def get_android_devices():
    devices = []
    output = run_command(["adb", "devices"])

    if not output:
        return devices

    lines = [line.strip() for line in output.splitlines() if line.strip()]

    for line in lines[1:]:  # skip "List of devices attached"
        parts = line.split()
        if len(parts) >= 2 and parts[1] == "device":
            udid = parts[0].strip()
            if not udid.startswith("emulator-"):
                devices.append({
                    "platform": "android",
                    "udid": udid
                })

    return devices


def get_ios_devices():
    devices = []

    # Cách 1: idevice_id -l
    output = run_command(["idevice_id", "-l"])
    if output:
        lines = [line.strip() for line in output.splitlines() if line.strip()]
        for line in lines:
            # hỗ trợ cả UDID 40 ký tự và dạng có dấu -
            if re.fullmatch(r"[0-9a-fA-F]{40}", line) or re.fullmatch(r"[0-9A-Fa-f-]{20,}", line):
                devices.append({
                    "platform": "ios",
                    "udid": line
                })

    # Cách 2: fallback tidevice nếu idevice_id không ra gì
    if not devices:
        output = run_command(["tidevice", "list", "--json"])
        if output:
            for line in output.splitlines():
                line = line.strip()
                if not line:
                    continue
                udid_match = re.search(r'"udid"\s*:\s*"([^"]+)"', line)
                if udid_match:
                    devices.append({
                        "platform": "ios",
                        "udid": udid_match.group(1).strip()
                    })

    return devices


def deduplicate_devices(devices):
    seen = set()
    result = []

    for d in devices:
        key = (d["platform"], d["udid"])
        if key not in seen:
            seen.add(key)
            result.append(d)

    return result


def main():
    ios_devices = get_ios_devices()
    android_devices = get_android_devices()

    # Ưu tiên iOS trước, rồi Android
    all_devices = ios_devices + android_devices
    all_devices = deduplicate_devices(all_devices)

    # Sort để thứ tự ổn định giữa các lần chạy
    all_devices = sorted(all_devices, key=lambda x: (x["platform"], x["udid"]))

    # Jenkinsfile hiện tại đang parse theo format: platform|udid
    for device in all_devices:
        print(f"{device['platform']}|{device['udid']}")


if __name__ == "__main__":
    main()