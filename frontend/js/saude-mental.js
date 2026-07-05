import { saudeCheckin, historicoSaude, buscarSugestoes } from './api.js';
import { t, getIdioma } from './i18n.js';
import { nivelPorNota, renderNivelDerivacaoPanel } from './saude-shared.js';

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

// Escala única de check-in (CVV v2): nota é a ÚNICA entrada que deriva ao
// CVV. Emoji é opcional — texto livre sozinho continua válido (nota=null).
let notaSelecionada = null;

function selectMood(btn, nota) {
    document.querySelectorAll('.mood-btn').forEach(el => {
        el.classList.remove('border-cyan-500', 'bg-slate-800');
        el.classList.add('border-slate-800', 'bg-slate-950');
        el.setAttribute('aria-pressed', 'false');
    });
    btn.classList.remove('border-slate-800', 'bg-slate-950');
    btn.classList.add('border-cyan-500', 'bg-slate-800');
    btn.setAttribute('aria-pressed', 'true');
    notaSelecionada = nota;
}
window.selectMood = selectMood;

// Aplica os textos do idioma atual nos rótulos/âncoras/modal estáticos.
function aplicarTextos() {
    const set = (id, txt) => { const el = document.getElementById(id); if (el) el.textContent = txt; };
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
            nota: notaSelecionada,
            contexto: document.getElementById('healthContext')?.value || '',
            idioma: getIdioma(),
        });
        const leituraEl = document.getElementById('aiLeituraEmocional');
        if (leituraEl) {
            if (data.leituraEmocional) {
                leituraEl.textContent = data.leituraEmocional;
                leituraEl.classList.remove('hidden');
            } else {
                leituraEl.classList.add('hidden');
            }
        }
        renderNivelDerivacaoPanel({
            container: document.getElementById('aiResponseContainer'),
            title: document.getElementById('aiResponseTitle'),
            msg: document.getElementById('aiResponseMsg'),
            action: document.getElementById('aiResponseAction'),
        }, data);
    } catch { alert(t('saude.erroSalvar')); }
    finally { if (submitBtn) { submitBtn.disabled = false; submitBtn.textContent = t('saude.enviarRegistro'); } }
}

// Passo de confirmação empática (Plano A): para nota 1 (REFORÇADO), confirma
// antes de gravar. A confirmação é o único filtro do clique acidental — texto
// e IA nunca cancelam nada.
const confirmModal = document.getElementById('confirmModal');
function abrirConfirmacao() { confirmModal?.classList.remove('hidden'); }
function fecharConfirmacao() { confirmModal?.classList.add('hidden'); }

document.getElementById('confirmSim')?.addEventListener('click', () => {
    fecharConfirmacao();
    enviarCheckin();
});
document.getElementById('confirmNao')?.addEventListener('click', () => {
    // Cancela o envio: não submete, não grava. O usuário pode reescolher o nível.
    fecharConfirmacao();
});
confirmModal?.addEventListener('click', (e) => { if (e.target === confirmModal) fecharConfirmacao(); });

formSaude?.addEventListener('submit', async (event) => {
    event.preventDefault();
    const contexto = document.getElementById('healthContext')?.value || '';
    // Nada obrigatório sozinho — só exige pelo menos um dos dois (emoji ou texto).
    if (!notaSelecionada && !contexto.trim()) { alert(t('saude.alertaSemNadaPreencher')); return; }
    // Nota 1 (derivação REFORÇADA): consentimento ANTES de gravar.
    // "Cliquei sem querer" não grava nada. Sem nota, ou nota 3/5/7/9, grava direto.
    if (notaSelecionada === 1) {
        abrirConfirmacao();
        return;
    }
    await enviarCheckin();
});

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
document.addEventListener('DOMContentLoaded', aplicarTextos);

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
        grid.innerHTML = registros.slice(0, 10).map(r => {
            const nivel = nivelPorNota(r.nota);
            const emoji = nivel ? nivel.emoji : '📝';
            const rotulo = nivel ? t(nivel.labelKey) : t('saude.semNota');
            return `
            <div class="rounded-2xl bg-slate-900/60 border border-slate-800 p-4 flex items-center gap-4">
                <span class="text-2xl">${emoji}</span>
                <div class="flex-1 min-w-0">
                    <p class="text-sm font-semibold text-white">${rotulo}</p>
                    <p class="text-xs text-slate-400 truncate">${r.contexto || t('saude.semContexto')}</p>
                </div>
                <span class="text-[10px] text-slate-500 shrink-0">${r.createdAt ? new Date(r.createdAt).toLocaleDateString('pt-BR') : '—'}</span>
            </div>`;
        }).join('');
    } catch {
        empty.classList.remove('hidden');
        empty.textContent = t('saude.historicoErro');
    }
}

document.addEventListener('DOMContentLoaded', carregarHistorico);
