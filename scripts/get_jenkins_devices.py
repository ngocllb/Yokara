import subprocess
import json
import re

def get_android_devices():
    devices = []
    try:
        output = subprocess.check_output(['adb', 'devices']).decode('utf-8')
        lines = [line.strip() for line in output.split('\n') if line.strip()]
        for line in lines[1:]: # Skip "List of devices attached"
            parts = line.split()
            if len(parts) >= 2 and parts[1] == 'device':
                udid = parts[0]
                if not udid.startswith('emulator-'):
                    devices.append({"platform": "android", "udid": udid})
    except Exception as e:
        pass
    return devices

def get_ios_devices():
    devices = []
    try:
        # 1. Try idevice_id
        output = subprocess.check_output(['idevice_id', '-l']).decode('utf-8')
        lines = [line.strip() for line in output.split('\n') if line.strip()]
        for line in lines:
            if re.match(r'[0-9a-fA-F]{8}-[0-9a-fA-F]{16}|[0-9a-fA-F]{40}', line):
                devices.append({"platform": "ios", "udid": line})
    except Exception as e:
        import sys
        print(f"[get_jenkins_devices] idevice_id failed: {e}", file=sys.stderr)

    if not devices:
        try:
            # 2. Fallback to tidevice
            output = subprocess.check_output(['tidevice', 'list', '--json']).decode('utf-8')
            lines = [line.strip() for line in output.split('\n') if line.strip()]
            for line in lines:
                udid_match = re.search(r'"udid"\s*:\s*"([^"]+)"', line)
                if udid_match:
                    devices.append({"platform": "ios", "udid": udid_match.group(1)})
        except Exception as e:
            import sys
            print(f"[get_jenkins_devices] Fallback tidevice failed: {e}", file=sys.stderr)

    return devices

def main():
    android_devices = get_android_devices()
    ios_devices = get_ios_devices()
    
    all_devices = android_devices + ios_devices
    
    # We output clean plain-text that Jenkins can parse natively without the readJSON plugin.
    # Format: platform|udid
    for device in all_devices:
        print(f"{device['platform']}|{device['udid']}")

if __name__ == '__main__':
    main()
