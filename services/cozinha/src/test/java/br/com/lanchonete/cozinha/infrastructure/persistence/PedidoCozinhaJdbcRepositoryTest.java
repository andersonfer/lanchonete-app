package br.com.lanchonete.cozinha.infrastructure.persistence;

import br.com.lanchonete.cozinha.domain.model.PedidoCozinha;
import br.com.lanchonete.cozinha.domain.model.StatusPedido;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJdbcTest
class PedidoCozinhaJdbcRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private PedidoCozinhaJdbcRepository pedidoCozinhaRepository;

    private final Long PEDIDO_ID_JA_CADASTRADO = 123L;

    private PedidoCozinha pedidoPreCadastrado;

    @BeforeEach
    void configurar() {
        pedidoCozinhaRepository = new PedidoCozinhaJdbcRepository(jdbcTemplate);

        PedidoCozinha novoPedido = new PedidoCozinha(PEDIDO_ID_JA_CADASTRADO);
        pedidoPreCadastrado = pedidoCozinhaRepository.save(novoPedido);
    }

    @Test
    @DisplayName("Deve encontrar o pedido por ID")
    void t1() {
        Optional<PedidoCozinha> resultado = pedidoCozinhaRepository.findById(pedidoPreCadastrado.getId());

        assertTrue(resultado.isPresent());
        PedidoCozinha pedido = resultado.get();
        assertEquals(PEDIDO_ID_JA_CADASTRADO, pedido.getPedidoId());
        assertEquals(StatusPedido.AGUARDANDO, pedido.getStatus());
    }

    @Test
    @DisplayName("Deve retornar vazio quando ID não existe")
    void t2() {
        Optional<PedidoCozinha> resultado = pedidoCozinhaRepository.findById(999L);

        assertFalse(resultado.isPresent());
        assertTrue(resultado.isEmpty());
    }

    @Test
    @DisplayName("Deve encontrar pedido por pedidoId")
    void t3() {
        Optional<PedidoCozinha> resultado = pedidoCozinhaRepository.findByPedidoId(PEDIDO_ID_JA_CADASTRADO);

        assertTrue(resultado.isPresent());
        PedidoCozinha pedido = resultado.get();
        assertEquals(PEDIDO_ID_JA_CADASTRADO, pedido.getPedidoId());
    }

    @Test
    @DisplayName("Deve retornar vazio quando pedidoId não existe")
    void t4() {
        Long pedidoIdInexistente = 999L;

        Optional<PedidoCozinha> resultado = pedidoCozinhaRepository.findByPedidoId(pedidoIdInexistente);

        assertFalse(resultado.isPresent());
    }

    @Test
    @DisplayName("Deve encontrar pedido após salvar")
    void t5() {
        Long pedidoId = 456L;
        PedidoCozinha novoPedido = new PedidoCozinha(pedidoId);

        PedidoCozinha pedidoSalvo = pedidoCozinhaRepository.save(novoPedido);
        Optional<PedidoCozinha> pedidoEncontrado = pedidoCozinhaRepository.findByPedidoId(pedidoId);

        assertNotNull(pedidoSalvo.getId());
        assertTrue(pedidoEncontrado.isPresent());
        PedidoCozinha encontrado = pedidoEncontrado.get();
        assertEquals(pedidoSalvo.getId(), encontrado.getId());
        assertEquals(pedidoSalvo.getPedidoId(), encontrado.getPedidoId());
        assertEquals(pedidoSalvo.getStatus(), encontrado.getStatus());
    }

    @Test
    @DisplayName("Não deve permitir inserção de pedidoId duplicado")
    void t6() {
        PedidoCozinha pedidoComIdDuplicado = new PedidoCozinha(PEDIDO_ID_JA_CADASTRADO);

        assertThrows(DataIntegrityViolationException.class,
            () -> pedidoCozinhaRepository.save(pedidoComIdDuplicado));
    }

    @Test
    @DisplayName("Deve atualizar pedido existente")
    void t7() {
        pedidoPreCadastrado.iniciarPreparo();

        PedidoCozinha pedidoAtualizado = pedidoCozinhaRepository.save(pedidoPreCadastrado);

        assertEquals(StatusPedido.EM_PREPARO, pedidoAtualizado.getStatus());

        Optional<PedidoCozinha> pedidoEncontrado = pedidoCozinhaRepository.findById(pedidoPreCadastrado.getId());
        assertTrue(pedidoEncontrado.isPresent());
        assertEquals(StatusPedido.EM_PREPARO, pedidoEncontrado.get().getStatus());
    }

    @Test
    @DisplayName("Deve listar todos os pedidos")
    void t8() {
        PedidoCozinha pedido2 = new PedidoCozinha(789L);
        pedidoCozinhaRepository.save(pedido2);

        List<PedidoCozinha> pedidos = pedidoCozinhaRepository.findAll();

        assertNotNull(pedidos);
        assertTrue(pedidos.size() >= 2);
    }

    @Test
    @DisplayName("Deve listar pedidos por status")
    void t9() {
        PedidoCozinha pedido2 = new PedidoCozinha(321L);
        pedido2 = pedidoCozinhaRepository.save(pedido2);
        pedido2.iniciarPreparo();
        pedidoCozinhaRepository.save(pedido2);

        List<PedidoCozinha> pedidosEmPreparo = pedidoCozinhaRepository.findByStatus(StatusPedido.EM_PREPARO);
        List<PedidoCozinha> pedidosAguardando = pedidoCozinhaRepository.findByStatus(StatusPedido.AGUARDANDO);

        assertFalse(pedidosEmPreparo.isEmpty());
        assertEquals(1, pedidosEmPreparo.size());
        assertEquals(StatusPedido.EM_PREPARO, pedidosEmPreparo.get(0).getStatus());

        assertFalse(pedidosAguardando.isEmpty());
        assertTrue(pedidosAguardando.stream()
                .allMatch(p -> p.getStatus() == StatusPedido.AGUARDANDO));
    }

    @Test
    @DisplayName("Deve salvar pedido com dataFim quando marcado como pronto")
    void t10() {
        pedidoPreCadastrado.iniciarPreparo();
        pedidoCozinhaRepository.save(pedidoPreCadastrado);

        pedidoPreCadastrado.marcarComoPronto();
        PedidoCozinha pedidoAtualizado = pedidoCozinhaRepository.save(pedidoPreCadastrado);

        assertNotNull(pedidoAtualizado.getDataFim());
        assertEquals(StatusPedido.PRONTO, pedidoAtualizado.getStatus());
    }
}
