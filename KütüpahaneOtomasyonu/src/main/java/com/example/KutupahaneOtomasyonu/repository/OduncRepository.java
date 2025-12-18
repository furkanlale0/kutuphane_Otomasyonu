package com.example.KutupahaneOtomasyonu.repository;

import com.example.KutupahaneOtomasyonu.entity.OduncIslemi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/*
 * BU SINIF NE İŞE YARAR?
 * Ödünç alma işlemlerini yöneten veri erişim katmanıdır.
 * "OduncIslemleri" tablosu üzerinde çalışır.
 * Aktif ödünçleri, gecikmiş kitapları ve kullanıcı geçmişini sorgulamak için kullanılır.
 */
@Repository
public interface OduncRepository extends JpaRepository<OduncIslemi, Integer> {

    /*
     * ÜYENİN ELİNDEKİ KİTAPLAR
     * Bir üyenin aldığı ancak henüz iade etmediği (iade tarihi NULL olan)
     * tüm aktif kitapları listeler.
     */
    List<OduncIslemi> findByUye_UyeIdAndIadeTarihiIsNull(Integer uyeId);

    /*
     * İADE KONTROL SORGUSU
     * İade işlemi yapılırken, sistemin doğru kaydı bulmasını sağlar.
     * "Bu üyenin elinde şu an bu kitap var mı?" sorusunun cevabıdır.
     */
    List<OduncIslemi> findByUye_UyeIdAndKitap_KitapIdAndIadeTarihiIsNull(Integer uyeId, Integer kitapId);

    /*
     * KİTAP SİLME GÜVENLİĞİ
     * Yönetici bir kitabı silmeye çalıştığında bu metod devreye girer.
     * Eğer kitap şu an bir üyedeyse (iade tarihi NULL ise) silme işlemini engellemek için true döner.
     */
    boolean existsByKitap_KitapIdAndIadeTarihiIsNull(Integer kitapId);

    /*
     * KİTAP HAREKET GEÇMİŞİ
     * Bir kitabın bugüne kadar kimler tarafından ne zaman alındığının dökümünü verir.
     */
    List<OduncIslemi> findByKitap_KitapId(Integer kitapId);

    /*
     * ÜYE İŞLEM GEÇMİŞİ
     * Bir üyenin bugüne kadar yaptığı tüm alış-veriş işlemlerini listeler.
     * Profil sayfasındaki "Geçmiş İşlemlerim" tablosu için kullanılır.
     */
    List<OduncIslemi> findByUye_UyeId(Integer uyeId);

    /*
     * MÜKERRER İŞLEM KONTROLÜ
     * Bir üyenin aynı kitabı iade etmeden ikinci kez almasını engeller.
     * Mantık hatasını ve stok tutarsızlığını önler.
     */
    boolean existsByUye_UyeIdAndKitap_KitapIdAndIadeTarihiIsNull(Integer uyeId, Integer kitapId);

    /*
     * GECİKMİŞ KİTAPLARI TESPİT ETME
     * Teslim tarihi geçmiş (Before) ancak hala iade edilmemiş (IsNull) kayıtları bulur.
     * Ceza hesaplama ve bildirim gönderme mekanizması bu sorguyu kullanır.
     */
    List<OduncIslemi> findBySonTeslimTarihiBeforeAndIadeTarihiIsNull(java.time.LocalDateTime tarih);
}