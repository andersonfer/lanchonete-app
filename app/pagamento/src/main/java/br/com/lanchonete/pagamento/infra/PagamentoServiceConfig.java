package br.com.lanchonete.pagamento.infra;

import br.com.lanchonete.pagamento.service.PagamentoService;
import br.com.lanchonete.pagamento.service.WebhookService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class PagamentoServiceConfig {

    @Bean
    WebhookService webhookService(final WebClient webClient, 
                                  @Value("${mock.webhook.url}") final String webhookUrl) {
        return new WebhookService(webClient, webhookUrl);
    }

    @Bean
    PagamentoService pagamentoService(final WebhookService webhookService) {
        return new PagamentoService(webhookService);
    }
}