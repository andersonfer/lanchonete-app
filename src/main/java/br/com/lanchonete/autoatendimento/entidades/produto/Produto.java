package br.com.lanchonete.autoatendimento.entidades.produto;

import br.com.lanchonete.autoatendimento.frameworks.util.Utils;
import br.com.lanchonete.autoatendimento.entidades.shared.Preco;

import java.math.BigDecimal;
import java.util.Objects;

public class Produto {
    private Long id;
    private String nome;
    private String descricao;
    private Preco preco;
    private Categoria categoria;

    private Produto() {}

    private Produto(String nome, String descricao, BigDecimal preco, Categoria categoria) {
        setNome(nome);
        setDescricao(descricao);
        setPreco(new Preco(preco));
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
        produto.preco = new Preco(preco);
        produto.categoria = categoria;
        return produto;
    }

    public void setNome(String nome) {
        if (Utils.isNuloOuVazio(nome))
            throw new IllegalArgumentException("Nome do produto é obrigatório");
        this.nome = nome;
    }

    public void setPreco(Preco preco) {
        if (preco == null)
            throw new IllegalArgumentException("Preço do produto é obrigatório");
        this.preco = preco;
    }

    public void setCategoria(Categoria categoria) {
        if (categoria == null)
            throw new IllegalArgumentException("Categoria do produto é obrigatória");
        this.categoria = categoria;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
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

    public String getDescricao() {
        return descricao;
    }

    public Preco getPreco() {
        return preco;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Produto produto = (Produto) obj;
        return Objects.equals(id, produto.id) &&
                Objects.equals(nome, produto.nome) &&
                Objects.equals(descricao, produto.descricao) &&
                Objects.equals(preco, produto.preco) &&
                categoria == produto.categoria;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nome, descricao, preco, categoria);
    }

    @Override
    public String toString() {
        return "Produto{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", descricao='" + descricao + '\'' +
                ", preco=" + preco +
                ", categoria=" + categoria +
                '}';
    }
}