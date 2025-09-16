package br.com.lanchonete.auth;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AuthHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final CognitoIdentityProviderClient cognitoClient;
    private final ObjectMapper objectMapper;
    private final String userPoolId;
    private final String clientId;

    public AuthHandler() {
        this.cognitoClient = CognitoIdentityProviderClient.builder()
                .region(Region.US_EAST_1)
                .build();
        this.objectMapper = new ObjectMapper();
        this.userPoolId = System.getenv("USER_POOL_ID");
        this.clientId = System.getenv("CLIENT_ID");
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        context.getLogger().log("Iniciando autenticação via CPF");

        try {
            // Parse do body da requisição
            IdentificacaoRequest request = objectMapper.readValue(input.getBody(), IdentificacaoRequest.class);
            
            if (request.getCpf() == null || request.getCpf().trim().isEmpty()) {
                // Cliente anônimo
                return criarTokenAnonimo(context);
            } else {
                // Cliente identificado via CPF
                return autenticarComCpf(request.getCpf(), context);
            }

        } catch (Exception e) {
            context.getLogger().log("Erro na autenticação: " + e.getMessage());
            return criarErroResponse(500, "Erro interno do servidor");
        }
    }

    private APIGatewayProxyResponseEvent autenticarComCpf(String cpf, Context context) {
        try {
            String cpfLimpo = limparCpf(cpf);
            context.getLogger().log("Autenticando CPF: " + cpfLimpo);

            // Tentar criar usuário (se não existir)
            try {
                criarUsuarioSeNaoExistir(cpfLimpo, context);
            } catch (Exception e) {
                context.getLogger().log("Usuário já existe ou erro na criação: " + e.getMessage());
            }

            // Fazer autenticação admin (sem senha)
            AdminInitiateAuthRequest authRequest = AdminInitiateAuthRequest.builder()
                    .userPoolId(userPoolId)
                    .clientId(clientId)
                    .authFlow(AuthFlowType.ADMIN_NO_SRP_AUTH)
                    .authParameters(Map.of(
                            "USERNAME", cpfLimpo,
                            "PASSWORD", "Temp1234" // Senha temporária fixa
                    ))
                    .build();

            AdminInitiateAuthResponse authResponse = cognitoClient.adminInitiateAuth(authRequest);

            // Verificar se precisa definir senha permanente
            if (authResponse.challengeName() == ChallengeNameType.NEW_PASSWORD_REQUIRED) {
                // Definir senha permanente automaticamente
                AdminRespondToAuthChallengeRequest challengeRequest = AdminRespondToAuthChallengeRequest.builder()
                        .userPoolId(userPoolId)
                        .clientId(clientId)
                        .challengeName(ChallengeNameType.NEW_PASSWORD_REQUIRED)
                        .session(authResponse.session())
                        .challengeResponses(Map.of(
                                "USERNAME", cpfLimpo,
                                "NEW_PASSWORD", "Lanchonete@2024"
                        ))
                        .build();

                AdminRespondToAuthChallengeResponse challengeResponse = cognitoClient.adminRespondToAuthChallenge(challengeRequest);
                authResponse = AdminInitiateAuthResponse.builder()
                        .authenticationResult(challengeResponse.authenticationResult())
                        .build();
            }

            // Retornar tokens
            AuthenticationResultType result = authResponse.authenticationResult();
            IdentificacaoResponse response = new IdentificacaoResponse(
                    result.idToken(), // API Gateway Cognito Authorizer precisa do ID Token
                    result.expiresIn(),
                    cpfLimpo,
                    "IDENTIFICADO"
            );

            return criarSucessoResponse(response);

        } catch (Exception e) {
            context.getLogger().log("Erro ao autenticar CPF: " + e.getMessage());
            return criarErroResponse(400, "Erro na autenticação");
        }
    }

    private APIGatewayProxyResponseEvent criarTokenAnonimo(Context context) {
        try {
            // Para anônimos, criar usuário temporário
            String userId = "anonimo_" + UUID.randomUUID().toString().substring(0, 8);
            
            criarUsuarioSeNaoExistir(userId, context);

            // Autenticar usuário anônimo
            AdminInitiateAuthRequest authRequest = AdminInitiateAuthRequest.builder()
                    .userPoolId(userPoolId)
                    .clientId(clientId)
                    .authFlow(AuthFlowType.ADMIN_NO_SRP_AUTH)
                    .authParameters(Map.of(
                            "USERNAME", userId,
                            "PASSWORD", "Temp1234"
                    ))
                    .build();

            AdminInitiateAuthResponse authResponse = cognitoClient.adminInitiateAuth(authRequest);

            // Verificar se precisa definir nova senha
            if (authResponse.challengeName() == ChallengeNameType.NEW_PASSWORD_REQUIRED) {
                // Definir senha permanente automaticamente
                AdminRespondToAuthChallengeRequest challengeRequest = AdminRespondToAuthChallengeRequest.builder()
                        .userPoolId(userPoolId)
                        .clientId(clientId)
                        .challengeName(ChallengeNameType.NEW_PASSWORD_REQUIRED)
                        .session(authResponse.session())
                        .challengeResponses(Map.of(
                                "USERNAME", userId,
                                "NEW_PASSWORD", "Lanchonete@2024"
                        ))
                        .build();

                AdminRespondToAuthChallengeResponse challengeResponse = cognitoClient.adminRespondToAuthChallenge(challengeRequest);
                authResponse = AdminInitiateAuthResponse.builder()
                        .authenticationResult(challengeResponse.authenticationResult())
                        .build();
            }

            AuthenticationResultType result = authResponse.authenticationResult();
            IdentificacaoResponse response = new IdentificacaoResponse(
                    result.idToken(), // API Gateway Cognito Authorizer precisa do ID Token
                    1800, // 30 minutos para anônimos
                    null,
                    "ANONIMO"
            );

            return criarSucessoResponse(response);

        } catch (Exception e) {
            context.getLogger().log("Erro ao criar token anônimo: " + e.getMessage());
            return criarErroResponse(500, "Erro ao criar sessão anônima");
        }
    }

    private void criarUsuarioSeNaoExistir(String username, Context context) {
        try {
            AdminCreateUserRequest createRequest = AdminCreateUserRequest.builder()
                    .userPoolId(userPoolId)
                    .username(username)
                    .temporaryPassword("Temp1234")
                    .messageAction(MessageActionType.SUPPRESS) // Não enviar email
                    .build();

            cognitoClient.adminCreateUser(createRequest);
            context.getLogger().log("Usuário criado: " + username);

        } catch (UsernameExistsException e) {
            context.getLogger().log("Usuário já existe: " + username);
        }
    }

    private String limparCpf(String cpf) {
        return cpf.replaceAll("[^0-9]", "");
    }

    private APIGatewayProxyResponseEvent criarSucessoResponse(Object body) {
        try {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(200)
                    .withHeaders(Map.of(
                            "Content-Type", "application/json",
                            "Access-Control-Allow-Origin", "*"
                    ))
                    .withBody(objectMapper.writeValueAsString(body));
        } catch (Exception e) {
            return criarErroResponse(500, "Erro ao serializar resposta");
        }
    }

    private APIGatewayProxyResponseEvent criarErroResponse(int statusCode, String message) {
        Map<String, String> error = Map.of("error", message);
        try {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(statusCode)
                    .withHeaders(Map.of(
                            "Content-Type", "application/json",
                            "Access-Control-Allow-Origin", "*"
                    ))
                    .withBody(objectMapper.writeValueAsString(error));
        } catch (Exception e) {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(500)
                    .withBody("{\"error\":\"Erro interno\"}");
        }
    }
}