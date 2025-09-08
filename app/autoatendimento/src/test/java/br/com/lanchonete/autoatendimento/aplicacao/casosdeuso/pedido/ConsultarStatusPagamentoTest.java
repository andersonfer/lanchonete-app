package br.com.lanchonete.autoatendimento.aplicacao.casosdeuso.pedido;

import br.com.lanchonete.autoatendimento.aplicacao.gateways.PedidoGateway;
import br.com.lanchonete.autoatendimento.dominio.excecoes.RecursoNaoEncontradoException;
import br.com.lanchonete.autoatendimento.dominio.modelo.pedido.Pedido;
import br.com.lanchonete.autoatendimento.dominio.modelo.pedido.StatusPagamento;
import br.com.lanchonete.autoatendimento.dominio.modelo.pedido.StatusPedido;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsultarStatusPagamentoTest {

    @Mock
    private PedidoGateway pedidoGateway;

    private ConsultarStatusPagamento consultarStatusPagamento;

    @BeforeEach
    void configurar() {
        consultarStatusPagamento = new ConsultarStatusPagamento(pedidoGateway);
    }

    @Test
    @DisplayName("Deve retornar status PENDENTE quando pedido existir com pagamento pendente")
    void t1() {
        // Criar pedido com status de pagamento PENDENTE
        Pedido pedido = Pedido.criar(null, StatusPedido.RECEBIDO, LocalDateTime.now());
        pedido.setId(1L);
        
        // Mock do gateway
        when(pedidoGateway.buscarPorId(1L)).thenReturn(Optional.of(pedido));

        // Executar caso de uso
        StatusPagamento resultado = consultarStatusPagamento.executar(1L);

        // Verificações
        assertEquals(StatusPagamento.PENDENTE, resultado);
        verify(pedidoGateway).buscarPorId(1L);
    }

    @Test
    @DisplayName("Deve retornar status APROVADO quando pedido existir com pagamento aprovado")
    void t2() {
        // Criar pedido com status de pagamento APROVADO
        Pedido pedido = Pedido.criar(null, StatusPedido.RECEBIDO, LocalDateTime.now());
        pedido.setId(2L);
        pedido.aprovarPagamento();
        
        // Mock do gateway
        when(pedidoGateway.buscarPorId(2L)).thenReturn(Optional.of(pedido));

        // Executar caso de uso
        StatusPagamento resultado = consultarStatusPagamento.executar(2L);

        // Verificações
        assertEquals(StatusPagamento.APROVADO, resultado);
        verify(pedidoGateway).buscarPorId(2L);
    }

    @Test
    @DisplayName("Deve retornar status REJEITADO quando pedido existir com pagamento rejeitado")
    void t3() {
        // Criar pedido com status de pagamento REJEITADO
        Pedido pedido = Pedido.criar(null, StatusPedido.RECEBIDO, LocalDateTime.now());
        pedido.setId(3L);
        pedido.rejeitarPagamento();
        
        // Mock do gateway
        when(pedidoGateway.buscarPorId(3L)).thenReturn(Optional.of(pedido));

        // Executar caso de uso
        StatusPagamento resultado = consultarStatusPagamento.executar(3L);

        // Verificações
        assertEquals(StatusPagamento.REJEITADO, resultado);
        verify(pedidoGateway).buscarPorId(3L);
    }

    @Test
    @DisplayName("Deve lançar exceção quando pedido não existir")
    void t4() {
        // Mock do gateway retornando vazio
        when(pedidoGateway.buscarPorId(999L)).thenReturn(Optional.empty());

        // Executar caso de uso e verificar exceção
        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> consultarStatusPagamento.executar(999L)
        );

        // Verificações
        assertEquals("Pedido não encontrado com ID: 999", exception.getMessage());
        verify(pedidoGateway).buscarPorId(999L);
    }

    @Test
    @DisplayName("Deve chamar gateway apenas uma vez por execução")
    void t5() {
        // Criar pedido
        Pedido pedido = Pedido.criar(null, StatusPedido.RECEBIDO, LocalDateTime.now());
        pedido.setId(1L);
        
        // Mock do gateway
        when(pedidoGateway.buscarPorId(1L)).thenReturn(Optional.of(pedido));

        // Executar caso de uso
        consultarStatusPagamento.executar(1L);

        // Verificar que foi chamado apenas uma vez
        verify(pedidoGateway, times(1)).buscarPorId(1L);
        verifyNoMoreInteractions(pedidoGateway);
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar consultar com ID nulo")
    void t6() {
        // Executar caso de uso com ID nulo e verificar exceção
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> consultarStatusPagamento.executar(null)
        );

        // Verificações
        assertEquals("ID do pedido não pode ser nulo", exception.getMessage());
        verifyNoInteractions(pedidoGateway);
    }

    @Test
    @DisplayName("Deve funcionar corretamente com diferentes IDs válidos")
    void t7() {
        // Criar pedidos diferentes
        Pedido pedido1 = Pedido.criar(null, StatusPedido.RECEBIDO, LocalDateTime.now());
        pedido1.setId(100L);
        
        Pedido pedido2 = Pedido.criar(null, StatusPedido.EM_PREPARACAO, LocalDateTime.now());
        pedido2.setId(200L);
        pedido2.aprovarPagamento();
        
        // Mock do gateway
        when(pedidoGateway.buscarPorId(100L)).thenReturn(Optional.of(pedido1));
        when(pedidoGateway.buscarPorId(200L)).thenReturn(Optional.of(pedido2));

        // Executar casos de uso
        StatusPagamento resultado1 = consultarStatusPagamento.executar(100L);
        StatusPagamento resultado2 = consultarStatusPagamento.executar(200L);

        // Verificações
        assertEquals(StatusPagamento.PENDENTE, resultado1);
        assertEquals(StatusPagamento.APROVADO, resultado2);
        verify(pedidoGateway).buscarPorId(100L);
        verify(pedidoGateway).buscarPorId(200L);
    }
}