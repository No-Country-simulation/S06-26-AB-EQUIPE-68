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

// Cada ponto tem lat/lng PRÓPRIOS (geocodificados via OpenStreetMap/Nominatim).
// O campo `regiao` é mantido pois alimenta filtro, dropdown, etiqueta do card,
// estatísticas e modal. O mapa e a rota usam p.lat/p.lng diretamente.
const pontosData = [
    { id: 1, nome: 'Lagoa da Conceição', tipo: 'Parque', regiao: 'LAGOA_CONCEICAO', lat: -27.609074, lng: -48.454245, descricao: 'Cartão-postal de Floripa: lagoa cercada por morros, com bares à beira d\'água, esportes náuticos (stand-up, caiaque) e a Avenida das Rendeiras.', gratuito: true, acessivel: true, horario: 'Diário 24h', tags: ['natureza', 'esporte', 'gastronomia'] },
    { id: 2, nome: 'Teatro Ademir Rosa (CIC)', tipo: 'Teatro', regiao: 'CBD_BEIRAMAR', lat: -27.577496, lng: -48.526197, descricao: 'Principal casa de espetáculos de Florianópolis, no Centro Integrado de Cultura. Teatro, dança, música e ópera com programação diversificada.', gratuito: false, acessivel: true, horario: 'Seg–Sáb 10h–20h', tags: ['cultura', 'espetáculos', 'teatro'] },
    { id: 3, nome: 'Parque de Coqueiros', tipo: 'Parque', regiao: 'ESTREITO_CAPOEIRAS', lat: -27.601751, lng: -48.574500, descricao: 'Parque urbano à beira-mar no continente, com pista de caminhada, ciclovia, academia ao ar livre e playground. Vista para a Baía Sul.', gratuito: true, acessivel: true, horario: 'Diário 6h–21h', tags: ['natureza', 'esporte', 'família'] },
    { id: 4, nome: 'MArquE – Museu de Arqueologia e Etnologia da UFSC', tipo: 'Museu', regiao: 'UFSC', lat: -27.602345, lng: -48.523926, descricao: 'Museu da UFSC com acervo de sambaquis, cultura indígena e exposições temporárias. Entrada gratuita.', gratuito: true, acessivel: true, horario: 'Ter–Sex 9h–18h', tags: ['cultura', 'história', 'educação'] },
    { id: 5, nome: 'Mercado Público de São José', tipo: 'Feira', regiao: 'SAO_JOSE_CENTRO', lat: -27.613403, lng: -48.625779, descricao: 'Mercado no centro histórico de São José, famoso pelas ostras frescas, gastronomia local e artesanato catarinense.', gratuito: true, acessivel: false, horario: 'Seg–Sex 7h–18h, Sáb 7h–14h', tags: ['gastronomia', 'regional', 'compras'] },
    { id: 6, nome: 'Biblioteca Pública de Santa Catarina', tipo: 'Biblioteca', regiao: 'CBD_BEIRAMAR', lat: -27.595258, lng: -48.552666, descricao: 'Maior biblioteca pública do estado, no centro de Floripa. Amplo acervo, salas de estudo, wi-fi e programação cultural.', gratuito: true, acessivel: true, horario: 'Seg–Sex 8h–19h, Sáb 9h–13h', tags: ['estudo', 'leitura', 'wifi'] },
    { id: 7, nome: 'Museu Histórico de SC (Palácio Cruz e Sousa)', tipo: 'Museu', regiao: 'CBD_BEIRAMAR', lat: -27.596914, lng: -48.550084, descricao: 'Museu na Praça XV, dentro do histórico Palácio Cruz e Sousa. Mobiliário de época, arte e a história de Santa Catarina.', gratuito: true, acessivel: false, horario: 'Ter–Sex 10h–18h, Sáb–Dom 10h–16h', tags: ['cultura', 'história', 'arte'] },
    { id: 8, nome: 'Praia do Campeche', tipo: 'Praia', regiao: 'CAMPECHE', lat: -27.685926, lng: -48.480379, descricao: 'Uma das praias mais extensas do sul da ilha. Águas claras e fortes, ótima para surf e longas caminhadas na areia.', gratuito: true, acessivel: false, horario: 'Diário', tags: ['praia', 'surf', 'natureza'] },
    { id: 9, nome: 'Praia da Armação', tipo: 'Praia', regiao: 'CAMPECHE', lat: -27.736035, lng: -48.507903, descricao: 'Antiga vila de pescadores no sul da ilha. Praia tranquila, igreja histórica e ponto de partida para a trilha da Lagoinha do Leste.', gratuito: true, acessivel: false, horario: 'Diário', tags: ['praia', 'história', 'natureza'] },
    { id: 10, nome: 'Teatro da UFSC', tipo: 'Teatro', regiao: 'UFSC', lat: -27.597853, lng: -48.521633, descricao: 'Espetáculos ligados à graduação em Artes Cênicas. Teatro, dança e música com ingressos acessíveis.', gratuito: false, acessivel: true, horario: 'Conforme programação', tags: ['teatro', 'cultura', 'estudantes'] },
    { id: 11, nome: 'Praia da Joaquina', tipo: 'Praia', regiao: 'LAGOA_CONCEICAO', lat: -27.634363, lng: -48.454295, descricao: 'Famosa pelo surf e pelas dunas de areia. Área de sandboard e trilha para o Morro da Lagoa.', gratuito: true, acessivel: false, horario: 'Diário', tags: ['praia', 'surf', 'natureza'] },
    { id: 12, nome: 'Mercado Público de Florianópolis', tipo: 'Feira', regiao: 'CBD_BEIRAMAR', lat: -27.597329, lng: -48.553060, descricao: 'Mercado centenário com peixarias, boxes de café, artesanato e gastronomia local. Patrimônio histórico da cidade.', gratuito: true, acessivel: true, horario: 'Seg–Sáb 6h–18h', tags: ['gastronomia', 'história', 'compras'] },
    { id: 13, nome: 'Parque Ecológico do Córrego Grande', tipo: 'Parque', regiao: 'TRINDADE', lat: -27.596584, lng: -48.510198, descricao: 'Parque urbano com trilhas, viveiro de mudas, orquidário, playground e fauna local. Ótimo para famílias e caminhadas.', gratuito: true, acessivel: true, horario: 'Ter–Dom 8h–18h', tags: ['natureza', 'família', 'caminhada'] },
    { id: 14, nome: 'Praia dos Ingleses', tipo: 'Praia', regiao: 'INGLESES', lat: -27.429447, lng: -48.396534, descricao: 'Praia movimentada no norte da ilha, com boa infraestrutura, restaurantes e mar próprio para banho. Ideal para famílias.', gratuito: true, acessivel: false, horario: 'Diário', tags: ['praia', 'família', 'gastronomia'] },
    { id: 15, nome: 'CentroSul – Centro de Eventos', tipo: 'Centro Cultural', regiao: 'CBD_BEIRAMAR', lat: -27.602035, lng: -48.552115, descricao: 'Maior centro de eventos de Floripa. Sedia feiras, shows, congressos e exposições durante todo o ano.', gratuito: false, acessivel: true, horario: 'Conforme eventos', tags: ['shows', 'eventos', 'cultura'] },
    { id: 16, nome: 'Parque da Lagoa do Peri', tipo: 'Parque', regiao: 'CAMPECHE', lat: -27.726084, lng: -48.507971, descricao: 'Lagoa de água doce cercada por mata atlântica. Trilhas, banho na lagoa, observação de aves e área de piquenique.', gratuito: true, acessivel: true, horario: 'Diário 8h–18h', tags: ['natureza', 'trilhas', 'aves'] },
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
                <button onclick="tracarRota(${p.id})" class="text-xs font-semibold text-emerald-400 hover:text-emerald-300 transition mr-3" aria-label="Tracar rota ate ${p.nome}">🛣️ Rota</button>
                <button onclick="focarNoMapa(${p.id})" class="text-xs font-semibold text-cyan-400 hover:text-cyan-300 transition mr-3" aria-label="Ver ${p.nome} no mapa">📍 No mapa</button>
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

// ============================================================
// MAPA LEAFLET — Camada 1: marcadores + interação com cards
// ============================================================
let mapa = null;
const marcadores = {}; // índice: id do ponto → marcador no mapa

function initMapa() {
    const el = document.getElementById('mapa');
    if (!el || mapa) return; // se não existe a div ou já foi criado, sai

    // Cria o mapa centrado em Florianópolis
    mapa = L.map('mapa').setView([-27.5954, -48.5480], 12);

    // Camada de base (OpenStreetMap — gratuito, sem chave)
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '© OpenStreetMap',
        maxZoom: 19
    }).addTo(mapa);

    // Plota um marcador para cada ponto de interesse
    pontosData.forEach(p => {
        if (p.lat == null || p.lng == null) return; // sem coordenada própria, pula

        const marcador = L.marker([p.lat, p.lng]).addTo(mapa);
        marcador.bindPopup(
            '<strong>' + p.nome + '</strong><br>' +
            '<span style="color:#666">' + p.tipo + '</span><br>' +
            p.horario
        );
        marcadores[p.id] = marcador; // guarda no índice por id
    });
}

// Chamada pelos cards: centraliza o mapa no ponto e abre o popup
window.focarNoMapa = function(id) {
    const marcador = marcadores[id];
    if (!marcador) return;
    const pos = marcador.getLatLng();
    mapa.setView(pos, 15);        // voa até o ponto com zoom
    marcador.openPopup();          // abre o popup
    // rola a tela até o mapa, para o usuário ver o resultado
    document.getElementById('mapa').scrollIntoView({ behavior: 'smooth', block: 'center' });
}

// Inicializa o mapa quando a página carrega
document.addEventListener('DOMContentLoaded', initMapa);

// ============================================================
// CAMADA 2: Localização do usuário
// ============================================================
let marcadorUsuario = null;

window.localizarUsuario = function() {
    if (!navigator.geolocation) {
        alert('Seu navegador não suporta geolocalização.');
        return;
    }

    navigator.geolocation.getCurrentPosition(
        // Sucesso: pegou a localização
        function(pos) {
            const lat = pos.coords.latitude;
            const lng = pos.coords.longitude;

            // Remove marcador anterior se já existir
            if (marcadorUsuario) {
                mapa.removeLayer(marcadorUsuario);
            }

            // Ícone azul diferente para "você"
            const iconeUsuario = L.divIcon({
                html: '<div style="background:#06b6d4;width:18px;height:18px;border-radius:50%;border:3px solid white;box-shadow:0 0 8px rgba(6,182,212,0.8);"></div>',
                className: '',
                iconSize: [18, 18],
                iconAnchor: [9, 9]
            });

            coordsUsuario = [lat, lng];
            marcadorUsuario = L.marker([lat, lng], { icon: iconeUsuario })
                .addTo(mapa)
                .bindPopup('<strong>Você está aqui</strong>')
                .openPopup();

            mapa.setView([lat, lng], 14);
        },
        // Erro: negou permissão ou falhou
        function(err) {
            if (err.code === err.PERMISSION_DENIED) {
                alert('Permissão de localização negada. Você pode ativá-la nas configurações do navegador.');
            } else {
                alert('Não foi possível obter sua localização.');
            }
        }
    );
};



// ============================================================
// CAMADA 3: Rota via OpenRouteService (pelo backend)
// ============================================================
let coordsUsuario = null;   // [lat, lng] da localizacao do usuario
let linhaRota = null;       // a linha desenhada no mapa

// Chamada pelo botao "Rota" no card — ja recebe o id do destino
window.tracarRota = async function(idDestino) {
    if (!coordsUsuario) {
        alert('Primeiro clique em "Minha localização" para definir o ponto de partida.');
        return;
    }

    const ponto = pontosData.find(p => p.id === idDestino);
    if (!ponto || ponto.lat == null || ponto.lng == null) {
        alert('Este ponto não tem coordenada disponível.');
        return;
    }
    const coordDestino = { lat: ponto.lat, lng: ponto.lng };

    try {
        const resp = await fetch(API_BASE_URL + '/api/rota', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                origem:  [coordsUsuario[1], coordsUsuario[0]],
                destino: [coordDestino.lng, coordDestino.lat]
            })
        });

        if (!resp.ok) {
            abrirNoGoogleMaps(coordsUsuario, coordDestino);
            return;
        }

        const dados = await resp.json();
        const coords = dados.features[0].geometry.coordinates;

        if (linhaRota) mapa.removeLayer(linhaRota);

        const pontosLinha = coords.map(c => [c[1], c[0]]);
        linhaRota = L.polyline(pontosLinha, { color: '#06b6d4', weight: 5, opacity: 0.8 }).addTo(mapa);
        mapa.fitBounds(linhaRota.getBounds(), { padding: [40, 40] });

        const dist = (dados.features[0].properties.summary.distance / 1000).toFixed(1);
        const min = Math.round(dados.features[0].properties.summary.duration / 60);
        document.getElementById('mapa').scrollIntoView({ behavior: 'smooth', block: 'center' });
        alert('Rota tracada: ' + dist + ' km - aprox. ' + min + ' min de carro ate ' + ponto.nome);

    } catch (err) {
        abrirNoGoogleMaps(coordsUsuario, coordDestino);
    }
};

// Fallback: abre a rota no Google Maps em nova aba
function abrirNoGoogleMaps(origem, destino) {
    const url = 'https://www.google.com/maps/dir/' + origem[0] + ',' + origem[1] + '/' + destino.lat + ',' + destino.lng;
    window.open(url, '_blank');
}
