package br.com.lanchonete.auth.util;

import br.com.lanchonete.auth.service.JwtService;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JwtAuthorizerHandler implements RequestHandler<Map<String, Object>, Map<String, Object>> {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthorizerHandler.class);
    
    private final JwtService jwtService;
    private final ObjectMapper objectMapper;

    public JwtAuthorizerHandler() {
        String segredoJwt = System.getenv("JWT_SECRET");
        this.jwtService = new JwtService(segredoJwt);
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> entrada, Context contexto) {
        try {
            logger.info("Processando autorizacao JWT - RequestId: {}", contexto.getAwsRequestId());
            
            String tokenAutorizacao = (String) entrada.get("authorizationToken");
            String arnMetodo = (String) entrada.get("methodArn");

            if (tokenAutorizacao == null || !tokenAutorizacao.startsWith("Bearer ")) {
                logger.warn("Token de autorizacao ausente ou formato invalido");
                throw new RuntimeException("Unauthorized");
            }

            String token = tokenAutorizacao.substring(7);

            if (!jwtService.validarToken(token)) {
                logger.warn("Token JWT invalido ou expirado");
                throw new RuntimeException("Unauthorized");
            }

            DecodedJWT jwtDecodificado = jwtService.decodificarToken(token);
            String tipo = jwtDecodificado.getClaim("type").asString();

            Map<String, Object> dadosContexto = new HashMap<>();
            dadosContexto.put("authType", tipo);

            if ("cliente".equals(tipo)) {
                dadosContexto.put("clienteId", jwtDecodificado.getSubject());
                dadosContexto.put("cpf", jwtDecodificado.getClaim("cpf").asString());
                dadosContexto.put("nome", jwtDecodificado.getClaim("nome").asString());
                dadosContexto.put("email", jwtDecodificado.getClaim("email").asString());
                
                logger.info("Autorizacao cliente aprovada - ClienteId: {}", jwtDecodificado.getSubject());
            } else if ("anonimo".equals(tipo)) {
                dadosContexto.put("sessionId", jwtDecodificado.getClaim("sessionId").asString());
                
                logger.info("Autorizacao anonima aprovada - SessionId: {}", 
                           jwtDecodificado.getClaim("sessionId").asString());
            }

            return criarPoliticaPermissao(arnMetodo, dadosContexto);

        } catch (Exception e) {
            logger.error("Erro na autorizacao - RequestId: {}, Erro: {}", 
                        contexto.getAwsRequestId(), e.getMessage());
            throw new RuntimeException("Unauthorized");
        }
    }

    private Map<String, Object> criarPoliticaPermissao(String arnMetodo, Map<String, Object> dadosContexto) {
        Map<String, Object> politica = new HashMap<>();
        politica.put("principalId", "user");

        Map<String, Object> documentoPolitica = new HashMap<>();
        documentoPolitica.put("Version", "2012-10-17");

        Map<String, Object> declaracao = new HashMap<>();
        declaracao.put("Action", "execute-api:Invoke");
        declaracao.put("Effect", "Allow");
        declaracao.put("Resource", obterPadraoRecurso(arnMetodo));

        documentoPolitica.put("Statement", List.of(declaracao));
        politica.put("policyDocument", documentoPolitica);
        politica.put("context", dadosContexto);

        logger.debug("Politica IAM criada com contexto: {}", dadosContexto.keySet());

        return politica;
    }

    private String obterPadraoRecurso(String arnMetodo) {
        String[] partesArn = arnMetodo.split(":");
        if (partesArn.length >= 6) {
            String[] partesRecurso = partesArn[5].split("/");
            if (partesRecurso.length >= 3) {
                return String.format("%s:%s:%s:%s:%s:%s/*/*", 
                    partesArn[0], partesArn[1], partesArn[2], partesArn[3], partesArn[4],
                    partesRecurso[0] + "/" + partesRecurso[1]);
            }
        }
        return arnMetodo;
    }
}