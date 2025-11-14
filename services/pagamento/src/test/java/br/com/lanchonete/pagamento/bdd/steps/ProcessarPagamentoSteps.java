package br.com.lanchonete.pagamento.bdd.steps;

import br.com.lanchonete.pagamento.domain.exceptions.ValidacaoException;
import br.com.lanchonete.pagamento.domain.model.Valor;
import io.cucumber.java.pt.Quando;
import io.cucumber.java.pt.Então;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class ProcessarPagamentoSteps {

    private Valor valor;
    private Exception excecaoCapturada;

    @Quando("eu crio um valor de R$ {double}")
    public void euCrioUmValorDeRS(double valorDecimal) {
        try {
            valor = new Valor(BigDecimal.valueOf(valorDecimal));
            excecaoCapturada = null;
        } catch (Exception e) {
            excecaoCapturada = e;
            valor = null;
        }
    }

    @Quando("eu tento criar um valor de R$ {double}")
    public void euTentoCriarUmValorDeRS(double valorDecimal) {
        try {
            valor = new Valor(BigDecimal.valueOf(valorDecimal));
            excecaoCapturada = null;
        } catch (Exception e) {
            excecaoCapturada = e;
            valor = null;
        }
    }

    @Quando("eu tento criar um valor nulo")
    public void euTentoCriarUmValorNulo() {
        try {
            valor = new Valor(null);
            excecaoCapturada = null;
        } catch (Exception e) {
            excecaoCapturada = e;
            valor = null;
        }
    }

    @Então("o valor deve ser criado com sucesso")
    public void oValorDeveSerCriadoComSucesso() {
        assertNotNull(valor, "O valor deveria ter sido criado");
        assertNull(excecaoCapturada, "Não deveria ter lançado exceção");
    }

    @Então("o sistema deve lançar exceção de valor inválido")
    public void oSistemaDeveLancarExcecaoDeValorInvalido() {
        assertNull(valor, "O valor não deveria ter sido criado");
        assertNotNull(excecaoCapturada, "Deveria ter lançado exceção");
        assertTrue(excecaoCapturada instanceof ValidacaoException,
                "Deveria ser ValidacaoException, mas foi: " + excecaoCapturada.getClass().getSimpleName());
        assertTrue(excecaoCapturada.getMessage().toLowerCase().contains("maior") ||
                   excecaoCapturada.getMessage().toLowerCase().contains("zero"),
                "Mensagem deveria mencionar valor maior que zero");
    }

    @Então("o sistema deve lançar exceção de valor nulo")
    public void oSistemaDeveLancarExcecaoDeValorNulo() {
        assertNull(valor, "O valor não deveria ter sido criado");
        assertNotNull(excecaoCapturada, "Deveria ter lançado exceção");
        assertTrue(excecaoCapturada instanceof ValidacaoException,
                "Deveria ser ValidacaoException, mas foi: " + excecaoCapturada.getClass().getSimpleName());
        assertTrue(excecaoCapturada.getMessage().toLowerCase().contains("nulo") ||
                   excecaoCapturada.getMessage().toLowerCase().contains("null"),
                "Mensagem deveria mencionar valor nulo");
    }
}
