// Card do Bit — check-in conversacional na entrada do dashboard.
// Moldura única (modal centrado, sem variantes por query param), aparece em
// toda carga do dashboard (sem guard de "já fez check-in hoje" e sem
// persistência em localStorage — "agora não" só esconde na visualização
// atual). Check-in é a escala única CVV v2 (emoji opcional) + texto livre;
// sem emoji, nota=null (não deriva, não entra em agregação/tendência).
// Reaproveita saudeCheckin/buscarSugestoes (api.js), os painéis graduados e
// o modal de consentimento (saude-shared.js / mesmos textos do DICT).
// saude-mental.html continua funcionando igual — o Bit é um caminho
// adicional, não substituto.
import { saudeCheckin, buscarSugestoes } from './api.js';
import { t, getIdioma } from './i18n.js';
import { NIVEL_CHECKIN, nivelPorNota, renderNivelDerivacaoPanel } from './saude-shared.js';

const SESSION_KEY = 'bitapp_usuario';

function getUsuarioLogado() {
    const raw = localStorage.getItem(SESSION_KEY);
    return raw ? JSON.parse(raw) : null;
}

const usuario = getUsuarioLogado();

function saudacaoPorHora() {
    const hora = new Date().getHours();
    if (hora < 12) return t('bit.saudacaoManha', { nome: usuario.nome });
    if (hora < 18) return t('bit.saudacaoTarde', { nome: usuario.nome });
    return t('bit.saudacaoNoite', { nome: usuario.nome });
}

// ════════════════════════════════════════════════════════════════════════
//  MODAL — moldura única centrada (substitui as variantes card/full)
// ════════════════════════════════════════════════════════════════════════

function onEscKey(e) {
    if (e.key === 'Escape') dispensar();
}

function onTabTrap(e) {
    if (e.key !== 'Tab') return;
    const overlay = document.getElementById('bitOverlay');
    if (!overlay) return;
    const focusables = overlay.querySelectorAll('button, [href], input, textarea, select, [tabindex]:not([tabindex="-1"])');
    if (!focusables.length) return;
    const first = focusables[0];
    const last = focusables[focusables.length - 1];
    if (e.shiftKey && document.activeElement === first) {
        e.preventDefault();
        last.focus();
    } else if (!e.shiftKey && document.activeElement === last) {
        e.preventDefault();
        first.focus();
    }
}

function criarModal() {
    const overlay = document.createElement('div');
    overlay.id = 'bitOverlay';
    overlay.className = 'fixed inset-0 z-[90] flex items-center justify-center bg-black/60 backdrop-blur-sm px-4 py-8';
    overlay.setAttribute('role', 'dialog');
    overlay.setAttribute('aria-modal', 'true');
    overlay.setAttribute('aria-label', t('bit.tituloModal'));
    overlay.innerHTML = `
        <div class="w-full max-w-[420px] max-h-[90vh] overflow-y-auto rounded-3xl border border-slate-800 bg-slate-900/95 p-6 sm:p-8 shadow-2xl backdrop-blur-lg relative animate-fade-in">
            <button type="button" id="bitFechar" aria-label="${t('bit.dispensar')}"
                class="absolute top-4 right-4 w-9 h-9 rounded-full bg-slate-800 border border-slate-700 text-slate-300 hover:text-white hover:border-slate-500 transition flex items-center justify-center">✕</button>
            <div id="bitBubbles" class="space-y-4 pr-2"></div>
        </div>
    `;
    document.body.appendChild(overlay);
    overlay.querySelector('#bitFechar').addEventListener('click', dispensar);
    overlay.addEventListener('click', (e) => { if (e.target === overlay) dispensar(); });
    document.addEventListener('keydown', onEscKey);
    overlay.addEventListener('keydown', onTabTrap);
    overlay.querySelector('#bitFechar').focus(); // foco entra no modal ao abrir
    return overlay.querySelector('#bitBubbles');
}

function dispensar() {
    // Só esconde na visualização atual — sem localStorage, reload sempre traz o Bit de volta.
    document.removeEventListener('keydown', onEscKey);
    document.getElementById('bitOverlay')?.remove();
}

// ════════════════════════════════════════════════════════════════════════
//  BOLHAS — helpers de renderização do fluxo conversacional
// ════════════════════════════════════════════════════════════════════════

function addBitBubble(bubblesEl, innerHtml) {
    const bubble = document.createElement('div');
    bubble.className = 'flex items-start gap-3';
    bubble.innerHTML = `
        <span class="shrink-0 text-2xl leading-none" aria-hidden="true">🤖</span>
        <div class="flex-1 rounded-2xl rounded-tl-sm bg-slate-800/80 border border-slate-700/60 p-4 text-sm text-slate-100">${innerHtml}</div>
    `;
    bubblesEl.appendChild(bubble);
    return bubble;
}

function addUserBubble(bubblesEl, texto) {
    const bubble = document.createElement('div');
    bubble.className = 'flex justify-end';
    bubble.innerHTML = `<div class="rounded-2xl rounded-tr-sm bg-cyan-500 text-slate-950 font-semibold px-4 py-2 text-sm">${texto}</div>`;
    bubblesEl.appendChild(bubble);
    return bubble;
}

function linkAgoraNao(bubblesEl) {
    const wrap = document.createElement('div');
    wrap.className = 'text-center pt-1';
    wrap.innerHTML = `<button type="button" class="text-xs text-slate-500 hover:text-slate-300 underline transition">${t('bit.agoraNao')}</button>`;
    wrap.querySelector('button').addEventListener('click', dispensar);
    bubblesEl.appendChild(wrap);
}

// ════════════════════════════════════════════════════════════════════════
//  CONSENTIMENTO (nota 0-1) — mesmos textos/comportamento do lote 2
// ════════════════════════════════════════════════════════════════════════

function pedirConsentimento() {
    return new Promise((resolve) => {
        const modal = document.createElement('div');
        modal.className = 'fixed inset-0 z-[95] flex items-center justify-center bg-slate-950/80 backdrop-blur-sm px-4';
        modal.setAttribute('role', 'dialog');
        modal.setAttribute('aria-modal', 'true');
        modal.innerHTML = `
            <div class="w-full max-w-md rounded-3xl border border-slate-800 bg-slate-900 p-8 shadow-2xl space-y-5 text-center animate-fade-in">
                <div class="text-4xl" aria-hidden="true">🫶</div>
                <h3 class="text-lg font-bold text-white">${t('saude.modalTitulo')}</h3>
                <p class="text-sm text-slate-300 leading-relaxed">${t('saude.modalMsg')}</p>
                <div class="flex flex-col gap-3">
                    <button type="button" data-acao="sim" class="w-full rounded-2xl bg-cyan-500 px-6 py-3 text-sm font-bold text-slate-950 shadow-xl transition hover:bg-cyan-400 transform active:scale-[0.98]">${t('saude.modalSim')}</button>
                    <button type="button" data-acao="nao" class="w-full rounded-2xl border border-slate-700 bg-slate-950 px-6 py-3 text-sm font-semibold text-slate-300 transition hover:border-slate-600">${t('saude.modalNao')}</button>
                </div>
            </div>
        `;
        modal.addEventListener('click', (e) => {
            if (e.target === modal) { modal.remove(); resolve(false); }
        });
        modal.querySelector('[data-acao="sim"]').addEventListener('click', () => { modal.remove(); resolve(true); });
        modal.querySelector('[data-acao="nao"]').addEventListener('click', () => { modal.remove(); resolve(false); });
        document.body.appendChild(modal);
    });
}

// ════════════════════════════════════════════════════════════════════════
//  SUGESTÕES — cards curtos (título + motivo) após a resposta do Bit
// ════════════════════════════════════════════════════════════════════════

async function renderSugestoes(bubblesEl) {
    try {
        const data = await buscarSugestoes(usuario.id, getIdioma());
        const sugestoes = data?.sugestoes || [];
        if (sugestoes.length === 0) return;
        const grid = document.createElement('div');
        grid.className = 'grid gap-2 sm:grid-cols-3 pl-9';
        grid.innerHTML = sugestoes.map(s => `
            <div class="rounded-xl bg-slate-900/60 border border-slate-800 p-3">
                <p class="text-xs font-bold text-white">${s.titulo}</p>
                <p class="text-[11px] text-slate-400 mt-1 leading-relaxed">${s.motivo || ''}</p>
            </div>
        `).join('');
        bubblesEl.appendChild(grid);
    } catch { /* sugestões são um extra — falha silenciosa */ }
}

// ════════════════════════════════════════════════════════════════════════
//  FLUXO PRINCIPAL — check-in único (emoji opcional + texto livre)
// ════════════════════════════════════════════════════════════════════════

function perguntarCheckinDiario(bubblesEl) {
    addBitBubble(bubblesEl, saudacaoPorHora());

    const moodWrap = document.createElement('div');
    moodWrap.className = 'flex flex-wrap gap-2 pl-9';
    moodWrap.innerHTML = NIVEL_CHECKIN.map(n => `
        <button type="button" data-nota="${n.nota}" class="bit-mood-btn text-2xl px-3 py-2 bg-slate-950 hover:bg-slate-800 border border-slate-800 rounded-xl transition focus:ring-2 focus:ring-cyan-500 outline-none" aria-label="${t(n.labelKey)}">
            ${n.emoji}
        </button>
    `).join('');
    bubblesEl.appendChild(moodWrap);

    addBitBubble(bubblesEl, t('bit.perguntaContar'));

    const formWrap = document.createElement('div');
    formWrap.className = 'pl-9 space-y-2';
    formWrap.innerHTML = `
        <textarea rows="3" placeholder="${t('saude.contextoPlaceholder')}"
            class="w-full rounded-2xl border border-slate-800 bg-slate-950 px-4 py-3 text-sm text-slate-100 focus:outline-none focus:border-cyan-500 focus:ring-1 focus:ring-cyan-500 transition"></textarea>
        <p class="hidden text-[11px] text-amber-400/90">${t('bit.pedirAlgumaCoisa')}</p>
        <button type="button" disabled class="rounded-2xl bg-cyan-500 px-6 py-2.5 text-sm font-bold text-slate-950 shadow-xl transition hover:bg-cyan-400 transform active:scale-[0.98] disabled:opacity-40 disabled:cursor-not-allowed">${t('saude.enviarRegistro')}</button>
    `;
    bubblesEl.appendChild(formWrap);

    const textarea = formWrap.querySelector('textarea');
    const hint = formWrap.querySelector('p');
    const botaoEnviar = formWrap.querySelector('button');

    let notaEscolhida = null;

    function atualizarEstadoEnvio() {
        const podeEnviar = Boolean(notaEscolhida) || textarea.value.trim().length > 0;
        botaoEnviar.disabled = !podeEnviar;
        hint.classList.toggle('hidden', podeEnviar);
    }

    moodWrap.querySelectorAll('.bit-mood-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            moodWrap.querySelectorAll('.bit-mood-btn').forEach(b => {
                b.classList.remove('border-cyan-500', 'bg-slate-800');
                b.classList.add('border-slate-800', 'bg-slate-950');
            });
            btn.classList.remove('border-slate-800', 'bg-slate-950');
            btn.classList.add('border-cyan-500', 'bg-slate-800');
            notaEscolhida = Number(btn.dataset.nota);
            atualizarEstadoEnvio();
        });
    });

    textarea.addEventListener('input', atualizarEstadoEnvio);

    botaoEnviar.addEventListener('click', async () => {
        const contexto = textarea.value || '';
        if (!notaEscolhida && !contexto.trim()) return; // guarda defensiva

        // Nota 1 (derivação REFORÇADA): consentimento ANTES de gravar.
        if (notaEscolhida === 1) {
            const consentiu = await pedirConsentimento();
            if (!consentiu) return; // "cliquei sem querer": não grava, não avança
        }

        botaoEnviar.disabled = true;
        botaoEnviar.innerHTML = `<span class="loader"></span> ${t('saude.salvando')}`;

        try {
            const data = await saudeCheckin({
                usuarioId: usuario.id,
                nota: notaEscolhida,
                contexto,
                idioma: getIdioma(),
            });
            moodWrap.remove();
            formWrap.remove();
            if (notaEscolhida) addUserBubble(bubblesEl, nivelPorNota(notaEscolhida)?.emoji || '');
            if (contexto) addUserBubble(bubblesEl, contexto);
            renderRespostaBit(bubblesEl, data);
        } catch {
            botaoEnviar.disabled = false;
            botaoEnviar.textContent = t('saude.enviarRegistro');
            alert(t('saude.erroSalvar'));
        }
    });
}

function renderRespostaBit(bubblesEl, data) {
    if (data.leituraEmocional) {
        addBitBubble(bubblesEl, `<span class="italic text-slate-300">${data.leituraEmocional}</span>`);
    }

    // Não reaproveita addBitBubble aqui: renderNivelDerivacaoPanel substitui o
    // className inteiro do container (mesmo comportamento de saude-mental.js),
    // então o painel precisa de um wrapper flex-1 próprio em vez de ser o
    // próprio item flex da linha avatar+conteúdo.
    const row = document.createElement('div');
    row.className = 'flex items-start gap-3';
    row.innerHTML = `<span class="shrink-0 text-2xl leading-none" aria-hidden="true">🤖</span>`;
    const wrapper = document.createElement('div');
    wrapper.className = 'flex-1';
    row.appendChild(wrapper);
    bubblesEl.appendChild(row);

    const painel = document.createElement('div');
    const titleEl = document.createElement('h4');
    const msgEl = document.createElement('p');
    msgEl.className = 'text-sm text-slate-200 leading-relaxed';
    const actionEl = document.createElement('div');
    actionEl.className = 'mt-4 pt-4 border-t border-slate-800/60';
    painel.append(titleEl, msgEl, actionEl);
    wrapper.appendChild(painel);

    renderNivelDerivacaoPanel({ container: painel, title: titleEl, msg: msgEl, action: actionEl }, data);

    renderSugestoes(bubblesEl);
    linkAgoraNao(bubblesEl);
}

// ════════════════════════════════════════════════════════════════════════
//  BOOT
// ════════════════════════════════════════════════════════════════════════

async function iniciarBit() {
    if (!usuario?.id) return;

    const bubblesEl = criarModal();
    if (!bubblesEl) return;

    perguntarCheckinDiario(bubblesEl);
}

document.addEventListener('DOMContentLoaded', iniciarBit);
