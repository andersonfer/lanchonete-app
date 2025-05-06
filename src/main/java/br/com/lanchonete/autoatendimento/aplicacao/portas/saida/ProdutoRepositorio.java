package br.com.lanchonete.autoatendimento.aplicacao.portas.saida;

import br.com.lanchonete.autoatendimento.aplicacao.dominio.Categoria;
import br.com.lanchonete.autoatendimento.aplicacao.dominio.Produto;

import java.util.List;
import java.util.Optional;

public interface ProdutoRepositorio {
    Produto salvar(Produto produto);
    Produto atualizar(Produto produto);
    void remover(Long id);
    Optional<Produto> buscarPorId(Long id);
    List<Produto> buscarPorCategoria(Categoria categoria);
    List<Produto> listarTodos();
    boolean existePorId(Long id);
    boolean existePorNome(String nome);
}