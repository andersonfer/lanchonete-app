package br.com.lanchonete.autoatendimento.aplicacao.casosdeuso.pedido;

import br.com.lanchonete.autoatendimento.aplicacao.gateways.PedidoGateway;
import br.com.lanchonete.autoatendimento.dominio.excecoes.RecursoNaoEncontradoException;
import br.com.lanchonete.autoatendimento.dominio.excecoes.ValidacaoException;
import br.com.lanchonete.autoatendimento.dominio.modelo.pedido.Pedido;
import br.com.lanchonete.autoatendimento.dominio.modelo.pedido.StatusPedido;
import br.com.lanchonete.autoatendimento.dominio.modelo.pedido.StatusPagamento;

public class AtualizarStatusPedido {

    private final PedidoGateway pedidoGateway;

    public AtualizarStatusPedido(final PedidoGateway pedidoGateway) {
        this.pedidoGateway = pedidoGateway;
    }

    public void executar(final Long pedidoId, final StatusPedido novoStatus) {
        // 1. Buscar o pedido
        final Pedido pedido = pedidoGateway.buscarPorId(pedidoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pedido não encontrado com ID: " + pedidoId));

        // 2. Validar se o pagamento está aprovado
        if (pedido.getStatusPagamento() != StatusPagamento.APROVADO) {
            throw new ValidacaoException("Só é possível atualizar status de pedidos com pagamento aprovado");
        }

        // 3. Validar se não está finalizado
        if (pedido.getStatus() == StatusPedido.FINALIZADO) {
            throw new ValidacaoException("Não é possível alterar status de pedido finalizado");
        }

        // 4. Validar transição de status
        validarTransicaoStatus(pedido.getStatus(), novoStatus);

        // 5. Atualizar o status
        pedidoGateway.atualizarStatus(pedidoId, novoStatus);
    }

    private void validarTransicaoStatus(final StatusPedido statusAtual, final StatusPedido novoStatus) {
        // Permitir manter o mesmo status
        if (statusAtual == novoStatus) {
            return;
        }

        // Definir transições válidas
        boolean transicaoValida = switch (statusAtual) {
            case RECEBIDO -> novoStatus == StatusPedido.EM_PREPARACAO;
            case EM_PREPARACAO -> novoStatus == StatusPedido.PRONTO;
            case PRONTO -> novoStatus == StatusPedido.FINALIZADO;
            case FINALIZADO -> false; // Já validado acima
        };

        if (!transicaoValida) {
            throw new ValidacaoException("Transição inválida de " + statusAtual + " para " + novoStatus);
        }
    }
}