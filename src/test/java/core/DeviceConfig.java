package core;

public class DeviceConfig {
    private final String platform;
    private final String appiumServer;
    private final String udid;

    // iOS
    private final Integer wdaLocalPort;
    private final Integer mjpegServerPort;
    private final String derivedDataPath;

    // Android
    private final Integer systemPort;

    public DeviceConfig(
            String platform,
            String appiumServer,
            String udid,
            Integer wdaLocalPort,
            Integer mjpegServerPort,
            String derivedDataPath,
            Integer systemPort
    ) {
        this.platform = platform;
        this.appiumServer = appiumServer;
        this.udid = udid;
        this.wdaLocalPort = wdaLocalPort;
        this.mjpegServerPort = mjpegServerPort;
        this.derivedDataPath = derivedDataPath;
        this.systemPort = systemPort;
    }

    public String getPlatform() {
        return platform;
    }

    public String getAppiumServer() {
        return appiumServer;
    }

    public String getUdid() {
        return udid;
    }

    public Integer getWdaLocalPort() {
        return wdaLocalPort;
    }

    public Integer getMjpegServerPort() {
        return mjpegServerPort;
    }

    public String getDerivedDataPath() {
        return derivedDataPath;
    }

    public Integer getSystemPort() {
        return systemPort;
    }

    @Override
    public String toString() {
        return "DeviceConfig{" +
                "platform='" + platform + '\'' +
                ", appiumServer='" + appiumServer + '\'' +
                ", udid='" + udid + '\'' +
                ", wdaLocalPort=" + wdaLocalPort +
                ", mjpegServerPort=" + mjpegServerPort +
                ", derivedDataPath='" + derivedDataPath + '\'' +
                ", systemPort=" + systemPort +
                '}';
    }
}