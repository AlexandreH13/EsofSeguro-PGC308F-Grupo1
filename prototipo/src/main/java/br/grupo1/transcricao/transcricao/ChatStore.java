package br.grupo1.transcricao.transcricao;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class ChatStore {

    public record Mensagem(String autor, String texto, Instant enviadaEm) {}

    public static final class Conversa {
        private final UUID id = UUID.randomUUID();
        private final UUID transcricaoId;
        private final String promotorId;
        private final String midia;
        private final String processo;
        private final String contexto;
        private final List<Mensagem> mensagens = new CopyOnWriteArrayList<>();

        private Conversa(Transcricao transcricao) {
            this.transcricaoId = transcricao.getId();
            this.promotorId = transcricao.getPromotorId();
            this.midia = transcricao.getMidia();
            this.processo = transcricao.getProcesso();
            this.contexto = transcricao.getTexto();
        }

        public UUID id() { return id; }
        public UUID transcricaoId() { return transcricaoId; }
        public String promotorId() { return promotorId; }
        public String midia() { return midia; }
        public String processo() { return processo; }
        public List<Mensagem> mensagens() { return List.copyOf(mensagens); }

        private void perguntar(String pergunta) {
            mensagens.add(new Mensagem("usuario", pergunta, Instant.now()));
            String resposta = "Resposta de demonstração: a pergunta foi recebida com a transcrição de "
                    + midia + " como contexto. A integração real com IA ainda não está configurada.";
            if (contexto == null || contexto.isBlank()) {
                resposta = "A transcrição não está disponível como contexto.";
            }
            mensagens.add(new Mensagem("ia", resposta, Instant.now()));
        }
    }

    private final Map<UUID, Conversa> conversas = new ConcurrentHashMap<>();

    public Conversa criar(Transcricao transcricao) {
        Conversa conversa = new Conversa(transcricao);
        conversas.put(conversa.id(), conversa);
        return conversa;
    }

    public Optional<Conversa> buscar(UUID id, String promotorId) {
        Conversa conversa = conversas.get(id);
        return conversa != null && conversa.promotorId().equals(promotorId)
                ? Optional.of(conversa)
                : Optional.empty();
    }

    public void perguntar(Conversa conversa, String pergunta) {
        conversa.perguntar(pergunta);
    }

    public void encerrar(UUID id) {
        conversas.remove(id);
    }

    public void encerrarDoPromotor(String promotorId) {
        conversas.entrySet().removeIf(entrada -> entrada.getValue().promotorId().equals(promotorId));
    }

    public int quantidade() {
        return conversas.size();
    }
}
