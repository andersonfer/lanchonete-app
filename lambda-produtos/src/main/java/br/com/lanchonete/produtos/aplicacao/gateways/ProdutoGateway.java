package br.com.lanchonete.produtos.aplicacao.gateways;

import br.com.lanchonete.produtos.dominio.enums.CategoriaProduto;
import br.com.lanchonete.produtos.dominio.entidades.Produto;

import java.util.List;
import java.util.Optional;

public interface ProdutoGateway {
    Produto salvar(Produto produto);
    Produto atualizar(Produto produto);
    void remover(Long id);
    Optional<Produto> buscarPorId(Long id);
    List<Produto> buscarPorCategoria(CategoriaProduto categoria);
    List<Produto> listarTodos();
    boolean existePorId(Long id);
    boolean existePorNome(String nome);
}