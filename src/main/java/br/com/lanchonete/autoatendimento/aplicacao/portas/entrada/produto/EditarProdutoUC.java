package br.com.lanchonete.autoatendimento.aplicacao.portas.entrada.produto;

import br.com.lanchonete.autoatendimento.aplicacao.dto.ProdutoRequestDTO;
import br.com.lanchonete.autoatendimento.aplicacao.dto.ProdutoResponseDTO;

public interface EditarProdutoUC {
    ProdutoResponseDTO executar(Long id, ProdutoRequestDTO produtoRequest);
}
