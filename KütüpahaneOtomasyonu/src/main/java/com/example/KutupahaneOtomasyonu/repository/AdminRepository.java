package com.example.KutupahaneOtomasyonu.repository;

import com.example.KutupahaneOtomasyonu.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Integer> {
    // Spring Security için Admin'i kullanıcı adına göre bulmak gerekli olacak
    Optional<Admin> findByUsername(String username);
}