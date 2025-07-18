package br.com.lanchonete.autoatendimento.aplicacao.casosdeuso.produto;

import br.com.lanchonete.autoatendimento.dominio.excecoes.RecursoNaoEncontradoException;
import br.com.lanchonete.autoatendimento.dominio.excecoes.ValidacaoException;
import br.com.lanchonete.autoatendimento.aplicacao.portas.saida.ProdutoGateway;
import br.com.lanchonete.autoatendimento.dominio.modelo.produto.Categoria;
import br.com.lanchonete.autoatendimento.dominio.modelo.produto.Produto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class RemoverProdutoTest {

    @Mock
    private ProdutoGateway produtoGateway;

    @InjectMocks
    private RemoverProduto removerProduto;

    private Produto produtoExistente;

    @BeforeEach
    void configurar() {
        produtoExistente = Produto.reconstituir(
                1L,
                "X-Bacon",
                "Hambúrguer com bacon",
                new BigDecimal("28.90"),
                Categoria.LANCHE);
    }

    @Test
    @DisplayName("Deve remover produto com sucesso quando ID existe")
    void t1() {

        when(produtoGateway.buscarPorId(1L)).thenReturn(Optional.of(produtoExistente));
        doNothing().when(produtoGateway).remover(1L);

        assertDoesNotThrow(() -> removerProduto.executar(1L),
                "Não deveria lançar exceção ao remover produto existente");

        verify(produtoGateway).buscarPorId(1L);
        verify(produtoGateway).remover(1L);
    }

    @Test
    @DisplayName("Deve lançar exceção quando ID é nulo")
    void t2() {

        ValidacaoException exception = assertThrows(ValidacaoException.class,
                () -> removerProduto.executar(null),
                "Deveria lançar exceção para ID nulo");

        assertEquals("ID do produto é obrigatório", exception.getMessage(),
                "Mensagem de erro incorreta");

        verify(produtoGateway, never()).buscarPorId(any());
        verify(produtoGateway, never()).remover(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando produto não existe")
    void t3() {

        when(produtoGateway.buscarPorId(999L)).thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(RecursoNaoEncontradoException.class,
                () -> removerProduto.executar(999L),
                "Deveria lançar exceção quando produto não existe");

        assertEquals("Produto não encontrado", exception.getMessage(),
                "Mensagem de erro incorreta");

        verify(produtoGateway).buscarPorId(999L);
        verify(produtoGateway, never()).remover(any());
    }
}