package br.com.lanchonete.autoatendimento.entidades.cliente;

import br.com.lanchonete.autoatendimento.frameworks.util.Utils;
import br.com.lanchonete.autoatendimento.entidades.shared.Cpf;
import br.com.lanchonete.autoatendimento.entidades.shared.Email;
import lombok.Data;

@Data
public class Cliente {
    private Long id;
    private String nome;
    private Cpf cpf;
    private Email email;

    private Cliente() {}

    private Cliente(String nome, String email, Cpf cpf) {
        setNome(nome);
        setEmail(new Email(email));
        setCpf(cpf);
    }

    public static Cliente criar(String nome, String email, String cpf) {
        return new Cliente(nome, email, new Cpf(cpf));
    }

    public static Cliente criarSemValidacao(Long id, String nome, String email, String cpf) {
        Cliente cliente = new Cliente();
        cliente.setId(id);
        cliente.setNome(nome);
        cliente.setEmail(new Email(email));
        cliente.setCpf(new Cpf(cpf));
        return cliente;
    }

    public void setNome(String nome) {
        if (Utils.isNuloOuVazio(nome))
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
}
