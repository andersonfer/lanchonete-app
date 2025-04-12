package br.com.lanchonete.autoatendimento.aplicacao.portas.entrada;

import br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada.dto.ProdutoResponseDTO;
import br.com.lanchonete.autoatendimento.dominio.Categoria;

import java.util.List;

public interface BuscarProdutosPorCategoriaUC {
    List<ProdutoResponseDTO> buscarProdutoPorCategoria(Categoria categoria);
}
