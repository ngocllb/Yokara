package core;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Tiện ích macOS: tự phát hiện Apple Development Team ID cho ký WebDriverAgent.
 */
final class IosEnvHelper {

    private static final Pattern TEAM_IN_PARENS = Pattern.compile("\\(([A-Z0-9]{10})\\)\\s*$");

    private IosEnvHelper() {
    }

    static boolean isMacOs() {
        String os = System.getProperty("os.name", "");
        return os.toLowerCase().contains("mac");
    }

    /**
     * Đọc Team ID đầu tiên từ {@code security find-identity} (Apple Development / Apple Distribution).
     */
    static String detectFirstAppleTeamId() {
        if (!isMacOs()) {
            return null;
        }
        try {
            Process process = new ProcessBuilder(
                    "security", "find-identity", "-v", "-p", "codesigning"
            ).redirectErrorStream(true).start();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.contains("Apple Development") && !line.contains("Apple Distribution")) {
                        continue;
                    }
                    Matcher m = TEAM_IN_PARENS.matcher(line.trim());
                    if (m.find()) {
                        String team = m.group(1);
                        System.out.println("[IosEnvHelper] Tự phát hiện Team ID: " + team);
                        return team;
                    }
                }
            }
            process.waitFor();
        } catch (Exception e) {
            System.out.println("[IosEnvHelper] Không đọc được Team ID: " + e.getMessage());
        }
        return null;
    }
}
