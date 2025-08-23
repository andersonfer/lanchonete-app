package br.com.lanchonete.produtos.aplicacao.casosdeuso;

import br.com.lanchonete.produtos.dominio.excecoes.RecursoNaoEncontradoException;
import br.com.lanchonete.produtos.dominio.entidades.Preco;
import br.com.lanchonete.produtos.dominio.excecoes.ValidacaoException;
import br.com.lanchonete.produtos.aplicacao.gateways.ProdutoGateway;
import br.com.lanchonete.produtos.dominio.entidades.Produto;
import br.com.lanchonete.produtos.dominio.enums.CategoriaProduto;
import java.math.BigDecimal;

public class EditarProduto {

    private final ProdutoGateway produtoGateway;

    public EditarProduto(final ProdutoGateway produtoGateway) {
        this.produtoGateway = produtoGateway;
    }

    public Produto executar(final Long id, final String nome, final String descricao, final BigDecimal preco, final CategoriaProduto categoria) {
        try {
            if (id == null) {
                throw new ValidacaoException("ID do produto é obrigatório");
            }

            final Produto produto = produtoGateway.buscarPorId(id)
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado"));

            validarDuplicidade(produto, nome);

            produto.definirNome(nome);
            produto.definirDescricao(descricao);
            produto.definirPreco(new Preco(preco));
            produto.definirCategoria(categoria);

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