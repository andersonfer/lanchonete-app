package br.com.lanchonete.autoatendimento.aplicacao.casosdeuso.produto;

import br.com.lanchonete.autoatendimento.aplicacao.dto.ProdutoRequestDTO;
import br.com.lanchonete.autoatendimento.aplicacao.dto.ProdutoResponseDTO;
import br.com.lanchonete.autoatendimento.aplicacao.portas.entrada.produto.EditarProdutoUC;
import br.com.lanchonete.autoatendimento.aplicacao.excecao.RecursoNaoEncontradoException;
import br.com.lanchonete.autoatendimento.aplicacao.excecao.ValidacaoException;
import br.com.lanchonete.autoatendimento.aplicacao.portas.saida.ProdutoRepositorio;
import br.com.lanchonete.autoatendimento.dominio.modelo.Produto;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class EditarProduto implements EditarProdutoUC {

    private final ProdutoRepositorio produtoRepositorio;

    @Override
    public ProdutoResponseDTO executar(Long id, ProdutoRequestDTO produtoParaEditar) {
        try {
            if (id == null) {
                throw new ValidacaoException("ID do produto é obrigatório");
            }

            Produto produto = produtoRepositorio.buscarPorId(id)
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado"));

            validarDuplicidade(produto, produtoParaEditar);

            produto.setNome(produtoParaEditar.nome());
            produto.setDescricao(produtoParaEditar.descricao());
            produto.setPreco(produtoParaEditar.preco());
            produto.setCategoria(produtoParaEditar.categoria());

            Produto produtoAtualizado = produtoRepositorio.atualizar(produto);

            return ProdutoResponseDTO.converterParaDTO(produtoAtualizado);
        } catch (IllegalArgumentException e) {
            throw new ValidacaoException(e.getMessage());
        }
    }


    private void validarDuplicidade(Produto produto, ProdutoRequestDTO produtoRequest) {
        boolean houveAlteracaoNoNome = !produto.getNome().equals(produtoRequest.nome());
        if (houveAlteracaoNoNome) {
            boolean existeOutroProdutoComMesmoNome = produtoRepositorio.existePorNome(produtoRequest.nome());
            if (existeOutroProdutoComMesmoNome) {
                throw new ValidacaoException("Já existe um produto com este nome");
            }
        }
    }

}