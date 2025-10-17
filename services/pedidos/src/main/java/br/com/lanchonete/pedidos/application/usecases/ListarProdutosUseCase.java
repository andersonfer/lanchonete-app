package br.com.lanchonete.pedidos.application.usecases;

import br.com.lanchonete.pedidos.application.gateways.ProdutoGateway;
import br.com.lanchonete.pedidos.domain.model.Categoria;
import br.com.lanchonete.pedidos.domain.model.Produto;

import java.util.List;

public class ListarProdutosUseCase {

    private final ProdutoGateway produtoGateway;

    public ListarProdutosUseCase(ProdutoGateway produtoGateway) {
        this.produtoGateway = produtoGateway;
    }

    public List<Produto> executar(Categoria categoria) {
        if (categoria != null) {
            return produtoGateway.listarPorCategoria(categoria);
        }
        return produtoGateway.listarTodos();
    }
}
