package br.com.lanchonete.auth.service;

import br.com.lanchonete.auth.model.Cliente;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private Cliente clienteTeste;

    @BeforeEach
    void configurar() {
        String segredoTeste = "teste-secreto-super-forte-para-jwt-256-bits-minimo";
        jwtService = new JwtService(segredoTeste);
        clienteTeste = new Cliente(1L, "João Silva", "joao@email.com", "12345678901");
    }

    @Test
    @DisplayName("Deve gerar token JWT para cliente com dados corretos")
    void t1() {
        String token = jwtService.gerarTokenCliente(clienteTeste);

        assertNotNull(token, "Token não deveria ser nulo");
        assertTrue(token.startsWith("eyJ"), "Token deve ter formato JWT válido");
        
        DecodedJWT jwt = jwtService.decodificarToken(token);
        assertEquals("1", jwt.getSubject(), "Subject deve ser o ID do cliente");
        assertEquals("12345678901", jwt.getClaim("cpf").asString(), "CPF deve estar no token");
        assertEquals("João Silva", jwt.getClaim("nome").asString(), "Nome deve estar no token");
        assertEquals("joao@email.com", jwt.getClaim("email").asString(), "Email deve estar no token");
        assertEquals("cliente", jwt.getClaim("type").asString(), "Type deve ser cliente");
        assertEquals("lanchonete-auth", jwt.getIssuer(), "Issuer deve estar correto");
    }

    @Test
    @DisplayName("Deve gerar token JWT para usuário anônimo com session ID")
    void t2() {
        String sessionId = "session-123-teste";
        String token = jwtService.gerarTokenAnonimo(sessionId);

        assertNotNull(token, "Token não deveria ser nulo");
        assertTrue(token.startsWith("eyJ"), "Token deve ter formato JWT válido");
        
        DecodedJWT jwt = jwtService.decodificarToken(token);
        assertEquals(sessionId, jwt.getSubject(), "Subject deve ser o session ID");
        assertEquals(sessionId, jwt.getClaim("sessionId").asString(), "Session ID deve estar no token");
        assertEquals("anonimo", jwt.getClaim("type").asString(), "Type deve ser anonimo");
        assertEquals("lanchonete-auth", jwt.getIssuer(), "Issuer deve estar correto");
    }

    @Test
    @DisplayName("Deve validar token válido como true")
    void t3() {
        String token = jwtService.gerarTokenCliente(clienteTeste);
        
        boolean resultado = jwtService.validarToken(token);
        
        assertTrue(resultado, "Token válido deve retornar true");
    }

    @Test
    @DisplayName("Deve validar token inválido como false")
    void t4() {
        String tokenInvalido = "token.invalido.aqui";
        
        boolean resultado = jwtService.validarToken(tokenInvalido);
        
        assertFalse(resultado, "Token inválido deve retornar false");
    }

    @Test
    @DisplayName("Deve validar token vazio como false")
    void t5() {
        boolean resultado = jwtService.validarToken("");
        
        assertFalse(resultado, "Token vazio deve retornar false");
    }

    @Test
    @DisplayName("Deve validar token null como false")
    void t6() {
        boolean resultado = jwtService.validarToken(null);
        
        assertFalse(resultado, "Token null deve retornar false");
    }

    @Test
    @DisplayName("Deve decodificar token válido sem erros")
    void t7() {
        String token = jwtService.gerarTokenCliente(clienteTeste);
        
        DecodedJWT resultado = jwtService.decodificarToken(token);
        
        assertNotNull(resultado, "JWT decodificado não deveria ser nulo");
        assertEquals("1", resultado.getSubject(), "Subject deve ser o ID do cliente");
        assertEquals("lanchonete-auth", resultado.getIssuer(), "Issuer deve estar correto");
    }

    @Test
    @DisplayName("Deve lançar exceção ao decodificar token inválido")
    void t8() {
        String tokenInvalido = "token.completamente.invalido";
        
        assertThrows(Exception.class, 
                    () -> jwtService.decodificarToken(tokenInvalido),
                    "Deve lançar exceção para token inválido");
    }
}