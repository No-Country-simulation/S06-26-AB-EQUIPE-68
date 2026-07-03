import { saudeCheckin, historicoSaude, buscarSugestoes } from './api.js';
import { t, getIdioma } from './i18n.js';

const SESSION_KEY = 'bitapp_usuario';

function getUsuarioLogado() {
    const raw = localStorage.getItem(SESSION_KEY);
    return raw ? JSON.parse(raw) : null;
}

const usuario = getUsuarioLogado();
if (!usuario) {
    window.location.href = 'index.html?msg=auth_required';
}

const REGION_LABELS = {
    CBD_BEIRAMAR: 'Centro/Beiramar',
    TRINDADE: 'Trindade',
    UFSC: 'UFSC',
    CAMPECHE: 'Campeche',
    INGLESES: 'Ingleses',
    LAGOA_CONCEICAO: 'Lagoa da Conceição',
    ESTREITO_CAPOEIRAS: 'Estreito/Capoeiras',
    SAO_JOSE_CENTRO: 'São José — Centro',
};

// Estado do check-in — humor e nota são INDEPENDENTES (contrato do Dia 1).
// O humor alimenta só o tom do acolhimento; a nota é a única entrada que deriva.
let selectedMoodState = null;
let selectedNota = null; // nenhuma nota pré-selecionada: o envio exige escolha.

// O emoji seleciona SÓ o humor/tom — nunca mais é convertido em nota.
function selectMood(btn, mood) {
    document.querySelectorAll('.mood-btn').forEach(el => {
        el.classList.remove('border-cyan-500', 'bg-slate-800');
        el.classList.add('border-slate-800', 'bg-slate-950');
        el.setAttribute('aria-pressed', 'false');
    });
    btn.classList.remove('border-slate-800', 'bg-slate-950');
    btn.classList.add('border-cyan-500', 'bg-slate-800');
    btn.setAttribute('aria-pressed', 'true');
    selectedMoodState = mood;
}
window.selectMood = selectMood;

// Onze botões 0-10 (nenhum pré-selecionado), navegáveis por Tab. Mesmo realce
// visual dos botões de emoji. A nota é a autoavaliação da semana, só ela deriva.
function renderNotaBotoes() {
    const grid = document.getElementById('notaBotoes');
    if (!grid) return;
    grid.innerHTML = '';
    for (let n = 0; n <= 10; n++) {
        const btn = document.createElement('button');
        btn.type = 'button';
        btn.dataset.nota = String(n);
        btn.textContent = String(n);
        btn.setAttribute('aria-pressed', 'false');
        btn.setAttribute('aria-label', `Nota ${n}`);
        btn.className = 'nota-btn w-10 h-10 text-sm font-bold bg-slate-950 hover:bg-slate-800 border border-slate-800 rounded-xl transition text-slate-200 focus:ring-2 focus:ring-cyan-500 outline-none';
        btn.addEventListener('click', () => selectNota(btn, n));
        grid.appendChild(btn);
    }
}

function selectNota(btn, nota) {
    document.querySelectorAll('.nota-btn').forEach(el => {
        el.classList.remove('border-cyan-500', 'bg-slate-800', 'text-white');
        el.classList.add('border-slate-800', 'bg-slate-950', 'text-slate-200');
        el.setAttribute('aria-pressed', 'false');
    });
    btn.classList.remove('border-slate-800', 'bg-slate-950', 'text-slate-200');
    btn.classList.add('border-cyan-500', 'bg-slate-800', 'text-white');
    btn.setAttribute('aria-pressed', 'true');
    selectedNota = nota;
}

// Aplica os textos do idioma atual nos rótulos/âncoras/modal estáticos.
function aplicarTextos() {
    const set = (id, txt) => { const el = document.getElementById(id); if (el) el.textContent = txt; };
    set('notaLabel', t('saude.notaLabel'));
    set('notaAncoraMin', t('saude.notaAncoraMin'));
    set('notaAncoraMax', t('saude.notaAncoraMax'));
    set('contextoLabel', t('saude.contextoLabel'));
    set('confirmTitle', t('saude.modalTitulo'));
    set('confirmMsg', t('saude.modalMsg'));
    set('confirmSim', t('saude.modalSim'));
    set('confirmNao', t('saude.modalNao'));
}

const formSaude = document.getElementById('formSaude');

// Envio real do check-in — só chega aqui após a confirmação (quando aplicável).
// É a ÚNICA porta de gravação: um clique acidental cancelado nem toca no banco.
async function enviarCheckin() {
    const submitBtn = formSaude?.querySelector('button[type="submit"]');
    if (submitBtn) { submitBtn.disabled = true; submitBtn.innerHTML = `<span class="loader"></span> ${t('saude.salvando')}`; }
    try {
        const data = await saudeCheckin({
            usuarioId: usuario.id,
            humor: selectedMoodState,
            notaSemanal: selectedNota,
            contexto: document.getElementById('healthContext')?.value || '',
            idioma: getIdioma(),
        });
        renderAiResponse(data);
    } catch { alert(t('saude.erroSalvar')); }
    finally { if (submitBtn) { submitBtn.disabled = false; submitBtn.textContent = t('saude.enviarRegistro'); } }
}

// Passo de confirmação empática (Plano A): para nota <= 2, confirma antes de gravar.
// A confirmação é o único filtro do clique acidental — texto e IA nunca cancelam nada.
const confirmModal = document.getElementById('confirmModal');
function abrirConfirmacao() { confirmModal?.classList.remove('hidden'); }
function fecharConfirmacao() { confirmModal?.classList.add('hidden'); }

document.getElementById('confirmSim')?.addEventListener('click', () => {
    fecharConfirmacao();
    enviarCheckin();
});
document.getElementById('confirmNao')?.addEventListener('click', () => {
    // Cancela o envio: não submete, não grava. O usuário pode reescolher o humor.
    fecharConfirmacao();
});
confirmModal?.addEventListener('click', (e) => { if (e.target === confirmModal) fecharConfirmacao(); });

formSaude?.addEventListener('submit', async (event) => {
    event.preventDefault();
    if (!selectedMoodState) { alert(t('saude.alertaSemHumor')); return; }
    if (selectedNota === null) { alert(t('saude.alertaSemNota')); return; }
    // Nota 0-1 (derivação REFORÇADA): consentimento ANTES de gravar.
    // "Cliquei sem querer" não grava nada. Nota 2-10 grava direto.
    if (selectedNota <= 1) {
        abrirConfirmacao();
        return;
    }
    await enviarCheckin();
});

// Renderiza o acolhimento. O painel é escolhido por data.nivelDerivacao — decidido
// SÓ pela nota no backend. Linguagem de cuidado: nada de "SUPORTE CRÍTICO"/⚠️.
function renderAiResponse(data) {
    const container = document.getElementById('aiResponseContainer');
    const title = document.getElementById('aiResponseTitle');
    const msg = document.getElementById('aiResponseMsg');
    const action = document.getElementById('aiResponseAction');
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

async function carregarDicasLazer() {
    const regiao = usuario?.cidade;
    const subtitulo = document.getElementById('dicasSubtitulo');
    const grid = document.getElementById('dicasGrid');

    if (!usuario?.id || !grid) return;

    const nomeRegiao = REGION_LABELS[regiao] || regiao;
    if (subtitulo && regiao) subtitulo.textContent = t('saude.baseadoNaRegiao', { regiao: nomeRegiao });

    try {
        const data = await buscarSugestoes(usuario.id, getIdioma());
        const sugestoes = data?.sugestoes || [];
        grid.innerHTML = sugestoes.map(s => `
            <div class="rounded-2xl bg-slate-900/60 border border-slate-800 p-5 hover:border-emerald-500/40 transition-all duration-200 flex flex-col gap-3">
                <div class="flex items-center gap-3">
                    <span class="text-2xl">💡</span>
                    <h3 class="text-sm font-bold text-white">${s.titulo}</h3>
                </div>
                <p class="text-xs text-slate-400 leading-relaxed">${s.motivo || ''}</p>
            </div>
        `).join('');
    } catch {
        grid.innerHTML = '';
    }
}

document.addEventListener('DOMContentLoaded', carregarDicasLazer);
document.addEventListener('DOMContentLoaded', () => { renderNotaBotoes(); aplicarTextos(); });

const MOOD_EMOJIS = {
    feliz: '😊',
    cansado: '😴',
    triste: '😢',
    ansioso: '😰',
    sobrecarregado: '🤯',
};

async function carregarHistorico() {
    const grid = document.getElementById('historicoGrid');
    const empty = document.getElementById('historicoEmpty');
    if (!grid || !empty || !usuario?.id) return;

    try {
        const registros = await historicoSaude(usuario.id);
        if (!registros || registros.length === 0) {
            empty.classList.remove('hidden');
            return;
        }
        empty.classList.add('hidden');
        grid.innerHTML = registros.slice(0, 10).map(r => `
            <div class="rounded-2xl bg-slate-900/60 border border-slate-800 p-4 flex items-center gap-4">
                <span class="text-2xl">${MOOD_EMOJIS[r.humor] || '❓'}</span>
                <div class="flex-1 min-w-0">
                    <p class="text-sm font-semibold text-white capitalize">${r.humor || '—'}</p>
                    <p class="text-xs text-slate-400 truncate">${r.contexto || t('saude.semContexto')}</p>
                </div>
                <span class="text-[10px] text-slate-500 shrink-0">${r.createdAt ? new Date(r.createdAt).toLocaleDateString('pt-BR') : '—'}</span>
            </div>
        `).join('');
    } catch {
        empty.classList.remove('hidden');
        empty.textContent = t('saude.historicoErro');
    }
}

document.addEventListener('DOMContentLoaded', carregarHistorico);
