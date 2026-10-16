// Módulo compartilhado entre saude-mental.js e bit.js — extraído para não
// duplicar os painéis graduados REFORCADO/PREVENTIVO/tendência nem os textos
// do DICT (mesma regra de derivação do backend, decidida só pela nota do
// check-in — CVV v2).
import { t } from './i18n.js';
import { escapeHtml } from './escape.js';

// Escala única de check-in (CVV v2): nota fixa por nível, na ordem exibida
// nos botões (do mais positivo ao mais negativo). Espelha exatamente
// NivelCheckin.java — rótulo/emoji só pra UI, a nota é o que vai no payload.
export const NIVEL_CHECKIN = [
    { nota: 9, emoji: '😄', labelKey: 'saude.nivelMuitoFeliz' },
    { nota: 7, emoji: '🙂', labelKey: 'saude.nivelFeliz' },
    { nota: 5, emoji: '😌', labelKey: 'saude.nivelTranquilo' },
    { nota: 3, emoji: '😔', labelKey: 'saude.nivelTriste' },
    { nota: 1, emoji: '😢', labelKey: 'saude.nivelMuitoTriste' },
];

export function nivelPorNota(nota) {
    return NIVEL_CHECKIN.find(n => n.nota === nota) || null;
}

// Limites da derivação ao CVV. ESPELHAM o backend: NOTA_MAX_REFORCADO = 1 e
// NOTA_MAX_PREVENTIVO = 3 em SaudeMentalService.java. Se mudarem lá, mudam aqui.
const NOTA_MAX_REFORCADO = 1;
const NOTA_MAX_PREVENTIVO = 3;

// Nível de derivação calculado localmente a partir da nota escolhida, com a
// mesma regra do backend (SaudeMentalService.nivelDerivacao): nota <= 1 é
// REFORCADO, nota <= 3 é PREVENTIVO, o resto (ou check-in só-texto, sem nota)
// não deriva. Serve para mostrar o CVV na hora, sem esperar o servidor nem a IA.
export function nivelDerivacaoLocal(nota) {
    if (nota === null || nota === undefined) return null;
    if (nota <= NOTA_MAX_REFORCADO) return 'REFORCADO';
    if (nota <= NOTA_MAX_PREVENTIVO) return 'PREVENTIVO';
    return null;
}

// Roda para a próxima variação de acolhimento CVV (persistida em localStorage),
// só deve ser chamada por gatilho pontual (REFORCADO/PREVENTIVO) — a
// tendência semanal usa mensagem fixa, sem rotação (ver comentário abaixo).
function proximaVariacaoCvv() {
    const variacoes = t('saude.cvvVariacoes');
    const idx = (parseInt(localStorage.getItem('cvvMsgIdx') || '-1', 10) + 1) % variacoes.length;
    localStorage.setItem('cvvMsgIdx', String(idx));
    return variacoes[idx];
}

// Botões de ação do CVV (ligar/chat), com cor ajustada à identidade de cada painel.
function botoesCvv(cor) {
    return `<div class="flex flex-col sm:flex-row gap-2 pt-1">
        <a href="tel:188" class="flex-1 text-center rounded-xl bg-${cor}-500 px-5 py-2.5 text-sm font-bold text-slate-950 shadow-lg transition hover:bg-${cor}-400 transform active:scale-[0.98]">${t('saude.cvvBtnLigar')}</a>
        <a href="https://www.cvv.org.br" target="_blank" rel="noopener noreferrer" class="flex-1 text-center rounded-xl border border-${cor}-700/50 bg-slate-950 px-5 py-2.5 text-sm font-semibold text-${cor}-200 transition hover:border-${cor}-600">${t('saude.cvvBtnChat')}</a>
    </div>`;
}

// Bloco do CVV para dentro do modal de confirmação da nota 1 (bit.js e
// saude-mental.html): o 188 fica na tela mesmo antes de a pessoa confirmar.
export function cvvBlocoHtml() {
    return `<div class="p-4 bg-slate-950 border border-amber-700/40 rounded-2xl space-y-2 text-left">
        <p class="text-sm font-bold text-white">${t('saude.reforcadoCvv')}</p>
        ${botoesCvv('amber')}
    </div>`;
}

// ── Painel do CVV (REFORCADO/PREVENTIVO) ────────────────────────────────

// Campos preenchidos DEPOIS, quando a resposta chega: ação sugerida (IA) e
// tendência da semana. Ficam no fim da caixa do CVV, escondidos, para a
// resposta não empurrar os botões de lugar.
const RODAPE_RESPOSTA = `
    <p data-acao-sugerida class="hidden text-xs text-slate-400 pt-1"></p>
    <div data-tendencia class="hidden pt-2 mt-1 border-t border-slate-800 text-sm text-slate-200"></div>`;

// Ordem e classes do painel com CVV: título, caixa do CVV e SÓ DEPOIS a mensagem
// de acolhimento. Assim o texto que chega do servidor cresce abaixo dos botões.
function ordenarComCvvPrimeiro({ container, title, msg, action }) {
    action.className = 'mt-1';
    msg.className = 'text-sm text-slate-200 leading-relaxed whitespace-pre-line mt-4';
    container.append(title, action, msg);
}

// Ordem e classes originais (mensagem, depois a ação), para os painéis sem CVV.
function ordenarMensagemPrimeiro({ container, title, msg, action }) {
    msg.className = 'text-sm text-slate-200 leading-relaxed whitespace-pre-line';
    action.className = 'mt-4 pt-4 border-t border-slate-800/60';
    container.append(title, msg, action);
}

function montarPainelCvv(els, nivel) {
    const { container, title, action } = els;
    container.classList.remove('hidden');

    if (nivel === 'REFORCADO') {
        // Nota 1: cuidado, CVV em destaque, tom acolhedor (sem alarme).
        container.className = 'p-6 rounded-3xl border border-amber-800/60 bg-amber-950/20 mt-6 animate-fade-in';
        title.className = 'text-sm font-bold uppercase tracking-wider mb-2 text-amber-300 flex items-center gap-2';
        title.innerHTML = `🫂 ${t('saude.reforcadoTitulo')}`;
        action.innerHTML = `<div class="p-4 bg-slate-950 border border-amber-700/40 rounded-2xl space-y-2">
            <p class="text-sm text-amber-100">${proximaVariacaoCvv()}</p>
            <p class="text-sm font-bold text-white">${t('saude.reforcadoCvv')}</p>
            ${botoesCvv('amber')}${RODAPE_RESPOSTA}</div>`;
    } else {
        // Nota 3: escuta suave, apresentada como recurso — não como alerta.
        container.className = 'p-6 rounded-3xl border border-cyan-800/50 bg-cyan-950/20 mt-6 animate-fade-in';
        title.className = 'text-sm font-bold uppercase tracking-wider mb-2 text-cyan-300 flex items-center gap-2';
        title.innerHTML = `💬 ${t('saude.preventivoTitulo')}`;
        action.innerHTML = `<div class="p-4 bg-slate-950 border border-cyan-800/40 rounded-2xl space-y-2">
            <p class="text-sm text-cyan-100">${proximaVariacaoCvv()}</p>
            <p class="text-sm text-slate-200">${t('saude.preventivoCvv')}</p>
            ${botoesCvv('cyan')}${RODAPE_RESPOSTA}</div>`;
    }
    ordenarComCvvPrimeiro(els);
}

// Mostra o CVV AGORA, antes de a requisição sair e independente da resposta.
// O nível vem de nivelDerivacaoLocal(nota). A mensagem de acolhimento fica
// provisória até atualizarAcolhimento() ou marcarAcolhimentoIndisponivel().
export function renderCvvImediato(els, nivel) {
    montarPainelCvv(els, nivel);
    els.msg.textContent = t('saude.acolhimentoCarregando');
    // Instantâneo, sem animação: nada se move sob o dedo de quem acabou de tocar em enviar.
    els.container.scrollIntoView?.({ block: 'nearest' });
}

// Resposta chegou: atualiza SÓ a mensagem de acolhimento, a ação sugerida e a
// tendência da semana. O nível do CVV já está na tela e não muda aqui.
export function atualizarAcolhimento({ container, msg }, data) {
    msg.textContent = data.mensagem || '';
    const acao = container.querySelector('[data-acao-sugerida]');
    if (acao && data.acaoSugerida) {
        acao.textContent = data.acaoSugerida;
        acao.classList.remove('hidden');
    }
    const tendencia = container.querySelector('[data-tendencia]');
    if (tendencia && data.tendenciaSemana) {
        tendencia.textContent = `📈 ${t('saude.tendenciaMsg')}`;
        tendencia.classList.remove('hidden');
    }
}

// A requisição falhou ou estourou o tempo: o painel do CVV continua como está e
// só a mensagem provisória troca por um aviso calmo — nada de alert().
export function marcarAcolhimentoIndisponivel({ msg }, erro) {
    msg.textContent = erro?.status === 401 ? t('saude.sessaoExpiradaCvv') : t('saude.acolhimentoIndisponivel');
}

// Falha de envio SEM derivação ao CVV: aviso dentro do painel, no lugar do alert().
export function mostrarErroSalvar(els) {
    const { container, title, msg, action } = els;
    container.classList.remove('hidden');
    container.className = 'p-6 rounded-3xl border border-slate-800 bg-slate-900/60 mt-6 animate-fade-in';
    title.className = 'hidden';
    title.textContent = '';
    action.innerHTML = '';
    ordenarMensagemPrimeiro(els);
    msg.textContent = t('saude.erroSalvar');
}

// Renderiza o acolhimento a partir da resposta do servidor, quando NÃO há painel
// local do CVV na tela (nota 5/7/9, check-in só-texto) ou como rede de segurança.
// Prioridade visual: REFORCADO > PREVENTIVO > tendência semanal (sem gatilho hoje)
// > normal. nivelDerivacao e tendenciaSemana são decididos SÓ pela nota no
// backend (SaudeMentalService, inviolável). Linguagem de cuidado: nada de
// "SUPORTE CRÍTICO"/⚠️.
export function renderNivelDerivacaoPanel(els, data) {
    const { container, title, msg, action } = els;
    container.classList.remove('hidden');

    if (data.nivelDerivacao === 'REFORCADO' || data.nivelDerivacao === 'PREVENTIVO') {
        montarPainelCvv(els, data.nivelDerivacao);
        atualizarAcolhimento(els, data);
        return;
    }

    ordenarMensagemPrimeiro(els);
    msg.textContent = data.mensagem || '';
    title.textContent = '';

    if (data.tendenciaSemana) {
        // Sem gatilho hoje, mas os últimos dias vêm pesados (CVV v2): mesma
        // estrutura dos gatilhos pontuais, cor distinta (índigo). Mensagem é
        // FIXA — explica o alerta agregado da semana, não pode virar frase
        // genérica rotativa — mas ganha os mesmos botões de ação.
        container.className = 'p-6 rounded-3xl border border-indigo-800/50 bg-indigo-950/20 mt-6 animate-fade-in';
        title.className = 'text-sm font-bold uppercase tracking-wider mb-2 text-indigo-300 flex items-center gap-2';
        title.innerHTML = `📈 ${t('saude.tendenciaTitulo')}`;
        action.innerHTML = `<div class="p-4 bg-slate-950 border border-indigo-800/40 rounded-2xl space-y-2">
            <p class="text-sm text-slate-200">${t('saude.tendenciaMsg')}</p>
            ${botoesCvv('indigo')}
            <p class="text-xs text-slate-400 pt-1">${escapeHtml(data.acaoSugerida)}</p></div>`;
    } else {
        // Nota 5/7/9 ou ausente, sem tendência: acolhimento normal.
        container.className = 'p-6 rounded-3xl border border-slate-800 bg-slate-900/60 mt-6 animate-fade-in';
        title.className = 'text-sm font-bold uppercase tracking-wider mb-2 text-cyan-400';
        title.innerText = t('saude.normalTitulo');
        action.innerHTML = `<p class="text-xs text-slate-400 font-semibold mb-1">${t('saude.acaoLabel')}</p>
            <p class="text-sm text-slate-200">${escapeHtml(data.acaoSugerida)}</p>`;
    }
}
