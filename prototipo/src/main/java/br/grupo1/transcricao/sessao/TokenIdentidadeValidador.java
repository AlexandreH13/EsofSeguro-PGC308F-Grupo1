package br.grupo1.transcricao.sessao;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.time.Clock;
import java.util.List;

/**
 * Prática 1 (RS02): valida o token de identidade emitido pelo provedor e só aceita
 * autenticação com segundo fator, pelo claim amr contendo mfa (RFC 8176).
 * Qualquer falha vira TokenRecusado com o motivo, que fica só no log.
 */
@Component
public class TokenIdentidadeValidador {

    public record Identidade(String promotorId, String nome) {}

    public static class TokenRecusado extends Exception {
        public TokenRecusado(String motivo) {
            super(motivo);
        }
    }

    private final RSAPublicKey chavePublica;
    private final String emissor;
    private final String audiencia;
    private final Clock relogio;

    public TokenIdentidadeValidador(RSAPublicKey chavePublicaIdp,
                                    @Value("${idp.emissor}") String emissor,
                                    @Value("${idp.audiencia}") String audiencia,
                                    Clock relogio) {
        this.chavePublica = chavePublicaIdp;
        this.emissor = emissor;
        this.audiencia = audiencia;
        this.relogio = relogio;
    }

    public Identidade validar(String token) throws TokenRecusado {
        if (token == null || token.isBlank()) {
            throw new TokenRecusado("token ausente");
        }
        SignedJWT jwt;
        JWTClaimsSet claims;
        try {
            jwt = SignedJWT.parse(token);
            if (!JWSAlgorithm.RS256.equals(jwt.getHeader().getAlgorithm())) {
                throw new TokenRecusado("algoritmo de assinatura inesperado");
            }
            if (!jwt.verify(new RSASSAVerifier(chavePublica))) {
                throw new TokenRecusado("assinatura inválida");
            }
            claims = jwt.getJWTClaimsSet();
        } catch (ParseException | JOSEException e) {
            throw new TokenRecusado("token malformado");
        }
        if (!emissor.equals(claims.getIssuer())) {
            throw new TokenRecusado("emissor inesperado");
        }
        if (claims.getAudience() == null || !claims.getAudience().contains(audiencia)) {
            throw new TokenRecusado("audiência inesperada");
        }
        if (claims.getExpirationTime() == null || !claims.getExpirationTime().toInstant().isAfter(relogio.instant())) {
            throw new TokenRecusado("token expirado");
        }
        if (claims.getSubject() == null || claims.getSubject().isBlank()) {
            throw new TokenRecusado("token sem sujeito");
        }
        List<String> metodos;
        try {
            metodos = claims.getStringListClaim("amr");
        } catch (ParseException e) {
            throw new TokenRecusado("claim amr malformado");
        }
        if (metodos == null || !metodos.contains("mfa")) {
            // RS02: assinatura, emissor, audiência e prazo válidos não bastam sem o segundo fator
            throw new TokenRecusado("autenticação sem segundo fator");
        }
        String nome;
        try {
            nome = claims.getStringClaim("name");
        } catch (ParseException e) {
            nome = null;
        }
        return new Identidade(claims.getSubject(), nome != null ? nome : claims.getSubject());
    }
}
