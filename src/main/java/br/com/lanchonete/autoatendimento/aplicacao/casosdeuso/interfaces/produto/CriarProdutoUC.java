package br.com.lanchonete.autoatendimento.aplicacao.casosdeuso.interfaces.produto;

import br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada.dto.ProdutoRequestDTO;
import br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada.dto.ProdutoResponseDTO;

public interface CriarProdutoUC {
    ProdutoResponseDTO executar(ProdutoRequestDTO produtoRequest);
}
