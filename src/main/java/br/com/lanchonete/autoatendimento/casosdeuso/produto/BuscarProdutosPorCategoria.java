package br.com.lanchonete.autoatendimento.casosdeuso.produto;

import br.com.lanchonete.autoatendimento.adaptadores.web.dto.ProdutoResponseDTO;
import br.com.lanchonete.autoatendimento.aplicacao.excecao.ValidacaoException;
import br.com.lanchonete.autoatendimento.interfaces.ProdutoGateway;
import br.com.lanchonete.autoatendimento.entidades.produto.Categoria;
import br.com.lanchonete.autoatendimento.entidades.produto.Produto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class BuscarProdutosPorCategoria {

    private final ProdutoGateway produtoGateway;

    public List<ProdutoResponseDTO> executar(final Categoria categoria) {
        if (categoria == null) {
            throw new ValidacaoException("Categoria é obrigatória");
        }

        final List<Produto> produtos = produtoGateway.buscarPorCategoria(categoria);

        return Stream.of(produtos)
                .flatMap(List::stream)
                .map(ProdutoResponseDTO::converterParaDTO)
                .toList();
    }

}