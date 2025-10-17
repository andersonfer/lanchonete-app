package br.com.lanchonete.pedidos.application.gateways;

import br.com.lanchonete.pedidos.domain.model.Categoria;
import br.com.lanchonete.pedidos.domain.model.Produto;

import java.util.List;
import java.util.Optional;

public interface ProdutoGateway {
    Optional<Produto> buscarPorId(Long id);
    List<Produto> listarTodos();
    List<Produto> listarPorCategoria(Categoria categoria);
}
