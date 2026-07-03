// Módulo compartilhado entre saude-mental.js e bit.js — extraído para não
// duplicar os painéis graduados REFORCADO/PREVENTIVO nem os textos do DICT
// (mesma regra de derivação do backend, decidida só pela nota semanal).
import { t } from './i18n.js';

export const MOOD_EMOJIS = {
    feliz: '😊',
    cansado: '😴',
    triste: '😢',
    ansioso: '😰',
    sobrecarregado: '🤯',
};

// Ordem/labels usados pelos botões de humor (saude-mental.html e bit.js).
export const MOOD_ORDER = ['feliz', 'cansado', 'ansioso', 'triste', 'sobrecarregado'];
export const MOOD_LABEL_KEYS = {
    feliz: 'saude.humorLeve',
    cansado: 'saude.humorCansado',
    ansioso: 'saude.humorAnsioso',
    triste: 'saude.humorTriste',
    sobrecarregado: 'saude.humorSobrecarregado',
};

// Onze botões 0-10 (nenhum pré-selecionado), navegáveis por Tab. `onSelect(n)`
// é chamado com a nota escolhida; o realce visual é tratado aqui.
export function renderNotaBotoes(gridEl, onSelect) {
    if (!gridEl) return;
    gridEl.innerHTML = '';
    for (let n = 0; n <= 10; n++) {
        const btn = document.createElement('button');
        btn.type = 'button';
        btn.dataset.nota = String(n);
        btn.textContent = String(n);
        btn.setAttribute('aria-pressed', 'false');
        btn.setAttribute('aria-label', `Nota ${n}`);
        btn.className = 'nota-btn w-10 h-10 text-sm font-bold bg-slate-950 hover:bg-slate-800 border border-slate-800 rounded-xl transition text-slate-200 focus:ring-2 focus:ring-cyan-500 outline-none';
        btn.addEventListener('click', () => {
            gridEl.querySelectorAll('.nota-btn').forEach(el => {
                el.classList.remove('border-cyan-500', 'bg-slate-800', 'text-white');
                el.classList.add('border-slate-800', 'bg-slate-950', 'text-slate-200');
                el.setAttribute('aria-pressed', 'false');
            });
            btn.classList.remove('border-slate-800', 'bg-slate-950', 'text-slate-200');
            btn.classList.add('border-cyan-500', 'bg-slate-800', 'text-white');
            btn.setAttribute('aria-pressed', 'true');
            onSelect(n);
        });
        gridEl.appendChild(btn);
    }
}

// Renderiza o acolhimento. O painel é escolhido por data.nivelDerivacao — decidido
// SÓ pela nota no backend (SaudeMentalService, inviolável). Linguagem de cuidado:
// nada de "SUPORTE CRÍTICO"/⚠️.
export function renderNivelDerivacaoPanel({ container, title, msg, action }, data) {
    container.classList.remove('hidden');
    msg.innerText = data.mensagem || '';

    if (data.nivelDerivacao === 'REFORCADO') {
        // Nota 0-1: cuidado, CVV em destaque, tom acolhedor (sem alarme).
        container.className = 'p-6 rounded-3xl border border-amber-800/60 bg-amber-950/20 mt-6 animate-fade-in';
        title.className = 'text-sm font-bold uppercase tracking-wider mb-2 text-amber-300 flex items-center gap-2';
        title.innerHTML = `🫂 ${t('saude.reforcadoTitulo')}`;
        action.innerHTML = `<div class="p-4 bg-slate-950 border border-amber-700/40 rounded-2xl space-y-1">
            <p class="text-sm font-bold text-white">${t('saude.reforcadoCvv')}</p>
            <p class="text-xs text-slate-400">${data.acaoSugerida || ''}</p></div>`;
    } else if (data.nivelDerivacao === 'PREVENTIVO') {
        // Nota 2-3: escuta suave, apresentada como recurso — não como alerta.
        container.className = 'p-6 rounded-3xl border border-cyan-800/50 bg-cyan-950/20 mt-6 animate-fade-in';
        title.className = 'text-sm font-bold uppercase tracking-wider mb-2 text-cyan-300 flex items-center gap-2';
        title.innerHTML = `💬 ${t('saude.preventivoTitulo')}`;
        action.innerHTML = `<div class="p-4 bg-slate-950 border border-cyan-800/40 rounded-2xl space-y-1">
            <p class="text-sm text-slate-200">${t('saude.preventivoCvv')}</p>
            <p class="text-xs text-slate-400">${data.acaoSugerida || ''}</p></div>`;
    } else {
        // Nota 4-10: acolhimento normal.
        container.className = 'p-6 rounded-3xl border border-slate-800 bg-slate-900/60 mt-6 animate-fade-in';
        title.className = 'text-sm font-bold uppercase tracking-wider mb-2 text-cyan-400';
        title.innerText = t('saude.normalTitulo');
        action.innerHTML = `<p class="text-xs text-slate-400 font-semibold mb-1">${t('saude.acaoLabel')}</p>
            <p class="text-sm text-slate-200">${data.acaoSugerida || ''}</p>`;
    }
}