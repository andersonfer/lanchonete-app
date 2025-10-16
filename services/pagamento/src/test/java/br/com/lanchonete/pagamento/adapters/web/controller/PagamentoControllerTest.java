package br.com.lanchonete.pagamento.adapters.web.controller;

import br.com.lanchonete.pagamento.adapters.web.dto.PagamentoRequest;
import br.com.lanchonete.pagamento.adapters.web.dto.PagamentoResponse;
import br.com.lanchonete.pagamento.adapters.web.service.PagamentoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PagamentoControllerTest {

    @Mock
    private PagamentoService pagamentoService;

    @Test
    @DisplayName("Deve processar pagamento e retornar status 200 OK")
    void t1() {
        PagamentoController controller = new PagamentoController(pagamentoService);
        PagamentoRequest request = new PagamentoRequest(123L, new BigDecimal("50.00"));
        PagamentoResponse responseMock = new PagamentoResponse("507f1f77bcf86cd799439011", 123L, "APROVADO");

        when(pagamentoService.processar(any(PagamentoRequest.class))).thenReturn(responseMock);

        ResponseEntity<PagamentoResponse> response = controller.processar(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(pagamentoService, times(1)).processar(request);
    }

    @Test
    @DisplayName("Deve retornar response com dados corretos")
    void t2() {
        PagamentoController controller = new PagamentoController(pagamentoService);
        PagamentoRequest request = new PagamentoRequest(456L, new BigDecimal("100.00"));
        PagamentoResponse responseMock = new PagamentoResponse("507f1f77bcf86cd799439012", 456L, "REJEITADO");

        when(pagamentoService.processar(any(PagamentoRequest.class))).thenReturn(responseMock);

        ResponseEntity<PagamentoResponse> response = controller.processar(request);

        assertEquals("507f1f77bcf86cd799439012", response.getBody().id());
        assertEquals(456L, response.getBody().pedidoId());
        assertEquals("REJEITADO", response.getBody().status());
    }

    @Test
    @DisplayName("Deve chamar service com request correto")
    void t3() {
        PagamentoController controller = new PagamentoController(pagamentoService);
        PagamentoRequest request = new PagamentoRequest(789L, new BigDecimal("75.50"));
        PagamentoResponse responseMock = new PagamentoResponse("507f1f77bcf86cd799439013", 789L, "APROVADO");

        when(pagamentoService.processar(request)).thenReturn(responseMock);

        controller.processar(request);

        verify(pagamentoService).processar(request);
    }
}
