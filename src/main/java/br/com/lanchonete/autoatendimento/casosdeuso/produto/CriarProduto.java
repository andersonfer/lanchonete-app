package br.com.lanchonete.autoatendimento.casosdeuso.produto;

import br.com.lanchonete.autoatendimento.controllers.dto.ProdutoRequestDTO;
import br.com.lanchonete.autoatendimento.controllers.dto.ProdutoResponseDTO;
import br.com.lanchonete.autoatendimento.aplicacao.excecao.ValidacaoException;
import br.com.lanchonete.autoatendimento.interfaces.ProdutoGateway;
import br.com.lanchonete.autoatendimento.entidades.produto.Produto;
public class CriarProduto {

    private final ProdutoGateway produtoGateway;

    public CriarProduto(final ProdutoGateway produtoGateway) {
        this.produtoGateway = produtoGateway;
    }

    public ProdutoResponseDTO executar(final ProdutoRequestDTO novoProduto) {
        try {
            if (produtoGateway.existePorNome(novoProduto.nome())) {
                throw new ValidacaoException("Já existe um produto com este nome");
            }

            final Produto produto = Produto.criar(
                    novoProduto.nome(),
                    novoProduto.descricao(),
                    novoProduto.preco(),
                    novoProduto.categoria());

            final Produto produtoSalvo = produtoGateway.salvar(produto);

            return ProdutoResponseDTO.converterParaDTO(produtoSalvo);
        } catch (IllegalArgumentException e) {
            throw new ValidacaoException(e.getMessage());
        }
    }

}
