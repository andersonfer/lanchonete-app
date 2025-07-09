package br.com.lanchonete.autoatendimento.adaptadores.rest.controllers;

import br.com.lanchonete.autoatendimento.adaptadores.rest.dto.WebhookPagamentoDTO;
import br.com.lanchonete.autoatendimento.adaptadores.rest.servicos.WebhookService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/webhook")
public class WebhookController {
    
    private final WebhookService webhookService;
    
    public WebhookController(WebhookService webhookService) {
        this.webhookService = webhookService;
    }
    
    @PostMapping("/pagamento")
    public ResponseEntity<Void> receberWebhookPagamento(@RequestBody WebhookPagamentoDTO webhookRequest) {
        webhookService.processarWebhookPagamento(webhookRequest);
        return ResponseEntity.ok().build();
    }
}