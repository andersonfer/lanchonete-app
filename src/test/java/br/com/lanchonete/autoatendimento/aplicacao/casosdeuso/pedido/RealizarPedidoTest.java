package br.com.lanchonete.autoatendimento.aplicacao.casosdeuso.pedido;

import br.com.lanchonete.autoatendimento.aplicacao.dto.ItemPedidoDTO;
import br.com.lanchonete.autoatendimento.aplicacao.dto.PedidoRequestDTO;
import br.com.lanchonete.autoatendimento.aplicacao.dto.PedidoResponseDTO;
import br.com.lanchonete.autoatendimento.aplicacao.excecao.RecursoNaoEncontradoException;
import br.com.lanchonete.autoatendimento.aplicacao.excecao.ValidacaoException;
import br.com.lanchonete.autoatendimento.aplicacao.portas.saida.ClienteRepositorio;
import br.com.lanchonete.autoatendimento.aplicacao.portas.saida.PedidoRepositorio;
import br.com.lanchonete.autoatendimento.aplicacao.portas.saida.ProdutoRepositorio;
import br.com.lanchonete.autoatendimento.dominio.modelo.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class RealizarPedidoTest {

    @Mock
    private PedidoRepositorio pedidoRepositorio;

    @Mock
    private ClienteRepositorio clienteRepositorio;

    @Mock
    private ProdutoRepositorio produtoRepositorio;

    @InjectMocks
    private RealizarPedido realizarPedido;

    private Cliente cliente;
    private PedidoRequestDTO pedidoComCliente;
    private PedidoRequestDTO pedidoSemCliente;

    @BeforeEach
    void configurar() {
        // Cliente
        cliente = Cliente.criarSemValidacao(1L, "João Silva", "joao@email.com", "12345678901");

        // Produtos
        Produto produto1 = Produto.criarSemValidacao(1L, "X-Bacon", "Hambúrguer com bacon",
                new BigDecimal("25.90"), Categoria.LANCHE);

        Produto produto2 = Produto.criarSemValidacao(2L, "Refrigerante", "Refrigerante lata 350ml",
                new BigDecimal("6.00"), Categoria.BEBIDA);

        // Mock para busca de cliente por CPF
        when(clienteRepositorio.buscarPorCpf("12345678901")).thenReturn(Optional.of(cliente));

        // Mock para busca de produtos por ID
        when(produtoRepositorio.buscarPorId(1L)).thenReturn(Optional.of(produto1));
        when(produtoRepositorio.buscarPorId(2L)).thenReturn(Optional.of(produto2));

        // Mock para salvar pedido (retorna o mesmo objeto)
        when(pedidoRepositorio.salvar(any(Pedido.class))).thenAnswer(i -> {
            Pedido p = i.getArgument(0);
            p.setId(1L);
            return p;
        });

        // Criar pedido request com cliente
        List<ItemPedidoDTO> itensComCliente = Arrays.asList(
                new ItemPedidoDTO(1L, 2),
                new ItemPedidoDTO(2L, 1)
        );
        pedidoComCliente = new PedidoRequestDTO("12345678901", itensComCliente);

        // Criar pedido request sem cliente
        List<ItemPedidoDTO> itensSemCliente = List.of(
                new ItemPedidoDTO(1L, 1)
        );
        pedidoSemCliente = new PedidoRequestDTO(null, itensSemCliente);
    }

    @Test
    @DisplayName("Deve realizar checkout de pedido com cliente com sucesso")
    void t1() {
        // Executar o checkout
        PedidoResponseDTO resposta = realizarPedido.executar(pedidoComCliente);

        // Verificações
        assertNotNull(resposta, "A resposta não deveria ser nula");
        assertEquals(1L, resposta.id(), "O ID do pedido deve ser 1");
        assertEquals(1L, resposta.clienteId(), "O ID do cliente deve ser 1");
        assertEquals("João Silva", resposta.nomeCliente(), "O nome do cliente deve estar correto");
        assertEquals(2, resposta.itens().size(), "O pedido deve ter 2 itens");
        assertEquals(StatusPedido.RECEBIDO, resposta.status(), "O status deve ser RECEBIDO");
        assertEquals(new BigDecimal("57.80"), resposta.valorTotal(),
                "O valor total deve ser 2 * 25.90 + 1 * 6.00 = 57.80");

        // Verificar chamada para salvar pedido
        ArgumentCaptor<Pedido> pedidoCaptor = ArgumentCaptor.forClass(Pedido.class);
        verify(pedidoRepositorio).salvar(pedidoCaptor.capture());

        Pedido pedidoSalvo = pedidoCaptor.getValue();
        assertEquals(cliente, pedidoSalvo.getCliente(), "O cliente deve ser o mesmo");
        assertEquals(StatusPedido.RECEBIDO, pedidoSalvo.getStatus(), "O status deve ser RECEBIDO");
        assertEquals(2, pedidoSalvo.getItens().size(), "O pedido deve ter 2 itens");
    }

    @Test
    @DisplayName("Deve realizar checkout de pedido sem cliente com sucesso")
    void t2() {
        // Executar o checkout
        PedidoResponseDTO resposta = realizarPedido.executar(pedidoSemCliente);

        // Verificações
        assertNotNull(resposta, "A resposta não deveria ser nula");
        assertEquals(1L, resposta.id(), "O ID do pedido deve ser 1");
        assertNull(resposta.clienteId(), "O ID do cliente deve ser nulo");
        assertNull(resposta.nomeCliente(), "O nome do cliente deve ser nulo");
        assertEquals(1, resposta.itens().size(), "O pedido deve ter 1 item");
        assertEquals(StatusPedido.RECEBIDO, resposta.status(), "O status deve ser RECEBIDO");
        assertEquals(new BigDecimal("25.90"), resposta.valorTotal(),
                "O valor total deve ser igual ao preço do produto");

        // Verificar que não buscou cliente
        verify(clienteRepositorio, never()).buscarPorCpf(anyString());
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar checkout com cliente inexistente")
    void t3() {
        // Criar pedido com CPF inexistente
        PedidoRequestDTO pedidoRequestClienteInexistente = new PedidoRequestDTO(
                "99999999999",
                List.of(new ItemPedidoDTO(1L, 1))
        );

        // Mock para retornar cliente não encontrado
        when(clienteRepositorio.buscarPorCpf("99999999999")).thenReturn(Optional.empty());

        // Verificar exceção
        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> realizarPedido.executar(pedidoRequestClienteInexistente),
                "Deveria lançar exceção para cliente não encontrado"
        );

        assertEquals("Cliente não encontrado com o CPF informado", exception.getMessage(),
                "Mensagem de erro incorreta");

        // Verificar que não tentou salvar pedido
        verify(pedidoRepositorio, never()).salvar(any());
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar checkout com produto inexistente")
    void t4() {
        // Criar pedido com produto inexistente
        PedidoRequestDTO pedidoRequestProdutoInexistente = new PedidoRequestDTO(
                null,
                List.of(new ItemPedidoDTO(999L, 1))
        );

        // Mock para retornar produto não encontrado
        when(produtoRepositorio.buscarPorId(999L)).thenReturn(Optional.empty());

        // Verificar exceção
        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> realizarPedido.executar(pedidoRequestProdutoInexistente),
                "Deveria lançar exceção para produto não encontrado"
        );

        assertEquals("Produto não encontrado: 999", exception.getMessage(),
                "Mensagem de erro incorreta");

        // Verificar que não tentou salvar pedido
        verify(pedidoRepositorio, never()).salvar(any());
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar checkout sem itens")
    void t5() {
        // Criar pedido sem itens
        PedidoRequestDTO pedidoRequestSemItens = new PedidoRequestDTO(
                null,
                List.of()
        );

        // Verificar exceção
        ValidacaoException exception = assertThrows(
                ValidacaoException.class,
                () -> realizarPedido.executar(pedidoRequestSemItens),
                "Deveria lançar exceção para pedido sem itens"
        );

        assertEquals("Pedido deve conter pelo menos um item", exception.getMessage(),
                "Mensagem de erro incorreta");

        // Verificar que não tentou salvar pedido
        verify(pedidoRepositorio, never()).salvar(any());
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar checkout com quantidade inválida")
    void t6() {
        // Criar pedido com quantidade zero
        PedidoRequestDTO pedidoRequestQuantidadeZero = new PedidoRequestDTO(
                null,
                List.of(new ItemPedidoDTO(1L, 0))
        );

        // Verificar exceção
        ValidacaoException exception = assertThrows(
                ValidacaoException.class,
                () -> realizarPedido.executar(pedidoRequestQuantidadeZero),
                "Deveria lançar exceção para quantidade zero"
        );

        assertEquals("Quantidade deve ser maior que zero", exception.getMessage(),
                "Mensagem de erro incorreta");

        // Verificar que não tentou salvar pedido
        verify(pedidoRepositorio, never()).salvar(any());
    }
}