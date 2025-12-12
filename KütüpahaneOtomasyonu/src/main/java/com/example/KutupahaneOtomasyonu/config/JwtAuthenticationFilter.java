package com.example.KutupahaneOtomasyonu.config;

import com.example.KutupahaneOtomasyonu.service.AdminDetailsService;
import com.example.KutupahaneOtomasyonu.service.JwtService;
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

// @Component: Spring'e "Bu bir güvenlik parçasıdır, bunu yönet" diyoruz.
// extends OncePerRequestFilter: Bu çok önemli. Kullanıcıdan gelen HER İSTEKTE
// bu filtrenin sadece "1 KERE" çalışmasını garanti eder.
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService; // Token'ı okumak ve çözmek için gerekli araç.
    private final AdminDetailsService userDetailsService; // Veritabanından kullanıcıyı bulmak için gerekli servis.

    // Constructor Injection: Gerekli servisleri Spring'den istiyoruz.
    @Autowired
    public JwtAuthenticationFilter(JwtService jwtService, AdminDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    // --- ASIL İŞİN YAPILDIĞI YER ---
    // Gelen her istek (Request) bu metodun içinden geçer.
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 1. Gelen isteğin başlığında (Header) "Authorization" diye bir kısım var mı diye bakıyoruz.
        // Çünkü Token'lar genelde burada taşınır.
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        // KONTROL: Header boş mu? Veya "Bearer " kelimesi ile başlamıyor mu?
        // Eğer token yoksa veya formatı yanlışsa, biz karışmıyoruz.
        // "filterChain.doFilter" diyerek isteği bir sonraki adıma (belki Login sayfasına) salıyoruz.
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Token'ı Ayıkla
        // Gelen veri şöyledir: "Bearer eyJhbGciOiJIUz..."
        // Bizim baştaki "Bearer " (7 karakter) kısmını kesip sadece saf token kodunu almamız lazım.
        jwt = authHeader.substring(7);

        // 3. Token'ın içinden Kullanıcı Adını Çıkar (JwtService bu işi yapar)
        username = jwtService.extractUsername(jwt);

        // 4. Doğrulama Aşaması
        // - username != null: Token'dan bir isim çıktı mı?
        // - SecurityContextHolder... == null: Bu kullanıcı sisteme ZATEN giriş yapmış mı?
        // (Eğer zaten giriş yapmışsa tekrar yormayalım, yapmamışsa (null ise) kontrol edelim).
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Veritabanına git ve bu kullanıcı adıyla kayıtlı kişinin tüm bilgilerini getir.
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            // 5. Token Geçerli mi? (Süresi dolmuş mu? İmzası doğru mu? Kullanıcı adı eşleşiyor mu?)
            if (jwtService.isTokenValid(jwt, userDetails)) {

                // EVET, GEÇERLİ! O zaman Spring Security'ye "Bu adam bizden, içeri al" dememiz lazım.

                // Kimlik kartını oluşturuyoruz (Kullanıcı bilgileri, şifre(null), yetkileri)
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

                // İsteğin detaylarını (IP adresi, tarayıcı bilgisi vs.) ekliyoruz.
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // VE FİNAL: Kullanıcıyı "Giriş Yapmış" (Authenticated) olarak sisteme kaydediyoruz.
                // Artık bu istek boyunca sistem bu kullanıcıyı tanıyacak.
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // 6. Zincirleme Devam Et
        // Biz işimizi bitirdik (kontrolü yaptık), artık istek yoluna devam edebilir (Controller'a gidebilir).
        filterChain.doFilter(request, response);
    }
}