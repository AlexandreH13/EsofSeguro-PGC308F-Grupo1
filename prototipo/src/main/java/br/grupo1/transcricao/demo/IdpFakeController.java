package br.grupo1.transcricao.demo;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;

/**
 * Provedor de identidade simulado para a demonstração. Faz o papel do SSO institucional:
 * emite o token de identidade assinado, com ou sem o segundo fator no claim amr.
 * Fica fora de /api porque não é rota do serviço. Ativo só com idp.fake=true.
 */
@RestController
@RequestMapping("/idp-fake")
@ConditionalOnProperty(name = "idp.fake", havingValue = "true")
public class IdpFakeController {

    public record Promotor(String id, String nome) {}
    public record Pedido(String promotorId, boolean segundoFator) {}
    public record Token(String idToken) {}
    public record Erro(String erro) {}

    static final List<Promotor> PROMOTORES = List.of(
            new Promotor(DadosIniciais.PROMOTOR_A, "Ana Souza, 3ª Promotoria Criminal"),
            new Promotor(DadosIniciais.PROMOTOR_B, "Beatriz Ramos, 5ª Promotoria Criminal"));

    private final ChavesIdpFake chaves;
    private final String emissor;
    private final String audiencia;

    public IdpFakeController(ChavesIdpFake chaves,
                             @Value("${idp.emissor}") String emissor,
                             @Value("${idp.audiencia}") String audiencia) {
        this.chaves = chaves;
        this.emissor = emissor;
        this.audiencia = audiencia;
    }

    @GetMapping("/promotores")
    public List<Promotor> promotores() {
        return PROMOTORES;
    }

    @PostMapping("/token")
    public ResponseEntity<?> emitir(@RequestBody Pedido pedido) throws JOSEException {
        Promotor promotor = PROMOTORES.stream()
                .filter(p -> p.id().equals(pedido.promotorId()))
                .findFirst()
                .orElse(null);
        if (promotor == null) {
            return ResponseEntity.badRequest().body(new Erro("promotor desconhecido"));
        }
        Instant agora = Instant.now();
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .issuer(emissor)
                .audience(audiencia)
                .subject(promotor.id())
                .claim("name", promotor.nome())
                .claim("amr", pedido.segundoFator() ? List.of("pwd", "mfa") : List.of("pwd"))
                .issueTime(Date.from(agora))
                .expirationTime(Date.from(agora.plus(Duration.ofMinutes(5))))
                .build();
        SignedJWT jwt = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256).type(JOSEObjectType.JWT).build(), claims);
        jwt.sign(new RSASSASigner(chaves.privada()));
        return ResponseEntity.ok(new Token(jwt.serialize()));
    }
}
