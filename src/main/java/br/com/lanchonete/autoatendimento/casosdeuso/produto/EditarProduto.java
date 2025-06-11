package br.com.lanchonete.autoatendimento.casosdeuso.produto;

import br.com.lanchonete.autoatendimento.controllers.dto.ProdutoRequestDTO;
import br.com.lanchonete.autoatendimento.controllers.dto.ProdutoResponseDTO;
import br.com.lanchonete.autoatendimento.dominio.shared.excecao.RecursoNaoEncontradoException;
import br.com.lanchonete.autoatendimento.entidades.shared.Preco;
import br.com.lanchonete.autoatendimento.dominio.shared.excecao.ValidacaoException;
import br.com.lanchonete.autoatendimento.interfaces.ProdutoGateway;
import br.com.lanchonete.autoatendimento.entidades.produto.Produto;
public class EditarProduto {

    private final ProdutoGateway produtoGateway;

    public EditarProduto(final ProdutoGateway produtoGateway) {
        this.produtoGateway = produtoGateway;
    }

    public ProdutoResponseDTO executar(final Long id, final ProdutoRequestDTO produtoParaEditar) {
        try {
            if (id == null) {
                throw new ValidacaoException("ID do produto é obrigatório");
            }

            final Produto produto = produtoGateway.buscarPorId(id)
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado"));

            validarDuplicidade(produto, produtoParaEditar);

            produto.setNome(produtoParaEditar.nome());
            produto.setDescricao(produtoParaEditar.descricao());
            produto.setPreco(new Preco(produtoParaEditar.preco()));
            produto.setCategoria(produtoParaEditar.categoria());

            final Produto produtoAtualizado = produtoGateway.atualizar(produto);

            return ProdutoResponseDTO.converterParaDTO(produtoAtualizado);
        } catch (IllegalArgumentException e) {
            throw new ValidacaoException(e.getMessage());
        }
    }


    private void validarDuplicidade(Produto produto, ProdutoRequestDTO produtoRequest) {
        final boolean houveAlteracaoNoNome = !produto.getNome().equals(produtoRequest.nome());
        if (houveAlteracaoNoNome) {
            final boolean existeOutroProdutoComMesmoNome = produtoGateway.existePorNome(produtoRequest.nome());
            if (existeOutroProdutoComMesmoNome) {
                throw new ValidacaoException("Já existe um produto com este nome");
            }
        }
    }

}