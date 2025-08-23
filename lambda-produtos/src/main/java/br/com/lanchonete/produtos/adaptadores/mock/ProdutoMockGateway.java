package br.com.lanchonete.produtos.adaptadores.mock;

import br.com.lanchonete.produtos.aplicacao.gateways.ProdutoGateway;
import br.com.lanchonete.produtos.dominio.entidades.Produto;
import br.com.lanchonete.produtos.dominio.enums.CategoriaProduto;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class ProdutoMockGateway implements ProdutoGateway {

    private static final AtomicLong ID_GENERATOR = new AtomicLong(1);
    private final Map<Long, Produto> produtos = new HashMap<>();

    public ProdutoMockGateway() {
        inicializarProdutosMock();
    }

    private void inicializarProdutosMock() {
        // Produtos pré-cadastrados para teste
        salvar(Produto.reconstituir(nextId(), "Big Mac", "Hambúrguer clássico com dois hambúrgueres", new BigDecimal("25.90"), CategoriaProduto.LANCHE));
        salvar(Produto.reconstituir(nextId(), "Quarterão", "Hambúrguer com carne de 113g", new BigDecimal("22.50"), CategoriaProduto.LANCHE));
        salvar(Produto.reconstituir(nextId(), "Cheeseburger", "Hambúrguer com queijo", new BigDecimal("18.90"), CategoriaProduto.LANCHE));
        
        salvar(Produto.reconstituir(nextId(), "Batata Frita Pequena", "Batata crocante porção pequena", new BigDecimal("8.50"), CategoriaProduto.ACOMPANHAMENTO));
        salvar(Produto.reconstituir(nextId(), "Batata Frita Média", "Batata crocante porção média", new BigDecimal("12.50"), CategoriaProduto.ACOMPANHAMENTO));
        salvar(Produto.reconstituir(nextId(), "Chicken McNuggets", "Nuggets de frango 10 unidades", new BigDecimal("15.90"), CategoriaProduto.ACOMPANHAMENTO));
        
        salvar(Produto.reconstituir(nextId(), "Coca-Cola", "Refrigerante 350ml", new BigDecimal("8.90"), CategoriaProduto.BEBIDA));
        salvar(Produto.reconstituir(nextId(), "Suco de Laranja", "Suco natural 300ml", new BigDecimal("9.50"), CategoriaProduto.BEBIDA));
        salvar(Produto.reconstituir(nextId(), "Água Mineral", "Água sem gás 500ml", new BigDecimal("4.50"), CategoriaProduto.BEBIDA));
        
        salvar(Produto.reconstituir(nextId(), "Sorvete Casquinha", "Sorvete de baunilha na casquinha", new BigDecimal("6.90"), CategoriaProduto.SOBREMESA));
        salvar(Produto.reconstituir(nextId(), "Torta de Maçã", "Torta quente de maçã", new BigDecimal("8.50"), CategoriaProduto.SOBREMESA));
        salvar(Produto.reconstituir(nextId(), "Cookies", "Cookies de chocolate chip", new BigDecimal("7.90"), CategoriaProduto.SOBREMESA));
    }

    private Long nextId() {
        return ID_GENERATOR.getAndIncrement();
    }

    @Override
    public Produto salvar(Produto produto) {
        if (produto.getId() == null) {
            produto.definirId(nextId());
        }
        produtos.put(produto.getId(), produto);
        return produto;
    }

    @Override
    public Produto atualizar(Produto produto) {
        if (!produtos.containsKey(produto.getId())) {
            throw new RuntimeException("Produto não encontrado para atualização");
        }
        produtos.put(produto.getId(), produto);
        return produto;
    }

    @Override
    public void remover(Long id) {
        produtos.remove(id);
    }

    @Override
    public Optional<Produto> buscarPorId(Long id) {
        return Optional.ofNullable(produtos.get(id));
    }

    @Override
    public List<Produto> buscarPorCategoria(CategoriaProduto categoria) {
        return produtos.values().stream()
                .filter(produto -> produto.getCategoria() == categoria)
                .collect(Collectors.toList());
    }

    @Override
    public List<Produto> listarTodos() {
        return new ArrayList<>(produtos.values());
    }

    @Override
    public boolean existePorId(Long id) {
        return produtos.containsKey(id);
    }

    @Override
    public boolean existePorNome(String nome) {
        return produtos.values().stream()
                .anyMatch(produto -> produto.getNome().equals(nome));
    }
}