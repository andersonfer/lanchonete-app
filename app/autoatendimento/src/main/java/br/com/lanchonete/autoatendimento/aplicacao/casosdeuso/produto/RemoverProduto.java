package br.com.lanchonete.autoatendimento.aplicacao.casosdeuso.produto;

import br.com.lanchonete.autoatendimento.dominio.excecoes.RecursoNaoEncontradoException;
import br.com.lanchonete.autoatendimento.dominio.excecoes.ValidacaoException;
import br.com.lanchonete.autoatendimento.aplicacao.gateways.ProdutoGateway;
import br.com.lanchonete.autoatendimento.dominio.modelo.produto.Produto;
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