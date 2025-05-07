package br.com.lanchonete.autoatendimento.aplicacao.casosdeuso.produto;

import br.com.lanchonete.autoatendimento.aplicacao.dto.ProdutoResponseDTO;
import br.com.lanchonete.autoatendimento.aplicacao.portas.entrada.produto.BuscarProdutosPorCategoriaUC;
import br.com.lanchonete.autoatendimento.aplicacao.excecao.ValidacaoException;
import br.com.lanchonete.autoatendimento.aplicacao.portas.saida.ProdutoRepositorio;
import br.com.lanchonete.autoatendimento.dominio.modelo.Categoria;
import br.com.lanchonete.autoatendimento.dominio.modelo.Produto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BuscarProdutosPorCategoria implements BuscarProdutosPorCategoriaUC {

    private final ProdutoRepositorio produtoRepositorio;

    @Override
    public List<ProdutoResponseDTO> executar(Categoria categoria) {
        if (categoria == null) {
            throw new ValidacaoException("Categoria é obrigatória");
        }

        List<Produto> produtos = produtoRepositorio.buscarPorCategoria(categoria);

        return produtos.stream()
                .map(ProdutoResponseDTO::converterParaDTO)
                .collect(Collectors.toList());
    }

}