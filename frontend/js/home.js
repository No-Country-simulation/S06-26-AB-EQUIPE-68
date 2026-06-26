import { login, logout } from './api.js';

const SESSION_KEY = 'bitapp_usuario';

function getUsuarioLogado() {
    const raw = localStorage.getItem(SESSION_KEY);
    return raw ? JSON.parse(raw) : null;
}

function handleLogout() {
    logout();
    window.location.reload();
}
window.handleLogout = handleLogout;

function showError(msg) {
    const el = document.getElementById('formLoginError');
    if (el) {
        el.textContent = msg;
        el.classList.remove('hidden');
    }
}

function clearError() {
    const el = document.getElementById('formLoginError');
    if (el) {
        el.textContent = '';
        el.classList.add('hidden');
    }
}

document.getElementById('formLogin')?.addEventListener('submit', async (event) => {
    event.preventDefault();
    clearError();

    const emailEl = document.getElementById('loginEmail');
    const passEl = document.getElementById('loginPassword');
    const emailHint = document.getElementById('loginEmailHint');
    const passHint = document.getElementById('loginPasswordHint');

    const email = emailEl?.value.trim() || '';
    const password = passEl?.value || '';

    let valid = true;

    if (!email || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
        emailEl?.classList.add('border-rose-500');
        emailEl?.classList.remove('border-slate-800');
        emailHint?.classList.remove('hidden');
        valid = false;
    } else {
        emailEl?.classList.remove('border-rose-500');
        emailEl?.classList.add('border-slate-800');
        emailHint?.classList.add('hidden');
    }

    if (!password || password.length < 6) {
        passEl?.classList.add('border-rose-500');
        passEl?.classList.remove('border-slate-800');
        passHint?.classList.remove('hidden');
        valid = false;
    } else {
        passEl?.classList.remove('border-rose-500');
        passEl?.classList.add('border-slate-800');
        passHint?.classList.add('hidden');
    }

    if (!valid) return;

    const btn = document.getElementById('btnLogin');
    btn.disabled = true;
    btn.innerHTML = '<span class="loader"></span> Entrando...';

    try {
        const data = await login({ email, password });
        localStorage.setItem(SESSION_KEY, JSON.stringify({
            id: data.userId,
            nome: data.nome,
            email: data.email,
            cidade: data.cidade || '',
            whatsapp: data.whatsapp || '',
            nivelProfissional: data.nivelProfissional || '',
            areaTecnologia: data.areaTecnologia || '',
            competenciasAtuais: data.competenciasAtuais || '',
        }));
        localStorage.setItem('bitapp_token', data.token);
        localStorage.setItem('bitapp_refresh', data.refreshToken);
        window.location.href = 'dashboard.html';
    } catch (err) {
        showError(err.message || 'E-mail ou senha incorretos.');
        btn.disabled = false;
        btn.textContent = 'Entrar';
    }
});

document.getElementById('loginEmail')?.addEventListener('input', function() {
    this.classList.remove('border-rose-500');
    this.classList.add('border-slate-800');
    document.getElementById('loginEmailHint')?.classList.add('hidden');
    clearError();
});

document.getElementById('loginPassword')?.addEventListener('input', function() {
    this.classList.remove('border-rose-500');
    this.classList.add('border-slate-800');
    document.getElementById('loginPasswordHint')?.classList.add('hidden');
    clearError();
});

(function init() {
    // Removed auto-redirect: login page is the initial landing page
})();
