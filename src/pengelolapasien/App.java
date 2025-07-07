package pengelolapasien;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import java.util.Locale;
import pengelolapasien.auth.LoginForm;
import pengelolapasien.i18n.I18n;

public final class App {

    private App() {
    }   // util‑class, no instantiation

    public static void main(String[] args) {

        /* 0) ——— Locale global JVM: Bahasa Indonesia ——————————— */
        //  ‑ Locale.setDefault() perlu dipanggil SEBELUM class lain
        //    yang mungkin membaca Locale bawaan (mis. DateFormat).
        Locale indo = new Locale("id", "ID");
        Locale.setDefault(indo);
        I18n.setLocale(indo);      // sinkronkan bundle milik aplikasi

        /* 1) ——— Nimbus Look & Feel (jika tersedia) ——————————— */
        try {
            for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ex) {
            // tidak fatal – gunakan L&F default
        }

        /* 2) ——— Tampilkan layar login di EDT ——————————————— */
        SwingUtilities.invokeLater(() -> new LoginForm().setVisible(true));
    }
}
