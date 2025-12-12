package com.example.KutupahaneOtomasyonu.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

// @Entity: Spring ve Hibernate'e "Bu sınıf bir veritabanı tablosudur" diyoruz.
// @Table(name = "fines"): Veritabanındaki tablonun adı "fines" (cezalar) olacak.
@Entity
@Table(name = "fines")
// @Data: Lombok. Getter, Setter, toString gibi metodları bizim yerimize yazar.
@Data
@NoArgsConstructor // Parametresiz boş constructor (Hibernate'in çalışması için şart).
@AllArgsConstructor // Tüm alanları içeren dolu constructor.
public class Fine {

    // @Id: Bu alan tablonun kimlik numarasıdır (Primary Key).
    // @GeneratedValue: ID'yi biz vermiyoruz, veritabanı (1, 2, 3...) otomatik üretiyor.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fine_id")
    private Integer fineId;

    // --- İLİŞKİ: CEZA -> ÖDÜNÇ İŞLEMİ ---
    // @OneToOne: Bire-Bir İlişki.
    // Mantık şudur: "Bir ceza makbuzu, sadece TEK BİR ödünç alma işlemine aittir."
    // Yani bu ceza hangi kitaba, hangi tarihteki alıma ait? Bunu bilmek zorundayız.
    @OneToOne
    @JoinColumn(name = "borrow_id", nullable = false) // Bu alan boş olamaz, her cezanın bir kaynağı olmalı.
    private Borrowing borrowing;

    // Cezanın parasal miktarı (Örn: 15.0 TL).
    // Double kullanıyoruz çünkü küsuratlı olabilir.
    @Column(name = "fine_amount")
    private Double fineAmount;

    // Cezanın kesildiği tarih ve saat.
    @Column(name = "fine_date")
    private LocalDateTime fineDate;

    // Ceza ödendi mi? (true = Ödendi, false = Borç duruyor).
    private boolean paid;
}