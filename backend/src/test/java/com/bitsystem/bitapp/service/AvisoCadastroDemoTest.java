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
 * O aviso do cadastro promete apagar a conta em N dias. O número do texto
 * (português e espanhol) precisa ser o mesmo padrão da retenção no backend.
 */
class AvisoCadastroDemoTest {

    private static final Path BASE = Path.of(System.getProperty("basedir", "."));

    @Test
    void numeroDeDiasDoAvisoBateComOPadraoDaRetencao() throws IOException {
        String props = Files.readString(BASE.resolve("src/main/resources/application.properties"));
        Matcher padrao = Pattern.compile("demo\\.retencao-dias=\\$\\{DEMO_RETENCAO_DIAS:(\\d+)\\}").matcher(props);
        assertThat(padrao.find()).as("demo.retencao-dias no application.properties").isTrue();
        String diasDoBackend = padrao.group(1);

        String i18n = Files.readString(BASE.resolve("../frontend/js/i18n.js").normalize());
        Matcher aviso = Pattern.compile("avisoDemoTexto:\\s*'[^']*?(\\d+) d[ií]as").matcher(i18n);
        List<String> diasDosAvisos = new ArrayList<>();
        while (aviso.find()) {
            diasDosAvisos.add(aviso.group(1));
        }

        assertThat(diasDosAvisos).as("aviso em português e em espanhol").hasSize(2);
        assertThat(diasDosAvisos).containsOnly(diasDoBackend);
    }

    @Test
    void cadastroMostraOAvisoAntesDoFormulario() throws IOException {
        String html = Files.readString(BASE.resolve("../frontend/cadastro.html").normalize());

        int aviso = html.indexOf("data-i18n=\"cadastro.avisoDemoTexto\"");
        int formulario = html.indexOf("<form id=\"formCadastro\"");

        assertThat(aviso).isPositive();
        assertThat(aviso).isLessThan(formulario);
    }
}
