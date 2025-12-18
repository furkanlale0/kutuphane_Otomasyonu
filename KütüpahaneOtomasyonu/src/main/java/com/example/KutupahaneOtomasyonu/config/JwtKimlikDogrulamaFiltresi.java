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

/*
 * BU SINIF NE İŞE YARAR?
 * Bu bir "Güvenlik Filtresi"dir. Sunucuya gelen HER isteği havada yakalar.
 * Amacı: Gelen isteğin cebinde (Header kısmında) geçerli bir "Kimlik Kartı" (JWT Token) var mı diye bakmaktır.
 * Eğer token geçerliyse, kullanıcıyı sisteme "Giriş Yapmış" olarak işaretler.
 */
@Component
public class JwtKimlikDogrulamaFiltresi extends OncePerRequestFilter {

    private final JwtServisi jwtServisi;
    private final KullaniciDetayServisi kullaniciDetayServisi;

    /*
     * Dependency Injection (Bağımlılık Enjeksiyonu)
     * Gerekli servislerin (Token işlemleri ve Veritabanı işlemleri) sınıfa dahil edildiği yer.
     */
    @Autowired
    public JwtKimlikDogrulamaFiltresi(JwtServisi jwtServisi, KullaniciDetayServisi kullaniciDetayServisi) {
        this.jwtServisi = jwtServisi;
        this.kullaniciDetayServisi = kullaniciDetayServisi;
    }

    /*
     * ANA FİLTRELEME METODU
     * Gelen isteği analiz eder:
     * 1. "Authorization" başlığını kontrol eder.
     * 2. "Bearer " ile başlayan token'ı ayıklar.
     * 3. Token içindeki kullanıcı adını çözer.
     * 4. Token geçerliyse, Spring Security'ye "Bu kullanıcı güvenlidir, kapıyı aç" der (Context'e yazar).
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        final String kimlikBasligi = request.getHeader("Authorization");
        final String jwtToken;
        final String kullaniciAdi;

        if (kimlikBasligi == null || !kimlikBasligi.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwtToken = kimlikBasligi.substring(7);
        kullaniciAdi = jwtServisi.kullaniciAdiniCikar(jwtToken);

        if (kullaniciAdi != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            UserDetails kullaniciDetaylari = this.kullaniciDetayServisi.loadUserByUsername(kullaniciAdi);

            if (jwtServisi.tokenGecerliMi(jwtToken, kullaniciDetaylari)) {

                UsernamePasswordAuthenticationToken kimlikBelgesi = new UsernamePasswordAuthenticationToken(
                        kullaniciDetaylari,
                        null,
                        kullaniciDetaylari.getAuthorities()
                );

                kimlikBelgesi.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(kimlikBelgesi);
            }
        }

        filterChain.doFilter(request, response);
    }
}