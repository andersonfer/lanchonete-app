package br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada.dto;

import br.com.lanchonete.autoatendimento.dominio.Categoria;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ProdutoRequestDTO {
    private String nome;
    private String descricao;
    private BigDecimal preco;
    private Categoria categoria;
}