package br.com.lanchonete.produtos.aplicacao.casosdeuso;

import br.com.lanchonete.produtos.dominio.excecoes.ValidacaoException;
import br.com.lanchonete.produtos.aplicacao.gateways.ProdutoGateway;
import br.com.lanchonete.produtos.dominio.entidades.Produto;
import br.com.lanchonete.produtos.dominio.enums.CategoriaProduto;
import java.math.BigDecimal;

public class CriarProduto {

    private final ProdutoGateway produtoGateway;

    public CriarProduto(final ProdutoGateway produtoGateway) {
        this.produtoGateway = produtoGateway;
    }

    public Produto executar(final String nome, final String descricao, final BigDecimal preco, final CategoriaProduto categoria) {
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