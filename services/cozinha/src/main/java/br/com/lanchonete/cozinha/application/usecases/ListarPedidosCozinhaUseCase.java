package br.com.lanchonete.cozinha.application.usecases;

import br.com.lanchonete.cozinha.domain.model.PedidoCozinha;
import br.com.lanchonete.cozinha.domain.model.StatusPedido;
import br.com.lanchonete.cozinha.domain.repository.PedidoCozinhaRepository;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ListarPedidosCozinhaUseCase {
    private final PedidoCozinhaRepository pedidoCozinhaRepository;

    public ListarPedidosCozinhaUseCase(PedidoCozinhaRepository pedidoCozinhaRepository) {
        this.pedidoCozinhaRepository = pedidoCozinhaRepository;
    }

    public List<PedidoCozinha> executar() {
        return pedidoCozinhaRepository.findAll()
                .stream()
                .filter(pedido -> pedido.getStatus() != StatusPedido.RETIRADO)
                .sorted(Comparator.comparing(this::getPrioridade)
                        .thenComparing(PedidoCozinha::getDataInicio))
                .collect(Collectors.toList());
    }

    private int getPrioridade(PedidoCozinha pedido) {
        return switch (pedido.getStatus()) {
            case AGUARDANDO -> 1;
            case EM_PREPARO -> 2;
            case PRONTO -> 3;
            case RETIRADO -> 4;
        };
    }
}
