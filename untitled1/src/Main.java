import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.Vector;

public class Main extends JFrame {

    private static Connection connection;
    private static final String DB_URL = "jdbc:mysql://localhost:3306/kutuphane_sistemi";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Asdera4545";

    private JButton btnBaglan, btnKes, btnBilgi, btnEkle, btnGuncelle, btnSil;
    private JTextArea textArea;
    private JTable table;
    private Vector<Vector<Object>> tableData;
    private Vector<String> columnNames;

    public Main() {
        setTitle("Kitap Veritabanı Uygulaması");
        setSize(900, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel panel = new JPanel();
        btnBaglan = new JButton("Bağlan");
        btnKes = new JButton("Bağlantıyı Kes");
        btnBilgi = new JButton("Bilgileri Göster");
        btnEkle = new JButton("Kitap Ekle");
        btnGuncelle = new JButton("Kitap Güncelle");
        btnSil = new JButton("Kitap Sil");

        panel.add(btnBaglan);
        panel.add(btnKes);
        panel.add(btnBilgi);
        panel.add(btnEkle);
        panel.add(btnGuncelle);
        panel.add(btnSil);
        add(panel, BorderLayout.NORTH);

        textArea = new JTextArea();
        textArea.setEditable(false);
        add(new JScrollPane(textArea), BorderLayout.SOUTH);

        tableData = new Vector<>();
        columnNames = new Vector<>();
        table = new JTable(tableData, columnNames);
        add(new JScrollPane(table), BorderLayout.CENTER);

        btnBaglan.addActionListener(e -> baglan());
        btnKes.addActionListener(e -> baglantiyiKes());
        btnBilgi.addActionListener(e -> bilgileriGoster());
        btnEkle.addActionListener(e -> kitapEkle());
        btnGuncelle.addActionListener(e -> kitapGuncelle());
        btnSil.addActionListener(e -> kitapSil());
    }

    private void baglan() {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                try {
                    if (connection == null || connection.isClosed()) {
                        Class.forName("com.mysql.cj.jdbc.Driver");
                        connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                        appendText("✅ MySQL veritabanına bağlanıldı!");
                    } else {
                        appendText("ℹ️ Zaten bağlantı açık.");
                    }
                } catch (Exception ex) {
                    appendSqlException(ex);
                }
                return null;
            }
        }.execute();
    }

    private void baglantiyiKes() {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                try {
                    if (connection != null && !connection.isClosed()) {
                        connection.close();
                        appendText("🔌 Bağlantı kapatıldı.");
                    } else {
                        appendText("⚠️ Bağlantı zaten kapalı.");
                    }
                } catch (Exception ex) {
                    appendSqlException(ex);
                }
                return null;
            }
        }.execute();
    }

    private void bilgileriGoster() {
        final String tableName = "kitap";
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                if (connection == null) {
                    appendText("⚠️ Önce bağlanın!");
                    return null;
                }
                try {
                    Statement st = connection.createStatement();
                    ResultSet rs = st.executeQuery("SELECT * FROM " + tableName);
                    ResultSetMetaData rsmd = rs.getMetaData();
                    int colCount = rsmd.getColumnCount();

                    columnNames.clear();
                    for (int i = 1; i <= colCount; i++) {
                        columnNames.add(rsmd.getColumnName(i));
                    }

                    tableData.clear();
                    while (rs.next()) {
                        Vector<Object> row = new Vector<>();
                        for (int i = 1; i <= colCount; i++) {
                            row.add(rs.getObject(i));
                        }
                        tableData.add(row);
                    }

                    SwingUtilities.invokeLater(() -> {
                        table.setModel(new javax.swing.table.DefaultTableModel(tableData, columnNames));
                        appendText("📚 Kitap tablosu güncellendi.");
                    });

                } catch (SQLException ex) {
                    appendSqlException(ex);
                }
                return null;
            }
        }.execute();
    }

    private void kitapEkle() {
        if (connection == null) {
            appendText("⚠️ Önce bağlanın!");
            return;
        }

        try {
            int kategoriID = Integer.parseInt(JOptionPane.showInputDialog(this, "Kategori ID:"));
            String ad = JOptionPane.showInputDialog(this, "Kitap Adı:");
            int sayfasayisi = Integer.parseInt(JOptionPane.showInputDialog(this, "Sayfa Sayısı:"));
            String yayinTarihi = JOptionPane.showInputDialog(this, "Yayın Tarihi (YYYY-MM-DD):");
            int adet = Integer.parseInt(JOptionPane.showInputDialog(this, "Adet:"));

            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() {
                    try {
                        String sql = "INSERT INTO kitap (kategoriID, ad, sayfasayisi, yayinTarihi, adet) VALUES (?, ?, ?, ?, ?)";
                        PreparedStatement ps = connection.prepareStatement(sql);
                        ps.setInt(1, kategoriID);
                        ps.setString(2, ad);
                        ps.setInt(3, sayfasayisi);
                        ps.setDate(4, java.sql.Date.valueOf(yayinTarihi));
                        ps.setInt(5, adet);
                        int rows = ps.executeUpdate();
                        appendText("✅ " + rows + " kitap eklendi.");
                        bilgileriGoster();
                    } catch (SQLException ex) {
                        appendSqlException(ex);
                    }
                    return null;
                }
            }.execute();

        } catch (Exception ex) {
            appendText("⚠️ Geçersiz veri girdiniz!");
        }
    }

    private void kitapGuncelle() {
        if (connection == null) {
            appendText("⚠️ Önce bağlanın!");
            return;
        }
        int row = table.getSelectedRow();
        if (row == -1) {
            appendText("⚠️ Lütfen güncellenecek satırı seçin.");
            return;
        }

        try {
            Object id = tableData.get(row).get(0);
            int kategoriID = Integer.parseInt(JOptionPane.showInputDialog(this, "Kategori ID:", tableData.get(row).get(1)));
            String ad = JOptionPane.showInputDialog(this, "Kitap Adı:", tableData.get(row).get(2));
            int sayfasayisi = Integer.parseInt(JOptionPane.showInputDialog(this, "Sayfa Sayısı:", tableData.get(row).get(3)));
            String yayinTarihi = JOptionPane.showInputDialog(this, "Yayın Tarihi (YYYY-MM-DD):", tableData.get(row).get(4));
            int adet = Integer.parseInt(JOptionPane.showInputDialog(this, "Adet:", tableData.get(row).get(5)));

            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() {
                    try {
                        String sql = "UPDATE kitap SET kategoriID=?, ad=?, sayfasayisi=?, yayinTarihi=?, adet=? WHERE ID=?";
                        PreparedStatement ps = connection.prepareStatement(sql);
                        ps.setInt(1, kategoriID);
                        ps.setString(2, ad);
                        ps.setInt(3, sayfasayisi);
                        ps.setDate(4, java.sql.Date.valueOf(yayinTarihi));
                        ps.setInt(5, adet);
                        ps.setObject(6, id);
                        int rows = ps.executeUpdate();
                        appendText("✅ " + rows + " kitap güncellendi.");
                        bilgileriGoster();
                    } catch (SQLException ex) {
                        appendSqlException(ex);
                    }
                    return null;
                }
            }.execute();

        } catch (Exception ex) {
            appendText("⚠️ Geçersiz veri girdiniz!");
        }
    }

    private void kitapSil() {
        if (connection == null) {
            appendText("⚠️ Önce bağlanın!");
            return;
        }
        int row = table.getSelectedRow();
        if (row == -1) {
            appendText("⚠️ Lütfen silinecek satırı seçin.");
            return;
        }
        Object id = tableData.get(row).get(0);

        int onay = JOptionPane.showConfirmDialog(this, "Seçilen kitabı silmek istediğine emin misin?", "Onay", JOptionPane.YES_NO_OPTION);
        if (onay != JOptionPane.YES_OPTION) return;

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                try {
                    String sql = "DELETE FROM kitap WHERE ID=?";
                    PreparedStatement ps = connection.prepareStatement(sql);
                    ps.setObject(1, id);
                    int rows = ps.executeUpdate();
                    appendText("✅ " + rows + " kitap silindi.");
                    bilgileriGoster();
                } catch (SQLException ex) {
                    appendSqlException(ex);
                }
                return null;
            }
        }.execute();
    }

    private void appendText(String s) {
        SwingUtilities.invokeLater(() -> textArea.append(s + "\n"));
    }

    private void appendSqlException(Exception ex) {
        SwingUtilities.invokeLater(() -> {
            textArea.append("❌ Hata: " + ex.getClass().getSimpleName() + " - " + ex.getMessage() + "\n");
            if (ex instanceof SQLException) {
                SQLException sqlEx = (SQLException) ex;
                textArea.append("   SQLState: " + sqlEx.getSQLState() + "  ErrorCode: " + sqlEx.getErrorCode() + "\n");
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().setVisible(true));
    }
}
