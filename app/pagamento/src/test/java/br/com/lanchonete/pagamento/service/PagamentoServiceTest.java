package br.com.lanchonete.pagamento.service;

import br.com.lanchonete.pagamento.dto.PagamentoRequestDTO;
import br.com.lanchonete.pagamento.dto.PagamentoResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PagamentoServiceTest {

    @Mock
    private WebhookService webhookService;

    private PagamentoService pagamentoService;
    
    private PagamentoRequestDTO requestValido;
    private String pedidoIdValido;
    private BigDecimal valorValido;

    @BeforeEach
    void configurar() {
        pagamentoService = new PagamentoService(webhookService);
        pedidoIdValido = "123";
        valorValido = new BigDecimal("25.50");
        requestValido = new PagamentoRequestDTO(pedidoIdValido, valorValido);
    }

    @Test
    @DisplayName("Deve processar pagamento com sucesso quando dados são válidos")
    void t1() {
        PagamentoResponseDTO response = pagamentoService.processarPagamento(requestValido);

        assertNotNull(response, "Response não deve ser nulo");
        assertEquals(pedidoIdValido, response.pedidoId(), "ID do pedido deve estar correto");
        assertEquals("PENDENTE", response.status(), "Status deve ser PENDENTE");
    }

    @Test
    @DisplayName("Deve iniciar processamento assíncrono do webhook quando processar pagamento")
    void t2() throws InterruptedException {
        pagamentoService.processarPagamento(requestValido);

        Thread.sleep(3500);

        verify(webhookService).enviarWebhook(anyString(), anyString());
    }

    @Test
    @DisplayName("Deve retornar mesmo pedidoId da requisição quando processar pagamento")
    void t3() {
        String pedidoIdEspecifico = "PEDIDO-456";
        PagamentoRequestDTO request = new PagamentoRequestDTO(pedidoIdEspecifico, valorValido);

        PagamentoResponseDTO response = pagamentoService.processarPagamento(request);

        assertEquals(pedidoIdEspecifico, response.pedidoId(), "Deve retornar o mesmo pedidoId da requisição");
    }
}