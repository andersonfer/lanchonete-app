package br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada.util;

import br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada.dto.ProdutoResponseDTO;
import br.com.lanchonete.autoatendimento.dominio.Categoria;
import br.com.lanchonete.autoatendimento.dominio.Produto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ProdutoMapperTest {

    @Test
    @DisplayName("Deve converter Produto para ProdutoResponseDTO corretamente")
    void t1() {
        // Arrange
        Produto produto = Produto.builder()
                .id(1L)
                .nome("X-Bacon")
                .descricao("Hambúrguer com bacon")
                .preco(new BigDecimal("29.90"))
                .categoria(Categoria.LANCHE)
                .build();

        // Act
        ProdutoResponseDTO dto = ProdutoMapper.converterParaResponseDTO(produto);

        // Assert
        assertNotNull(dto, "O DTO não deveria ser nulo");
        assertEquals(1L, dto.getId(), "O ID deveria ser 1L");
        assertEquals("X-Bacon", dto.getNome(), "O nome deveria ser 'X-Bacon'");
        assertEquals("Hambúrguer com bacon", dto.getDescricao(), "A descrição não foi mapeada corretamente");
        assertEquals(new BigDecimal("29.90"), dto.getPreco(), "O preço não foi mapeado corretamente");
        assertEquals(Categoria.LANCHE, dto.getCategoria(), "A categoria não foi mapeada corretamente");
    }
}