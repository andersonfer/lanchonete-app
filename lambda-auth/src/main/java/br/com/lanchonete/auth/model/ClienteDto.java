package br.com.lanchonete.auth.model;

public record ClienteDto(
    Long id,
    String nome, 
    String email,
    String cpf
) {
}