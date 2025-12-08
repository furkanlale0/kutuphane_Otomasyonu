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

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final AdminDetailsService userDetailsService; // Kullanıcı veritabanı servisi

    @Autowired
    public JwtAuthenticationFilter(JwtService jwtService, AdminDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 1. İstek Başlığından (Header) Token'ı Al
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        // Header yoksa veya "Bearer " ile başlamıyorsa işlemi pas geç
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // "Bearer " kısmını kesip sadece token'ı al (7 karakter)
        jwt = authHeader.substring(7);

        // Token'dan kullanıcı adını çıkar
        username = jwtService.extractUsername(jwt);

        // 2. Kullanıcı adı varsa ve sistemde henüz doğrulanmamışsa
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Veritabanından kullanıcıyı bul
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            // 3. Token geçerli mi kontrol et
            if (jwtService.isTokenValid(jwt, userDetails)) {

                // Spring Security'ye "Bu kullanıcı doğrulandı" bilgisini ver
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Güvenlik bağlamına (Context) kaydet
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // Zincirdeki diğer filtrelere devam et
        filterChain.doFilter(request, response);
    }
}