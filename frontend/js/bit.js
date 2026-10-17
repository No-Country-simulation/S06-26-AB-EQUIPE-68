// Card do Bit — check-in conversacional na entrada do dashboard.
// Moldura única (modal centrado, sem variantes por query param), aparece em
// toda carga do dashboard (sem guard de "já fez check-in hoje" e sem
// persistência em localStorage — "agora não" só esconde na visualização
// atual). Check-in é a escala única CVV v2 (emoji opcional) + texto livre;
// sem emoji, nota=null (não deriva, não entra em agregação/tendência).
// Reaproveita saudeCheckin/buscarSugestoes (api.js), os painéis graduados e
// o modal de consentimento (saude-shared.js / mesmos textos do DICT).
// saude-mental.html continua funcionando igual — o Bit é um caminho
// adicional, não substituto.
// CVV: com nota 1 ou 3 o painel do CVV aparece NA HORA, calculado aqui pela
// nota (nivelDerivacaoLocal), antes e independente da resposta do servidor;
// a resposta só atualiza a mensagem de acolhimento e a tendência da semana.
// Falha ou timeout nunca viram alert() e fechar o Bit não descarta o painel.
import { saudeCheckin, buscarSugestoes, sessaoExpirou } from './api.js';
import { t, getIdioma } from './i18n.js';
import {
    NIVEL_CHECKIN, nivelPorNota, nivelDerivacaoLocal, cvvBlocoHtml, renderNivelDerivacaoPanel,
    renderCvvImediato, atualizarAcolhimento, marcarAcolhimentoIndisponivel,
} from './saude-shared.js';
import { escapeHtml } from './escape.js';

const SESSION_KEY = 'bitapp_usuario';

function getUsuarioLogado() {
    const raw = localStorage.getItem(SESSION_KEY);
    return raw ? JSON.parse(raw) : null;
}

const usuario = getUsuarioLogado();

function saudacaoPorHora() {
    const hora = new Date().getHours();
    // O nome é digitado no cadastro e a saudação entra via innerHTML: escapa só o nome.
    const nome = escapeHtml(usuario.nome);
    if (hora < 12) return t('bit.saudacaoManha', { nome });
    if (hora < 18) return t('bit.saudacaoTarde', { nome });
    return t('bit.saudacaoNoite', { nome });
}

// ════════════════════════════════════════════════════════════════════════
//  MODAL — moldura única centrada (substitui as variantes card/full)
// ════════════════════════════════════════════════════════════════════════

function onEscKey(e) {
    // Com o consentimento aberto o Esc não fecha o Bit por trás dele.
    if (e.key === 'Escape' && !document.getElementById('bitConsentimento')) dispensar();
}

function onTabTrap(e) {
    if (e.key !== 'Tab') return;
    const overlay = document.getElementById('bitOverlay');
    if (!overlay) return;
    const focusables = overlay.querySelectorAll('button, [href], input, textarea, select, [tabindex]:not([tabindex="-1"])');
    if (!focusables.length) return;
    const first = focusables[0];
    const last = focusables[focusables.length - 1];
    if (e.shiftKey && document.activeElement === first) {
        e.preventDefault();
        last.focus();
    } else if (!e.shiftKey && document.activeElement === last) {
        e.preventDefault();
        first.focus();
    }
}

function criarModal() {
    const overlay = document.createElement('div');
    overlay.id = 'bitOverlay';
    overlay.className = 'fixed inset-0 z-[90] flex items-center justify-center bg-black/60 backdrop-blur-sm px-4 py-8';
    overlay.setAttribute('role', 'dialog');
    overlay.setAttribute('aria-modal', 'true');
    overlay.setAttribute('aria-label', t('bit.tituloModal'));
    overlay.innerHTML = `
        <div id="bitModalBox" class="w-full max-w-2xl max-h-[90vh] overflow-y-auto rounded-3xl border border-slate-800 bg-slate-900/95 p-6 sm:p-8 shadow-2xl backdrop-blur-lg relative animate-fade-in transition-[max-width]">
            <button type="button" id="bitFechar" aria-label="${t('bit.dispensar')}"
                class="absolute top-4 right-4 w-9 h-9 rounded-full bg-slate-800 border border-slate-700 text-slate-300 hover:text-white hover:border-slate-500 transition flex items-center justify-center">✕</button>
            <div id="bitBubbles" class="space-y-4 pr-2"></div>
        </div>
    `;
    document.body.appendChild(overlay);
    overlay.querySelector('#bitFechar').addEventListener('click', dispensar);
    overlay.addEventListener('click', (e) => { if (e.target === overlay) dispensar(); });
    document.addEventListener('keydown', onEscKey);
    overlay.addEventListener('keydown', onTabTrap);
    overlay.querySelector('#bitFechar').focus(); // foco entra no modal ao abrir
    return overlay.querySelector('#bitBubbles');
}

// Linha (avatar + painel) do CVV, quando o check-in derivou ao CVV nesta visualização.
let painelCvvRow = null;

// Cartão fixo que guarda o painel do CVV depois que o Bit é fechado. O nó do painel é
// movido (não recriado), então a resposta que ainda estiver a caminho continua atualizando-o.
// A pessoa pode recolher o cartão numa pílula no canto inferior direito, que mantém o
// "Ligar 188" sempre à mão. O estado recolhido vale só nesta visualização (sem localStorage).
const CVV_CARTAO_ABERTO = 'fixed bottom-4 left-4 right-4 sm:left-auto sm:w-96 z-[85] max-h-[70vh] flex flex-col rounded-3xl border border-slate-700 bg-slate-900/95 p-3 shadow-2xl backdrop-blur-lg';
const CVV_CARTAO_RECOLHIDO = 'fixed bottom-4 right-4 z-[85] flex items-center gap-2 rounded-full border border-slate-700 bg-slate-900/95 p-2 shadow-2xl backdrop-blur-lg';

function criarCartaoCvv() {
    const fixo = document.createElement('aside');
    fixo.id = 'bitCvvFixo';
    fixo.setAttribute('aria-label', t('saude.reforcadoTitulo'));
    fixo.className = CVV_CARTAO_ABERTO;
    // aria-expanded é fixo em cada botão: o Recolher só aparece com o cartão aberto (true) e o
    // Abrir só com ele recolhido (false).
    fixo.innerHTML = `
        <div data-cvv-barra class="flex justify-end pb-2">
            <button type="button" data-cvv-recolher aria-expanded="true" aria-controls="bitCvvArea"
                class="rounded-full border border-slate-700 bg-slate-800 px-4 py-2 text-xs font-semibold text-slate-300 transition hover:text-white hover:border-slate-500 focus:outline-none focus:ring-2 focus:ring-amber-500">${t('bit.cvvRecolher')}</button>
        </div>
        <div id="bitCvvArea" class="min-h-0 overflow-y-auto"></div>
        <a data-cvv-ligar href="tel:188" class="hidden rounded-full bg-amber-500 px-4 py-2 text-sm font-bold text-slate-950 shadow-lg transition hover:bg-amber-400 focus:outline-none focus:ring-2 focus:ring-amber-300">${t('saude.cvvBtnLigar')}</a>
        <button type="button" data-cvv-abrir aria-expanded="false" aria-controls="bitCvvArea"
            class="hidden rounded-full border border-slate-700 bg-slate-800 px-4 py-2 text-xs font-semibold text-slate-200 transition hover:text-white hover:border-slate-500 focus:outline-none focus:ring-2 focus:ring-amber-500">${t('bit.cvvAbrir')}</button>
    `;
    const recolher = fixo.querySelector('[data-cvv-recolher]');
    const abrir = fixo.querySelector('[data-cvv-abrir]');
    recolher.addEventListener('click', () => { definirCartaoCvvRecolhido(fixo, true); abrir.focus(); });
    abrir.addEventListener('click', () => { definirCartaoCvvRecolhido(fixo, false); recolher.focus(); });
    document.body.appendChild(fixo);
    return fixo;
}

// Recolher só troca classes: a área do painel fica escondida (hidden) mas NÃO sai do DOM, porque
// a resposta do servidor ainda pode chegar e precisa atualizar o painel que está lá dentro.
function definirCartaoCvvRecolhido(fixo, recolhido) {
    fixo.className = recolhido ? CVV_CARTAO_RECOLHIDO : CVV_CARTAO_ABERTO;
    fixo.querySelector('[data-cvv-barra]').classList.toggle('hidden', recolhido);
    fixo.querySelector('#bitCvvArea').classList.toggle('hidden', recolhido);
    fixo.querySelector('[data-cvv-ligar]').classList.toggle('hidden', !recolhido);
    fixo.querySelector('[data-cvv-abrir]').classList.toggle('hidden', !recolhido);
}

function preservarPainelCvv(row) {
    const fixo = document.getElementById('bitCvvFixo') || criarCartaoCvv();
    fixo.querySelector('#bitCvvArea').appendChild(row);
}

function dispensar() {
    // Só esconde na visualização atual — sem localStorage, reload sempre traz o Bit de volta.
    // O painel do CVV, se existir, NUNCA é descartado: vai para o cartão fixo, que nasce aberto.
    document.removeEventListener('keydown', onEscKey);
    if (painelCvvRow) preservarPainelCvv(painelCvvRow);
    document.getElementById('bitOverlay')?.remove();
}

// ════════════════════════════════════════════════════════════════════════
//  BOLHAS — helpers de renderização do fluxo conversacional
// ════════════════════════════════════════════════════════════════════════

function addBitBubble(bubblesEl, innerHtml) {
    const bubble = document.createElement('div');
    bubble.className = 'flex items-start gap-3';
    bubble.innerHTML = `
        <span class="shrink-0 text-2xl leading-none" aria-hidden="true">🤖</span>
        <div class="flex-1 rounded-2xl rounded-tl-sm bg-slate-800/80 border border-slate-700/60 p-4 text-sm text-slate-100">${innerHtml}</div>
    `;
    bubblesEl.appendChild(bubble);
    return bubble;
}

function addUserBubble(bubblesEl, texto) {
    const bubble = document.createElement('div');
    bubble.className = 'flex justify-end';
    bubble.innerHTML = `<div class="rounded-2xl rounded-tr-sm bg-cyan-500 text-slate-950 font-semibold px-4 py-2 text-sm">${escapeHtml(texto)}</div>`;
    bubblesEl.appendChild(bubble);
    return bubble;
}

function linkAgoraNao(bubblesEl) {
    const wrap = document.createElement('div');
    wrap.className = 'text-center pt-1';
    wrap.innerHTML = `<button type="button" class="text-xs text-slate-500 hover:text-slate-300 underline transition">${t('bit.agoraNao')}</button>`;
    wrap.querySelector('button').addEventListener('click', dispensar);
    bubblesEl.appendChild(wrap);
}

// ════════════════════════════════════════════════════════════════════════
//  CONSENTIMENTO (nota 0-1) — mesmos textos/comportamento do lote 2
// ════════════════════════════════════════════════════════════════════════

function pedirConsentimento() {
    return new Promise((resolve) => {
        const modal = document.createElement('div');
        modal.id = 'bitConsentimento';
        modal.className = 'fixed inset-0 z-[95] flex items-center justify-center bg-slate-950/80 backdrop-blur-sm px-4';
        modal.setAttribute('role', 'dialog');
        modal.setAttribute('aria-modal', 'true');
        modal.innerHTML = `
            <div class="w-full max-w-md rounded-3xl border border-slate-800 bg-slate-900 p-8 shadow-2xl space-y-5 text-center animate-fade-in">
                <div class="text-4xl" aria-hidden="true">🫶</div>
                <h3 class="text-lg font-bold text-white">${t('saude.modalTitulo')}</h3>
                <p class="text-sm text-slate-300 leading-relaxed">${t('saude.modalMsg')}</p>
                ${cvvBlocoHtml()}
                <div class="flex flex-col gap-3">
                    <button type="button" data-acao="sim" class="w-full rounded-2xl bg-cyan-500 px-6 py-3 text-sm font-bold text-slate-950 shadow-xl transition hover:bg-cyan-400 transform active:scale-[0.98]">${t('saude.modalSim')}</button>
                    <button type="button" data-acao="nao" class="w-full rounded-2xl border border-slate-700 bg-slate-950 px-6 py-3 text-sm font-semibold text-slate-300 transition hover:border-slate-600">${t('saude.modalNao')}</button>
                </div>
            </div>
        `;
        modal.addEventListener('click', (e) => {
            if (e.target === modal) { modal.remove(); resolve(false); }
        });
        modal.addEventListener('keydown', (e) => {
            if (e.key === 'Escape') {
                // Sem isto o Esc subiria ao document e, com o modal já removido, fecharia o Bit também.
                e.stopPropagation();
                modal.remove();
                resolve(false);
            }
        });
        modal.querySelector('[data-acao="sim"]').addEventListener('click', () => { modal.remove(); resolve(true); });
        modal.querySelector('[data-acao="nao"]').addEventListener('click', () => { modal.remove(); resolve(false); });
        document.body.appendChild(modal);
        // O foco entra no modal (e não fica no "Enviar" por baixo): os links do CVV ficam
        // alcançáveis pelo teclado e o Esc passa a cancelar.
        modal.querySelector('[data-acao="sim"]').focus();
    });
}

// ════════════════════════════════════════════════════════════════════════
//  SUGESTÕES — cards curtos (título + motivo) após a resposta do Bit
// ════════════════════════════════════════════════════════════════════════

async function renderSugestoes(bubblesEl) {
    try {
        const data = await buscarSugestoes(usuario.id, getIdioma(), { redirecionarEm401: false });
        const sugestoes = data?.sugestoes || [];
        if (sugestoes.length === 0) return;
        const grid = document.createElement('div');
        grid.className = 'grid gap-2 sm:grid-cols-3 pl-9';
        grid.innerHTML = sugestoes.map(s => `
            <div class="rounded-xl bg-slate-900/60 border border-slate-800 p-3">
                <p class="text-xs font-bold text-white">${escapeHtml(s.titulo)}</p>
                <p class="text-[11px] text-slate-400 mt-1 leading-relaxed">${escapeHtml(s.motivo)}</p>
            </div>
        `).join('');
        bubblesEl.appendChild(grid);
    } catch { /* sugestões são um extra — falha silenciosa */ }
}

// ════════════════════════════════════════════════════════════════════════
//  FLUXO PRINCIPAL — check-in único (emoji opcional + texto livre)
// ════════════════════════════════════════════════════════════════════════

function perguntarCheckinDiario(bubblesEl) {
    addBitBubble(bubblesEl, saudacaoPorHora());

    const moodWrap = document.createElement('div');
    moodWrap.className = 'flex flex-wrap gap-2 pl-9';
    moodWrap.innerHTML = NIVEL_CHECKIN.map(n => `
        <button type="button" data-nota="${n.nota}" class="bit-mood-btn text-2xl px-3 py-2 bg-slate-950 hover:bg-slate-800 border border-slate-800 rounded-xl transition focus:ring-2 focus:ring-cyan-500 outline-none" aria-label="${t(n.labelKey)}">
            ${n.emoji}
        </button>
    `).join('');
    bubblesEl.appendChild(moodWrap);

    addBitBubble(bubblesEl, t('bit.perguntaContar'));

    const formWrap = document.createElement('div');
    formWrap.className = 'pl-9 space-y-2';
    formWrap.innerHTML = `
        <textarea rows="3" placeholder="${t('saude.contextoPlaceholder')}"
            class="w-full rounded-2xl border border-slate-800 bg-slate-950 px-4 py-3 text-sm text-slate-100 focus:outline-none focus:border-cyan-500 focus:ring-1 focus:ring-cyan-500 transition"></textarea>
        <p data-hint class="hidden text-[11px] text-amber-400/90">${t('bit.pedirAlgumaCoisa')}</p>
        <p data-erro role="alert" class="hidden text-[11px] text-rose-400">${t('saude.erroSalvar')}</p>
        <button type="button" disabled class="rounded-2xl bg-cyan-500 px-6 py-2.5 text-sm font-bold text-slate-950 shadow-xl transition hover:bg-cyan-400 transform active:scale-[0.98] disabled:opacity-40 disabled:cursor-not-allowed">${t('saude.enviarRegistro')}</button>
    `;
    bubblesEl.appendChild(formWrap);

    const textarea = formWrap.querySelector('textarea');
    const hint = formWrap.querySelector('[data-hint]');
    const erroEl = formWrap.querySelector('[data-erro]');
    const botaoEnviar = formWrap.querySelector('button');

    let notaEscolhida = null;

    function atualizarEstadoEnvio() {
        const podeEnviar = Boolean(notaEscolhida) || textarea.value.trim().length > 0;
        botaoEnviar.disabled = !podeEnviar;
        hint.classList.toggle('hidden', podeEnviar);
    }

    moodWrap.querySelectorAll('.bit-mood-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            moodWrap.querySelectorAll('.bit-mood-btn').forEach(b => {
                b.classList.remove('border-cyan-500', 'bg-slate-800');
                b.classList.add('border-slate-800', 'bg-slate-950');
            });
            btn.classList.remove('border-slate-800', 'bg-slate-950');
            btn.classList.add('border-cyan-500', 'bg-slate-800');
            notaEscolhida = Number(btn.dataset.nota);
            atualizarEstadoEnvio();
        });
    });

    textarea.addEventListener('input', atualizarEstadoEnvio);

    botaoEnviar.addEventListener('click', async () => {
        const contexto = textarea.value || '';
        if (!notaEscolhida && !contexto.trim()) return; // guarda defensiva

        // Nota 1 (derivação REFORÇADA): consentimento ANTES de gravar. O CVV já aparece
        // dentro do modal, então o 188 está na tela mesmo se a pessoa desistir.
        if (notaEscolhida === 1) {
            // Enquanto o modal está aberto, um segundo clique ou Enter não pode empilhar outro modal
            // nem disparar dois envios.
            botaoEnviar.disabled = true;
            const consentiu = await pedirConsentimento();
            if (!consentiu) { // "cliquei sem querer": não grava, não avança
                botaoEnviar.disabled = false;
                return;
            }
        }

        const dados = { usuarioId: usuario.id, nota: notaEscolhida, contexto, idioma: getIdioma() };
        const nivelLocal = nivelDerivacaoLocal(notaEscolhida);

        if (nivelLocal) {
            await enviarComCvv(bubblesEl, { moodWrap, formWrap }, dados, nivelLocal);
            return;
        }

        botaoEnviar.disabled = true;
        erroEl.classList.add('hidden');
        botaoEnviar.innerHTML = `<span class="loader"></span> ${t('saude.salvando')}`;

        try {
            const data = await saudeCheckin(dados);
            moodWrap.remove();
            formWrap.remove();
            if (notaEscolhida) addUserBubble(bubblesEl, nivelPorNota(notaEscolhida)?.emoji || '');
            if (contexto) addUserBubble(bubblesEl, contexto);
            renderRespostaBit(bubblesEl, data);
        } catch (err) {
            if (err?.status === 401) { sessaoExpirou(); return; }
            // Falha ou timeout sem CVV em jogo: aviso no próprio formulário, sem alert().
            botaoEnviar.disabled = false;
            botaoEnviar.textContent = t('saude.enviarRegistro');
            erroEl.classList.remove('hidden');
        }
    });
}

// Linha do Bit (avatar + painel) com os quatro elementos que o saude-shared.js preenche.
// Não usa addBitBubble: o painel substitui o className do container inteiro, então precisa
// de um wrapper flex-1 próprio em vez de ser o item flex da linha avatar+conteúdo.
function criarPainelResposta(bubblesEl) {
    const row = document.createElement('div');
    row.className = 'flex items-start gap-3';
    row.innerHTML = `<span class="shrink-0 text-2xl leading-none" aria-hidden="true">🤖</span>`;
    const wrapper = document.createElement('div');
    wrapper.className = 'flex-1';
    row.appendChild(wrapper);
    bubblesEl.appendChild(row);

    const container = document.createElement('div');
    const title = document.createElement('h4');
    const msg = document.createElement('p');
    const action = document.createElement('div');
    container.append(title, msg, action);
    wrapper.appendChild(container);
    return { row, els: { container, title, msg, action } };
}

// O modal do Bit é centralizado na vertical: sem isto, tudo que cresce depois (a mensagem
// de acolhimento, as sugestões) reposicionaria o painel e o botão do 188 se mexeria sob o
// dedo de quem está em crise. Trava o topo do modal onde ele está agora.
function fixarPosicaoDoModal() {
    const overlay = document.getElementById('bitOverlay');
    const box = document.getElementById('bitModalBox');
    if (!overlay || !box) return;
    const topo = Math.max(16, Math.round(box.getBoundingClientRect().top));
    overlay.classList.remove('items-center');
    overlay.classList.add('items-start');
    overlay.style.paddingTop = '0';
    overlay.style.paddingBottom = '0';
    box.style.marginTop = `${topo}px`;
    box.style.maxHeight = `calc(100vh - ${topo}px - 1rem)`;
}

// Check-in com nota 1 ou 3: o painel do CVV entra na tela ANTES da requisição e a
// resposta (ou a falta dela) só mexe na mensagem de acolhimento e na tendência.
async function enviarComCvv(bubblesEl, { moodWrap, formWrap }, dados, nivelLocal) {
    fixarPosicaoDoModal();
    moodWrap.remove();
    formWrap.remove();
    if (dados.nota) addUserBubble(bubblesEl, nivelPorNota(dados.nota)?.emoji || '');
    if (dados.contexto) addUserBubble(bubblesEl, dados.contexto);

    const { row, els } = criarPainelResposta(bubblesEl);
    painelCvvRow = row;
    renderCvvImediato(els, nivelLocal);
    // Se o Bit foi fechado durante o consentimento, o painel vai direto para o cartão fixo.
    if (!bubblesEl.isConnected) preservarPainelCvv(row);

    try {
        const data = await saudeCheckin(dados);
        atualizarAcolhimento(els, data);
        // Leitura emocional e sugestões entram ABAIXO do painel: nada acima dos botões do CVV se move.
        if (data.leituraEmocional) {
            addBitBubble(bubblesEl, `<span class="italic text-slate-300">${escapeHtml(data.leituraEmocional)}</span>`);
        }
        renderSugestoes(bubblesEl);
    } catch (err) {
        marcarAcolhimentoIndisponivel(els, err);
    }
    linkAgoraNao(bubblesEl);
}

function renderRespostaBit(bubblesEl, data) {
    if (data.leituraEmocional) {
        addBitBubble(bubblesEl, `<span class="italic text-slate-300">${escapeHtml(data.leituraEmocional)}</span>`);
    }

    const { row, els } = criarPainelResposta(bubblesEl);
    renderNivelDerivacaoPanel(els, data);

    // A tendência da semana (ou um nível vindo do servidor) também leva o CVV: se o Bit já foi
    // fechado quando a resposta chega, o painel vai direto para o cartão fixo, e se for fechado
    // depois, dispensar() o preserva.
    if (data.nivelDerivacao || data.tendenciaSemana) {
        painelCvvRow = row;
        if (!bubblesEl.isConnected) preservarPainelCvv(row);
    }

    renderSugestoes(bubblesEl);
    linkAgoraNao(bubblesEl);
}

// ════════════════════════════════════════════════════════════════════════
//  BOOT
// ════════════════════════════════════════════════════════════════════════

async function iniciarBit() {
    if (!usuario?.id) return;

    const bubblesEl = criarModal();
    if (!bubblesEl) return;

    perguntarCheckinDiario(bubblesEl);
}

document.addEventListener('DOMContentLoaded', iniciarBit);
