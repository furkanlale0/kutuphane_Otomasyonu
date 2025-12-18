const API_URL = "http://localhost:8080/api";
let tumKitaplar = [];

// --- YARDIMCILAR ---
function showToast(title, icon = 'success') {
    Swal.fire({ title: title, icon: icon, toast: true, position: 'top-end', showConfirmButton: false, timer: 3000, background: '#1e1e1e', color: '#fff' });
}
function showPopup(title, text, icon = 'error') {
    Swal.fire({ title: title, text: text, icon: icon, background: '#1e1e1e', color: '#fff' });
}

// --- BASLANGIC ---
document.addEventListener("DOMContentLoaded", () => {
    if (window.location.pathname.includes("dashboard.html")) {
        kimlikKontrolu();
        kitaplariYukle();
        if(localStorage.getItem("kullaniciRolu") === "UYE") aktifOdunclerimiYukle();

        // Arama
        const searchInput = document.getElementById("searchInput");
        if(searchInput) {
            searchInput.addEventListener("input", (e) => {
                const val = e.target.value.toLowerCase();
                const filtered = tumKitaplar.filter(k => k.kitapAdi.toLowerCase().includes(val));
                tabloyuCiz(filtered);
            });
        }
    }
    formlariHazirla();
});

// --- KITAP YUKLE ---
async function kitaplariYukle() {
    const token = localStorage.getItem("jwtToken");
    try {
        const res = await fetch(`${API_URL}/kitaplar`, { headers: { "Authorization": `Bearer ${token}` } });
        if (res.ok) {
            tumKitaplar = await res.json();
            tabloyuCiz(tumKitaplar);
            istatistikleriGuncelle(tumKitaplar);
        }
    } catch (e) { console.error(e); }
}

function tabloyuCiz(kitaplar) {
    const tbody = document.getElementById("bookListBody");
    const role = localStorage.getItem("kullaniciRolu");
    tbody.innerHTML = "";

    if (kitaplar.length === 0) { tbody.innerHTML = "<tr><td colspan='4' class='text-center text-muted'>Kitap yok.</td></tr>"; return; }

    kitaplar.forEach(k => {
        const yazar = k.yazar ? `${k.yazar.ad} ${k.yazar.soyad}` : "-";
        let btn = "";
        if (role === "ADMIN") {
            btn = `<button class="btn btn-outline-danger btn-sm" onclick="kitapSil(${k.kitapId})">Sil</button>`;
        } else {
            btn = k.stokSayisi > 0
                ? `<button class="btn btn-outline-info btn-sm" onclick="oduncAl(${k.kitapId})">Al</button>`
                : `<button class="btn btn-secondary btn-sm" disabled>Yok</button>`;
        }

        tbody.innerHTML += `
            <tr class="align-middle">
                <td class="text-white fw-bold">${k.kitapAdi}</td>
                <td>${yazar}</td>
                <td class="text-center"><span class="badge ${k.stokSayisi > 0 ? 'bg-success' : 'bg-danger'}">${k.stokSayisi}</span></td>
                <td class="text-end">
                    <button class="btn btn-outline-secondary btn-sm me-1" onclick="detayGoster(${k.kitapId})">Detay</button>
                    ${btn}
                </td>
            </tr>`;
    });
}

// --- ODUNC AL ---
window.oduncAl = async function(kitapId) {
    const result = await Swal.fire({ title: 'Onay', html: `
            <div class="text-start">
                <p>Bu bir sunum demosudur.</p>
                <ul class="text-warning">
                    <li>Ödünç Süresi: <b>1 Dakika</b></li>
                    <li>Gecikme Cezası: <b>Dakika başı 5 TL</b></li>
                </ul>
                <p class="mt-3 text-center">Onaylıyor musunuz?</p>
            </div>
        `, icon: 'warning', showCancelButton: true, confirmButtonText: 'Evet,Alıyorum',cancelButtonText: 'Vazgeç', background: '#1e1e1e', color: '#fff' });
    if (!result.isConfirmed) return;

    const token = localStorage.getItem("jwtToken");
    const uyeId = localStorage.getItem("kullaniciId");

    try {
        const res = await fetch(`${API_URL}/odunc/al`, {
            method: "POST",
            headers: { "Content-Type": "application/json", "Authorization": `Bearer ${token}` },
            body: JSON.stringify({ uyeId: uyeId, kitapId: kitapId })
        });

        // Backend'den gelen cevabi (text veya json) duzgun oku
        const cevapMetni = await res.text();

        if (res.ok) {
            showToast("Kitap alındı!");
            kitaplariYukle();
            aktifOdunclerimiYukle();
        } else {
            // Hatayi ekrana bas
            showPopup("Hata Oluştu", cevapMetni);
        }
    } catch (e) { showPopup("Sunucu Hatası", "Bağlantı kurulamadı."); }
};

// --- IADE ET ---
window.iadeEt = async function(kitapId) {
    if(!confirm("İade edilsin mi?")) return;
    const token = localStorage.getItem("jwtToken");
    const uyeId = localStorage.getItem("kullaniciId");

    try {
        const res = await fetch(`${API_URL}/odunc/iade`, {
            method: "POST",
            headers: { "Content-Type": "application/json", "Authorization": `Bearer ${token}` },
            body: JSON.stringify({ uyeId: uyeId, kitapId: kitapId })
        });
        if (res.ok) {
            showToast("İade edildi!");
            kitaplariYukle();
            aktifOdunclerimiYukle();
        }
    } catch (e) { console.error(e); }
};

// --- YAN MENU (AKTIFLER) ---
async function aktifOdunclerimiYukle() {
    const token = localStorage.getItem("jwtToken");
    const uyeId = localStorage.getItem("kullaniciId");
    try {
        const res = await fetch(`${API_URL}/odunc/uye/${uyeId}/aktif`, { headers: { "Authorization": `Bearer ${token}` } });
        if (res.ok) {
            const data = await res.json();
            const tbody = document.getElementById("myLoansBody");
            tbody.innerHTML = "";
            if (data.length === 0) tbody.innerHTML = "<tr><td colspan='2' class='text-muted small text-center'>Ödünç yok.</td></tr>";

            data.forEach(item => {
                tbody.innerHTML += `
                    <tr>
                        <td class="text-white small">${item.kitapAdi}</td>
                        <td class="text-end"><button class="btn btn-xs btn-success" style="font-size:10px" onclick="iadeEt(${item.kitapId})">İade</button></td>
                    </tr>`;
            });
        }
    } catch (e) { console.error(e); }
}

// --- MODALLAR VE BUTONLAR ---

// --- PROFIL VE CEZA DETAYLARI ---
// --- UYE: PROFIL (AKTIF CEZA GORUNUMU EKLENDI) ---
window.profilAc = async function() {
    const rol = localStorage.getItem("kullaniciRolu");
    const adSoyad = localStorage.getItem("adSoyad") || "Kullanıcı"; // Ismi al

    // 1. ISMI YAZDIR ("Yükleniyor" yerine isim gelecek)
    document.getElementById("profName").innerText = adSoyad;

    // --- ADMIN ISE ---
    if(rol === "ADMIN") {
        document.getElementById("profName").innerText = adSoyad + " (Yönetici)";

        // Admin icin borc kutularini GIZLE
        // Not: HTML'deki ID'lere gore gizliyoruz.
        // Eger ID bulamazsa hata vermemesi icin "?." kullaniyoruz
        document.querySelector("#profileModal .card")?.style.setProperty("display", "none", "important");
        document.getElementById("debtDetailsContainer")?.parentElement.style.setProperty("display", "none", "important");

        new bootstrap.Modal(document.getElementById('profileModal')).show();
        return;
    }

    // --- UYE ISE ---
    // Uye icin her seyi goster
    document.querySelector("#profileModal .card")?.style.removeProperty("display");
    document.getElementById("debtDetailsContainer")?.parentElement.style.removeProperty("display");

    const token = localStorage.getItem("jwtToken");
    const uyeId = localStorage.getItem("kullaniciId");

    try {
        const res = await fetch(`${API_URL}/odunc/uye/${uyeId}/ceza-detay`, {
            headers: { "Authorization": `Bearer ${token}` }
        });

        if (res.ok) {
            const data = await res.json();
            // ... (Tablo oluşturma kodları aynen kalacak) ...
            // Sadece tabloyu doldurma kısmı aynı, burayı tekrar yazmıyorum
            // Önceki cevaptaki tablo oluşturma kodunu buraya koyabilirsin.
            let toplamBorc = 0;
            let bekleyenBorc = 0;

            let tabloHtml = `<table class="table table-dark table-sm mt-2">
                <thead><tr><th>Kitap</th><th>Tutar</th><th>Durum</th></tr></thead><tbody>`;

            if(data.length === 0) {
                tabloHtml += `<tr><td colspan="3" class="text-center text-muted">Borcunuz yok.</td></tr>`;
            } else {
                data.forEach(c => {
                    let renk = "text-danger";
                    let durumYazi = "Ödenmedi";

                    if(c.durum === "AKTIF_GECIKME") {
                        renk = "text-info";
                        durumYazi = "Şu an Gecikmede";
                        toplamBorc += c.tutar;
                    } else if(c.durum === "ONAY_BEKLIYOR") {
                        renk = "text-warning";
                        durumYazi = "İnceleniyor";
                        bekleyenBorc += c.tutar;
                    } else if(c.durum === "ODENMEDI") {
                        toplamBorc += c.tutar;
                    } else if(c.durum === "ODENDI") {
                        renk = "text-success";
                        durumYazi = "Ödendi";
                    }

                    tabloHtml += `<tr><td>${c.kitap}</td><td>${c.tutar} TL</td><td class="${renk}">${durumYazi}</td></tr>`;
                });
            }
            tabloHtml += `</tbody></table>`;

            document.getElementById("debtDetailsContainer").innerHTML = tabloHtml;

            const borcElem = document.getElementById("profDebt");
            if(toplamBorc > 0) {
                borcElem.innerText = `${toplamBorc} TL`;
                borcElem.className = "fw-bold text-danger me-2";
            } else if (bekleyenBorc > 0) {
                borcElem.innerText = "Onay Bekliyor";
                borcElem.className = "fw-bold text-warning me-2";
            } else {
                borcElem.innerText = "0 TL";
                borcElem.className = "fw-bold text-success me-2";
            }

            new bootstrap.Modal(document.getElementById('profileModal')).show();
        }
    } catch (e) { console.error(e); }
};

// --- ODEME MODALI AC ---
window.openPaymentModal = function() {
    const borcText = document.getElementById("profDebt").innerText;

    // Eger 0 TL ise veya Onay Bekliyorsa acma
    if (borcText === "0 TL" || borcText.includes("Onay")) {
        Swal.fire({
            title: 'İşlem Gerekmiyor',
            text: 'Ödenecek aktif bir cezanız bulunmamaktadır.',
            icon: 'info',
            background: '#1e1e1e', color: '#fff'
        });
        return;
    }

    // Profili kapat odemeyi ac
    bootstrap.Modal.getInstance(document.getElementById('profileModal')).hide();
    new bootstrap.Modal(document.getElementById('paymentModal')).show();
};

// --- ODEME BILDIR (Process Payment) ---
window.processPayment = async function() {
    const token = localStorage.getItem("jwtToken");
    const uyeId = localStorage.getItem("kullaniciId");

    try {
        const res = await fetch(`${API_URL}/odunc/ceza-bildir/${uyeId}`, {
            method: "POST",
            headers: { "Authorization": `Bearer ${token}` }
        });
        if(res.ok) {
            bootstrap.Modal.getInstance(document.getElementById('paymentModal')).hide();
            showToast("Bildirim gönderildi! Yönetici onayı bekleniyor.");
        } else {
            showPopup("Hata", "İşlem başarısız.");
        }
    } catch(e) { console.error(e); }
};

// --- ADMIN: CEZALAR VE ONAY ---
window.cezalarAc = async function() {
    const token = localStorage.getItem("jwtToken");
    try {
        const res = await fetch(`${API_URL}/odunc/admin/cezalar`, { headers: { "Authorization": `Bearer ${token}` } });
        if (res.ok) {
            const data = await res.json();
            const tbody = document.getElementById("adminFinesBody");
            tbody.innerHTML = "";

            if (data.length === 0) {
                tbody.innerHTML = "<tr><td colspan='5' class='text-center text-muted'>Tahsil edilecek ceza yok.</td></tr>";
            } else {
                data.forEach(c => {
                    let buton = "";
                    let durumBadge = "";

                    if(c.durum === "ONAY_BEKLIYOR") {
                        durumBadge = '<span class="badge bg-warning text-dark">Onay Bekliyor</span>';
                        buton = `<button class="btn btn-sm btn-success" onclick="cezaOnayla(${c.oduncId})">Onayla</button>`;
                    } else {
                        durumBadge = '<span class="badge bg-danger">Ödenmedi</span>';
                        buton = `<span class="text-muted small">Bildirim Bekleniyor</span>`;
                    }

                    tbody.innerHTML += `
                        <tr>
                            <td>${c.uyeAd}</td>
                            <td>${c.kitapAd}</td>
                            <td class="text-danger fw-bold">${c.tutar} TL</td>
                            <td>${durumBadge}</td>
                            <td>${buton}</td>
                        </tr>`;
                });
            }
            new bootstrap.Modal(document.getElementById('adminFinesModal')).show();
        }
    } catch (e) { console.error(e); }
};

// --- ADMIN ONAYLA FONKSIYONU ---
window.cezaOnayla = async function(oduncId) {
    const token = localStorage.getItem("jwtToken");
    try {
        const res = await fetch(`${API_URL}/odunc/ceza-onayla/${oduncId}`, {
            method: "POST",
            headers: { "Authorization": `Bearer ${token}` }
        });
        if(res.ok) {
            showToast("Tahsilat Onaylandı!");
            cezalarAc(); // Listeyi yenile
        }
    } catch(e) { console.error(e); }
};
// 2. Geçmiş Modalı
window.gecmisAc = async function() {
    const token = localStorage.getItem("jwtToken");
    const uyeId = localStorage.getItem("kullaniciId");
    try {
        const res = await fetch(`${API_URL}/odunc/uye/${uyeId}/gecmis`, { headers: { "Authorization": `Bearer ${token}` } });
        if (res.ok) {
            const data = await res.json();
            const tbody = document.getElementById("historyTableBody");
            tbody.innerHTML = "";
            if(data.length === 0) tbody.innerHTML = "<tr><td colspan='4' class='text-center'>İşlem yok.</td></tr>";

            data.forEach(d => {
                tbody.innerHTML += `<tr><td>${d.kitapAdi}</td><td>${d.alisTarihi.substring(0,10)}</td><td>${d.iadeTarihi ? d.iadeTarihi.substring(0,10) : '-'}</td><td>${d.durum}</td></tr>`;
            });
            new bootstrap.Modal(document.getElementById('historyModal')).show();
        }
    } catch (e) { console.error(e); }
};

// --- ADMIN: CEZALAR (UNDEFINED SORUNU ÇÖZÜLDÜ) ---
window.cezalarAc = async function() {
    const token = localStorage.getItem("jwtToken");
    try {
        const res = await fetch(`${API_URL}/odunc/admin/cezalar`, { headers: { "Authorization": `Bearer ${token}` } });
        if (res.ok) {
            const data = await res.json();
            const tbody = document.getElementById("adminFinesBody");
            tbody.innerHTML = "";

            if (data.length === 0) {
                tbody.innerHTML = "<tr><td colspan='5' class='text-center text-muted'>Hiç ceza yok.</td></tr>";
            } else {
                data.forEach(c => {
                    let durumHtml = "";
                    let islemButonu = "";

                    // Duruma göre renk ve buton ayarla
                    if (c.durum === "AKTIF_GECIKME") {
                        durumHtml = '<span class="badge bg-secondary">Kitap Üyede (Gecikmiş)</span>';
                        islemButonu = '<span class="text-muted small">İade Bekleniyor</span>';
                    } else if (c.durum === "ONAY_BEKLIYOR") {
                        durumHtml = '<span class="badge bg-warning text-dark">Onay Bekliyor</span>';
                        islemButonu = `<button class="btn btn-sm btn-success" onclick="cezaOnayla(${c.oduncId})">Onayla</button>`;
                    } else if (c.durum === "ODENMEDI") {
                        durumHtml = '<span class="badge bg-danger">Ödenmedi</span>';
                        islemButonu = '<span class="text-muted small">Ödeme Bekleniyor</span>';
                    }

                    // Burada backend'den gelen "c.uye", "c.kitap" isimlerini kullanıyoruz!
                    tbody.innerHTML += `
                        <tr>
                            <td>${c.uye}</td>
                            <td>${c.kitap}</td>
                            <td class="text-warning">${c.gecikme}</td>
                            <td class="text-danger fw-bold">${c.tutar} TL</td>
                            <td>${durumHtml}</td>
                            <td>${islemButonu}</td>
                        </tr>`;
                });
            }
            new bootstrap.Modal(document.getElementById('adminFinesModal')).show();
        }
    } catch (e) { console.error(e); }
};

// Diğerleri
window.detayGoster = function(id) {
    const k = tumKitaplar.find(x => x.kitapId === id);
    if(k) {
        document.getElementById("detailTitle").innerText = k.kitapAdi;
        document.getElementById("detailAuthor").innerText = k.yazar ? `${k.yazar.ad} ${k.yazar.soyad}` : "-";
        document.getElementById("detailStock").innerText = k.stokSayisi;
        new bootstrap.Modal(document.getElementById('bookDetailModal')).show();
    }
};

window.cikisYap = function() { localStorage.clear(); window.location.href="index.html"; };

function kimlikKontrolu() {
    if(!localStorage.getItem("jwtToken")) window.location.href = "index.html";
    const rol = localStorage.getItem("kullaniciRolu");
    document.getElementById("userInfo").innerText = rol === "ADMIN" ? "Yönetici" : "Üye";
    if(rol === "ADMIN") {
        document.getElementById("addBookCard").style.display = "block";
        document.getElementById("myLoansCard").style.display = "none";
    } else {
        document.getElementById("addBookCard").style.display = "none";
        document.getElementById("myLoansCard").style.display = "block";
    }
}

function istatistikleriGuncelle(data) {
    document.getElementById("totalBooksCount").innerText = data.length;
    document.getElementById("availableBooksCount").innerText = data.reduce((t, c) => t + (c.stokSayisi || 0), 0);
}

function formlariHazirla() {
    // Login
    const lf = document.getElementById("loginForm");
    if(lf) lf.addEventListener("submit", async (e) => {
        e.preventDefault();
        const u = document.getElementById("username").value;
        const p = document.getElementById("password").value;
        try {
            const res = await fetch(`${API_URL}/auth/giris`, {
                method: "POST", headers: {"Content-Type":"application/json"},
                body: JSON.stringify({ girilenBilgi: u, sifre: p })
            });
            if(res.ok) {
                const data = await res.json();
                localStorage.setItem("jwtToken", data.token);
                localStorage.setItem("kullaniciRolu", data.rol);
                localStorage.setItem("kullaniciId", data.id);

                // YENİ: İsmi kaydet
                localStorage.setItem("adSoyad", data.adSoyad);

                window.location.href = "dashboard.html";
            } else { showPopup("Hata", "Giriş başarısız!"); }
        } catch(e) { console.error(e); }
    });

    // Kayit
    const rf = document.getElementById("registerForm");
    if(rf) rf.addEventListener("submit", async (e) => {
        e.preventDefault();
        const m = { ad: document.getElementById("regName").value, soyad: document.getElementById("regSurname").value, email: document.getElementById("regEmail").value, sifre: document.getElementById("regPassword").value };
        try {
            const res = await fetch(`${API_URL}/auth/kayit`, {
                method: "POST", headers: {"Content-Type":"application/json"},
                body: JSON.stringify(m)
            });
            if(res.ok) { showToast("Kayıt Başarılı!"); setTimeout(() => window.location.href="index.html", 1500); }
            else showPopup("Hata", "Kayıt olunamadı!");
        } catch(e) { console.error(e); }
    });

    // Kitap Ekle
    const af = document.getElementById("addBookForm");
    if(af) af.addEventListener("submit", async (e) => {
        e.preventDefault();
        const b = { kitapAdi: document.getElementById("bookTitle").value, isbn: document.getElementById("bookIsbn").value, yayinYili: document.getElementById("bookYear").value, stokSayisi: document.getElementById("bookCopies").value, ozet: document.getElementById("bookSummary").value, yazar: { ad: document.getElementById("authorName").value, soyad: document.getElementById("authorSurname").value } };
        const token = localStorage.getItem("jwtToken");
        try {
            const res = await fetch(`${API_URL}/kitaplar`, { method: "POST", headers: { "Content-Type":"application/json", "Authorization": `Bearer ${token}` }, body: JSON.stringify(b) });
            if(res.ok) { showToast("Kitap Eklendi!"); af.reset(); kitaplariYukle(); }
        } catch(e) { console.error(e); }
    });
}