package br.com.lanchonete.pagamento.service;

import br.com.lanchonete.pagamento.dto.PagamentoRequestDTO;
import br.com.lanchonete.pagamento.dto.PagamentoResponseDTO;
import org.springframework.scheduling.annotation.Async;

public class PagamentoService {

    private final WebhookService webhookService;

    public PagamentoService(final WebhookService webhookService) {
        this.webhookService = webhookService;
    }

    public PagamentoResponseDTO processarPagamento(PagamentoRequestDTO request) {
        // Resposta imediata com status PENDENTE
        PagamentoResponseDTO response = new PagamentoResponseDTO(request.pedidoId(), "PENDENTE");
        
        // Processar webhook assíncrono
        processarWebhookAssincrono(request.pedidoId());
        
        return response;
    }

    @Async
    public void processarWebhookAssincrono(String pedidoId) {
        try {
            // Aguardar exatamente 3 segundos
            Thread.sleep(3000);
            
            // Determinar status (80% aprovação, 20% rejeição)
            String status = Math.random() < 0.8 ? "APROVADO" : "REJEITADO";
            
            // Chamar webhook
            webhookService.enviarWebhook(pedidoId, status);
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}