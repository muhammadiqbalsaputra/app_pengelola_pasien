package pengelolapasien.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Model entitas PASIEN
 */
public class Pasien implements Serializable {

    private static final long serialVersionUID = 1L;

    /* ---------- kolom ---------- */
    private int    id;            // PK (auto‑increment)
    private String nama;
    private String nik;
    private String telepon;
    private String alamat;
    private Date   tanggalMasuk;
    private String jenisKelamin;
    private String diagnosa;

    /* ---------- constructor untuk data BARU (belum ada ID) ---------- */
    public Pasien(String nama, String nik, String telepon, String alamat,
                  Date tanggalMasuk, String jenisKelamin, String diagnosa) {
        this.nama          = nama;
        this.nik           = nik;
        this.telepon       = telepon;
        this.alamat        = alamat;
        this.tanggalMasuk  = tanggalMasuk;
        this.jenisKelamin  = jenisKelamin;
        this.diagnosa      = diagnosa;
    }

    /* ---------- constructor LENGKAP (data dari DB) ---------- */
    public Pasien(int id, String nama, String nik, String telepon, String alamat,
                  Date tanggalMasuk, String jenisKelamin, String diagnosa) {
        this(nama, nik, telepon, alamat, tanggalMasuk, jenisKelamin, diagnosa);
        this.id = id;
    }

    /* ---------- getter & setter ---------- */
    public int    getId()             { return id; }
    public void   setId(int id)       { this.id = id; }

    public String getNama()           { return nama; }
    public void   setNama(String n)   { this.nama = n; }

    public String getNik()            { return nik; }
    public void   setNik(String n)    { this.nik = n; }

    public String getTelepon()        { return telepon; }
    public void   setTelepon(String t){ this.telepon = t; }

    public String getAlamat()         { return alamat; }
    public void   setAlamat(String a) { this.alamat = a; }

    public Date   getTanggalMasuk()   { return tanggalMasuk; }
    public void   setTanggalMasuk(Date d){ this.tanggalMasuk = d; }

    public String getJenisKelamin()   { return jenisKelamin; }
    public void   setJenisKelamin(String jk){ this.jenisKelamin = jk; }

    public String getDiagnosa()       { return diagnosa; }
    public void   setDiagnosa(String d){ this.diagnosa = d; }

    @Override
    public String toString() {
        return String.format("%s (%s)", nama, nik);
    }
}
