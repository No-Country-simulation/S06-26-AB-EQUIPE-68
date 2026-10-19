package com.bitsystem.bitapp.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

/**
 * Textos do CVV no frontend (português e espanhol): a caixa da semana diz de onde vem o
 * alerta, e o consentimento da nota 1 oferece "Não quero apoio agora" com o 188 discreto.
 * Não executa o JavaScript: confere o que está escrito em i18n.js e no HTML.
 */
class TextosCvvFrontendTest {

    private static final Path FRONTEND = Path.of(System.getProperty("basedir", ".")).resolve("../frontend").normalize();

    private static String ler(String arquivo) throws IOException {
        return Files.readString(FRONTEND.resolve(arquivo));
    }

    /** Valores de uma chave de i18n.js, na ordem em que aparecem: o 1º é português, o 2º é espanhol. */
    private static List<String> valores(String i18n, String chave) {
        Matcher m = Pattern.compile("\\b" + chave + ":\\s*'((?:[^'\\\\]|\\\\.)*)'").matcher(i18n);
        List<String> achados = new ArrayList<>();
        while (m.find()) {
            achados.add(m.group(1));
        }
        return achados;
    }

    @Test
    void caixaDaSemana_dizDeOndeVemOAlerta_emPortuguesEEspanhol() throws IOException {
        List<String> msgs = valores(ler("js/i18n.js"), "tendenciaMsg");

        assertThat(msgs).hasSize(2);
        assertThat(msgs.get(0)).startsWith("Pelos seus check-ins dos últimos dias, a semana tem sido pesada.")
                .contains("o CVV escuta 24h, sem julgamento");
        assertThat(msgs.get(1)).startsWith("Por tus check-ins de los últimos días, la semana ha sido pesada.")
                .contains("el CVV escucha 24h");
    }

    @Test
    void tituloDaCaixaDaSemana_naoMudou() throws IOException {
        List<String> titulos = valores(ler("js/i18n.js"), "tendenciaTitulo");

        assertThat(titulos).containsExactly("Essa semana parece pesada", "Esta semana parece pesada");
    }

    @Test
    void consentimento_temOsTextosDasTresSaidas_emPortuguesEEspanhol() throws IOException {
        String i18n = ler("js/i18n.js");

        assertThat(valores(i18n, "modalSim")).containsExactly("Sim, quero apoio", "Sí, quiero apoyo");
        assertThat(valores(i18n, "modalNaoQueroAgora")).containsExactly("Não quero apoio agora", "No quiero apoyo ahora");
        assertThat(valores(i18n, "modalNao")).containsExactly("Cliquei sem querer", "Fue sin querer");
    }

    @Test
    void linhaDiscretaDoCvv_temOLinkParaOTelefone188_emPortuguesEEspanhol() throws IOException {
        List<String> linhas = valores(ler("js/i18n.js"), "cvvSeMudarDeIdeia");

        assertThat(linhas).hasSize(2);
        assertThat(linhas.get(0)).startsWith("Se mudar de ideia, o CVV atende 24h pelo ").contains("href=\"tel:188\"");
        assertThat(linhas.get(1)).startsWith("Si cambias de idea, el CVV atiende 24h en el ").contains("href=\"tel:188\"");
    }

    @Test
    void paginaSaudeMental_temAsTresSaidasEOBotaoFechar_naOrdemCerta() throws IOException {
        String html = ler("saude-mental.html");

        int sim = html.indexOf("id=\"confirmSim\"");
        int recusa = html.indexOf("id=\"confirmRecusa\"");
        int semQuerer = html.indexOf("id=\"confirmNao\"");
        assertThat(sim).isPositive();
        assertThat(recusa).isGreaterThan(sim);
        assertThat(semQuerer).isGreaterThan(recusa);
        assertThat(html).contains("id=\"confirmFechar\"");
    }
}
