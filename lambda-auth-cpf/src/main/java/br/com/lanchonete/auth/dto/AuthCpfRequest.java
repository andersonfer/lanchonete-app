package br.com.lanchonete.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AuthCpfRequest {
    @JsonProperty("cpf")
    private String cpf;
    private boolean cpfFieldPresent = false;
    
    public AuthCpfRequest() {}
    
    public String getCpf() { 
        return cpf; 
    }
    
    public void setCpf(String cpf) { 
        this.cpf = cpf;
        this.cpfFieldPresent = true;
    }
    
    public boolean hasCpfField() { 
        return cpfFieldPresent; 
    }
}