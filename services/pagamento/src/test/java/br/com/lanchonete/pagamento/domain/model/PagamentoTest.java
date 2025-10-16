package br.com.lanchonete.pagamento.domain.model;

import br.com.lanchonete.pagamento.domain.exceptions.ValidacaoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PagamentoTest {

    private Long pedidoIdValido;
    private Valor valorValido;
    private StatusPagamento statusValido;

    @BeforeEach
    void configurar() {
        pedidoIdValido = 123L;
        valorValido = new Valor(new BigDecimal("50.00"));
        statusValido = StatusPagamento.APROVADO;
    }

    @Test
    @DisplayName("Deve criar Pagamento com sucesso quando dados são válidos")
    void t1() {
        Pagamento pagamento = new Pagamento(pedidoIdValido, valorValido, statusValido);

        assertNotNull(pagamento);
        assertEquals(pedidoIdValido, pagamento.getPedidoId());
        assertEquals(valorValido.getValor(), pagamento.getValor());
        assertEquals(statusValido, pagamento.getStatus());
        assertNotNull(pagamento.getDataCriacao());
    }

    @Test
    @DisplayName("Deve lançar exceção quando pedidoId é nulo")
    void t2() {
        ValidacaoException exception = assertThrows(ValidacaoException.class, () -> {
            new Pagamento(null, valorValido, statusValido);
        });
        assertEquals("PedidoId não pode ser nulo", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção quando pedidoId é zero")
    void t3() {
        ValidacaoException exception = assertThrows(ValidacaoException.class, () -> {
            new Pagamento(0L, valorValido, statusValido);
        });
        assertEquals("PedidoId deve ser maior que zero", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção quando pedidoId é negativo")
    void t4() {
        ValidacaoException exception = assertThrows(ValidacaoException.class, () -> {
            new Pagamento(-1L, valorValido, statusValido);
        });
        assertEquals("PedidoId deve ser maior que zero", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção quando valor é nulo")
    void t5() {
        ValidacaoException exception = assertThrows(ValidacaoException.class, () -> {
            new Pagamento(pedidoIdValido, null, statusValido);
        });
        assertEquals("Valor não pode ser nulo", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção quando status é nulo")
    void t6() {
        ValidacaoException exception = assertThrows(ValidacaoException.class, () -> {
            new Pagamento(pedidoIdValido, valorValido, null);
        });
        assertEquals("Status não pode ser nulo", exception.getMessage());
    }

    @Test
    @DisplayName("Deve criar Pagamento com status REJEITADO")
    void t7() {
        Pagamento pagamento = new Pagamento(pedidoIdValido, valorValido, StatusPagamento.REJEITADO);
        assertEquals(StatusPagamento.REJEITADO, pagamento.getStatus());
    }

    @Test
    @DisplayName("Deve permitir setar ID após criação")
    void t8() {
        Pagamento pagamento = new Pagamento(pedidoIdValido, valorValido, statusValido);
        String id = "507f1f77bcf86cd799439011";
        pagamento.setId(id);
        assertEquals(id, pagamento.getId());
    }

    @Test
    @DisplayName("Deve retornar string correta no toString")
    void t9() {
        Pagamento pagamento = new Pagamento(pedidoIdValido, valorValido, statusValido);
        pagamento.setId("123");
        String result = pagamento.toString();
        assertTrue(result.contains("Pagamento{"));
        assertTrue(result.contains("id='123'"));
        assertTrue(result.contains("pedidoId=123"));
    }

    @Test
    @DisplayName("Deve retornar true quando compara pagamentos iguais")
    void t10() {
        Pagamento pagamento1 = new Pagamento("1", pedidoIdValido, valorValido, statusValido, java.time.LocalDateTime.now());
        Pagamento pagamento2 = new Pagamento("1", pedidoIdValido, valorValido, statusValido, java.time.LocalDateTime.now());
        assertEquals(pagamento1, pagamento2);
    }

    @Test
    @DisplayName("Deve retornar false quando compara pagamentos diferentes")
    void t11() {
        Pagamento pagamento1 = new Pagamento("1", pedidoIdValido, valorValido, statusValido, java.time.LocalDateTime.now());
        Pagamento pagamento2 = new Pagamento("2", 456L, valorValido, statusValido, java.time.LocalDateTime.now());
        assertNotEquals(pagamento1, pagamento2);
    }

    @Test
    @DisplayName("Deve retornar hashCode consistente")
    void t12() {
        Pagamento pagamento = new Pagamento("1", pedidoIdValido, valorValido, statusValido, java.time.LocalDateTime.now());
        int hash1 = pagamento.hashCode();
        int hash2 = pagamento.hashCode();
        assertEquals(hash1, hash2);
    }
}
