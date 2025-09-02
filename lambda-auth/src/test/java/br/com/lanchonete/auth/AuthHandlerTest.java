package br.com.lanchonete.auth;

import br.com.lanchonete.auth.model.AuthRequest;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthHandlerTest {

    @Mock
    private Context context;

    private AuthHandler authHandler;
    private ObjectMapper objectMapper;

    @BeforeEach
    void configurar() {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();
        
        // Configurar ambiente de teste
        System.setProperty("DATABASE_URL", "jdbc:mysql://localhost:3306/test");
        System.setProperty("DB_USERNAME", "test");
        System.setProperty("DB_PASSWORD", "test");
        System.setProperty("JWT_SECRET", "teste-secreto-super-forte-para-jwt-256-bits-minimo");
    }

    @Test
    @DisplayName("Deve retornar erro 500 para JSON inválido")
    void t1() {
        APIGatewayProxyRequestEvent input = new APIGatewayProxyRequestEvent();
        input.setBody("{json_invalido}");

        // Não inicializar AuthHandler para evitar conexão com banco
        authHandler = new AuthHandler();

        APIGatewayProxyResponseEvent response = authHandler.handleRequest(input, context);

        assertEquals(500, response.getStatusCode(), "Status code deve ser 500");
        assertTrue(response.getBody().contains("Erro interno"), "Body deve conter mensagem de erro interno");
    }

    @Test
    @DisplayName("Deve incluir headers CORS na resposta de erro")
    void t2() {
        APIGatewayProxyRequestEvent input = new APIGatewayProxyRequestEvent();
        input.setBody("{json_invalido}");

        authHandler = new AuthHandler();
        APIGatewayProxyResponseEvent response = authHandler.handleRequest(input, context);

        assertEquals("*", response.getHeaders().get("Access-Control-Allow-Origin"),
                "Access-Control-Allow-Origin deve estar presente");
        assertEquals("POST, OPTIONS", response.getHeaders().get("Access-Control-Allow-Methods"),
                "Access-Control-Allow-Methods deve estar presente");
        assertEquals("Content-Type, Authorization", response.getHeaders().get("Access-Control-Allow-Headers"),
                "Access-Control-Allow-Headers deve estar presente");
    }

    @Test
    @DisplayName("Deve deserializar AuthRequest corretamente")
    void t3() throws Exception {
        AuthRequest request = new AuthRequest("12345678901", "cliente");
        String json = objectMapper.writeValueAsString(request);

        AuthRequest deserializado = objectMapper.readValue(json, AuthRequest.class);

        assertEquals("12345678901", deserializado.getCpf(), "CPF deve estar correto");
        assertEquals("cliente", deserializado.getTipoAuth(), "Tipo auth deve estar correto");
        assertTrue(deserializado.isAuthCliente(), "Deve identificar como cliente");
        assertFalse(deserializado.isAuthAnonimo(), "Não deve identificar como anônimo");
    }

    @Test
    @DisplayName("Deve deserializar AuthRequest anônimo corretamente")
    void t4() throws Exception {
        AuthRequest request = new AuthRequest(null, "anonimo");
        String json = objectMapper.writeValueAsString(request);

        AuthRequest deserializado = objectMapper.readValue(json, AuthRequest.class);

        assertNull(deserializado.getCpf(), "CPF deve ser null");
        assertEquals("anonimo", deserializado.getTipoAuth(), "Tipo auth deve estar correto");
        assertFalse(deserializado.isAuthCliente(), "Não deve identificar como cliente");
        assertTrue(deserializado.isAuthAnonimo(), "Deve identificar como anônimo");
    }
}