package com.example.KutupahaneOtomasyonu.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

/*
 * BU SINIF NE İŞE YARAR?
 * Kütüphane sistemindeki yazarları temsil eder ("Yazarlar" tablosu).
 * Yazarın kişisel bilgilerini tutar ve Kitaplar tablosu ile ilişki kurar.
 */
@Entity
@Table(name = "Yazarlar")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Yazar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "yazar_id")
    private Integer yazarId;

    private String ad;

    private String soyad;

    @Column(name = "dogum_yili")
    private Integer dogumYili;

    /*
     * KİTAPLAR İLİŞKİSİ (One-to-Many)
     * Mantık: "Bir yazarın birden fazla kitabı olabilir."
     *
     * CascadeType.ALL: Eğer sistemden bir yazar silinirse, yazdığı tüm kitaplar da
     * veri tutarlılığı için otomatik olarak silinir.
     *
     * FetchType.LAZY: Performans ayarıdır. Yazar bilgisi çekildiğinde kitap listesi
     * belleğe hemen yüklenmez, sadece talep edildiğinde (getKitaplar) getirilir.
     *
     * @JsonIgnore: JSON dönüşümü sırasında sonsuz döngüye (StackOverflow) girmeyi engeller.
     */
    @OneToMany(mappedBy = "yazar", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Kitap> kitaplar;
}