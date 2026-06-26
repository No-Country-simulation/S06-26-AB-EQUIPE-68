import { listarCursos, buscarCurso, listarRegioesCursos } from './api.js';

const grid = document.getElementById('cursosGrid');
const statusEl = document.getElementById('cursosStatus');
const buscaInput = document.getElementById('buscaGeral');
const filtroRegiao = document.getElementById('filtroRegiao');
const filtroArea = document.getElementById('filtroArea');
const filtroModalidade = document.getElementById('filtroModalidade');
const modal = document.getElementById('cursoModal');
const modalContent = document.getElementById('modalContent');
const closeModal = document.getElementById('closeModal');
const totalEl = document.getElementById('cursosTotal');
const regioesEl = document.getElementById('cursosRegioes');
const btnTodos = document.getElementById('btnTodos');
const btnGratuitos = document.getElementById('btnGratuitos');
const btnBeneficentes = document.getElementById('btnBeneficentes');

let filtroGratuitoAtual = null;
let filtroBeneficenteAtual = false;
let debounceTimer = null;

function formatarRegiao(r) {
    return r.replace(/_/g, ' ').replace(/\b\w/g, c => c.toUpperCase());
}

function badgeGratuito(g) {
    return g
        ? 'bg-emerald-500/15 text-emerald-400 border-emerald-500/30'
        : 'bg-amber-500/15 text-amber-400 border-amber-500/30';
}

function badgeModalidade(m) {
    switch (m) {
        case 'Online': return 'bg-cyan-500/15 text-cyan-400 border-cyan-500/30';
        case 'Presencial': return 'bg-rose-500/15 text-rose-400 border-rose-500/30';
        case 'Híbrido': return 'bg-indigo-500/15 text-indigo-400 border-indigo-500/30';
        default: return 'bg-slate-500/15 text-slate-400 border-slate-500/30';
    }
}

function badgeNivel(n) {
    switch (n) {
        case 'Básico': return 'bg-emerald-500/15 text-emerald-400 border-emerald-500/30';
        case 'Intermediário': return 'bg-cyan-500/15 text-cyan-400 border-cyan-500/30';
        case 'Avançado': return 'bg-indigo-500/15 text-indigo-400 border-indigo-500/30';
        default: return 'bg-slate-500/15 text-slate-400 border-slate-500/30';
    }
}

function atualizarBotoesAtivos() {
    [btnTodos, btnGratuitos, btnBeneficentes].forEach(b => {
        b.classList.remove('bg-emerald-500/20', 'text-emerald-400', 'border-emerald-500/40', 'bg-amber-500/20', 'text-amber-400', 'border-amber-500/40');
        b.classList.add('bg-slate-800', 'text-slate-300', 'border-slate-700');
    });

    if (filtroBeneficenteAtual) {
        btnBeneficentes.classList.remove('bg-slate-800', 'text-slate-300', 'border-slate-700');
        btnBeneficentes.classList.add('bg-amber-500/20', 'text-amber-400', 'border-amber-500/40');
    } else if (filtroGratuitoAtual === true) {
        btnGratuitos.classList.remove('bg-slate-800', 'text-slate-300', 'border-slate-700');
        btnGratuitos.classList.add('bg-emerald-500/20', 'text-emerald-400', 'border-emerald-500/40');
    } else {
        btnTodos.classList.remove('bg-slate-800', 'text-slate-300', 'border-slate-700');
        btnTodos.classList.add('bg-emerald-500/20', 'text-emerald-400', 'border-emerald-500/40');
    }
}

function renderizarCursos(cursos) {
    if (!cursos || cursos.length === 0) {
        grid.innerHTML = `
            <div class="col-span-full text-center py-16 text-slate-500">
                <p class="text-lg mb-2">Nenhum curso encontrado</p>
                <p class="text-sm">Tente ajustar os filtros de busca.</p>
            </div>`;
        statusEl.textContent = '';
        return;
    }

    statusEl.textContent = `${cursos.length} curso(s) encontrado(s)`;

    grid.innerHTML = cursos.map(c => `
        <div class="rounded-2xl bg-slate-900/60 border border-slate-800 p-6 hover:border-emerald-500/40 transition-all duration-200 cursor-pointer flex flex-col justify-between group ${c.beneficente ? 'ring-1 ring-amber-500/20' : ''}"
             data-id="${c.id}">
            <div>
                <div class="flex items-start justify-between gap-2 mb-3">
                    <h3 class="text-base font-bold text-white group-hover:text-emerald-400 transition">${c.titulo}</h3>
                    ${c.beneficente ? '<span class="shrink-0 text-xs" title="Instituição Beneficente">❤️</span>' : ''}
                </div>
                <p class="text-sm font-medium text-slate-300 mb-1">${c.instituicao}</p>
                ${c.beneficente ? `<p class="text-xs text-amber-400 font-semibold mb-2">${c.beneficente}</p>` : ''}
                <p class="text-xs text-slate-500 mb-4 line-clamp-2">${c.descricao || ''}</p>
            </div>
            <div class="space-y-3">
                <div class="flex flex-wrap gap-2">
                    <span class="text-xs font-semibold px-2.5 py-1 rounded-full border ${badgeGratuito(c.gratuito)}">${c.gratuito ? 'Gratuito' : 'Pago'}</span>
                    <span class="text-xs font-semibold px-2.5 py-1 rounded-full border ${badgeModalidade(c.modalidade)}">${c.modalidade}</span>
                    <span class="text-xs font-semibold px-2.5 py-1 rounded-full border ${badgeNivel(c.nivel)}">${c.nivel}</span>
                </div>
                <div class="flex items-center gap-3 text-xs text-slate-400">
                    <span>📍 ${formatarRegiao(c.regiao)}</span>
                    <span>⏱️ ${c.duracao}</span>
                    ${c.certificado ? '<span>📜 Certificado</span>' : ''}
                </div>
                <div class="flex items-center justify-between text-xs">
                    <span class="text-slate-500">${c.vagas} vagas</span>
                    <span class="text-emerald-400 font-semibold group-hover:underline">Ver detalhes →</span>
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
        const c = await buscarCurso(id);
        modalContent.innerHTML = `
            <div class="space-y-4">
                <div>
                    <h2 class="text-xl font-bold text-white">${c.titulo}</h2>
                    <p class="text-sm font-medium text-emerald-400 mt-1">${c.instituicao}</p>
                    ${c.beneficente ? `<p class="text-xs text-amber-400 font-semibold mt-1">❤️ ${c.beneficente}</p>` : ''}
                </div>
                <div class="flex flex-wrap gap-2">
                    <span class="text-xs font-semibold px-2.5 py-1 rounded-full border ${badgeGratuito(c.gratuito)}">${c.gratuito ? 'Gratuito' : 'Pago'}</span>
                    <span class="text-xs font-semibold px-2.5 py-1 rounded-full border ${badgeModalidade(c.modalidade)}">${c.modalidade}</span>
                    <span class="text-xs font-semibold px-2.5 py-1 rounded-full border ${badgeNivel(c.nivel)}">${c.nivel}</span>
                    ${c.certificado ? '<span class="text-xs font-semibold px-2.5 py-1 rounded-full border bg-slate-500/15 text-slate-400 border-slate-500/30">📜 Certificado</span>' : ''}
                </div>
                <div class="grid grid-cols-2 gap-3 text-sm">
                    <div class="rounded-xl bg-slate-950 border border-slate-800 p-3">
                        <span class="text-xs text-slate-500 block">Região</span>
                        <span class="text-white font-medium">📍 ${formatarRegiao(c.regiao)}</span>
                    </div>
                    <div class="rounded-xl bg-slate-950 border border-slate-800 p-3">
                        <span class="text-xs text-slate-500 block">Duração</span>
                        <span class="text-white font-medium">⏱️ ${c.duracao}</span>
                    </div>
                    <div class="rounded-xl bg-slate-950 border border-slate-800 p-3">
                        <span class="text-xs text-slate-500 block">Vagas</span>
                        <span class="text-white font-medium">${c.vagas} disponíveis</span>
                    </div>
                    <div class="rounded-xl bg-slate-950 border border-slate-800 p-3">
                        <span class="text-xs text-slate-500 block">Área</span>
                        <span class="text-white font-medium">${c.area}</span>
                    </div>
                </div>
                <div class="rounded-xl bg-slate-950 border border-slate-800 p-4">
                    <h4 class="text-xs font-bold text-slate-400 uppercase tracking-wider mb-2">Sobre o Curso</h4>
                    <p class="text-sm text-slate-300 leading-relaxed">${c.descricao || 'Sem descrição detalhada.'}</p>
                </div>
                <div class="text-xs text-slate-500 text-right">
                    Publicado em ${new Date(c.createdAt).toLocaleDateString('pt-BR')}
                </div>
            </div>`;
        modal.classList.remove('hidden');
    } catch (err) {
        console.error('Erro ao abrir curso:', err);
    }
}

closeModal?.addEventListener('click', () => modal.classList.add('hidden'));
modal?.addEventListener('click', (e) => {
    if (e.target === modal) modal.classList.add('hidden');
});

async function aplicarFiltros() {
    const params = {
        q: buscaInput.value.trim() || undefined,
        regiao: filtroRegiao.value || undefined,
        area: filtroArea.value || undefined,
        modalidade: filtroModalidade.value || undefined,
        gratuito: filtroGratuitoAtual,
    };

    try {
        let cursos;
        if (filtroBeneficenteAtual) {
            const { listarCursos: list } = await import('./api.js');
            cursos = await list({});
            cursos = cursos.filter(c => c.beneficente);
        } else {
            cursos = await listarCursos(params);
        }
        renderizarCursos(cursos);
    } catch (err) {
        grid.innerHTML = `
            <div class="col-span-full text-center py-16 text-rose-400">
                <p class="text-lg mb-2">Erro ao carregar cursos</p>
                <p class="text-sm text-slate-500">${err.message}</p>
            </div>`;
    }
}

window.filtroGratuito = function(valor) {
    filtroGratuitoAtual = valor;
    filtroBeneficenteAtual = false;
    atualizarBotoesAtivos();
    aplicarFiltros();
};

window.filtroBeneficente = function() {
    filtroBeneficenteAtual = true;
    filtroGratuitoAtual = null;
    atualizarBotoesAtivos();
    aplicarFiltros();
};

buscaInput?.addEventListener('input', () => {
    clearTimeout(debounceTimer);
    debounceTimer = setTimeout(aplicarFiltros, 300);
});

filtroRegiao?.addEventListener('change', aplicarFiltros);
filtroArea?.addEventListener('change', aplicarFiltros);
filtroModalidade?.addEventListener('change', aplicarFiltros);

async function init() {
    try {
        const [cursos, regioes] = await Promise.all([
            listarCursos({}),
            listarRegioesCursos()
        ]);

        totalEl.textContent = cursos.length;
        regioesEl.textContent = regioes.length;

        regioes.forEach(r => {
            const opt = document.createElement('option');
            opt.value = r;
            opt.textContent = formatarRegiao(r);
            filtroRegiao.appendChild(opt);
        });

        renderizarCursos(cursos);
    } catch (err) {
        console.error('Erro ao inicializar cursos:', err);
        grid.innerHTML = `
            <div class="col-span-full text-center py-16 text-slate-500">
                <p class="text-lg mb-2">Cursos indisponíveis no momento</p>
                <p class="text-sm">Verifique se o backend está rodando.</p>
            </div>`;
    }
}

init();
