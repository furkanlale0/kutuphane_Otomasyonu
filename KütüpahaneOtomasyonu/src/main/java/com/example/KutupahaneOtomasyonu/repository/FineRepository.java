package com.example.KutupahaneOtomasyonu.repository;

import com.example.KutupahaneOtomasyonu.entity.Fine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FineRepository extends JpaRepository<Fine, Integer> {
    // İÇİ BOŞ - Hata yok.
}