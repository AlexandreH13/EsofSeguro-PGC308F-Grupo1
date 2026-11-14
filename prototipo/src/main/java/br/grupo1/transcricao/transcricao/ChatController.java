package br.grupo1.transcricao.transcricao;

import br.grupo1.transcricao.auditoria.Auditoria;
import br.grupo1.transcricao.sessao.Sessao;
import br.grupo1.transcricao.sessao.SessaoFilter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/chats")
public class ChatController {

    public record Iniciar(UUID transcricaoId) {}
    public record Pergunta(String texto) {}
    public record Mensagem(String autor, String texto, Instant enviadaEm) {}
    public record Visao(UUID id, UUID transcricaoId, String midia, String processo, List<Mensagem> mensagens) {
        static Visao de(ChatStore.Conversa conversa) {
            List<Mensagem> mensagens = conversa.mensagens().stream()
                    .map(mensagem -> new Mensagem(mensagem.autor(), mensagem.texto(), mensagem.enviadaEm()))
                    .toList();
            return new Visao(conversa.id(), conversa.transcricaoId(), conversa.midia(), conversa.processo(), mensagens);
        }
    }
    public record Erro(String erro) {}

    private final TranscricaoRepository transcricoes;
    private final ChatStore chats;
    private final Auditoria auditoria;

    public ChatController(TranscricaoRepository transcricoes, ChatStore chats, Auditoria auditoria) {
        this.transcricoes = transcricoes;
        this.chats = chats;
        this.auditoria = auditoria;
    }

    @PostMapping
    public ResponseEntity<?> iniciar(@RequestBody(required = false) Iniciar pedido,
                                     @RequestAttribute(SessaoFilter.ATRIBUTO_SESSAO) Sessao sessao) {
        if (pedido == null || pedido.transcricaoId() == null) {
            return ResponseEntity.badRequest().body(new Erro("transcrição obrigatória"));
        }
        var transcricao = transcricoes.findByIdAndPromotorId(pedido.transcricaoId(), sessao.promotorId());
        if (transcricao.isEmpty()) return acessoNegado(sessao, pedido.transcricaoId());
        Transcricao recurso = transcricao.get();
        if (recurso.getStatus() != StatusTranscricao.FINALIZADA || recurso.getTexto() == null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new Erro("transcrição ainda não disponível"));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(Visao.de(chats.criar(recurso)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> consultar(@PathVariable UUID id,
                                       @RequestAttribute(SessaoFilter.ATRIBUTO_SESSAO) Sessao sessao) {
        return chats.buscar(id, sessao.promotorId())
                .<ResponseEntity<?>>map(conversa -> ResponseEntity.ok(Visao.de(conversa)))
                .orElseGet(() -> acessoNegado(sessao, id));
    }

    @PostMapping("/{id}/mensagens")
    public ResponseEntity<?> perguntar(@PathVariable UUID id, @RequestBody(required = false) Pergunta pedido,
                                       @RequestAttribute(SessaoFilter.ATRIBUTO_SESSAO) Sessao sessao) {
        if (pedido == null || pedido.texto() == null || pedido.texto().isBlank() || pedido.texto().length() > 4000) {
            return ResponseEntity.badRequest().body(new Erro("pergunta inválida"));
        }
        var conversa = chats.buscar(id, sessao.promotorId());
        if (conversa.isEmpty()) return acessoNegado(sessao, id);
        chats.perguntar(conversa.get(), pedido.texto().trim());
        return ResponseEntity.ok(Visao.de(conversa.get()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> encerrar(@PathVariable UUID id,
                                      @RequestAttribute(SessaoFilter.ATRIBUTO_SESSAO) Sessao sessao) {
        if (chats.buscar(id, sessao.promotorId()).isEmpty()) return acessoNegado(sessao, id);
        chats.encerrar(id);
        return ResponseEntity.noContent().build();
    }

    private ResponseEntity<Erro> acessoNegado(Sessao sessao, UUID id) {
        auditoria.alerta("ACESSO_NEGADO", sessao.promotorId(), "operação no chat " + id);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Erro("acesso negado"));
    }
}
