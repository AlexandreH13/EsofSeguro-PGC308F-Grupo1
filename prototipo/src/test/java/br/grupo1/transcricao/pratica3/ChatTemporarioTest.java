package br.grupo1.transcricao.pratica3;

import br.grupo1.transcricao.auditoria.Auditoria;
import br.grupo1.transcricao.sessao.Sessao;
import br.grupo1.transcricao.sessao.SessaoStore;
import br.grupo1.transcricao.transcricao.ChatStore;
import br.grupo1.transcricao.transcricao.StatusTranscricao;
import br.grupo1.transcricao.transcricao.Transcricao;
import br.grupo1.transcricao.transcricao.TranscricaoRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ChatTemporarioTest {

    @Autowired
    private MockMvc http;
    @Autowired
    private TranscricaoRepository transcricoes;
    @Autowired
    private SessaoStore sessoes;
    @Autowired
    private ChatStore chats;
    @Autowired
    private Auditoria auditoria;

    private Transcricao transcricao;
    private String sessaoA;
    private String sessaoB;

    @BeforeEach
    void preparar() {
        transcricoes.deleteAll();
        chats.encerrarDoPromotor("promotor-a");
        chats.encerrarDoPromotor("promotor-b");
        auditoria.limpar();
        transcricao = transcricoes.save(new Transcricao("promotor-a", "0001234-56.2025.8.26.0100",
                "2ª Vara Criminal", "depoimento.mp3", StatusTranscricao.FINALIZADA,
                null, Instant.now(), "00:00:01 relato de demonstração"));
        sessaoA = sessoes.criar(new Sessao("promotor-a", "Ana Souza"));
        sessaoB = sessoes.criar(new Sessao("promotor-b", "Beatriz Ramos"));
    }

    @Test
    void conversaETemporariaProtegidaPorPromotorEApagadaNoLogout() throws Exception {
        String corpo = http.perform(post("/api/chats")
                        .header("Authorization", "Bearer " + sessaoA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"transcricaoId\":\"" + transcricao.getId() + "\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String chatId = JsonPath.read(corpo, "$.id");
        assertEquals(1, chats.quantidade());

        http.perform(post("/api/chats/{id}/mensagens", chatId)
                        .header("Authorization", "Bearer " + sessaoA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"texto\":\"Quais são os pontos principais?\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensagens.length()").value(2));

        http.perform(get("/api/chats/{id}", chatId)
                        .header("Authorization", "Bearer " + sessaoB))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.erro").value("acesso negado"));
        assertTrue(auditoria.alertas().stream().anyMatch(alerta -> alerta.tipo().equals("ACESSO_NEGADO")));

        http.perform(delete("/api/sessao").header("Authorization", "Bearer " + sessaoA))
                .andExpect(status().isNoContent());
        assertEquals(0, chats.quantidade(), "logout descarta o contexto e o histórico temporários");
        http.perform(get("/api/chats/{id}", chatId)
                        .header("Authorization", "Bearer " + sessaoA))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void naoIniciaChatDeTranscricaoAlheiaOuNaoFinalizada() throws Exception {
        Transcricao deB = transcricoes.save(new Transcricao("promotor-b", "0009876-54.2025.8.26.0100",
                "Vara de teste", "b.mp3", StatusTranscricao.FINALIZADA, null, Instant.now(), "texto de B"));
        Transcricao pendente = transcricoes.save(new Transcricao("promotor-a", "0001111-22.2025.8.26.0100",
                "Vara de teste", "pendente.mp3", StatusTranscricao.EM_PROCESSAMENTO, null, Instant.now(), null));

        http.perform(post("/api/chats")
                        .header("Authorization", "Bearer " + sessaoA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"transcricaoId\":\"" + deB.getId() + "\"}"))
                .andExpect(status().isForbidden());
        http.perform(post("/api/chats")
                        .header("Authorization", "Bearer " + sessaoA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"transcricaoId\":\"" + pendente.getId() + "\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.erro").value("transcrição ainda não disponível"));
        assertEquals(0, chats.quantidade());
    }

        @Test
        void encerrarChatApagaContextoEHistorico() throws Exception {
                String corpo = http.perform(post("/api/chats")
                                                .header("Authorization", "Bearer " + sessaoA)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content("{\"transcricaoId\":\"" + transcricao.getId() + "\"}"))
                                .andExpect(status().isCreated())
                                .andReturn().getResponse().getContentAsString();
                String chatId = JsonPath.read(corpo, "$.id");

                http.perform(delete("/api/chats/{id}", chatId)
                                                .header("Authorization", "Bearer " + sessaoA))
                                .andExpect(status().isNoContent());

                assertEquals(0, chats.quantidade());
                http.perform(get("/api/chats/{id}", chatId)
                                                .header("Authorization", "Bearer " + sessaoA))
                                .andExpect(status().isForbidden());
        }
}
