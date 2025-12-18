package com.example.KutupahaneOtomasyonu.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "odunc_islemleri")
public class OduncIslemi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer oduncId;

    @ManyToOne
    @JoinColumn(name = "uye_id")
    private Uye uye;

    @ManyToOne
    @JoinColumn(name = "kitap_id")
    private Kitap kitap;

    private LocalDateTime alisTarihi;
    private LocalDateTime sonTeslimTarihi;
    private LocalDateTime iadeTarihi;

    // Veritabaninda VARCHAR veya ENUM olarak tutulabilir
    @Enumerated(EnumType.STRING)
    private OduncDurumu durum;

    // ISTE HATA VEREN VE SONRADAN EKLEDIGIMIZ ALANLAR:
    private boolean bildirimGonderildi = false;
    private boolean cezaOdendiMi = false;

    // --- GETTER VE SETTERLAR ---
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

    private double cezaMiktari = 0.0;
    private String odemeDurumu = "YOK"; // YOK, ODENMEDI, ONAY_BEKLIYOR, ODENDI

    public double getCezaMiktari() { return cezaMiktari; }
    public void setCezaMiktari(double cezaMiktari) { this.cezaMiktari = cezaMiktari; }

    public String getOdemeDurumu() { return odemeDurumu; }
    public void setOdemeDurumu(String odemeDurumu) { this.odemeDurumu = odemeDurumu; }
}