package br.grupo1.transcricao.demo;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

/**
 * Par de chaves do provedor de identidade simulado, gerado a cada início da aplicação.
 * Nada fica gravado em disco nem no repositório.
 */
@Component
@ConditionalOnProperty(name = "idp.fake", havingValue = "true")
public class ChavesIdpFake {

    private final KeyPair par;

    public ChavesIdpFake() throws NoSuchAlgorithmException {
        KeyPairGenerator gerador = KeyPairGenerator.getInstance("RSA");
        gerador.initialize(2048);
        this.par = gerador.generateKeyPair();
    }

    public RSAPublicKey publica() {
        return (RSAPublicKey) par.getPublic();
    }

    public RSAPrivateKey privada() {
        return (RSAPrivateKey) par.getPrivate();
    }
}
