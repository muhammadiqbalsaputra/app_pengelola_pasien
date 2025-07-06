package pengelolapasien.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/dataforpasien";
    private static final String USER = "root";      // Ubah sesuai user MySQL
    private static final String PASSWORD = "";      // Ubah sesuai password MySQL

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // Untuk testing langsung
    public static void main(String[] args) {
        try (Connection conn = getConnection()) {
            if (conn != null) {
                System.out.println("✅ Koneksi ke database berhasil!");
            }
        } catch (SQLException e) {
            System.err.println("❌ Koneksi gagal: " + e.getMessage());
        }
    }
}
