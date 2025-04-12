package br.com.lanchonete.autoatendimento.aplicacao.portas.entrada;

import br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada.dto.ProdutoRequestDTO;
import br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada.dto.ProdutoResponseDTO;

public interface EditarProdutoUC {
    ProdutoResponseDTO editar(Long id, ProdutoRequestDTO produtoRequest);
}
