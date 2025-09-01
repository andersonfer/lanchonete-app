package br.com.lanchonete.auth.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AuthResponse {

    @JsonProperty("token")
    private String token;

    @JsonProperty("authType")
    private String tipoAuth;

    @JsonProperty("cliente")
    private ClienteDto cliente;

    @JsonProperty("sessionId")
    private String sessionId;

    @JsonProperty("expiresIn")
    private Long expiresIn;

    public AuthResponse() {
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTipoAuth() {
        return tipoAuth;
    }

    public void setTipoAuth(String tipoAuth) {
        this.tipoAuth = tipoAuth;
    }

    public ClienteDto getCliente() {
        return cliente;
    }

    public void setCliente(ClienteDto cliente) {
        this.cliente = cliente;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public static AuthResponse criarRespostaCliente(String token, ClienteDto cliente, Long expiresIn) {
        AuthResponse response = new AuthResponse();
        response.setToken(token);
        response.setTipoAuth("cliente");
        response.setCliente(cliente);
        response.setExpiresIn(expiresIn);
        return response;
    }

    public static AuthResponse criarRespostaAnonima(String token, String sessionId, Long expiresIn) {
        AuthResponse response = new AuthResponse();
        response.setToken(token);
        response.setTipoAuth("anonimo");
        response.setSessionId(sessionId);
        response.setExpiresIn(expiresIn);
        return response;
    }
}