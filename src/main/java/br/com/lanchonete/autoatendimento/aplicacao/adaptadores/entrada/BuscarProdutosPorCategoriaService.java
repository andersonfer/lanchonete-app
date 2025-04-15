package br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada;

import br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada.dto.ProdutoResponseDTO;
import br.com.lanchonete.autoatendimento.aplicacao.excecao.ValidacaoException;
import br.com.lanchonete.autoatendimento.aplicacao.portas.entrada.BuscarProdutosPorCategoriaUC;
import br.com.lanchonete.autoatendimento.aplicacao.portas.saida.ProdutoRepositorio;
import br.com.lanchonete.autoatendimento.dominio.Categoria;
import br.com.lanchonete.autoatendimento.dominio.Produto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BuscarProdutosPorCategoriaService implements BuscarProdutosPorCategoriaUC {

    private final ProdutoRepositorio produtoRepositorio;

    @Override
    public List<ProdutoResponseDTO> buscarProdutoPorCategoria(Categoria categoria) {
        validarParametros(categoria);

        List<Produto> produtos = produtoRepositorio.buscarPorCategoria(categoria);

        return produtos.stream()
                .map(ProdutoResponseDTO::converterParaDTO)
                .collect(Collectors.toList());
    }

    private void validarParametros(Categoria categoria) {
        if (categoria == null) {
            throw new ValidacaoException("Categoria é obrigatória");
        }
    }

}