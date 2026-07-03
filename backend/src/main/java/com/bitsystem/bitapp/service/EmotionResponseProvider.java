package com.bitsystem.bitapp.service;

import com.bitsystem.bitapp.dto.SaudeDto;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class EmotionResponseProvider {

    private static final Map<String, SaudeDto.RawResponse> RESPONSES_PT = Map.of(
        "feliz",
        new SaudeDto.RawResponse(
            "Que bom saber que você está se sentindo leve hoje. Esse estado é um recurso valioso — reconheço o esforço que você tem investido na sua jornada tech.",
            "Aproveite 10 minutos para anotar o que funcionou bem esta semana. Isso reforça sua confiança nos próximos passos."
        ),
        "cansado",
        new SaudeDto.RawResponse(
            "Percebo o cansaço, e faz sentido: aprender tecnologia exige energia constante. Você não está falhando — seu corpo está pedindo pausa.",
            "Feche o editor por 15 minutos, beba água e faça alongamentos leves. Retome com uma tarefa pequena e concreta."
        ),
        "ansioso",
        new SaudeDto.RawResponse(
            "Sinto a ansiedade no que você compartilhou, e ela é compreensível diante de entrevistas e pressão financeira. Seus sentimentos são válidos — você não precisa enfrentar isso sozinho.",
            "Respire 4 segundos, segure 4, solte 6 — repita 5 vezes. Se a angústia persistir, converse com o CVV pelo 188 (24h, gratuito e sigiloso)."
        ),
        "triste",
        new SaudeDto.RawResponse(
            "Obrigado por confiar esse momento difícil. Tristeza na transição de carreira é real, e reconhecer isso já é um ato de coragem — não de fraqueza.",
            "Escreva três coisas que você já conquistou na tech, por menores que pareçam. Se precisar de acolhimento agora, o CVV (188) está disponível 24h."
        ),
        "sobrecarregado",
        new SaudeDto.RawResponse(
            "Vejo que o peso está grande demais agora. Burnout e exaustão são sinais de que algo precisa mudar — e pedir ajuda é a decisão mais forte que você pode tomar.",
            "Priorize descanso imediato: pare de estudar hoje. Ligue 188 (CVV) ou acesse cvv.org.br — apoio humano, gratuito e sigiloso, 24 horas."
        )
    );

    private static final Map<String, SaudeDto.RawResponse> RESPONSES_ES = Map.of(
        "feliz",
        new SaudeDto.RawResponse(
            "Qué bueno saber que te sientes ligero hoy. Este estado es un recurso valioso — reconozco el esfuerzo que has invertido en tu camino tech.",
            "Aprovecha 10 minutos para anotar lo que funcionó bien esta semana. Esto refuerza tu confianza en los próximos pasos."
        ),
        "cansado",
        new SaudeDto.RawResponse(
            "Percibo el cansancio, y tiene sentido: aprender tecnología exige energía constante. No estás fallando — tu cuerpo está pidiendo una pausa.",
            "Cierra el editor por 15 minutos, bebe agua y haz estiramientos suaves. Retoma con una tarea pequeña y concreta."
        ),
        "ansioso",
        new SaudeDto.RawResponse(
            "Siento la ansiedad en lo que compartiste, y es comprensible ante entrevistas y presión financiera. Tus sentimientos son válidos — no necesitas enfrentar esto solo.",
            "Respira 4 segundos, sostén 4, suelta 6 — repite 5 veces. Si la angustia persiste, habla con el CVV al 188 (24h, gratuito y confidencial)."
        ),
        "triste",
        new SaudeDto.RawResponse(
            "Gracias por confiar este momento difícil. La tristeza en la transición de carrera es real, y reconocerlo ya es un acto de valentía — no de debilidad.",
            "Escribe tres cosas que ya lograste en tech, por pequeñas que parezcan. Si necesitas apoyo ahora, el CVV (188) está disponible 24h."
        ),
        "sobrecarregado",
        new SaudeDto.RawResponse(
            "Veo que el peso es demasiado grande ahora. El burnout y el agotamiento son señales de que algo necesita cambiar — y pedir ayuda es la decisión más fuerte que puedes tomar.",
            "Prioriza el descanso inmediato: deja de estudiar hoy. Llama al 188 (CVV) o entra a cvv.org.br — apoyo humano, gratuito y confidencial, 24 horas."
        )
    );

    public SaudeDto.RawResponse resolve(String humor, int notaSemanal, String idioma) {
        Map<String, SaudeDto.RawResponse> respostas = "es".equalsIgnoreCase(idioma) ? RESPONSES_ES : RESPONSES_PT;
        String key = humor != null ? humor.toLowerCase().trim() : "";
        SaudeDto.RawResponse curated = respostas.get(key);
        if (curated != null) {
            return curated;
        }
        if (notaSemanal < 4) {
            return respostas.get("ansioso");
        }
        if (notaSemanal == 4) {
            return respostas.get("cansado");
        }
        return respostas.get("feliz");
    }
}
