package br.com.lanchonete.auth.adapters.jwt;

import br.com.lanchonete.auth.domain.entities.Cliente;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JwtService {
    private static final String SECRET_KEY = "lanchonete-auth-secret-key-for-tech-challenge-fase-3-must-be-256-bits-long";
    private static final int EXPIRATION_TIME = 3600; // 1 hora em segundos
    
    private final SecretKey key;
    
    public JwtService() {
        this.key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }
    
    public String generateToken(Cliente cliente) {
        Map<String, Object> claims = new HashMap<>();
        
        // Claims para cliente autenticado
        if (!cliente.isAnonimo()) {
            claims.put("clienteId", cliente.getId());
            claims.put("cpf", cliente.getCpf().getValor());
            claims.put("nome", cliente.getNome());
            claims.put("email", cliente.getEmail().getValor());
            claims.put("tipo", "IDENTIFICADO");
        } else {
            // Claims para cliente anônimo
            claims.put("clienteId", null);
            claims.put("cpf", null);
            claims.put("nome", "Cliente Anônimo");
            claims.put("email", null);
            claims.put("tipo", "ANONIMO");
        }
        
        return createToken(claims, cliente.isAnonimo() ? "anonimo" : cliente.getCpf().getValor());
    }
    
    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + EXPIRATION_TIME * 1000);
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expirationDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
}