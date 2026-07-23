import { historicoAssessment } from './api.js';
import { t, getIdioma } from './i18n.js';

const SESSION_KEY = 'bitapp_usuario';

function getUsuarioLogado() {
    const raw = localStorage.getItem(SESSION_KEY);
    return raw ? JSON.parse(raw) : null;
}

const usuario = getUsuarioLogado();

function formatarData(iso) {
    try {
        const data = new Date(iso);
        const locale = getIdioma() === 'es' ? 'es-ES' : 'pt-BR';
        return data.toLocaleDateString(locale, { day: '2-digit', month: 'long', year: 'numeric' });
    } catch {
        return iso;
    }
}

function listaHtml(itens, corTexto, icone) {
    if (!itens || itens.length === 0) return '';
    return `<ul class="mt-2 space-y-1.5">${itens.map(item =>
        `<li class="flex items-start gap-2 text-sm text-slate-300">
            <span class="${corTexto} shrink-0">${icone}</span> <span>${item}</span>
        </li>`
    ).join('')}</ul>`;
}

function cardHtml(item, index) {
    const compat = typeof item.compatibilidade === 'number' ? item.compatibilidade : 0;
    const badgeRecente = index === 0
        ? `<span class="inline-block mb-2 rounded-lg bg-cyan-950 border border-cyan-800 text-cyan-400 px-2.5 py-1 text-xs font-bold">${t('trilha.maisRecente')}</span>`
        : '';
    return `
        <li class="rounded-3xl border border-slate-800 bg-slate-900/90 p-6 shadow-xl">
            ${badgeRecente}
            <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-2">
                <p class="text-sm text-slate-400">${t('trilha.avaliacaoDe', { data: formatarData(item.createdAt) })}</p>
                <div class="flex items-center gap-3">
                    <span class="text-2xl font-black text-cyan-400">${compat}%</span>
                    <span class="text-xs text-slate-500">${t('trilha.compatibilidade')}</span>
                </div>
            </div>
            ${item.nivel ? `<p class="mt-1 text-base font-bold text-white">${t('trilha.nivelLabel')}: ${item.nivel}</p>` : ''}

            <div class="mt-4 grid grid-cols-1 sm:grid-cols-3 gap-4 border-t border-slate-800 pt-4">
                <div>
                    <p class="text-xs font-bold text-emerald-400 uppercase tracking-wider">${t('trilha.pontosFortesTitulo')}</p>
                    ${listaHtml(item.pontosFortes, 'text-emerald-500', '✅')}
                </div>
                <div>
                    <p class="text-xs font-bold text-rose-400 uppercase tracking-wider">${t('trilha.gapsTitulo')}</p>
                    ${listaHtml(item.gaps, 'text-rose-500', '❌')}
                </div>
                <div>
                    <p class="text-xs font-bold text-cyan-400 uppercase tracking-wider">${t('trilha.planoTitulo')}</p>
                    ${listaHtml(item.planoDesenvolvimento, 'text-cyan-500', '📚')}
                </div>
            </div>
        </li>`;
}

async function carregarTrilha() {
    const carregando = document.getElementById('trilhaCarregando');
    const vazio = document.getElementById('trilhaVazio');
    const erro = document.getElementById('trilhaErro');
    const timeline = document.getElementById('trilhaTimeline');

    if (!usuario) return;

    try {
        const historico = await historicoAssessment(usuario.id);
        carregando.classList.add('hidden');

        if (!historico || historico.length === 0) {
            vazio.classList.remove('hidden');
            return;
        }

        timeline.innerHTML = historico.map((item, index) => cardHtml(item, index)).join('');
    } catch (err) {
        carregando.classList.add('hidden');
        erro.classList.remove('hidden');
    }
}

document.addEventListener('DOMContentLoaded', carregarTrilha);
