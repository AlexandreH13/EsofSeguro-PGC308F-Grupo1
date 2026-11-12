package br.grupo1.transcricao.sessao;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Criação (RQF1), consulta e encerramento (RQF11) da sessão.
 * A sessão só nasce de um token de identidade aprovado pelo TokenIdentidadeValidador.
 */
@RestController
@RequestMapping("/api/sessao")
public class SessaoController {

    public record Pedido(String idToken) {}
    public record Criada(String tokenSessao, String promotorId, String nome) {}
    public record Quem(String promotorId, String nome) {}
    public record Erro(String erro) {}

    private static final Logger log = LoggerFactory.getLogger(SessaoController.class);

    private final TokenIdentidadeValidador validador;
    private final SessaoStore sessoes;

    public SessaoController(TokenIdentidadeValidador validador, SessaoStore sessoes) {
        this.validador = validador;
        this.sessoes = sessoes;
    }

    @PostMapping
    public ResponseEntity<?> criar(@RequestBody(required = false) Pedido pedido) {
        try {
            var identidade = validador.validar(pedido == null ? null : pedido.idToken());
            String token = sessoes.criar(new Sessao(identidade.promotorId(), identidade.nome()));
            return ResponseEntity.ok(new Criada(token, identidade.promotorId(), identidade.nome()));
        } catch (TokenIdentidadeValidador.TokenRecusado recusa) {
            // O motivo fica no log. A resposta é genérica para não orientar quem forja tokens.
            log.warn("Token de identidade recusado: {}", recusa.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Erro("erro de autenticação"));
        }
    }

    @GetMapping
    public Quem quem(@RequestAttribute(SessaoFilter.ATRIBUTO_SESSAO) Sessao sessao) {
        return new Quem(sessao.promotorId(), sessao.nome());
    }

    @DeleteMapping
    public ResponseEntity<Void> encerrar(HttpServletRequest requisicao) {
        SessaoFilter.extrairToken(requisicao).ifPresent(sessoes::encerrar);
        return ResponseEntity.noContent().build();
    }
}
