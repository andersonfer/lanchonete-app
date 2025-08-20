package br.com.lanchonete.auth;

import br.com.lanchonete.auth.adapters.jwt.JwtService;
import br.com.lanchonete.auth.adapters.mock.ClienteMockGateway;
import br.com.lanchonete.auth.application.gateways.ClienteGateway;
import br.com.lanchonete.auth.application.services.AuthService;
import br.com.lanchonete.auth.application.usecases.IdentificarCliente;
import br.com.lanchonete.auth.dto.AuthCpfRequest;
import br.com.lanchonete.auth.dto.AuthCpfResponse;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

public class AuthCpfHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    
    private final AuthService authService;
    private final ObjectMapper objectMapper;
    
    public AuthCpfHandler() {
        // Configurar dependências (DI manual para Lambda)
        ClienteGateway clienteGateway = new ClienteMockGateway();
        IdentificarCliente identificarCliente = new IdentificarCliente(clienteGateway);
        JwtService jwtService = new JwtService();
        this.authService = new AuthService(identificarCliente, jwtService);
        this.objectMapper = new ObjectMapper();
    }
    
    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        context.getLogger().log("Iniciando autenticação CPF - Request: " + input.getBody());
        
        try {
            // 1. Parse da requisição
            AuthCpfRequest request = parseRequest(input.getBody());
            
            // 2. Delegar para Service
            AuthCpfResponse response = authService.autenticar(request);
            
            // 3. Converter para resposta HTTP
            int statusCode = response.isSuccess() ? 200 : 400;
            return createResponse(statusCode, response);
            
        } catch (Exception e) {
            context.getLogger().log("Erro inesperado: " + e.getMessage());
            AuthCpfResponse errorResponse = new AuthCpfResponse(false, "Erro interno do servidor");
            return createResponse(500, errorResponse);
        }
    }
    
    private AuthCpfRequest parseRequest(String body) throws Exception {
        if (body == null || body.trim().isEmpty()) {
            return new AuthCpfRequest(); // Request vazio (sem campo cpf)
        }
        
        // Parse do JSON para AuthCpfRequest
        return objectMapper.readValue(body, AuthCpfRequest.class);
    }
    
    private APIGatewayProxyResponseEvent createResponse(int statusCode, Object body) {
        try {
            APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
            response.setStatusCode(statusCode);
            response.setBody(objectMapper.writeValueAsString(body));
            
            // Headers CORS
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json");
            headers.put("Access-Control-Allow-Origin", "*");
            headers.put("Access-Control-Allow-Methods", "POST, OPTIONS");
            headers.put("Access-Control-Allow-Headers", "Content-Type");
            response.setHeaders(headers);
            
            return response;
            
        } catch (Exception e) {
            // Fallback em caso de erro de serialização
            APIGatewayProxyResponseEvent errorResponse = new APIGatewayProxyResponseEvent();
            errorResponse.setStatusCode(500);
            errorResponse.setBody("{\"success\": false, \"error\": \"Erro interno do servidor\"}");
            return errorResponse;
        }
    }
}