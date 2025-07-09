package br.com.lanchonete.pagamento.service;

import br.com.lanchonete.pagamento.dto.WebhookRequestDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.reactive.function.client.WebClient;

public class WebhookService {

    private final WebClient webClient;
    private final String webhookUrl;

    public WebhookService(final WebClient webClient, final String webhookUrl) {
        this.webClient = webClient;
        this.webhookUrl = webhookUrl;
    }

    public void enviarWebhook(String pedidoId, String status) {
        try {
            WebhookRequestDTO request = new WebhookRequestDTO(Long.valueOf(pedidoId), status);
            
            webClient.post()
                    .uri(webhookUrl)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
                    
        } catch (Exception e) {
            System.err.println("Erro ao enviar webhook: " + e.getMessage());
        }
    }
}