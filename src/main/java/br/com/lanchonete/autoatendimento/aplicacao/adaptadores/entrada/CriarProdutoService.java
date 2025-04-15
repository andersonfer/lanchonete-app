package br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada;

import br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada.dto.ProdutoRequestDTO;
import br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada.dto.ProdutoResponseDTO;
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
                .nome(novoProduto.nome())
                .descricao(novoProduto.descricao())
                .preco(novoProduto.preco())
                .categoria(novoProduto.categoria())
                .build();

        Produto produtoSalvo = produtoRepositorio.salvar(produto);

        return ProdutoResponseDTO.converterParaDTO(produtoSalvo);
    }

    private void validarParametros(ProdutoRequestDTO produtoRequest) {
        if (StringUtils.isBlank(produtoRequest.nome())) {
            throw new ValidacaoException("Nome do produto é obrigatório");
        }

        if (produtoRequest.preco() == null) {
            throw new ValidacaoException("Preço do produto é obrigatório");
        }

        if (produtoRequest.preco().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidacaoException("Preço deve ser maior que zero");
        }

        if (produtoRequest.categoria() == null) {
            throw new ValidacaoException("Categoria do produto é obrigatória");
        }

        if (produtoRepositorio.existePorNome(produtoRequest.nome())) {
            throw new ValidacaoException("Já existe um produto com este nome");
        }
    }
}
