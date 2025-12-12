const API_URL = "http://localhost:8080/api";
let allBooks = [];

function showToast(title, icon = 'success') {
    Swal.fire({ title: title, icon: icon, toast: true, position: 'top-end', showConfirmButton: false, timer: 3000, timerProgressBar: true, background: '#1e1e1e', color: '#fff' });
}

function showPopup(title, text, icon = 'error') {
    Swal.fire({ title: title, text: text, icon: icon, background: '#1e1e1e', color: '#fff', confirmButtonColor: '#3085d6' });
}

document.addEventListener("DOMContentLoaded", () => {
    if (window.location.pathname.includes("dashboard.html")) {
        checkAuth();
        loadBooks();
        if(localStorage.getItem("userRole") === "MEMBER") loadMyLoans();

        const searchInput = document.getElementById("searchInput");
        if (searchInput) {
            searchInput.addEventListener("input", (e) => {
                const query = e.target.value.toLowerCase().trim();
                if (!query) { renderBooksTable(allBooks); return; }
                const filteredBooks = allBooks.filter(book => book.title.toLowerCase().includes(query) || (book.author && book.author.name.toLowerCase().includes(query)) || (book.author && book.author.surname.toLowerCase().includes(query)));
                renderBooksTable(filteredBooks);
            });
        }
    }
    setupForms();
});

async function loadBooks() {
    const token = localStorage.getItem("jwtToken");
    try {
        const response = await fetch(`${API_URL}/books`, { headers: { "Authorization": `Bearer ${token}` } });
        if (response.ok) { allBooks = await response.json(); renderBooksTable(allBooks); updateStats(allBooks); }
    } catch (e) { console.error(e); }
}

function renderBooksTable(books) {
    const tableBody = document.getElementById("bookListBody");
    const role = localStorage.getItem("userRole");
    tableBody.innerHTML = "";
    if (books.length === 0) { tableBody.innerHTML = `<tr><td colspan="4" class="text-center py-4 text-muted fs-5">Sonuç bulunamadı...</td></tr>`; return; }
    books.forEach(book => {
        const authorName = book.author ? `${book.author.name} ${book.author.surname}` : "Bilinmiyor";
        let actionBtn = "";
        if (role === "ADMIN") actionBtn = `<button class="btn btn-outline-danger" onclick="deleteBook(${book.bookId})"><i class="bi bi-trash"></i></button>`;
        else {
            if (book.copies > 0) actionBtn = `<button class="btn btn-outline-info" onclick="borrowBook(${book.bookId})">Al</button>`;
            else actionBtn = `<button class="btn btn-secondary" disabled>Yok</button>`;
        }
        tableBody.innerHTML += `<tr class="align-middle"><td class="fw-bold text-white fs-5">${book.title}</td><td class="fs-6">${authorName}</td><td class="text-center"><span class="badge ${book.copies > 0 ? 'bg-success' : 'bg-danger'} fs-6">${book.copies}</span></td><td class="text-end"><button class="btn btn-outline-secondary me-2" onclick="showBookDetail(${book.bookId})">Detay</button>${actionBtn}</td></tr>`;
    });
}

// --- İŞLEMLER ---
window.borrowBook = async function(bookId) {
    const result = await Swal.fire({ title: 'Kütüphane Kuralları', html: `<div class="text-start fs-6"><p>1. Kitabı teslim etmek için <b>14 gün</b> süreniz vardır.</p><p>2. Geciken her gün için <b>5 TL</b> ceza uygulanır.</p><p class="text-center mt-3">Onaylıyor musunuz?</p></div>`, icon: 'info', showCancelButton: true, confirmButtonText: 'Evet, Onaylıyorum', cancelButtonText: 'Vazgeç', background: '#1e1e1e', color: '#fff', confirmButtonColor: '#28a745' });
    if (!result.isConfirmed) return;
    const token = localStorage.getItem("jwtToken");
    const memberId = localStorage.getItem("userId");
    try { const response = await fetch(`${API_URL}/loans/borrow`, { method: "POST", headers: { "Content-Type": "application/json", "Authorization": `Bearer ${token}` }, body: JSON.stringify({ memberId, bookId }) });
        if (response.ok) { showToast("Kitap ödünç alındı!"); loadBooks(); loadMyLoans(); } else { const msg = await response.text(); showPopup("Hata", msg); } } catch (e) { console.error(e); }
};

window.returnBook = async function(bookId) {
    const result = await Swal.fire({ title: 'İade edilsin mi?', icon: 'question', showCancelButton: true, confirmButtonText: 'Evet, İade Et', cancelButtonText: 'İptal', background: '#1e1e1e', color: '#fff' });
    if (!result.isConfirmed) return;
    const token = localStorage.getItem("jwtToken");
    const memberId = localStorage.getItem("userId");
    try { const response = await fetch(`${API_URL}/loans/return`, { method: "POST", headers: { "Content-Type": "application/json", "Authorization": `Bearer ${token}` }, body: JSON.stringify({ memberId, bookId }) });
        if (response.ok) { showToast("İade başarılı!", "success"); loadBooks(); loadMyLoans(); } else { const msg = await response.text(); showPopup("Hata", msg); } } catch (e) { console.error(e); }
};

window.deleteBook = async function(id) {
    const result = await Swal.fire({ title: 'Kitap Silinsin mi?', text: "Bu işlem geri alınamaz!", icon: 'warning', showCancelButton: true, confirmButtonColor: '#d33', confirmButtonText: 'Evet, Sil', background: '#1e1e1e', color: '#fff' });
    if (!result.isConfirmed) return;
    const token = localStorage.getItem("jwtToken");
    try { const response = await fetch(`${API_URL}/books/${id}`, { method: "DELETE", headers: { "Authorization": `Bearer ${token}` } });
        if (response.ok) { showToast("Kitap silindi."); loadBooks(); } else { const msg = await response.text(); showPopup("Hata", msg); } } catch (e) { console.error(e); }
};

// --- MODALLAR ---
window.openHistoryModal = async function() {
    const token = localStorage.getItem("jwtToken");
    const memberId = localStorage.getItem("userId");
    try { const response = await fetch(`${API_URL}/loans/history?memberId=${memberId}`, { headers: { "Authorization": `Bearer ${token}` } });
        if (response.ok) { const history = await response.json(); const tbody = document.getElementById("historyTableBody"); tbody.innerHTML = "";
            if (history.length === 0) { tbody.innerHTML = "<tr><td colspan='4' class='text-center text-muted'>Henüz bir işlem geçmişiniz yok.</td></tr>"; } else {
                history.forEach(item => { const borrowDate = item.borrowDate ? item.borrowDate.substring(0, 10) : "-"; const returnDate = item.returnDate ? item.returnDate.substring(0, 10) : "<span class='text-warning'>Devam Ediyor</span>"; let statusBadge = item.status === "RETURNED" ? `<span class="badge bg-success">İade Edildi</span>` : `<span class="badge bg-warning text-dark">Aktif</span>`; tbody.innerHTML += `<tr><td>${item.bookTitle}</td><td>${borrowDate}</td><td>${returnDate}</td><td>${statusBadge}</td></tr>`; }); }
            new bootstrap.Modal(document.getElementById('historyModal')).show(); } } catch (e) { console.error(e); showPopup("Hata", "Geçmiş yüklenemedi."); }
};

window.openFinesModal = async function() {
    const token = localStorage.getItem("jwtToken");
    try { const response = await fetch(`${API_URL}/loans/admin/fines`, { headers: { "Authorization": `Bearer ${token}` } });
        if (response.ok) { const fines = await response.json(); const tbody = document.getElementById("adminFinesBody"); tbody.innerHTML = "";
            if (fines.length === 0) { tbody.innerHTML = "<tr><td colspan='6' class='text-center text-success py-4'>Harika! Ödenmemiş ceza bulunmuyor.</td></tr>"; } else {
                fines.forEach(f => { const statusText = f.isReturned ? `<span class="badge bg-secondary">İade Edildi (Borçlu)</span>` : `<span class="badge bg-danger">Hala Üyede!</span>`; tbody.innerHTML += `<tr id="fine-row-${f.borrowId}"><td class="fw-bold">${f.memberName}</td><td>${f.bookTitle}</td><td class="text-center text-warning">${f.days} Gün</td><td class="text-center">${statusText}</td><td class="text-end fw-bold text-danger">${f.amount} TL</td><td class="text-end"><button class="btn btn-sm btn-success" onclick="payFine(${f.borrowId})"><i class="bi bi-cash-stack me-1"></i> Tahsil Et</button></td></tr>`; }); }
            new bootstrap.Modal(document.getElementById('adminFinesModal')).show(); } } catch (e) { console.error(e); }
};

window.payFine = async function(borrowId) {
    if(!confirm("Bu cezanın ödendiğini onaylıyor musunuz?")) return;
    const token = localStorage.getItem("jwtToken");
    try { const response = await fetch(`${API_URL}/loans/pay-fine/${borrowId}`, { method: "POST", headers: { "Authorization": `Bearer ${token}` } });
        if (response.ok) { showToast("Tahsilat Başarılı!"); const row = document.getElementById(`fine-row-${borrowId}`); if(row) { row.classList.add("table-success"); row.style.opacity = "0.5"; row.cells[5].innerHTML = `<i class="bi bi-check-circle-fill text-success fs-4"></i>`; } } else { showPopup("Hata", "İşlem yapılamadı."); } } catch (e) { console.error(e); }
};

window.showBookDetail = function(bookId) {
    const book = allBooks.find(b => b.bookId === bookId);
    if (!book) return;
    document.getElementById("detailTitle").innerText = book.title; document.getElementById("detailAuthor").innerText = book.author ? `${book.author.name} ${book.author.surname}` : "-"; document.getElementById("detailIsbn").innerText = book.isbn || "Belirtilmemiş"; document.getElementById("detailYear").innerText = book.year || "-"; document.getElementById("detailStock").innerText = book.copies; document.getElementById("detailSummary").innerText = book.summary || "Bu kitap için henüz bir özet girilmemiş.";
    new bootstrap.Modal(document.getElementById('bookDetailModal')).show();
};

// --- MODAL: PROFİL (GÜÇLENDİRİLMİŞ - KESİN ÇÖZÜM) ---
window.openProfileModal = async function() {
    const role = localStorage.getItem("userRole");

    // --- SENARYO 1: ADMIN GİRİŞİ ---
    if (role === "ADMIN") {
        document.getElementById("profName").innerText = "Sistem Yöneticisi";
        document.getElementById("profUser").innerText = "@admin";
        document.getElementById("profEmail").innerText = "admin@nexus.com";

        // --- KESİN GİZLEME KODU BAŞLANGICI ---
        // ID'ye güvenmiyoruz. Listede "Borç" yazan satırı bulup zorla siliyoruz.
        const listItems = document.querySelectorAll("#profileModal li");
        listItems.forEach(item => {
            if (item.innerText.includes("Borç") || item.innerText.includes("Ödemeye Geç")) {
                item.style.setProperty("display", "none", "important"); // !important ile zorla gizle
            }
        });
        // --- KESİN GİZLEME KODU BİTİŞİ ---

        // Ceza detaylarını da gizle
        const fineSection = document.getElementById("fineSectionContainer");
        if(fineSection) fineSection.style.setProperty("display", "none", "important");

        new bootstrap.Modal(document.getElementById('profileModal')).show();
        return;
    }

    // --- SENARYO 2: ÜYE GİRİŞİ ---
    const memberId = localStorage.getItem("userId");
    const token = localStorage.getItem("jwtToken");

    try {
        const response = await fetch(`${API_URL}/members/${memberId}/profile`, {
            headers: { "Authorization": `Bearer ${token}` }
        });

        if (response.ok) {
            const data = await response.json();

            document.getElementById("profName").innerText = `${data.name} ${data.surname}`;
            document.getElementById("profUser").innerText = `@${data.username}`;
            document.getElementById("profEmail").innerText = data.email;

            // --- ÜYE İSE TEKRAR GÖSTER ---
            // Gizlenen satırları geri açıyoruz
            const listItems = document.querySelectorAll("#profileModal li");
            listItems.forEach(item => {
                if (item.innerText.includes("Borç")) {
                    item.style.display = "flex"; // Tekrar görünür yap
                }
            });

            const debtEl = document.getElementById("profDebt");
            if(debtEl) {
                debtEl.innerText = `${data.totalFine} TL`;
                if(data.totalFine > 0) debtEl.classList.add("text-danger");
                else debtEl.classList.remove("text-danger");
            }

            // Ceza Detayları
            const container = document.getElementById("fineSectionContainer");
            const list = document.getElementById("fineList");
            const detailsDiv = document.getElementById("fineDetailsList");

            list.innerHTML = "";
            if(detailsDiv) detailsDiv.style.display = "none";

            if (container) {
                if (data.fineDetails && data.fineDetails.length > 0) {
                    container.style.display = "block";
                    data.fineDetails.forEach(f => {
                        list.innerHTML += `
                            <li class="list-group-item bg-transparent text-danger border-danger border-opacity-25">
                                <i class="bi bi-exclamation-circle me-2"></i><strong>${f.bookTitle}</strong><br>
                                <span class="ms-4 small">${f.days} gün gecikme: <b>${f.amount} TL</b></span>
                            </li>`;
                    });
                } else {
                    container.style.display = "none";
                }
            }

            new bootstrap.Modal(document.getElementById('profileModal')).show();
        }
    } catch (e) { console.error(e); }
};

window.toggleFineDetails = function() {
    const detailsDiv = document.getElementById("fineDetailsList");
    if (detailsDiv.style.display === "none") detailsDiv.style.display = "block"; else detailsDiv.style.display = "none";
};

// --- DİĞER ---
async function loadMyLoans() {
    const token = localStorage.getItem("jwtToken"); const memberId = localStorage.getItem("userId");
    try { const response = await fetch(`${API_URL}/loans/my-loans?memberId=${memberId}`, { headers: { "Authorization": `Bearer ${token}` } });
        if (response.ok) { const loans = await response.json(); const tableBody = document.getElementById("myLoansBody"); tableBody.innerHTML = "";
            if (loans.length === 0) { tableBody.innerHTML = "<tr><td colspan='3' class='text-center text-muted'>Şu an ödünç kitap yok.</td></tr>"; return; }
            loans.forEach(loan => { tableBody.innerHTML += `<tr class="align-middle"><td class="text-white">${loan.bookTitle}</td><td>${loan.dueDate ? loan.dueDate.substring(0,10) : "-"}</td><td class="text-end"><button class="btn btn-xs btn-outline-success" onclick="returnBook(${loan.bookId})" style="font-size: 0.75rem;">İade</button></td></tr>`; }); } } catch (error) { console.error(error); }
}

function updateStats(books) { const t=document.getElementById("totalBooksCount"); const a=document.getElementById("availableBooksCount"); if(t&&a){t.innerText=books.length; a.innerText=books.reduce((s,b)=>s+b.copies,0);} }

function checkAuth() {
    const token = localStorage.getItem("jwtToken"); if (!token) { window.location.href = "index.html"; return; }
    const role = localStorage.getItem("userRole"); const addBookCard = document.getElementById("addBookCard"); const myLoansCard = document.getElementById("myLoansCard"); const userInfo = document.getElementById("userInfo");
    if(userInfo) userInfo.innerText = role === "ADMIN" ? "Yönetici" : "Üye";
    if (role === "ADMIN") { if (addBookCard) addBookCard.style.display = "block"; if (myLoansCard) myLoansCard.style.display = "none"; } else { if (addBookCard) addBookCard.style.display = "none"; if (myLoansCard) myLoansCard.style.display = "block"; }
}

function setupForms() {
    const loginForm = document.getElementById("loginForm"); if(loginForm) loginForm.addEventListener("submit", async(e)=>{ e.preventDefault(); const u = document.getElementById("username").value; const p = document.getElementById("password").value; try{ const r=await fetch(`${API_URL}/auth/login`,{method:"POST",headers:{"Content-Type":"application/json"},body:JSON.stringify({username:u,password:p})}); if(r.ok){ const d=await r.json(); localStorage.setItem("jwtToken",d.token); localStorage.setItem("userRole",d.role); localStorage.setItem("userId",d.id); window.location.href="dashboard.html";} else showPopup("Hata","Giriş başarısız."); }catch(e){showPopup("Hata","Sunucu hatası.");}});
    const registerForm = document.getElementById("registerForm"); if(registerForm) registerForm.addEventListener("submit", async(e)=>{ e.preventDefault(); const m={username:document.getElementById("regUsername").value, name:document.getElementById("regName").value, surname:document.getElementById("regSurname").value, email:document.getElementById("regEmail").value, phone:document.getElementById("regPhone").value, password:document.getElementById("regPassword").value}; try{const r=await fetch(`${API_URL}/auth/register`,{method:"POST",headers:{"Content-Type":"application/json"},body:JSON.stringify(m)}); if(r.ok){ showToast("Kayıt başarılı!"); setTimeout(()=>window.location.href="index.html",1500);} else showPopup("Hata","Kayıt başarısız.");}catch(e){showPopup("Hata","Sunucu hatası.");}});
    const addBookForm = document.getElementById("addBookForm"); if(addBookForm) addBookForm.addEventListener("submit", async(e)=>{ e.preventDefault(); const b={title:document.getElementById("bookTitle").value, isbn:document.getElementById("bookIsbn").value, year:document.getElementById("bookYear").value, copies:document.getElementById("bookCopies").value, summary:document.getElementById("bookSummary").value, author:{name:document.getElementById("authorName").value, surname:document.getElementById("authorSurname").value}}; const t=localStorage.getItem("jwtToken"); try{const r=await fetch(`${API_URL}/books`,{method:"POST",headers:{"Content-Type":"application/json","Authorization":`Bearer ${t}`},body:JSON.stringify(b)}); if(r.ok){ showToast("Kitap eklendi!"); addBookForm.reset(); loadBooks(); } else showPopup("Hata","Eklenemedi.");}catch(e){console.error(e);}});
}

window.openPaymentModal = function() {
    const debtText = document.getElementById("profDebt").innerText;
    if (debtText === "0 TL" || debtText === "0.0 TL") { Swal.fire({ icon: 'info', title: 'Borcunuz Yok', text: 'Şu an ödenmesi gereken bir cezanız bulunmamaktadır.', background: '#191919', color: '#fff' }); return; }
    const profileModalEl = document.getElementById('profileModal'); const profileModal = bootstrap.Modal.getInstance(profileModalEl); if (profileModal) { profileModal.hide(); }
    new bootstrap.Modal(document.getElementById('paymentModal')).show();
};

window.processPayment = function() {
    const paymentModalEl = document.getElementById('paymentModal'); const paymentModal = bootstrap.Modal.getInstance(paymentModalEl); if (paymentModal) { paymentModal.hide(); }
    Swal.fire({ icon: 'success', title: 'Bildirim Alındı', text: 'Ödemeniz yönetici tarafından kontrol edildikten sonra ceza miktarı sistemden düşürülecektir.', background: '#191919', color: '#fff', confirmButtonColor: '#198754' });
};

window.logout = function(){ localStorage.clear(); window.location.href="index.html"; };