package application.utils;

public class ValidateUtil {
    public static boolean isNullOrBlank(String... values) {
        for (String value : values) {
            if (value == null || value.isBlank()) {
                return true;
            }
        }
        return false;
    }
}
