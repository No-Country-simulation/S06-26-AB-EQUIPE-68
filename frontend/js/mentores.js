import { listarMentores, solicitarMentoria, historicoMentorias } from './api.js';
import { t, getIdioma } from './i18n.js';

const SESSION_KEY = 'bitapp_usuario';

function getUsuarioLogado() {
    const raw = localStorage.getItem(SESSION_KEY);
    return raw ? JSON.parse(raw) : null;
}

const usuario = getUsuarioLogado();

const carregando = document.getElementById('mentoresCarregando');
const erroEl = document.getElementById('mentoresErro');
const grid = document.getElementById('mentoresGrid');
const historicoConteudo = document.getElementById('historicoConteudo');
const modal = document.getElementById('mentoriaModal');
const modalContent = document.getElementById('modalContent');
const closeModal = document.getElementById('closeModal');

let mentoresCache = [];

function formatarData(iso) {
    try {
        const data = new Date(iso);
        const locale = getIdioma() === 'es' ? 'es-ES' : 'pt-BR';
        return data.toLocaleDateString(locale, { day: '2-digit', month: 'long', year: 'numeric' });
    } catch {
        return iso;
    }
}

function statusLabel(status) {
    switch (status) {
        case 'CONFIRMADA': return t('mentores.statusConfirmada');
        case 'CONCLUIDA': return t('mentores.statusConcluida');
        default: return t('mentores.statusAguardandoConfirmacao');
    }
}

function statusBadgeClasse(status) {
    switch (status) {
        case 'CONFIRMADA': return 'bg-cyan-500/15 text-cyan-400 border-cyan-500/30';
        case 'CONCLUIDA': return 'bg-emerald-500/15 text-emerald-400 border-emerald-500/30';
        default: return 'bg-amber-500/15 text-amber-400 border-amber-500/30';
    }
}

function mentorCardHtml(mentor) {
    const recomendado = usuario?.areaTecnologia && mentor.areasAtendidas.includes(usuario.areaTecnologia);
    return `
        <div class="rounded-2xl bg-slate-900/60 border border-slate-800 p-6 hover:border-cyan-500/40 transition-all duration-200 flex flex-col justify-between ${recomendado ? 'ring-1 ring-cyan-500/30' : ''}">
            <div>
                ${recomendado ? `<span class="inline-block mb-2 rounded-lg bg-cyan-950 border border-cyan-800 text-cyan-400 px-2.5 py-1 text-xs font-bold">${t('mentores.recomendado')}</span>` : ''}
                <h3 class="text-base font-bold text-white">${mentor.nome}</h3>
                <p class="text-sm font-medium text-cyan-400 mb-3">${mentor.papel}</p>
                <p class="text-xs text-slate-500 mb-2">${t('mentores.areasAtendidas')}</p>
                <div class="flex flex-wrap gap-1.5 mb-4">
                    ${mentor.areasAtendidas.map(a => `<span class="text-xs font-semibold px-2.5 py-1 rounded-full border bg-slate-800 text-slate-300 border-slate-700">${a}</span>`).join('')}
                </div>
            </div>
            <button data-mentor-id="${mentor.id}"
                class="btn-solicitar w-full rounded-2xl bg-cyan-500 px-4 py-2.5 text-sm font-bold text-slate-950 hover:bg-cyan-400 transition">
                ${t('mentores.solicitarMentoria')}
            </button>
        </div>`;
}

function renderizarMentores(mentores) {
    grid.innerHTML = mentores.map(mentorCardHtml).join('');
    grid.querySelectorAll('.btn-solicitar').forEach(btn => {
        btn.addEventListener('click', () => abrirModal(Number(btn.dataset.mentorId)));
    });
}

function abrirModal(mentorId) {
    const mentor = mentoresCache.find(m => m.id === mentorId);
    if (!mentor) return;

    if (!usuario) {
        modalContent.innerHTML = `
            <div class="text-center py-6">
                <p class="text-sm text-slate-300 mb-4">${t('mentores.facaLogin')}</p>
                <a href="index.html" class="inline-block rounded-2xl bg-cyan-500 px-6 py-3 text-sm font-bold text-slate-950 hover:bg-cyan-400 transition">${t('index.entrar')}</a>
            </div>`;
        modal.classList.remove('hidden');
        return;
    }

    modalContent.innerHTML = `
        <h2 class="text-xl font-bold text-white mb-1">${mentor.nome}</h2>
        <p class="text-sm font-medium text-cyan-400 mb-5">${mentor.papel}</p>
        <form id="formSolicitarMentoria" class="space-y-4">
            <div>
                <label for="mentoriaArea" class="text-xs font-bold text-slate-400 uppercase tracking-wider block mb-1.5">${t('mentores.areaLabel')}</label>
                <select id="mentoriaArea" required
                    class="w-full rounded-2xl border border-slate-800 bg-slate-950 px-4 py-3 text-sm text-slate-100 focus:outline-none focus:border-cyan-500 focus:ring-1 focus:ring-cyan-500 transition">
                    ${mentor.areasAtendidas.map(a => `<option value="${a}" ${a === usuario.areaTecnologia ? 'selected' : ''}>${a}</option>`).join('')}
                </select>
            </div>
            <div>
                <label for="mentoriaMensagem" class="text-xs font-bold text-slate-400 uppercase tracking-wider block mb-1.5">${t('mentores.mensagemLabel')}</label>
                <textarea id="mentoriaMensagem" rows="3" placeholder="${t('mentores.mensagemPlaceholder')}"
                    class="w-full rounded-2xl border border-slate-800 bg-slate-950 px-4 py-3 text-sm text-slate-100 placeholder-slate-500 focus:outline-none focus:border-cyan-500 focus:ring-1 focus:ring-cyan-500 transition"></textarea>
            </div>
            <p id="mentoriaErroForm" class="hidden text-sm text-rose-400"></p>
            <button type="submit" id="btnEnviarMentoria"
                class="w-full rounded-2xl bg-cyan-500 px-6 py-3 text-sm font-bold text-slate-950 shadow-xl transition hover:bg-cyan-400 transform active:scale-[0.98]">
                ${t('mentores.enviar')}
            </button>
        </form>`;
    modal.classList.remove('hidden');

    document.getElementById('formSolicitarMentoria').addEventListener('submit', async (e) => {
        e.preventDefault();
        const btn = document.getElementById('btnEnviarMentoria');
        const erroForm = document.getElementById('mentoriaErroForm');
        btn.disabled = true;
        btn.textContent = t('mentores.enviando');
        erroForm.classList.add('hidden');

        try {
            const area = document.getElementById('mentoriaArea').value;
            const msg = document.getElementById('mentoriaMensagem').value.trim();
            const resposta = await solicitarMentoria(
                { mentorId: mentor.id, areaSolicitada: area, mensagem: msg || null },
                usuario.id
            );
            modalContent.innerHTML = `
                <div class="text-center py-4">
                    <p class="text-lg font-bold text-white mb-2">${t('mentores.solicitacaoEnviada')}</p>
                    <p class="text-sm text-slate-400 mb-4">${t('mentores.com', { mentor: mentor.nome })}</p>
                    <div class="rounded-xl bg-slate-950 border border-slate-800 p-4 mb-4">
                        <span class="text-xs text-slate-500 block mb-1">${t('mentores.salaLabel')}</span>
                        <a href="${mentor.linkSala}" target="_blank" rel="noopener" class="text-cyan-400 font-semibold hover:underline break-all">${mentor.linkSala}</a>
                    </div>
                    <span class="text-xs font-semibold px-2.5 py-1 rounded-full border ${statusBadgeClasse(resposta.status)}">${statusLabel(resposta.status)}</span>
                </div>`;
            carregarHistorico();
        } catch (err) {
            erroForm.textContent = t('mentores.erroSolicitar');
            erroForm.classList.remove('hidden');
            btn.disabled = false;
            btn.textContent = t('mentores.enviar');
        }
    });
}

closeModal?.addEventListener('click', () => modal.classList.add('hidden'));
modal?.addEventListener('click', (e) => {
    if (e.target === modal) modal.classList.add('hidden');
});
document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape' && modal && !modal.classList.contains('hidden')) {
        modal.classList.add('hidden');
    }
});

function historicoItemHtml(item) {
    return `
        <li class="rounded-2xl border border-slate-800 bg-slate-900/60 p-5 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3">
            <div>
                <p class="text-sm font-bold text-white">${t('mentores.com', { mentor: item.mentorNome })}</p>
                <p class="text-xs text-slate-500 mt-0.5">${t('mentores.solicitadoEm', { data: formatarData(item.createdAt) })} · ${item.areaSolicitada}</p>
            </div>
            <span class="text-xs font-semibold px-2.5 py-1 rounded-full border shrink-0 ${statusBadgeClasse(item.status)}">${statusLabel(item.status)}</span>
        </li>`;
}

async function carregarHistorico() {
    if (!usuario) {
        historicoConteudo.innerHTML = `<p class="text-sm text-slate-400 text-center py-8">${t('mentores.historicoFacaLogin')}</p>`;
        return;
    }
    try {
        const historico = await historicoMentorias(usuario.id);
        if (!historico || historico.length === 0) {
            historicoConteudo.innerHTML = `<p class="text-sm text-slate-400 text-center py-8">${t('mentores.historicoVazio')}</p>`;
            return;
        }
        historicoConteudo.innerHTML = `<ul class="space-y-3">${historico.map(historicoItemHtml).join('')}</ul>`;
    } catch {
        historicoConteudo.innerHTML = `<p class="text-sm text-rose-400 text-center py-8">${t('mentores.erroCarregar')}</p>`;
    }
}

async function init() {
    try {
        mentoresCache = await listarMentores();
        carregando.classList.add('hidden');
        grid.classList.remove('hidden');
        renderizarMentores(mentoresCache);
        await carregarHistorico();
    } catch (err) {
        carregando.classList.add('hidden');
        erroEl.classList.remove('hidden');
    }
}

init();
