package br.com.lanchonete.autoatendimento.dominio;

import br.com.lanchonete.autoatendimento.adaptadores.util.Utils;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class Produto {
    private Long id;
    private String nome;
    private String descricao;
    private BigDecimal preco;
    private Categoria categoria;

    private Produto() {}

    private Produto(String nome, String descricao, BigDecimal preco, Categoria categoria) {
        setNome(nome);
        setDescricao(descricao);
        setPreco(preco);
        setCategoria(categoria);
    }

    public static Produto criar(String nome, String descricao, BigDecimal preco, Categoria categoria) {
        return new Produto(nome, descricao, preco, categoria);
    }

    public static Produto criarSemValidacao(Long id, String nome, String descricao, BigDecimal preco, Categoria categoria) {
        Produto produto = new Produto();
        produto.setId(id);
        produto.nome = nome;
        produto.descricao = descricao;
        produto.preco = preco;
        produto.categoria = categoria;
        return produto;
    }

    public void setNome(String nome) {
        if (Utils.isNuloOuVazio(nome))
            throw new IllegalArgumentException("Nome do produto é obrigatório");
        this.nome = nome;
    }

    public void setPreco(BigDecimal preco) {
        if (preco == null)
            throw new IllegalArgumentException("Preço do produto é obrigatório");
        if (preco.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Preço deve ser maior que zero");
        this.preco = preco;
    }

    public void setCategoria(Categoria categoria) {
        if (categoria == null)
            throw new IllegalArgumentException("Categoria do produto é obrigatória");
        this.categoria = categoria;
    }
}