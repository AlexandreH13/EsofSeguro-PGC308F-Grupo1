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
    private final List<Alerta> eventos = new CopyOnWriteArrayList<>();

    public void alerta(String tipo, String promotorId, String detalhe) {
        Alerta alerta = new Alerta(Instant.now(), tipo, promotorId, detalhe);
        log.warn("ALERTA {} promotor={} {}", tipo, promotorId, detalhe);
        alertas.add(alerta);
    }

    /** Evento da Etapa 6. Não entra na lista de alertas, para os testes de 403 continuarem contando só o alerta. */
    public void evento(String tipo, String promotorId, String detalhe) {
        Alerta evento = new Alerta(Instant.now(), tipo, promotorId, detalhe);
        log.info("EVENTO {} promotor={} {}", tipo, promotorId, detalhe);
        eventos.add(evento);
    }

    public List<Alerta> alertas() {
        return List.copyOf(alertas);
    }

    public List<Alerta> eventos() {
        return List.copyOf(eventos);
    }

    public void limpar() {
        alertas.clear();
        eventos.clear();
    }
}
