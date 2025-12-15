// --- AYARLAR ---
const API_URL = "http://localhost:8080/api";
let tumKitaplar = [];

// --- YARDIMCI FONKSIYONLAR ---
function showToast(baslik, icon = 'success') {
    Swal.fire({ title: baslik, icon: icon, toast: true, position: 'top-end', showConfirmButton: false, timer: 3000, timerProgressBar: true, background: '#1e1e1e', color: '#fff' });
}

function showPopup(baslik, metin, icon = 'error') {
    Swal.fire({ title: baslik, text: metin, icon: icon, background: '#1e1e1e', color: '#fff', confirmButtonColor: '#3085d6' });
}

// --- SAYFA YUKLENDIGINDE ---
document.addEventListener("DOMContentLoaded", () => {
    if (window.location.pathname.includes("dashboard.html")) {
        kimlikKontrolu();
        kitaplariYukle();
        if(localStorage.getItem("kullaniciRolu") === "UYE") aktifOdunclerimiYukle();

        const aramaKutusu = document.getElementById("searchInput");
        if (aramaKutusu) {
            aramaKutusu.addEventListener("input", (e) => {
                const sorgu = e.target.value.toLowerCase().trim();
                if (!sorgu) { tabloyuCiz(tumKitaplar); return; }
                const filtrelenmis = tumKitaplar.filter(k =>
                    k.kitapAdi.toLowerCase().includes(sorgu) ||
                    (k.yazar && k.yazar.ad.toLowerCase().includes(sorgu)) ||
                    (k.yazar && k.yazar.soyad.toLowerCase().includes(sorgu))
                );
                tabloyuCiz(filtrelenmis);
            });
        }
    }
    formlariHazirla();
});

// --- KITAP ISLEMLERI ---
async function kitaplariYukle() {
    const token = localStorage.getItem("jwtToken");
    try {
        const response = await fetch(`${API_URL}/kitaplar`, { headers: { "Authorization": `Bearer ${token}` } });
        if (response.ok) {
            tumKitaplar = await response.json();
            tabloyuCiz(tumKitaplar);
            istatistikleriGuncelle(tumKitaplar);
        }
    } catch (e) { console.error(e); }
}

function tabloyuCiz(kitaplar) {
    const govde = document.getElementById("bookListBody");
    const rol = localStorage.getItem("kullaniciRolu");
    govde.innerHTML = "";

    if (kitaplar.length === 0) { govde.innerHTML = `<tr><td colspan="4" class="text-center py-4 text-muted">Sonuç bulunamadı...</td></tr>`; return; }

    kitaplar.forEach(k => {
        const yazar = k.yazar ? `${k.yazar.ad} ${k.yazar.soyad}` : "Bilinmiyor";
        let btn = "";
        if (rol === "ADMIN") btn = `<button class="btn btn-outline-danger" onclick="kitapSil(${k.kitapId})"><i class="bi bi-trash"></i></button>`;
        else btn = k.stokSayisi > 0 ? `<button class="btn btn-outline-info" onclick="kitapOduncAl(${k.kitapId})">Al</button>` : `<button class="btn btn-secondary" disabled>Yok</button>`;

        govde.innerHTML += `<tr class="align-middle"><td class="fw-bold text-white fs-5">${k.kitapAdi}</td><td>${yazar}</td><td class="text-center"><span class="badge ${k.stokSayisi > 0 ? 'bg-success' : 'bg-danger'}">${k.stokSayisi}</span></td><td class="text-end"><button class="btn btn-outline-secondary me-2" onclick="kitapDetay(${k.kitapId})">Detay</button>${btn}</td></tr>`;
    });
}

// --- ODUNC VE IADE ---
window.kitapOduncAl = async function(kitapId) {
    if (!(await Swal.fire({ title: 'Onaylıyor musunuz?', text: "14 gün süreniz var. Gecikme bedeli günlük 5 TL.", icon: 'info', showCancelButton: true, confirmButtonText: 'Evet, Al', background: '#1e1e1e', color: '#fff' })).isConfirmed) return;

    const token = localStorage.getItem("jwtToken");
    const uyeId = localStorage.getItem("kullaniciId");
    try {
        const res = await fetch(`${API_URL}/odunc/al`, { method: "POST", headers: { "Content-Type": "application/json", "Authorization": `Bearer ${token}` }, body: JSON.stringify({ uyeId: uyeId, kitapId: kitapId }) });
        if (res.ok) { showToast("Kitap alındı!"); kitaplariYukle(); aktifOdunclerimiYukle(); } else showPopup("Hata", await res.text());
    } catch (e) { console.error(e); }
};

window.kitapIadeEt = async function(kitapId) {
    if (!(await Swal.fire({ title: 'İade edilsin mi?', icon: 'question', showCancelButton: true, confirmButtonText: 'İade Et', background: '#1e1e1e', color: '#fff' })).isConfirmed) return;

    const token = localStorage.getItem("jwtToken");
    const uyeId = localStorage.getItem("kullaniciId");
    try {
        const res = await fetch(`${API_URL}/odunc/iade`, { method: "POST", headers: { "Content-Type": "application/json", "Authorization": `Bearer ${token}` }, body: JSON.stringify({ uyeId: uyeId, kitapId: kitapId }) });
        if (res.ok) { showToast("İade başarılı!"); kitaplariYukle(); aktifOdunclerimiYukle(); } else showPopup("Hata", await res.text());
    } catch (e) { console.error(e); }
};

window.kitapSil = async function(id) {
    if (!(await Swal.fire({ title: 'Silinsin mi?', text: "Geri alınamaz!", icon: 'warning', showCancelButton: true, confirmButtonText: 'Sil', confirmButtonColor: '#d33', background: '#1e1e1e', color: '#fff' })).isConfirmed) return;

    const token = localStorage.getItem("jwtToken");
    try {
        const res = await fetch(`${API_URL}/kitaplar/${id}`, { method: "DELETE", headers: { "Authorization": `Bearer ${token}` } });
        if (res.ok) { showToast("Kitap silindi."); kitaplariYukle(); } else showPopup("Hata", await res.text());
    } catch (e) { console.error(e); }
};

// --- YAN PANEL ---
async function aktifOdunclerimiYukle() {
    const token = localStorage.getItem("jwtToken");
    const uyeId = localStorage.getItem("kullaniciId");
    try {
        const res = await fetch(`${API_URL}/odunc/uye/${uyeId}/aktif`, { headers: { "Authorization": `Bearer ${token}` } });
        if (res.ok) {
            const data = await res.json();
            const govde = document.getElementById("myLoansBody");
            govde.innerHTML = "";
            if (data.length === 0) { govde.innerHTML = "<tr><td colspan='3' class='text-center text-muted'>Ödünç kitap yok.</td></tr>"; return; }
            data.forEach(d => { govde.innerHTML += `<tr class="align-middle"><td class="text-white">${d.kitapAdi}</td><td>${d.sonTeslimTarihi ? d.sonTeslimTarihi.substring(0,10) : "-"}</td><td class="text-end"><button class="btn btn-xs btn-outline-success" onclick="kitapIadeEt(${d.kitapId})">İade</button></td></tr>`; });
        }
    } catch (e) { console.error(e); }
}

// --- MODALLAR ---
window.profilAc = async function() {
    const rol = localStorage.getItem("kullaniciRolu");
    if (rol === "ADMIN") {
        document.getElementById("profName").innerText = "Sistem Yöneticisi";
        document.getElementById("profUser").innerText = "@admin";
        document.getElementById("profEmail").innerText = "admin@nexus.com";
        document.querySelectorAll("#profileModal li").forEach(li => { if(li.innerText.includes("Borç")) li.style.setProperty("display", "none", "important"); });
        document.getElementById("fineSectionContainer").style.setProperty("display", "none", "important");
        new bootstrap.Modal(document.getElementById('profileModal')).show();
        return;
    }
    const token = localStorage.getItem("jwtToken");
    const uyeId = localStorage.getItem("kullaniciId");
    try {
        const res = await fetch(`${API_URL}/uyeler/${uyeId}/profil`, { headers: { "Authorization": `Bearer ${token}` } });
        if (res.ok) {
            const d = await res.json();
            document.getElementById("profName").innerText = `${d.ad} ${d.soyad}`;
            document.getElementById("profUser").innerText = `@Uye${uyeId}`;
            document.getElementById("profEmail").innerText = d.email;
            document.querySelectorAll("#profileModal li").forEach(li => { if(li.innerText.includes("Borç")) li.style.display = "flex"; });

            const debtEl = document.getElementById("profDebt");
            debtEl.innerText = `${d.toplamBorc} TL`;
            d.toplamBorc > 0 ? debtEl.classList.add("text-danger") : debtEl.classList.remove("text-danger");

            const list = document.getElementById("fineList");
            const cont = document.getElementById("fineSectionContainer");
            list.innerHTML = "";
            if (d.cezaDetaylari && d.cezaDetaylari.length > 0) {
                cont.style.display = "block";
                d.cezaDetaylari.forEach(f => list.innerHTML += `<li class="list-group-item bg-transparent text-danger"><i class="bi bi-exclamation-circle me-2"></i><strong>${f.kitapAdi}</strong><br><span class="ms-4 small">${f.gecikenGun} gün: <b>${f.tutar} TL</b></span></li>`);
            } else cont.style.display = "none";

            new bootstrap.Modal(document.getElementById('profileModal')).show();
        }
    } catch (e) { console.error(e); }
};

window.gecmisAc = async function() {
    const token = localStorage.getItem("jwtToken");
    const uyeId = localStorage.getItem("kullaniciId");
    try {
        const res = await fetch(`${API_URL}/odunc/uye/${uyeId}/gecmis`, { headers: { "Authorization": `Bearer ${token}` } });
        if (res.ok) {
            const data = await res.json();
            const govde = document.getElementById("historyTableBody");
            govde.innerHTML = "";
            data.forEach(d => {
                let durum = d.durum === "IADE_EDILDI" ? '<span class="badge bg-success">İade Edildi</span>' : '<span class="badge bg-warning text-dark">Aktif</span>';
                govde.innerHTML += `<tr><td>${d.kitapAdi}</td><td>${d.alisTarihi.substring(0,10)}</td><td>${d.iadeTarihi ? d.iadeTarihi.substring(0,10) : "-"}</td><td>${durum}</td></tr>`;
            });
            new bootstrap.Modal(document.getElementById('historyModal')).show();
        }
    } catch (e) { console.error(e); }
};

window.cezalarAc = async function() {
    const token = localStorage.getItem("jwtToken");
    try {
        // Backend: OduncController icine bu endpoint eklenecek
        const res = await fetch(`${API_URL}/odunc/admin/cezalar`, { headers: { "Authorization": `Bearer ${token}` } });
        if(res.ok) {
            const data = await res.json();
            const govde = document.getElementById("adminFinesBody");
            govde.innerHTML = "";
            if(data.length === 0) govde.innerHTML = "<tr><td colspan='5' class='text-center py-4 text-success'>Ödenmemiş ceza yok!</td></tr>";
            else data.forEach(c => {
                govde.innerHTML += `<tr><td>${c.uyeAdSoyad}</td><td>${c.kitapAdi}</td><td class="text-warning">${c.gecikenGun} Gün</td><td class="fw-bold text-danger">${c.toplamTutar} TL</td><td><button class="btn btn-sm btn-success" onclick="cezaTahsilEt(${c.oduncId})">Tahsil Et</button></td></tr>`;
            });
            new bootstrap.Modal(document.getElementById('adminFinesModal')).show();
        }
    } catch(e) { console.error(e); }
};

window.cezaTahsilEt = async function(oduncId) {
    if(!confirm("Ödeme alındı mı?")) return;
    const token = localStorage.getItem("jwtToken");
    try {
        const res = await fetch(`${API_URL}/odunc/ceza-ode/${oduncId}`, { method: "POST", headers: { "Authorization": `Bearer ${token}` } });
        if(res.ok) { showToast("Tahsilat başarılı!"); cezalarAc(); }
    } catch(e) { console.error(e); }
};

window.kitapDetay = function(id) {
    const k = tumKitaplar.find(b => b.kitapId === id);
    if (!k) return;
    document.getElementById("detailTitle").innerText = k.kitapAdi;
    document.getElementById("detailAuthor").innerText = k.yazar ? `${k.yazar.ad} ${k.yazar.soyad}` : "-";
    document.getElementById("detailIsbn").innerText = k.isbn || "-";
    document.getElementById("detailYear").innerText = k.basimYili || "-";
    document.getElementById("detailStock").innerText = k.stokSayisi;
    document.getElementById("detailSummary").innerText = k.ozet || "Özet yok.";
    new bootstrap.Modal(document.getElementById('bookDetailModal')).show();
};

window.openPaymentModal = function() {
    const borc = document.getElementById("profDebt").innerText;
    if (borc === "0 TL") { Swal.fire({ icon: 'info', title: 'Borcunuz Yok', background: '#191919', color: '#fff' }); return; }
    bootstrap.Modal.getInstance(document.getElementById('profileModal')).hide();
    new bootstrap.Modal(document.getElementById('paymentModal')).show();
};

window.processPayment = function() {
    bootstrap.Modal.getInstance(document.getElementById('paymentModal')).hide();
    Swal.fire({ icon: 'success', title: 'Talep Alındı', text: 'Yönetici onaylayınca borcunuz silinecektir.', background: '#191919', color: '#fff' });
};

// --- AUTH ---
function kimlikKontrolu() {
    const t = localStorage.getItem("jwtToken");
    if (!t) { window.location.href = "index.html"; return; }
    const rol = localStorage.getItem("kullaniciRolu");
    document.getElementById("userInfo").innerText = rol === "ADMIN" ? "Yönetici" : "Üye";
    if (rol === "ADMIN") { document.getElementById("addBookCard").style.display = "block"; document.getElementById("myLoansCard").style.display = "none"; }
    else { document.getElementById("addBookCard").style.display = "none"; document.getElementById("myLoansCard").style.display = "block"; }
}

function istatistikleriGuncelle(kitaplar) {
    document.getElementById("totalBooksCount").innerText = kitaplar.length;
    document.getElementById("availableBooksCount").innerText = kitaplar.reduce((toplam, k) => toplam + k.stokSayisi, 0);
}

function formlariHazirla() {
    const loginForm = document.getElementById("loginForm");
    if(loginForm) loginForm.addEventListener("submit", async(e)=>{
        e.preventDefault();
        const u = document.getElementById("username").value;
        const p = document.getElementById("password").value;
        try{
            const res = await fetch(`${API_URL}/auth/giris`,{ method:"POST", headers:{"Content-Type":"application/json"}, body:JSON.stringify({girilenBilgi:u, sifre:p}) });
            if(res.ok){ const d=await res.json(); localStorage.setItem("jwtToken",d.token); localStorage.setItem("kullaniciRolu",d.rol); localStorage.setItem("kullaniciId",d.id); window.location.href="dashboard.html";}
            else showPopup("Hata","Giriş başarısız.");
        }catch(e){showPopup("Hata","Sunucu hatası.");}
    });

    const regForm = document.getElementById("registerForm");
    if(regForm) regForm.addEventListener("submit", async(e)=>{
        e.preventDefault();
        const m={ ad: document.getElementById("regName").value, soyad: document.getElementById("regSurname").value, email: document.getElementById("regEmail").value, sifre: document.getElementById("regPassword").value };
        try{
            const res=await fetch(`${API_URL}/auth/kayit`,{method:"POST",headers:{"Content-Type":"application/json"},body:JSON.stringify(m)});
            if(res.ok){ showToast("Kayıt başarılı!"); setTimeout(()=>window.location.href="index.html",1500);}
            else showPopup("Hata","Kayıt başarısız.");
        }catch(e){showPopup("Hata","Sunucu hatası.");}
    });

    const addForm = document.getElementById("addBookForm");
    if(addForm) addForm.addEventListener("submit", async(e)=>{
        e.preventDefault();
        const b={ kitapAdi: document.getElementById("bookTitle").value, isbn: document.getElementById("bookIsbn").value, basimYili: document.getElementById("bookYear").value, stokSayisi: document.getElementById("bookCopies").value, ozet: document.getElementById("bookSummary").value, yazar: { ad: document.getElementById("authorName").value, soyad: document.getElementById("authorSurname").value } };
        try{
            const res=await fetch(`${API_URL}/kitaplar`,{ method:"POST", headers:{"Content-Type":"application/json","Authorization":`Bearer ${localStorage.getItem("jwtToken")}`}, body:JSON.stringify(b)});
            if(res.ok){ showToast("Kitap eklendi!"); addForm.reset(); kitaplariYukle(); } else showPopup("Hata","Eklenemedi.");
        }catch(e){console.error(e);}
    });
}

window.cikisYap = function(){ localStorage.clear(); window.location.href="index.html"; };
window.cezaDetayAcKapa = function() { const d = document.getElementById("fineDetailsList"); d.style.display = d.style.display === "none" ? "block" : "none"; };