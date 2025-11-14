package br.com.lanchonete.pagamento.application.gateways;

import br.com.lanchonete.pagamento.domain.model.Pagamento;

public interface PagamentoGateway {

    Pagamento salvar(Pagamento pagamento);
}
