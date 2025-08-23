package br.com.lanchonete.produtos.aplicacao.casosdeuso;

import br.com.lanchonete.produtos.dominio.excecoes.ValidacaoException;
import br.com.lanchonete.produtos.aplicacao.gateways.ProdutoGateway;
import br.com.lanchonete.produtos.dominio.enums.CategoriaProduto;
import br.com.lanchonete.produtos.dominio.entidades.Produto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CriarProdutoTest {

    @Mock
    private ProdutoGateway produtoGateway;

    @InjectMocks
    private CriarProduto criarProduto;

    private String nomeValido;
    private String descricaoValida;
    private BigDecimal precoValido;
    private CategoriaProduto categoriaValida;
    private Produto produtoSalvo;

    @BeforeEach
    void configurar() {
        nomeValido = "X-Bacon";
        descricaoValida = "Hambúrguer com bacon crocante";
        precoValido = new BigDecimal("28.90");
        categoriaValida = CategoriaProduto.LANCHE;

        produtoSalvo = Produto.reconstituir(1L, "X-Bacon",
                "Hambúrguer com bacon crocante", new BigDecimal("28.90"), CategoriaProduto.LANCHE);
    }

    @Test
    @DisplayName("Deve criar produto com sucesso quando os dados são válidos")
    void t1() {
        // Arrange
        when(produtoGateway.existePorNome(nomeValido)).thenReturn(false);
        when(produtoGateway.salvar(any(Produto.class))).thenReturn(produtoSalvo);

        // Act
        Produto produtoRetornado = criarProduto.executar(nomeValido, descricaoValida, precoValido, categoriaValida);

        // Assert
        assertNotNull(produtoRetornado, "O produto retornado não deveria ser nulo");
        assertEquals(1L, produtoRetornado.getId(), "O ID do produto salvo deveria ser 1");
        assertEquals("X-Bacon", produtoRetornado.getNome(), "O nome do produto salvo está incorreto");
        assertEquals(new BigDecimal("28.90"), produtoRetornado.getPreco().getValor(), "O preço do produto salvo está incorreto");
        assertEquals(CategoriaProduto.LANCHE, produtoRetornado.getCategoria(), "A categoria do produto salvo está incorreta");

        verify(produtoGateway).existePorNome(nomeValido);
        verify(produtoGateway).salvar(any(Produto.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar criar produto com nome duplicado")
    void t2() {
        // Arrange
        when(produtoGateway.existePorNome(nomeValido)).thenReturn(true);

        // Act & Assert
        ValidacaoException exception = assertThrows(ValidacaoException.class,
                () -> criarProduto.executar(nomeValido, descricaoValida, precoValido, categoriaValida),
                "Deveria lançar uma exceção para nome duplicado");

        assertEquals("Já existe um produto com este nome", exception.getMessage(),
                "Mensagem da exceção está incorreta");

        verify(produtoGateway).existePorNome(nomeValido);
        verify(produtoGateway, never()).salvar(any(Produto.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar criar produto sem nome")
    void t3() {
        // Arrange
        String nomeVazio = "";

        // Act & Assert
        ValidacaoException exception = assertThrows(ValidacaoException.class,
                () -> criarProduto.executar(nomeVazio, descricaoValida, precoValido, categoriaValida),
                "Deveria lançar uma exceção para nome vazio");

        assertEquals("Nome do produto é obrigatório", exception.getMessage(),
                "Mensagem da exceção está incorreta");

        verify(produtoGateway, never()).salvar(any(Produto.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar criar produto sem preço")
    void t4() {
        // Arrange
        BigDecimal precoNulo = null;

        // Act & Assert
        ValidacaoException exception = assertThrows(ValidacaoException.class,
                () -> criarProduto.executar(nomeValido, descricaoValida, precoNulo, categoriaValida),
                "Deveria lançar uma exceção para preço nulo");

        assertEquals("Preço é obrigatório", exception.getMessage(),
                "Mensagem da exceção está incorreta");

        verify(produtoGateway, never()).salvar(any(Produto.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar criar produto com preço zero ou negativo")
    void t5() {
        // Arrange
        BigDecimal precoZero = BigDecimal.ZERO;

        // Act & Assert
        ValidacaoException exception = assertThrows(ValidacaoException.class,
                () -> criarProduto.executar(nomeValido, descricaoValida, precoZero, categoriaValida),
                "Deveria lançar uma exceção para preço zero");

        assertEquals("Preço deve ser maior que zero", exception.getMessage(),
                "Mensagem da exceção está incorreta");

        // Teste com preço negativo
        BigDecimal precoNegativo = new BigDecimal("-5.00");

        ValidacaoException exception2 = assertThrows(ValidacaoException.class,
                () -> criarProduto.executar(nomeValido, descricaoValida, precoNegativo, categoriaValida),
                "Deveria lançar uma exceção para preço negativo");

        assertEquals("Preço deve ser maior que zero", exception2.getMessage(),
                "Mensagem da exceção está incorreta");

        verify(produtoGateway, never()).salvar(any(Produto.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar criar produto sem categoria")
    void t6() {
        // Arrange
        CategoriaProduto categoriaNula = null;

        // Act & Assert
        ValidacaoException exception = assertThrows(ValidacaoException.class,
                () -> criarProduto.executar(nomeValido, descricaoValida, precoValido, categoriaNula),
                "Deveria lançar uma exceção para categoria nula");

        assertEquals("Categoria do produto é obrigatória", exception.getMessage(),
                "Mensagem da exceção está incorreta");

        verify(produtoGateway, never()).salvar(any(Produto.class));
    }
}