package br.com.lanchonete.autoatendimento.adaptadores.rest.servicos;

import br.com.lanchonete.autoatendimento.adaptadores.rest.dto.WebhookPagamentoDTO;
import br.com.lanchonete.autoatendimento.aplicacao.casosdeuso.webhook.ProcessarWebhookPagamento;
import br.com.lanchonete.autoatendimento.dominio.modelo.pedido.StatusPagamento;
import br.com.lanchonete.autoatendimento.dominio.excecoes.ValidacaoException;
import org.springframework.stereotype.Service;

@Service
public class WebhookService {
    
    private final ProcessarWebhookPagamento processarWebhookPagamento;
    
    public WebhookService(ProcessarWebhookPagamento processarWebhookPagamento) {
        this.processarWebhookPagamento = processarWebhookPagamento;
    }
    
    public void processarWebhookPagamento(WebhookPagamentoDTO webhookRequest) {
        StatusPagamento statusPagamento = converterStringParaStatus(webhookRequest.statusPagamento());
        
        processarWebhookPagamento.processar(webhookRequest.pedidoId(), statusPagamento);
    }
    
    private StatusPagamento converterStringParaStatus(String statusString) {
        if (statusString == null) {
            throw new ValidacaoException("StatusPagamento não pode ser null");
        }
        
        try {
            return StatusPagamento.valueOf(statusString.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidacaoException("Status de pagamento inválido: " + statusString);
        }
    }
}