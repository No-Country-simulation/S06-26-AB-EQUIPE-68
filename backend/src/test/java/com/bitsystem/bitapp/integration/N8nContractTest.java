package com.bitsystem.bitapp.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.bitsystem.bitapp.dto.AssessmentDto;
import com.bitsystem.bitapp.dto.MentalHealthDto;
import org.junit.jupiter.api.Test;

/**
 * Contrato de resposta do webhook bit/agent do n8n: os exemplos em
 * src/test/resources/contracts/n8n/ são desserializados pelos mesmos DTOs e pelo
 * mesmo ObjectMapper que o N8NBaseClient usa. O formato dos exemplos é o que o nó
 * "Responder" do workflow devolve (um objeto, não uma lista).
 */
class N8nContractTest {

    @Test
    void saudeMental_respostaDoAgente_trazMensagemEAcaoSugerida() {
        MentalHealthDto.Response resposta = ContratosN8n.ler("mental-health.response", MentalHealthDto.Response.class);

        assertThat(resposta.mensagem()).startsWith("Semanas assim cansam mesmo");
        assertThat(resposta.acaoSugerida()).contains("respiração 4-4-6");
    }

    @Test
    void saudeMental_parseFalhou_deixaOsCamposNulosParaOFallbackAssumir() {
        MentalHealthDto.Response resposta = ContratosN8n.ler("mental-health.response-parse-falhou", MentalHealthDto.Response.class);

        assertThat(resposta.mensagem()).isNull();
        assertThat(resposta.acaoSugerida()).isNull();
    }

    @Test
    void assessment_respostaDoAgente_trazOsCincoCampos() {
        AssessmentDto.Response resposta = ContratosN8n.ler("assessment.response", AssessmentDto.Response.class);

        assertThat(resposta.compatibilidade()).isEqualTo(72);
        assertThat(resposta.nivel()).isEqualTo("Júnior");
        assertThat(resposta.pontosFortes()).containsExactly("Base sólida em Java", "Boa comunicação");
        assertThat(resposta.gaps()).containsExactly("Testes automatizados", "Spring Boot");
        assertThat(resposta.planoDesenvolvimento()).hasSize(2);
    }

    @Test
    void assessment_parseFalhou_deixaCompatibilidadeNulaParaOServicoTratarComoFalha() {
        AssessmentDto.Response resposta = ContratosN8n.ler("assessment.response-parse-falhou", AssessmentDto.Response.class);

        assertThat(resposta.compatibilidade()).isNull();
        assertThat(resposta.nivel()).isNull();
    }

    @Test
    void assessment_requestDeExemplo_temOsMesmosCamposDoDtoDeEnvio() throws Exception {
        AssessmentDto.Request enviado = ContratosN8n.ler("assessment.request", AssessmentDto.Request.class);

        // Reserializar o DTO e comparar com o exemplo garante que nenhum campo some nem sobra.
        assertThat(ContratosN8n.campos(ContratosN8n.MAPPER_DO_CLIENTE.valueToTree(enviado)))
                .containsExactlyInAnyOrderElementsOf(ContratosN8n.campos(ContratosN8n.json("assessment.request")));
        assertThat(enviado.tipo()).isEqualTo("orientacao");
    }
}
