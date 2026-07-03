package com.bitsystem.bitapp.seed;

import com.bitsystem.bitapp.dto.DicaLazerDto;
import java.util.List;
import java.util.Map;

/**
 * ============================================================================
 * SEED: DicasLazerSeed
 * ============================================================================
 *
 * Dicas de lazer/bem-estar por região, migradas de frontend/js/saude-mental.js
 * (DICAS_LAZER/DICAS_GERAIS — antes 100% hardcoded no cliente). Backend passa
 * a ser a fonte única; o frontend consome via GET /api/sugestoes/{usuarioId}.
 *
 * Cada dica ganhou dois campos que não existiam no front:
 *  - categoria: taxonomia fixa (natureza, esporte, cultura, social, calma,
 *    pausa, estudo) usada pelo fallback determinístico humor→categoria.
 *  - offlineFriendly: true para praticamente todas (são atividades físicas
 *    presenciais); false só para as 2 que dependem de streaming/ligação
 *    ("Ouça música que te acalma", "Converse com alguém querido").
 *
 * @author BiT System
 * @version 1.0.0
 */
public final class DicasLazerSeed {

    private DicasLazerSeed() {}

    public static final Map<String, List<DicaLazerDto>> DICAS_LAZER = Map.ofEntries(
        Map.entry("TRINDADE", List.of(
            new DicaLazerDto("📚", "Biblioteca Pública Alcides", "Acervo de 100 mil títulos, wifi gratuito e espaço de estudo. Leitura reduz o estresse em até 68%.", "estudo", true),
            new DicaLazerDto("🎭", "Teatro Ademir Rosa", "Programação cultural diversificada. Assistir teatro estimula empatia e reduz ansiedade.", "cultura", true),
            new DicaLazerDto("🌳", "Passeio pela UFSC", "Caminhe pelos corredores verdes do campus. Caminhada leve regula o cortisol.", "natureza", true),
            new DicaLazerDto("☕", "Café na Rua Meinhardt", "Momentos de pausa com café gourmet. Uma pausa consciente recarrega o foco.", "pausa", true),
            new DicaLazerDto("🎨", "Galeria de Arte da UFSC", "Exposições gratuitas de arte contemporânea. Arte eleva o humor e estimula a criatividade.", "cultura", true),
            new DicaLazerDto("🧘", "Yoga no Parque da UFSC", "Aulas gratuitas ao ar livre aos sábados. Yoga reduz ansiedade e melhora o sono.", "calma", true)
        )),
        Map.entry("CBD_BEIRAMAR", List.of(
            new DicaLazerDto("🌊", "Passeio Beira-Mar Norte", "Caminhada ou corrida na orla. Exercício aeróbico libera endorfina em 20 minutos.", "esporte", true),
            new DicaLazerDto("🏛️", "Mercado Público", "Visite barracas centenárias. Ambientes sociais combatem o isolamento.", "social", true),
            new DicaLazerDto("🎨", "Centro Cultural García", "Oficinas e exposições gratuitas. Criatividade é um antídoto natural contra o estresse.", "cultura", true),
            new DicaLazerDto("📖", "Livraria da Travessa", "Leitura de 15 min reduz batimentos cardíacos. Uma pausa literária transforma o dia.", "estudo", true),
            new DicaLazerDto("🌿", "Praça XV de Novembro", "Sente-se sob as árvores e respire. Contato com natureza urbana acalma a mente.", "calma", true),
            new DicaLazerDto("🎵", "Show na Casa de Cultura", "Eventos musicais gratuitos. Música libera dopamina e melhora o humor imediatamente.", "cultura", true)
        )),
        Map.entry("UFSC", List.of(
            new DicaLazerDto("🏫", "Trilhas do Campus", "Caminhe pelos 260 hectares de mata atlântica preservada. Natureza reduz cortisol em 12%.", "natureza", true),
            new DicaLazerDto("🔬", "Museu da UFSC", "Acervo de artes visuais e fotografias. Exposição cultural estimula novas perspectivas.", "cultura", true),
            new DicaLazerDto("📚", "Biblioteca Central", "Espaço silencioso para leitura e estudo. Ambiente focado reduz ruído mental.", "estudo", true),
            new DicaLazerDto("🏊", "Piscina do CAG", "Natação relaxa os músculos e liberta tensões. Atividade aquática é terapêutica.", "esporte", true),
            new DicaLazerDto("🌿", "Jardim Botânico (perto)", "Contato com plantas e flores. Botanicamente comprovado: natureza acalma.", "natureza", true),
            new DicaLazerDto("☕", "Café no Naufrago", "Pausa para café entre os estudiosos. Momentos de descontração fortalecem laços.", "social", true)
        )),
        Map.entry("CAMPECHE", List.of(
            new DicaLazerDto("🏖️", "Praia do Campeche", "Águas claras e arrecife de corais. O som da maré reduz ansiedade em 30%.", "natureza", true),
            new DicaLazerDto("🏝️", "Barco para Ilha do Campeche", "Excursão de fim de semana. Mudança de cenário renova as energias.", "natureza", true),
            new DicaLazerDto("🌳", "Parque Municipal do Campeche", "Trilhas ecológicas e áreas de lazer. Exercício ao ar livre eleva o humor.", "natureza", true),
            new DicaLazerDto("🎣", "Pescaria na Lagoa", "Atividade contemplativa e pacífica. Pescaria é meditação em movimento.", "calma", true),
            new DicaLazerDto("🚴", "Ciclismo na Litorânea", "Pedalar libera endorfina. 30 minutos de bicicleta melhoram o sono.", "esporte", true),
            new DicaLazerDto("🌅", "Pôr do sol no Morro", "Contemplação da natureza. Observar o horizonte amplia a percepção e acalma.", "calma", true)
        )),
        Map.entry("INGLESES", List.of(
            new DicaLazerDto("🏖️", "Praia dos Ingleses", "Caminhe pela orla ao nascer do sol. Luz natural regula o ritmo circadiano.", "natureza", true),
            new DicaLazerDto("🌳", "Lagoinha da Baía", "Trilha leve até a lagoinha. Contato com água doce é profundamente relaxante.", "natureza", true),
            new DicaLazerDto("📚", "Biblioteca Comunitária", "Espaço comunitário com wi-fi e atividades. Socialização reduz sentimentos de solidão.", "estudo", true),
            new DicaLazerDto("🍉", "Feira do Ingleses", "Produtos regionais e gastronomia local. Alimentação saudável impacta o humor.", "social", true),
            new DicaLazerDto("🏄", "Aula de Surf", "Surf é meditação na água. A combinação de exercício e natureza é poderosa.", "esporte", true),
            new DicaLazerDto("🌿", "Trilha do Morro da Lagoa", "Vista panorâmica e ar puro. Exercício em altitude libera endorfina tripla.", "esporte", true)
        )),
        Map.entry("LAGOA_CONCEICAO", List.of(
            new DicaLazerDto("🚣", "Stand Up Paddle na Lagoa", "Equilíbrio na água é meditação ativa. SUP reduz ansiedade em 40%.", "esporte", true),
            new DicaLazerDto("🌿", "Trilha da Lagoa do Peri", "Mata atlântica preservada. Caminhada na floresta reduz pressão arterial.", "natureza", true),
            new DicaLazerDto("☕", "Cafés da Lagoa", "Bares à beira d'água. Pausas sociais fortalecem o bem-estar emocional.", "social", true),
            new DicaLazerDto("🎨", "Artesanato Local", "Galerias e ateliês de artistas locais. Criatividade é terapia.", "cultura", true),
            new DicaLazerDto("🌅", "Mirante da Lagoa", "Contemplação ao pôr do sol. Meditação visual acalma a mente em minutos.", "calma", true),
            new DicaLazerDto("🎵", "Música ao vivo nos bares", "Samba e MPB à beira da lagoa. Música ao vivo libera oxitocina.", "social", true)
        )),
        Map.entry("ESTREITO_CAPOEIRAS", List.of(
            new DicaLazerDto("🌳", "Parque da Cidade Sarah", "870 mil m² de natureza urbana. Caminhada no parque reduz estresse em 25%.", "natureza", true),
            new DicaLazerDto("🧗", "Rocha de Escalada", "Escalada desafia e distrai. Foco físico esquece preocupações mentais.", "esporte", true),
            new DicaLazerDto("🏃", "Linha do Corredor", "Pista de cooper ao redor do parque. 30 min de corrida = 4h de bom humor.", "esporte", true),
            new DicaLazerDto("🧘", "Yoga ao ar livre", "Aulas gratuitas no parque. Yoga restaura o equilíbrio corpo-mente.", "calma", true),
            new DicaLazerDto("🌿", "Lago e Trilhas", "Caminhada ecológica com vistas do lago. Natureza é o melhor remédio natural.", "natureza", true),
            new DicaLazerDto("📚", "Leitura no Parque", "Leve um livro e sente-se na grama. Leitura ao ar livre amplifica o relaxamento.", "estudo", true)
        )),
        Map.entry("SAO_JOSE_CENTRO", List.of(
            new DicaLazerDto("🏛️", "Feira de São José", "Produtos artesanais e gastronomia. Exposição cultural diversifica perspectivas.", "social", true),
            new DicaLazerDto("🌳", "Parque Municipal", "Verde no coração da cidade. Contato com natureza urbana acalma a ansiedade.", "natureza", true),
            new DicaLazerDto("📚", "Biblioteca Municipal", "Espaço de estudo e leitura. Ambiente silencioso restaura a concentração.", "estudo", true),
            new DicaLazerDto("🎭", "Teatro Municipal", "Peças e espetáculos culturais. Arte cênica desperta emoções positivas.", "cultura", true),
            new DicaLazerDto("☕", "Café do Centro", "Pausa para café em praça histórica. Momentos de pausa são essenciais.", "pausa", true),
            new DicaLazerDto("🚴", "Ciclovia do Centro", "Pedale pelo centro histórico. Ciclismo combate sintomas de depressão leve.", "esporte", true)
        ))
    );

    public static final List<DicaLazerDto> DICAS_GERAIS = List.of(
        new DicaLazerDto("🌿", "Caminhe 20 minutos", "Exercício leve ao ar livre regula o cortisol e melhora o humor em minutos.", "esporte", true),
        new DicaLazerDto("📖", "Leia por 15 minutos", "Leitura reduz o estresse em 68%. Escolha algo que te escape da rotina.", "estudo", true),
        new DicaLazerDto("☕", "Faça uma pausa consciente", "Café ou chá sem celular. Momentos de quietude restauram o foco.", "pausa", true),
        new DicaLazerDto("🎵", "Ouça música que te acalma", "Música a 60 BPM sincroniza o coração e reduz ansiedade.", "calma", false),
        new DicaLazerDto("🌳", "Conecte-se com a natureza", "15 min de contato com verde reduz batimentos cardíacos e acalma.", "natureza", true),
        new DicaLazerDto("💬", "Converse com alguém querido", "Socialização libera oxitocina. Uma ligação de 10 min muda o dia.", "social", false)
    );
}