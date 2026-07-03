import { updateProfile } from './api.js';
import { t } from './i18n.js';

const SESSION_KEY = 'bitapp_usuario';

function getUsuarioLogado() {
    const raw = localStorage.getItem(SESSION_KEY);
    return raw ? JSON.parse(raw) : null;
}

function showMsg(msg, type) {
    const el = document.getElementById('formPerfilMsg');
    if (!el) return;
    el.textContent = msg;
    el.className = `text-sm text-center ${type === 'error' ? 'text-rose-400' : 'text-emerald-400'}`;
    el.classList.remove('hidden');
}

function hideMsg() {
    const el = document.getElementById('formPerfilMsg');
    if (el) { el.textContent = ''; el.classList.add('hidden'); }
}

document.getElementById('formPerfil')?.addEventListener('submit', async (event) => {
    event.preventDefault();
    hideMsg();

    const nomeEl = document.getElementById('perfilNome');
    const nome = nomeEl?.value.trim() || '';
    if (!nome) {
        nomeEl?.classList.add('border-rose-500');
        nomeEl?.classList.remove('border-slate-800');
        showMsg(t('perfil.erroNome'), 'error');
        return;
    }
    nomeEl?.classList.remove('border-rose-500');
    nomeEl?.classList.add('border-slate-800');

    const btn = document.getElementById('btnSalvar');
    btn.disabled = true;
    btn.innerHTML = `<span class="loader"></span> ${t('perfil.salvando')}`;

    try {
        const data = await updateProfile({
            nome: nome,
            cidade: document.getElementById('perfilCidade')?.value || '',
            whatsapp: document.getElementById('perfilWhatsapp')?.value.trim() || '',
            nivelProfissional: document.getElementById('perfilNivel')?.value || 'Estudante',
            areaTecnologia: document.getElementById('perfilArea')?.value || 'Web',
            competenciasAtuais: document.getElementById('perfilSkills')?.value.trim() || '',
        });

        const usuario = getUsuarioLogado();
        if (usuario) {
            localStorage.setItem(SESSION_KEY, JSON.stringify({
                ...usuario,
                nome: data.nome || nome,
                cidade: data.cidade || usuario.cidade,
                whatsapp: data.whatsapp || usuario.whatsapp,
                nivelProfissional: data.nivelProfissional || usuario.nivelProfissional,
                areaTecnologia: data.areaTecnologia || usuario.areaTecnologia,
                competenciasAtuais: data.competenciasAtuais || usuario.competenciasAtuais,
            }));
        }

        showMsg(t('perfil.sucesso'), 'success');
        btn.disabled = false;
        btn.textContent = t('perfil.salvarAlteracoes');
    } catch (err) {
        showMsg(err.message || t('perfil.erroSalvar'), 'error');
        btn.disabled = false;
        btn.textContent = t('perfil.salvarAlteracoes');
    }
});

document.getElementById('btnCancelar')?.addEventListener('click', () => {
    window.location.href = 'dashboard.html';
});

['perfilNome'].forEach(id => {
    document.getElementById(id)?.addEventListener('input', function () {
        this.classList.remove('border-rose-500');
        this.classList.add('border-slate-800');
        hideMsg();
    });
});
