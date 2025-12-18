package com.example.KutupahaneOtomasyonu.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/*
 * BU SINIF NE İŞE YARAR?
 * Kütüphane sisteminin "Hareket Dökümü" (Transaction) tablosudur.
 * Bir üyenin bir kitabı ödünç almasından iade etmesine kadar geçen süreci kayıt altına alır.
 * Ceza takibi ve ödeme durumları da (performans artışı için) bu sınıf üzerinden yönetilir.
 */
@Entity
@Table(name = "odunc_islemleri")
public class OduncIslemi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer oduncId;

    /*
     * İLİŞKİLER
     * Bir ödünç işlemi mutlaka BİR ÜYE ve BİR KİTAP ile ilişkili olmalıdır.
     */
    @ManyToOne
    @JoinColumn(name = "uye_id")
    private Uye uye;

    @ManyToOne
    @JoinColumn(name = "kitap_id")
    private Kitap kitap;

    // İşlem Tarihleri
    private LocalDateTime alisTarihi;
    private LocalDateTime sonTeslimTarihi;
    private LocalDateTime iadeTarihi; // Kitap geri geldiğinde dolar, yoksa null'dır.

    /*
     * İŞLEM DURUMU
     * İşlemin şu anki statüsünü tutar (Örn: DEVAM_EDIYOR, TESLIM_EDILDI, GECIKMEDE).
     * EnumType.STRING: Veritabanında sayı (0,1) yerine okunabilir metin olarak tutulur.
     */
    @Enumerated(EnumType.STRING)
    private OduncDurumu durum;

    /*
     * OPERASYONEL KONTROL ALANLARI
     * Ceza ve bildirim süreçlerini yönetmek için kullanılan bayraklar (flags).
     */
    private boolean bildirimGonderildi = false; // Üyeye "Borcun var" maili/bildirimi gitti mi?
    private boolean cezaOdendiMi = false;       // Eski sistemden kalan kontrol alanı.

    /*
     * FİNANSAL ALANLAR
     * Ceza takibini kolaylaştırmak için eklenmiştir.
     */
    private double cezaMiktari = 0.0;

    // Ödeme Durumu: "YOK", "ODENMEDI", "ONAY_BEKLIYOR", "ODENDI"
    private String odemeDurumu = "YOK";

    // --- GETTER VE SETTER METODLARI ---

    public Integer getOduncId() { return oduncId; }
    public void setOduncId(Integer oduncId) { this.oduncId = oduncId; }

    public Uye getUye() { return uye; }
    public void setUye(Uye uye) { this.uye = uye; }

    public Kitap getKitap() { return kitap; }
    public void setKitap(Kitap kitap) { this.kitap = kitap; }

    public LocalDateTime getAlisTarihi() { return alisTarihi; }
    public void setAlisTarihi(LocalDateTime alisTarihi) { this.alisTarihi = alisTarihi; }

    public LocalDateTime getSonTeslimTarihi() { return sonTeslimTarihi; }
    public void setSonTeslimTarihi(LocalDateTime sonTeslimTarihi) { this.sonTeslimTarihi = sonTeslimTarihi; }

    public LocalDateTime getIadeTarihi() { return iadeTarihi; }
    public void setIadeTarihi(LocalDateTime iadeTarihi) { this.iadeTarihi = iadeTarihi; }

    public OduncDurumu getDurum() { return durum; }
    public void setDurum(OduncDurumu durum) { this.durum = durum; }

    public boolean isBildirimGonderildi() { return bildirimGonderildi; }
    public void setBildirimGonderildi(boolean bildirimGonderildi) { this.bildirimGonderildi = bildirimGonderildi; }

    public boolean isCezaOdendiMi() { return cezaOdendiMi; }
    public void setCezaOdendiMi(boolean cezaOdendiMi) { this.cezaOdendiMi = cezaOdendiMi; }

    public double getCezaMiktari() { return cezaMiktari; }
    public void setCezaMiktari(double cezaMiktari) { this.cezaMiktari = cezaMiktari; }

    public String getOdemeDurumu() { return odemeDurumu; }
    public void setOdemeDurumu(String odemeDurumu) { this.odemeDurumu = odemeDurumu; }
}