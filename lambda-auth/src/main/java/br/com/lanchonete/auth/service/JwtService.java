package br.com.lanchonete.auth.service;

import br.com.lanchonete.auth.model.Cliente;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import java.time.Instant;
import java.util.Date;

public class JwtService {
    
    private final String jwtSecret;
    private static final String ISSUER = "lanchonete-auth";
    private static final int EXPIRATION_SECONDS = 3600;
    
    public JwtService(String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }
    
    public String gerarTokenCliente(Cliente cliente) {
        return JWT.create()
                .withIssuer(ISSUER)
                .withSubject(cliente.getId().toString())
                .withClaim("cpf", cliente.getCpf())
                .withClaim("nome", cliente.getNome())
                .withClaim("email", cliente.getEmail())
                .withClaim("type", "cliente")
                .withIssuedAt(Date.from(Instant.now()))
                .withExpiresAt(Date.from(Instant.now().plusSeconds(EXPIRATION_SECONDS)))
                .sign(Algorithm.HMAC256(jwtSecret));
    }
    
    public String gerarTokenAnonimo(String sessionId) {
        return JWT.create()
                .withIssuer(ISSUER)
                .withSubject(sessionId)
                .withClaim("sessionId", sessionId)
                .withClaim("type", "anonimo")
                .withIssuedAt(Date.from(Instant.now()))
                .withExpiresAt(Date.from(Instant.now().plusSeconds(EXPIRATION_SECONDS)))
                .sign(Algorithm.HMAC256(jwtSecret));
    }
    
    public boolean validarToken(String token) {
        try {
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(jwtSecret))
                    .withIssuer(ISSUER)
                    .build();
            verifier.verify(token);
            return true;
        } catch (JWTVerificationException e) {
            return false;
        }
    }
    
    public DecodedJWT decodificarToken(String token) {
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(jwtSecret))
                .withIssuer(ISSUER)
                .build();
        return verifier.verify(token);
    }
}