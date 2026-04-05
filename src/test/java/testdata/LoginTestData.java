package testdata;

/**
 * Bộ dữ liệu đăng nhập — nạp từ {@code testdata.properties} hoặc ghi đè qua env/system property.
 */
public record LoginTestData(String uid, String password, String phone, String otpValid, String otpInvalid) {

    public static LoginTestData load() {
        return new LoginTestData(
                TestDataReader.getRequired("login.uid"),
                TestDataReader.getRequired("login.password"),
                TestDataReader.getRequired("login.phone"),
                TestDataReader.getRequired("login.otp.valid"),
                TestDataReader.getRequired("login.otp.invalid")
        );
    }
}
