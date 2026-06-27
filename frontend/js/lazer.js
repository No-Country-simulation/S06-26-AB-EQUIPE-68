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

const REGION_COORDS = {
    CBD_BEIRAMAR: { lat: -27.5954, lng: -48.5480 },
    TRINDADE: { lat: -27.5970, lng: -48.5180 },
    UFSC: { lat: -27.6012, lng: -48.5210 },
    CAMPECHE: { lat: -27.6045, lng: -48.5812 },
    INGLESES: { lat: -27.4350, lng: -48.3820 },
    SAO_JOSE_CENTRO: { lat: -27.5950, lng: -48.6150 },
    ESTREITO_CAPOEIRAS: { lat: -27.5721, lng: -48.5043 },
    LAGOA_CONCEICAO: { lat: -27.6120, lng: -48.4610 },
};

const pontosData = [
    { id: 1, nome: 'Parque Municipal da Lagoa da Conceição', tipo: 'Parque', regiao: 'LAGOA_CONCEICAO', descricao: 'Trilhas ecológicas, áreas de lazer à beira da lagoa, playground e espaço para piquenique. Ideal para caminhadas ao entardecer.', gratuito: true, acessivel: true, horario: 'Diário 6h–20h', tags: ['natureza', 'caminhada', 'família'] },
    { id: 2, nome: 'Teatro Ademir Rosa', tipo: 'Teatro', regiao: 'TRINDADE', descricao: 'Principal casa de espetáculos de Florianópolis. Teatro, dança, música e ópera com programação cultural diversificada.', gratuito: false, acessivel: true, horario: 'Seg–Sáb 10h–20h', tags: ['cultura', 'espetáculos', 'teatro'] },
    { id: 3, nome: 'Parque da Cidade Dona Sarah Kubitschek', tipo: 'Parque', regiao: 'ESTREITO_CAPOEIRAS', descricao: 'Maior parque urbano da ilha com 870 mil m². Lago, trilhas, quadras esportivas, rocha de escalada e playground.', gratuito: true, acessivel: true, horario: 'Diário 6h–21h', tags: ['natureza', 'esporte', 'família'] },
    { id: 4, nome: 'Museu da UFSC', tipo: 'Museu', regiao: 'UFSC', descricao: 'Acervo de artes visuais, fotografias e exposições temporárias. Entrada gratuita para estudantes.', gratuito: true, acessivel: true, horario: 'Seg–Sex 9h–17h', tags: ['cultura', 'arte', 'educação'] },
    { id: 5, nome: 'Feira de São José', tipo: 'Feira', regiao: 'SAO_JOSE_CENTRO', descricao: 'Feira artesanal e gastronômica com produtos regionais, artesanato catarinense e comidas típicas.', gratuito: true, acessivel: false, horario: 'Sáb 8h–13h', tags: ['gastronomia', 'compras', 'regional'] },
    { id: 6, nome: 'Biblioteca Pública Alcides Carlos de Carvalho', tipo: 'Biblioteca', regiao: 'TRINDADE', descricao: 'Acervo de mais de 100 mil títulos. Espaço de estudo, wifi gratuito, salas de reunião e programação cultural.', gratuito: true, acessivel: true, horario: 'Seg–Sáb 9h–20h', tags: ['estudo', 'leitura', 'wifi'] },
    { id: 7, nome: 'Centro Cultural García de Resende', tipo: 'Centro Cultural', regiao: 'CBD_BEIRAMAR', descricao: 'Espaço cultural com exposições, oficinas de arte, eventos musicais e teatrais. Programação gratuita.', gratuito: true, acessivel: true, horario: 'Ter–Dom 10h–18h', tags: ['cultura', 'arte', 'oficinas'] },
    { id: 8, nome: 'Praia do Campeche', tipo: 'Praia', regiao: 'CAMPECHE', descricao: 'Uma das praias mais bonitas de Floripa. Águas claras, arrecife de corais e acesso ao Parque Estadual.', gratuito: true, acessivel: false, horario: 'Diário 6h–18h', tags: ['praia', 'natureza', 'mergulho'] },
    { id: 9, nome: 'Parque Estadual do Campeche', tipo: 'Parque', regiao: 'CAMPECHE', descricao: 'Ilha com sítios arqueológicos, trilhas, mirantes e piscinas naturais. Patrimônio histórico e ecológico.', gratuito: true, acessivel: false, horario: 'Diário 8h–16h', tags: ['natureza', 'história', 'trilhas'] },
    { id: 10, nome: 'Teatro da UFSC', tipo: 'Teatro', regiao: 'UFSC', descricao: 'Espetáculos de graduação em Artes Cênicas. Teatro, dança e música com ingressos acessíveis.', gratuito: false, acessivel: true, horario: 'Conforme programação', tags: ['teatro', 'cultura', 'estudantes'] },
    { id: 11, nome: 'Praia da Joaquina', tipo: 'Praia', regiao: 'LAGOA_CONCEICAO', descricao: 'Famosa pelo surf e dunas de areia. Área de camping e trilha para o Morro da Lagoa.', gratuito: true, acessivel: false, horario: 'Diário', tags: ['praia', 'surf', 'natureza'] },
    { id: 12, nome: 'Mercado Público de Florianópolis', tipo: 'Feira', regiao: 'CBD_BEIRAMAR', descricao: 'Mercado centenário com peixarias, barracas de café, artesanato e gastronomia local. Patrimônio histórico.', gratuito: true, acessivel: true, horario: 'Seg–Sáb 6h–18h', tags: ['gastronomia', 'história', 'compras'] },
    { id: 13, nome: 'Parque do Morro da Lagoa', tipo: 'Parque', regiao: 'LAGOA_CONCEICAO', descricao: 'Mirante com vista panorâmica da lagoa e da ilha. Trilha de dificuldade moderada, ideal para fotografia.', gratuito: true, acessivel: false, horario: 'Diário 6h–18h', tags: ['natureza', 'mirante', 'fotografia'] },
    { id: 14, nome: 'Biblioteca Comunitário do Ingleses', tipo: 'Biblioteca', regiao: 'INGLESES', descricao: 'Espaço comunitário com acervo de livros, atividades de leitura para crianças e wi-fi aberto.', gratuito: true, acessivel: true, horario: 'Seg–Sex 9h–17h', tags: ['leitura', 'comunidade', 'wifi'] },
    { id: 15, nome: 'Centro de Convenções Pella Giordano', tipo: 'Centro Cultural', regiao: 'ESTREITO_CAPOEIRAS', descricao: 'Grandes shows, feiras e eventos culturais. Programação diversificada durante o ano.', gratuito: false, acessivel: true, horario: 'Conforme eventos', tags: ['shows', 'eventos', 'cultura'] },
    { id: 16, nome: 'Parque Municipal da Lagoa do Peri', tipo: 'Parque', regiao: 'CAMPECHE', descricao: 'Lagoa de águas negras cercada por mata atlântica. Trilhas, observação de aves e área de piquenique.', gratuito: true, acessivel: true, horario: 'Diário 8h–17h', tags: ['natureza', 'trilhas', 'aves'] },
];

let filteredPoints = [...pontosData];
let allRegions = [...new Set(pontosData.map(p => p.regiao))].sort();

function populateFilters() {
    const selectRegiao = document.getElementById('filtroRegiao');
    allRegions.forEach(r => {
        const opt = document.createElement('option');
        opt.value = r;
        opt.textContent = REGION_LABELS[r] || r;
        selectRegiao.appendChild(opt);
    });
}

function applyFilters() {
    const busca = (document.getElementById('buscaGeral')?.value || '').toLowerCase();
    const regiao = document.getElementById('filtroRegiao')?.value || '';
    const tipo = document.getElementById('filtroTipo')?.value || '';
    const gratuito = document.getElementById('filtroGratuito')?.value;

    filteredPoints = pontosData.filter(p => {
        if (regiao && p.regiao !== regiao) return false;
        if (tipo && p.tipo !== tipo) return false;
        if (gratuito === 'true' && !p.gratuito) return false;
        if (busca) {
            const text = `${p.nome} ${p.tipo} ${p.descricao} ${(p.tags || []).join(' ')}`.toLowerCase();
            if (!text.includes(busca)) return false;
        }
        return true;
    });

    renderGrid();
    updateStats();
}

function renderGrid() {
    const grid = document.getElementById('lazerGrid');
    const status = document.getElementById('lazerStatus');
    if (!grid) return;

    if (filteredPoints.length === 0) {
        grid.innerHTML = '';
        status.textContent = 'Nenhum ponto encontrado com os filtros selecionados.';
        return;
    }

    status.textContent = '';
    grid.innerHTML = '';

    filteredPoints.forEach(p => {
        const card = document.createElement('article');
        card.className = 'rounded-2xl bg-slate-900/60 border border-slate-800 p-5 flex flex-col justify-between hover:border-amber-500/40 transition group';
        card.setAttribute('role', 'listitem');

        const tipoColor = {
            Parque: 'emerald', Teatro: 'purple', Museu: 'purple',
            Praia: 'cyan', Feira: 'amber', Biblioteca: 'cyan', 'Centro Cultural': 'purple'
        }[p.tipo] || 'slate';

        card.innerHTML = `
            <div>
                <div class="flex items-start justify-between gap-2">
                    <h3 class="text-sm font-bold text-white leading-snug">${p.nome}</h3>
                    <span class="shrink-0 text-[10px] font-semibold px-2 py-0.5 rounded-full bg-${tipoColor}-500/10 text-${tipoColor}-400 border border-${tipoColor}-500/20">${p.tipo}</span>
                </div>
                <p class="mt-2 text-xs text-slate-400 line-clamp-2 leading-relaxed">${p.descricao}</p>
                <div class="flex flex-wrap gap-1.5 mt-3">
                    ${p.gratuito ? '<span class="text-[10px] font-semibold px-2 py-0.5 rounded-full bg-green-500/10 text-green-400 border border-green-500/20">Gratuito</span>' : '<span class="text-[10px] font-semibold px-2 py-0.5 rounded-full bg-slate-700/50 text-slate-400 border border-slate-600/30">Pago</span>'}
                    ${p.acessivel ? '<span class="text-[10px] font-semibold px-2 py-0.5 rounded-full bg-blue-500/10 text-blue-400 border border-blue-500/20">Acessível</span>' : ''}
                    <span class="text-[10px] font-semibold px-2 py-0.5 rounded-full bg-slate-700/50 text-slate-400 border border-slate-600/30">${REGION_LABELS[p.regiao] || p.regiao}</span>
                </div>
                <p class="mt-2 text-[11px] text-slate-500">${p.horario}</p>
            </div>
            <div class="mt-4 pt-3 border-t border-slate-800/60 flex items-center justify-between">
                <div class="flex flex-wrap gap-1">${(p.tags || []).map(t => `<span class="text-[9px] text-slate-500">#${t}</span>`).join(' ')}</div>
                <button onclick="abrirModal(${p.id})" class="text-xs font-semibold text-amber-400 hover:text-amber-300 transition focus:outline-none focus:ring-2 focus:ring-amber-500 rounded" aria-label="Ver detalhes de ${p.nome}">Detalhes →</button>
            </div>
        `;
        grid.appendChild(card);
    });
}

function updateStats() {
    const totalEl = document.getElementById('lazerTotal');
    const regioesEl = document.getElementById('lazerRegioes');
    if (totalEl) totalEl.textContent = filteredPoints.length;
    if (regioesEl) regioesEl.textContent = new Set(filteredPoints.map(p => p.regiao)).size;
}

window.abrirModal = function(id) {
    const p = pontosData.find(x => x.id === id);
    if (!p) return;

    const modal = document.getElementById('lazerModal');
    const content = document.getElementById('modalContent');

    content.innerHTML = `
        <div class="space-y-4">
            <div>
                <span class="text-[10px] font-semibold px-2 py-0.5 rounded-full bg-amber-500/10 text-amber-400 border border-amber-500/20">${p.tipo}</span>
                <h2 class="mt-2 text-xl font-extrabold text-white">${p.nome}</h2>
                <p class="text-xs text-slate-400 mt-1">${REGION_LABELS[p.regiao] || p.regiao}</p>
            </div>
            <p class="text-sm text-slate-300 leading-relaxed">${p.descricao}</p>
            <div class="grid grid-cols-2 gap-3">
                <div class="rounded-xl bg-slate-950 p-3 border border-slate-800/60">
                    <p class="text-[10px] font-bold text-slate-400 uppercase">Horário</p>
                    <p class="text-sm text-white mt-1">${p.horario}</p>
                </div>
                <div class="rounded-xl bg-slate-950 p-3 border border-slate-800/60">
                    <p class="text-[10px] font-bold text-slate-400 uppercase">Acessibilidade</p>
                    <p class="text-sm text-white mt-1">${p.acessivel ? '✓ Acessível' : 'Parcial'}</p>
                </div>
                <div class="rounded-xl bg-slate-950 p-3 border border-slate-800/60">
                    <p class="text-[10px] font-bold text-slate-400 uppercase">Entrada</p>
                    <p class="text-sm text-white mt-1">${p.gratuito ? 'Gratuito' : 'Pago'}</p>
                </div>
                <div class="rounded-xl bg-slate-950 p-3 border border-slate-800/60">
                    <p class="text-[10px] font-bold text-slate-400 uppercase">Região</p>
                    <p class="text-sm text-white mt-1">${REGION_LABELS[p.regiao] || p.regiao}</p>
                </div>
            </div>
            <div class="flex flex-wrap gap-1.5">
                ${(p.tags || []).map(t => `<span class="text-[10px] font-semibold px-2 py-0.5 rounded-full bg-slate-800 text-slate-400 border border-slate-700">#${t}</span>`).join('')}
            </div>
        </div>
    `;

    modal.classList.remove('hidden');
    document.getElementById('closeModal')?.focus();
};

document.getElementById('closeModal')?.addEventListener('click', () => {
    document.getElementById('lazerModal')?.classList.add('hidden');
});

document.getElementById('lazerModal')?.addEventListener('click', (e) => {
    if (e.target === e.currentTarget) {
        e.currentTarget.classList.add('hidden');
    }
});

document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') {
        document.getElementById('lazerModal')?.classList.add('hidden');
    }
});

['buscaGeral'].forEach(id => {
    let timeout;
    document.getElementById(id)?.addEventListener('input', () => {
        clearTimeout(timeout);
        timeout = setTimeout(applyFilters, 300);
    });
});

['filtroRegiao', 'filtroTipo', 'filtroGratuito'].forEach(id => {
    document.getElementById(id)?.addEventListener('change', applyFilters);
});

document.addEventListener('DOMContentLoaded', () => {
    populateFilters();
    applyFilters();
});
