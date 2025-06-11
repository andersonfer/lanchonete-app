package br.com.lanchonete.autoatendimento.entidades.cliente;

import br.com.lanchonete.autoatendimento.entidades.shared.Cpf;
import br.com.lanchonete.autoatendimento.entidades.shared.Email;

import java.util.Objects;

public class Cliente {
    private Long id;
    private String nome;
    private Cpf cpf;
    private Email email;

    private Cliente() {}

    // Construtor para criação de negócio (novos clientes)
    private Cliente(String nome, Email email, Cpf cpf) {
        setNome(nome);
        setEmail(email);
        setCpf(cpf);
    }

    // Construtor para reconstituição (dados já validados do banco)
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

    public void setNome(String nome) {
        if (nome == null || nome.isBlank())
            throw new IllegalArgumentException("Nome é obrigatório");
        this.nome = nome;
    }

    public void setCpf(Cpf cpf) {
        if (cpf == null)
            throw new IllegalArgumentException("CPF é obrigatório");
        this.cpf = cpf;
    }

    public void setEmail(Email email) {
        if (email == null)
            throw new IllegalArgumentException("Email é obrigatório");
        this.email = email;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
