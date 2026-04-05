package core;

/**
 * Một slot chạy automation: một UDID + tên hiển thị + thư mục Allure (đã sanitize).
 */
public record AutomationDeviceSlot(String platform, String udid, String displayName, String reportFolderName) {

    public String testNgTestName() {
        String shortId = udid.length() > 8 ? udid.substring(udid.length() - 8) : udid;
        return platform + "-" + reportFolderName + "-" + shortId;
    }
}
