package br.com.lanchonete.produtos.aplicacao.casosdeuso;

import br.com.lanchonete.produtos.dominio.excecoes.RecursoNaoEncontradoException;
import br.com.lanchonete.produtos.dominio.excecoes.ValidacaoException;
import br.com.lanchonete.produtos.aplicacao.gateways.ProdutoGateway;
import br.com.lanchonete.produtos.dominio.entidades.Produto;
import java.util.Optional;

public class RemoverProduto {

    private final ProdutoGateway produtoGateway;

    public RemoverProduto(final ProdutoGateway produtoGateway) {
        this.produtoGateway = produtoGateway;
    }

    public void executar(final Long id) {

        if (id == null) {
            throw new ValidacaoException("ID do produto é obrigatório");
        }

        final Optional<Produto> produto = produtoGateway.buscarPorId(id);
        if (produto.isEmpty()) {
            throw new RecursoNaoEncontradoException("Produto não encontrado");
        }

        produtoGateway.remover(id);
    }

}