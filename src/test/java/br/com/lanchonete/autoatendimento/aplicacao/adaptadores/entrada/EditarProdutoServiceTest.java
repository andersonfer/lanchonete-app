package br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada;

import br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada.dto.ProdutoRequestDTO;
import br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada.dto.ProdutoResponseDTO;
import br.com.lanchonete.autoatendimento.aplicacao.excecao.RecursoNaoEncontradoException;
import br.com.lanchonete.autoatendimento.aplicacao.excecao.ValidacaoException;
import br.com.lanchonete.autoatendimento.aplicacao.portas.saida.ProdutoRepositorio;
import br.com.lanchonete.autoatendimento.dominio.Categoria;
import br.com.lanchonete.autoatendimento.dominio.Produto;
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
class EditarProdutoServiceTest {

    @Mock
    private ProdutoRepositorio produtoRepositorio;

    @InjectMocks
    private EditarProdutoService editarProdutoService;

    private ProdutoRequestDTO produtoValido;
    private Produto produtoExistente;
    private Produto produtoAtualizado;

    @BeforeEach
    void configurar() {
        // Produto existente no repositório
        produtoExistente = Produto.builder()
                .id(1L)
                .nome("X-Bacon")
                .descricao("Hambúrguer com bacon")
                .preco(new BigDecimal("28.90"))
                .categoria(Categoria.LANCHE)
                .build();

        // Produto com dados atualizados
        produtoValido = ProdutoRequestDTO.builder()
                .nome("X-Bacon Especial")
                .descricao("Hambúrguer com bacon crocante e molho especial")
                .preco(new BigDecimal("32.90"))
                .categoria(Categoria.LANCHE)
                .build();

        // Produto após atualização
        produtoAtualizado = Produto.builder()
                .id(1L)
                .nome("X-Bacon Especial")
                .descricao("Hambúrguer com bacon crocante e molho especial")
                .preco(new BigDecimal("32.90"))
                .categoria(Categoria.LANCHE)
                .build();
    }

    @Test
    @DisplayName("Deve editar produto com sucesso quando os dados são válidos")
    void t1() {

        when(produtoRepositorio.buscarPorId(1L)).thenReturn(Optional.of(produtoExistente));
        when(produtoRepositorio.atualizar(any(Produto.class))).thenReturn(produtoAtualizado);


        ProdutoResponseDTO response = editarProdutoService.editar(1L, produtoValido);

        assertNotNull(response, "A resposta não deveria ser nula");
        assertEquals(1L, response.getId(), "O ID do produto atualizado deveria ser 1");
        assertEquals("X-Bacon Especial", response.getNome(), "O nome do produto atualizado está incorreto");
        assertEquals(new BigDecimal("32.90"), response.getPreco(), "O preço do produto atualizado está incorreto");
        assertEquals(Categoria.LANCHE, response.getCategoria(), "A categoria do produto atualizado está incorreta");

        verify(produtoRepositorio).buscarPorId(1L);
        verify(produtoRepositorio).atualizar(any(Produto.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando o ID é nulo")
    void t2() {

        ValidacaoException exception = assertThrows(ValidacaoException.class,
                () -> editarProdutoService.editar(null, produtoValido),
                "Deveria lançar uma exceção para ID nulo");

        assertEquals("ID do produto é obrigatório", exception.getMessage(),
                "Mensagem da exceção está incorreta");

        verify(produtoRepositorio, never()).buscarPorId(any());
        verify(produtoRepositorio, never()).atualizar(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando produto não existe")
    void t3() {

        when(produtoRepositorio.buscarPorId(999L)).thenReturn(Optional.empty());


        assertThrows(RecursoNaoEncontradoException.class,
                () -> editarProdutoService.editar(999L, produtoValido),
                "Deveria lançar uma exceção quando o produto não existe");

        verify(produtoRepositorio).buscarPorId(999L);
        verify(produtoRepositorio, never()).atualizar(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando o nome já existe para outro produto")
    void t4() {

        Produto outroProduto = Produto.builder()
                .id(2L)
                .nome("X-Salada")
                .descricao("Hambúrguer com salada")
                .preco(new BigDecimal("26.90"))
                .categoria(Categoria.LANCHE)
                .build();

        when(produtoRepositorio.buscarPorId(2L)).thenReturn(Optional.of(outroProduto));


        ProdutoRequestDTO requestComNomeDuplicado = ProdutoRequestDTO.builder()
                .nome("X-Bacon") // Nome que já existe para o produto com ID 1
                .descricao("Hambúrguer com salada especial")
                .preco(new BigDecimal("27.90"))
                .categoria(Categoria.LANCHE)
                .build();

        when(produtoRepositorio.existePorNome("X-Bacon")).thenReturn(true);


        ValidacaoException exception = assertThrows(ValidacaoException.class,
                () -> editarProdutoService.editar(2L, requestComNomeDuplicado),
                "Deveria lançar uma exceção quando o nome já existe para outro produto");

        assertEquals("Já existe um produto com este nome", exception.getMessage(),
                "Mensagem da exceção está incorreta");

        verify(produtoRepositorio).buscarPorId(2L);
        verify(produtoRepositorio).existePorNome("X-Bacon");
        verify(produtoRepositorio, never()).atualizar(any());
    }

    @Test
    @DisplayName("Deve permitir editar mantendo o mesmo nome para o produto")
    void t5() {

        ProdutoRequestDTO requestMesmoNome = ProdutoRequestDTO.builder()
                .nome("X-Bacon") // Mesmo nome
                .descricao("Hambúrguer com bacon e queijo")
                .preco(new BigDecimal("29.90"))
                .categoria(Categoria.LANCHE)
                .build();

        Produto produtoAtualizadoMesmoNome = Produto.builder()
                .id(1L)
                .nome("X-Bacon")
                .descricao("Hambúrguer com bacon e queijo")
                .preco(new BigDecimal("29.90"))
                .categoria(Categoria.LANCHE)
                .build();

        when(produtoRepositorio.buscarPorId(1L)).thenReturn(Optional.of(produtoExistente));
        when(produtoRepositorio.atualizar(any(Produto.class))).thenReturn(produtoAtualizadoMesmoNome);


        ProdutoResponseDTO response = editarProdutoService.editar(1L, requestMesmoNome);


        assertNotNull(response);
        assertEquals("X-Bacon", response.getNome());
        assertEquals(new BigDecimal("29.90"), response.getPreco());

        verify(produtoRepositorio).buscarPorId(1L);
        verify(produtoRepositorio).atualizar(any(Produto.class));
        verify(produtoRepositorio, never()).existePorNome("X-Bacon");
    }

    @Test
    @DisplayName("Deve lançar exceção para campos inválidos")
    void t6() {

        when(produtoRepositorio.buscarPorId(1L)).thenReturn(Optional.of(produtoExistente));


        ProdutoRequestDTO requestNomeVazio = ProdutoRequestDTO.builder()
                .nome("")
                .descricao("Descrição teste")
                .preco(new BigDecimal("29.90"))
                .categoria(Categoria.LANCHE)
                .build();


        ValidacaoException exceptionNome = assertThrows(ValidacaoException.class,
                () -> editarProdutoService.editar(1L, requestNomeVazio),
                "Deveria lançar uma exceção para nome vazio");

        assertEquals("Nome do produto é obrigatório", exceptionNome.getMessage());


        ProdutoRequestDTO requestPrecoNulo = ProdutoRequestDTO.builder()
                .nome("X-Bacon Especial")
                .descricao("Descrição teste")
                .preco(null)
                .categoria(Categoria.LANCHE)
                .build();


        ValidacaoException exceptionPreco = assertThrows(ValidacaoException.class,
                () -> editarProdutoService.editar(1L, requestPrecoNulo),
                "Deveria lançar uma exceção para preço nulo");

        assertEquals("Preço do produto é obrigatório", exceptionPreco.getMessage());


        ProdutoRequestDTO requestPrecoZero = ProdutoRequestDTO.builder()
                .nome("X-Bacon Especial")
                .descricao("Descrição teste")
                .preco(BigDecimal.ZERO)
                .categoria(Categoria.LANCHE)
                .build();


        ValidacaoException exceptionPrecoZero = assertThrows(ValidacaoException.class,
                () -> editarProdutoService.editar(1L, requestPrecoZero),
                "Deveria lançar uma exceção para preço zero");

        assertEquals("Preço deve ser maior que zero", exceptionPrecoZero.getMessage());


        ProdutoRequestDTO requestCategoriaNula = ProdutoRequestDTO.builder()
                .nome("X-Bacon Especial")
                .descricao("Descrição teste")
                .preco(new BigDecimal("29.90"))
                .categoria(null)
                .build();


        ValidacaoException exceptionCategoria = assertThrows(ValidacaoException.class,
                () -> editarProdutoService.editar(1L, requestCategoriaNula),
                "Deveria lançar uma exceção para categoria nula");

        assertEquals("Categoria do produto é obrigatória", exceptionCategoria.getMessage());


        verify(produtoRepositorio, never()).atualizar(any());
    }
}