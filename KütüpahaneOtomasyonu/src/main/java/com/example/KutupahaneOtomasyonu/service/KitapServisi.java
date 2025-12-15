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

@Service
public class KitapServisi { // BookService -> KitapServisi

    private final KitapRepository kitapRepository;
    private final YazarRepository yazarRepository;
    private final OduncRepository oduncRepository;

    @Autowired
    public KitapServisi(KitapRepository kitapRepository, YazarRepository yazarRepository, OduncRepository oduncRepository) {
        this.kitapRepository = kitapRepository;
        this.yazarRepository = yazarRepository;
        this.oduncRepository = oduncRepository;
    }

    public List<Kitap> tumunuGetir() { return kitapRepository.findAll(); }

    public Optional<Kitap> idIleGetir(Integer id) { return kitapRepository.findById(id); }

    // @Transactional: "Ya hep ya hiç" kuralıdır.
    // Eğer metodun yarısında bir hata olursa, yapılan tüm veritabanı işlemlerini geri alır (Rollback).
    // Kitap kaydedilirken yazar işleminde hata çıkarsa, yarım yamalak kayıt oluşmasını önler.
    @Transactional
    public Kitap kaydet(Kitap kitap) {
        // --- STOK KONTROLU ---
        if (kitap.getStokSayisi() == null || kitap.getStokSayisi() < 1) {
            throw new IllegalArgumentException("Stok adedi en az 1 olmalidir!");
        }

        // --- YAZAR KONTROLU (AKILLI EKLEME) ---
        // Kitabi eklerken gelen Yazar nesnesini aliyoruz.
        Yazar gelenYazar = kitap.getYazar();

        if (gelenYazar != null && gelenYazar.getAd() != null) {
            String ad = gelenYazar.getAd().trim();
            String soyad = gelenYazar.getSoyad().trim();

            // Veritabanina soruyoruz: "Boyle bir yazar zaten var mi?"
            Optional<Yazar> mevcutYazar = yazarRepository.findByAdAndSoyad(ad, soyad);

            if (mevcutYazar.isPresent()) {
                // VARSA: Yeni yazar yaratma, var olani kitaba bagla.
                // Boylece "Ahmet Umit" isminden 50 tane kayit olusmasini engelleriz.
                kitap.setYazar(mevcutYazar.get());
            } else {
                // YOKSA: Yeni yazari once kaydet, sonra kitaba bagla.
                gelenYazar.setAd(ad);
                gelenYazar.setSoyad(soyad);
                Yazar kaydedilenYazar = yazarRepository.save(gelenYazar);
                kitap.setYazar(kaydedilenYazar);
            }
        } else {
            throw new RuntimeException("Yazar adi bos olamaz!");
        }

        // Her sey tamamsa kitabi kaydet.
        return kitapRepository.save(kitap);
    }

    @Transactional
    public void sil(Integer id) {
        // --- GUVENLI SILME KONTROLU ---
        // Kitabi silmeden once soruyoruz: "Bu kitap su an bir uyede mi?"
        // (Repository'de yazdigimiz exists... metodu burada devreye giriyor)
        boolean kitapBirindeMi = oduncRepository.existsByKitap_KitapIdAndIadeTarihiIsNull(id);

        if (kitapBirindeMi) {
            // Eger birindeyse SILME, hata firlat. Veri butunlugu icin sarttir.
            throw new IllegalStateException("Bu kitap su an bir uyede! Teslim almadan silemezsiniz.");
        }

        // --- GECMISI TEMIZLEME ---
        // Kitap kimse de degil ama gecmiste 50 kere odunc alinmis olabilir.
        // Kitabi silince bu eski kayitlar "bosa dusmesin" diye onlari da siliyoruz.
        List<OduncIslemi> gecmis = oduncRepository.findByKitap_KitapId(id);
        if (!gecmis.isEmpty()) {
            oduncRepository.deleteAll(gecmis);
        }

        // Ve mutlu son: Kitabi siliyoruz.
        kitapRepository.deleteById(id);
    }

    // Arama kutusu icin metot
    public List<Kitap> ismeGoreAra(String kitapAdi) {
        return kitapRepository.findByKitapAdiContainingIgnoreCase(kitapAdi);
    }
}