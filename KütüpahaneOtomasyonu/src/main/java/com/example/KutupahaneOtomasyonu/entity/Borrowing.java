package com.example.KutupahaneOtomasyonu.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

// @Entity: Bu sınıfın bir veritabanı tablosu olduğunu belirtir.
@Entity
// @Table: Veritabanındaki adı "borrowings" olsun.
@Table(name = "borrowings")
// @Data: Lombok (Getter, Setter, toString metodlarını otomatik yazar).
@Data
@NoArgsConstructor // Boş constructor (Hibernate için zorunlu).
@AllArgsConstructor // Dolu constructor.
public class Borrowing {

    // @Id: Tablonun benzersiz kimlik numarası (Primary Key).
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "borrow_id")
    private Integer borrowId;

    // --- ÖZEL KONTROL: SPAM ENGELLEYİCİ ---
    // Mail gönderildi mi kontrolü için yeni kutucuk.
    // Scheduler (Zamanlayıcı) her çalıştığında tekrar tekrar mail atmasın diye,
    // mail attıktan sonra burayı 'true' yapıyoruz.
    @Column(name = "notification_sent")
    private boolean notificationSent = false;

    // --- İLİŞKİ 1: ÖDÜNÇ -> ÜYE ---
    // @ManyToOne: Bir üye birden fazla kitap alabilir.
    // fetch = FetchType.EAGER: "Hemen Getir".
    // Bir ödünç işlemini sorguladığımızda, kitabı alan üyenin kim olduğunu da hemen bilmek isteriz.
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "member_id", nullable = false) // Üye olmadan ödünç işlemi olamaz.

    // @JsonIgnoreProperties: ÇOK ÖNEMLİ! Sonsuz döngüyü ve güvenlik açığını önler.
    // 1. "borrowings": Üyenin içindeki eski ödünçleri getirme (Sonsuz döngü: Borrowing->Member->Borrowing...).
    // 2. "password": Üyenin şifresini JSON içinde gösterme (Güvenlik).
    // 3. "hibernateLazyInitializer": Teknik hataları önler.
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "borrowings", "password"})
    private Member member;

    // --- İLİŞKİ 2: ÖDÜNÇ -> KİTAP ---
    // @ManyToOne: Bir kitap defalarca ödünç alınabilir (Farklı zamanlarda).
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "book_id", nullable = false)
    // Kitap objesini JSON'a çevirirken, kitabı ekleyen admini ve kitabın eski ödünç geçmişini gizle.
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "borrowings", "addedByAdmin"})
    private Book book;

    // Kitabın alındığı tarih ve saat.
    @Column(name = "borrow_date", nullable = false)
    private LocalDateTime borrowDate;

    // Kitabın geri getirildiği tarih. (Henüz getirilmediyse NULL olur).
    @Column(name = "return_date")
    private LocalDateTime returnDate;

    // Son teslim tarihi (Alış tarihi + 14 gün gibi).
    @Column(name = "due_date")
    private LocalDateTime dueDate;

    // Durum: ALINDI, İADE EDİLDİ, GECİKTİ vs. (Enum olarak tutuyoruz).
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BorrowingStatus status;

    // --- CEZA KONTROLÜ ---
    // Düzeltilen Kısım: Küçük 'boolean' yerine büyük 'Boolean' kullandık.
    // Neden? Veritabanında eski kayıtlarda bu alan NULL olabilir.
    // Küçük 'boolean' null kabul etmez, patlar. Büyük 'Boolean' null kabul eder.
    @Column(name = "fine_paid")
    private Boolean finePaid = false;

    // Getter ve Setter'ları Lombok (@Data) otomatik halleder ama
    // manuel eklenen bu metodlar, özel durumlarda (örneğin boolean isimlendirme kuralları) garanti sağlar.
    public boolean isNotificationSent() { return notificationSent; }
    public void setNotificationSent(boolean notificationSent) { this.notificationSent = notificationSent; }
}