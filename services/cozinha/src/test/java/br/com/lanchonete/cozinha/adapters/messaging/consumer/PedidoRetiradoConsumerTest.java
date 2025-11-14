package br.com.lanchonete.cozinha.adapters.messaging.consumer;

import br.com.lanchonete.cozinha.adapters.messaging.dto.PedidoRetiradoEvent;
import br.com.lanchonete.cozinha.domain.model.PedidoCozinha;
import br.com.lanchonete.cozinha.domain.model.StatusPedido;
import br.com.lanchonete.cozinha.domain.repository.PedidoCozinhaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PedidoRetiradoConsumerTest {

    @Mock
    private PedidoCozinhaRepository pedidoCozinhaRepository;

    private PedidoRetiradoConsumer pedidoRetiradoConsumer;

    @BeforeEach
    void configurar() {
        pedidoRetiradoConsumer = new PedidoRetiradoConsumer(pedidoCozinhaRepository);
    }

    @Test
    @DisplayName("Deve marcar pedido como retirado quando evento for recebido")
    void t1() {
        Long pedidoId = 123L;
        PedidoCozinha pedidoPronto = new PedidoCozinha(pedidoId);
        pedidoPronto.setId(1L);
        pedidoPronto.setStatus(StatusPedido.PRONTO);
        PedidoRetiradoEvent event = new PedidoRetiradoEvent(pedidoId);

        when(pedidoCozinhaRepository.findByPedidoId(pedidoId)).thenReturn(Optional.of(pedidoPronto));

        pedidoRetiradoConsumer.receberPedidoRetirado(event);

        verify(pedidoCozinhaRepository, times(1)).findByPedidoId(pedidoId);
        verify(pedidoCozinhaRepository, times(1)).save(any(PedidoCozinha.class));
    }

    @Test
    @DisplayName("Deve buscar pedido por pedidoId correto")
    void t2() {
        Long pedidoId = 456L;
        PedidoCozinha pedidoPronto = new PedidoCozinha(pedidoId);
        pedidoPronto.setStatus(StatusPedido.PRONTO);
        PedidoRetiradoEvent event = new PedidoRetiradoEvent(pedidoId);

        when(pedidoCozinhaRepository.findByPedidoId(pedidoId)).thenReturn(Optional.of(pedidoPronto));

        pedidoRetiradoConsumer.receberPedidoRetirado(event);

        verify(pedidoCozinhaRepository).findByPedidoId(pedidoId);
    }

    @Test
    @DisplayName("Deve chamar repository ao consumir evento")
    void t3() {
        Long pedidoId = 789L;
        PedidoCozinha pedidoPronto = new PedidoCozinha(pedidoId);
        pedidoPronto.setStatus(StatusPedido.PRONTO);
        PedidoRetiradoEvent event = new PedidoRetiradoEvent(pedidoId);

        when(pedidoCozinhaRepository.findByPedidoId(anyLong())).thenReturn(Optional.of(pedidoPronto));

        pedidoRetiradoConsumer.receberPedidoRetirado(event);

        verify(pedidoCozinhaRepository, times(1)).findByPedidoId(anyLong());
        verify(pedidoCozinhaRepository, times(1)).save(any(PedidoCozinha.class));
    }
}
