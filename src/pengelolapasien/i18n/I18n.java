package pengelolapasien.i18n;

import java.util.Locale;
import java.util.ResourceBundle;

public class I18n {
    private static Locale currentLocale = new Locale("id"); // default
    private static ResourceBundle bundle = loadBundle();

    private static ResourceBundle loadBundle() {
        return ResourceBundle.getBundle("pengelolapasien.i18n.messages", currentLocale);
    }

    public static void setLocale(Locale locale) {
        currentLocale = locale;
        bundle = loadBundle();
    }

    public static String t(String key) {
        try {
            return bundle.getString(key);
        } catch (Exception e) {
            return "!" + key + "!";
        }
    }

    public static Locale getLocale() {
        return currentLocale;
    }
}
