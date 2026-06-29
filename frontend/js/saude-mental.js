import { saudeCheckin } from './api.js';

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

let selectedMoodState = null;
let selectedNoteState = 5;

function selectMood(btn, mood, note) {
    document.querySelectorAll('.mood-btn').forEach(el => {
        el.classList.remove('border-cyan-500', 'bg-slate-800');
        el.classList.add('border-slate-800', 'bg-slate-950');
        el.setAttribute('aria-pressed', 'false');
    });
    btn.classList.remove('border-slate-800', 'bg-slate-950');
    btn.classList.add('border-cyan-500', 'bg-slate-800');
    btn.setAttribute('aria-pressed', 'true');
    selectedMoodState = mood;
    selectedNoteState = note;
}
window.selectMood = selectMood;

document.getElementById('formSaude')?.addEventListener('submit', async (event) => {
    event.preventDefault();
    if (!selectedMoodState) { alert('Selecione um emoji.'); return; }
    const submitBtn = event.target.querySelector('button[type="submit"]');
    if (submitBtn) { submitBtn.disabled = true; submitBtn.innerHTML = '<span class="loader"></span> Salvando...'; }
    try {
        const data = await saudeCheckin({
            usuarioId: usuario.id,
            humor: selectedMoodState,
            notaSemanal: selectedNoteState,
            contexto: document.getElementById('healthContext')?.value || '',
        });
        renderAiResponse(data);
    } catch { alert('Erro ao salvar check-in.'); }
    finally { if (submitBtn) { submitBtn.disabled = false; submitBtn.textContent = 'Enviar Registro'; } }
});

function renderAiResponse(data) {
    const container = document.getElementById('aiResponseContainer');
    const title = document.getElementById('aiResponseTitle');
    const msg = document.getElementById('aiResponseMsg');
    const action = document.getElementById('aiResponseAction');
    container.classList.remove('hidden');
    if (data.derivarCvv) {
        container.className = 'p-6 rounded-3xl border border-rose-900 bg-rose-950/30 mt-6 animate-fade-in';
        title.className = 'text-sm font-bold uppercase tracking-wider mb-2 text-rose-400 flex items-center gap-2';
        title.innerHTML = '⚠️ Suporte Crítico Ativado';
        msg.innerText = data.mensagem;
        action.innerHTML = `<div class="p-4 bg-slate-950 border border-rose-800/50 rounded-2xl space-y-2">
            <p class="text-sm font-bold text-white">CVV — Disque 188</p>
            <p class="text-xs text-slate-400">${data.acaoSugerida}</p></div>`;
    } else {
        container.className = 'p-6 rounded-3xl border border-slate-800 bg-slate-900/60 mt-6 animate-fade-in';
        title.className = 'text-sm font-bold uppercase tracking-wider mb-2 text-cyan-400';
        title.innerText = '✨ Resposta do Agente BiT';
        msg.innerText = data.mensagem;
        action.innerHTML = `<p class="text-xs text-slate-400 font-semibold mb-1">Ação:</p>
            <p class="text-sm text-slate-200">${data.acaoSugerida}</p>`;
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
