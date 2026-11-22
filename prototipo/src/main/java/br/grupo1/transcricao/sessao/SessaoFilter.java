package br.grupo1.transcricao.sessao;

import br.grupo1.transcricao.auditoria.Auditoria;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

/**
 * Exige sessão válida em toda rota /api, exceto a criação da sessão (RQF1).
 * O promotor autenticado fica disponível para os controllers como atributo da requisição.
 */
@Component
public class SessaoFilter extends OncePerRequestFilter {

    public static final String ATRIBUTO_SESSAO = "sessao";

    private final SessaoStore sessoes;
    private final Auditoria auditoria;

    public SessaoFilter(SessaoStore sessoes, Auditoria auditoria) {
        this.sessoes = sessoes;
        this.auditoria = auditoria;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest requisicao) {
        String caminho = requisicao.getRequestURI();
        boolean criacaoDeSessao = "POST".equals(requisicao.getMethod()) && "/api/sessao".equals(caminho);
        return !caminho.startsWith("/api/") || criacaoDeSessao;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest requisicao, HttpServletResponse resposta, FilterChain cadeia)
            throws ServletException, IOException {
        Optional<Sessao> sessao = extrairToken(requisicao).flatMap(sessoes::buscar);
        if (sessao.isEmpty()) {
            auditoria.evento("REQUISICAO_SEM_SESSAO", "-",
                    "rota=" + requisicao.getMethod() + " " + requisicao.getRequestURI()
                            + " origem=" + requisicao.getRemoteAddr());
            resposta.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resposta.setContentType("application/json");
            resposta.setCharacterEncoding("UTF-8");
            resposta.getWriter().write("{\"erro\":\"sessão inválida\"}");
            return;
        }
        requisicao.setAttribute(ATRIBUTO_SESSAO, sessao.get());
        cadeia.doFilter(requisicao, resposta);
    }

    public static Optional<String> extrairToken(HttpServletRequest requisicao) {
        String cabecalho = requisicao.getHeader("Authorization");
        if (cabecalho == null || !cabecalho.startsWith("Bearer ")) {
            return Optional.empty();
        }
        String token = cabecalho.substring("Bearer ".length()).trim();
        return token.isEmpty() ? Optional.empty() : Optional.of(token);
    }
}
