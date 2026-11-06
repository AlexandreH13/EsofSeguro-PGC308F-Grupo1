package br.grupo1.transcricao.auditoria;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Alertas de segurança. Vão para o log e ficam em memória para os testes conferirem.
 * Entram só identificadores; nunca o conteúdo de mídias, transcrições ou tokens.
 */
@Component
public class Auditoria {

    public record Alerta(Instant quando, String tipo, String promotorId, String detalhe) {}

    private static final Logger log = LoggerFactory.getLogger(Auditoria.class);

    private final List<Alerta> alertas = new CopyOnWriteArrayList<>();

    public void alerta(String tipo, String promotorId, String detalhe) {
        Alerta alerta = new Alerta(Instant.now(), tipo, promotorId, detalhe);
        log.warn("ALERTA {} promotor={} {}", tipo, promotorId, detalhe);
        alertas.add(alerta);
    }

    public List<Alerta> alertas() {
        return List.copyOf(alertas);
    }

    public void limpar() {
        alertas.clear();
    }
}
