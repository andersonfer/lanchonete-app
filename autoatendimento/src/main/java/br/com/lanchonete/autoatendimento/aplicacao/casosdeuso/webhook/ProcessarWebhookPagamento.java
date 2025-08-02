package br.com.lanchonete.autoatendimento.aplicacao.casosdeuso.webhook;

import br.com.lanchonete.autoatendimento.aplicacao.gateways.PedidoGateway;
import br.com.lanchonete.autoatendimento.dominio.modelo.pedido.Pedido;
import br.com.lanchonete.autoatendimento.dominio.modelo.pedido.StatusPagamento;
import br.com.lanchonete.autoatendimento.dominio.excecoes.ValidacaoException;
import br.com.lanchonete.autoatendimento.dominio.excecoes.RecursoNaoEncontradoException;

import java.util.Optional;

public class ProcessarWebhookPagamento {
    
    private final PedidoGateway pedidoGateway;
    
    public ProcessarWebhookPagamento(PedidoGateway pedidoGateway) {
        this.pedidoGateway = pedidoGateway;
    }
    
    public void processar(Long pedidoId, StatusPagamento novoStatus) {
        validarParametros(pedidoId, novoStatus);
        
        Optional<Pedido> pedidoOptional = pedidoGateway.buscarPorId(pedidoId);
        
        if (pedidoOptional.isEmpty()) {
            throw new RecursoNaoEncontradoException("Pedido não encontrado: " + pedidoId);
        }
        
        Pedido pedido = pedidoOptional.get();
        
        validarStatusPagamento(pedido, novoStatus);
        
        if (novoStatus == StatusPagamento.APROVADO) {
            pedido.aprovarPagamento();
        } else if (novoStatus == StatusPagamento.REJEITADO) {
            pedido.rejeitarPagamento();
        }
        
        pedidoGateway.atualizarStatusPagamento(pedidoId, novoStatus);
    }
    
    private void validarParametros(Long pedidoId, StatusPagamento novoStatus) {
        if (pedidoId == null) {
            throw new ValidacaoException("PedidoId não pode ser null");
        }
        
        if (novoStatus == null) {
            throw new ValidacaoException("StatusPagamento não pode ser null");
        }
        
        if (novoStatus == StatusPagamento.PENDENTE) {
            throw new ValidacaoException("Status de pagamento inválido: " + novoStatus);
        }
    }
    
    private void validarStatusPagamento(Pedido pedido, StatusPagamento novoStatus) {
        if (pedido.getStatusPagamento().foiProcessado()) {
            throw new ValidacaoException("Pagamento do pedido " + pedido.getId() + " já foi processado");
        }
    }
}