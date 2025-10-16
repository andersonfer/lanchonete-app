package br.com.lanchonete.pagamento.adapters.persistence;

import br.com.lanchonete.pagamento.application.gateways.PagamentoGateway;
import br.com.lanchonete.pagamento.domain.model.Pagamento;
import br.com.lanchonete.pagamento.domain.model.StatusPagamento;
import br.com.lanchonete.pagamento.domain.model.Valor;

public class PagamentoGatewayMongo implements PagamentoGateway {

    private final PagamentoRepository repository;

    public PagamentoGatewayMongo(PagamentoRepository repository) {
        this.repository = repository;
    }

    @Override
    public Pagamento salvar(Pagamento pagamento) {
        PagamentoDocument document = paraDocument(pagamento);
        PagamentoDocument salvo = repository.save(document);
        return paraDominio(salvo);
    }

    private PagamentoDocument paraDocument(Pagamento pagamento) {
        return new PagamentoDocument(
                pagamento.getPedidoId(),
                pagamento.getValor(),
                pagamento.getStatus(),
                pagamento.getDataCriacao()
        );
    }

    private Pagamento paraDominio(PagamentoDocument document) {
        Valor valor = new Valor(document.getValor());
        StatusPagamento status = StatusPagamento.valueOf(document.getStatus());

        return new Pagamento(
                document.getId(),
                document.getPedidoId(),
                valor,
                status,
                document.getDataCriacao()
        );
    }
}
