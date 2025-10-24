package br.com.lanchonete.cozinha.application.usecases;

import br.com.lanchonete.cozinha.domain.exception.PedidoNaoEncontradoException;
import br.com.lanchonete.cozinha.domain.model.PedidoCozinha;
import br.com.lanchonete.cozinha.domain.repository.PedidoCozinhaRepository;

public class MarcarPedidoComoProntoUseCase {
    private final PedidoCozinhaRepository pedidoCozinhaRepository;

    public MarcarPedidoComoProntoUseCase(PedidoCozinhaRepository pedidoCozinhaRepository) {
        this.pedidoCozinhaRepository = pedidoCozinhaRepository;
    }

    public PedidoCozinha executar(Long id) {
        PedidoCozinha pedido = pedidoCozinhaRepository.findById(id)
                .orElseThrow(() -> new PedidoNaoEncontradoException(id));

        pedido.marcarComoPronto();
        return pedidoCozinhaRepository.save(pedido);
    }
}
