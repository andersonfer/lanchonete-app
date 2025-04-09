package br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada;

import br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada.dto.ClienteResponseDTO;
import br.com.lanchonete.autoatendimento.aplicacao.portas.saida.ClienteRepositorio;
import br.com.lanchonete.autoatendimento.dominio.Cliente;
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
class IdentificarClienteServiceTest {

    @Mock
    private ClienteRepositorio clienteRepositorio;

    @InjectMocks
    private IdentificarClienteService identificarClienteService;


    @Test
    @DisplayName("Deve retornar ClienteResponseDTO ao encontrar cliente pelo CPF")
    void t1() {
        String cpf = "12345678910";
        Cliente cliente = Cliente.builder()
                .id(1L)
                .nome("Maria Oliveira")
                .email("maria.oliveira@email.com")
                .cpf(cpf)
                .build();

        when(clienteRepositorio.buscarPorCpf(cpf)).thenReturn(Optional.of(cliente));

        Optional<ClienteResponseDTO> resultado = identificarClienteService.identificar(cpf);

        assertTrue(resultado.isPresent(), "O cliente deveria estar presente");
        ClienteResponseDTO dto = resultado.get();
        assertEquals(cliente.getId(), dto.getId(), "O ID do cliente não está correto");
        assertEquals(cliente.getNome(), dto.getNome(), "O nome do cliente não está correto");
        assertEquals(cliente.getEmail(), dto.getEmail(), "O email do cliente não está correto");
        assertEquals(cliente.getCpf(), dto.getCpf(), "O CPF do cliente não está correto");
    }

    @Test
    @DisplayName("Deve retornar vazio quando cliente não for encontrado pelo CPF")
    void t2() {
        String cpf = "12345678910";

        when(clienteRepositorio.buscarPorCpf(cpf)).thenReturn(Optional.empty());

        Optional<ClienteResponseDTO> resultado = identificarClienteService.identificar(cpf);

        assertTrue(resultado.isEmpty(), "O cliente não deveria estar presente");
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException ao informar CPF vazio")
    void t3() {
        String cpf = "";

        Exception excecao = assertThrows(IllegalArgumentException.class,
                () -> identificarClienteService.identificar(cpf),
                "Deveria lançar IllegalArgumentException para CPF vazio");

        assertEquals("CPF é obrigatório", excecao.getMessage(), "A mensagem de exceção não está correta");
    }

}