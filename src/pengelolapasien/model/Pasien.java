package pengelolapasien.model;

import java.io.Serializable;
import java.util.Date;

public class Pasien implements Serializable {
    private String nama, nik, telepon, alamat, diagnosa, jenisKelamin;
    private Date tanggalMasuk;

    public Pasien(String nama, String nik, String telepon, String alamat, Date tanggalMasuk, String jenisKelamin, String diagnosa) {
        this.nama = nama;
        this.nik = nik;
        this.telepon = telepon;
        this.alamat = alamat;
        this.tanggalMasuk = tanggalMasuk;
        this.jenisKelamin = jenisKelamin;
        this.diagnosa = diagnosa;
    }

    // Getter dan Setter...
    public String getNama() { return nama; }
    public String getNik() { return nik; }
    public String getTelepon() { return telepon; }
    public String getAlamat() { return alamat; }
    public Date getTanggalMasuk() { return tanggalMasuk; }
    public String getJenisKelamin() { return jenisKelamin; }
    public String getDiagnosa() { return diagnosa; }
}
