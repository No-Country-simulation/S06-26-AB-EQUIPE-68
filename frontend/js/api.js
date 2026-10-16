const API_BASE_URL = (window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1') ? 'http://localhost:8080' : '';

function getToken() {
    return localStorage.getItem('bitapp_token');
}

// Erro de chamada à API com o status HTTP, para quem precisa decidir pelo 401/403.
export class ApiError extends Error {
    constructor(message, status, codigo) {
        super(message);
        this.name = 'ApiError';
        this.status = status;
        this.codigo = codigo;
    }
}

// 401 = sem token, token vencido ou sessão revogada: limpa o que ficou guardado
// e leva ao login. Na própria página de login não redireciona (evita loop).
function sessaoExpirou() {
    ['bitapp_token', 'bitapp_refresh', 'bitapp_usuario'].forEach((k) => localStorage.removeItem(k));
    clearAssessmentCache();
    const pagina = window.location.pathname.split('/').pop();
    if (pagina === '' || pagina === 'index.html') return;
    window.location.href = 'index.html?msg=auth_required';
}

// options.redirecionarEm401 = false deixa o 401 com quem chamou (ex.: o check-in
// de saúde, que não pode tirar o painel do CVV da tela).
// options.bruto = true devolve o corpo como veio, para as rotas que não usam o
// envelope {success, data} (POST /api/rota repassa o GeoJSON da ORS).
async function apiFetch(path, options = {}) {
    const { redirecionarEm401 = true, bruto = false, ...fetchOptions } = options;
    const headers = { 'Content-Type': 'application/json' };
    const token = getToken();
    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }

    const response = await fetch(`${API_BASE_URL}${path}`, {
        ...fetchOptions,
        headers: { ...headers, ...(fetchOptions.headers || {}) },
    });

    if (response.status === 401 && redirecionarEm401) {
        sessaoExpirou();
    }

    let json;
    try {
        json = await response.json();
    } catch {
        throw new ApiError(`API Error ${response.status}`, response.status);
    }

    if (bruto) {
        if (!response.ok) {
            throw new ApiError(json.mensagem || json.erro || `API Error ${response.status}`, response.status, json.erro);
        }
        return json;
    }

    if (!response.ok || !json.success) {
        throw new ApiError(json.error || `API Error ${response.status}`, response.status, json.codigo);
    }

    return json.data;
}

// DESATIVADO (2026-07): o Dashboard passou a usar o Assessment Agent (assessment()).
// A chamada a /api/orientar fica comentada para retomada futura.
// export async function orientar(dados) {
//     return apiFetch('/api/orientar', {
//         method: 'POST',
//         body: JSON.stringify(dados),
//     });
// }

export async function saudeCheckin(dados) {
    return apiFetch('/api/saude', {
        method: 'POST',
        body: JSON.stringify(dados),
    });
}

export async function networkStatus(usuarioId, raioMetros = 5000) {
    return apiFetch(`/api/network-status/${usuarioId}?raioMetros=${raioMetros}`);
}

export async function register(data) {
    return apiFetch('/api/auth/register', {
        method: 'POST',
        body: JSON.stringify(data),
    });
}

export async function login(data) {
    return apiFetch('/api/auth/login', {
        method: 'POST',
        body: JSON.stringify(data),
    });
}

export async function me() {
    return apiFetch('/api/auth/me', { method: 'GET' });
}

export async function updateProfile(data) {
    return apiFetch('/api/auth/profile', {
        method: 'PUT',
        body: JSON.stringify(data),
    });
}

const ASSESSMENT_CACHE_PREFIX = 'bit_assessment_';

export function getAssessmentCache(usuarioId) {
    try {
        const raw = sessionStorage.getItem(ASSESSMENT_CACHE_PREFIX + usuarioId);
        return raw ? JSON.parse(raw) : null;
    } catch {
        return null;
    }
}

export function setAssessmentCache(usuarioId, data) {
    try {
        sessionStorage.setItem(ASSESSMENT_CACHE_PREFIX + usuarioId, JSON.stringify(data));
    } catch {
        // falha de escrita degrada para "sem cache" — próxima visita chama a API de novo
    }
}

export function clearAssessmentCache(usuarioId) {
    if (usuarioId) {
        sessionStorage.removeItem(ASSESSMENT_CACHE_PREFIX + usuarioId);
        return;
    }
    Object.keys(sessionStorage)
        .filter((k) => k.startsWith(ASSESSMENT_CACHE_PREFIX))
        .forEach((k) => sessionStorage.removeItem(k));
}

export async function logout() {
    try {
        await apiFetch('/api/auth/logout', { method: 'POST' });
    } catch (e) { }
    localStorage.removeItem('bitapp_token');
    localStorage.removeItem('bitapp_refresh');
    localStorage.removeItem('bitapp_usuario');
    clearAssessmentCache();
}

export async function assessment(dados, usuarioId = 0) {
    return apiFetch(`/api/assessment?usuarioId=${usuarioId}`, {
        method: 'POST',
        body: JSON.stringify(dados),
    });
}

export async function listarVagas(params = {}) {
    const qs = new URLSearchParams();
    if (params.q) qs.set('q', params.q);
    if (params.regiao) qs.set('regiao', params.regiao);
    if (params.nivel) qs.set('nivel', params.nivel);
    if (params.area) qs.set('area', params.area);
    if (params.contrato) qs.set('contrato', params.contrato);
    const query = qs.toString();
    return apiFetch(`/api/vagas${query ? '?' + query : ''}`);
}

export async function buscarVaga(id) {
    return apiFetch(`/api/vagas/${id}`);
}

export async function listarRegioesVagas() {
    return apiFetch('/api/vagas/regioes');
}

export async function matchLoteVagas(usuarioId) {
    return apiFetch(`/api/vagas/match-lote?usuarioId=${usuarioId}`);
}

export async function matchVaga(vagaId, usuarioId, idioma = 'pt') {
    return apiFetch(`/api/vagas/${vagaId}/match?usuarioId=${usuarioId}&idioma=${idioma}`);
}

export async function listarCursos(params = {}) {
    const qs = new URLSearchParams();
    if (params.q) qs.set('q', params.q);
    if (params.regiao) qs.set('regiao', params.regiao);
    if (params.area) qs.set('area', params.area);
    if (params.modalidade) qs.set('modalidade', params.modalidade);
    if (params.gratuito !== undefined && params.gratuito !== null) qs.set('gratuito', params.gratuito);
    if (params.nivel) qs.set('nivel', params.nivel);
    const query = qs.toString();
    return apiFetch(`/api/cursos${query ? '?' + query : ''}`);
}

export async function buscarCurso(id) {
    return apiFetch(`/api/cursos/${id}`);
}

export async function listarRegioesCursos() {
    return apiFetch('/api/cursos/regioes');
}

export async function historicoSaude(usuarioId) {
    return apiFetch(`/api/saude/historico?usuarioId=${usuarioId}`);
}

export async function historicoAssessment(usuarioId) {
    return apiFetch(`/api/assessment/historico?usuarioId=${usuarioId}`);
}

export async function salvarLocalizacao(usuarioId, latitude, longitude) {
    return apiFetch(`/api/usuarios/${usuarioId}/localizacao`, {
        method: 'PUT',
        body: JSON.stringify({ latitude, longitude }),
    });
}

// Rota de carro entre dois pontos [lng, lat], calculada pela ORS no backend.
// Devolve o GeoJSON bruto. O 401 fica com quem chamou: o mapa cai no Google Maps
// em vez de mandar o usuário para o login no meio da tela.
export async function calcularRota(origem, destino) {
    return apiFetch('/api/rota', {
        method: 'POST',
        body: JSON.stringify({ origem, destino }),
        bruto: true,
        redirecionarEm401: false,
    });
}

export async function listarPontosLazer() {
    return apiFetch('/api/lazer/pontos');
}

export async function buscarSugestoes(usuarioId, idioma = 'pt') {
    return apiFetch(`/api/sugestoes/${usuarioId}?idioma=${idioma}`);
}

export async function listarMentores() {
    return apiFetch('/api/mentores');
}

export async function solicitarMentoria(dados, usuarioId = 0) {
    return apiFetch(`/api/mentorias?usuarioId=${usuarioId}`, {
        method: 'POST',
        body: JSON.stringify(dados),
    });
}

export async function historicoMentorias(usuarioId) {
    return apiFetch(`/api/mentorias/historico?usuarioId=${usuarioId}`);
}

export async function atualizarStatusMentoria(id, status) {
    return apiFetch(`/api/mentorias/${id}/status`, {
        method: 'PATCH',
        body: JSON.stringify({ status }),
    });
}
