package br.com.lanchonete.autoatendimento.aplicacao.casosdeuso.pedido;

import br.com.lanchonete.autoatendimento.aplicacao.portas.entrada.ConsultarStatusPagamentoUC;
import br.com.lanchonete.autoatendimento.aplicacao.portas.saida.PedidoGateway;
import br.com.lanchonete.autoatendimento.dominio.excecoes.RecursoNaoEncontradoException;
import br.com.lanchonete.autoatendimento.dominio.modelo.pedido.Pedido;
import br.com.lanchonete.autoatendimento.dominio.modelo.pedido.StatusPagamento;

public class ConsultarStatusPagamento implements ConsultarStatusPagamentoUC {

    private final PedidoGateway pedidoGateway;

    public ConsultarStatusPagamento(PedidoGateway pedidoGateway) {
        this.pedidoGateway = pedidoGateway;
    }

    @Override
    public StatusPagamento executar(Long pedidoId) {
        if (pedidoId == null) {
            throw new IllegalArgumentException("ID do pedido não pode ser nulo");
        }

        Pedido pedido = pedidoGateway.buscarPorId(pedidoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pedido não encontrado com ID: " + pedidoId));

        return pedido.getStatusPagamento();
    }
}