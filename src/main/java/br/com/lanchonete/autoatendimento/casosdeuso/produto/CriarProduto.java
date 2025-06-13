package br.com.lanchonete.autoatendimento.casosdeuso.produto;

import br.com.lanchonete.autoatendimento.dominio.shared.excecao.ValidacaoException;
import br.com.lanchonete.autoatendimento.interfaces.ProdutoGateway;
import br.com.lanchonete.autoatendimento.entidades.produto.Produto;
import br.com.lanchonete.autoatendimento.entidades.produto.Categoria;
import java.math.BigDecimal;
public class CriarProduto {

    private final ProdutoGateway produtoGateway;

    public CriarProduto(final ProdutoGateway produtoGateway) {
        this.produtoGateway = produtoGateway;
    }

    public Produto executar(final String nome, final String descricao, final BigDecimal preco, final Categoria categoria) {
        try {
            if (produtoGateway.existePorNome(nome)) {
                throw new ValidacaoException("Já existe um produto com este nome");
            }

            final Produto produto = Produto.criar(nome, descricao, preco, categoria);

            return produtoGateway.salvar(produto);
        } catch (IllegalArgumentException e) {
            throw new ValidacaoException(e.getMessage());
        }
    }

}
