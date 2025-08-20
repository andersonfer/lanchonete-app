package br.com.lanchonete.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AuthCpfResponse {
    @JsonProperty("success")
    private boolean success;
    
    @JsonProperty("cliente")
    private ClienteResponse cliente;
    
    @JsonProperty("token")
    private String token;
    
    @JsonProperty("expiresIn")
    private int expiresIn;
    
    @JsonProperty("error")
    private String error;

    public AuthCpfResponse() {}

    // Construtor para sucesso
    public AuthCpfResponse(boolean success, ClienteResponse cliente, String token, int expiresIn) {
        this.success = success;
        this.cliente = cliente;
        this.token = token;
        this.expiresIn = expiresIn;
    }

    // Construtor para erro
    public AuthCpfResponse(boolean success, String error) {
        this.success = success;
        this.error = error;
    }

    // Getters e setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public ClienteResponse getCliente() { return cliente; }
    public void setCliente(ClienteResponse cliente) { this.cliente = cliente; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public int getExpiresIn() { return expiresIn; }
    public void setExpiresIn(int expiresIn) { this.expiresIn = expiresIn; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
}