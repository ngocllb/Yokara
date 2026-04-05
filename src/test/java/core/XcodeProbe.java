package core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Nhận diện Xcode đang active (xcode-select) và các bản iOS DeviceSupport có trên máy.
 */
public final class XcodeProbe {

    private XcodeProbe() {
    }

    public static void logSummaryIfEnabled() {
        if (!IosEnvHelper.isMacOs()) {
            return;
        }
        String flag = ConfigManager.get("ios.logXcodeEnvironment");
        if (!"false".equalsIgnoreCase(flag == null ? "" : flag)) {
            logSummary();
        }
    }

    /**
     * In ra stderr/stdout cho Maven: đường dẫn Xcode, phiên bản, danh sách DeviceSupport.
     */
    public static void logSummary() {
        if (!IosEnvHelper.isMacOs()) {
            return;
        }
        String devDir = readDeveloperDir();
        System.out.println("[XcodeProbe] DEVELOPER_DIR (xcode-select): "
                + (devDir != null ? devDir : "(không đọc được — chạy: sudo xcode-select -s /Applications/Xcode.app/Contents/Developer)"));

        List<String> verLines = runCommandOutput("xcodebuild", "-version");
        if (verLines.isEmpty()) {
            System.out.println("[XcodeProbe] xcodebuild: (lỗi hoặc không có trong PATH)");
        } else {
            verLines.forEach(l -> System.out.println("[XcodeProbe] " + l));
        }

        if (devDir != null) {
            Path ds = Paths.get(devDir, "Platforms/iPhoneOS.platform/DeviceSupport");
            List<String> packs = listDeviceSupportNames(ds);
            if (packs.isEmpty()) {
                System.out.println("[XcodeProbe] DeviceSupport: (không có hoặc không đọc được: " + ds + ")");
            } else {
                System.out.println("[XcodeProbe] DeviceSupport (" + packs.size() + "): "
                        + packs.stream().limit(12).collect(Collectors.joining(", "))
                        + (packs.size() > 12 ? ", …" : ""));
            }
        }
    }

    public static String readDeveloperDir() {
        List<String> out = runCommandOutput("xcode-select", "-p");
        if (out.isEmpty()) {
            return null;
        }
        String p = out.get(0).trim();
        return p.isEmpty() ? null : p.replaceAll("/$", "");
    }

    private static List<String> listDeviceSupportNames(Path deviceSupportDir) {
        if (!Files.isDirectory(deviceSupportDir)) {
            return Collections.emptyList();
        }
        List<String> names = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(deviceSupportDir)) {
            for (Path p : stream) {
                if (Files.isDirectory(p)) {
                    names.add(p.getFileName().toString());
                }
            }
        } catch (IOException e) {
            return Collections.emptyList();
        }
        Collections.sort(names);
        return names;
    }

    private static List<String> runCommandOutput(String... cmd) {
        List<String> lines = new ArrayList<>();
        try {
            Process p = new ProcessBuilder(cmd)
                    .redirectErrorStream(true)
                    .start();
            try (BufferedReader r = new BufferedReader(
                    new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = r.readLine()) != null) {
                    lines.add(line);
                }
            }
            p.waitFor();
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
        }
        return lines;
    }
}
