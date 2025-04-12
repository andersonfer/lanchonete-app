package br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada;

import br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada.dto.ProdutoRequestDTO;
import br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada.dto.ProdutoResponseDTO;
import br.com.lanchonete.autoatendimento.aplicacao.excecao.ValidacaoException;
import br.com.lanchonete.autoatendimento.aplicacao.portas.entrada.EditarProdutoUC;
import br.com.lanchonete.autoatendimento.aplicacao.portas.saida.ProdutoRepositorio;
import br.com.lanchonete.autoatendimento.dominio.Produto;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class EditarProdutoService implements EditarProdutoUC {

    private final ProdutoRepositorio produtoRepositorio;

    @Override
    public ProdutoResponseDTO editar(Long id, ProdutoRequestDTO produtoRequest) {

        validarDadosProduto(id, produtoRequest);

        Produto produto = produtoRepositorio.buscarPorId(id)
                .orElseThrow(() -> new ValidacaoException("Produto não encontrado"));

        validarProdutoParaEdicao(produto, produtoRequest);

        produto.setNome(produtoRequest.getNome());
        produto.setDescricao(produtoRequest.getDescricao());
        produto.setPreco(produtoRequest.getPreco());
        produto.setCategoria(produtoRequest.getCategoria());

        Produto produtoAtualizado = produtoRepositorio.atualizar(produto);

        return converterParaResponseDTO(produtoAtualizado);
    }

    private void validarDadosProduto(Long idAtual, ProdutoRequestDTO produtoRequest) {

        if (idAtual == null) {
            throw new ValidacaoException("ID do produto é obrigatório");
        }

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

    }

    private void validarProdutoParaEdicao(Produto produto, ProdutoRequestDTO produtoRequest) {
        boolean houveAlteracaoNoNome = !produto.getNome().equals(produtoRequest.getNome());
        if (houveAlteracaoNoNome) {
            boolean existeOutroProdutoComMesmoNome = produtoRepositorio.existePorNome(produtoRequest.getNome());
            if (existeOutroProdutoComMesmoNome) {
                throw new ValidacaoException("Já existe um produto com este nome");
            }
        }
    }

    private ProdutoResponseDTO converterParaResponseDTO(Produto produto) {
        return ProdutoResponseDTO.builder()
                .id(produto.getId())
                .nome(produto.getNome())
                .descricao(produto.getDescricao())
                .preco(produto.getPreco())
                .categoria(produto.getCategoria())
                .build();
    }
}