package com.example.KutupahaneOtomasyonu.service;

import com.example.KutupahaneOtomasyonu.entity.Yazar;
import com.example.KutupahaneOtomasyonu.entity.Kitap;
import com.example.KutupahaneOtomasyonu.entity.OduncIslemi;
import com.example.KutupahaneOtomasyonu.repository.YazarRepository;
import com.example.KutupahaneOtomasyonu.repository.KitapRepository;
import com.example.KutupahaneOtomasyonu.repository.OduncRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/*
 * BU SINIF NE İŞE YARAR?
 * Kitap yönetimi için İş Mantığı (Business Logic) katmanıdır.
 * Kitap ekleme, silme, güncelleme ve arama işlemlerini yönetir.
 * Veritabanı tutarlılığını sağlamak için gerekli kontrolleri (Validasyon) yapar.
 */
@Service
public class KitapServisi {

    private final KitapRepository kitapRepository;
    private final YazarRepository yazarRepository;
    private final OduncRepository oduncRepository;

    @Autowired
    public KitapServisi(KitapRepository kitapRepository, YazarRepository yazarRepository, OduncRepository oduncRepository) {
        this.kitapRepository = kitapRepository;
        this.yazarRepository = yazarRepository;
        this.oduncRepository = oduncRepository;
    }

    public List<Kitap> tumunuGetir() {
        return kitapRepository.findAll();
    }

    public Optional<Kitap> idIleGetir(Integer id) {
        return kitapRepository.findById(id);
    }

    /*
     * KİTAP KAYDETME VE GÜNCELLEME
     * @Transactional: İşlem bütünlüğünü (Atomicity) garanti eder.
     * Metot içinde bir hata oluşursa yapılan tüm veritabanı değişikliklerini geri alır.
     *
     * ÖZELLİK: Akıllı Yazar Yönetimi
     * Kitap eklenirken, girilen yazarın sistemde kayıtlı olup olmadığını kontrol eder.
     * Eğer yazar varsa yeni kayıt oluşturmaz, mevcut yazarı kitaba bağlar (Mükerrer kaydı önler).
     */
    @Transactional
    public Kitap kaydet(Kitap kitap) {
        // Validasyon: Stok sayısı kontrolü
        if (kitap.getStokSayisi() == null || kitap.getStokSayisi() < 1) {
            throw new IllegalArgumentException("Stok adedi en az 1 olmalıdır!");
        }

        Yazar gelenYazar = kitap.getYazar();

        if (gelenYazar != null && gelenYazar.getAd() != null) {
            String ad = gelenYazar.getAd().trim();
            String soyad = gelenYazar.getSoyad().trim();

            // Mükerrer Yazar Kontrolü
            Optional<Yazar> mevcutYazar = yazarRepository.findByAdAndSoyad(ad, soyad);

            if (mevcutYazar.isPresent()) {
                // Yazar zaten varsa, mevcut ID'yi kullan.
                kitap.setYazar(mevcutYazar.get());
            } else {
                // Yazar yoksa, önce yazarı kaydet sonra kitaba bağla.
                gelenYazar.setAd(ad);
                gelenYazar.setSoyad(soyad);
                Yazar kaydedilenYazar = yazarRepository.save(gelenYazar);
                kitap.setYazar(kaydedilenYazar);
            }
        } else {
            throw new RuntimeException("Yazar adı boş olamaz!");
        }

        return kitapRepository.save(kitap);
    }

    /*
     * KİTAP SİLME İŞLEMİ
     * İlişkisel bütünlüğü korumak için iki aşamalı güvenlik kontrolü yapar.
     * 1. Eğer kitap şu an ödünç verilmişse silme işlemini engeller.
     * 2. Eğer kitap geçmişte işlem görmüşse, Foreign Key hatası almamak için
     * önce geçmiş kayıtları temizler, sonra kitabı siler.
     */
    @Transactional
    public void sil(Integer id) {
        // 1. Güvenlik Kontrolü: Kitap şu an kullanımda mı?
        boolean kitapBirindeMi = oduncRepository.existsByKitap_KitapIdAndIadeTarihiIsNull(id);

        if (kitapBirindeMi) {
            throw new IllegalStateException("Bu kitap şu an bir üyede! Teslim almadan silemezsiniz.");
        }

        // 2. Geçmiş Temizliği: İlişkili eski kayıtların silinmesi
        List<OduncIslemi> gecmis = oduncRepository.findByKitap_KitapId(id);
        if (!gecmis.isEmpty()) {
            oduncRepository.deleteAll(gecmis);
        }

        // 3. Kitabın Silinmesi
        kitapRepository.deleteById(id);
    }

    // İsme göre dinamik arama
    public List<Kitap> ismeGoreAra(String kitapAdi) {
        return kitapRepository.findByKitapAdiContainingIgnoreCase(kitapAdi);
    }
}