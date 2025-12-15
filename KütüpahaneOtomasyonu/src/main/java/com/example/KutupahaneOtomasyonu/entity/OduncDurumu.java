package com.example.KutupahaneOtomasyonu.entity;

// Enum: "Sabit Secenekler Listesi" demektir.
// Bir kitap odunc alindiginda durumu kafamiza gore "Verdim", "Gitti", "Gelmedi" yazamayiz.
// Sadece asagidaki 3 secenekten biri olabilir. Bu sayede veritabani duzenli kalir.
public enum OduncDurumu {

    // Kitap su an uyede ve suresi henuz dolmadi.
    ODUNC_ALINDI,

    // Kitap kutuphaneye geri getirildi, islem kapandi.
    IADE_EDILDI,

    // Son teslim tarihi gecti ama kitap hala gelmedi (Ceza islemeye baslar).
    GECIKTI
}