import { listarUsuarios } from './api.js';

const tableBody = document.getElementById('usuariosTableBody');
const statusEl = document.getElementById('usuariosStatus');
const btnAtualizar = document.getElementById('btnAtualizar');
const buscaNome = document.getElementById('buscaNome');

let todosUsuarios = [];

function escapeHtml(value) {
    return String(value ?? '')
        .replaceAll('&', '&amp;')
        .replaceAll('<', '&lt;')
        .replaceAll('>', '&gt;')
        .replaceAll('"', '&quot;')
        .replaceAll("'", '&#039;');
}

function renderEmpty(msg) {
    tableBody.innerHTML = `
        <tr>
            <td colspan="8" class="px-4 py-8 text-center text-slate-400">
                ${msg || 'Nenhum cadastro encontrado.'}
            </td>
        </tr>`;
}

function renderUsuarios(usuarios) {
    if (!usuarios.length) {
        renderEmpty('Nenhum resultado para essa busca.');
        return;
    }

    tableBody.innerHTML = usuarios.map(u => `
        <tr class="hover:bg-slate-800/50 transition">
            <td class="px-4 py-3 font-mono text-xs text-slate-400">${escapeHtml(u.id)}</td>
            <td class="px-4 py-3 font-semibold text-white">${escapeHtml(u.nome)}</td>
            <td class="px-4 py-3 text-slate-300">${escapeHtml(u.email)}</td>
            <td class="px-4 py-3 text-slate-300">${escapeHtml(u.cidade || '-')}</td>
            <td class="px-4 py-3 text-slate-300">${escapeHtml(u.whatsapp || '-')}</td>
            <td class="px-4 py-3 text-slate-300">${escapeHtml(u.nivelProfissional || '-')}</td>
            <td class="px-4 py-3 text-slate-300">${escapeHtml(u.areaTecnologia || '-')}</td>
            <td class="px-4 py-3 text-slate-400 max-w-xs truncate" title="${escapeHtml(u.competenciasAtuais || '')}">
                ${escapeHtml(u.competenciasAtuais || '-')}
            </td>
        </tr>`).join('');
}

function filtrarPorNome() {
    const termo = (buscaNome?.value || '').toLowerCase().trim();
    if (!termo) {
        renderUsuarios(todosUsuarios);
        statusEl.textContent = `${todosUsuarios.length} cadastro(s) encontrado(s).`;
        return;
    }
    const filtrados = todosUsuarios.filter(u =>
        (u.nome || '').toLowerCase().includes(termo)
    );
    renderUsuarios(filtrados);
    statusEncontrados = `${filtrados.length} resultado(s) para "${buscaNome.value.trim()}".`;
    statusEl.textContent = statusEncontrados;
}

let statusEncontrados = '';

async function carregarUsuarios() {
    try {
        btnAtualizar.disabled = true;
        btnAtualizar.textContent = 'Atualizando...';
        statusEl.textContent = '';
        buscaNome.value = '';

        tableBody.innerHTML = `
            <tr>
                <td colspan="8" class="px-4 py-8 text-center text-slate-400">
                    Carregando cadastrados...
                </td>
            </tr>`;

        todosUsuarios = await listarUsuarios();
        renderUsuarios(todosUsuarios);
        statusEl.textContent = `${todosUsuarios.length} cadastro(s) encontrado(s).`;
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

buscaNome?.addEventListener('input', filtrarPorNome);
btnAtualizar?.addEventListener('click', carregarUsuarios);
document.addEventListener('DOMContentLoaded', carregarUsuarios);
