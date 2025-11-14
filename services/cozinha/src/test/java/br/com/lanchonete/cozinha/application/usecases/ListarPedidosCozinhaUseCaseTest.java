package br.com.lanchonete.cozinha.application.usecases;

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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class ListarPedidosCozinhaUseCaseTest {

    @Mock
    private PedidoCozinhaRepository pedidoCozinhaRepository;

    @InjectMocks
    private ListarPedidosCozinhaUseCase listarPedidosCozinhaUseCase;

    private PedidoCozinha pedidoPronto;
    private PedidoCozinha pedidoEmPreparo;
    private PedidoCozinha pedidoAguardando;
    private PedidoCozinha pedidoRetirado;

    @BeforeEach
    void configurar() {
        pedidoPronto = new PedidoCozinha(1L);
        pedidoPronto.setId(1L);
        pedidoPronto.setStatus(StatusPedido.PRONTO);
        pedidoPronto.setDataInicio(LocalDateTime.now().minusMinutes(30));

        pedidoEmPreparo = new PedidoCozinha(2L);
        pedidoEmPreparo.setId(2L);
        pedidoEmPreparo.setStatus(StatusPedido.EM_PREPARO);
        pedidoEmPreparo.setDataInicio(LocalDateTime.now().minusMinutes(20));

        pedidoAguardando = new PedidoCozinha(3L);
        pedidoAguardando.setId(3L);
        pedidoAguardando.setStatus(StatusPedido.AGUARDANDO);
        pedidoAguardando.setDataInicio(LocalDateTime.now().minusMinutes(10));

        pedidoRetirado = new PedidoCozinha(4L);
        pedidoRetirado.setId(4L);
        pedidoRetirado.setStatus(StatusPedido.RETIRADO);
        pedidoRetirado.setDataInicio(LocalDateTime.now().minusMinutes(40));
    }

    @Test
    @DisplayName("Deve listar pedidos excluindo pedidos retirados")
    void t1() {
        when(pedidoCozinhaRepository.findAll())
            .thenReturn(Arrays.asList(pedidoPronto, pedidoEmPreparo, pedidoAguardando, pedidoRetirado));

        List<PedidoCozinha> resultado = listarPedidosCozinhaUseCase.executar();

        assertNotNull(resultado);
        assertEquals(3, resultado.size());
        assertFalse(resultado.contains(pedidoRetirado));
        verify(pedidoCozinhaRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Deve ordenar pedidos por prioridade: AGUARDANDO, EM_PREPARO, PRONTO")
    void t2() {
        when(pedidoCozinhaRepository.findAll())
            .thenReturn(Arrays.asList(pedidoPronto, pedidoEmPreparo, pedidoAguardando));

        List<PedidoCozinha> resultado = listarPedidosCozinhaUseCase.executar();

        assertNotNull(resultado);
        assertEquals(3, resultado.size());
        assertEquals(StatusPedido.AGUARDANDO, resultado.get(0).getStatus());
        assertEquals(StatusPedido.EM_PREPARO, resultado.get(1).getStatus());
        assertEquals(StatusPedido.PRONTO, resultado.get(2).getStatus());
    }

    @Test
    @DisplayName("Deve ordenar por dataInicio do mais antigo para o mais recente quando status for igual")
    void t3() {
        PedidoCozinha pedidoAguardando1 = new PedidoCozinha(10L);
        pedidoAguardando1.setId(10L);
        pedidoAguardando1.setStatus(StatusPedido.AGUARDANDO);
        pedidoAguardando1.setDataInicio(LocalDateTime.now().minusMinutes(5));

        PedidoCozinha pedidoAguardando2 = new PedidoCozinha(11L);
        pedidoAguardando2.setId(11L);
        pedidoAguardando2.setStatus(StatusPedido.AGUARDANDO);
        pedidoAguardando2.setDataInicio(LocalDateTime.now().minusMinutes(15));

        when(pedidoCozinhaRepository.findAll())
            .thenReturn(Arrays.asList(pedidoAguardando1, pedidoAguardando2));

        List<PedidoCozinha> resultado = listarPedidosCozinhaUseCase.executar();

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals(11L, resultado.get(0).getId());
        assertEquals(10L, resultado.get(1).getId());
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não houver pedidos")
    void t4() {
        when(pedidoCozinhaRepository.findAll()).thenReturn(Arrays.asList());

        List<PedidoCozinha> resultado = listarPedidosCozinhaUseCase.executar();

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(pedidoCozinhaRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando todos os pedidos foram retirados")
    void t5() {
        when(pedidoCozinhaRepository.findAll()).thenReturn(Arrays.asList(pedidoRetirado));

        List<PedidoCozinha> resultado = listarPedidosCozinhaUseCase.executar();

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(pedidoCozinhaRepository, times(1)).findAll();
    }
}
