package br.com.lanchonete.autoatendimento.casosdeuso.produto;

import br.com.lanchonete.autoatendimento.aplicacao.excecao.RecursoNaoEncontradoException;
import br.com.lanchonete.autoatendimento.aplicacao.excecao.ValidacaoException;
import br.com.lanchonete.autoatendimento.interfaces.ProdutoGateway;
import br.com.lanchonete.autoatendimento.entidades.produto.Produto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RemoverProduto {

    private final ProdutoGateway produtoGateway;

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