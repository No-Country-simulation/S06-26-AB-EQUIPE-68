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

// Nota fixa por emoji (lote 4.1 — escala 0-10 extinta), usada SÓ na pergunta
// semanal: o clique no emoji já resolve a notaSemanal a enviar ao mesmo
// /api/saude, sem grade de botões numéricos.
export const SEMANA_NOTA = { sobrecarregado: 1, triste: 3, ansioso: 5, cansado: 7, feliz: 9 };

// "Devida" = não há registro com notaSemanal preenchida nos últimos 7 dias.
// Usado por bit.js e saude-mental.js para decidir se oferece a pergunta semanal.
export function semanalDevida(registros) {
    if (!registros || registros.length === 0) return true;
    const corte = Date.now() - 7 * 24 * 60 * 60 * 1000;
    return !registros.some(r => r.notaSemanal != null && r.createdAt && new Date(r.createdAt).getTime() >= corte);
}

// Roda para a próxima variação de acolhimento CVV (persistida em localStorage),
// só deve ser chamada quando um painel CVV (REFORCADO/PREVENTIVO) é de fato exibido.
function proximaVariacaoCvv() {
    const variacoes = t('saude.cvvVariacoes');
    const idx = (parseInt(localStorage.getItem('cvvMsgIdx') || '-1', 10) + 1) % variacoes.length;
    localStorage.setItem('cvvMsgIdx', String(idx));
    return variacoes[idx];
}

// Botões de ação do CVV (ligar/chat), com cor ajustada à identidade de cada painel.
function botoesCvv(cor) {
    return `<div class="flex flex-col sm:flex-row gap-2 pt-1">
        <a href="tel:188" class="flex-1 text-center rounded-xl bg-${cor}-500 px-5 py-2.5 text-sm font-bold text-slate-950 shadow-lg transition hover:bg-${cor}-400 transform active:scale-[0.98]">${t('saude.cvvBtnLigar')}</a>
        <a href="https://www.cvv.org.br" target="_blank" rel="noopener noreferrer" class="flex-1 text-center rounded-xl border border-${cor}-700/50 bg-slate-950 px-5 py-2.5 text-sm font-semibold text-${cor}-200 transition hover:border-${cor}-600">${t('saude.cvvBtnChat')}</a>
    </div>`;
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
        action.innerHTML = `<div class="p-4 bg-slate-950 border border-amber-700/40 rounded-2xl space-y-2">
            <p class="text-sm text-amber-100">${proximaVariacaoCvv()}</p>
            <p class="text-sm font-bold text-white">${t('saude.reforcadoCvv')}</p>
            ${botoesCvv('amber')}
            <p class="text-xs text-slate-400 pt-1">${data.acaoSugerida || ''}</p></div>`;
    } else if (data.nivelDerivacao === 'PREVENTIVO') {
        // Nota 2-3: escuta suave, apresentada como recurso — não como alerta.
        container.className = 'p-6 rounded-3xl border border-cyan-800/50 bg-cyan-950/20 mt-6 animate-fade-in';
        title.className = 'text-sm font-bold uppercase tracking-wider mb-2 text-cyan-300 flex items-center gap-2';
        title.innerHTML = `💬 ${t('saude.preventivoTitulo')}`;
        action.innerHTML = `<div class="p-4 bg-slate-950 border border-cyan-800/40 rounded-2xl space-y-2">
            <p class="text-sm text-cyan-100">${proximaVariacaoCvv()}</p>
            <p class="text-sm text-slate-200">${t('saude.preventivoCvv')}</p>
            ${botoesCvv('cyan')}
            <p class="text-xs text-slate-400 pt-1">${data.acaoSugerida || ''}</p></div>`;
    } else {
        // Nota 4-10: acolhimento normal.
        container.className = 'p-6 rounded-3xl border border-slate-800 bg-slate-900/60 mt-6 animate-fade-in';
        title.className = 'text-sm font-bold uppercase tracking-wider mb-2 text-cyan-400';
        title.innerText = t('saude.normalTitulo');
        action.innerHTML = `<p class="text-xs text-slate-400 font-semibold mb-1">${t('saude.acaoLabel')}</p>
            <p class="text-sm text-slate-200">${data.acaoSugerida || ''}</p>`;
    }
}