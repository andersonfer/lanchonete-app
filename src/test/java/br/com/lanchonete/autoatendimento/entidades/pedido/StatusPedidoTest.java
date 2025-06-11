package br.com.lanchonete.autoatendimento.entidades.pedido;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StatusPedidoTest {

    @Test
    @DisplayName("Deve conter todos os status esperados")
    void t1() {
        // Verificar se todos os valores esperados existem
        StatusPedido[] status = StatusPedido.values();

        assertEquals(4, status.length, "Deve conter exatamente 4 status");
        assertTrue(containsStatus(status, StatusPedido.RECEBIDO), "Deve conter status RECEBIDO");
        assertTrue(containsStatus(status, StatusPedido.EM_PREPARACAO), "Deve conter status EM_PREPARACAO");
        assertTrue(containsStatus(status, StatusPedido.PRONTO), "Deve conter status PRONTO");
        assertTrue(containsStatus(status, StatusPedido.FINALIZADO), "Deve conter status FINALIZADO");
    }

    @Test
    @DisplayName("Deve converter string para status - RECEBIDO")
    void t2() {
        StatusPedido status = StatusPedido.valueOf("RECEBIDO");
        assertEquals(StatusPedido.RECEBIDO, status, "Deve converter RECEBIDO corretamente");
    }

    @Test
    @DisplayName("Deve converter string para status - EM_PREPARACAO")
    void t3() {
        StatusPedido status = StatusPedido.valueOf("EM_PREPARACAO");
        assertEquals(StatusPedido.EM_PREPARACAO, status, "Deve converter EM_PREPARACAO corretamente");
    }

    @Test
    @DisplayName("Deve converter string para status - PRONTO")
    void t4() {
        StatusPedido status = StatusPedido.valueOf("PRONTO");
        assertEquals(StatusPedido.PRONTO, status, "Deve converter PRONTO corretamente");
    }

    @Test
    @DisplayName("Deve converter string para status - FINALIZADO")
    void t5() {
        StatusPedido status = StatusPedido.valueOf("FINALIZADO");
        assertEquals(StatusPedido.FINALIZADO, status, "Deve converter FINALIZADO corretamente");
    }

    @Test
    @DisplayName("Deve lançar exceção para status inválido")
    void t6() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> StatusPedido.valueOf("STATUS_INEXISTENTE"),
                "Deve lançar exceção para status inválido"
        );

        assertNotNull(exception.getMessage(), "Mensagem de erro deve estar presente");
    }

    @Test
    @DisplayName("Deve ter nomes corretos para cada status")
    void t7() {
        assertEquals("RECEBIDO", StatusPedido.RECEBIDO.name(), "Nome do status RECEBIDO deve estar correto");
        assertEquals("EM_PREPARACAO", StatusPedido.EM_PREPARACAO.name(), "Nome do status EM_PREPARACAO deve estar correto");
        assertEquals("PRONTO", StatusPedido.PRONTO.name(), "Nome do status PRONTO deve estar correto");
        assertEquals("FINALIZADO", StatusPedido.FINALIZADO.name(), "Nome do status FINALIZADO deve estar correto");
    }

    @Test
    @DisplayName("Deve ser igual apenas a si mesmo")
    void t8() {
        assertEquals(StatusPedido.RECEBIDO, StatusPedido.RECEBIDO, "Status deve ser igual a si mesmo");
        assertNotEquals(StatusPedido.RECEBIDO, StatusPedido.PRONTO, "Status diferentes devem ser diferentes");
        assertNotEquals(StatusPedido.EM_PREPARACAO, StatusPedido.FINALIZADO, "Status diferentes devem ser diferentes");
    }

    @Test
    @DisplayName("Deve ter toString igual ao name")
    void t9() {
        assertEquals("RECEBIDO", StatusPedido.RECEBIDO.toString(), "ToString de RECEBIDO deve ser igual ao name");
        assertEquals("EM_PREPARACAO", StatusPedido.EM_PREPARACAO.toString(), "ToString de EM_PREPARACAO deve ser igual ao name");
        assertEquals("PRONTO", StatusPedido.PRONTO.toString(), "ToString de PRONTO deve ser igual ao name");
        assertEquals("FINALIZADO", StatusPedido.FINALIZADO.toString(), "ToString de FINALIZADO deve ser igual ao name");
    }

    @Test
    @DisplayName("Deve funcionar com switch statement")
    void t10() {
        String resultado = switch (StatusPedido.RECEBIDO) {
            case RECEBIDO -> "Pedido foi recebido";
            case EM_PREPARACAO -> "Pedido está sendo preparado";
            case PRONTO -> "Pedido está pronto";
            case FINALIZADO -> "Pedido foi finalizado";
        };

        assertEquals("Pedido foi recebido", resultado, "Switch deve funcionar corretamente");
    }

    @Test
    @DisplayName("Deve ser utilizável em comparações de igualdade")
    void t11() {
        StatusPedido status1 = StatusPedido.PRONTO;
        StatusPedido status2 = StatusPedido.valueOf("PRONTO");

        assertTrue(status1 == status2, "Instâncias do mesmo status devem ser iguais com ==");
        assertEquals(status1, status2, "Instâncias do mesmo status devem ser iguais com equals");
    }

    @Test
    @DisplayName("Deve permitir verificação de estados específicos")
    void t12() {
        // Simulando verificações que podem existir no negócio
        assertTrue(isStatusInicial(StatusPedido.RECEBIDO), "RECEBIDO deve ser considerado status inicial");
        assertFalse(isStatusInicial(StatusPedido.FINALIZADO), "FINALIZADO não deve ser considerado status inicial");
        
        assertTrue(isStatusFinal(StatusPedido.FINALIZADO), "FINALIZADO deve ser considerado status final");
        assertFalse(isStatusFinal(StatusPedido.RECEBIDO), "RECEBIDO não deve ser considerado status final");
    }

    private boolean containsStatus(StatusPedido[] statusArray, StatusPedido status) {
        for (StatusPedido s : statusArray) {
            if (s == status) {
                return true;
            }
        }
        return false;
    }

    private boolean isStatusInicial(StatusPedido status) {
        return status == StatusPedido.RECEBIDO;
    }

    private boolean isStatusFinal(StatusPedido status) {
        return status == StatusPedido.FINALIZADO;
    }
}