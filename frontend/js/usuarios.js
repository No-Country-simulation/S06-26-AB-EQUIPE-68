/**
 * ============================================================================
 * usuarios.js — Lógica da Tela de Cadastrados (usuarios.html)
 * ============================================================================
 *
 * Carrega e exibe a lista de pessoas cadastradas no sistema BiT App.
 * Consome o endpoint GET /api/usuarios do backend Spring Boot.
 */

import { listarUsuarios } from './api.js';

const tableBody = document.getElementById('usuariosTableBody');
const statusEl = document.getElementById('usuariosStatus');
const btnAtualizar = document.getElementById('btnAtualizar');

/**
 * Escapa caracteres HTML para evitar XSS na renderização da tabela.
 */
function escapeHtml(value) {
    return String(value ?? '')
        .replaceAll('&', '&amp;')
        .replaceAll('<', '&lt;')
        .replaceAll('>', '&gt;')
        .replaceAll('"', '&quot;')
        .replaceAll("'", '&#039;');
}

function renderEmpty() {
    tableBody.innerHTML = `
        <tr>
            <td colspan="8" class="px-4 py-8 text-center text-slate-400">
                Nenhum cadastro encontrado.
            </td>
        </tr>`;
}

function renderUsuarios(usuarios) {
    if (!usuarios.length) {
        renderEmpty();
        return;
    }

    tableBody.innerHTML = usuarios.map(usuario => `
        <tr class="hover:bg-slate-800/50 transition">
            <td class="px-4 py-3 font-mono text-xs text-slate-400">${escapeHtml(usuario.id)}</td>
            <td class="px-4 py-3 font-semibold text-white">${escapeHtml(usuario.nome)}</td>
            <td class="px-4 py-3 text-slate-300">${escapeHtml(usuario.email)}</td>
            <td class="px-4 py-3 text-slate-300">${escapeHtml(usuario.cidade || '-')}</td>
            <td class="px-4 py-3 text-slate-300">${escapeHtml(usuario.whatsapp || '-')}</td>
            <td class="px-4 py-3 text-slate-300">${escapeHtml(usuario.nivelProfissional || '-')}</td>
            <td class="px-4 py-3 text-slate-300">${escapeHtml(usuario.areaTecnologia || '-')}</td>
            <td class="px-4 py-3 text-slate-400 max-w-xs truncate" title="${escapeHtml(usuario.competenciasAtuais || '')}">
                ${escapeHtml(usuario.competenciasAtuais || '-')}
            </td>
        </tr>`).join('');
}

async function carregarUsuarios() {
    try {
        btnAtualizar.disabled = true;
        btnAtualizar.textContent = 'Atualizando...';
        statusEl.textContent = '';

        tableBody.innerHTML = `
            <tr>
                <td colspan="8" class="px-4 py-8 text-center text-slate-400">
                    Carregando cadastrados...
                </td>
            </tr>`;

        const usuarios = await listarUsuarios();
        renderUsuarios(usuarios);
        statusEl.textContent = `${usuarios.length} cadastro(s) encontrado(s).`;
    } catch (err) {
        tableBody.innerHTML = `
            <tr>
                <td colspan="8" class="px-4 py-8 text-center text-rose-400">
                    Não foi possível carregar os cadastrados. Verifique se a API está rodando em <strong>http://localhost:8080</strong>.
                </td>
            </tr>`;
        statusEl.textContent = err.message;
    } finally {
        btnAtualizar.disabled = false;
        btnAtualizar.textContent = 'Atualizar';
    }
}

btnAtualizar?.addEventListener('click', carregarUsuarios);
document.addEventListener('DOMContentLoaded', carregarUsuarios);
