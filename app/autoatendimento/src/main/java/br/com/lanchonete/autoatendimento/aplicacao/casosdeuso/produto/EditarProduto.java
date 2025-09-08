package br.com.lanchonete.autoatendimento.aplicacao.casosdeuso.produto;

import br.com.lanchonete.autoatendimento.dominio.excecoes.RecursoNaoEncontradoException;
import br.com.lanchonete.autoatendimento.dominio.modelo.shared.Preco;
import br.com.lanchonete.autoatendimento.dominio.excecoes.ValidacaoException;
import br.com.lanchonete.autoatendimento.aplicacao.gateways.ProdutoGateway;
import br.com.lanchonete.autoatendimento.dominio.modelo.produto.Produto;
import br.com.lanchonete.autoatendimento.dominio.modelo.produto.Categoria;
import java.math.BigDecimal;
public class EditarProduto {

    private final ProdutoGateway produtoGateway;

    public EditarProduto(final ProdutoGateway produtoGateway) {
        this.produtoGateway = produtoGateway;
    }

    public Produto executar(final Long id, final String nome, final String descricao, final BigDecimal preco, final Categoria categoria) {
        try {
            if (id == null) {
                throw new ValidacaoException("ID do produto é obrigatório");
            }

            final Produto produto = produtoGateway.buscarPorId(id)
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado"));

            validarDuplicidade(produto, nome);

            produto.setNome(nome);
            produto.setDescricao(descricao);
            produto.setPreco(new Preco(preco));
            produto.setCategoria(categoria);

            return produtoGateway.atualizar(produto);
        } catch (IllegalArgumentException e) {
            throw new ValidacaoException(e.getMessage());
        }
    }


    private void validarDuplicidade(Produto produto, String novoNome) {
        final boolean houveAlteracaoNoNome = !produto.getNome().equals(novoNome);
        if (houveAlteracaoNoNome) {
            final boolean existeOutroProdutoComMesmoNome = produtoGateway.existePorNome(novoNome);
            if (existeOutroProdutoComMesmoNome) {
                throw new ValidacaoException("Já existe um produto com este nome");
            }
        }
    }

}