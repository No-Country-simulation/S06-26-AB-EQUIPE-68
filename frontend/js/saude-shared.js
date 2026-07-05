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
        action.innerHTML = `<div class="p-4 bg-slate-950 border border-amber-700/40 rounded-2xl space-y-1">
            <p class="text-sm font-bold text-white">${t('saude.reforcadoCvv')}</p>
            <p class="text-xs text-slate-400">${data.acaoSugerida || ''}</p></div>`;
    } else if (data.nivelDerivacao === 'PREVENTIVO') {
        // Nota 3: escuta suave, apresentada como recurso — não como alerta.
        container.className = 'p-6 rounded-3xl border border-cyan-800/50 bg-cyan-950/20 mt-6 animate-fade-in';
        title.className = 'text-sm font-bold uppercase tracking-wider mb-2 text-cyan-300 flex items-center gap-2';
        title.innerHTML = `💬 ${t('saude.preventivoTitulo')}`;
        action.innerHTML = `<div class="p-4 bg-slate-950 border border-cyan-800/40 rounded-2xl space-y-1">
            <p class="text-sm text-slate-200">${t('saude.preventivoCvv')}</p>
            <p class="text-xs text-slate-400">${data.acaoSugerida || ''}</p></div>`;
    } else if (data.tendenciaSemana) {
        // Sem gatilho hoje, mas os últimos dias vêm pesados (CVV v2): mesma
        // estrutura do preventivo, cor distinta (índigo) e wording de semana
        // — sinaliza um padrão, não o check-in de agora.
        container.className = 'p-6 rounded-3xl border border-indigo-800/50 bg-indigo-950/20 mt-6 animate-fade-in';
        title.className = 'text-sm font-bold uppercase tracking-wider mb-2 text-indigo-300 flex items-center gap-2';
        title.innerHTML = `📈 ${t('saude.tendenciaTitulo')}`;
        action.innerHTML = `<div class="p-4 bg-slate-950 border border-indigo-800/40 rounded-2xl space-y-1">
            <p class="text-sm text-slate-200">${t('saude.tendenciaMsg')}</p>
            <p class="text-xs text-slate-400">${data.acaoSugerida || ''}</p></div>`;
    } else {
        // Nota 5/7/9 ou ausente, sem tendência: acolhimento normal.
        container.className = 'p-6 rounded-3xl border border-slate-800 bg-slate-900/60 mt-6 animate-fade-in';
        title.className = 'text-sm font-bold uppercase tracking-wider mb-2 text-cyan-400';
        title.innerText = t('saude.normalTitulo');
        action.innerHTML = `<p class="text-xs text-slate-400 font-semibold mb-1">${t('saude.acaoLabel')}</p>
            <p class="text-sm text-slate-200">${data.acaoSugerida || ''}</p>`;
    }
}
