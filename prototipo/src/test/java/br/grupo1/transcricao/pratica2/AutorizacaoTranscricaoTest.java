package br.grupo1.transcricao.pratica2;

import br.grupo1.transcricao.auditoria.Auditoria;
import br.grupo1.transcricao.sessao.Sessao;
import br.grupo1.transcricao.sessao.SessaoStore;
import br.grupo1.transcricao.transcricao.StatusTranscricao;
import br.grupo1.transcricao.transcricao.Transcricao;
import br.grupo1.transcricao.transcricao.TranscricaoRepository;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.io.ByteArrayInputStream;
import java.time.Instant;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prática 2, RS01. Testes TS04 a TS06 de docs/etapa4-codigo-seguro.md, escritos antes do controller.
 * Cenário: promotor A com sessão válida; uma transcrição de A e uma de B no registro.
 */
@SpringBootTest
@AutoConfigureMockMvc
class AutorizacaoTranscricaoTest {

    private static final String TEXTO_DE_A = "Texto da transcrição de A";
    private static final String TEXTO_DE_B = "Texto sigiloso da transcrição de B";

    @Autowired
    private MockMvc http;
    @Autowired
    private TranscricaoRepository repositorio;
    @Autowired
    private SessaoStore sessoes;
    @Autowired
    private Auditoria auditoria;

    private Transcricao deA;
    private Transcricao deB;
    private String sessaoDeA;

    @BeforeEach
    void cenario() {
        deA = repositorio.save(nova("promotor-a", "Depoimento_de_A.mp3", TEXTO_DE_A));
        deB = repositorio.save(nova("promotor-b", "Depoimento_de_B.mp3", TEXTO_DE_B));
        sessaoDeA = sessoes.criar(new Sessao("promotor-a", "Ana Souza"));
        auditoria.limpar();
    }

    @Test
    void ts04_abrirTranscricaoDeOutroPromotorEhNegadoSemDevolverDados() throws Exception {
        comoA(get("/api/transcricoes/{id}", deB.getId()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.erro").value("acesso negado"))
                .andExpect(content().string(not(containsString(TEXTO_DE_B))))
                .andExpect(content().string(not(containsString(deB.getMidia()))))
                .andExpect(content().string(not(containsString(deB.getProcesso()))));
        assertAlertaDeAcessoNegado(deB.getId());
    }

    @Test
    void ts04_excluirTranscricaoDeOutroPromotorEhNegadoENaoAlteraNada() throws Exception {
        comoA(delete("/api/transcricoes/{id}", deB.getId()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.erro").value("acesso negado"));
        assertTrue(repositorio.existsById(deB.getId()), "a transcrição de B continua no registro");
        assertAlertaDeAcessoNegado(deB.getId());
    }

    @Test
    void ts04_idInexistenteRecebeAMesmaRespostaDoIdAlheio() throws Exception {
        UUID inexistente = UUID.randomUUID();
        comoA(get("/api/transcricoes/{id}", inexistente))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.erro").value("acesso negado"));
        assertAlertaDeAcessoNegado(inexistente);
    }

    @Test
    void ts05_abrirPropriaTranscricaoDevolveOConteudo() throws Exception {
        comoA(get("/api/transcricoes/{id}", deA.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(deA.getId().toString()))
                .andExpect(jsonPath("$.texto").value(TEXTO_DE_A));
        assertTrue(auditoria.alertas().isEmpty(), "acesso legítimo não gera alerta");
    }

    @Test
    void ts05_excluirPropriaTranscricaoRemoveORegistro() throws Exception {
        comoA(delete("/api/transcricoes/{id}", deA.getId()))
                .andExpect(status().isNoContent());
        assertFalse(repositorio.existsById(deA.getId()));
        assertTrue(auditoria.alertas().isEmpty(), "acesso legítimo não gera alerta");
    }

    @Test
    void ts05_baixarDocxDaPropriaTranscricaoGeraDocumentoValido() throws Exception {
        byte[] conteudo = comoA(get("/api/transcricoes/{id}/docx", deA.getId()))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("attachment")))
                .andReturn().getResponse().getContentAsByteArray();

        try (XWPFDocument documento = new XWPFDocument(new ByteArrayInputStream(conteudo))) {
            assertTrue(documento.getParagraphs().stream()
                    .anyMatch(paragrafo -> paragrafo.getText().contains(TEXTO_DE_A)));
        }
        assertTrue(auditoria.alertas().isEmpty(), "download legítimo não gera alerta");
    }

    @Test
    void ts05_baixarDocxDeOutroPromotorEhNegado() throws Exception {
        comoA(get("/api/transcricoes/{id}/docx", deB.getId()))
                .andExpect(status().isForbidden())
                .andExpect(content().string(not(containsString(TEXTO_DE_B))));
        assertAlertaDeAcessoNegado(deB.getId());
    }

    @Test
    void ts05_baixarDocxAntesDaFinalizacaoEhRecusado() throws Exception {
        Transcricao pendente = repositorio.save(new Transcricao("promotor-a", "0000000-00.2026.8.26.0000",
            "Vara de teste", "pendente.mp3", StatusTranscricao.EM_PROCESSAMENTO,
            null, Instant.now(), null));

        comoA(get("/api/transcricoes/{id}/docx", pendente.getId()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.erro").value("transcrição ainda não disponível"));
    }

    @Test
    void ts06_idsSaoUuidVersao4ESemRelacaoEntreSi() {
        Transcricao primeira = nova("promotor-a", "a.mp3", null);
        Transcricao segunda = nova("promotor-a", "b.mp3", null);
        assertEquals(4, primeira.getId().version());
        assertEquals(4, segunda.getId().version());
        assertNotEquals(primeira.getId(), segunda.getId());
    }

    private ResultActions comoA(org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder requisicao) throws Exception {
        return http.perform(requisicao.header("Authorization", "Bearer " + sessaoDeA));
    }

    private void assertAlertaDeAcessoNegado(UUID id) {
        var alertas = auditoria.alertas();
        assertEquals(1, alertas.size(), "um alerta por tentativa");
        assertEquals("ACESSO_NEGADO", alertas.get(0).tipo());
        assertEquals("promotor-a", alertas.get(0).promotorId());
        assertTrue(alertas.get(0).detalhe().contains(id.toString()));
        assertFalse(alertas.get(0).detalhe().contains(TEXTO_DE_B), "o alerta não carrega conteúdo");
    }

    private static Transcricao nova(String promotorId, String midia, String texto) {
        return new Transcricao(promotorId, "0000000-00.2026.8.26.0000", "Vara de teste", midia,
                StatusTranscricao.FINALIZADA, null, Instant.now(), texto);
    }
}
