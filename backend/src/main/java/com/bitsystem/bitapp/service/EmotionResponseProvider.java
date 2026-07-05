package com.bitsystem.bitapp.service;

import com.bitsystem.bitapp.dto.SaudeDto;
import com.bitsystem.bitapp.model.NivelCheckin;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class EmotionResponseProvider {

    private static final SaudeDto.RawResponse SEM_NOTA_PT = new SaudeDto.RawResponse(
        "Obrigado por compartilhar um pouco de como você está. Cada check-in é um passo de cuidado com você mesmo.",
        "Reserve um momento hoje para uma pausa curta e consciente — mesmo pequena, ela conta."
    );
    private static final SaudeDto.RawResponse SEM_NOTA_ES = new SaudeDto.RawResponse(
        "Gracias por compartir un poco de cómo estás. Cada check-in es un paso de cuidado contigo mismo.",
        "Reserva un momento hoy para una pausa corta y consciente — aunque sea pequeña, cuenta."
    );

    private static final Map<NivelCheckin, SaudeDto.RawResponse> RESPONSES_PT = Map.of(
        NivelCheckin.MUITO_FELIZ,
        new SaudeDto.RawResponse(
            "Que bom saber que você está se sentindo muito bem hoje. Esse estado é um recurso valioso — reconheço o esforço que você tem investido na sua jornada tech.",
            "Aproveite 10 minutos para anotar o que funcionou bem esta semana. Isso reforça sua confiança nos próximos passos."
        ),
        NivelCheckin.FELIZ,
        new SaudeDto.RawResponse(
            "Bom saber que você está bem hoje. Dias assim valem a pena ser notados, mesmo em meio à correria da transição de carreira.",
            "Continue no seu ritmo — talvez seja um bom momento para avançar naquela tarefa que você andava adiando."
        ),
        NivelCheckin.TRANQUILO,
        new SaudeDto.RawResponse(
            "Obrigado por compartilhar um pouco de como você está. Cada check-in é um passo de cuidado com você mesmo.",
            "Reserve um momento hoje para uma pausa curta e consciente — mesmo pequena, ela conta."
        ),
        NivelCheckin.TRISTE,
        new SaudeDto.RawResponse(
            "Obrigado por confiar esse momento difícil. Tristeza na transição de carreira é real, e reconhecer isso já é um ato de coragem — não de fraqueza.",
            "Escreva três coisas que você já conquistou na tech, por menores que pareçam. Se precisar de acolhimento agora, o CVV (188) está disponível 24h."
        ),
        NivelCheckin.MUITO_TRISTE,
        new SaudeDto.RawResponse(
            "Vejo que o peso está grande demais agora. Momentos assim são sinais de que algo precisa de atenção agora — e pedir ajuda é a decisão mais forte que você pode tomar.",
            "Priorize seu cuidado imediato. Ligue 188 (CVV) ou acesse cvv.org.br — apoio humano, gratuito e sigiloso, 24 horas."
        )
    );

    private static final Map<NivelCheckin, SaudeDto.RawResponse> RESPONSES_ES = Map.of(
        NivelCheckin.MUITO_FELIZ,
        new SaudeDto.RawResponse(
            "Qué bueno saber que te sientes muy bien hoy. Este estado es un recurso valioso — reconozco el esfuerzo que has invertido en tu camino tech.",
            "Aprovecha 10 minutos para anotar lo que funcionó bien esta semana. Esto refuerza tu confianza en los próximos pasos."
        ),
        NivelCheckin.FELIZ,
        new SaudeDto.RawResponse(
            "Qué bueno saber que estás bien hoy. Días así valen la pena ser notados, incluso en medio de la transición de carrera.",
            "Sigue a tu ritmo — quizás sea un buen momento para avanzar en esa tarea que venías postergando."
        ),
        NivelCheckin.TRANQUILO,
        new SaudeDto.RawResponse(
            "Gracias por compartir un poco de cómo estás. Cada check-in es un paso de cuidado contigo mismo.",
            "Reserva un momento hoy para una pausa corta y consciente — aunque sea pequeña, cuenta."
        ),
        NivelCheckin.TRISTE,
        new SaudeDto.RawResponse(
            "Gracias por confiar este momento difícil. La tristeza en la transición de carrera es real, y reconocerlo ya es un acto de valentía — no de debilidad.",
            "Escribe tres cosas que ya lograste en tech, por pequeñas que parezcan. Si necesitas apoyo ahora, el CVV (188) está disponible 24h."
        ),
        NivelCheckin.MUITO_TRISTE,
        new SaudeDto.RawResponse(
            "Veo que el peso es demasiado grande ahora. Momentos así son señales de que algo necesita atención ahora — y pedir ayuda es la decisión más fuerte que puedes tomar.",
            "Prioriza tu cuidado inmediato. Llama al 188 (CVV) o entra a cvv.org.br — apoyo humano, gratuito y confidencial, 24 horas."
        )
    );

    public SaudeDto.RawResponse resolve(Integer nota, String idioma) {
        boolean es = "es".equalsIgnoreCase(idioma);
        if (nota == null) {
            return es ? SEM_NOTA_ES : SEM_NOTA_PT;
        }
        Map<NivelCheckin, SaudeDto.RawResponse> respostas = es ? RESPONSES_ES : RESPONSES_PT;
        return respostas.get(NivelCheckin.fromNota(nota));
    }
}
