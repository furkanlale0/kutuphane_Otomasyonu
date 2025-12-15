package com.example.KutupahaneOtomasyonu.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

// @Entity: Spring'e "Bu sinifi al, veritabanindaki Cezalar tablosuyla eslestir" diyoruz.
@Entity
// @Table: Veritabanindaki tablonun adi "Cezalar" olsun.
@Table(name = "Cezalar")
// @Data: Lombok. Getter, Setter, toString gibi metodlari bizim yerimize yazar, kod kalabaligi olmaz.
@Data
@NoArgsConstructor // Bos constructor (Hibernate'in calismasi icin sart).
@AllArgsConstructor // Dolu constructor.
public class Ceza { // Fine -> Ceza

    // @Id: Bu alan tablonun kimlik numarasidir (Primary Key).
    // @GeneratedValue: ID'yi biz vermiyoruz, veritabani (1, 2, 3...) otomatik uretiyor.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ceza_id")
    private Integer cezaId;

    // --- ILISKI: CEZA -> ODUNC ISLEMI ---
    // @OneToOne: Bire-Bir Iliski.
    // Mantik sudur: "Bir ceza makbuzu, sadece TEK BIR odunc alma islemine aittir."
    // Yani bu ceza hangi kitaba, hangi tarihteki gecikmeye ait? Bunu 'oduncIslemi' sayesinde biliriz.
    @OneToOne
    @JoinColumn(name = "odunc_id", nullable = false) // Bu alan bos olamaz, her cezanin bir kaynagi olmali.
    private OduncIslemi oduncIslemi;

    // Cezanin parasal miktari (Orn: 15.50 TL).
    // Double kullaniyoruz cunku kusuratli olabilir.
    @Column(name = "miktar")
    private Double miktar; // fineAmount -> miktar

    // Cezanin kesildigi tarih ve saat.
    @Column(name = "olusturulma_tarihi")
    private LocalDateTime olusturulmaTarihi; // fineDate -> olusturulmaTarihi

    // Ceza odendi mi? (true = Odendi/Borc Yok, false = Borc duruyor).
    @Column(name = "odendi_mi")
    private boolean odendiMi; // paid -> odendiMi
}