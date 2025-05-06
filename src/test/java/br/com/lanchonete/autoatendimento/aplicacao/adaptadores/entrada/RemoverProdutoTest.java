package br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada;

import br.com.lanchonete.autoatendimento.aplicacao.casosdeuso.produto.RemoverProduto;
import br.com.lanchonete.autoatendimento.aplicacao.excecao.RecursoNaoEncontradoException;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class RemoverProdutoTest {

    @Mock
    private ProdutoRepositorio produtoRepositorio;

    @InjectMocks
    private RemoverProduto removerProduto;

    private Produto produtoExistente;

    @BeforeEach
    void configurar() {
        produtoExistente = Produto.builder()
                .id(1L)
                .nome("X-Bacon")
                .descricao("Hambúrguer com bacon")
                .preco(new BigDecimal("28.90"))
                .categoria(Categoria.LANCHE)
                .build();
    }

    @Test
    @DisplayName("Deve remover produto com sucesso quando ID existe")
    void t1() {

        when(produtoRepositorio.buscarPorId(1L)).thenReturn(Optional.of(produtoExistente));
        doNothing().when(produtoRepositorio).remover(1L);

        assertDoesNotThrow(() -> removerProduto.executar(1L),
                "Não deveria lançar exceção ao remover produto existente");

        verify(produtoRepositorio).buscarPorId(1L);
        verify(produtoRepositorio).remover(1L);
    }

    @Test
    @DisplayName("Deve lançar exceção quando ID é nulo")
    void t2() {

        ValidacaoException exception = assertThrows(ValidacaoException.class,
                () -> removerProduto.executar(null),
                "Deveria lançar exceção para ID nulo");

        assertEquals("ID do produto é obrigatório", exception.getMessage(),
                "Mensagem de erro incorreta");

        verify(produtoRepositorio, never()).buscarPorId(any());
        verify(produtoRepositorio, never()).remover(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando produto não existe")
    void t3() {

        when(produtoRepositorio.buscarPorId(999L)).thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(RecursoNaoEncontradoException.class,
                () -> removerProduto.executar(999L),
                "Deveria lançar exceção quando produto não existe");

        assertEquals("Produto não encontrado", exception.getMessage(),
                "Mensagem de erro incorreta");

        verify(produtoRepositorio).buscarPorId(999L);
        verify(produtoRepositorio, never()).remover(any());
    }
}