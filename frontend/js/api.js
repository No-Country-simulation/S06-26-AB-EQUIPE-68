/**
 * ============================================================================
 * api.js — Módulo Central de Chamadas à API BiT App
 * ============================================================================
 *
 * Centraliza todas as chamadas HTTP à API REST do backend Spring Boot.
 * Altere a constante API_BASE_URL para apontar para o ambiente correto.
 *
 * Em desenvolvimento local: http://localhost:8080
 * Em produção: https://api.seudominio.com
 */

const API_BASE_URL = 'http://localhost:8080';

/**
 * Função auxiliar para chamadas fetch com tratamento de erro padronizado.
 * @param {string} path - Caminho relativo (ex: '/api/usuarios')
 * @param {object} options - Opções do fetch (method, body, etc.)
 * @returns {Promise<object>} - JSON da resposta
 */
async function apiFetch(path, options = {}) {
    const defaultHeaders = { 'Content-Type': 'application/json' };
    const response = await fetch(`${API_BASE_URL}${path}`, {
        ...options,
        headers: { ...defaultHeaders, ...(options.headers || {}) },
    });

    if (!response.ok) {
        const errorBody = await response.text();
        throw new Error(`API Error ${response.status}: ${errorBody}`);
    }

    return response.json();
}

/**
 * Cadastra ou atualiza um usuário (onboarding).
 * Implementa upsert no backend: se o email já existir, atualiza.
 *
 * @param {{ nome, email, cidade, whatsapp, nivelProfissional, areaTecnologia, competenciasAtuais }} dados
 * @returns {Promise<{ id, nome, email, cidade, whatsapp, nivelProfissional, areaTecnologia, competenciasAtuais }>}
 */
export async function cadastrarUsuario(dados) {
    return apiFetch('/api/usuarios', {
        method: 'POST',
        body: JSON.stringify(dados),
    });
}

/**
 * Solicita análise de orientação profissional via IA Gemini.
 *
 * @param {{ usuarioId, perfil, nivel, regiao, idioma, lat, lng }} dados
 * @returns {Promise<{ gapPercentual, gapItens, trilhaSugerida, vagasCompatibles, confianca }>}
 */
export async function orientar(dados) {
    return apiFetch('/api/orientar', {
        method: 'POST',
        body: JSON.stringify(dados),
    });
}

/**
 * Envia check-in de saúde mental e recebe resposta empática da IA.
 *
 * @param {{ usuarioId, humor, notaSemanal, contexto }} dados
 * @returns {Promise<{ mensagem, acaoSugerida, derivarCvv, notaAtual, alerta }>}
 */
export async function saudeCheckin(dados) {
    return apiFetch('/api/saude', {
        method: 'POST',
        body: JSON.stringify(dados),
    });
}

/**
 * Busca o status de conectividade de rede para um usuário.
 *
 * @param {number} usuarioId
 * @param {number} [raioMetros=5000]
 * @returns {Promise<{ status, tecnologiaPredominante, cssClass }>}
 */
export async function networkStatus(usuarioId, raioMetros = 5000) {
    return apiFetch(`/api/network-status/${usuarioId}?raioMetros=${raioMetros}`);
}
