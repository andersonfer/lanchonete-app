package br.com.lanchonete.autoatendimento.aplicacao.casosdeuso.produto;

import br.com.lanchonete.autoatendimento.aplicacao.dto.ProdutoRequestDTO;
import br.com.lanchonete.autoatendimento.aplicacao.dto.ProdutoResponseDTO;
import br.com.lanchonete.autoatendimento.aplicacao.excecao.ValidacaoException;
import br.com.lanchonete.autoatendimento.aplicacao.portas.entrada.produto.CriarProdutoUC;
import br.com.lanchonete.autoatendimento.aplicacao.portas.saida.ProdutoRepositorio;
import br.com.lanchonete.autoatendimento.entidades.produto.Produto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CriarProduto implements CriarProdutoUC {

    private final ProdutoRepositorio produtoRepositorio;

    @Override
    public ProdutoResponseDTO executar(final ProdutoRequestDTO novoProduto) {
        try {
            if (produtoRepositorio.existePorNome(novoProduto.nome())) {
                throw new ValidacaoException("Já existe um produto com este nome");
            }

            final Produto produto = Produto.criar(
                    novoProduto.nome(),
                    novoProduto.descricao(),
                    novoProduto.preco(),
                    novoProduto.categoria());

            final Produto produtoSalvo = produtoRepositorio.salvar(produto);

            return ProdutoResponseDTO.converterParaDTO(produtoSalvo);
        } catch (IllegalArgumentException e) {
            throw new ValidacaoException(e.getMessage());
        }
    }

}
