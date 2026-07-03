import { listarVagas, buscarVaga, listarRegioesVagas, enviarCurriculo, matchLoteVagas, matchVaga } from './api.js';

// Textos bilíngues (PT/ES) — mesmo padrão do lote 2 (saude-mental.js): PT é o
// idioma exibido por padrão, a troca de idioma chega no próximo lote.
const IDIOMA = 'pt';
const TEXTOS = {
    pt: {
        matchLabel: 'Match',
        semDadosBadge: 'Sem dados de match',
        facaLoginBadge: 'Faça login para ver seu match',
        matchTitulo: 'Compatibilidade com a vaga',
        carregandoMatch: 'Calculando seu match...',
        facaLoginDetalhe: 'Faça login para ver seu match com esta vaga.',
        semDadosDetalhe: 'Esta vaga não tem tecnologias cadastradas — sem dados de match.',
        voceAtende: 'Você atende {pct}% dos requisitos desta vaga.',
        atendidasTitulo: 'Você já tem:',
        faltantesTitulo: 'Ainda falta:',
        comoResolverLabel: 'Como resolver:',
        erroMatch: 'Não foi possível calcular seu match agora.',
    },
    es: {
        matchLabel: 'Match',
        semDadosBadge: 'Sin datos de match',
        facaLoginBadge: 'Inicia sesión para ver tu match',
        matchTitulo: 'Compatibilidad con la vacante',
        carregandoMatch: 'Calculando tu match...',
        facaLoginDetalhe: 'Inicia sesión para ver tu match con esta vacante.',
        semDadosDetalhe: 'Esta vacante no tiene tecnologías registradas — sin datos de match.',
        voceAtende: 'Cumples con el {pct}% de los requisitos de esta vacante.',
        atendidasTitulo: 'Ya tienes:',
        faltantesTitulo: 'Todavía falta:',
        comoResolverLabel: 'Cómo resolver:',
        erroMatch: 'No fue posible calcular tu match ahora.',
    },
};
const T = TEXTOS[IDIOMA] || TEXTOS.pt;

const grid = document.getElementById('vagasGrid');
const statusEl = document.getElementById('vagasStatus');
const buscaInput = document.getElementById('buscaGeral');
const filtroRegiao = document.getElementById('filtroRegiao');
const filtroNivel = document.getElementById('filtroNivel');
const filtroArea = document.getElementById('filtroArea');
const modal = document.getElementById('vagaModal');
const modalContent = document.getElementById('modalContent');
const closeModal = document.getElementById('closeModal');
const totalEl = document.getElementById('vagasTotal');
const regioesEl = document.getElementById('vagasRegioes');

let todasVagas = [];
let debounceTimer = null;
let matchMap = new Map(); // vagaId -> matchPercentual (null = vaga sem tecnologias)

function usuarioAtual() {
    return JSON.parse(localStorage.getItem('bitapp_usuario') || 'null');
}

function formatarRegiao(r) {
    return r.replace(/_/g, ' ').replace(/\b\w/g, c => c.toUpperCase());
}

function matchCor(pct) {
    if (pct >= 70) return 'bg-emerald-500/15 text-emerald-400 border-emerald-500/30';
    if (pct >= 40) return 'bg-amber-500/15 text-amber-400 border-amber-500/30';
    return 'bg-rose-500/15 text-rose-400 border-rose-500/30';
}

function badgeMatch(vagaId) {
    if (!usuarioAtual()) {
        return `<span class="text-xs font-semibold px-2.5 py-1 rounded-full border bg-slate-500/15 text-slate-400 border-slate-500/30">${T.facaLoginBadge}</span>`;
    }
    if (!matchMap.has(vagaId)) return '';
    const pct = matchMap.get(vagaId);
    if (pct === null) {
        return `<span class="text-xs font-semibold px-2.5 py-1 rounded-full border bg-slate-500/15 text-slate-400 border-slate-500/30">${T.semDadosBadge}</span>`;
    }
    return `<span class="text-xs font-semibold px-2.5 py-1 rounded-full border ${matchCor(pct)}">${T.matchLabel}: ${pct}%</span>`;
}

function badgeCor(nivel) {
    switch (nivel) {
        case 'Estágio': return 'bg-emerald-500/15 text-emerald-400 border-emerald-500/30';
        case 'Júnior': return 'bg-cyan-500/15 text-cyan-400 border-cyan-500/30';
        case 'Pleno': return 'bg-indigo-500/15 text-indigo-400 border-indigo-500/30';
        default: return 'bg-slate-500/15 text-slate-400 border-slate-500/30';
    }
}

function badgeRemoto(tipo) {
    switch (tipo) {
        case 'Remoto': return 'bg-green-500/15 text-green-400 border-green-500/30';
        case 'Híbrido': return 'bg-amber-500/15 text-amber-400 border-amber-500/30';
        case 'Presencial': return 'bg-rose-500/15 text-rose-400 border-rose-500/30';
        default: return 'bg-slate-500/15 text-slate-400 border-slate-500/30';
    }
}

function renderizarVagas(vagas) {
    if (!vagas || vagas.length === 0) {
        grid.innerHTML = `
            <div class="col-span-full text-center py-16 text-slate-500">
                <p class="text-lg mb-2">Nenhuma vaga encontrada</p>
                <p class="text-sm">Tente ajustar os filtros de busca.</p>
            </div>`;
        statusEl.textContent = '';
        return;
    }

    statusEl.textContent = `${vagas.length} vaga(s) encontrada(s)`;

    grid.innerHTML = vagas.map(v => `
        <div class="rounded-2xl bg-slate-900/60 border border-slate-800 p-6 hover:border-cyan-500/40 transition-all duration-200 cursor-pointer flex flex-col justify-between group"
             data-id="${v.id}">
            <div>
                <div class="flex items-start justify-between gap-2 mb-3">
                    <h3 class="text-base font-bold text-white group-hover:text-cyan-400 transition">${v.titulo}</h3>
                </div>
                <p class="text-sm font-medium text-slate-300 mb-2">${v.empresa}</p>
                <p class="text-xs text-slate-500 mb-4 line-clamp-2">${v.descricao || ''}</p>
            </div>
            <div class="space-y-3">
                <div class="flex flex-wrap gap-2">
                    <span class="text-xs font-semibold px-2.5 py-1 rounded-full border ${badgeCor(v.nivel)}">${v.nivel}</span>
                    <span class="text-xs font-semibold px-2.5 py-1 rounded-full border ${badgeRemoto(v.remoto)}">${v.remoto || '—'}</span>
                    <span class="text-xs font-semibold px-2.5 py-1 rounded-full border bg-slate-500/15 text-slate-400 border-slate-500/30">${v.tipoContrato}</span>
                    ${badgeMatch(v.id)}
                </div>
                <div class="flex items-center gap-3 text-xs text-slate-400">
                    <span>📍 ${formatarRegiao(v.regiao)}</span>
                    <span>💰 ${v.salario || '—'}</span>
                </div>
                <div class="flex flex-wrap gap-1.5 mt-2">
                    ${(v.tecnologias || '').split(',').map(t =>
                        `<span class="text-[10px] px-2 py-0.5 rounded-full bg-slate-800 text-slate-300 border border-slate-700">${t.trim()}</span>`
                    ).join('')}
                </div>
            </div>
        </div>
    `).join('');

    grid.querySelectorAll('[data-id]').forEach(card => {
        card.addEventListener('click', () => abrirModal(Number(card.dataset.id)));
    });
}

async function abrirModal(id) {
    try {
        const v = await buscarVaga(id);
        modalContent.innerHTML = `
            <div class="space-y-4">
                <div>
                    <h2 class="text-xl font-bold text-white">${v.titulo}</h2>
                    <p class="text-sm font-medium text-cyan-400 mt-1">${v.empresa}</p>
                </div>
                <div class="flex flex-wrap gap-2">
                    <span class="text-xs font-semibold px-2.5 py-1 rounded-full border ${badgeCor(v.nivel)}">${v.nivel}</span>
                    <span class="text-xs font-semibold px-2.5 py-1 rounded-full border ${badgeRemoto(v.remoto)}">${v.remoto || '—'}</span>
                    <span class="text-xs font-semibold px-2.5 py-1 rounded-full border bg-slate-500/15 text-slate-400 border-slate-500/30">${v.tipoContrato}</span>
                </div>
                <div class="grid grid-cols-2 gap-3 text-sm">
                    <div class="rounded-xl bg-slate-950 border border-slate-800 p-3">
                        <span class="text-xs text-slate-500 block">Região</span>
                        <span class="text-white font-medium">📍 ${formatarRegiao(v.regiao)}</span>
                    </div>
                    <div class="rounded-xl bg-slate-950 border border-slate-800 p-3">
                        <span class="text-xs text-slate-500 block">Salário</span>
                        <span class="text-white font-medium">💰 ${v.salario || 'A combinar'}</span>
                    </div>
                </div>
                <div class="rounded-xl bg-slate-950 border border-slate-800 p-4">
                    <h4 class="text-xs font-bold text-slate-400 uppercase tracking-wider mb-2">Descrição</h4>
                    <p class="text-sm text-slate-300 leading-relaxed">${v.descricao || 'Sem descrição detalhada.'}</p>
                </div>
                <div class="rounded-xl bg-slate-950 border border-slate-800 p-4">
                    <h4 class="text-xs font-bold text-slate-400 uppercase tracking-wider mb-2">Tecnologias</h4>
                    <div class="flex flex-wrap gap-2">
                        ${(v.tecnologias || '').split(',').map(t =>
                            `<span class="text-xs px-3 py-1 rounded-full bg-cyan-500/10 text-cyan-400 border border-cyan-500/30">${t.trim()}</span>`
                        ).join('')}
                    </div>
                </div>
                <div class="rounded-xl bg-slate-950 border border-slate-800 p-4">
                    <h4 class="text-xs font-bold text-slate-400 uppercase tracking-wider mb-2">${T.matchTitulo}</h4>
                    <div id="matchSection">${renderMatchInicial()}</div>
                </div>
                <div class="text-xs text-slate-500 text-right">
                    Publicada em ${new Date(v.createdAt).toLocaleDateString('pt-BR')}
                </div>
                <div id="enviarCurriculoArea">
                    <button onclick="enviarCurriculoParaVaga(${v.id})"
                        class="w-full rounded-2xl bg-cyan-500 px-6 py-3 text-sm font-bold text-slate-950 shadow-xl transition hover:bg-cyan-400 transform active:scale-[0.98] focus:ring-2 focus:ring-cyan-500 focus:ring-offset-2 focus:ring-offset-slate-900 outline-none">
                        Enviar Currículo
                    </button>
                    <p id="enviarCurriculoMsg" class="mt-2 text-xs text-center hidden"></p>
                </div>
            </div>`;
        modal.classList.remove('hidden');
        carregarMatchDetalhado(id);
    } catch (err) {
        console.error('Erro ao abrir vaga:', err);
    }
}

function renderMatchInicial() {
    const usuario = usuarioAtual();
    if (!usuario) {
        return `<p class="text-sm text-slate-400">${T.facaLoginDetalhe}</p>`;
    }
    return `<p class="text-sm text-slate-400"><span class="loader align-middle mr-2"></span>${T.carregandoMatch}</p>`;
}

function renderMatchDetalhe(data) {
    if (data.matchPercentual === null || data.matchPercentual === undefined) {
        return `<p class="text-sm text-slate-400">${T.semDadosDetalhe}</p>`;
    }
    const atendidasHtml = (data.skillsAtendidas || []).map(s =>
        `<span class="text-xs text-emerald-400 block">✓ ${s}</span>`).join('');
    const faltantesHtml = (data.skillsFaltantes || []).map(s =>
        `<span class="text-xs text-slate-400 block">○ ${s}</span>`).join('');
    return `
        <p class="text-2xl font-bold text-cyan-400 mb-1">${data.matchPercentual}%</p>
        <p class="text-sm text-slate-300 mb-3">${T.voceAtende.replace('{pct}', data.matchPercentual)}</p>
        <div class="grid grid-cols-2 gap-3 mb-3">
            <div>
                <span class="text-[10px] font-bold text-slate-500 uppercase block mb-1">${T.atendidasTitulo}</span>
                ${atendidasHtml || '<span class="text-xs text-slate-500">—</span>'}
            </div>
            <div>
                <span class="text-[10px] font-bold text-slate-500 uppercase block mb-1">${T.faltantesTitulo}</span>
                ${faltantesHtml || '<span class="text-xs text-slate-500">—</span>'}
            </div>
        </div>
        ${data.comoResolver ? `<p class="text-xs text-slate-400 mt-2"><strong class="text-slate-300">${T.comoResolverLabel}</strong> ${data.comoResolver}</p>` : ''}
    `;
}

async function carregarMatchDetalhado(vagaId) {
    const usuario = usuarioAtual();
    if (!usuario) return;
    const el = document.getElementById('matchSection');
    try {
        const data = await matchVaga(vagaId, usuario.id);
        if (el) el.innerHTML = renderMatchDetalhe(data);
    } catch (err) {
        console.error('Erro ao calcular match:', err);
        if (el) el.innerHTML = `<p class="text-sm text-rose-400">${T.erroMatch}</p>`;
    }
}

closeModal?.addEventListener('click', () => modal.classList.add('hidden'));
modal?.addEventListener('click', (e) => {
    if (e.target === modal) modal.classList.add('hidden');
});
document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape' && modal && !modal.classList.contains('hidden')) {
        modal.classList.add('hidden');
    }
});

window.enviarCurriculoParaVaga = async function(vagaId) {
    const usuario = JSON.parse(localStorage.getItem('bitapp_usuario') || 'null');
    if (!usuario) { window.location.href = 'index.html?msg=auth_required'; return; }
    const msgEl = document.getElementById('enviarCurriculoMsg');
    const area = document.getElementById('enviarCurriculoArea');
    const btn = area?.querySelector('button');
    if (btn) { btn.disabled = true; btn.innerHTML = '<span class="loader"></span> Enviando...'; }
    try {
        await enviarCurriculo(vagaId, usuario.id);
        if (msgEl) { msgEl.textContent = 'Currículo enviado com sucesso!'; msgEl.className = 'mt-2 text-xs text-center text-emerald-400'; }
        if (btn) { btn.textContent = 'Enviado ✓'; btn.classList.remove('bg-cyan-500'); btn.classList.add('bg-emerald-500/20', 'text-emerald-400', 'border', 'border-emerald-500/30'); }
    } catch (err) {
        if (msgEl) { msgEl.textContent = err.message || 'Erro ao enviar currículo.'; msgEl.className = 'mt-2 text-xs text-center text-rose-400'; }
        if (btn) { btn.disabled = false; btn.textContent = 'Enviar Currículo'; }
    }
};

async function aplicarFiltros() {
    const params = {
        q: buscaInput.value.trim() || undefined,
        regiao: filtroRegiao.value || undefined,
        nivel: filtroNivel.value || undefined,
        area: filtroArea.value || undefined,
    };

    try {
        const vagas = await listarVagas(params);
        todasVagas = vagas;
        renderizarVagas(vagas);
    } catch (err) {
        grid.innerHTML = `
            <div class="col-span-full text-center py-16 text-rose-400">
                <p class="text-lg mb-2">Erro ao carregar vagas</p>
                <p class="text-sm text-slate-500">${err.message}</p>
            </div>`;
    }
}

buscaInput?.addEventListener('input', () => {
    clearTimeout(debounceTimer);
    debounceTimer = setTimeout(aplicarFiltros, 300);
});

filtroRegiao?.addEventListener('change', aplicarFiltros);
filtroNivel?.addEventListener('change', aplicarFiltros);
filtroArea?.addEventListener('change', aplicarFiltros);

async function init() {
    try {
        const [vagas, regioes] = await Promise.all([
            listarVagas(),
            listarRegioesVagas()
        ]);

        todasVagas = vagas;
        totalEl.textContent = vagas.length;
        regioesEl.textContent = regioes.length;

        regioes.forEach(r => {
            const opt = document.createElement('option');
            opt.value = r;
            opt.textContent = formatarRegiao(r);
            filtroRegiao.appendChild(opt);
        });

        renderizarVagas(vagas);
        await carregarMatchLote();
    } catch (err) {
        console.error('Erro ao inicializar vagas:', err);
        grid.innerHTML = `
            <div class="col-span-full text-center py-16 text-slate-500">
                <p class="text-lg mb-2">Vagas indisponíveis no momento</p>
                <p class="text-sm">Verifique se o backend está rodando.</p>
            </div>`;
    }
}

// Um único GET para todas as vagas listadas — evita N chamadas por card.
// Sem usuário logado, nem chama o endpoint (badges mostram "Faça login").
async function carregarMatchLote() {
    const usuario = usuarioAtual();
    if (!usuario) return;
    try {
        const lista = await matchLoteVagas(usuario.id);
        matchMap = new Map(lista.map(item => [item.vagaId, item.matchPercentual]));
        renderizarVagas(todasVagas);
    } catch (err) {
        console.error('Erro ao carregar match em lote:', err);
    }
}

init();
