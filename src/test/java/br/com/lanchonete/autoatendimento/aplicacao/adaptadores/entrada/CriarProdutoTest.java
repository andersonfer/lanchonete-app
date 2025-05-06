package br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada;

import br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada.dto.ProdutoRequestDTO;
import br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada.dto.ProdutoResponseDTO;
import br.com.lanchonete.autoatendimento.aplicacao.casosdeuso.impl.produto.CriarProduto;
import br.com.lanchonete.autoatendimento.aplicacao.excecao.ValidacaoException;
import br.com.lanchonete.autoatendimento.aplicacao.repositorios.ProdutoRepositorio;
import br.com.lanchonete.autoatendimento.aplicacao.dominio.Categoria;
import br.com.lanchonete.autoatendimento.aplicacao.dominio.Produto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class CriarProdutoTest {

    @Mock
    private ProdutoRepositorio produtoRepositorio;

    @InjectMocks
    private CriarProduto criarProduto;

    private ProdutoRequestDTO produtoValido;
    private Produto produtoSalvo;

    @BeforeEach
    void configurar() {
        produtoValido = new ProdutoRequestDTO("X-Bacon",
                "Hambúrguer com bacon crocante", new BigDecimal("28.90"), Categoria.LANCHE);


        produtoSalvo = Produto.builder()
                .id(1L)
                .nome("X-Bacon")
                .descricao("Hambúrguer com bacon crocante")
                .preco(new BigDecimal("28.90"))
                .categoria(Categoria.LANCHE)
                .build();
    }

    @Test
    @DisplayName("Deve criar produto com sucesso quando os dados são válidos")
    void t1() {
        // Arrange
        when(produtoRepositorio.existePorNome(produtoValido.nome())).thenReturn(false);
        when(produtoRepositorio.salvar(any(Produto.class))).thenReturn(produtoSalvo);

        // Act
        ProdutoResponseDTO response = criarProduto.executar(produtoValido);

        // Assert
        assertNotNull(response, "A resposta não deveria ser nula");
        assertEquals(1L, response.id(), "O ID do produto salvo deveria ser 1");
        assertEquals("X-Bacon", response.nome(), "O nome do produto salvo está incorreto");
        assertEquals(new BigDecimal("28.90"), response.preco(), "O preço do produto salvo está incorreto");
        assertEquals(Categoria.LANCHE, response.categoria(), "A categoria do produto salvo está incorreta");

        verify(produtoRepositorio).existePorNome(produtoValido.nome());
        verify(produtoRepositorio).salvar(any(Produto.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar criar produto com nome duplicado")
    void t2() {
        // Arrange
        when(produtoRepositorio.existePorNome(produtoValido.nome())).thenReturn(true);

        // Act & Assert
        ValidacaoException exception = assertThrows(ValidacaoException.class,
                () -> criarProduto.executar(produtoValido),
                "Deveria lançar uma exceção para nome duplicado");

        assertEquals("Já existe um produto com este nome", exception.getMessage(),
                "Mensagem da exceção está incorreta");

        verify(produtoRepositorio).existePorNome(produtoValido.nome());
        verify(produtoRepositorio, never()).salvar(any(Produto.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar criar produto sem nome")
    void t3() {
        // Arrange
        ProdutoRequestDTO produtoSemNome = new ProdutoRequestDTO("", "Descrição teste", new BigDecimal("15.90"), Categoria.BEBIDA);


        // Act & Assert
        ValidacaoException exception = assertThrows(ValidacaoException.class,
                () -> criarProduto.executar(produtoSemNome),
                "Deveria lançar uma exceção para nome vazio");

        assertEquals("Nome do produto é obrigatório", exception.getMessage(),
                "Mensagem da exceção está incorreta");

        verify(produtoRepositorio, never()).existePorNome(anyString());
        verify(produtoRepositorio, never()).salvar(any(Produto.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar criar produto sem preço")
    void t4() {
        // Arrange
        ProdutoRequestDTO produtoSemPreco = new ProdutoRequestDTO("Refrigerante Cola",
                "Refrigerante de cola 350ml", null, Categoria.BEBIDA);

        // Act & Assert
        ValidacaoException exception = assertThrows(ValidacaoException.class,
                () -> criarProduto.executar(produtoSemPreco),
                "Deveria lançar uma exceção para preço nulo");

        assertEquals("Preço do produto é obrigatório", exception.getMessage(),
                "Mensagem da exceção está incorreta");

        verify(produtoRepositorio, never()).existePorNome(anyString());
        verify(produtoRepositorio, never()).salvar(any(Produto.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar criar produto com preço zero ou negativo")
    void t5() {
        // Arrange
        ProdutoRequestDTO produtoPrecoZero = new ProdutoRequestDTO("Refrigerante Cola",
                "Refrigerante de cola 350ml", BigDecimal.ZERO, Categoria.BEBIDA);


        // Act & Assert
        ValidacaoException exception = assertThrows(ValidacaoException.class,
                () -> criarProduto.executar(produtoPrecoZero),
                "Deveria lançar uma exceção para preço zero");

        assertEquals("Preço deve ser maior que zero", exception.getMessage(),
                "Mensagem da exceção está incorreta");

        // Teste com preço negativo
        ProdutoRequestDTO produtoPrecoNegativo = new ProdutoRequestDTO("Refrigerante Cola",
                "Refrigerante de cola 350ml", new BigDecimal("-5.00"), Categoria.BEBIDA);

        ValidacaoException exception2 = assertThrows(ValidacaoException.class,
                () -> criarProduto.executar(produtoPrecoNegativo),
                "Deveria lançar uma exceção para preço negativo");

        assertEquals("Preço deve ser maior que zero", exception2.getMessage(),
                "Mensagem da exceção está incorreta");

        verify(produtoRepositorio, never()).existePorNome(anyString());
        verify(produtoRepositorio, never()).salvar(any(Produto.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar criar produto sem categoria")
    void t6() {
        // Arrange
        ProdutoRequestDTO produtoSemCategoria = new ProdutoRequestDTO("Refrigerante Cola",
                "Refrigerante de cola 350ml", new BigDecimal("5.50"), null);

        // Act & Assert
        ValidacaoException exception = assertThrows(ValidacaoException.class,
                () -> criarProduto.executar(produtoSemCategoria),
                "Deveria lançar uma exceção para categoria nula");

        assertEquals("Categoria do produto é obrigatória", exception.getMessage(),
                "Mensagem da exceção está incorreta");

        verify(produtoRepositorio, never()).existePorNome(anyString());
        verify(produtoRepositorio, never()).salvar(any(Produto.class));
    }
}