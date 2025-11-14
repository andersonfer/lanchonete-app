package br.com.lanchonete.cozinha.application.usecases;

import br.com.lanchonete.cozinha.domain.exception.PedidoNaoEncontradoException;
import br.com.lanchonete.cozinha.domain.model.PedidoCozinha;
import br.com.lanchonete.cozinha.domain.model.StatusPedido;
import br.com.lanchonete.cozinha.domain.repository.PedidoCozinhaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class IniciarPreparoPedidoUseCaseTest {

    @Mock
    private PedidoCozinhaRepository pedidoCozinhaRepository;

    @InjectMocks
    private IniciarPreparoPedidoUseCase iniciarPreparoPedidoUseCase;

    private PedidoCozinha pedidoAguardando;

    @BeforeEach
    void configurar() {
        pedidoAguardando = new PedidoCozinha(1L);
        pedidoAguardando.setId(1L);
        pedidoAguardando.setStatus(StatusPedido.AGUARDANDO);
    }

    @Test
    @DisplayName("Deve iniciar preparo de pedido com sucesso")
    void t1() {
        when(pedidoCozinhaRepository.findById(1L)).thenReturn(Optional.of(pedidoAguardando));
        when(pedidoCozinhaRepository.save(any(PedidoCozinha.class))).thenReturn(pedidoAguardando);

        PedidoCozinha resultado = iniciarPreparoPedidoUseCase.executar(1L);

        assertNotNull(resultado);
        assertEquals(StatusPedido.EM_PREPARO, resultado.getStatus());
        verify(pedidoCozinhaRepository, times(1)).findById(1L);
        verify(pedidoCozinhaRepository, times(1)).save(pedidoAguardando);
    }

    @Test
    @DisplayName("Deve lançar exceção quando pedido não for encontrado")
    void t2() {
        when(pedidoCozinhaRepository.findById(999L)).thenReturn(Optional.empty());

        PedidoNaoEncontradoException exception = assertThrows(
            PedidoNaoEncontradoException.class,
            () -> iniciarPreparoPedidoUseCase.executar(999L)
        );

        assertNotNull(exception);
        verify(pedidoCozinhaRepository, times(1)).findById(999L);
        verify(pedidoCozinhaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando pedido não estiver em status AGUARDANDO")
    void t3() {
        PedidoCozinha pedidoEmPreparo = new PedidoCozinha(1L);
        pedidoEmPreparo.setId(1L);
        pedidoEmPreparo.setStatus(StatusPedido.EM_PREPARO);

        when(pedidoCozinhaRepository.findById(1L)).thenReturn(Optional.of(pedidoEmPreparo));

        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> iniciarPreparoPedidoUseCase.executar(1L)
        );

        assertEquals("Pedido não está aguardando preparo", exception.getMessage());
        verify(pedidoCozinhaRepository, times(1)).findById(1L);
        verify(pedidoCozinhaRepository, never()).save(any());
    }
}
