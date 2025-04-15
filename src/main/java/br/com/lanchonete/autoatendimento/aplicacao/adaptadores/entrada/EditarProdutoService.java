package br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada;

import br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada.dto.ProdutoRequestDTO;
import br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada.dto.ProdutoResponseDTO;
import br.com.lanchonete.autoatendimento.aplicacao.excecao.RecursoNaoEncontradoException;
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
    public ProdutoResponseDTO editar(Long id, ProdutoRequestDTO produtoParaEditar) {

        validarParametros(id, produtoParaEditar);

        Produto produto = produtoRepositorio.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado"));

        validarDuplicidade(produto, produtoParaEditar);

        produto.setNome(produtoParaEditar.nome());
        produto.setDescricao(produtoParaEditar.descricao());
        produto.setPreco(produtoParaEditar.preco());
        produto.setCategoria(produtoParaEditar.categoria());

        Produto produtoAtualizado = produtoRepositorio.atualizar(produto);

        return ProdutoResponseDTO.converterParaDTO(produtoAtualizado);
    }

    private void validarParametros(Long idAtual, ProdutoRequestDTO produtoRequest) {

        if (idAtual == null) {
            throw new ValidacaoException("ID do produto é obrigatório");
        }

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