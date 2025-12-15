package com.example.KutupahaneOtomasyonu.config;

import com.example.KutupahaneOtomasyonu.service.KullaniciDetayServisi;
import com.example.KutupahaneOtomasyonu.service.JwtServisi;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// @Component: Spring'e "Bu sinif guvenlik kontrolunun bir parcasidir, bunu yonet" diyoruz.
// extends OncePerRequestFilter: Bu cok onemli. Kullanicidan gelen HER ISTEKTE (Login, Kitap Ekleme vs.)
// bu filtrenin sadece "1 KERE" calismasini garanti eder. Guvenlik kapisidir.
@Component
public class JwtKimlikDogrulamaFiltresi extends OncePerRequestFilter { // JwtAuthenticationFilter -> JwtKimlikDogrulamaFiltresi

    private final JwtServisi jwtServisi; // Token'i okumak, cozmek ve gecerliligini kontrol etmek icin.
    private final KullaniciDetayServisi kullaniciDetayServisi; // Veritabanindan kullaniciyi bulmak icin.

    // Gerekli servisleri Spring'den istiyoruz (Dependency Injection).
    @Autowired
    public JwtKimlikDogrulamaFiltresi(JwtServisi jwtServisi, KullaniciDetayServisi kullaniciDetayServisi) {
        this.jwtServisi = jwtServisi;
        this.kullaniciDetayServisi = kullaniciDetayServisi;
    }

    // --- ASIL ISIN YAPILDIGI YER ---
    // Gelen her istek (Request) once bu metodun icinden gecer.
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 1. HEADER KONTROLU
        // Gelen istegin basliginda (Header) "Authorization" diye bir kisim var mi?
        // Token'lar genelde burada tasinir.
        final String kimlikBasligi = request.getHeader("Authorization"); // authHeader
        final String jwtToken;
        final String kullaniciAdi; // username

        // Eger baslik bossa veya "Bearer " ile baslamiyorsa, bu istekte token yok demektir.
        // Biz karismiyoruz, istegi bir sonraki adima (belki Login sayfasina) saliyoruz.
        if (kimlikBasligi == null || !kimlikBasligi.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. TOKEN'I AYIKLA
        // Gelen veri su sekildedir: "Bearer eyJhbGciOiJIUz..."
        // Bizim bastaki "Bearer " (7 karakter) kismini kesip sadece saf token kodunu almamiz lazim.
        jwtToken = kimlikBasligi.substring(7);

        // 3. KULLANICI ADINI BUL
        // Sifreli token'i cozup icindeki kullanici adini cikariyoruz.
        kullaniciAdi = jwtServisi.kullaniciAdiniCikar(jwtToken);

        // 4. DOGRULAMA ASAMASI
        // Eger isim bulunduysa VE kullanici sisteme henuz giris yapmis gozukmuyorsa (SecurityContext bos ise):
        if (kullaniciAdi != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Veritabanina git ve bu isimdeki kisinin tum bilgilerini getir.
            UserDetails kullaniciDetaylari = this.kullaniciDetayServisi.loadUserByUsername(kullaniciAdi);

            // 5. TOKEN GECERLI MI?
            // (Suresi dolmus mu? Imzasi dogru mu? Veritabanindaki kisiyle uyusuyor mu?)
            if (jwtServisi.tokenGecerliMi(jwtToken, kullaniciDetaylari)) {

                // EVET, GECERLI! Spring Security'ye "Bu kisi bizden, iceri al" diyoruz.

                // Gecici bir Kimlik Karti (Token) olusturuyoruz.
                UsernamePasswordAuthenticationToken kimlikBelgesi = new UsernamePasswordAuthenticationToken(
                        kullaniciDetaylari,
                        null,
                        kullaniciDetaylari.getAuthorities()
                );

                // Istegin detaylarini (IP adresi, tarayici bilgisi vs.) ekliyoruz.
                kimlikBelgesi.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // VE FINAL: Kullaniciyi "Giris Yapmis" (Authenticated) olarak sisteme kaydediyoruz.
                // Artik bu istek boyunca sistem bu kullaniciyi taniyacak.
                SecurityContextHolder.getContext().setAuthentication(kimlikBelgesi);
            }
        }

        // 6. DEVAM ET
        // Biz isimiz bitti (kontrolu yaptik), artik istek yoluna devam edebilir (Controller'a gidebilir).
        filterChain.doFilter(request, response);
    }
}