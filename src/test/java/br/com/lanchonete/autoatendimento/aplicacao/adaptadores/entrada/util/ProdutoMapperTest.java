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

        Produto produto = Produto.builder()
                .id(1L)
                .nome("X-Bacon")
                .descricao("Hambúrguer com bacon")
                .preco(new BigDecimal("29.90"))
                .categoria(Categoria.LANCHE)
                .build();


        ProdutoResponseDTO dto = ProdutoMapper.converterParaResponseDTO(produto);

        assertNotNull(dto, "O DTO não deveria ser nulo");
        assertEquals(1L, dto.id(), "O ID deveria ser 1L");
        assertEquals("X-Bacon", dto.nome(), "O nome deveria ser 'X-Bacon'");
        assertEquals("Hambúrguer com bacon", dto.descricao(), "A descrição não foi mapeada corretamente");
        assertEquals(new BigDecimal("29.90"), dto.preco(), "O preço não foi mapeado corretamente");
        assertEquals(Categoria.LANCHE, dto.categoria(), "A categoria não foi mapeada corretamente");
    }

    @Test
    @DisplayName("Deve retornar null quando Produto for null")
    void t2() {
        ProdutoResponseDTO dto = ProdutoMapper.converterParaResponseDTO(null);
        assertNull(dto, "O DTO deveria ser nulo quando o produto é nulo");
    }
}