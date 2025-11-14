package br.com.lanchonete.cozinha.domain.repository;

import br.com.lanchonete.cozinha.domain.model.PedidoCozinha;
import br.com.lanchonete.cozinha.domain.model.StatusPedido;

import java.util.List;
import java.util.Optional;

public interface PedidoCozinhaRepository {
    PedidoCozinha save(PedidoCozinha pedidoCozinha);
    Optional<PedidoCozinha> findById(Long id);
    Optional<PedidoCozinha> findByPedidoId(Long pedidoId);
    List<PedidoCozinha> findAll();
    List<PedidoCozinha> findByStatus(StatusPedido status);
}
