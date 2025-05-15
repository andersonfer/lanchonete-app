package br.com.lanchonete.autoatendimento.aplicacao.portas.entrada.produto;

import br.com.lanchonete.autoatendimento.aplicacao.dto.ProdutoResponseDTO;
import br.com.lanchonete.autoatendimento.dominio.Categoria;

import java.util.List;

public interface BuscarProdutosPorCategoriaUC {
    List<ProdutoResponseDTO> executar(Categoria categoria);
}
