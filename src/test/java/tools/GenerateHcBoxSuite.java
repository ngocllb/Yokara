package tools;

import core.AutomationDeviceSlot;
import core.DeviceManager;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Sinh {@code target/testng-hc-box.xml}: một {@code &lt;test&gt;} / thiết bị, song song toàn bộ.
 * Chạy qua Maven exec (profile {@code hc-box}).
 */
public final class GenerateHcBoxSuite {

    private GenerateHcBoxSuite() {
    }

    public static void main(String[] args) throws Exception {
        List<AutomationDeviceSlot> slots = DeviceManager.listAutomationSlots();
        if (slots.isEmpty()) {
            System.err.println("[GenerateHcBoxSuite] Không có thiết bị Android/iOS USB. Kiểm tra adb / idevice_id.");
            System.exit(1);
            return;
        }

        Path out = Paths.get("target", "testng-hc-box.xml");
        Files.createDirectories(out.getParent());
        Files.writeString(out, buildXml(slots), StandardCharsets.UTF_8);
        System.out.println("[GenerateHcBoxSuite] " + slots.size() + " thiết bị → " + out.toAbsolutePath());
    }

    private static String buildXml(List<AutomationDeviceSlot> slots) {
        int n = Math.max(slots.size(), 1);
        StringBuilder sb = new StringBuilder(8192);
        sb.append("<!DOCTYPE suite SYSTEM \"https://testng.org/testng-1.0.dtd\">\n");
        sb.append("<!-- Auto-generated — mvn test -Phc-box — song song theo số thiết bị USB -->\n");
        sb.append("<suite name=\"Yokara HC BOX (N thiết bị)\" parallel=\"tests\" thread-count=\"")
                .append(n).append("\">\n\n");
        sb.append("    <!-- AllureTestNg: SPI allure-testng — tránh khai báo trùng trong suite -->\n");
        sb.append("    <listeners>\n");
        sb.append("        <listener class-name=\"listeners.AllureListener\"/>\n");
        sb.append("    </listeners>\n\n");

        for (AutomationDeviceSlot s : slots) {
            sb.append("    <test name=\"").append(escAttr(s.testNgTestName())).append("\">\n");
            sb.append("        <parameter name=\"suitePlatform\" value=\"").append(escAttr(s.platform())).append("\"/>\n");
            sb.append("        <parameter name=\"suiteUdid\" value=\"").append(escAttr(s.udid())).append("\"/>\n");
            sb.append("        <parameter name=\"suiteDeviceLabel\" value=\"").append(escAttr(humanLabel(s))).append("\"/>\n");
            sb.append("        <parameter name=\"suiteDeviceFolder\" value=\"").append(escAttr(s.reportFolderName())).append("\"/>\n");
            sb.append("        <classes>\n");
            sb.append("            <class name=\"tests.NavigationTest\"/>\n");
            sb.append("            <class name=\"tests.LoginMethodTest\"/>\n");
            sb.append("            <class name=\"tests.RoomTest\"/>\n");
            sb.append("        </classes>\n");
            sb.append("    </test>\n\n");
        }

        sb.append("</suite>\n");
        return sb.toString();
    }

    private static String humanLabel(AutomationDeviceSlot s) {
        return s.platform().toUpperCase() + " · " + s.displayName() + " · " + s.udid();
    }

    private static String escAttr(String v) {
        if (v == null) {
            return "";
        }
        return v.replace("&", "&amp;")
                .replace("\"", "&quot;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}
