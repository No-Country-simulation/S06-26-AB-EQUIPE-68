import { orientar, logout, listarVagas, listarCursos } from './api.js';

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
                    <span class="text-rose-500">❌</span> ${item}</li>`
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
                vagaDesc.textContent = `Mercado atende ${match}% das necessidades. Foque nos ${data.gapPercentual || 30}% restantes.`;
            }
        }
    } catch (err) {
        const vagaTitulo = document.getElementById('dashVagaTitulo');
        if (vagaTitulo) vagaTitulo.textContent = 'Análise indisponível';
    }
}

document.addEventListener('DOMContentLoaded', carregarOrientacao);

async function loadSignal() {
    const badge = document.getElementById('signalBadge');
    const text = document.getElementById('signalText');
    if (!badge || !text) return;
    badge.classList.remove('hidden');
    try {
        if (usuario?.id) {
            const resp = await fetch(`http://localhost:8080/api/network-status/${usuario.id}`);
            const json = await resp.json();
            if (json.success && json.data) {
                const d = json.data;
                text.textContent = d.status === 'Estavel' ? `Rede Estável — ${d.tecnologiaPredominante || '4G'}` : 'Rede Instável';
                return;
            }
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
