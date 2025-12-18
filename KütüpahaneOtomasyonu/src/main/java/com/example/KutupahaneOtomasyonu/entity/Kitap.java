package com.example.KutupahaneOtomasyonu.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/*
 * BU SINIF NE İŞE YARAR?
 * Kütüphanedeki "Kitap" nesnesini temsil eder.
 * Veritabanındaki "Kitaplar" tablosunun Java karşılığıdır.
 * Kitabın adı, ISBN numarası, stoğu ve ilişkili olduğu yazar gibi verileri tutar.
 */
@Entity
@Table(name = "Kitaplar")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Kitap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "kitap_id")
    private Integer kitapId;

    @Column(name = "kitap_adi", nullable = false)
    private String kitapAdi;

    private String isbn;

    @Column(name = "yayin_yili")
    private Integer yayinYili;

    @Column(name = "stok_sayisi")
    private Integer stokSayisi;

    /*
     * Kitap Özeti
     * Standart metin alanından (255 karakter) daha uzun olabileceği için
     * veritabanında "TEXT" veri tipi olarak tutulur.
     */
    @Column(columnDefinition = "TEXT")
    private String ozet;

    /*
     * YAZAR İLİŞKİSİ (Many-to-One)
     * Mantık: "Bir yazarın birden çok kitabı olabilir."
     * FetchType.EAGER: Kitap verisi çekildiğinde, yazar bilgisi de otomatik olarak getirilir.
     * (Çünkü kitap listesinde yazar adını her zaman görmek isteriz.)
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "yazar_id", nullable = false)
    private Yazar yazar;

    /*
     * EKLEYEN YÖNETİCİ İLİŞKİSİ (Many-to-One)
     * Mantık: "Bir yönetici sisteme çok sayıda kitap ekleyebilir."
     * FetchType.LAZY: Performans için, özel olarak istenmediği sürece yönetici verisi çekilmez.
     *
     * @JsonIgnoreProperties:
     * 1. Sonsuz döngüyü (Kitap -> Yönetici -> Kitap...) önlemek.
     * 2. Yöneticinin şifre gibi hassas bilgilerini API yanıtında gizlemek.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ekleyen_yonetici_id")
    @JsonIgnoreProperties({"ekledigiKitaplar", "sifre", "hibernateLazyInitializer"})
    private Yonetici ekleyenYonetici;
}