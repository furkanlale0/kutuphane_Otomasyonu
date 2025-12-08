const API_URL = "http://localhost:8080/api";

document.addEventListener("DOMContentLoaded", () => {
    if (window.location.pathname.includes("dashboard.html")) {
        checkAuth();
        loadBooks();
        if(localStorage.getItem("userRole") === "MEMBER") {
            loadMyLoans();
        }
    }
    setupForms();
});

// --- YENİ EKLENEN İADE FONKSİYONU ---
window.returnBook = async function(bookId) {
    if(!confirm("Kitabı iade etmek istiyor musunuz?")) return;

    const token = localStorage.getItem("jwtToken");
    const memberId = localStorage.getItem("userId");

    try {
        const response = await fetch(`${API_URL}/loans/return`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${token}`
            },
            body: JSON.stringify({ memberId, bookId })
        });

        if (response.ok) {
            alert("Kitap iade edildi!");
            loadBooks();   // Ana listede stok artsın
            loadMyLoans(); // Tablodan kitap silinsin
        } else {
            const msg = await response.text();
            alert("Hata: " + msg);
        }
    } catch (error) {
        console.error(error);
        alert("İşlem başarısız.");
    }
};

// --- ÖDÜNÇ LİSTESİ (BUTONLU HALİ) ---
async function loadMyLoans() {
    const token = localStorage.getItem("jwtToken");
    const memberId = localStorage.getItem("userId");

    try {
        const response = await fetch(`${API_URL}/loans/my-loans?memberId=${memberId}`, {
            headers: { "Authorization": `Bearer ${token}` }
        });

        if (response.ok) {
            const loans = await response.json();
            const tableBody = document.getElementById("myLoansBody");
            tableBody.innerHTML = "";

            if (loans.length === 0) {
                tableBody.innerHTML = "<tr><td colspan='5' class='text-center'>Şu an ödünç kitabınız yok.</td></tr>";
                return;
            }

            loans.forEach(loan => {
                const row = `
                    <tr>
                        <td>${loan.bookTitle}</td>
                        <td>${loan.borrowDate.substring(0,10)}</td>
                        <td>${loan.dueDate.substring(0,10)}</td>
                        <td><span class="badge bg-warning text-dark">${loan.status}</span></td>
                        <td>
                            <button class="btn btn-sm btn-outline-danger" onclick="returnBook(${loan.bookId})">İade Et</button>
                        </td>
                    </tr>
                `;
                tableBody.innerHTML += row;
            });
        }
    } catch (error) { console.error(error); }
}

// --- DİĞER KODLAR (AYNI KALDI) ---

async function borrowBook(bookId) {
    const token = localStorage.getItem("jwtToken");
    const memberId = localStorage.getItem("userId");
    try {
        const response = await fetch(`${API_URL}/loans/borrow`, {
            method: "POST",
            headers: { "Content-Type": "application/json", "Authorization": `Bearer ${token}` },
            body: JSON.stringify({ memberId, bookId })
        });
        if (response.ok) {
            alert("Kitap başarıyla ödünç alındı!");
            loadBooks();
            loadMyLoans();
        } else {
            const msg = await response.text();
            alert("Hata: " + msg);
        }
    } catch (error) { alert("İşlem başarısız."); }
};

async function loadBooks() {
    const token = localStorage.getItem("jwtToken");
    const role = localStorage.getItem("userRole");
    try {
        const response = await fetch(`${API_URL}/books`, {
            headers: { "Authorization": `Bearer ${token}` }
        });
        if (response.ok) {
            const books = await response.json();
            const tableBody = document.getElementById("bookListBody");
            tableBody.innerHTML = "";
            books.forEach(book => {
                const authorName = book.author ? `${book.author.name} ${book.author.surname}` : "Bilinmiyor";
                let actionBtn = "";
                if (role === "ADMIN") {
                    actionBtn = `<button class="btn btn-sm btn-danger" onclick="deleteBook(${book.bookId})">Sil</button>`;
                } else {
                    actionBtn = `<button class="btn btn-sm btn-info" onclick="borrowBook(${book.bookId})">Ödünç Al</button>`;
                }
                const row = `<tr>
                    <td>${book.bookId}</td>
                    <td>${book.title}</td>
                    <td>${authorName}</td>
                    <td>${book.isbn || '-'}</td>
                    <td>${book.copies}</td>
                    <td>${actionBtn}</td>
                </tr>`;
                tableBody.innerHTML += row;
            });
        }
    } catch (e) { console.error(e); }
}

function checkAuth() {
    const token = localStorage.getItem("jwtToken");
    if (!token) { window.location.href = "index.html"; return; }

    const role = localStorage.getItem("userRole");
    const addBookCard = document.getElementById("addBookCard");
    const myLoansCard = document.getElementById("myLoansCard");

    if (role === "ADMIN") {
        if (addBookCard) addBookCard.style.display = "block";
        if (myLoansCard) myLoansCard.style.display = "none";
    } else {
        if (addBookCard) addBookCard.style.display = "none";
        if (myLoansCard) myLoansCard.style.display = "block";
    }
}

function setupForms() {
    const loginForm = document.getElementById("loginForm");
    if (loginForm) {
        loginForm.addEventListener("submit", async (e) => {
            e.preventDefault();
            const username = document.getElementById("username").value;
            const password = document.getElementById("password").value;
            const response = await fetch(`${API_URL}/auth/login`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ username, password })
            });
            if (response.ok) {
                const data = await response.json();
                localStorage.setItem("jwtToken", data.token);
                localStorage.setItem("userRole", data.role);
                localStorage.setItem("userId", data.id);
                window.location.href = "dashboard.html";
            } else alert("Giriş başarısız!");
        });
    }

    const addBookForm = document.getElementById("addBookForm");
    if (addBookForm) {
        addBookForm.addEventListener("submit", async (e) => {
            e.preventDefault();
            const newBook = {
                title: document.getElementById("bookTitle").value,
                isbn: document.getElementById("bookIsbn").value,
                year: document.getElementById("bookYear").value,
                copies: document.getElementById("bookCopies").value,
                author: { name: document.getElementById("authorName").value, surname: document.getElementById("authorSurname").value }
            };
            const token = localStorage.getItem("jwtToken");
            const response = await fetch(`${API_URL}/books`, {
                method: "POST",
                headers: { "Content-Type": "application/json", "Authorization": `Bearer ${token}` },
                body: JSON.stringify(newBook)
            });
            if (response.ok) {
                alert("Kitap eklendi!");
                addBookForm.reset();
                loadBooks();
            }
        });
    }

    const registerForm = document.getElementById("registerForm");
    if (registerForm) {
        registerForm.addEventListener("submit", async (e) => {
            e.preventDefault();
            const member = {
                username: document.getElementById("regUsername").value,
                name: document.getElementById("regName").value,
                surname: document.getElementById("regSurname").value,
                email: document.getElementById("regEmail").value,
                phone: document.getElementById("regPhone").value,
                password: document.getElementById("regPassword").value
            };
            const response = await fetch(`${API_URL}/auth/register`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(member)
            });
            if (response.ok) {
                alert("Kayıt başarılı!");
                window.location.href = "index.html";
            } else alert("Hata");
        });
    }
}

window.logout = function() { localStorage.clear(); window.location.href = "index.html"; };
window.deleteBook = async function(id) {
    if(!confirm("Silmek istediğine emin misin?")) return;
    const token = localStorage.getItem("jwtToken");
    const response = await fetch(`${API_URL}/books/${id}`, {
        method: "DELETE",
        headers: { "Authorization": `Bearer ${token}` }
    });
    if (response.ok) { loadBooks(); }
    else { const msg = await response.text(); alert("HATA: " + msg); }
};