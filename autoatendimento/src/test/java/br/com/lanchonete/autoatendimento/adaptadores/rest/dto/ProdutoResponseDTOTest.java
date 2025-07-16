package br.com.lanchonete.autoatendimento.adaptadores.rest.dto;

import br.com.lanchonete.autoatendimento.adaptadores.rest.mappers.EnumsMapper;
import br.com.lanchonete.autoatendimento.adaptadores.rest.mappers.ProdutoMapper;
import br.com.lanchonete.autoatendimento.dominio.modelo.produto.Categoria;
import br.com.lanchonete.autoatendimento.dominio.modelo.produto.Produto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ProdutoResponseDTOTest {

    private ProdutoMapper produtoMapper;

    @BeforeEach
    void configurar() {
        EnumsMapper enumsMapper = new EnumsMapper();
        produtoMapper = new ProdutoMapper(enumsMapper);
    }

    @Test
    @DisplayName("Deve converter Produto para ProdutoResponseDTO corretamente")
    void t1() {

        Produto produto = Produto.reconstituir(
                1L,
                "X-Bacon",
                "Hambúrguer com bacon",
                new BigDecimal("29.90"),
                Categoria.LANCHE);


        ProdutoResponseDTO dto = produtoMapper.paraDTO(produto);

        assertNotNull(dto, "O DTO não deveria ser nulo");
        assertEquals(1L, dto.id(), "O ID deveria ser 1L");
        assertEquals("X-Bacon", dto.nome(), "O nome deveria ser 'X-Bacon'");
        assertEquals("Hambúrguer com bacon", dto.descricao(), "A descrição não foi mapeada corretamente");
        assertEquals(new BigDecimal("29.90"), dto.preco(), "O preço não foi mapeado corretamente");
        assertEquals(CategoriaDTO.LANCHE, dto.categoria(), "A categoria não foi mapeada corretamente");
    }

    @Test
    @DisplayName("Deve retornar null quando Produto for null")
    void t2() {
        ProdutoResponseDTO dto = produtoMapper.paraDTO(null);
        assertNull(dto, "O DTO deveria ser nulo quando o produto é nulo");
    }
}