package br.com.lanchonete.pagamento.adapters.web.service;

import br.com.lanchonete.pagamento.adapters.web.dto.PagamentoRequest;
import br.com.lanchonete.pagamento.adapters.web.dto.PagamentoResponse;
import br.com.lanchonete.pagamento.application.usecases.ProcessarPagamento;
import br.com.lanchonete.pagamento.domain.model.Pagamento;
import org.springframework.stereotype.Service;

@Service
public class PagamentoService {

    private final ProcessarPagamento processarPagamento;

    public PagamentoService(ProcessarPagamento processarPagamento) {
        this.processarPagamento = processarPagamento;
    }

    public PagamentoResponse processar(PagamentoRequest request) {
        Pagamento pagamento = processarPagamento.executar(request.pedidoId(), request.valor());
        return paraResponse(pagamento);
    }

    private PagamentoResponse paraResponse(Pagamento pagamento) {
        return new PagamentoResponse(
                pagamento.getId(),
                pagamento.getPedidoId(),
                pagamento.getStatus().name()
        );
    }
}
