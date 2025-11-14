package br.com.lanchonete.cozinha.adapters.messaging.consumer;

import br.com.lanchonete.cozinha.adapters.messaging.dto.PagamentoAprovadoEvent;
import br.com.lanchonete.cozinha.domain.model.PedidoCozinha;
import br.com.lanchonete.cozinha.domain.repository.PedidoCozinhaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PagamentoAprovadoConsumerTest {

    @Mock
    private PedidoCozinhaRepository pedidoCozinhaRepository;

    private PagamentoAprovadoConsumer pagamentoAprovadoConsumer;

    @BeforeEach
    void configurar() {
        pagamentoAprovadoConsumer = new PagamentoAprovadoConsumer(pedidoCozinhaRepository);
    }

    @Test
    @DisplayName("Deve adicionar pedido na fila da cozinha quando pagamento for aprovado")
    void t1() {
        Long pedidoId = 123L;
        PagamentoAprovadoEvent event = new PagamentoAprovadoEvent(pedidoId);

        pagamentoAprovadoConsumer.receberPagamentoAprovado(event);

        verify(pedidoCozinhaRepository, times(1)).save(any(PedidoCozinha.class));
    }

    @Test
    @DisplayName("Deve salvar pedido com pedidoId correto")
    void t2() {
        Long pedidoId = 456L;
        PagamentoAprovadoEvent event = new PagamentoAprovadoEvent(pedidoId);

        pagamentoAprovadoConsumer.receberPagamentoAprovado(event);

        verify(pedidoCozinhaRepository).save(
                argThat((PedidoCozinha pedido) -> pedido.getPedidoId().equals(pedidoId))
        );
    }

    @Test
    @DisplayName("Deve chamar repository ao consumir evento")
    void t3() {
        PagamentoAprovadoEvent event = new PagamentoAprovadoEvent(789L);

        pagamentoAprovadoConsumer.receberPagamentoAprovado(event);

        verify(pedidoCozinhaRepository, times(1)).save(any(PedidoCozinha.class));
    }
}
