package br.grupo1.transcricao.sessao;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Sessões ativas em memória. O token é opaco, com 256 bits de um gerador criptográfico,
 * e é a única coisa que o promotor apresenta nas rotas de dados.
 */
@Component
public class SessaoStore {

    private final Map<String, Sessao> sessoes = new ConcurrentHashMap<>();
    private final SecureRandom aleatorio = new SecureRandom();

    public String criar(Sessao sessao) {
        byte[] bytes = new byte[32];
        aleatorio.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        sessoes.put(token, sessao);
        return token;
    }

    public Optional<Sessao> buscar(String token) {
        return Optional.ofNullable(sessoes.get(token));
    }

    public void encerrar(String token) {
        sessoes.remove(token);
    }

    public int ativas() {
        return sessoes.size();
    }
}
