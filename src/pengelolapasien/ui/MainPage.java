/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package pengelolapasien.ui;

import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Vector;
import javax.swing.table.TableColumn;
import pengelolapasien.db.DBConnection;
import pengelolapasien.model.Pasien;
import pengelolapasien.dao.PasienDAO;   // ⬅ import
import java.util.List;                  // ⬅ import
import java.util.Locale;
import pengelolapasien.i18n.I18n;

public class MainPage extends javax.swing.JFrame {

    DefaultTableModel model;
    private final PasienDAO pasienDAO = new PasienDAO();

    public MainPage() {
        initComponents();

        model = (DefaultTableModel) tblPasien.getModel();   // pastikan model di‑init

        cmbBahasa = new javax.swing.JComboBox<>();
        cmbBahasa.addItem("Indonesia");
        cmbBahasa.addItem("English");
        cmbBahasa.addActionListener(e -> onLanguageChanged());

        settingsPanel.add(cmbBahasa);

        loadComboJenisKelamin();                            // ⬅️ panggilan penting
        setLocationRelativeTo(null);

        loadDataPasienAsync();
        hideIdColumn();
        applyI18n();
    }

    // ===========  CRUD & UTIL ===========
    /**
     * Memuat data pasien di background supaya UI tetap responsif
     */
    private void loadDataPasienAsync() {

        btnRefresh.setEnabled(false);          // opsional: matikan tombol

        new javax.swing.SwingWorker<java.util.List<Pasien>, Void>() {

            @Override
            protected java.util.List<Pasien> doInBackground() throws Exception {
                // proses berat → thread terpisah
                return pasienDAO.findAll();
            }

            @Override
            protected void done() {
                try {
                    tampilkanKeTabel(get());  // hasil ditampilkan di EDT
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(MainPage.this,
                            "Gagal load: " + ex.getMessage());
                } finally {
                    btnRefresh.setEnabled(true);
                }
            }
        }.execute();
    }

    private void tambahPasien() {
        if (!validasiInput()) {
            return;
        }

        try {
            Pasien p = new Pasien(
                    txtNama.getText().trim(),
                    txtNIK.getText().trim(),
                    txtTelepon.getText().trim(),
                    txtAlamat.getText().trim(),
                    dateTanggalMasuk.getDate(),
                    cmbJenisKelamin.getSelectedItem().toString(),
                    txtDiagnosa.getText().trim()
            );
            pasienDAO.insert(p);                    // ← DAO
            JOptionPane.showMessageDialog(this, "Pasien ditambahkan!");
            clearForm();
            loadDataPasienAsync();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal tambah: " + ex.getMessage());
        }
    }

    private void updatePasien() {
        int row = tblPasien.getSelectedRow();
        if (row == -1 || !validasiInput()) {
            return;
        }

        int modelRow = tblPasien.convertRowIndexToModel(row);
        int id = (int) model.getValueAt(modelRow, 0);

        try {
            Pasien p = new Pasien(
                    id,
                    txtNama.getText().trim(),
                    txtNIK.getText().trim(),
                    txtTelepon.getText().trim(),
                    txtAlamat.getText().trim(),
                    dateTanggalMasuk.getDate(),
                    cmbJenisKelamin.getSelectedItem().toString(),
                    txtDiagnosa.getText().trim()
            );
            pasienDAO.update(p);                    // ← DAO
            JOptionPane.showMessageDialog(this, "Data di‑update!");
            clearForm();
            loadDataPasienAsync();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal update: " + ex.getMessage());
        }
    }

    private void hapusPasien() {
        int row = tblPasien.getSelectedRow();
        if (row == -1) {
            return;
        }
        int id = (int) model.getValueAt(tblPasien.convertRowIndexToModel(row), 0);

        if (JOptionPane.showConfirmDialog(this, "Yakin hapus?", "Konfirmasi",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            pasienDAO.delete(id);                   // ← DAO
            JOptionPane.showMessageDialog(this, "Data terhapus!");
            clearForm();
            loadDataPasienAsync();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal hapus: " + ex.getMessage());
        }
    }

    private void isiFormDariTabel() {
        int row = tblPasien.getSelectedRow();
        if (row == -1) {
            return;
        }
        int mRow = tblPasien.convertRowIndexToModel(row);

        txtNama.setText(model.getValueAt(mRow, 1).toString());       // Nama
        txtNIK.setText(model.getValueAt(mRow, 2).toString());        // NIK
        txtTelepon.setText(model.getValueAt(mRow, 3).toString());    // Telepon
        txtAlamat.setText(model.getValueAt(mRow, 4).toString());     // Alamat
        try {
            String tglStr = model.getValueAt(mRow, 5).toString(); // misalnya "2025-07-07"
            java.util.Date parsedDate = new SimpleDateFormat("yyyy-MM-dd").parse(tglStr);
            dateTanggalMasuk.setDate(parsedDate);
        } catch (Exception e) {
            e.printStackTrace();
            dateTanggalMasuk.setDate(null);
        }
        cmbJenisKelamin.setSelectedItem(model.getValueAt(mRow, 6));  // JK
        txtDiagnosa.setText(model.getValueAt(mRow, 7).toString());   // Diagnosa
    }

    private void clearForm() {
        txtNama.setText("");          // ← tambahkan
        txtNIK.setText("");
        txtTelepon.setText("");
        txtAlamat.setText("");
        txtDiagnosa.setText("");
        txtCari.setText("");
        cmbJenisKelamin.setSelectedIndex(0);
        dateTanggalMasuk.setDate(null);
        tblPasien.clearSelection();
    }

    private boolean validasiInput() {
        if (txtNama.getText().trim().isEmpty()
                || // ← tambahkan Nama
                txtNIK.getText().trim().isEmpty()
                || txtTelepon.getText().trim().isEmpty()
                || txtAlamat.getText().trim().isEmpty()
                || txtDiagnosa.getText().trim().isEmpty()
                || dateTanggalMasuk.getDate() == null) {
            JOptionPane.showMessageDialog(this, "Isi semua field!");
            return false;
        }
        return true;
    }

    private void isiPreparedStatement(PreparedStatement ps, boolean withId, int id) throws Exception {
        ps.setString(1, txtNama.getText().trim());          // nama
        ps.setString(2, txtNIK.getText().trim());           // nik
        ps.setString(3, txtTelepon.getText().trim());       // telepon
        ps.setString(4, txtAlamat.getText().trim());        // alamat
        String tgl = new SimpleDateFormat("yyyy-MM-dd").format(dateTanggalMasuk.getDate());
        ps.setString(5, tgl);                               // tanggal_masuk
        ps.setString(6, txtDiagnosa.getText().trim());      // diagnosa
        ps.setString(7, cmbJenisKelamin.getSelectedItem().toString()); // jenis_kelamin
        if (withId) {
            ps.setInt(8, id);                       // id untuk UPDATE
        }
    }

    private void hideIdColumn() {
        TableColumn idCol = tblPasien.getColumnModel().getColumn(0);
        idCol.setMinWidth(0);
        idCol.setMaxWidth(0);
        idCol.setPreferredWidth(0);
    }

    private void logout() {
        new pengelolapasien.auth.LoginForm().setVisible(true);
        dispose();
    }

    private void loadComboJenisKelamin() {
        cmbJenisKelamin.removeAllItems();
        try (Connection conn = DBConnection.getConnection(); Statement st = conn.createStatement(); ResultSet rs = st.executeQuery("SELECT DISTINCT jenis_kelamin FROM pasien WHERE jenis_kelamin IS NOT NULL")) {

            while (rs.next()) {
                String jk = rs.getString("jenis_kelamin");
                cmbJenisKelamin.addItem(jk);
            }
        } catch (SQLException e) {
            // Jika gagal, tampilkan default saja
        }

        // Tambahkan default jika belum ada
        if (cmbJenisKelamin.getItemCount() == 0) {
            cmbJenisKelamin.addItem("Laki-Laki");
            cmbJenisKelamin.addItem("Perempuan");
        } else {
            // Tambahkan yang belum ada
            if (((DefaultComboBoxModel<String>) cmbJenisKelamin.getModel()).getIndexOf("Laki-Laki") == -1) {
                cmbJenisKelamin.addItem("Laki-Laki");
            }
            if (((DefaultComboBoxModel<String>) cmbJenisKelamin.getModel()).getIndexOf("Perempuan") == -1) {
                cmbJenisKelamin.addItem("Perempuan");
            }
        }
    }

    private void backupDataPasien() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("backup_pasien.ser"))) {
            try (Connection conn = DBConnection.getConnection(); Statement st = conn.createStatement(); ResultSet rs = st.executeQuery("SELECT * FROM pasien")) {
                while (rs.next()) {
                    Pasien p = new Pasien(
                            rs.getString("nama"),
                            rs.getString("nik"),
                            rs.getString("telepon"),
                            rs.getString("alamat"),
                            rs.getDate("tanggal_masuk"),
                            rs.getString("jenis_kelamin"),
                            rs.getString("diagnosa")
                    );
                    oos.writeObject(p);
                }
            }
            JOptionPane.showMessageDialog(this, "Backup berhasil disimpan ke backup_pasien.ser");
        } catch (IOException | SQLException ex) {
            JOptionPane.showMessageDialog(this, "Gagal backup: " + ex.getMessage());
        }
    }

    private void restoreDataPasien() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("backup_pasien.ser"))) {
            model.setRowCount(0); // bersihkan tabel

            while (true) {
                try {
                    Pasien p = (Pasien) ois.readObject();
                    Vector<Object> v = new Vector<>();
                    v.add(null); // ID = null (tidak dipakai saat restore)
                    v.add(p.getNama());
                    v.add(p.getNik());
                    v.add(p.getTelepon());
                    v.add(p.getAlamat());
                    v.add(p.getTanggalMasuk());
                    v.add(p.getJenisKelamin());
                    v.add(p.getDiagnosa());
                    model.addRow(v);
                } catch (EOFException e) {
                    break; // selesai
                }
            }

            JOptionPane.showMessageDialog(this, "Restore selesai dari backup_pasien.ser");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal restore: " + ex.getMessage());
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jSeparator1 = new javax.swing.JSeparator();
        jSeparator2 = new javax.swing.JSeparator();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        pasienPanel = new javax.swing.JPanel();
        formPanel = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        cmbJenisKelamin = new javax.swing.JComboBox<>();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        dateTanggalMasuk = new com.toedter.calendar.JDateChooser();
        btnTambah = new javax.swing.JButton();
        btnHapus = new javax.swing.JButton();
        btnRefresh = new javax.swing.JButton();
        btnEdit = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblPasien = new javax.swing.JTable();
        btnLogout = new javax.swing.JButton();
        jLabel9 = new javax.swing.JLabel();
        txtTelepon = new javax.swing.JTextField();
        txtNama = new javax.swing.JTextField();
        txtAlamat = new javax.swing.JTextField();
        txtNIK = new javax.swing.JTextField();
        txtDiagnosa = new javax.swing.JTextField();
        txtCari = new javax.swing.JTextField();
        btnBackup = new javax.swing.JButton();
        btnRestore = new javax.swing.JButton();
        settingsPanel = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        lblSectionTitle = new javax.swing.JLabel();
        cmbBahasa = new javax.swing.JComboBox<>();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(0, 204, 255));

        jLabel2.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel2.setText("Nama :");

        jLabel3.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel3.setText("Telepon :");

        jLabel4.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel4.setText("Alamat :");

        jLabel5.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel5.setText("Tanggal Masuk:");

        jLabel6.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel6.setText("Jenis Kelamin :");

        cmbJenisKelamin.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel7.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel7.setText("Diagnosa :");

        jLabel8.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel8.setText("NIK :");

        btnTambah.setText("Tambah");
        btnTambah.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTambahActionPerformed(evt);
            }
        });

        btnHapus.setText("Hapus");
        btnHapus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnHapusActionPerformed(evt);
            }
        });

        btnRefresh.setText("Refresh");
        btnRefresh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRefreshActionPerformed(evt);
            }
        });

        btnEdit.setText("Edit");
        btnEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditActionPerformed(evt);
            }
        });

        tblPasien.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null}
            },
            new String [] {
                "ID", "Nama", "NIK", "Telepon", "Alamat", "Tgl Masuk", "JK", "Diagnosa"
            }
        ));
        tblPasien.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblPasienMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tblPasien);

        btnLogout.setText("Logout");
        btnLogout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLogoutActionPerformed(evt);
            }
        });

        jLabel9.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel9.setText("Cari :");

        txtTelepon.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtTeleponActionPerformed(evt);
            }
        });

        txtAlamat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtAlamatActionPerformed(evt);
            }
        });

        txtNIK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtNIKActionPerformed(evt);
            }
        });

        txtDiagnosa.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtDiagnosaActionPerformed(evt);
            }
        });

        txtCari.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtCariActionPerformed(evt);
            }
        });

        btnBackup.setText("Backup");
        btnBackup.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBackupActionPerformed(evt);
            }
        });

        btnRestore.setText("Restore");
        btnRestore.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRestoreActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout formPanelLayout = new javax.swing.GroupLayout(formPanel);
        formPanel.setLayout(formPanelLayout);
        formPanelLayout.setHorizontalGroup(
            formPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(formPanelLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(formPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(formPanelLayout.createSequentialGroup()
                        .addComponent(jScrollPane1)
                        .addContainerGap())
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, formPanelLayout.createSequentialGroup()
                        .addGroup(formPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 97, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel4)
                            .addComponent(jLabel3)
                            .addComponent(jLabel2))
                        .addGap(18, 18, 18)
                        .addGroup(formPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(txtTelepon, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(dateTanggalMasuk, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 187, Short.MAX_VALUE)
                            .addComponent(txtNama)
                            .addComponent(txtAlamat, javax.swing.GroupLayout.Alignment.TRAILING))
                        .addGroup(formPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(formPanelLayout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 620, Short.MAX_VALUE)
                                .addComponent(btnRestore)
                                .addGap(18, 18, 18)
                                .addComponent(btnBackup)
                                .addGap(18, 18, 18)
                                .addComponent(btnLogout)
                                .addGap(52, 52, 52))
                            .addGroup(formPanelLayout.createSequentialGroup()
                                .addGap(59, 59, 59)
                                .addGroup(formPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel8)
                                    .addComponent(jLabel6)
                                    .addComponent(jLabel7))
                                .addGap(20, 20, 20)
                                .addGroup(formPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(formPanelLayout.createSequentialGroup()
                                        .addComponent(txtNIK)
                                        .addGap(233, 233, 233))
                                    .addGroup(formPanelLayout.createSequentialGroup()
                                        .addComponent(cmbJenisKelamin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(0, 0, Short.MAX_VALUE))
                                    .addComponent(txtDiagnosa))
                                .addContainerGap())))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, formPanelLayout.createSequentialGroup()
                        .addComponent(btnTambah)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnEdit)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnHapus)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnRefresh)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel9)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtCari)
                        .addGap(141, 141, 141))))
        );
        formPanelLayout.setVerticalGroup(
            formPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(formPanelLayout.createSequentialGroup()
                .addGroup(formPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(formPanelLayout.createSequentialGroup()
                        .addGap(16, 16, 16)
                        .addGroup(formPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtNama, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(formPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtTelepon, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(formPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtAlamat, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(formPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(dateTanggalMasuk, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(formPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(formPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnLogout)
                            .addComponent(btnBackup)
                            .addComponent(btnRestore))
                        .addGap(16, 16, 16)
                        .addGroup(formPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtNIK, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(19, 19, 19)
                        .addGroup(formPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(cmbJenisKelamin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(formPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtDiagnosa, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(25, 25, 25)
                .addGroup(formPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(formPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnTambah)
                        .addComponent(btnRefresh)
                        .addComponent(btnEdit)
                        .addComponent(btnHapus))
                    .addGroup(formPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(txtCari, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 202, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout pasienPanelLayout = new javax.swing.GroupLayout(pasienPanel);
        pasienPanel.setLayout(pasienPanelLayout);
        pasienPanelLayout.setHorizontalGroup(
            pasienPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pasienPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(formPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        pasienPanelLayout.setVerticalGroup(
            pasienPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pasienPanelLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(formPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jTabbedPane1.addTab("Data", pasienPanel);

        jLabel10.setBackground(new java.awt.Color(255, 255, 255));
        jLabel10.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel10.setText("Bahasa :");

        lblSectionTitle.setBackground(new java.awt.Color(255, 255, 255));
        lblSectionTitle.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lblSectionTitle.setText("PENGATURAN");

        cmbBahasa.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "English", "Indonesia", " " }));
        cmbBahasa.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbBahasaActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout settingsPanelLayout = new javax.swing.GroupLayout(settingsPanel);
        settingsPanel.setLayout(settingsPanelLayout);
        settingsPanelLayout.setHorizontalGroup(
            settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(settingsPanelLayout.createSequentialGroup()
                .addGap(67, 67, 67)
                .addComponent(jLabel10)
                .addGap(59, 59, 59)
                .addComponent(cmbBahasa, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(949, Short.MAX_VALUE))
            .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, settingsPanelLayout.createSequentialGroup()
                    .addContainerGap(547, Short.MAX_VALUE)
                    .addComponent(lblSectionTitle)
                    .addGap(543, 543, 543)))
        );
        settingsPanelLayout.setVerticalGroup(
            settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(settingsPanelLayout.createSequentialGroup()
                .addGap(124, 124, 124)
                .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(cmbBahasa, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(278, Short.MAX_VALUE))
            .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(settingsPanelLayout.createSequentialGroup()
                    .addGap(40, 40, 40)
                    .addComponent(lblSectionTitle)
                    .addContainerGap(356, Short.MAX_VALUE)))
        );

        jTabbedPane1.addTab("Settings", settingsPanel);

        jLabel1.setBackground(new java.awt.Color(255, 255, 255));
        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("DATA PASIEN");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSeparator1)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jSeparator2))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(543, 543, 543)
                .addComponent(jLabel1)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTabbedPane1)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
/**
     * Update JTable; harus dipanggil di Event Dispatch Thread
     */
    private void tampilkanKeTabel(java.util.List<Pasien> list) {
        model.setRowCount(0);
        for (Pasien p : list) {
            model.addRow(new Object[]{
                p.getId(), p.getNama(), p.getNik(), p.getTelepon(),
                p.getAlamat(), p.getTanggalMasuk(),
                p.getJenisKelamin(), p.getDiagnosa()
            });
        }
        hideIdColumn();
    }


    private void txtCariActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtCariActionPerformed
        String keyword = txtCari.getText().trim();
        if (keyword.isEmpty()) {
            loadDataPasienAsync();
            return;
        }
        model.setRowCount(0);
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM pasien WHERE nama LIKE ? OR nik LIKE ?")) {
            ps.setString(1, "%" + keyword + "%");
            ps.setString(2, "%" + keyword + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Vector<Object> v = new Vector<>();
                v.add(rs.getInt("id"));                 // 0  ← ID (disembunyikan)
                v.add(rs.getString("nama"));            // 1  Nama
                v.add(rs.getString("nik"));             // 2  NIK
                v.add(rs.getString("telepon"));         // 3  Telepon
                v.add(rs.getString("alamat"));          // 4  Alamat
                v.add(rs.getDate("tanggal_masuk"));     // 5  Tgl Masuk
                v.add(rs.getString("jenis_kelamin"));   // 6  JK
                v.add(rs.getString("diagnosa"));        // 7  Diagnosa
                model.addRow(v);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Gagal cari: " + ex.getMessage());
        }
        hideIdColumn();                                 // sembunyikan lagi
    }//GEN-LAST:event_txtCariActionPerformed

    private void onLanguageChanged() {
        String selected = (String) cmbBahasa.getSelectedItem();
        if ("English".equals(selected)) {
            I18n.setLocale(new java.util.Locale("en"));
        } else {
            I18n.setLocale(new java.util.Locale("id"));
        }
        applyI18n();        // refresh semua label
    }

    private void applyI18n() {
        setTitle(I18n.t("app.title"));

        jLabel1.setText(I18n.t("app.title"));
        jLabel2.setText(I18n.t("label.name") + " :");
        jLabel3.setText(I18n.t("label.phone") + " :");
        jLabel4.setText(I18n.t("label.address") + " :");
        jLabel5.setText(I18n.t("label.date") + " :");
        jLabel6.setText(I18n.t("label.gender") + " :");
        jLabel7.setText(I18n.t("label.diagnosis") + " :");
        jLabel9.setText(I18n.t("search"));

        btnTambah.setText(I18n.t("btn.add"));
        btnEdit.setText(I18n.t("btn.edit"));
        btnHapus.setText(I18n.t("btn.delete"));
        btnRefresh.setText(I18n.t("btn.refresh"));
        btnLogout.setText(I18n.t("btn.logout"));
        btnBackup.setText(I18n.t("btn.backup"));
        btnRestore.setText(I18n.t("btn.restore"));

//        cmbJenisKelamin.removeAllItems();
//        cmbJenisKelamin.addItem(I18n.t("gender.male"));
//        cmbJenisKelamin.addItem(I18n.t("gender.female"));
    }


    private void txtDiagnosaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtDiagnosaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtDiagnosaActionPerformed

    private void txtNIKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtNIKActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtNIKActionPerformed

    private void txtAlamatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtAlamatActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtAlamatActionPerformed

    private void txtTeleponActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtTeleponActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtTeleponActionPerformed

    private void btnLogoutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLogoutActionPerformed
        // TODO add your handling code here:
        int c = JOptionPane.showConfirmDialog(this,
                "Yakin ingin logout?", "Konfirmasi",
                JOptionPane.YES_NO_OPTION);
        if (c == JOptionPane.YES_OPTION)
            logout();
    }//GEN-LAST:event_btnLogoutActionPerformed

    private void btnEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditActionPerformed
        // TODO add your handling code here:
        updatePasien();
    }//GEN-LAST:event_btnEditActionPerformed

    private void btnRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshActionPerformed
        clearForm();
        loadDataPasienAsync();        // muat ulang tabel
        hideIdColumn();
    }//GEN-LAST:event_btnRefreshActionPerformed

    private void btnHapusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnHapusActionPerformed
        // TODO add your handling code here:
        hapusPasien();
    }//GEN-LAST:event_btnHapusActionPerformed

    private void btnTambahActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTambahActionPerformed
        // TODO add your handling code here:
        tambahPasien();
    }//GEN-LAST:event_btnTambahActionPerformed

    private void tblPasienMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblPasienMouseClicked
        // TODO add your handling code here:
        // Pindahkan data baris yang dipilih ke form  
        isiFormDariTabel();

        // (Opsional) fokus ke field pertama supaya siap diedit

    }//GEN-LAST:event_tblPasienMouseClicked

    private void btnBackupActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBackupActionPerformed
        // TODO add your handling code here:
        backupDataPasien();
    }//GEN-LAST:event_btnBackupActionPerformed

    private void btnRestoreActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRestoreActionPerformed
        // TODO add your handling code here:\
        restoreDataPasien();
    }//GEN-LAST:event_btnRestoreActionPerformed

    private void cmbBahasaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbBahasaActionPerformed
//        String selected = cmbBahasa.getSelectedItem().toString();
//
//        if (selected.equalsIgnoreCase("Indonesia")) {
//            pengelolapasien.i18n.I18n.setLocale(new java.util.Locale("id"));
//        } else {
//            pengelolapasien.i18n.I18n.setLocale(new java.util.Locale("en"));
//        }
//
//        applyI18n();  // update semua label/tombol
    }//GEN-LAST:event_cmbBahasaActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MainPage.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MainPage.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MainPage.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainPage.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MainPage().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBackup;
    private javax.swing.JButton btnEdit;
    private javax.swing.JButton btnHapus;
    private javax.swing.JButton btnLogout;
    private javax.swing.JButton btnRefresh;
    private javax.swing.JButton btnRestore;
    private javax.swing.JButton btnTambah;
    private javax.swing.JComboBox<String> cmbBahasa;
    private javax.swing.JComboBox<String> cmbJenisKelamin;
    private com.toedter.calendar.JDateChooser dateTanggalMasuk;
    private javax.swing.JPanel formPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JLabel lblSectionTitle;
    private javax.swing.JPanel pasienPanel;
    private javax.swing.JPanel settingsPanel;
    private javax.swing.JTable tblPasien;
    private javax.swing.JTextField txtAlamat;
    private javax.swing.JTextField txtCari;
    private javax.swing.JTextField txtDiagnosa;
    private javax.swing.JTextField txtNIK;
    private javax.swing.JTextField txtNama;
    private javax.swing.JTextField txtTelepon;
    // End of variables declaration//GEN-END:variables
}
