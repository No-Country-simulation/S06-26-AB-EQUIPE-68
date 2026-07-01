import { assessment, logout, listarVagas, listarCursos, networkStatus } from './api.js';

const SESSION_KEY = 'bitapp_usuario';

function getUsuarioLogado() {
    const raw = localStorage.getItem(SESSION_KEY);
    return raw ? JSON.parse(raw) : null;
}

const usuario = getUsuarioLogado();
if (!usuario) {
    window.location.href = 'index.html';
}

if (usuario) {
    const nomeEl = document.getElementById('dashUsuarioNome');
    if (nomeEl) nomeEl.textContent = `Olá, ${usuario.nome}`;
}

function closeMobileMenu() {
    document.getElementById('mobileNav')?.classList.add('hidden');
    document.getElementById('menuToggle')?.setAttribute('aria-expanded', 'false');
}
window.closeMobileMenu = closeMobileMenu;

document.getElementById('menuToggle')?.addEventListener('click', () => {
    const mobileNav = document.getElementById('mobileNav');
    const toggle = document.getElementById('menuToggle');
    const isOpen = !mobileNav.classList.contains('hidden');
    mobileNav.classList.toggle('hidden', isOpen);
    toggle.setAttribute('aria-expanded', String(!isOpen));
});

async function handleLogout() {
    await logout();
    window.location.href = 'index.html';
}
window.handleLogout = handleLogout;

// Dashboard usa o Assessment Agent (POST /api/assessment). Enquanto o n8n do Tiago
// não estiver plugado, o backend responde com o FALLBACK do AssessmentService — o que
// é o comportamento esperado. Formato: {compatibilidade, nivel, pontosFortes[], gaps[],
// planoDesenvolvimento[]}.
async function carregarAssessment() {
    if (!usuario) return;
    try {
        // Monta o request a partir do que existir no perfil do usuário logado.
        const competencias = (usuario.competenciasAtuais || '')
            .split(',').map(s => s.trim()).filter(Boolean);
        const data = await assessment({
            nome: usuario.nome || 'Usuário',
            idade: null,
            escolaridade: null,
            experiencia: usuario.nivelProfissional || null,
            hardSkills: competencias,
            softSkills: [],
            tecnologias: usuario.areaTecnologia ? [usuario.areaTecnologia] : [],
        }, usuario.id);

        const match = typeof data.compatibilidade === 'number' ? data.compatibilidade : 0;
        const matchEl = document.getElementById('dashMatchPercent');
        if (matchEl) matchEl.textContent = match + '%';

        const gapList = document.getElementById('dashGapItens');
        if (gapList) {
            gapList.innerHTML = (data.gaps || []).map(item =>
                `<li class="rounded-xl bg-slate-950 px-4 py-3 border border-slate-800/60 flex items-center gap-2">
                    <span class="text-rose-500">❌</span> ${item}</li>`
            ).join('');
        }

        const trilha = document.getElementById('dashTrilha');
        if (trilha) {
            trilha.innerHTML = (data.planoDesenvolvimento || []).map(item =>
                `<article class="rounded-2xl bg-slate-950/80 p-5 border border-slate-800 hover:border-slate-700 transition">
                    <h3 class="text-base font-bold text-white">${item}</h3>
                    <span class="inline-block mt-2 rounded-lg bg-cyan-950 border border-cyan-800 text-cyan-400 px-2.5 py-1 text-xs font-bold">Recomendado</span>
                </article>`
            ).join('');
        }

        const vagaTitulo = document.getElementById('dashVagaTitulo');
        const vagaDesc = document.getElementById('dashVagaDesc');
        if (vagaTitulo) vagaTitulo.textContent = data.nivel ? `Nível estimado: ${data.nivel}` : 'Perfil analisado';
        if (vagaDesc) {
            const fortes = data.pontosFortes || [];
            vagaDesc.textContent = fortes.length > 0
                ? `Pontos fortes: ${fortes.join('; ')}.`
                : `Compatibilidade de ${match}% com o mercado. Foque no seu plano de desenvolvimento.`;
        }
    } catch (err) {
        const vagaTitulo = document.getElementById('dashVagaTitulo');
        if (vagaTitulo) vagaTitulo.textContent = 'Análise indisponível';
    }
}

document.addEventListener('DOMContentLoaded', carregarAssessment);

async function loadSignal() {
    const badge = document.getElementById('signalBadge');
    const text = document.getElementById('signalText');
    if (!badge || !text) return;
    badge.classList.remove('hidden');
    try {
        if (usuario?.id) {
            const d = await networkStatus(usuario.id);
            text.textContent = d.status === 'Estavel' ? `Rede Estável — ${d.tecnologiaPredominante || '4G'}` : 'Rede Instável';
            return;
        }
        text.textContent = 'Rede Estável — 4G';
    } catch { text.textContent = 'Rede Estável — 4G'; }
}
document.addEventListener('DOMContentLoaded', loadSignal);

const REGION_LABELS = {
    CBD_BEIRAMAR: 'Centro/Beiramar',
    TRINDADE: 'Trindade',
    UFSC: 'UFSC',
    CAMPECHE: 'Campeche',
    INGLESES: 'Ingleses',
    SAO_JOSE_CENTRO: 'São José — Centro',
    ESTREITO_CAPOEIRAS: 'Estreito/Capoeiras',
    LAGOA_CONCEICAO: 'Lagoa da Conceição',
};

const AREA_MAP = {
    Java: ['Java', 'Dados', 'Web'],
    Web: ['Web', 'Java', 'Dados'],
    Dados: ['Dados', 'Java', 'Web'],
    Mobile: ['Java', 'Web'],
    DevOps: ['Infraestrutura', 'Java'],
    Ciberseguranca: ['Infraestrutura', 'Web'],
    IA: ['Dados', 'Java'],
    Games: ['Web', 'Java'],
    UIUX: ['Web', 'Dados'],
    Infraestrutura: ['Infraestrutura', 'Java'],
};

function createVagaCard(vaga) {
    const card = document.createElement('div');
    card.className = 'rounded-2xl bg-slate-900/40 border border-slate-800 p-5 hover:border-cyan-500/50 transition backdrop-blur-md';
    card.innerHTML = `
        <h4 class="text-sm font-bold text-white truncate">${vaga.titulo}</h4>
        <p class="text-xs text-slate-400 mt-1">${vaga.empresa}</p>
        <div class="flex flex-wrap gap-2 mt-3">
            <span class="text-[10px] font-semibold px-2 py-0.5 rounded-full bg-cyan-500/10 text-cyan-400 border border-cyan-500/20">${vaga.nivel}</span>
            <span class="text-[10px] font-semibold px-2 py-0.5 rounded-full bg-indigo-500/10 text-indigo-400 border border-indigo-500/20">${vaga.area}</span>
            ${vaga.remoto ? `<span class="text-[10px] font-semibold px-2 py-0.5 rounded-full bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">${vaga.remoto}</span>` : ''}
            ${vaga.salario ? `<span class="text-[10px] font-semibold px-2 py-0.5 rounded-full bg-amber-500/10 text-amber-400 border border-amber-500/20">${vaga.salario}</span>` : ''}
        </div>
        ${vaga.tecnologias ? `<p class="text-[11px] text-slate-500 mt-2 truncate">${vaga.tecnologias}</p>` : ''}
    `;
    return card;
}

function createCursoCard(curso) {
    const card = document.createElement('div');
    card.className = 'rounded-2xl bg-slate-900/40 border border-slate-800 p-5 hover:border-indigo-500/50 transition backdrop-blur-md';
    card.innerHTML = `
        <h4 class="text-sm font-bold text-white truncate">${curso.titulo}</h4>
        <p class="text-xs text-slate-400 mt-1">${curso.instituicao}</p>
        <div class="flex flex-wrap gap-2 mt-3">
            <span class="text-[10px] font-semibold px-2 py-0.5 rounded-full bg-indigo-500/10 text-indigo-400 border border-indigo-500/20">${curso.area}</span>
            ${curso.modalidade ? `<span class="text-[10px] font-semibold px-2 py-0.5 rounded-full bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">${curso.modalidade}</span>` : ''}
            ${curso.gratuito ? '<span class="text-[10px] font-semibold px-2 py-0.5 rounded-full bg-green-500/10 text-green-400 border border-green-500/20">Gratuito</span>' : ''}
            ${curso.duracao ? `<span class="text-[10px] font-semibold px-2 py-0.5 rounded-full bg-slate-700/50 text-slate-400 border border-slate-600/30">${curso.duracao}</span>` : ''}
        </div>
    `;
    return card;
}

async function carregarRecomendacoes() {
    if (!usuario?.cidade) return;

    const section = document.getElementById('recomendacoesRegionais');
    const subtitulo = document.getElementById('recomendacoesSubtitulo');
    const sectionVagas = document.getElementById('recomendacoesVagas');
    const sectionCursos = document.getElementById('recomendacoesCursos');
    const listaVagas = document.getElementById('listaVagasRecomendadas');
    const listaCursos = document.getElementById('listaCursosRecomendados');

    const regiao = usuario.cidade;
    const nomeRegiao = REGION_LABELS[regiao] || regiao;
    const areas = AREA_MAP[usuario.areaTecnologia] || ['Java', 'Web', 'Dados'];

    subtitulo.textContent = `Baseado na sua região — ${nomeRegiao}`;

    let vagasEncontradas = [];
    let cursosEncontrados = [];

    for (const area of areas) {
        if (vagasEncontradas.length < 4) {
            try {
                const result = await listarVagas({ regiao, area });
                const list = Array.isArray(result) ? result : [];
                vagasEncontradas = [...vagasEncontradas, ...list.filter(v => !vagasEncontradas.find(x => x.id === v.id))];
            } catch { }
        }
        if (cursosEncontrados.length < 4) {
            try {
                const result = await listarCursos({ regiao, area });
                const list = Array.isArray(result) ? result : [];
                cursosEncontrados = [...cursosEncontrados, ...list.filter(c => !cursosEncontrados.find(x => x.id === c.id))];
            } catch { }
        }
    }

    if (vagasEncontradas.length === 0) {
        try {
            const result = await listarVagas({});
            vagasEncontradas = (Array.isArray(result) ? result : []).slice(0, 4);
        } catch { }
    }

    if (cursosEncontrados.length === 0) {
        try {
            const result = await listarCursos({});
            cursosEncontrados = (Array.isArray(result) ? result : []).slice(0, 4);
        } catch { }
    }

    section.classList.remove('hidden');

    if (vagasEncontradas.length > 0) {
        sectionVagas.classList.remove('hidden');
        listaVagas.innerHTML = '';
        vagasEncontradas.slice(0, 4).forEach(v => listaVagas.appendChild(createVagaCard(v)));
    }

    if (cursosEncontrados.length > 0) {
        sectionCursos.classList.remove('hidden');
        listaCursos.innerHTML = '';
        cursosEncontrados.slice(0, 4).forEach(c => listaCursos.appendChild(createCursoCard(c)));
    }
}

document.addEventListener('DOMContentLoaded', carregarRecomendacoes);
