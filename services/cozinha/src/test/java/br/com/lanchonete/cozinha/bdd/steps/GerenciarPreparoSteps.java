package br.com.lanchonete.cozinha.bdd.steps;

import br.com.lanchonete.cozinha.domain.model.PedidoCozinha;
import br.com.lanchonete.cozinha.domain.model.StatusPedido;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Quando;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.E;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class GerenciarPreparoSteps {

    private List<StatusPedido> statusDisponiveis;
    private PedidoCozinha pedido;

    @Dado("que um pedido pode ter diferentes status")
    public void queUmPedidoPodeTermDiferentesStatus() {
        // Obter todos os status do enum
        statusDisponiveis = Arrays.asList(StatusPedido.values());
    }

    @Quando("eu verifico os status possíveis")
    public void euVerificoOsStatusPossiveis() {
        // Validar que o enum contém os status esperados
        assertNotNull(statusDisponiveis);
        assertFalse(statusDisponiveis.isEmpty());
    }

    @Então("o pedido deve ter os status: AGUARDANDO, EM_PREPARO, PRONTO, RETIRADO")
    public void oPedidoDeveTerOsStatusAguardandoEmPreparoProntoRetirado() {
        List<String> statusNames = statusDisponiveis.stream()
                .map(Enum::name)
                .collect(Collectors.toList());

        assertTrue(statusNames.contains("AGUARDANDO"), "Deve conter status AGUARDANDO");
        assertTrue(statusNames.contains("EM_PREPARO"), "Deve conter status EM_PREPARO");
        assertTrue(statusNames.contains("PRONTO"), "Deve conter status PRONTO");
        assertTrue(statusNames.contains("RETIRADO"), "Deve conter status RETIRADO");
        assertEquals(4, statusNames.size(), "Deve ter exatamente 4 status");
    }

    @Dado("que um pedido inicia com status AGUARDANDO")
    public void queUmPedidoIniciaComStatusAguardando() {
        pedido = new PedidoCozinha(123L);
        assertEquals(StatusPedido.AGUARDANDO, pedido.getStatus());
    }

    @Quando("o pedido passa para EM_PREPARO")
    public void oPedidoPassaParaEmPreparo() {
        pedido.iniciarPreparo();
    }

    @E("depois passa para PRONTO")
    public void depoisPassaParaPronto() {
        pedido.marcarComoPronto();
    }

    @Então("o fluxo de status deve estar correto")
    public void oFluxoDeStatusDeveEstarCorreto() {
        assertEquals(StatusPedido.PRONTO, pedido.getStatus());
        assertNotNull(pedido.getDataFim(), "Pedido pronto deve ter data fim");
    }
}
