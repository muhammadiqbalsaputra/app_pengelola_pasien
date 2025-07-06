package pengelolapasien.util;

import pengelolapasien.model.Pasien;
import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.util.List;

public final class BackupUtil {

    /* ==== ganti kunci 16‑byte sesuai kebutuhan ==== */
    private static final String AES_KEY = "p4ssw0rdp4ssw0rd";

    /* ---------------- helper cipher ---------------- */
    private static Cipher cipher(int mode) throws Exception {
        SecretKeySpec key = new SecretKeySpec(AES_KEY.getBytes(), "AES");
        Cipher c = Cipher.getInstance("AES");
        c.init(mode, key);
        return c;
    }

    /* ---------------- save list ke file .ser (plain) ---------------- */
    public static void savePlain(List<Pasien> list, File out) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(out))) {
            oos.writeObject(list);
        }
    }

    /* ---------------- load list dari file .ser (plain) -------------- */
    @SuppressWarnings("unchecked")
    public static List<Pasien> loadPlain(File src) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(src))) {
            return (List<Pasien>) ois.readObject();
        }
    }

    /* ---------------- save terenkripsi AES -------------------------- */
    public static void saveEncrypted(List<Pasien> list, File out) throws Exception {
        try (CipherOutputStream cos =
                    new CipherOutputStream(new FileOutputStream(out), cipher(Cipher.ENCRYPT_MODE));
             ObjectOutputStream oos = new ObjectOutputStream(cos)) {
            oos.writeObject(list);
        }
    }

    /* ---------------- load terenkripsi AES -------------------------- */
    @SuppressWarnings("unchecked")
    public static List<Pasien> loadEncrypted(File src) throws Exception {
        try (CipherInputStream cis =
                    new CipherInputStream(new FileInputStream(src), cipher(Cipher.DECRYPT_MODE));
             ObjectInputStream ois = new ObjectInputStream(cis)) {
            return (List<Pasien>) ois.readObject();
        }
    }

    private BackupUtil() {} // utility class
}
