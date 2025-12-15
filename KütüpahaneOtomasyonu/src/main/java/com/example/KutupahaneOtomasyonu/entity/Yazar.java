package com.example.KutupahaneOtomasyonu.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

// @Entity: Spring Boot'a "Bu sinif siradan bir Java sinifi degil, veritabanindaki bir TABLODUR" diyoruz.
// Hibernate bunu gorunce veritabanindaki tabloyla eslestirir.
@Entity
// @Table: Veritabaninda bu sinifin karsiligi olan tablonun adi "Yazarlar" olsun diyoruz.
@Table(name = "Yazarlar")
// @Data: Lombok kutuphanesi sayesinde Getter, Setter, toString gibi metodlari
// tek tek elle yazmak zorunda kalmiyoruz, o bizim yerimize arka planda yaziyor.
@Data
@NoArgsConstructor // Bos constructor (Hibernate'in veri cekerken nesne olusturabilmesi icin sarttir).
@AllArgsConstructor // Dolu constructor (Testlerde veya elle nesne olustururken isimize yarar).
public class Yazar { // Author -> Yazar

    // @Id: Bu alanin tablonun benzersiz kimlik numarasi (Primary Key) oldugunu belirtir.
    // @GeneratedValue: ID numarasini biz elle vermiyoruz (1, 2, 3 diye biz saymiyoruz).
    // Veritabani her yeni kayitta bu sayiyi otomatik olarak arttirir.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "yazar_id")
    private Integer yazarId;

    // Yazarin adi (Tablodaki 'ad' sutunu)
    private String ad;

    // Yazarin soyadi (Tablodaki 'soyad' sutunu)
    private String soyad;

    // Yazarinn dogum yili (Tablodaki 'dogum_yili' sutunu)
    @Column(name = "dogum_yili")
    private Integer dogumYili;

    // --- KRITIK NOKTA: ILISKI YONETIMI (1 Yazar -> N Kitap) ---

    // @OneToMany: "Bir yazarin, birden fazla kitabi olabilir" anlamina gelir.

    // mappedBy = "yazar": Bu iliskinin yoneticisi biz degiliz, Kitap (Kitaplar) sinifindaki 'yazar' degiskenidir diyoruz.
    // Veritabaninda foreign_key (yabanci anahtar) Kitaplar tablosunda durur.

    // cascade = CascadeType.ALL: BU COK GUCLU BIR AYARDIR!
    // Eger bu yazari veritabanindan silersek, yazdigi BUTUN KITAPLAR da otomatik olarak silinir.
    // (Yazarsiz kitap olmaz mantigiyla veritabanini temiz tutar).

    // fetch = FetchType.LAZY: Performans ayaridir. Veritabanindan yazari cektigimizde,
    // hemen gidip 100 tane kitabini da cekme. Biz ne zaman "yazar.getKitaplar()" dersek o zaman git getir.
    // Bu sayede sistem bosuna yorulmaz.
    @OneToMany(mappedBy = "yazar", cascade = CascadeType.ALL, fetch = FetchType.LAZY)

    // @JsonIgnore: SONSUZ DONGUYU ENGELLER.
    // Frontend'e (React/Postman) veri gonderirken; Yazari yazdirinca icindeki kitaplari yazdirir,
    // kitaplarin icinde tekrar yazar var onu yazdirir... Bu boyle sonsuza gider ve program coker.
    // Bu etiketle "Yazari gonderirken kitap listesini gormezden gel" diyoruz.
    @JsonIgnore
    private List<Kitap> kitaplar;
}