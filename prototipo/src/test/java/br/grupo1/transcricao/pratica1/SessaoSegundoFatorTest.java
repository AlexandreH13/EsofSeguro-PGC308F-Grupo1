package br.grupo1.transcricao.pratica1;

import br.grupo1.transcricao.sessao.SessaoStore;
import com.jayway.jsonpath.JsonPath;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prática 1, RS02. Testes TS01 a TS03 de docs/etapa4-codigo-seguro.md, escritos antes do controller.
 * O teste gera o par de chaves do provedor, entrega a pública ao Backend e assina os tokens com a privada.
 */
@SpringBootTest
@AutoConfigureMockMvc
class SessaoSegundoFatorTest {

    private static final KeyPair CHAVES_DO_PROVEDOR = gerarChaves();
    private static final KeyPair CHAVES_DE_TERCEIRO = gerarChaves();

    @DynamicPropertySource
    static void chavePublicaDoProvedor(DynamicPropertyRegistry registro) {
        registro.add("idp.chave-publica",
                () -> Base64.getEncoder().encodeToString(CHAVES_DO_PROVEDOR.getPublic().getEncoded()));
    }

    @Autowired
    private MockMvc http;
    @Autowired
    private SessaoStore sessoes;
    @Value("${idp.emissor}")
    private String emissor;
    @Value("${idp.audiencia}")
    private String audiencia;

    @Test
    void ts01_tokenValidoSemSegundoFatorNaoCriaSessao() throws Exception {
        int ativasAntes = sessoes.ativas();
        String token = token(CHAVES_DO_PROVEDOR, audiencia, daquiA(5), List.of("pwd"));

        http.perform(criarSessao(token))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.tokenSessao").doesNotExist())
                .andExpect(jsonPath("$.erro").value("erro de autenticação"));

        assertEquals(ativasAntes, sessoes.ativas(), "nenhuma sessão criada");
        http.perform(get("/api/transcricoes")).andExpect(status().isUnauthorized());
    }

    @Test
    void ts02_tokenComSegundoFatorCriaSessaoQueAbreAsRotasDeDados() throws Exception {
        String token = token(CHAVES_DO_PROVEDOR, audiencia, daquiA(5), List.of("pwd", "mfa"));

        String corpo = http.perform(criarSessao(token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.promotorId").value("promotor-a"))
                .andReturn().getResponse().getContentAsString();
        String tokenSessao = JsonPath.read(corpo, "$.tokenSessao");

        http.perform(get("/api/transcricoes").header("Authorization", "Bearer " + tokenSessao))
                .andExpect(status().isOk());
    }

    @Test
    void ts03_tokenForaDoContratoEhRecusadoMesmoComSegundoFator() throws Exception {
        String assinadoPorTerceiro = token(CHAVES_DE_TERCEIRO, audiencia, daquiA(5), List.of("pwd", "mfa"));
        String paraOutraAudiencia = token(CHAVES_DO_PROVEDOR, "outro-servico", daquiA(5), List.of("pwd", "mfa"));
        String expirado = token(CHAVES_DO_PROVEDOR, audiencia, daquiA(-1), List.of("pwd", "mfa"));

        for (String token : List.of(assinadoPorTerceiro, paraOutraAudiencia, expirado)) {
            http.perform(criarSessao(token))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.tokenSessao").doesNotExist());
        }
    }

    private static org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder criarSessao(String idToken) {
        return post("/api/sessao")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"idToken\":\"" + idToken + "\"}");
    }

    private String token(KeyPair chaves, String audiencia, Instant expiraEm, List<String> amr) throws Exception {
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .issuer(emissor)
                .audience(audiencia)
                .subject("promotor-a")
                .claim("name", "Ana Souza")
                .claim("amr", amr)
                .issueTime(Date.from(Instant.now()))
                .expirationTime(Date.from(expiraEm))
                .build();
        SignedJWT jwt = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256).type(JOSEObjectType.JWT).build(), claims);
        jwt.sign(new RSASSASigner(chaves.getPrivate()));
        return jwt.serialize();
    }

    private static Instant daquiA(int minutos) {
        return Instant.now().plus(Duration.ofMinutes(minutos));
    }

    private static KeyPair gerarChaves() {
        try {
            KeyPairGenerator gerador = KeyPairGenerator.getInstance("RSA");
            gerador.initialize(2048);
            return gerador.generateKeyPair();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
