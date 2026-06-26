const API_BASE_URL = 'http://localhost:8080';

function getToken() {
    return localStorage.getItem('bitapp_token');
}

async function apiFetch(path, options = {}) {
    const headers = { 'Content-Type': 'application/json' };
    const token = getToken();
    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }

    const response = await fetch(`${API_BASE_URL}${path}`, {
        ...options,
        headers: { ...headers, ...(options.headers || {}) },
    });

    let json;
    try {
        json = await response.json();
    } catch {
        throw new Error(`API Error ${response.status}`);
    }

    if (!response.ok || !json.success) {
        throw new Error(json.error || `API Error ${response.status}`);
    }

    return json.data;
}

export async function orientar(dados) {
    return apiFetch('/api/orientar', {
        method: 'POST',
        body: JSON.stringify(dados),
    });
}

export async function saudeCheckin(dados) {
    return apiFetch('/api/saude', {
        method: 'POST',
        body: JSON.stringify(dados),
    });
}

export async function networkStatus(usuarioId, raioMetros = 5000) {
    return apiFetch(`/api/network-status/${usuarioId}?raioMetros=${raioMetros}`);
}

export async function listarUsuarios() {
    return apiFetch('/api/usuarios', { method: 'GET' });
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

export async function logout() {
    try {
        await apiFetch('/api/auth/logout', { method: 'POST' });
    } catch (e) { }
    localStorage.removeItem('bitapp_token');
    localStorage.removeItem('bitapp_refresh');
    localStorage.removeItem('bitapp_usuario');
}

export async function assessment(dados, usuarioId = 0) {
    return apiFetch(`/api/assessment?usuarioId=${usuarioId}`, {
        method: 'POST',
        body: JSON.stringify(dados),
    });
}

export async function mentalHealth(usuarioId = 0) {
    return apiFetch(`/api/mental-health?usuarioId=${usuarioId}`, {
        method: 'POST',
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
