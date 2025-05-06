package br.com.lanchonete.autoatendimento.aplicacao.casosdeuso.interfaces.produto;

import br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada.dto.ProdutoResponseDTO;
import br.com.lanchonete.autoatendimento.aplicacao.dominio.Categoria;

import java.util.List;

public interface BuscarProdutosPorCategoriaUC {
    List<ProdutoResponseDTO> executar(Categoria categoria);
}
