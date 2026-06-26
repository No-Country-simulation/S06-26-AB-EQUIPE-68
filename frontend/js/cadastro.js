import { register } from './api.js';

const SESSION_KEY = 'bitapp_usuario';

function showError(msg) {
    const el = document.getElementById('formCadastroError');
    if (el) {
        el.textContent = msg;
        el.classList.remove('hidden');
    }
}

function clearError() {
    const el = document.getElementById('formCadastroError');
    if (el) {
        el.textContent = '';
        el.classList.add('hidden');
    }
}

document.getElementById('formCadastro')?.addEventListener('submit', async (event) => {
    event.preventDefault();
    clearError();

    const nomeEl = document.getElementById('regNome');
    const emailEl = document.getElementById('regEmail');
    const passEl = document.getElementById('regPassword');

    let valid = true;

    if (!nomeEl?.value.trim()) {
        nomeEl?.classList.add('border-rose-500');
        nomeEl?.classList.remove('border-slate-800');
        valid = false;
    } else {
        nomeEl?.classList.remove('border-rose-500');
        nomeEl?.classList.add('border-slate-800');
    }

    const email = emailEl?.value.trim() || '';
    if (!email || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
        emailEl?.classList.add('border-rose-500');
        emailEl?.classList.remove('border-slate-800');
        valid = false;
    } else {
        emailEl?.classList.remove('border-rose-500');
        emailEl?.classList.add('border-slate-800');
    }

    const password = passEl?.value || '';
    if (!password || password.length < 6) {
        passEl?.classList.add('border-rose-500');
        passEl?.classList.remove('border-slate-800');
        valid = false;
    } else {
        passEl?.classList.remove('border-rose-500');
        passEl?.classList.add('border-slate-800');
    }

    if (!valid) {
        showError('Preencha todos os campos obrigatórios corretamente.');
        return;
    }

    const btn = document.getElementById('btnCadastro');
    btn.disabled = true;
    btn.innerHTML = '<span class="loader"></span> Criando...';

    try {
        const data = await register({
            nome: nomeEl.value.trim(),
            email: email,
            password: password,
            cidade: document.getElementById('regCidade')?.value || '',
            whatsapp: document.getElementById('regWhatsapp')?.value.trim() || '',
            nivelProfissional: document.getElementById('regNivel')?.value || 'Estudante',
            areaTecnologia: document.getElementById('regArea')?.value || 'Web',
            competenciasAtuais: document.getElementById('regSkills')?.value.trim() || '',
        });
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
        showError(err.message || 'Erro ao criar conta. Tente novamente.');
        btn.disabled = false;
        btn.textContent = 'Criar Conta';
    }
});

['regNome', 'regEmail', 'regPassword'].forEach(id => {
    document.getElementById(id)?.addEventListener('input', function () {
        this.classList.remove('border-rose-500');
        this.classList.add('border-slate-800');
        clearError();
    });
});
