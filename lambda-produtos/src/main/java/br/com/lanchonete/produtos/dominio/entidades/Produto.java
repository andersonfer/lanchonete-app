package br.com.lanchonete.produtos.dominio.entidades;

import br.com.lanchonete.produtos.dominio.enums.CategoriaProduto;

import java.math.BigDecimal;
import java.util.Objects;

public class Produto {
    private Long id;
    private String nome;
    private String descricao;
    private Preco preco;
    private CategoriaProduto categoria;

    private Produto() {}

    // Construtor para criação de negócio (novos produtos)
    private Produto(String nome, String descricao, BigDecimal preco, CategoriaProduto categoria) {
        definirNome(nome);
        definirDescricao(descricao);
        definirPreco(new Preco(preco));
        definirCategoria(categoria);
    }

    // Construtor para reconstituição (dados já validados do banco)
    private Produto(Long id, String nome, String descricao, Preco preco, CategoriaProduto categoria) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.preco = preco;
        this.categoria = categoria;
    }

    public static Produto criar(String nome, String descricao, BigDecimal preco, CategoriaProduto categoria) {
        return new Produto(nome, descricao, preco, categoria);
    }

    public static Produto reconstituir(Long id, String nome, String descricao, BigDecimal preco, CategoriaProduto categoria) {
        return new Produto(id, nome, descricao, new Preco(preco), categoria);
    }

    public void definirNome(String nome) {
        if (nome == null || nome.isBlank())
            throw new IllegalArgumentException("Nome do produto é obrigatório");
        this.nome = nome;
    }

    public void definirPreco(Preco preco) {
        if (preco == null)
            throw new IllegalArgumentException("Preço do produto é obrigatório");
        this.preco = preco;
    }

    public void definirCategoria(CategoriaProduto categoria) {
        if (categoria == null)
            throw new IllegalArgumentException("Categoria do produto é obrigatória");
        this.categoria = categoria;
    }

    public void definirDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Long getId() {
        return id;
    }

    public void definirId(Long id) {
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

    public CategoriaProduto getCategoria() {
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