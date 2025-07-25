package br.com.lanchonete.autoatendimento.adaptadores.rest.servicos;

import br.com.lanchonete.autoatendimento.adaptadores.rest.dto.ItemPedidoRequestDTO;
import br.com.lanchonete.autoatendimento.adaptadores.rest.dto.ItemPedidoResponseDTO;
import br.com.lanchonete.autoatendimento.adaptadores.rest.dto.PedidoRequestDTO;
import br.com.lanchonete.autoatendimento.adaptadores.rest.dto.PedidoResponseDTO;
import br.com.lanchonete.autoatendimento.adaptadores.rest.dto.StatusPedidoDTO;
import br.com.lanchonete.autoatendimento.adaptadores.rest.dto.StatusPagamentoDTO;
import br.com.lanchonete.autoatendimento.adaptadores.rest.mappers.PedidoMapper;
import br.com.lanchonete.autoatendimento.dominio.excecoes.ValidacaoException;
import br.com.lanchonete.autoatendimento.dominio.excecoes.RecursoNaoEncontradoException;
import br.com.lanchonete.autoatendimento.aplicacao.casosdeuso.pedido.RealizarPedido;
import br.com.lanchonete.autoatendimento.aplicacao.casosdeuso.pedido.ListarPedidos;
import br.com.lanchonete.autoatendimento.dominio.modelo.pedido.StatusPedido;
import br.com.lanchonete.autoatendimento.dominio.modelo.pedido.Pedido;
import br.com.lanchonete.autoatendimento.dominio.modelo.cliente.Cliente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock
    private RealizarPedido realizarPedido;

    @Mock
    private ListarPedidos listarPedidos;

    @Mock
    private PedidoMapper pedidoMapper;

    @InjectMocks
    private PedidoService pedidoService;

    private PedidoRequestDTO pedidoRequest;
    private PedidoResponseDTO pedidoResponse;
    private Pedido pedido;

    @BeforeEach
    void configurar() {
        List<ItemPedidoRequestDTO> itens = Arrays.asList(
                new ItemPedidoRequestDTO(1L, 2),
                new ItemPedidoRequestDTO(2L, 1)
        );
        pedidoRequest = new PedidoRequestDTO("12345678901", itens);

        List<ItemPedidoResponseDTO> itensResponse = Arrays.asList(
                new ItemPedidoResponseDTO(1L, 1L, "X-Bacon", "Hambúrguer com bacon", 2,
                        new BigDecimal("25.90"), new BigDecimal("51.80")),
                new ItemPedidoResponseDTO(2L, 2L, "Refrigerante", "Refrigerante lata 350ml", 1,
                        new BigDecimal("6.00"), new BigDecimal("6.00"))
        );

        pedidoResponse = new PedidoResponseDTO(
                1L,
                "PED000001",
                1L,
                "João Silva",
                itensResponse,
                StatusPedidoDTO.RECEBIDO,
                StatusPagamentoDTO.PENDENTE,
                LocalDateTime.now(),
                new BigDecimal("57.80")
        );

        // Criar entidade Pedido para os testes do UC
        Cliente cliente = Cliente.reconstituir(1L, "João Silva", "joao@email.com", "12345678901");
        pedido = Pedido.criar(cliente, StatusPedido.RECEBIDO, LocalDateTime.now());
        pedido.setId(1L); // This sets the numeroPedido automatically
        pedido.setValorTotal(new BigDecimal("57.80"));
    }

    @Test
    @DisplayName("Deve realizar checkout com sucesso quando use case executa corretamente")
    void t1() {
        when(realizarPedido.executar(anyString(), anyList())).thenReturn(pedido);
        when(pedidoMapper.paraDTO(pedido)).thenReturn(pedidoResponse);

        PedidoResponseDTO resultado = pedidoService.realizarCheckout(pedidoRequest);

        assertNotNull(resultado);
        assertEquals(pedidoResponse, resultado);
    }

    @Test
    @DisplayName("Deve propagar exceção quando use case lança ValidacaoException")
    void t2() {
        when(realizarPedido.executar(anyString(), anyList()))
                .thenThrow(new ValidacaoException("Pedido deve conter pelo menos um item"));

        assertThrows(ValidacaoException.class, () -> {
            pedidoService.realizarCheckout(pedidoRequest);
        });
    }

    @Test
    @DisplayName("Deve propagar exceção quando use case lança RecursoNaoEncontradoException")
    void t3() {
        when(realizarPedido.executar(anyString(), anyList()))
                .thenThrow(new RecursoNaoEncontradoException("Cliente não encontrado"));

        assertThrows(RecursoNaoEncontradoException.class, () -> {
            pedidoService.realizarCheckout(pedidoRequest);
        });
    }

    @Test
    @DisplayName("Deve listar pedidos com sucesso quando use case retorna lista")
    void t4() {
        List<Pedido> pedidos = Arrays.asList(pedido);
        when(listarPedidos.executar()).thenReturn(pedidos);
        when(pedidoMapper.paraDTO(pedido)).thenReturn(pedidoResponse);

        List<PedidoResponseDTO> resultado = pedidoService.listarPedidos();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(pedidoResponse, resultado.get(0));
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando use case retorna lista vazia")
    void t5() {
        when(listarPedidos.executar()).thenReturn(Collections.emptyList());

        List<PedidoResponseDTO> resultado = pedidoService.listarPedidos();

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }
}