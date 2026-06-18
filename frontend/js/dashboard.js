/**
 * ============================================================================
 * dashboard.js — Lógica do Painel do Aluno (dashboard.html)
 * ============================================================================
 *
 * Gerencia:
 * - Verificação de sessão (redireciona para index.html se não logado)
 * - Carregamento da análise de orientação profissional (POST /api/orientar)
 * - Check-in de saúde mental (POST /api/saude)
 * - Navegação entre abas (dashboard / saúde)
 */

import { orientar, saudeCheckin } from './api.js';

// ─────────────────────────────────────────────
// Sessão do usuário
// ─────────────────────────────────────────────
const SESSION_KEY = 'bitapp_usuario';

function getUsuarioLogado() {
    const raw = localStorage.getItem(SESSION_KEY);
    return raw ? JSON.parse(raw) : null;
}

// Redireciona se não há usuário na sessão
const usuario = getUsuarioLogado();
if (!usuario) {
    window.location.href = 'index.html';
}

// Preenche nome na tela
if (usuario) {
    const nomeEl = document.getElementById('dashUsuarioNome');
    if (nomeEl) nomeEl.textContent = `Olá, ${usuario.nome}`;
}

// ─────────────────────────────────────────────
// Navegação por abas
// ─────────────────────────────────────────────
function switchTab(tabName) {
    ['dashboard', 'saude'].forEach(t => {
        document.getElementById(`tab-${t}`)?.classList.add('hidden');
    });
    document.getElementById(`tab-${tabName}`)?.classList.remove('hidden');

    document.querySelectorAll('.nav-tab').forEach(btn => {
        const active = btn.dataset.tab === tabName;
        btn.classList.toggle('bg-slate-800', active);
        btn.classList.toggle('border', active);
        btn.classList.toggle('border-slate-700', active);
        btn.classList.toggle('rounded-full', active);
    });

    window.scrollTo({ top: 0, behavior: 'smooth' });
}

function closeMobileMenu() {
    document.getElementById('mobileNav')?.classList.add('hidden');
    document.getElementById('menuToggle')?.setAttribute('aria-expanded', 'false');
}

document.getElementById('menuToggle')?.addEventListener('click', () => {
    const mobileNav = document.getElementById('mobileNav');
    const toggle = document.getElementById('menuToggle');
    const isOpen = !mobileNav.classList.contains('hidden');
    mobileNav.classList.toggle('hidden', isOpen);
    toggle.setAttribute('aria-expanded', String(!isOpen));
});

window.switchTab = switchTab;
window.closeMobileMenu = closeMobileMenu;

// ─────────────────────────────────────────────
// Análise de orientação profissional
// ─────────────────────────────────────────────
function formatConfianca(valor) {
    if (typeof valor === 'number') {
        return valor >= 0.8 ? 'Alta' : valor >= 0.5 ? 'Média' : 'Baixa';
    }
    return valor || 'Alta';
}

async function carregarOrientacao() {
    if (!usuario) return;

    try {
        const data = await orientar({
            usuarioId: usuario.id,
            perfil: usuario.competenciasAtuais || 'Perfil em formação',
            nivel: usuario.nivelProfissional || 'Junior',
            regiao: usuario.cidade || 'BR-SP',
            idioma: 'PT',
            lat: null,
            lng: null,
        });

        const match = 100 - (data.gapPercentual || 30);
        const matchEl = document.getElementById('dashMatchPercent');
        if (matchEl) matchEl.textContent = match + '%';

        const gapList = document.getElementById('dashGapItens');
        if (gapList) {
            gapList.innerHTML = (data.gapItens || []).map(item =>
                `<li class="rounded-xl bg-slate-950 px-4 py-3 border border-slate-800/60 flex items-center gap-2">
                    <span class="text-rose-500">❌</span> ${item}
                </li>`
            ).join('');
        }

        const trilha = document.getElementById('dashTrilha');
        if (trilha) {
            trilha.innerHTML = (data.trilhaSugerida || []).map(item =>
                `<article class="rounded-2xl bg-slate-950/80 p-5 border border-slate-800 hover:border-slate-700 transition">
                    <h3 class="text-base font-bold text-white">${item}</h3>
                    <span class="inline-block mt-2 rounded-lg bg-cyan-950 border border-cyan-800 text-cyan-400 px-2.5 py-1 text-xs font-bold">Gratuito</span>
                </article>`
            ).join('');
        }

        const vagas = data.vagasCompatibles || [];
        const vagaTitulo = document.getElementById('dashVagaTitulo');
        const vagaDesc = document.getElementById('dashVagaDesc');
        if (vagas.length > 0 && vagaTitulo) {
            vagaTitulo.textContent = vagas[0];
            if (vagaDesc) {
                vagaDesc.textContent = `O mercado já atende ${match}% das suas necessidades. Foque nos ${data.gapPercentual || 30}% restantes com a trilha sugerida.`;
            }
        }

    } catch (err) {
        console.error('Erro ao carregar orientação:', err);
        const vagaTitulo = document.getElementById('dashVagaTitulo');
        if (vagaTitulo) vagaTitulo.textContent = 'Análise indisponível no momento';
    }
}

// ─────────────────────────────────────────────
// Check-in de saúde mental
// ─────────────────────────────────────────────
let selectedMoodState = null;
let selectedNoteState = 5;

function selectMood(btn, mood, note) {
    document.querySelectorAll('.mood-btn').forEach(el => {
        el.classList.remove('border-cyan-500', 'bg-slate-800');
        el.classList.add('border-slate-800', 'bg-slate-950');
        el.setAttribute('aria-pressed', 'false');
    });
    btn.classList.remove('border-slate-800', 'bg-slate-950');
    btn.classList.add('border-cyan-500', 'bg-slate-800');
    btn.setAttribute('aria-pressed', 'true');
    selectedMoodState = mood;
    selectedNoteState = note;
}

window.selectMood = selectMood;

document.getElementById('formSaude')?.addEventListener('submit', async (event) => {
    event.preventDefault();

    if (!selectedMoodState) {
        alert('Por favor, selecione um emoji antes de enviar.');
        return;
    }

    const submitBtn = event.target.querySelector('button[type="submit"]');
    if (submitBtn) {
        submitBtn.disabled = true;
        submitBtn.innerHTML = '<span class="loader"></span> Salvando...';
    }

    try {
        const data = await saudeCheckin({
            usuarioId: usuario.id,
            humor: selectedMoodState,
            notaSemanal: selectedNoteState,
            contexto: document.getElementById('healthContext')?.value || '',
        });
        renderAiResponse(data);
    } catch {
        alert('Ocorreu um erro ao salvar o check-in. Tente novamente.');
    } finally {
        if (submitBtn) {
            submitBtn.disabled = false;
            submitBtn.textContent = 'Enviar Registro Diário';
        }
    }
});

function renderAiResponse(data) {
    const container = document.getElementById('aiResponseContainer');
    const title = document.getElementById('aiResponseTitle');
    const msg = document.getElementById('aiResponseMsg');
    const action = document.getElementById('aiResponseAction');

    container.classList.remove('hidden');

    if (data.derivarCvv) {
        container.className = 'p-6 rounded-3xl border border-rose-900 bg-rose-950/30 mt-6 animate-fade-in';
        title.className = 'text-sm font-bold uppercase tracking-wider mb-2 text-rose-400 flex items-center gap-2';
        title.innerHTML = '⚠️ Suporte Crítico Ativado (Acolhimento Humanizado)';
        msg.innerText = data.mensagem;
        action.innerHTML = `
            <div class="p-4 bg-slate-950 border border-rose-800/50 rounded-2xl space-y-2">
                <p class="text-sm font-bold text-white">Centro de Valorização da Vida (CVV) — Disque 188</p>
                <p class="text-xs text-slate-400 leading-relaxed">${data.acaoSugerida}</p>
            </div>`;
    } else {
        container.className = 'p-6 rounded-3xl border border-slate-800 bg-slate-900/60 mt-6 animate-fade-in';
        title.className = 'text-sm font-bold uppercase tracking-wider mb-2 text-cyan-400';
        title.innerText = '✨ Resposta Empática do Agente BiT';
        msg.innerText = data.mensagem;
        action.innerHTML = `
            <p class="text-xs text-slate-400 font-semibold mb-1">Sugestão de Ação Imediata:</p>
            <p class="text-sm text-slate-200">${data.acaoSugerida}</p>`;
    }
}

// ─────────────────────────────────────────────
// Inicialização
// ─────────────────────────────────────────────
document.addEventListener('DOMContentLoaded', carregarOrientacao);
