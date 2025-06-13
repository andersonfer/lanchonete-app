package br.com.lanchonete.autoatendimento.casosdeuso.cliente;

import br.com.lanchonete.autoatendimento.dominio.shared.excecao.RecursoNaoEncontradoException;
import br.com.lanchonete.autoatendimento.dominio.shared.excecao.ValidacaoException;
import br.com.lanchonete.autoatendimento.interfaces.ClienteGateway;
import br.com.lanchonete.autoatendimento.entidades.cliente.Cliente;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class IdentificarClienteTest {

    @Mock
    private ClienteGateway clienteGateway;

    @InjectMocks
    private IdentificarCliente identificarCliente;


    @Test
    @DisplayName("Deve retornar Cliente ao encontrar cliente pelo CPF")
    void t1() {
        String cpf = "12345678910";
        Cliente clienteEsperado = Cliente.reconstituir(
                1L,
                "Maria Oliveira",
                "maria.oliveira@email.com",
                cpf
        );
        when(clienteGateway.buscarPorCpf(cpf)).thenReturn(Optional.of(clienteEsperado));

        Optional<Cliente> resultado = identificarCliente.executar(cpf);

        assertTrue(resultado.isPresent(), "O cliente deveria estar presente");
        Cliente clienteRetornado = resultado.get();
        assertEquals(clienteEsperado.getId(), clienteRetornado.getId(), "O ID do cliente não está correto");
        assertEquals(clienteEsperado.getNome(), clienteRetornado.getNome(), "O nome do cliente não está correto");
        assertEquals(clienteEsperado.getEmail().getValor(), clienteRetornado.getEmail().getValor(), "O email do cliente não está correto");
        assertEquals(clienteEsperado.getCpf().getValor(), clienteRetornado.getCpf().getValor(), "O CPF do cliente não está correto");
    }

    @Test
    @DisplayName("Deve lançar RecursoNaoEncontradoException ao informa cliente inexistente")
    void t2() {
        String cpf = "12345678910";

        when(clienteGateway.buscarPorCpf(cpf)).thenReturn(Optional.empty());

        RecursoNaoEncontradoException excecao = assertThrows(RecursoNaoEncontradoException.class,
                () -> identificarCliente.executar(cpf),
                "Deveria lançar RecursoNaoEncontradoException para CPF inexistente");

        assertEquals("CPF não encontrado", excecao.getMessage(), "A mensagem de exceção não está correta");
    }

    @Test
    @DisplayName("Deve lançar ValidacaoException ao informar CPF vazio")
    void t3() {
        String cpf = "";

        ValidacaoException excecao = assertThrows(ValidacaoException.class,
                () -> identificarCliente.executar(cpf),
                "Deveria lançar ValidacaoException para CPF vazio");

        assertEquals("CPF é obrigatório", excecao.getMessage(), "A mensagem de exceção não está correta");
    }

}