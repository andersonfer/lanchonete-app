package br.com.lanchonete.autoatendimento.dominio;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class Produto {
    private Long id;
    private String nome;
    private String descricao;
    private BigDecimal preco;
    private Categoria categoria;
}