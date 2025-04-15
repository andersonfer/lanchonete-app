package br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada;

import br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada.dto.ProdutoRequestDTO;
import br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada.dto.ProdutoResponseDTO;
import br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada.util.ProdutoMapper;
import br.com.lanchonete.autoatendimento.aplicacao.excecao.ValidacaoException;
import br.com.lanchonete.autoatendimento.aplicacao.portas.entrada.CriarProdutoUC;
import br.com.lanchonete.autoatendimento.aplicacao.portas.saida.ProdutoRepositorio;
import br.com.lanchonete.autoatendimento.dominio.Produto;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class CriarProdutoService implements CriarProdutoUC {

    private final ProdutoRepositorio produtoRepositorio;

    @Override
    public ProdutoResponseDTO criar(ProdutoRequestDTO novoProduto) {

        validarParametros(novoProduto);

        Produto produto = Produto.builder()
                .nome(novoProduto.getNome())
                .descricao(novoProduto.getDescricao())
                .preco(novoProduto.getPreco())
                .categoria(novoProduto.getCategoria())
                .build();

        Produto produtoSalvo = produtoRepositorio.salvar(produto);

        return ProdutoMapper.converterParaResponseDTO(produtoSalvo);
    }

    private void validarParametros(ProdutoRequestDTO produtoRequest) {
        if (StringUtils.isBlank(produtoRequest.getNome())) {
            throw new ValidacaoException("Nome do produto é obrigatório");
        }

        if (produtoRequest.getPreco() == null) {
            throw new ValidacaoException("Preço do produto é obrigatório");
        }

        if (produtoRequest.getPreco().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidacaoException("Preço deve ser maior que zero");
        }

        if (produtoRequest.getCategoria() == null) {
            throw new ValidacaoException("Categoria do produto é obrigatória");
        }

        if (produtoRepositorio.existePorNome(produtoRequest.getNome())) {
            throw new ValidacaoException("Já existe um produto com este nome");
        }
    }
}
