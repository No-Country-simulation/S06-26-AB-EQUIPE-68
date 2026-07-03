import { saudeCheckin, historicoSaude } from './api.js';

const SESSION_KEY = 'bitapp_usuario';

function getUsuarioLogado() {
    const raw = localStorage.getItem(SESSION_KEY);
    return raw ? JSON.parse(raw) : null;
}

const usuario = getUsuarioLogado();
if (!usuario) {
    window.location.href = 'index.html?msg=auth_required';
}

const REGION_LABELS = {
    CBD_BEIRAMAR: 'Centro/Beiramar',
    TRINDADE: 'Trindade',
    UFSC: 'UFSC',
    CAMPECHE: 'Campeche',
    INGLESES: 'Ingleses',
    LAGOA_CONCEICAO: 'Lagoa da Conceição',
    ESTREITO_CAPOEIRAS: 'Estreito/Capoeiras',
    SAO_JOSE_CENTRO: 'São José — Centro',
};

const DICAS_LAZER = {
    TRINDADE: [
        { icon: '📚', titulo: 'Biblioteca Pública Alcides', desc: 'Acervo de 100 mil títulos, wifi gratuito e espaço de estudo. Leitura reduz o estresse em até 68%.' },
        { icon: '🎭', titulo: 'Teatro Ademir Rosa', desc: 'Programação cultural diversificada. Assistir teatro estimula empatia e reduz ansiedade.' },
        { icon: '🌳', titulo: 'Passeio pelaUFSC', desc: 'Caminhe pelos corredores verdes do campus. Caminhada leve regula o cortisol.' },
        { icon: '☕', titulo: 'Café na Rua Meinhardt', desc: 'Momentos de pausa com café gourmet. Uma pausa consciente recarrega o foco.' },
        { icon: '🎨', titulo: 'Galeria de Arte da UFSC', desc: 'Exposições gratuitas de arte contemporânea. Arte eleva o humor e estimula a criatividade.' },
        { icon: '🧘', titulo: 'Yoga no Parque da UFSC', desc: 'Aulas gratuitas ao ar livre aos sábados. Yoga reduz ansiedade e melhora o sono.' },
    ],
    CBD_BEIRAMAR: [
        { icon: '🌊', titulo: 'Passeio Beira-Mar Norte', desc: 'Caminhada ou corrida na orla. Exercício aeróbico libera endorfina em 20 minutos.' },
        { icon: '🏛️', titulo: 'Mercado Público', desc: 'Visite barracas centenárias. Ambientes sociais combatem o isolamento.' },
        { icon: '🎨', titulo: 'Centro Cultural García', desc: 'Oficinas e exposições gratuitas. Criatividade é um antídoto natural contra o estresse.' },
        { icon: '📖', titulo: 'Livraria da Travessa', desc: 'Leitura de 15 min reduz batimentos cardíacos. Uma pausa literária transforma o dia.' },
        { icon: '🌿', titulo: 'Praça XV de Novembro', desc: 'Sente-se sob as árvores e respire. Contato com natureza urbana acalma a mente.' },
        { icon: '🎵', titulo: 'Show na Casa de Cultura', desc: 'Eventos musicais gratuitos. Música libera dopamina e melhora o humor imediatamente.' },
    ],
    UFSC: [
        { icon: '🏫', titulo: 'Trilhas do Campus', desc: 'Caminhe pelos 260 hectares de mata atlântica preservada. Natureza reduz cortisol em 12%.' },
        { icon: '🔬', titulo: 'Museu da UFSC', desc: 'Acervo de artes visuais e fotografias. Exposição cultural estimula novas perspectivas.' },
        { icon: '📚', titulo: 'Biblioteca Central', desc: 'Espaço silencioso para leitura e estudo. Ambiente focado reduz ruído mental.' },
        { icon: '🏊', titulo: 'Piscina do CAG', desc: 'Natação relaxa os músculos e liberta tensões. Atividade aquática é terapêutica.' },
        { icon: '🌿', titulo: 'Jardim Botânico (perto)', desc: 'Contato com plantas e flores. Botanicamente comprovado: natureza acalma.' },
        { icon: '☕', titulo: 'Café no Naufrago', desc: 'Pausa para café entre os estudiosos.Momentos de descontração fortalecem laços.' },
    ],
    CAMPECHE: [
        { icon: '🏖️', titulo: 'Praia do Campeche', desc: 'Águas claras e arrecife de corais. O som da maré reduz ansiedade em 30%.' },
        { icon: '🏝️', titulo: 'Barco para Ilha do Campeche', desc: 'Excursão de fim de semana. Mudança de cenário renova as energias.' },
        { icon: '🌳', titulo: 'Parque Municipal do Campeche', desc: 'Trilhas ecológicas e áreas de lazer. Exercício ao ar livre eleva o humor.' },
        { icon: '🎣', titulo: 'Pescaria na Lagoa', desc: 'Atividade contemplativa e pacífica. Pescaria é meditação em movimento.' },
        { icon: '🚴', titulo: 'Ciclismo na Litorânea', desc: 'Pedalar libera endorfina. 30 minutos de bicicleta melhoram o sono.' },
        { icon: '🌅', titulo: 'Pôr do sol no Morro', desc: 'Contemplação da natureza. Observar o horizonte amplia a percepção e acalma.' },
    ],
    INGLESES: [
        { icon: '🏖️', titulo: 'Praia dos Ingleses', desc: 'Caminhe pela orla ao nascer do sol. Luz natural regula o ritmo circadiano.' },
        { icon: '🌳', titulo: 'Lagoinha da Baía', desc: 'Trilha leve até a lagoinha. Contato com água doce é profundamente relaxante.' },
        { icon: '📚', titulo: 'Biblioteca Comunitária', desc: 'Espaço comunitário com wi-fi e atividades. Socialização reduz sentimentos de solidão.' },
        { icon: '🍉', titulo: 'Feira do Ingleses', desc: 'Produtos regionais e gastronomia local. Alimentação saudável impacta o humor.' },
        { icon: '🏄', titulo: 'Aula de Surf', desc: 'Surf é meditação na água. A combinação de exercício e natureza é poderosa.' },
        { icon: '🌿', titulo: 'Trilha do Morro da Lagoa', desc: 'Vista panorâmica e ar puro. Exercício em altitude libera endorfina tripla.' },
    ],
    LAGOA_CONCEICAO: [
        { icon: '🚣', titulo: 'Stand Up Paddle na Lagoa', desc: 'Equilíbrio na água é meditação ativa. SUP reduz ansiedade em 40%.' },
        { icon: '🌿', titulo: 'Trilha da Lagoa do Peri', desc: 'Mata atlântica preservada. Caminhada na floresta reduz pressão arterial.' },
        { icon: '☕', titulo: 'Cafés da Lagoa', desc: 'Bares à beira d\'água. Pausas sociais fortalecem o bem-estar emocional.' },
        { icon: '🎨', titulo: 'Artesanato Local', desc: 'Galerias e ateliês de artistas locais. Criatividade é terapia.' },
        { icon: '🌅', titulo: 'Mirante da Lagoa', desc: 'Contemplação ao pôr do sol. Meditação visual acalma a mente em minutos.' },
        { icon: '🎵', titulo: 'Música ao vivo nos bares', desc: 'Samba e MPB à beira da lagoa. Música ao vivo libera oxitocina.' },
    ],
    ESTREITO_CAPOEIRAS: [
        { icon: '🌳', titulo: 'Parque da Cidade Sarah', desc: '870 mil m² de natureza urbana. Caminhada no parque reduz estresse em 25%.' },
        { icon: '🧗', titulo: 'Rocha de Escalada', desc: 'Escalada desafia e distrai. Foco físico esquece preocupações mentais.' },
        { icon: '🏃', titulo: 'Linha do Corredor', desc: 'Pista de cooper ao redor do parque. 30 min de corrida = 4h de bom humor.' },
        { icon: '🧘', titulo: 'Yoga ao ar livre', desc: 'Aulas gratuitas no parque. Yoga restaura o equilíbrio corpo-mente.' },
        { icon: '🌿', titulo: 'Lago e Trilhas', desc: 'Caminhada ecológica com vistas do lago. Natureza é o melhor remédio natural.' },
        { icon: '📚', titulo: 'Leitura no Parque', desc: 'Leve um livro e sente-se na grama. Leitura ao ar livre amplifica o relaxamento.' },
    ],
    SAO_JOSE_CENTRO: [
        { icon: '🏛️', titulo: 'Feira de São José', desc: 'Produtos artesanais e gastronomia. Exposição cultural diversifica perspectivas.' },
        { icon: '🌳', titulo: 'Parque Municipal', desc: 'Verde no coração da cidade. Contato com natureza urbana acalma a ansiedade.' },
        { icon: '📚', titulo: 'Biblioteca Municipal', desc: 'Espaço de estudo e leitura. Ambiente silencioso restaura a concentração.' },
        { icon: '🎭', titulo: 'Teatro Municipal', desc: 'Peças e espetáculos culturais. Arte cênica desperta emoções positivas.' },
        { icon: '☕', titulo: 'Café do Centro', desc: 'Pausa para café em praça histórica. Momentos de pausa são essenciais.' },
        { icon: '🚴', titulo: 'Ciclovia do Centro', desc: 'Pedale pelo centro histórico. Ciclismo combate sintomas de depressão leve.' },
    ],
};

const DICAS_GERAIS = [
    { icon: '🌿', titulo: 'Caminhe 20 minutos', desc: 'Exercício leve ao ar livre regula o cortisol e melhora o humor em minutos.' },
    { icon: '📖', titulo: 'Leia por 15 minutos', desc: 'Leitura reduz o estresse em 68%. Escolha algo que te escape da rotina.' },
    { icon: '☕', titulo: 'Faça uma pausa consciente', desc: 'Café ou chá sem celular. Momentos de quietude restauram o foco.' },
    { icon: '🎵', titulo: 'Ouça música que te acalma', desc: 'Música a 60 BPM sincroniza o coração e reduz ansiedade.' },
    { icon: '🌳', titulo: 'Conecte-se com a natureza', desc: '15 min de contato com verde reduz batimentos cardíacos e acalma.' },
    { icon: '💬', titulo: 'Converse com alguém querido', desc: 'Socialização libera oxitocina. Uma ligação de 10 min muda o dia.' },
];

// ────────────────────────────────────────────────────────────────────────────
// Textos bilíngues (PT/ES) — bilíngue de nascença, preparando o i18n do lote 2.5.
// PT é o idioma exibido por padrão; a troca de idioma chega no próximo lote.
// ────────────────────────────────────────────────────────────────────────────
const IDIOMA = 'pt';
const TEXTOS = {
    pt: {
        notaLabel: 'De 0 a 10, que nota você dá para a sua semana?',
        notaAncoraMin: '0 — semana muito difícil',
        notaAncoraMax: '10 — semana excelente',
        contextoLabel: 'Contexto (opcional)',
        modalTitulo: 'Um instante',
        modalMsg: 'Percebemos que a semana foi muito difícil. Você gostaria de receber apoio agora?',
        modalSim: 'Sim, quero apoio',
        modalNao: 'Cliquei sem querer',
        reforcadoTitulo: 'Estamos com você',
        reforcadoCvv: 'O CVV está disponível agora, 24h — ligue 188.',
        preventivoTitulo: 'Um recurso pra você',
        preventivoCvv: 'Conversar ajuda — o 188 escuta 24h, sem julgamento, sobre qualquer assunto.',
        normalTitulo: '✨ Resposta do Agente BiT',
        acaoLabel: 'Ação:',
        alertaSemNota: 'Escolha uma nota de 0 a 10 para a sua semana.',
        alertaSemHumor: 'Selecione um emoji.',
        erroSalvar: 'Erro ao salvar check-in.',
    },
    es: {
        notaLabel: 'De 0 a 10, ¿qué nota le das a tu semana?',
        notaAncoraMin: '0 — semana muy difícil',
        notaAncoraMax: '10 — semana excelente',
        contextoLabel: 'Contexto (opcional)',
        modalTitulo: 'Un momento',
        modalMsg: 'Notamos que la semana fue muy difícil. ¿Te gustaría recibir apoyo ahora?',
        modalSim: 'Sí, quiero apoyo',
        modalNao: 'Fue sin querer',
        reforcadoTitulo: 'Estamos contigo',
        reforcadoCvv: 'El CVV está disponible ahora, 24h — llama al 188.',
        preventivoTitulo: 'Un recurso para ti',
        preventivoCvv: 'Hablar ayuda — el 188 escucha 24h, sin juzgar, sobre cualquier tema.',
        normalTitulo: '✨ Respuesta del Agente BiT',
        acaoLabel: 'Acción:',
        alertaSemNota: 'Elige una nota de 0 a 10 para tu semana.',
        alertaSemHumor: 'Selecciona un emoji.',
        erroSalvar: 'Error al guardar el registro.',
    },
};
const T = TEXTOS[IDIOMA] || TEXTOS.pt;

// Estado do check-in — humor e nota são INDEPENDENTES (contrato do Dia 1).
// O humor alimenta só o tom do acolhimento; a nota é a única entrada que deriva.
let selectedMoodState = null;
let selectedNota = null; // nenhuma nota pré-selecionada: o envio exige escolha.

// O emoji seleciona SÓ o humor/tom — nunca mais é convertido em nota.
function selectMood(btn, mood) {
    document.querySelectorAll('.mood-btn').forEach(el => {
        el.classList.remove('border-cyan-500', 'bg-slate-800');
        el.classList.add('border-slate-800', 'bg-slate-950');
        el.setAttribute('aria-pressed', 'false');
    });
    btn.classList.remove('border-slate-800', 'bg-slate-950');
    btn.classList.add('border-cyan-500', 'bg-slate-800');
    btn.setAttribute('aria-pressed', 'true');
    selectedMoodState = mood;
}
window.selectMood = selectMood;

// Onze botões 0-10 (nenhum pré-selecionado), navegáveis por Tab. Mesmo realce
// visual dos botões de emoji. A nota é a autoavaliação da semana, só ela deriva.
function renderNotaBotoes() {
    const grid = document.getElementById('notaBotoes');
    if (!grid) return;
    grid.innerHTML = '';
    for (let n = 0; n <= 10; n++) {
        const btn = document.createElement('button');
        btn.type = 'button';
        btn.dataset.nota = String(n);
        btn.textContent = String(n);
        btn.setAttribute('aria-pressed', 'false');
        btn.setAttribute('aria-label', `Nota ${n}`);
        btn.className = 'nota-btn w-10 h-10 text-sm font-bold bg-slate-950 hover:bg-slate-800 border border-slate-800 rounded-xl transition text-slate-200 focus:ring-2 focus:ring-cyan-500 outline-none';
        btn.addEventListener('click', () => selectNota(btn, n));
        grid.appendChild(btn);
    }
}

function selectNota(btn, nota) {
    document.querySelectorAll('.nota-btn').forEach(el => {
        el.classList.remove('border-cyan-500', 'bg-slate-800', 'text-white');
        el.classList.add('border-slate-800', 'bg-slate-950', 'text-slate-200');
        el.setAttribute('aria-pressed', 'false');
    });
    btn.classList.remove('border-slate-800', 'bg-slate-950', 'text-slate-200');
    btn.classList.add('border-cyan-500', 'bg-slate-800', 'text-white');
    btn.setAttribute('aria-pressed', 'true');
    selectedNota = nota;
}

// Aplica os textos do idioma atual nos rótulos/âncoras/modal estáticos.
function aplicarTextos() {
    const set = (id, txt) => { const el = document.getElementById(id); if (el) el.textContent = txt; };
    set('notaLabel', T.notaLabel);
    set('notaAncoraMin', T.notaAncoraMin);
    set('notaAncoraMax', T.notaAncoraMax);
    set('contextoLabel', T.contextoLabel);
    set('confirmTitle', T.modalTitulo);
    set('confirmMsg', T.modalMsg);
    set('confirmSim', T.modalSim);
    set('confirmNao', T.modalNao);
}

const formSaude = document.getElementById('formSaude');

// Envio real do check-in — só chega aqui após a confirmação (quando aplicável).
// É a ÚNICA porta de gravação: um clique acidental cancelado nem toca no banco.
async function enviarCheckin() {
    const submitBtn = formSaude?.querySelector('button[type="submit"]');
    if (submitBtn) { submitBtn.disabled = true; submitBtn.innerHTML = '<span class="loader"></span> Salvando...'; }
    try {
        const data = await saudeCheckin({
            usuarioId: usuario.id,
            humor: selectedMoodState,
            notaSemanal: selectedNota,
            contexto: document.getElementById('healthContext')?.value || '',
        });
        renderAiResponse(data);
    } catch { alert(T.erroSalvar); }
    finally { if (submitBtn) { submitBtn.disabled = false; submitBtn.textContent = 'Enviar Registro'; } }
}

// Passo de confirmação empática (Plano A): para nota <= 2, confirma antes de gravar.
// A confirmação é o único filtro do clique acidental — texto e IA nunca cancelam nada.
const confirmModal = document.getElementById('confirmModal');
function abrirConfirmacao() { confirmModal?.classList.remove('hidden'); }
function fecharConfirmacao() { confirmModal?.classList.add('hidden'); }

document.getElementById('confirmSim')?.addEventListener('click', () => {
    fecharConfirmacao();
    enviarCheckin();
});
document.getElementById('confirmNao')?.addEventListener('click', () => {
    // Cancela o envio: não submete, não grava. O usuário pode reescolher o humor.
    fecharConfirmacao();
});
confirmModal?.addEventListener('click', (e) => { if (e.target === confirmModal) fecharConfirmacao(); });

formSaude?.addEventListener('submit', async (event) => {
    event.preventDefault();
    if (!selectedMoodState) { alert(T.alertaSemHumor); return; }
    if (selectedNota === null) { alert(T.alertaSemNota); return; }
    // Nota 0-1 (derivação REFORÇADA): consentimento ANTES de gravar.
    // "Cliquei sem querer" não grava nada. Nota 2-10 grava direto.
    if (selectedNota <= 1) {
        abrirConfirmacao();
        return;
    }
    await enviarCheckin();
});

// Renderiza o acolhimento. O painel é escolhido por data.nivelDerivacao — decidido
// SÓ pela nota no backend. Linguagem de cuidado: nada de "SUPORTE CRÍTICO"/⚠️.
function renderAiResponse(data) {
    const container = document.getElementById('aiResponseContainer');
    const title = document.getElementById('aiResponseTitle');
    const msg = document.getElementById('aiResponseMsg');
    const action = document.getElementById('aiResponseAction');
    container.classList.remove('hidden');
    msg.innerText = data.mensagem || '';

    if (data.nivelDerivacao === 'REFORCADO') {
        // Nota 0-1: cuidado, CVV em destaque, tom acolhedor (sem alarme).
        container.className = 'p-6 rounded-3xl border border-amber-800/60 bg-amber-950/20 mt-6 animate-fade-in';
        title.className = 'text-sm font-bold uppercase tracking-wider mb-2 text-amber-300 flex items-center gap-2';
        title.innerHTML = `🫂 ${T.reforcadoTitulo}`;
        action.innerHTML = `<div class="p-4 bg-slate-950 border border-amber-700/40 rounded-2xl space-y-1">
            <p class="text-sm font-bold text-white">${T.reforcadoCvv}</p>
            <p class="text-xs text-slate-400">${data.acaoSugerida || ''}</p></div>`;
    } else if (data.nivelDerivacao === 'PREVENTIVO') {
        // Nota 2-3: escuta suave, apresentada como recurso — não como alerta.
        container.className = 'p-6 rounded-3xl border border-cyan-800/50 bg-cyan-950/20 mt-6 animate-fade-in';
        title.className = 'text-sm font-bold uppercase tracking-wider mb-2 text-cyan-300 flex items-center gap-2';
        title.innerHTML = `💬 ${T.preventivoTitulo}`;
        action.innerHTML = `<div class="p-4 bg-slate-950 border border-cyan-800/40 rounded-2xl space-y-1">
            <p class="text-sm text-slate-200">${T.preventivoCvv}</p>
            <p class="text-xs text-slate-400">${data.acaoSugerida || ''}</p></div>`;
    } else {
        // Nota 4-10: acolhimento normal.
        container.className = 'p-6 rounded-3xl border border-slate-800 bg-slate-900/60 mt-6 animate-fade-in';
        title.className = 'text-sm font-bold uppercase tracking-wider mb-2 text-cyan-400';
        title.innerText = T.normalTitulo;
        action.innerHTML = `<p class="text-xs text-slate-400 font-semibold mb-1">${T.acaoLabel}</p>
            <p class="text-sm text-slate-200">${data.acaoSugerida || ''}</p>`;
    }
}

function carregarDicasLazer() {
    const regiao = usuario?.cidade;
    const subtitulo = document.getElementById('dicasSubtitulo');
    const grid = document.getElementById('dicasGrid');

    if (!regiao || !grid) return;

    const nomeRegiao = REGION_LABELS[regiao] || regiao;
    const dicas = DICAS_LAZER[regiao] || DICAS_GERAIS;

    if (subtitulo) subtitulo.textContent = `Baseado na sua região — ${nomeRegiao}`;

    grid.innerHTML = dicas.map(dica => `
        <div class="rounded-2xl bg-slate-900/60 border border-slate-800 p-5 hover:border-emerald-500/40 transition-all duration-200 flex flex-col gap-3">
            <div class="flex items-center gap-3">
                <span class="text-2xl">${dica.icon}</span>
                <h3 class="text-sm font-bold text-white">${dica.titulo}</h3>
            </div>
            <p class="text-xs text-slate-400 leading-relaxed">${dica.desc}</p>
        </div>
    `).join('');
}

document.addEventListener('DOMContentLoaded', carregarDicasLazer);
document.addEventListener('DOMContentLoaded', () => { renderNotaBotoes(); aplicarTextos(); });

const MOOD_EMOJIS = {
    feliz: '😊',
    cansado: '😴',
    triste: '😢',
    ansioso: '😰',
    sobrecarregado: '🤯',
};

async function carregarHistorico() {
    const grid = document.getElementById('historicoGrid');
    const empty = document.getElementById('historicoEmpty');
    if (!grid || !empty || !usuario?.id) return;

    try {
        const registros = await historicoSaude(usuario.id);
        if (!registros || registros.length === 0) {
            empty.classList.remove('hidden');
            return;
        }
        empty.classList.add('hidden');
        grid.innerHTML = registros.slice(0, 10).map(r => `
            <div class="rounded-2xl bg-slate-900/60 border border-slate-800 p-4 flex items-center gap-4">
                <span class="text-2xl">${MOOD_EMOJIS[r.humor] || '❓'}</span>
                <div class="flex-1 min-w-0">
                    <p class="text-sm font-semibold text-white capitalize">${r.humor || '—'}</p>
                    <p class="text-xs text-slate-400 truncate">${r.contexto || 'Sem contexto'}</p>
                </div>
                <span class="text-[10px] text-slate-500 shrink-0">${r.createdAt ? new Date(r.createdAt).toLocaleDateString('pt-BR') : '—'}</span>
            </div>
        `).join('');
    } catch {
        empty.classList.remove('hidden');
        empty.textContent = 'Erro ao carregar histórico.';
    }
}

document.addEventListener('DOMContentLoaded', carregarHistorico);
