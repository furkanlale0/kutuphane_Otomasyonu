package com.example.KutupahaneOtomasyonu.service;

import com.example.KutupahaneOtomasyonu.entity.Yonetici;
import com.example.KutupahaneOtomasyonu.repository.YoneticiRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

// @Service: Spring'e "Bu sinif is kurallarini yonetir, Controller ile Repository arasindaki koprudur" diyoruz.
@Service
public class YoneticiServisi { // AdminService -> YoneticiServisi

    private final YoneticiRepository yoneticiRepository;

    @Autowired
    public YoneticiServisi(YoneticiRepository yoneticiRepository) {
        this.yoneticiRepository = yoneticiRepository;
    }

    // Veritabanindan yoneticiyi getiren metot (Repository'i sarmalar).
    // DIKKAT: Repository'deki metodumuzun adi artik "findByKullaniciAdi" oldugu icin burayi da guncelledik.
    public Optional<Yonetici> kullaniciAdiIleGetir(String kullaniciAdi) {
        return yoneticiRepository.findByKullaniciAdi(kullaniciAdi);
    }
}