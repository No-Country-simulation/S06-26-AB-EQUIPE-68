// Módulo compartilhado entre saude-mental.js e bit.js — extraído para não
// duplicar os painéis graduados REFORCADO/PREVENTIVO/tendência nem os textos
// do DICT (mesma regra de derivação do backend, decidida só pela nota do
// check-in — CVV v2).
import { t } from './i18n.js';

// Escala única de check-in (CVV v2): nota fixa por nível, na ordem exibida
// nos botões (do mais positivo ao mais negativo). Espelha exatamente
// NivelCheckin.java — rótulo/emoji só pra UI, a nota é o que vai no payload.
export const NIVEL_CHECKIN = [
    { nota: 9, emoji: '😄', labelKey: 'saude.nivelMuitoFeliz' },
    { nota: 7, emoji: '🙂', labelKey: 'saude.nivelFeliz' },
    { nota: 5, emoji: '😌', labelKey: 'saude.nivelTranquilo' },
    { nota: 3, emoji: '😔', labelKey: 'saude.nivelTriste' },
    { nota: 1, emoji: '😢', labelKey: 'saude.nivelMuitoTriste' },
];

export function nivelPorNota(nota) {
    return NIVEL_CHECKIN.find(n => n.nota === nota) || null;
}

// Roda para a próxima variação de acolhimento CVV (persistida em localStorage),
// só deve ser chamada por gatilho pontual (REFORCADO/PREVENTIVO) — a
// tendência semanal usa mensagem fixa, sem rotação (ver comentário abaixo).
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

// Renderiza o acolhimento. Prioridade visual: REFORCADO > PREVENTIVO >
// tendência semanal (sem gatilho hoje) > normal. nivelDerivacao e
// tendenciaSemana são decididos SÓ pela nota no backend (SaudeMentalService,
// inviolável). Linguagem de cuidado: nada de "SUPORTE CRÍTICO"/⚠️.
export function renderNivelDerivacaoPanel({ container, title, msg, action }, data) {
    container.classList.remove('hidden');
    msg.innerText = data.mensagem || '';

    if (data.nivelDerivacao === 'REFORCADO') {
        // Nota 1: cuidado, CVV em destaque, tom acolhedor (sem alarme).
        container.className = 'p-6 rounded-3xl border border-amber-800/60 bg-amber-950/20 mt-6 animate-fade-in';
        title.className = 'text-sm font-bold uppercase tracking-wider mb-2 text-amber-300 flex items-center gap-2';
        title.innerHTML = `🫂 ${t('saude.reforcadoTitulo')}`;
        action.innerHTML = `<div class="p-4 bg-slate-950 border border-amber-700/40 rounded-2xl space-y-2">
            <p class="text-sm text-amber-100">${proximaVariacaoCvv()}</p>
            <p class="text-sm font-bold text-white">${t('saude.reforcadoCvv')}</p>
            ${botoesCvv('amber')}
            <p class="text-xs text-slate-400 pt-1">${data.acaoSugerida || ''}</p></div>`;
    } else if (data.nivelDerivacao === 'PREVENTIVO') {
        // Nota 3: escuta suave, apresentada como recurso — não como alerta.
        container.className = 'p-6 rounded-3xl border border-cyan-800/50 bg-cyan-950/20 mt-6 animate-fade-in';
        title.className = 'text-sm font-bold uppercase tracking-wider mb-2 text-cyan-300 flex items-center gap-2';
        title.innerHTML = `💬 ${t('saude.preventivoTitulo')}`;
        action.innerHTML = `<div class="p-4 bg-slate-950 border border-cyan-800/40 rounded-2xl space-y-2">
            <p class="text-sm text-cyan-100">${proximaVariacaoCvv()}</p>
            <p class="text-sm text-slate-200">${t('saude.preventivoCvv')}</p>
            ${botoesCvv('cyan')}
            <p class="text-xs text-slate-400 pt-1">${data.acaoSugerida || ''}</p></div>`;
    } else if (data.tendenciaSemana) {
        // Sem gatilho hoje, mas os últimos dias vêm pesados (CVV v2): mesma
        // estrutura dos gatilhos pontuais, cor distinta (índigo). Mensagem é
        // FIXA — explica o alerta agregado da semana, não pode virar frase
        // genérica rotativa — mas ganha os mesmos botões de ação.
        container.className = 'p-6 rounded-3xl border border-indigo-800/50 bg-indigo-950/20 mt-6 animate-fade-in';
        title.className = 'text-sm font-bold uppercase tracking-wider mb-2 text-indigo-300 flex items-center gap-2';
        title.innerHTML = `📈 ${t('saude.tendenciaTitulo')}`;
        action.innerHTML = `<div class="p-4 bg-slate-950 border border-indigo-800/40 rounded-2xl space-y-2">
            <p class="text-sm text-slate-200">${t('saude.tendenciaMsg')}</p>
            ${botoesCvv('indigo')}
            <p class="text-xs text-slate-400 pt-1">${data.acaoSugerida || ''}</p></div>`;
    } else {
        // Nota 5/7/9 ou ausente, sem tendência: acolhimento normal.
        container.className = 'p-6 rounded-3xl border border-slate-800 bg-slate-900/60 mt-6 animate-fade-in';
        title.className = 'text-sm font-bold uppercase tracking-wider mb-2 text-cyan-400';
        title.innerText = t('saude.normalTitulo');
        action.innerHTML = `<p class="text-xs text-slate-400 font-semibold mb-1">${t('saude.acaoLabel')}</p>
            <p class="text-sm text-slate-200">${data.acaoSugerida || ''}</p>`;
    }
}
