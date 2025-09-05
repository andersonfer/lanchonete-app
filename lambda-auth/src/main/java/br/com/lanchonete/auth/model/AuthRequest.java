package br.com.lanchonete.auth.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AuthRequest {

    @JsonProperty("cpf")
    private String cpf;

    @JsonProperty("authType")
    private String tipoAuth;

    public AuthRequest() {
    }

    public AuthRequest(String cpf, String tipoAuth) {
        this.cpf = cpf;
        this.tipoAuth = tipoAuth;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getTipoAuth() {
        return tipoAuth;
    }

    public void setTipoAuth(String tipoAuth) {
        this.tipoAuth = tipoAuth;
    }

    @com.fasterxml.jackson.annotation.JsonIgnore
    public boolean isAuthCliente() {
        return "cliente".equals(tipoAuth);
    }

    @com.fasterxml.jackson.annotation.JsonIgnore
    public boolean isAuthAnonimo() {
        return "anonimo".equals(tipoAuth);
    }

    @Override
    public String toString() {
        return "AuthRequest{" +
                "cpf='" + cpf + '\'' +
                ", tipoAuth='" + tipoAuth + '\'' +
                '}';
    }
}