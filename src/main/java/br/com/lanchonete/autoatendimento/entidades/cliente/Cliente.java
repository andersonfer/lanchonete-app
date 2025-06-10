package br.com.lanchonete.autoatendimento.entidades.cliente;

import br.com.lanchonete.autoatendimento.frameworks.util.Utils;
import br.com.lanchonete.autoatendimento.entidades.shared.Cpf;
import lombok.Data;

import java.util.regex.Pattern;

@Data
public class Cliente {
    private static final Pattern EMAIL_REGEX = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    private Long id;
    private String nome;
    private Cpf cpf;
    private String email;

    private Cliente() {}

    private Cliente(String nome, String email, Cpf cpf) {
        setNome(nome);
        setEmail(email);
        setCpf(cpf);
    }

    public static Cliente criar(String nome, String email, String cpf) {
        return new Cliente(nome, email, new Cpf(cpf));
    }

    public static Cliente criarSemValidacao(Long id, String nome, String email, String cpf) {
        Cliente cliente = new Cliente();
        cliente.setId(id);
        cliente.setNome(nome);
        cliente.setEmail(email);
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

    public void setEmail(String email) {
        if (Utils.isNuloOuVazio(email))
            throw new IllegalArgumentException("Email é obrigatório");
        if (!isEmailValido(email))
            throw new IllegalArgumentException("Email inválido");
        this.email = email;
    }


    private boolean isEmailValido(String email) {
        return EMAIL_REGEX.matcher(email).matches();
    }
}
