package br.com.lanchonete.autoatendimento.dominio.modelo;

import br.com.lanchonete.autoatendimento.adaptadores.util.Utils;
import lombok.Data;

import java.util.regex.Pattern;

@Data
public class Cliente {
    private static final Pattern EMAIL_REGEX = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final Pattern CPF_REGEX = Pattern.compile("^\\d{11}$");

    private Long id;
    private String nome;
    private String cpf;
    private String email;

    private Cliente() {}

    private Cliente(String nome, String email, String cpf) {
        setNome(nome);
        setEmail(email);
        setCpf(cpf);
    }

    public static Cliente criar(String nome, String email, String cpf) {
        return new Cliente(nome, email, cpf);
    }

    public static Cliente criarSemValidacao(Long id, String nome, String email, String cpf) {
        Cliente cliente = new Cliente();
        cliente.setId(id);
        cliente.setNome(nome);
        cliente.setEmail(email);
        cliente.setCpf(cpf);
        return cliente;
    }

    public void setNome(String nome) {
        if (Utils.isNuloOuVazio(nome))
            throw new IllegalArgumentException("Nome é obrigatório");
        this.nome = nome;
    }

    public void setCpf(String cpf) {
        if (Utils.isNuloOuVazio(cpf))
            throw new IllegalArgumentException("CPF é obrigatório");
        if (!isCpfValido(cpf))
            throw new IllegalArgumentException("CPF deve conter 11 dígitos numéricos");
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

    private boolean isCpfValido(String cpf) {
        return CPF_REGEX.matcher(cpf).matches();
    }
}
