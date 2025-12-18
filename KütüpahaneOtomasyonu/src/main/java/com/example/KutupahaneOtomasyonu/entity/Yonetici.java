package com.example.KutupahaneOtomasyonu.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

/*
 * BU SINIF NE İŞE YARAR?
 * Kütüphane yönetimini sağlayan idari personeli temsil eder ("Yoneticiler" tablosu).
 * Sistemdeki yetki seviyeleri (Admin, Personel vb.) bu sınıf üzerinden yönetilir.
 * Kitap ekleme, üye silme gibi kritik işlemleri yapacak kişiler burada tutulur.
 */
@Entity
@Table(name = "Yoneticiler")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Yonetici {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "yonetici_id")
    private Integer yoneticiId;

    /*
     * GİRİŞ BİLGİLERİ
     * Kullanıcı adı benzersiz (unique) olmalıdır.
     * Şifreler veritabanında asla açık metin olarak değil, şifrelenmiş (Hash) halde tutulur.
     */
    @Column(name = "kullanici_adi", nullable = false, unique = true)
    private String kullaniciAdi;

    @Column(nullable = false)
    private String sifre;

    private String ad;

    private String soyad;

    private String email;

    /*
     * ROL YÖNETİMİ (YETKİLENDİRME)
     * Yöneticinin sistemdeki yetki seviyesini belirler.
     * EnumType.STRING: Veritabanında okunabilirliği artırmak için sayı yerine
     * metin olarak ("ADMIN", "PERSONEL") kaydedilir.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Rol rol;

    @Column(name = "olusturulma_tarihi")
    private LocalDateTime olusturulmaTarihi;

    /*
     * EKLEDİĞİ KİTAPLAR İLİŞKİSİ (One-to-Many)
     * Bir yönetici sisteme birden fazla kitap ekleyebilir.
     *
     * FetchType.LAZY: Performans optimizasyonudur. Yönetici verisi çekildiğinde,
     * eklediği binlerce kitap listesi belleğe yüklenmez.
     *
     * @JsonIgnore: API yanıtı oluşturulurken sonsuz döngüyü (Yönetici -> Kitap -> Yönetici...) engeller.
     */
    @OneToMany(mappedBy = "ekleyenYonetici", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Kitap> ekledigiKitaplar;
}