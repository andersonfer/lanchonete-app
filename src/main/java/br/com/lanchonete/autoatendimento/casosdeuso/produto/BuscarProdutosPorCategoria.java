package br.com.lanchonete.autoatendimento.casosdeuso.produto;

import br.com.lanchonete.autoatendimento.dominio.shared.excecao.ValidacaoException;
import br.com.lanchonete.autoatendimento.interfaces.ProdutoGateway;
import br.com.lanchonete.autoatendimento.entidades.produto.Categoria;
import br.com.lanchonete.autoatendimento.entidades.produto.Produto;
import java.util.List;

public class BuscarProdutosPorCategoria {

    private final ProdutoGateway produtoGateway;

    public BuscarProdutosPorCategoria(final ProdutoGateway produtoGateway) {
        this.produtoGateway = produtoGateway;
    }

    public List<Produto> executar(final Categoria categoria) {
        if (categoria == null) {
            throw new ValidacaoException("Categoria é obrigatória");
        }

        return produtoGateway.buscarPorCategoria(categoria);
    }

}