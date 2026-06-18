/**
 * ============================================================================
 * home.js — Lógica da Página Principal (index.html)
 * ============================================================================
 *
 * Gerencia:
 * - Tabs de navegação (onboarding / dashboard preview / saúde)
 * - Formulário de cadastro (envia para POST /api/usuarios)
 * - Persistência de sessão via localStorage
 * - Check-in de saúde mental
 * - Carregamento da análise de orientação (preview)
 */

import { cadastrarUsuario, orientar, saudeCheckin } from './api.js';

// ─────────────────────────────────────────────
// Estado global da sessão (lido do localStorage)
// ─────────────────────────────────────────────
const SESSION_KEY = 'bitapp_usuario';

function getUsuarioLogado() {
    const raw = localStorage.getItem(SESSION_KEY);
    return raw ? JSON.parse(raw) : null;
}

function salvarSessao(usuario) {
    localStorage.setItem(SESSION_KEY, JSON.stringify(usuario));
}

// ─────────────────────────────────────────────
// Navegação por abas
// ─────────────────────────────────────────────
let orientacaoCarregada = false;

function switchTab(tabName) {
    ['onboarding', 'dashboard', 'saude'].forEach(t => {
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

    if (tabName === 'dashboard') {
        const usuario = getUsuarioLogado();
        if (usuario && !orientacaoCarregada) {
            carregarOrientacao(usuario);
        } else if (!usuario) {
            document.getElementById('dashTrilha').innerHTML =
                '<p class="text-sm text-slate-500">Faça seu cadastro para ver sua trilha personalizada.</p>';
        }
    }

    window.scrollTo({ top: 0, behavior: 'smooth' });
}

// ─────────────────────────────────────────────
// Menu mobile
// ─────────────────────────────────────────────
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

// ─────────────────────────────────────────────
// Formulário de onboarding
// ─────────────────────────────────────────────
document.getElementById('formOnboarding')?.addEventListener('submit', async (event) => {
    event.preventDefault();

    const btn = document.getElementById('btnOnboarding');
    if (btn) {
        btn.disabled = true;
        btn.innerHTML = '<span class="loader"></span> Salvando...';
    }

    const dados = {
        nome: document.getElementById('regNome').value.trim(),
        email: document.getElementById('regEmail').value.trim(),
        cidade: document.getElementById('regCidade').value.trim(),
        whatsapp: document.getElementById('regWhatsapp').value.trim(),
        nivelProfissional: document.getElementById('regNivel').value,
        areaTecnologia: document.getElementById('regArea').value,
        competenciasAtuais: document.getElementById('regSkills').value.trim(),
    };

    try {
        const usuario = await cadastrarUsuario(dados);
        salvarSessao(usuario);

        // Redireciona para o dashboard
        window.location.href = 'dashboard.html';
    } catch (err) {
        console.error('Erro no cadastro:', err);
        mostrarErro('Ocorreu um erro ao processar seu cadastro. Tente novamente.');
        if (btn) {
            btn.disabled = false;
            btn.textContent = 'Entrar no Ecossistema BiT';
        }
    }
});

function mostrarErro(msg) {
    let erroEl = document.getElementById('formError');
    if (!erroEl) {
        erroEl = document.createElement('p');
        erroEl.id = 'formError';
        erroEl.className = 'text-sm text-rose-400 text-center mt-2';
        document.getElementById('formOnboarding')?.appendChild(erroEl);
    }
    erroEl.textContent = msg;
}

// ─────────────────────────────────────────────
// Seletor de humor
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

// Expõe para uso inline no HTML
window.selectMood = selectMood;
window.switchTab = switchTab;
window.closeMobileMenu = closeMobileMenu;

// ─────────────────────────────────────────────
// Check-in de saúde mental
// ─────────────────────────────────────────────
async function submitHealthCheck() {
    if (!selectedMoodState) {
        alert('Por favor, selecione um emoji antes de enviar.');
        return;
    }

    const usuario = getUsuarioLogado();
    if (!usuario) {
        alert('Faça seu cadastro primeiro para registrar o check-in emocional.');
        switchTab('onboarding');
        return;
    }

    try {
        const data = await saudeCheckin({
            usuarioId: usuario.id,
            humor: selectedMoodState,
            notaSemanal: selectedNoteState,
            contexto: document.getElementById('healthContext')?.value || '',
        });
        renderAiResponse(data);
    } catch (err) {
        alert('Ocorreu um erro ao enviar check-in. Tente novamente.');
    }
}

window.submitHealthCheck = submitHealthCheck;

function renderAiResponse(data) {
    const container = document.getElementById('aiResponseContainer');
    const title = document.getElementById('aiResponseTitle');
    const msg = document.getElementById('aiResponseMsg');
    const action = document.getElementById('aiResponseAction');

    container.classList.remove('hidden');

    if (data.derivarCvv) {
        container.className = 'p-6 rounded-3xl border border-rose-900 bg-rose-950/30 mt-6 animate-fade-in';
        title.className = 'text-sm font-bold uppercase tracking-wider mb-2 text-rose-400 flex items-center gap-2';
        title.innerHTML = '⚠️ Suporte Crítico Ativado (Derivação Humanizada)';
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
// Preview de orientação na aba dashboard
// ─────────────────────────────────────────────
async function carregarOrientacao(usuario) {
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

        orientacaoCarregada = true;

        document.getElementById('dashGapPercent').textContent = (data.gapPercentual || 30) + '% gap';
        document.getElementById('dashConfianca').textContent = formatConfianca(data.confianca);

        document.getElementById('dashGapItens').innerHTML = (data.gapItens || []).map(item =>
            `<li class="rounded-xl bg-slate-950 px-4 py-3 border border-slate-800/60 flex items-center gap-2">
                <span class="text-rose-500">❌</span> ${item}
            </li>`
        ).join('');

        document.getElementById('dashTrilha').innerHTML = (data.trilhaSugerida || []).map(item =>
            `<div class="p-4 rounded-xl bg-slate-950 border border-slate-800">
                <p class="text-sm font-semibold text-white">${item}</p>
                <span class="inline-block mt-2 text-xs text-cyan-400">Gratuito</span>
            </div>`
        ).join('');

        document.getElementById('dashVagas').innerHTML = (data.vagasCompatibles || []).map(item =>
            `<p class="text-sm font-bold text-white">${item}</p>`
        ).join('') || '<p class="text-sm text-slate-400">Nenhuma vaga listada.</p>';

    } catch {
        document.getElementById('dashTrilha').innerHTML =
            '<p class="text-sm text-slate-500">Complete o cadastro para ver sua trilha personalizada.</p>';
    }
}

function formatConfianca(valor) {
    if (typeof valor === 'number') {
        return valor >= 0.8 ? 'Alta' : valor >= 0.5 ? 'Média' : 'Baixa';
    }
    return valor || 'Alta';
}

// ─────────────────────────────────────────────
// Inicialização
// ─────────────────────────────────────────────
(function init() {
    const usuario = getUsuarioLogado();
    if (usuario) {
        // Mostra botão "Meu Painel" na navbar se usuário está logado
        document.getElementById('btnMeuPainel')?.classList.remove('hidden');
    }
})();
