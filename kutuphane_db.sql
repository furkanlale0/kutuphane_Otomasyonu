DROP DATABASE IF EXISTS kutuphane_db;
CREATE DATABASE kutuphane_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE kutuphane_db;


CREATE TABLE yoneticiler (
    yonetici_id INT AUTO_INCREMENT PRIMARY KEY,
    ad VARCHAR(50),
    soyad VARCHAR(50),
    kullanici_adi VARCHAR(50) UNIQUE,
    email VARCHAR(100),
    sifre VARCHAR(255),
    rol VARCHAR(20), 
    olusturulma_tarihi DATETIME
);

CREATE TABLE uyeler (
    uye_id INT AUTO_INCREMENT PRIMARY KEY,
    ad VARCHAR(50),
    soyad VARCHAR(50),
    email VARCHAR(100) UNIQUE,
    sifre VARCHAR(255),
    telefon VARCHAR(20),
    kayit_tarihi DATETIME
);

CREATE TABLE yazarlar (
    yazar_id INT AUTO_INCREMENT PRIMARY KEY,
    ad VARCHAR(50),
    soyad VARCHAR(50),
    dogum_yili INT
);

CREATE TABLE kitaplar (
    kitap_id INT AUTO_INCREMENT PRIMARY KEY,
    kitap_adi VARCHAR(150),
    isbn VARCHAR(20),
    yayin_yili INT,
    stok_sayisi INT DEFAULT 0,
    ozet TEXT,
    yazar_id INT,
    ekleyen_yonetici_id INT,
    FOREIGN KEY (yazar_id) REFERENCES yazarlar(yazar_id),
    FOREIGN KEY (ekleyen_yonetici_id) REFERENCES yoneticiler(yonetici_id)
);

CREATE TABLE odunc_islemleri (
    odunc_id INT AUTO_INCREMENT PRIMARY KEY,
    uye_id INT,
    kitap_id INT,
    alis_tarihi DATETIME,
    son_teslim_tarihi DATETIME,
    iade_tarihi DATETIME,
    durum VARCHAR(20), -- ODUNC_ALINDI, IADE_EDILDI vb.
    FOREIGN KEY (uye_id) REFERENCES uyeler(uye_id),
    FOREIGN KEY (kitap_id) REFERENCES kitaplar(kitap_id)
);


DELIMITER //
CREATE TRIGGER stok_dusur
AFTER INSERT ON odunc_islemleri
FOR EACH ROW
BEGIN
    IF NEW.durum = 'ODUNC_ALINDI' THEN
        UPDATE kitaplar 
        SET stok_sayisi = stok_sayisi - 1 
        WHERE kitap_id = NEW.kitap_id;
    END IF;
END;
//
DELIMITER ;


DELIMITER //
CREATE TRIGGER stok_artir
AFTER UPDATE ON odunc_islemleri
FOR EACH ROW
BEGIN
    IF NEW.durum = 'IADE_EDILDI' AND OLD.durum != 'IADE_EDILDI' THEN
        UPDATE kitaplar 
        SET stok_sayisi = stok_sayisi + 1 
        WHERE kitap_id = NEW.kitap_id;
    END IF;
END;
//
DELIMITER ;

USE kutuphane_db;

ALTER TABLE odunc_islemleri 
ADD COLUMN bildirim_gonderildi BIT DEFAULT 0,
ADD COLUMN ceza_odendi_mi BIT DEFAULT 0;

USE kutuphane_db;

ALTER TABLE odunc_islemleri ADD COLUMN ceza_miktari DOUBLE DEFAULT 0;

ALTER TABLE odunc_islemleri ADD COLUMN odeme_durumu VARCHAR(20) DEFAULT 'YOK';