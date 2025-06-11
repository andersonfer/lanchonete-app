package br.com.lanchonete.autoatendimento.adaptadores;

import br.com.lanchonete.autoatendimento.controllers.dto.ItemPedidoDTO;
import br.com.lanchonete.autoatendimento.controllers.dto.ItemPedidoResponseDTO;
import br.com.lanchonete.autoatendimento.controllers.dto.PedidoRequestDTO;
import br.com.lanchonete.autoatendimento.controllers.dto.PedidoResponseDTO;
import br.com.lanchonete.autoatendimento.dominio.shared.excecao.ValidacaoException;
import br.com.lanchonete.autoatendimento.dominio.shared.excecao.RecursoNaoEncontradoException;
import br.com.lanchonete.autoatendimento.casosdeuso.pedido.RealizarPedido;
import br.com.lanchonete.autoatendimento.casosdeuso.pedido.ListarPedidos;
import br.com.lanchonete.autoatendimento.entidades.pedido.StatusPedido;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PedidoAdaptadorTest {

    @Mock
    private RealizarPedido realizarPedido;

    @Mock
    private ListarPedidos listarPedidos;

    @InjectMocks
    private PedidoAdaptador pedidoAdaptador;

    private PedidoRequestDTO pedidoRequest;
    private PedidoResponseDTO pedidoResponse;

    @BeforeEach
    void configurar() {
        List<ItemPedidoDTO> itens = Arrays.asList(
                new ItemPedidoDTO(1L, 2),
                new ItemPedidoDTO(2L, 1)
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
                StatusPedido.RECEBIDO,
                LocalDateTime.now(),
                new BigDecimal("57.80")
        );
    }

    @Test
    @DisplayName("Deve realizar checkout com sucesso quando use case executa corretamente")
    void t1() {
        when(realizarPedido.executar(any(PedidoRequestDTO.class))).thenReturn(pedidoResponse);

        PedidoResponseDTO resultado = pedidoAdaptador.realizarCheckout(pedidoRequest);

        assertNotNull(resultado);
        assertEquals(1L, resultado.id());
        assertEquals(1L, resultado.clienteId());
        assertEquals("João Silva", resultado.nomeCliente());
        assertEquals(StatusPedido.RECEBIDO, resultado.status());
        assertEquals(new BigDecimal("57.80"), resultado.valorTotal());
        assertEquals(2, resultado.itens().size());
    }

    @Test
    @DisplayName("Deve propagar exceção quando use case lança ValidacaoException")
    void t2() {
        when(realizarPedido.executar(any(PedidoRequestDTO.class)))
                .thenThrow(new ValidacaoException("Pedido deve conter pelo menos um item"));

        assertThrows(ValidacaoException.class, () -> {
            pedidoAdaptador.realizarCheckout(pedidoRequest);
        });
    }

    @Test
    @DisplayName("Deve propagar exceção quando use case lança RecursoNaoEncontradoException")
    void t3() {
        when(realizarPedido.executar(any(PedidoRequestDTO.class)))
                .thenThrow(new RecursoNaoEncontradoException("Cliente não encontrado"));

        assertThrows(RecursoNaoEncontradoException.class, () -> {
            pedidoAdaptador.realizarCheckout(pedidoRequest);
        });
    }

    @Test
    @DisplayName("Deve listar pedidos com sucesso quando use case retorna lista")
    void t4() {
        List<PedidoResponseDTO> pedidos = Arrays.asList(pedidoResponse);
        when(listarPedidos.executar()).thenReturn(pedidos);

        List<PedidoResponseDTO> resultado = pedidoAdaptador.listarPedidos();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(pedidoResponse, resultado.get(0));
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando use case retorna lista vazia")
    void t5() {
        when(listarPedidos.executar()).thenReturn(Collections.emptyList());

        List<PedidoResponseDTO> resultado = pedidoAdaptador.listarPedidos();

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }
}