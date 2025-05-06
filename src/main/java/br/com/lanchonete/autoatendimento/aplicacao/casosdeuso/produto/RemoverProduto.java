package br.com.lanchonete.autoatendimento.aplicacao.casosdeuso.produto;

import br.com.lanchonete.autoatendimento.aplicacao.excecao.RecursoNaoEncontradoException;
import br.com.lanchonete.autoatendimento.aplicacao.excecao.ValidacaoException;
import br.com.lanchonete.autoatendimento.aplicacao.portas.saida.ProdutoRepositorio;
import br.com.lanchonete.autoatendimento.aplicacao.dominio.Produto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RemoverProduto implements RemoverProdutoUC {

    private final ProdutoRepositorio produtoRepositorio;

    @Override
    public void executar(Long id) {

        validarParametros(id);

        produtoRepositorio.remover(id);
    }

    private void validarParametros(Long id) {
        if (id == null) {
            throw new ValidacaoException("ID do produto é obrigatório");
        }

        Optional<Produto> produto = produtoRepositorio.buscarPorId(id);
        if (produto.isEmpty()) {
            throw new RecursoNaoEncontradoException("Produto não encontrado");
        }
    }
}