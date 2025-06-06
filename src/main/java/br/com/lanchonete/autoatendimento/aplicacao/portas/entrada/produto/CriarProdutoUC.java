package br.com.lanchonete.autoatendimento.aplicacao.portas.entrada.produto;

import br.com.lanchonete.autoatendimento.adaptadores.web.dto.ProdutoRequestDTO;
import br.com.lanchonete.autoatendimento.adaptadores.web.dto.ProdutoResponseDTO;

public interface CriarProdutoUC {
    ProdutoResponseDTO executar(ProdutoRequestDTO produtoRequest);
}
