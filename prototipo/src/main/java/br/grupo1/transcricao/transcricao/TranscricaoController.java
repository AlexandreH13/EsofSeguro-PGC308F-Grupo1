package br.grupo1.transcricao.transcricao;

import br.grupo1.transcricao.auditoria.Auditoria;
import br.grupo1.transcricao.sessao.Sessao;
import br.grupo1.transcricao.sessao.SessaoFilter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Prática 2 (RS01): toda rota que recebe um id de transcrição confere, na própria consulta,
 * que o registro pertence ao promotor da sessão. Id alheio e id inexistente recebem a mesma
 * resposta, sem nenhum dado do recurso, e geram alerta de auditoria.
 */
@RestController
@RequestMapping("/api/transcricoes")
public class TranscricaoController {

    public record Resumo(UUID id, String processo, String vara, String midia, StatusTranscricao status,
                         String detalheStatus, Instant solicitadaEm) {
        static Resumo de(Transcricao t) {
            return new Resumo(t.getId(), t.getProcesso(), t.getVara(), t.getMidia(), t.getStatus(),
                    t.getDetalheStatus(), t.getSolicitadaEm());
        }
    }

    public record Detalhe(UUID id, String processo, String vara, String midia, StatusTranscricao status,
                          String detalheStatus, Instant solicitadaEm, String texto) {
        static Detalhe de(Transcricao t) {
            // RQF6: só transcrição finalizada expõe o conteúdo; nos demais status vai só o estado atual
            String texto = t.getStatus() == StatusTranscricao.FINALIZADA ? t.getTexto() : null;
            return new Detalhe(t.getId(), t.getProcesso(), t.getVara(), t.getMidia(), t.getStatus(),
                    t.getDetalheStatus(), t.getSolicitadaEm(), texto);
        }
    }

    public record Erro(String erro) {}

    private final TranscricaoRepository repositorio;
    private final Auditoria auditoria;

    public TranscricaoController(TranscricaoRepository repositorio, Auditoria auditoria) {
        this.repositorio = repositorio;
        this.auditoria = auditoria;
    }

    @GetMapping
    public List<Resumo> listar(@RequestAttribute(SessaoFilter.ATRIBUTO_SESSAO) Sessao sessao) {
        return repositorio.findByPromotorIdOrderBySolicitadaEmDesc(sessao.promotorId()).stream()
                .map(Resumo::de)
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> abrir(@PathVariable("id") UUID id,
                                   @RequestAttribute(SessaoFilter.ATRIBUTO_SESSAO) Sessao sessao) {
        return repositorio.findByIdAndPromotorId(id, sessao.promotorId())
                .<ResponseEntity<?>>map(t -> ResponseEntity.ok(Detalhe.de(t)))
                .orElseGet(() -> acessoNegado(sessao, id, "abrir"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> excluir(@PathVariable("id") UUID id,
                                     @RequestAttribute(SessaoFilter.ATRIBUTO_SESSAO) Sessao sessao) {
        var encontrada = repositorio.findByIdAndPromotorId(id, sessao.promotorId());
        if (encontrada.isEmpty()) {
            return acessoNegado(sessao, id, "excluir");
        }
        Transcricao transcricao = encontrada.get();
        if (transcricao.getStatus() == StatusTranscricao.NA_FILA
                || transcricao.getStatus() == StatusTranscricao.EM_PROCESSAMENTO) {
            // RQF9: exclusão só de transcrição finalizada ou com falha
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new Erro("exclusão não permitida durante o processamento"));
        }
        repositorio.delete(transcricao);
        return ResponseEntity.noContent().build();
    }

    private ResponseEntity<Erro> acessoNegado(Sessao sessao, UUID id, String operacao) {
        // Só o id entra no alerta. O conteúdo da transcrição nunca vai para o log.
        auditoria.alerta("ACESSO_NEGADO", sessao.promotorId(), "operação " + operacao + " na transcrição " + id);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Erro("acesso negado"));
    }
}
