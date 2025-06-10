package br.com.lanchonete.autoatendimento.interfaces;

import br.com.lanchonete.autoatendimento.entidades.produto.Categoria;
import br.com.lanchonete.autoatendimento.entidades.produto.Produto;

import java.util.List;
import java.util.Optional;

public interface ProdutoGateway {
    Produto salvar(Produto produto);
    Produto atualizar(Produto produto);
    void remover(Long id);
    Optional<Produto> buscarPorId(Long id);
    List<Produto> buscarPorCategoria(Categoria categoria);
    List<Produto> listarTodos();
    boolean existePorId(Long id);
    boolean existePorNome(String nome);
}