import { login, logout } from './api.js';

const SESSION_KEY = 'bitapp_usuario';

function getUsuarioLogado() {
    const raw = localStorage.getItem(SESSION_KEY);
    return raw ? JSON.parse(raw) : null;
}

async function handleLogout() {
    await logout();
    window.location.href = 'index.html';
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

document.getElementById('togglePassword')?.addEventListener('click', function() {
    const input = document.getElementById('loginPassword');
    if (!input) return;
    const isPassword = input.type === 'password';
    input.type = isPassword ? 'text' : 'password';
    this.setAttribute('aria-label', isPassword ? 'Ocultar senha' : 'Mostrar senha');
    this.innerHTML = isPassword
        ? '<svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.88 9.88l-3.29-3.29m7.532 7.532l3.29 3.29M3 3l3.59 3.59m0 0A9.953 9.953 0 0112 5c4.478 0 8.268 2.943 9.543 7a10.025 10.025 0 01-4.132 5.411m0 0L21 21"/></svg>'
        : '<svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"/></svg>';
});

(function init() {
    var msg = new URLSearchParams(window.location.search).get('msg');
    if (msg === 'auth_required') {
        var authMsg = document.getElementById('authMessage');
        if (authMsg) authMsg.classList.remove('hidden');
        window.history.replaceState({}, '', 'index.html');
    }
})();
