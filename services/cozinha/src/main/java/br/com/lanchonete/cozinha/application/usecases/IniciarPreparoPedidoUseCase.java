package br.com.lanchonete.cozinha.application.usecases;

import br.com.lanchonete.cozinha.domain.exception.PedidoNaoEncontradoException;
import br.com.lanchonete.cozinha.domain.model.PedidoCozinha;
import br.com.lanchonete.cozinha.domain.repository.PedidoCozinhaRepository;

public class IniciarPreparoPedidoUseCase {
    private final PedidoCozinhaRepository pedidoCozinhaRepository;

    public IniciarPreparoPedidoUseCase(PedidoCozinhaRepository pedidoCozinhaRepository) {
        this.pedidoCozinhaRepository = pedidoCozinhaRepository;
    }

    public PedidoCozinha executar(Long id) {
        PedidoCozinha pedido = pedidoCozinhaRepository.findById(id)
                .orElseThrow(() -> new PedidoNaoEncontradoException(id));

        pedido.iniciarPreparo();
        return pedidoCozinhaRepository.save(pedido);
    }
}
