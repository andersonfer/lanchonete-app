package br.com.lanchonete.autoatendimento.aplicacao.casosdeuso.produto;

import br.com.lanchonete.autoatendimento.aplicacao.dto.ProdutoRequestDTO;
import br.com.lanchonete.autoatendimento.aplicacao.dto.ProdutoResponseDTO;
import br.com.lanchonete.autoatendimento.aplicacao.excecao.RecursoNaoEncontradoException;
import br.com.lanchonete.autoatendimento.aplicacao.excecao.ValidacaoException;
import br.com.lanchonete.autoatendimento.aplicacao.portas.entrada.produto.EditarProdutoUC;
import br.com.lanchonete.autoatendimento.aplicacao.portas.saida.ProdutoRepositorio;
import br.com.lanchonete.autoatendimento.entidades.produto.Produto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EditarProduto implements EditarProdutoUC {

    private final ProdutoRepositorio produtoRepositorio;

    @Override
    public ProdutoResponseDTO executar(final Long id, final ProdutoRequestDTO produtoParaEditar) {
        try {
            if (id == null) {
                throw new ValidacaoException("ID do produto é obrigatório");
            }

            final Produto produto = produtoRepositorio.buscarPorId(id)
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado"));

            validarDuplicidade(produto, produtoParaEditar);

            produto.setNome(produtoParaEditar.nome());
            produto.setDescricao(produtoParaEditar.descricao());
            produto.setPreco(produtoParaEditar.preco());
            produto.setCategoria(produtoParaEditar.categoria());

            final Produto produtoAtualizado = produtoRepositorio.atualizar(produto);

            return ProdutoResponseDTO.converterParaDTO(produtoAtualizado);
        } catch (IllegalArgumentException e) {
            throw new ValidacaoException(e.getMessage());
        }
    }


    private void validarDuplicidade(Produto produto, ProdutoRequestDTO produtoRequest) {
        final boolean houveAlteracaoNoNome = !produto.getNome().equals(produtoRequest.nome());
        if (houveAlteracaoNoNome) {
            final boolean existeOutroProdutoComMesmoNome = produtoRepositorio.existePorNome(produtoRequest.nome());
            if (existeOutroProdutoComMesmoNome) {
                throw new ValidacaoException("Já existe um produto com este nome");
            }
        }
    }

}