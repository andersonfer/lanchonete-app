package br.com.lanchonete.autoatendimento.casosdeuso.produto;

import br.com.lanchonete.autoatendimento.dominio.shared.excecao.RecursoNaoEncontradoException;
import br.com.lanchonete.autoatendimento.dominio.shared.excecao.ValidacaoException;
import br.com.lanchonete.autoatendimento.interfaces.ProdutoGateway;
import br.com.lanchonete.autoatendimento.entidades.produto.Categoria;
import br.com.lanchonete.autoatendimento.entidades.produto.Produto;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class EditarProdutoTest {

    @Mock
    private ProdutoGateway produtoGateway;

    @InjectMocks
    private EditarProduto editarProduto;

    private String nomeNovo;
    private String descricaoNova;
    private BigDecimal precoNovo;
    private Categoria categoriaNova;
    private Produto produtoExistente;
    private Produto produtoAtualizado;

    @BeforeEach
    void configurar() {
        // Produto existente no repositório
        produtoExistente = Produto.reconstituir(1L, "X-Bacon",
                "Hambúrguer com bacon", new BigDecimal("28.90"), Categoria.LANCHE);

        // Dados atualizados
        nomeNovo = "X-Bacon Especial";
        descricaoNova = "Hambúrguer com bacon crocante e molho especial";
        precoNovo = new BigDecimal("32.90");
        categoriaNova = Categoria.LANCHE;

        // Produto após atualização
        produtoAtualizado = Produto.reconstituir(1L, "X-Bacon Especial",
                "Hambúrguer com bacon crocante e molho especial", new BigDecimal("32.90"), Categoria.LANCHE);
    }

    @Test
    @DisplayName("Deve editar produto com sucesso quando os dados são válidos")
    void t1() {

        when(produtoGateway.buscarPorId(1L)).thenReturn(Optional.of(produtoExistente));
        when(produtoGateway.atualizar(any(Produto.class))).thenReturn(produtoAtualizado);

        Produto produtoRetornado = editarProduto.executar(1L, nomeNovo, descricaoNova, precoNovo, categoriaNova);

        assertNotNull(produtoRetornado, "O produto retornado não deveria ser nulo");
        assertEquals(1L, produtoRetornado.getId(), "O ID do produto atualizado deveria ser 1");
        assertEquals("X-Bacon Especial", produtoRetornado.getNome(), "O nome do produto atualizado está incorreto");
        assertEquals(new BigDecimal("32.90"), produtoRetornado.getPreco().getValor(), "O preço do produto atualizado está incorreto");
        assertEquals(Categoria.LANCHE, produtoRetornado.getCategoria(), "A categoria do produto atualizado está incorreta");

        verify(produtoGateway).buscarPorId(1L);
        verify(produtoGateway).atualizar(any(Produto.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando o ID é nulo")
    void t2() {

        ValidacaoException exception = assertThrows(ValidacaoException.class,
                () -> editarProduto.executar(null, nomeNovo, descricaoNova, precoNovo, categoriaNova),
                "Deveria lançar uma exceção para ID nulo");

        assertEquals("ID do produto é obrigatório", exception.getMessage(),
                "Mensagem da exceção está incorreta");

        verify(produtoGateway, never()).buscarPorId(any());
        verify(produtoGateway, never()).atualizar(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando produto não existe")
    void t3() {

        when(produtoGateway.buscarPorId(999L)).thenReturn(Optional.empty());


        assertThrows(RecursoNaoEncontradoException.class,
                () -> editarProduto.executar(999L, nomeNovo, descricaoNova, precoNovo, categoriaNova),
                "Deveria lançar uma exceção quando o produto não existe");

        verify(produtoGateway).buscarPorId(999L);
        verify(produtoGateway, never()).atualizar(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando o nome já existe para outro produto")
    void t4() {

        Produto outroProduto = Produto.reconstituir(2L, "X-Salada",
                "Hambúrguer com salada", new BigDecimal("26.90"), Categoria.LANCHE);

        when(produtoGateway.buscarPorId(2L)).thenReturn(Optional.of(outroProduto));

        // Nome que já existe para o produto com ID 1
        String nomeJaExistente = "X-Bacon";

        when(produtoGateway.existePorNome("X-Bacon")).thenReturn(true);

        ValidacaoException exception = assertThrows(ValidacaoException.class,
                () -> editarProduto.executar(2L, nomeJaExistente, "Hambúrguer com salada especial", new BigDecimal("27.90"), Categoria.LANCHE),
                "Deveria lançar uma exceção quando o nome já existe para outro produto");

        assertEquals("Já existe um produto com este nome", exception.getMessage(),
                "Mensagem da exceção está incorreta");

        verify(produtoGateway).buscarPorId(2L);
        verify(produtoGateway).existePorNome("X-Bacon");
        verify(produtoGateway, never()).atualizar(any());
    }

    @Test
    @DisplayName("Deve permitir editar mantendo o mesmo nome para o produto")
    void t5() {

        // Mesmo nome, só mudando descrição e preço
        String mesmoNome = "X-Bacon";
        String novaDescricao = "Hambúrguer com bacon e queijo";
        BigDecimal novoPreco = new BigDecimal("29.90");

        Produto produtoAtualizadoMesmoNome = Produto.reconstituir(1L, "X-Bacon",
                "Hambúrguer com bacon e queijo", new BigDecimal("29.90"), Categoria.LANCHE);

        when(produtoGateway.buscarPorId(1L)).thenReturn(Optional.of(produtoExistente));
        when(produtoGateway.atualizar(any(Produto.class))).thenReturn(produtoAtualizadoMesmoNome);

        Produto produtoRetornado = editarProduto.executar(1L, mesmoNome, novaDescricao, novoPreco, Categoria.LANCHE);

        assertNotNull(produtoRetornado);
        assertEquals("X-Bacon", produtoRetornado.getNome());
        assertEquals(new BigDecimal("29.90"), produtoRetornado.getPreco().getValor());

        verify(produtoGateway).buscarPorId(1L);
        verify(produtoGateway).atualizar(any(Produto.class));
        verify(produtoGateway, never()).existePorNome("X-Bacon");
    }

    @Test
    @DisplayName("Deve lançar exceção para campos inválidos")
    void t6() {

        when(produtoGateway.buscarPorId(1L)).thenReturn(Optional.of(produtoExistente));

        // Teste nome vazio
        ValidacaoException exceptionNome = assertThrows(ValidacaoException.class,
                () -> editarProduto.executar(1L, "", "Descrição teste", new BigDecimal("29.90"), Categoria.LANCHE),
                "Deveria lançar uma exceção para nome vazio");

        assertEquals("Nome do produto é obrigatório", exceptionNome.getMessage());

        // Teste preço nulo
        ValidacaoException exceptionPreco = assertThrows(ValidacaoException.class,
                () -> editarProduto.executar(1L, "X-Bacon Especial", "Descrição teste", null, Categoria.LANCHE),
                "Deveria lançar uma exceção para preço nulo");

        assertEquals("Preço é obrigatório", exceptionPreco.getMessage());

        // Teste preço zero
        ValidacaoException exceptionPrecoZero = assertThrows(ValidacaoException.class,
                () -> editarProduto.executar(1L, "X-Bacon Especial", "Descrição teste", BigDecimal.ZERO, Categoria.LANCHE),
                "Deveria lançar uma exceção para preço zero");

        assertEquals("Preço deve ser maior que zero", exceptionPrecoZero.getMessage());

        // Teste categoria nula
        ValidacaoException exceptionCategoria = assertThrows(ValidacaoException.class,
                () -> editarProduto.executar(1L, "X-Bacon Especial", "Descrição teste", new BigDecimal("29.90"), null),
                "Deveria lançar uma exceção para categoria nula");

        assertEquals("Categoria do produto é obrigatória", exceptionCategoria.getMessage());


        verify(produtoGateway, never()).atualizar(any());
    }
}