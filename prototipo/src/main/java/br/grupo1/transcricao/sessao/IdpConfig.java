package br.grupo1.transcricao.sessao;

import br.grupo1.transcricao.demo.ChavesIdpFake;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.time.Clock;
import java.util.Base64;

/** Chave pública do provedor de identidade usada para conferir a assinatura dos tokens. */
@Configuration
public class IdpConfig {

    @Bean
    public Clock relogio() {
        return Clock.systemUTC();
    }

    @Bean
    public RSAPublicKey chavePublicaIdp(@Value("${idp.chave-publica:}") String chaveBase64,
                                        ObjectProvider<ChavesIdpFake> chavesFake)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        if (!chaveBase64.isBlank()) {
            X509EncodedKeySpec spec = new X509EncodedKeySpec(Base64.getDecoder().decode(chaveBase64));
            return (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(spec);
        }
        ChavesIdpFake fake = chavesFake.getIfAvailable();
        if (fake == null) {
            throw new IllegalStateException(
                    "Configure idp.chave-publica com a chave do provedor de identidade ou ative idp.fake=true para a demonstração");
        }
        return fake.publica();
    }
}
