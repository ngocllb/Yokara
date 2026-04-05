package core;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Đọc thông tin máy iOS thật qua {@code ideviceinfo} (libimobiledevice).
 */
final class IosDeviceInfo {

    private IosDeviceInfo() {
    }

    static String queryDeviceName(String udid) {
        return queryKey(udid, "DeviceName");
    }

    /** Ví dụ 18.4.1 → 18.4 để khớp capability Appium/Xcode. */
    static String queryProductVersionNormalized(String udid) {
        String raw = queryKey(udid, "ProductVersion");
        return normalizeIosVersion(raw);
    }

    static String normalizeIosVersion(String productVersion) {
        if (productVersion == null || productVersion.isBlank()) {
            return null;
        }
        String v = productVersion.trim();
        String[] p = v.split("\\.");
        if (p.length >= 2) {
            return p[0] + "." + p[1];
        }
        return v;
    }

    private static String queryKey(String udid, String key) {
        if (udid == null || udid.isBlank() || key == null) {
            return null;
        }
        List<String> out = run("ideviceinfo", "-u", udid, "-k", key);
        if (out.isEmpty()) {
            return null;
        }
        String v = out.get(0).trim();
        return v.isEmpty() ? null : v;
    }

    private static List<String> run(String... cmd) {
        List<String> lines = new ArrayList<>();
        try {
            Process p = new ProcessBuilder(cmd).redirectErrorStream(true).start();
            try (BufferedReader r = new BufferedReader(
                    new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = r.readLine()) != null) {
                    if (!line.isBlank()) {
                        lines.add(line);
                    }
                }
            }
            p.waitFor();
        } catch (Exception ignored) {
        }
        return lines;
    }
}
