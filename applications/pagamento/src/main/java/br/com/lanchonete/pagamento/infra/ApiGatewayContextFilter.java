package br.com.lanchonete.pagamento.infra;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class ApiGatewayContextFilter extends OncePerRequestFilter {
    
    private static final Logger logger = LoggerFactory.getLogger(ApiGatewayContextFilter.class);
    
    private static final String HEADER_CLIENTE_ID = "X-Cliente-ID";
    private static final String HEADER_CPF = "X-CPF";
    private static final String HEADER_NOME = "X-Nome";
    private static final String HEADER_AUTH_TYPE = "X-Auth-Type";
    private static final String HEADER_SESSION_ID = "X-Session-ID";
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        logger.debug("Processando context injection do API Gateway");
        
        // Extrair headers injetados pelo API Gateway JWT Authorizer
        String clienteId = request.getHeader(HEADER_CLIENTE_ID);
        String cpf = request.getHeader(HEADER_CPF);
        String nome = request.getHeader(HEADER_NOME);
        String authType = request.getHeader(HEADER_AUTH_TYPE);
        String sessionId = request.getHeader(HEADER_SESSION_ID);
        
        logger.debug("Headers recebidos - AuthType: {}, ClienteId: {}, CPF: {}, SessionId: {}", 
                    authType, clienteId, cpf, sessionId);
        
        // Criar Authentication baseado no tipo
        Authentication authentication = null;
        
        if ("cliente".equals(authType) && clienteId != null && !clienteId.trim().isEmpty()) {
            // Cliente autenticado
            ClienteAuthenticationToken clienteAuth = new ClienteAuthenticationToken(
                Long.valueOf(clienteId.trim()),
                cpf,
                nome,
                List.of(new SimpleGrantedAuthority("ROLE_CLIENTE"))
            );
            authentication = clienteAuth;
            logger.debug("Cliente autenticado: ID={}, Nome={}", clienteId, nome);
            
        } else if ("anonimo".equals(authType) && sessionId != null && !sessionId.trim().isEmpty()) {
            // Usuario anonimo
            AnonymousAuthenticationToken anonAuth = new AnonymousAuthenticationToken(
                sessionId.trim(),
                "anonymous",
                List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))
            );
            authentication = anonAuth;
            logger.debug("Usuario anonimo autenticado: SessionId={}", sessionId);
            
        } else {
            logger.warn("Headers de autenticacao invalidos ou ausentes - AuthType: {}, ClienteId: {}, SessionId: {}", 
                       authType, clienteId, sessionId);
        }
        
        // Definir authentication no contexto
        if (authentication != null) {
            SecurityContextHolder.getContext().setAuthentication(authentication);
            logger.debug("Authentication definido no SecurityContext: {}", authentication.getClass().getSimpleName());
        }
        
        // Continuar cadeia de filtros
        filterChain.doFilter(request, response);
    }
    
    // Classes de Authentication customizadas
    
    public static class ClienteAuthenticationToken extends UsernamePasswordAuthenticationToken {
        private final Long clienteId;
        private final String cpf;
        private final String nome;
        
        public ClienteAuthenticationToken(Long clienteId, String cpf, String nome, 
                                        List<SimpleGrantedAuthority> authorities) {
            super(clienteId.toString(), null, authorities);
            this.clienteId = clienteId;
            this.cpf = cpf;
            this.nome = nome;
            setAuthenticated(true);
        }
        
        public Long getClienteId() { return clienteId; }
        public String getCpf() { return cpf; }
        public String getNome() { return nome; }
    }
    
    public static class AnonymousAuthenticationToken extends org.springframework.security.authentication.AnonymousAuthenticationToken {
        private final String sessionId;
        
        public AnonymousAuthenticationToken(String sessionId, String principal, 
                                          List<SimpleGrantedAuthority> authorities) {
            super(sessionId, principal, authorities);
            this.sessionId = sessionId;
        }
        
        public String getSessionId() { return sessionId; }
    }
}