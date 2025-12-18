package com.example.KutupahaneOtomasyonu.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/*
 * BU SINIF NE İŞE YARAR?
 * JWT (JSON Web Token) yönetiminden sorumlu servis sınıfıdır.
 * Kimlik doğrulama (Authentication) süreçlerinde token üretme,
 * gelen tokenı doğrulama ve içindeki verileri (Claims) ayrıştırma işlemlerini yürütür.
 */
@Service
public class JwtServisi {

    /*
     * GİZLİ ANAHTAR (SECRET KEY)
     * Token imzalama ve doğrulama işlemlerinde kullanılan kriptografik anahtardır.
     * HMAC-SHA algoritması ile verinin bütünlüğünü sağlar.
     * (Not: Prodüksiyon ortamında bu değer çevresel değişkenlerden okunmalıdır.)
     */
    private static final String GIZLI_ANAHTAR = "bu_proje_icin_cok_gizli_ve_uzun_bir_sifreleme_anahtari_12345";

    /*
     * KULLANICI ADI ÇIKARMA
     * JWT içerisindeki "Subject" (Kullanıcı Adı/Email) bilgisini ayrıştırır.
     */
    public String kullaniciAdiniCikar(String token) {
        return veriyiCikar(token, Claims::getSubject);
    }

    /*
     * GENEL VERİ AYRIŞTIRMA (Helper Method)
     * Token içerisindeki herhangi bir veriyi (Claim) okumak için kullanılır.
     */
    public <T> T veriyiCikar(String token, Function<Claims, T> veriCozucu) {
        final Claims bilgiler = tumBilgileriCikar(token);
        return veriCozucu.apply(bilgiler);
    }

    /*
     * TOKEN ÜRETME (Basic)
     * Kullanıcı başarılı bir şekilde giriş yaptığında çağrılır.
     * Standart bir JWT oluşturur.
     */
    public String tokenUret(UserDetails kullaniciDetaylari) {
        return tokenUret(new HashMap<>(), kullaniciDetaylari);
    }

    /*
     * DETAYLI TOKEN OLUŞTURMA
     * JWT'nin payload kısmını oluşturur ve dijital olarak imzalar.
     *
     * - setSubject: Kullanıcı kimliği (Email/Username)
     * - setIssuedAt: Oluşturulma zamanı
     * - setExpiration: Geçerlilik süresi (Şu an + 24 Saat)
     * - signWith: Gizli anahtar ile imzalama (HS256 Algoritması)
     */
    public String tokenUret(Map<String, Object> ekstraBilgiler, UserDetails kullaniciDetaylari) {
        return Jwts.builder()
                .setClaims(ekstraBilgiler)
                .setSubject(kullaniciDetaylari.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24))
                .signWith(imzaAnahtariniGetir(), SignatureAlgorithm.HS256)
                .compact();
    }

    /*
     * TOKEN DOĞRULAMA (VALIDATION)
     * Gelen tokenın geçerliliğini iki aşamada kontrol eder:
     * 1. Token içerisindeki kullanıcı adı ile o anki kullanıcı eşleşiyor mu?
     * 2. Tokenın son kullanma tarihi (Expiration) geçmiş mi?
     */
    public boolean tokenGecerliMi(String token, UserDetails kullaniciDetaylari) {
        final String kullaniciAdi = kullaniciAdiniCikar(token);
        return (kullaniciAdi.equals(kullaniciDetaylari.getUsername())) && !tokenSuresiDolmusMu(token);
    }

    // Token süresinin dolup dolmadığını kontrol eder.
    private boolean tokenSuresiDolmusMu(String token) {
        return sonKullanmaTarihiniCikar(token).before(new Date());
    }

    private Date sonKullanmaTarihiniCikar(String token) {
        return veriyiCikar(token, Claims::getExpiration);
    }

    // Token'ı parse ederek içindeki tüm verileri (Claims) okur.
    private Claims tumBilgileriCikar(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(imzaAnahtariniGetir())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // String formatındaki gizli anahtarı, kriptografik 'Key' nesnesine dönüştürür.
    private Key imzaAnahtariniGetir() {
        byte[] keyBytes = Decoders.BASE64.decode(java.util.Base64.getEncoder().encodeToString(GIZLI_ANAHTAR.getBytes()));
        return Keys.hmacShaKeyFor(keyBytes);
    }
}