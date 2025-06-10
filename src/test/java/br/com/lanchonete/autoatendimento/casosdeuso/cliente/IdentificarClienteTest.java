package br.com.lanchonete.autoatendimento.casosdeuso.cliente;

import br.com.lanchonete.autoatendimento.controllers.dto.ClienteResponseDTO;
import br.com.lanchonete.autoatendimento.aplicacao.excecao.RecursoNaoEncontradoException;
import br.com.lanchonete.autoatendimento.aplicacao.excecao.ValidacaoException;
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
    @DisplayName("Deve retornar ClienteResponseDTO ao encontrar cliente pelo CPF")
    void t1() {
        String cpf = "12345678910";
        Cliente cliente = Cliente.criarSemValidacao(
                1L,
                "Maria Oliveira",
                "maria.oliveira@email.com",
                cpf
        );
        when(clienteGateway.buscarPorCpf(cpf)).thenReturn(Optional.of(cliente));

        Optional<ClienteResponseDTO> resultado = identificarCliente.executar(cpf);

        assertTrue(resultado.isPresent(), "O cliente deveria estar presente");
        ClienteResponseDTO dto = resultado.get();
        assertEquals(cliente.getId(), dto.id(), "O ID do cliente não está correto");
        assertEquals(cliente.getNome(), dto.nome(), "O nome do cliente não está correto");
        assertEquals(cliente.getEmail(), dto.email(), "O email do cliente não está correto");
        assertEquals(cliente.getCpf(), dto.cpf(), "O CPF do cliente não está correto");
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