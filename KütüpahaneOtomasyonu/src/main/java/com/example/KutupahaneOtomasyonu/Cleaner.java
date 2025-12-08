package com.example.KutupahaneOtomasyonu;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class Cleaner {
    public static void main(String[] args) {
        // Senin veritabanı bilgilerin
        String url = "jdbc:mysql://localhost:3306/library_automation?serverTimezone=UTC";
        String user = "root";
        String password = "Asdera4545"; // Senin şifren

        try (Connection con = DriverManager.getConnection(url, user, password);
             Statement stmt = con.createStatement()) {

            System.out.println("🧹 Temizlik başlıyor...");

            // 1. Kilitleri Aç
            stmt.execute("SET FOREIGN_KEY_CHECKS = 0");

            // 2. Tabloları Sil
            stmt.execute("DROP TABLE IF EXISTS fines");
            stmt.execute("DROP TABLE IF EXISTS borrowings");
            stmt.execute("DROP TABLE IF EXISTS books");
            stmt.execute("DROP TABLE IF EXISTS authors");
            stmt.execute("DROP TABLE IF EXISTS members");
            stmt.execute("DROP TABLE IF EXISTS admins");

            // 3. Kilitleri Geri Tak
            stmt.execute("SET FOREIGN_KEY_CHECKS = 1");

            System.out.println("✅ Veritabanı TERTEMİZ oldu! Şimdi projeyi çalıştırabilirsin.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}