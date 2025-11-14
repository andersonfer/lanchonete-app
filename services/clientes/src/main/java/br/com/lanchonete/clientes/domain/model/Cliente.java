package br.com.lanchonete.clientes.domain.model;

import java.util.Objects;

public class Cliente {
    private final Long id;
    private final String nome;
    private final Cpf cpf;
    private final Email email;

    private Cliente(String nome, Email email, Cpf cpf) {
        if (nome == null || nome.isBlank())
            throw new IllegalArgumentException("Nome é obrigatório");
        if (email == null)
            throw new IllegalArgumentException("Email é obrigatório");
        if (cpf == null)
            throw new IllegalArgumentException("CPF é obrigatório");

        this.id = null;
        this.nome = nome;
        this.email = email;
        this.cpf = cpf;
    }

    private Cliente(Long id, String nome, Email email, Cpf cpf) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.cpf = cpf;
    }

    public static Cliente criar(String nome, String email, String cpf) {
        return new Cliente(nome, new Email(email), new Cpf(cpf));
    }

    public static Cliente reconstituir(Long id, String nome, String email, String cpf) {
        return new Cliente(id, nome, new Email(email), new Cpf(cpf));
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public Cpf getCpf() {
        return cpf;
    }

    public Email getEmail() {
        return email;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Cliente cliente = (Cliente) obj;
        return Objects.equals(id, cliente.id) &&
                Objects.equals(nome, cliente.nome) &&
                Objects.equals(cpf, cliente.cpf) &&
                Objects.equals(email, cliente.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nome, cpf, email);
    }

    @Override
    public String toString() {
        return "Cliente{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", cpf=" + cpf +
                ", email=" + email +
                '}';
    }
}
