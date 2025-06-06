package br.com.lanchonete.autoatendimento.aplicacao.casosdeuso.produto;

import br.com.lanchonete.autoatendimento.aplicacao.dto.ProdutoResponseDTO;
import br.com.lanchonete.autoatendimento.aplicacao.excecao.ValidacaoException;
import br.com.lanchonete.autoatendimento.aplicacao.portas.entrada.produto.BuscarProdutosPorCategoriaUC;
import br.com.lanchonete.autoatendimento.aplicacao.portas.saida.ProdutoRepositorio;
import br.com.lanchonete.autoatendimento.entidades.produto.Categoria;
import br.com.lanchonete.autoatendimento.entidades.produto.Produto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class BuscarProdutosPorCategoria implements BuscarProdutosPorCategoriaUC {

    private final ProdutoRepositorio produtoRepositorio;

    @Override
    public List<ProdutoResponseDTO> executar(final Categoria categoria) {
        if (categoria == null) {
            throw new ValidacaoException("Categoria é obrigatória");
        }

        final List<Produto> produtos = produtoRepositorio.buscarPorCategoria(categoria);

        return Stream.of(produtos)
                .flatMap(List::stream)
                .map(ProdutoResponseDTO::converterParaDTO)
                .toList();
    }

}