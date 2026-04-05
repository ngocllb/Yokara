package testdata;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Đọc dữ liệu test từ classpath, hỗ trợ nhiều lớp file và ghi đè cho CI.
 * <p>
 * <b>Thứ tự nạp file</b> (sau đó mỗi file sau ghi đè key trùng của file trước):
 * <ol>
 *   <li>{@code testdata.properties} (bắt buộc)</li>
 *   <li>{@code testdata-&lt;profile&gt;.properties} nếu đặt {@code testdata.profile} hoặc {@code TESTDATA_PROFILE}</li>
 *   <li>{@code testdata.local.properties} (tùy chọn, thường gitignore — secrets local)</li>
 * </ol>
 * <p>
 * <b>Giá trị từng key</b>: {@link System#getProperty(String)} (tên key như trong file) →
 * {@code TESTDATA_&lt;KEY&gt;} (ví dụ {@code login.uid} → {@code TESTDATA_LOGIN_UID}) → giá trị sau khi merge file.
 */
public final class TestDataReader {

    private static final String BASE_RESOURCE = "testdata.properties";
    private static final String LOCAL_RESOURCE = "testdata.local.properties";
    private static final String ENV_PREFIX = "TESTDATA_";
    private static final String PROFILE_SYS = "testdata.profile";
    private static final String PROFILE_ENV = "TESTDATA_PROFILE";
    private static final Properties PROPERTIES = loadMerged();

    private TestDataReader() {
    }

    private static ClassLoader loader() {
        return Thread.currentThread().getContextClassLoader();
    }

    private static String resolveProfile() {
        String fromSys = System.getProperty(PROFILE_SYS);
        if (fromSys != null && !fromSys.isBlank()) {
            return fromSys.trim();
        }
        String fromEnv = System.getenv(PROFILE_ENV);
        if (fromEnv != null && !fromEnv.isBlank()) {
            return fromEnv.trim();
        }
        return null;
    }

    private static void loadInto(Properties target, String resourceName, boolean required) {
        try (InputStream in = loader().getResourceAsStream(resourceName)) {
            if (in == null) {
                if (required) {
                    throw new IllegalStateException("Missing classpath resource: " + resourceName);
                }
                return;
            }
            Properties layer = new Properties();
            layer.load(in);
            target.putAll(layer);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot load " + resourceName, e);
        }
    }

    private static Properties loadMerged() {
        Properties merged = new Properties();
        loadInto(merged, BASE_RESOURCE, true);
        String profile = resolveProfile();
        if (profile != null) {
            loadInto(merged, "testdata-" + profile + ".properties", false);
        }
        loadInto(merged, LOCAL_RESOURCE, false);
        return merged;
    }

    private static String envKeyFor(String propertyKey) {
        return ENV_PREFIX + propertyKey.replace('.', '_').toUpperCase();
    }

    public static String get(String key) {
        String fromSys = System.getProperty(key);
        if (fromSys != null && !fromSys.isBlank()) {
            return fromSys.trim();
        }

        String fromEnv = System.getenv(envKeyFor(key));
        if (fromEnv != null && !fromEnv.isBlank()) {
            return fromEnv.trim();
        }

        String fromFiles = PROPERTIES.getProperty(key);
        return fromFiles == null ? null : fromFiles.trim();
    }

    public static String getRequired(String key) {
        String value = get(key);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Missing test data for key: " + key);
        }
        return value;
    }
}
