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

@Service
public class JwtServisi { // JwtService -> JwtServisi

    // COK ONEMLI: Bu anahtar bizim dijital imzamizdir.
    // Kimlik kartini (Token) basarken altina bu imzayi atariz.
    // Biri sahte kart yaparsa, bu imzayi taklit edemez.
    private static final String GIZLI_ANAHTAR = "bu_proje_icin_cok_gizli_ve_uzun_bir_sifreleme_anahtari_12345";

    // 1. TOKEN'DAN KULLANICI ADINI CIKARMA
    // Gelen istekteki kimlik kartina bakip "Bu kim?" sorusunun cevabini verir.
    public String kullaniciAdiniCikar(String token) {
        return veriyiCikar(token, Claims::getSubject);
    }

    // 2. YARDIMCI METOT (Genel Veri Cikarma)
    public <T> T veriyiCikar(String token, Function<Claims, T> veriCozucu) {
        final Claims bilgiler = tumBilgileriCikar(token);
        return veriCozucu.apply(bilgiler);
    }

    // 3. TOKEN URETME (Giris Yapan Kullanici Icin)
    // Login basarili olunca bu metot cagirilir ve kullaniciya bir Token verilir.
    public String tokenUret(UserDetails kullaniciDetaylari) {
        return tokenUret(new HashMap<>(), kullaniciDetaylari);
    }

    // 4. DETAYLI TOKEN URETME
    public String tokenUret(Map<String, Object> ekstraBilgiler, UserDetails kullaniciDetaylari) {
        return Jwts.builder()
                .setClaims(ekstraBilgiler)
                // Kullanici adi (veya email) token icine gomulur:
                .setSubject(kullaniciDetaylari.getUsername())
                // Ne zaman olusturuldu:
                .setIssuedAt(new Date(System.currentTimeMillis()))
                // Ne zaman son kullanma tarihi dolacak (Simdi + 24 Saat):
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24))
                // Dijital Imza atiliyor (Mühür):
                .signWith(imzaAnahtariniGetir(), SignatureAlgorithm.HS256)
                .compact();
    }

    // 5. TOKEN GECERLI MI?
    // Kullanici bir islem yapmak istediginde Token'ini gosterir.
    // Biz de bakariz: "Bu Token'daki isim ile su anki kullanici ayni mi?" VE "Suresi dolmus mu?"
    public boolean tokenGecerliMi(String token, UserDetails kullaniciDetaylari) {
        final String kullaniciAdi = kullaniciAdiniCikar(token);
        // Token'daki isim veritabanindakiyle uyusuyor mu? VE Suresi bitmemis mi?
        return (kullaniciAdi.equals(kullaniciDetaylari.getUsername())) && !tokenSuresiDolmusMu(token);
    }

    // Token'in son kullanma tarihi gecmis mi?
    private boolean tokenSuresiDolmusMu(String token) {
        return sonKullanmaTarihiniCikar(token).before(new Date());
    }

    private Date sonKullanmaTarihiniCikar(String token) {
        return veriyiCikar(token, Claims::getExpiration);
    }

    // Token'in icindeki gizli bilgileri (Claims) okumak icin kilidi acar.
    private Claims tumBilgileriCikar(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(imzaAnahtariniGetir())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // Gizli anahtari (String) sisteme uygun formata (Key) cevirir.
    private Key imzaAnahtariniGetir() {
        byte[] keyBytes = Decoders.BASE64.decode(java.util.Base64.getEncoder().encodeToString(GIZLI_ANAHTAR.getBytes()));
        return Keys.hmacShaKeyFor(keyBytes);
    }
}