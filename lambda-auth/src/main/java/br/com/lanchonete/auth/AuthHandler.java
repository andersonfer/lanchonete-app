package br.com.lanchonete.auth;

import br.com.lanchonete.auth.casosdeuso.AutenticarCliente;
import br.com.lanchonete.auth.gateway.ClienteGateway;
import br.com.lanchonete.auth.infra.ClienteGatewayJdbc;
import br.com.lanchonete.auth.model.*;
import br.com.lanchonete.auth.service.JwtService;
import br.com.lanchonete.auth.util.CpfValidator;
import br.com.lanchonete.auth.exception.*;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AuthHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    
    private final ObjectMapper objectMapper;
    private final AutenticarCliente autenticarCliente;
    private final JwtService jwtService;
    
    public AuthHandler() {
        this.objectMapper = new ObjectMapper();
        
        String databaseUrl = System.getenv("DATABASE_URL");
        String dbUsername = System.getenv("DB_USERNAME");
        String dbPassword = System.getenv("DB_PASSWORD");
        String jwtSecret = System.getenv("JWT_SECRET");
        
        ClienteGateway clienteGateway = new ClienteGatewayJdbc(databaseUrl, dbUsername, dbPassword);
        CpfValidator cpfValidator = new CpfValidator();
        
        this.autenticarCliente = new AutenticarCliente(clienteGateway, cpfValidator);
        this.jwtService = new JwtService(jwtSecret);
    }
    
    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        try {
            AuthRequest request = objectMapper.readValue(input.getBody(), AuthRequest.class);
            AuthResponse response;
            
            if (request.isAuthCliente()) {
                Cliente cliente = autenticarCliente.executar(request.getCpf());
                ClienteDto clienteDto = converterParaDto(cliente);
                String token = jwtService.gerarTokenCliente(cliente);
                response = AuthResponse.criarRespostaCliente(token, clienteDto, 3600L);
            } else {
                String sessionId = UUID.randomUUID().toString();
                String token = jwtService.gerarTokenAnonimo(sessionId);
                response = AuthResponse.criarRespostaAnonima(token, sessionId, 3600L);
            }
            
            return criarRespostaSucesso(response);
            
        } catch (CpfInvalidoException e) {
            return criarRespostaErro(400, "CPF inválido", e.getMessage());
        } catch (ClienteNaoEncontradoException e) {
            return criarRespostaErro(404, "Cliente não encontrado", e.getMessage());
        } catch (Exception e) {
            return criarRespostaErro(500, "Erro interno", "Verifique os logs");
        }
    }
    
    private ClienteDto converterParaDto(Cliente cliente) {
        return new ClienteDto(
            cliente.getId(),
            cliente.getNome(),
            cliente.getEmail(),
            cliente.getCpf()
        );
    }
    
    private APIGatewayProxyResponseEvent criarRespostaSucesso(AuthResponse response) {
        try {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(200)
                    .withHeaders(criarHeadersCors())
                    .withBody(objectMapper.writeValueAsString(response));
        } catch (Exception e) {
            return criarRespostaErro(500, "Erro na serialização", e.getMessage());
        }
    }
    
    private APIGatewayProxyResponseEvent criarRespostaErro(int statusCode, String erro, String mensagem) {
        try {
            Map<String, String> body = new HashMap<>();
            body.put("erro", erro);
            body.put("mensagem", mensagem);
            
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(statusCode)
                    .withHeaders(criarHeadersCors())
                    .withBody(objectMapper.writeValueAsString(body));
        } catch (Exception e) {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(500)
                    .withHeaders(criarHeadersCors())
                    .withBody("{\"erro\":\"Erro interno\",\"mensagem\":\"Falha na serialização\"}");
        }
    }
    
    private Map<String, String> criarHeadersCors() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Access-Control-Allow-Origin", "*");
        headers.put("Access-Control-Allow-Methods", "POST, OPTIONS");
        headers.put("Access-Control-Allow-Headers", "Content-Type, Authorization");
        return headers;
    }
}