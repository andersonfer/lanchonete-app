package br.com.lanchonete.auth.dto;

import br.com.lanchonete.auth.domain.entities.Cliente;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ClienteResponse {
    @JsonProperty("id")
    private Long id;
    
    @JsonProperty("cpf")
    private String cpf;
    
    @JsonProperty("nome")
    private String nome;
    
    @JsonProperty("email")
    private String email;

    public ClienteResponse() {}

    public ClienteResponse(Long id, String cpf, String nome, String email) {
        this.id = id;
        this.cpf = cpf;
        this.nome = nome;
        this.email = email;
    }

    public static ClienteResponse from(Cliente cliente) {
        return new ClienteResponse(
            cliente.getId(),
            cliente.getCpf() != null ? cliente.getCpf().getValor() : null,
            cliente.getNome(),
            cliente.getEmail() != null ? cliente.getEmail().getValor() : null
        );
    }

    // Getters e setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}