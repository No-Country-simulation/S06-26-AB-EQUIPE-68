package com.bitsystem.bitapp.service;

import com.bitsystem.bitapp.dto.SaudeDto;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class EmotionResponseProvider {

    private static final Map<String, SaudeDto.RawResponse> RESPONSES = Map.of(
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

    public SaudeDto.RawResponse resolve(String humor, int notaSemanal) {
        String key = humor != null ? humor.toLowerCase().trim() : "";
        SaudeDto.RawResponse curated = RESPONSES.get(key);
        if (curated != null) {
            return curated;
        }
        if (notaSemanal < 4) {
            return RESPONSES.get("ansioso");
        }
        if (notaSemanal == 4) {
            return RESPONSES.get("cansado");
        }
        return RESPONSES.get("feliz");
    }
}
