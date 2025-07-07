package pengelolapasien.dao;

import pengelolapasien.db.DBConnection;
import pengelolapasien.model.Pasien;
import pengelolapasien.util.GenericDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PasienDAO implements GenericDAO<Pasien, Integer> {

    // ================= INSERT =================
    @Override
    public void insert(Pasien p) throws Exception {
        String sql = "INSERT INTO pasien (nama, nik, telepon, alamat, tanggal_masuk, jenis_kelamin, diagnosa) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            fillPreparedStatement(ps, p, false);
            ps.executeUpdate();

            // Ambil ID yang di-generate
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    p.setId(rs.getInt(1));
                }
            }
        }
    }

    // ================= UPDATE =================
    @Override
    public void update(Pasien p) throws Exception {
        String sql = "UPDATE pasien SET nama=?, nik=?, telepon=?, alamat=?, tanggal_masuk=?, jenis_kelamin=?, diagnosa=? WHERE id=?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            fillPreparedStatement(ps, p, true);
            ps.executeUpdate();
        }
    }

    // ================= DELETE =================
    @Override
    public void delete(Integer id) throws Exception {
        String sql = "DELETE FROM pasien WHERE id=?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // ================= FIND BY ID =============
    @Override
    public Pasien findById(Integer id) throws Exception {
        String sql = "SELECT * FROM pasien WHERE id=?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return toPasien(rs);
                }
            }
        }
        return null;
    }

    // ================= FIND ALL ===============
    @Override
    public List<Pasien> findAll() throws Exception {
        List<Pasien> list = new ArrayList<>();
        String sql = "SELECT * FROM pasien";
        try (Connection conn = DBConnection.getConnection(); Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                list.add(toPasien(rs));
            }
        }
        return list;
    }

    // ============== HELPER METHOD =============
    private void fillPreparedStatement(PreparedStatement ps, Pasien p, boolean withId) throws SQLException {
        ps.setString(1, p.getNama());
        ps.setString(2, p.getNik());
        ps.setString(3, p.getTelepon());
        ps.setString(4, p.getAlamat());
        ps.setDate(5, new java.sql.Date(p.getTanggalMasuk().getTime()));
        ps.setString(6, p.getJenisKelamin());
        ps.setString(7, p.getDiagnosa());

        if (withId) {
            ps.setInt(8, p.getId());
        }
    }

    private Pasien toPasien(ResultSet rs) throws SQLException {
        return new Pasien(
                rs.getInt("id"),
                rs.getString("nama"),
                rs.getString("nik"),
                rs.getString("telepon"),
                rs.getString("alamat"),
                rs.getDate("tanggal_masuk"),
                rs.getString("jenis_kelamin"),
                rs.getString("diagnosa")
        );
    }
}
