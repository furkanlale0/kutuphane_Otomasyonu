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
public class JwtService {

    // ÇOK ÖNEMLİ: Bu anahtar 256-bit (32 karakter) olmalı.
    // Gerçek projede application.properties dosyasında saklanır ama ödev için buraya sabitliyorum.
    private static final String SECRET_KEY = "bu_proje_icin_cok_gizli_ve_uzun_bir_sifreleme_anahtari_12345";

    // 1. Token'dan Kullanıcı Adını Çıkarma
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // 2. Token'dan Belirli Bir Veriyi Çıkarma (Expiration vb.)
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // 3. Token Üretme (Kullanıcı Adı ile)
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    // 4. Token Üretme (Detaylı)
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername()) // Kullanıcı adı token içine gömülür
                .setIssuedAt(new Date(System.currentTimeMillis())) // Oluşturulma zamanı
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24)) // 24 Saat geçerli
                .signWith(getSignInKey(), SignatureAlgorithm.HS256) // İmzalama
                .compact();
    }

    // 5. Token Geçerli mi? (Kullanıcı adı ve Süre kontrolü)
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(java.util.Base64.getEncoder().encodeToString(SECRET_KEY.getBytes()));
        return Keys.hmacShaKeyFor(keyBytes);
    }
}