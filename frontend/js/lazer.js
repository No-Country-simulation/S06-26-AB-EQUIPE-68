import { t } from './i18n.js';
import { listarPontosLazer } from './api.js';

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

// Selo de zona de movimento (Vísent-c), calculado no backend por LazerService.
const ZONA_BADGE = {
    tranquila: { icone: '🌿', cor: 'emerald', chave: 'lazer.zonaTranquila' },
    moderada: { icone: '🚶', cor: 'amber', chave: 'lazer.zonaModerada' },
    movimentada: { icone: '🏙️', cor: 'rose', chave: 'lazer.zonaMovimentada' },
};

function zonaBadgeHtml(p) {
    const zona = ZONA_BADGE[p.zonaMovimento];
    if (!zona) return '';
    return `<span class="text-[10px] font-semibold px-2 py-0.5 rounded-full bg-${zona.cor}-500/10 text-${zona.cor}-400 border border-${zona.cor}-500/20">${zona.icone} ${t(zona.chave)}</span>`;
}

// Pontos migrados para o backend (LazerService, fonte única — inclui o selo de
// zonaMovimento calculado a partir da antena Vísent mais próxima). O front só
// busca via listarPontosLazer(); o mapa e a rota continuam usando p.lat/p.lng.
let pontosData = [];
let filteredPoints = [];
let allRegions = [];

async function carregarPontos() {
    const status = document.getElementById('lazerStatus');
    try {
        pontosData = await listarPontosLazer();
    } catch {
        pontosData = [];
        if (status) status.textContent = t('lazer.erroCarregar');
    }
    filteredPoints = [...pontosData];
    allRegions = [...new Set(pontosData.map(p => p.regiao))].sort();
    populateFilters();
    applyFilters();
    initMapa();
}

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
        status.textContent = t('lazer.nenhumEncontrado');
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
                    ${p.gratuito ? `<span class="text-[10px] font-semibold px-2 py-0.5 rounded-full bg-green-500/10 text-green-400 border border-green-500/20">${t('lazer.gratuito')}</span>` : `<span class="text-[10px] font-semibold px-2 py-0.5 rounded-full bg-slate-700/50 text-slate-400 border border-slate-600/30">${t('lazer.pago')}</span>`}
                    ${p.acessivel ? `<span class="text-[10px] font-semibold px-2 py-0.5 rounded-full bg-blue-500/10 text-blue-400 border border-blue-500/20">${t('lazer.acessivel')}</span>` : ''}
                    <span class="text-[10px] font-semibold px-2 py-0.5 rounded-full bg-slate-700/50 text-slate-400 border border-slate-600/30">${REGION_LABELS[p.regiao] || p.regiao}</span>
                    ${zonaBadgeHtml(p)}
                </div>
                <p class="mt-2 text-[11px] text-slate-500">${p.horario}</p>
            </div>
            <div class="mt-4 pt-3 border-t border-slate-800/60 flex items-center justify-between">
                <div class="flex flex-wrap gap-1">${(p.tags || []).map(tag => `<span class="text-[9px] text-slate-500">#${tag}</span>`).join(' ')}</div>
                <button onclick="tracarRota(${p.id})" class="text-xs font-semibold text-emerald-400 hover:text-emerald-300 transition mr-3" aria-label="Tracar rota ate ${p.nome}">${t('lazer.rota')}</button>
                <button onclick="focarNoMapa(${p.id})" class="text-xs font-semibold text-cyan-400 hover:text-cyan-300 transition mr-3" aria-label="Ver ${p.nome} no mapa">${t('lazer.noMapa')}</button>
                <button onclick="abrirModal(${p.id})" class="text-xs font-semibold text-amber-400 hover:text-amber-300 transition focus:outline-none focus:ring-2 focus:ring-amber-500 rounded" aria-label="Ver detalhes de ${p.nome}">${t('lazer.detalhes')}</button>
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
                ${zonaBadgeHtml(p)}
                <h2 class="mt-2 text-xl font-extrabold text-white">${p.nome}</h2>
                <p class="text-xs text-slate-400 mt-1">${REGION_LABELS[p.regiao] || p.regiao}</p>
            </div>
            <p class="text-sm text-slate-300 leading-relaxed">${p.descricao}</p>
            <div class="grid grid-cols-2 gap-3">
                <div class="rounded-xl bg-slate-950 p-3 border border-slate-800/60">
                    <p class="text-[10px] font-bold text-slate-400 uppercase">${t('lazer.horario')}</p>
                    <p class="text-sm text-white mt-1">${p.horario}</p>
                </div>
                <div class="rounded-xl bg-slate-950 p-3 border border-slate-800/60">
                    <p class="text-[10px] font-bold text-slate-400 uppercase">${t('lazer.acessibilidade')}</p>
                    <p class="text-sm text-white mt-1">${p.acessivel ? '✓ ' + t('lazer.acessivel') : t('lazer.acessivelParcial')}</p>
                </div>
                <div class="rounded-xl bg-slate-950 p-3 border border-slate-800/60">
                    <p class="text-[10px] font-bold text-slate-400 uppercase">${t('lazer.entrada')}</p>
                    <p class="text-sm text-white mt-1">${p.gratuito ? t('lazer.gratuito') : t('lazer.pago')}</p>
                </div>
                <div class="rounded-xl bg-slate-950 p-3 border border-slate-800/60">
                    <p class="text-[10px] font-bold text-slate-400 uppercase">${t('lazer.regiao')}</p>
                    <p class="text-sm text-white mt-1">${REGION_LABELS[p.regiao] || p.regiao}</p>
                </div>
            </div>
            <div class="flex flex-wrap gap-1.5">
                ${(p.tags || []).map(tag => `<span class="text-[10px] font-semibold px-2 py-0.5 rounded-full bg-slate-800 text-slate-400 border border-slate-700">#${tag}</span>`).join('')}
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

// Busca os pontos no backend e só então popula filtros, grid e mapa.
document.addEventListener('DOMContentLoaded', carregarPontos);

// ============================================================
// CAMADA 2: Localização do usuário
// ============================================================
let marcadorUsuario = null;

window.localizarUsuario = function() {
    if (!navigator.geolocation) {
        alert(t('lazer.erroGeoNaoSuportada'));
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
                .bindPopup(`<strong>${t('lazer.voceEstaAqui')}</strong>`)
                .openPopup();

            mapa.setView([lat, lng], 14);
        },
        // Erro: negou permissão ou falhou
        function(err) {
            if (err.code === err.PERMISSION_DENIED) {
                alert(t('lazer.erroGeoNegada'));
            } else {
                alert(t('lazer.erroGeoFalhou'));
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
        alert(t('lazer.erroSemLocalizacao'));
        return;
    }

    const ponto = pontosData.find(p => p.id === idDestino);
    if (!ponto || ponto.lat == null || ponto.lng == null) {
        alert(t('lazer.erroSemCoordenada'));
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
        alert(t('lazer.rotaTracada', { dist, min, nome: ponto.nome }));

    } catch (err) {
        abrirNoGoogleMaps(coordsUsuario, coordDestino);
    }
};

// Fallback: abre a rota no Google Maps em nova aba
function abrirNoGoogleMaps(origem, destino) {
    const url = 'https://www.google.com/maps/dir/' + origem[0] + ',' + origem[1] + '/' + destino.lat + ',' + destino.lng;
    window.open(url, '_blank');
}
