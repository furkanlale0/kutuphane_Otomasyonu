package com.example.KutupahaneOtomasyonu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling // <-- BU SATIRI EKLEMELİSİN. "Zamanlayıcıları Devreye Sok" demektir.
public class KutupahaneOtomasyonuApplication {
    public static void main(String[] args) {
        SpringApplication.run(KutupahaneOtomasyonuApplication.class, args);
    }
}