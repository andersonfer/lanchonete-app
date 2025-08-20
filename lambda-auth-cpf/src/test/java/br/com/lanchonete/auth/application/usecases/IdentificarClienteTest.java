package br.com.lanchonete.auth.application.usecases;

import br.com.lanchonete.auth.application.gateways.ClienteGateway;
import br.com.lanchonete.auth.domain.entities.Cliente;
import br.com.lanchonete.auth.domain.exceptions.RecursoNaoEncontradoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IdentificarClienteTest {

    @Mock
    private ClienteGateway clienteGateway;

    @InjectMocks
    private IdentificarCliente identificarCliente;

    @Test
    @DisplayName("Deve retornar cliente anônimo quando CPF é null")
    void t1() {
        // Arrange
        String cpf = null;

        // Act
        Cliente resultado = identificarCliente.executar(cpf);

        // Assert
        assertNotNull(resultado, "O cliente não deveria ser nulo");
        assertTrue(resultado.isAnonimo(), "O cliente deveria ser anônimo");
        assertEquals("Cliente Anônimo", resultado.getNome(), "O nome do cliente anônimo não está correto");
        assertNull(resultado.getId(), "O ID do cliente anônimo deveria ser null");
        assertNull(resultado.getCpf(), "O CPF do cliente anônimo deveria ser null");
        assertNull(resultado.getEmail(), "O email do cliente anônimo deveria ser null");
        
        verify(clienteGateway, never()).buscarPorCpf(any());
    }

    @Test
    @DisplayName("Deve retornar cliente anônimo quando CPF é vazio")
    void t2() {
        // Arrange
        String cpf = "";

        // Act  
        Cliente resultado = identificarCliente.executar(cpf);

        // Assert
        assertNotNull(resultado, "O cliente não deveria ser nulo");
        assertTrue(resultado.isAnonimo(), "O cliente deveria ser anônimo");
        assertEquals("Cliente Anônimo", resultado.getNome(), "O nome do cliente anônimo não está correto");
        
        verify(clienteGateway, never()).buscarPorCpf(any());
    }

    @Test
    @DisplayName("Deve retornar cliente anônimo quando CPF contém apenas espaços")
    void t3() {
        // Arrange
        String cpf = "   ";

        // Act  
        Cliente resultado = identificarCliente.executar(cpf);

        // Assert
        assertNotNull(resultado, "O cliente não deveria ser nulo");
        assertTrue(resultado.isAnonimo(), "O cliente deveria ser anônimo");
        assertEquals("Cliente Anônimo", resultado.getNome(), "O nome do cliente anônimo não está correto");
        
        verify(clienteGateway, never()).buscarPorCpf(any());
    }

    @Test
    @DisplayName("Deve retornar cliente quando CPF válido existe")
    void t4() {
        // Arrange
        String cpf = "12345678901";
        Cliente clienteEsperado = Cliente.reconstituir(1L, "João Silva", "joao@email.com", cpf);
        when(clienteGateway.buscarPorCpf(cpf)).thenReturn(Optional.of(clienteEsperado));

        // Act
        Cliente resultado = identificarCliente.executar(cpf);

        // Assert
        assertNotNull(resultado, "O cliente não deveria ser nulo");
        assertEquals(clienteEsperado.getId(), resultado.getId(), "O ID do cliente não está correto");
        assertEquals(clienteEsperado.getNome(), resultado.getNome(), "O nome do cliente não está correto");
        assertEquals(clienteEsperado.getCpf().getValor(), resultado.getCpf().getValor(), "O CPF do cliente não está correto");
        assertFalse(resultado.isAnonimo(), "O cliente não deveria ser anônimo");
        
        verify(clienteGateway).buscarPorCpf(cpf);
    }

    @Test
    @DisplayName("Deve lançar RecursoNaoEncontradoException quando CPF não existe")
    void t5() {
        // Arrange
        String cpf = "99999999999";
        when(clienteGateway.buscarPorCpf(cpf)).thenReturn(Optional.empty());

        // Act & Assert
        RecursoNaoEncontradoException exception = assertThrows(RecursoNaoEncontradoException.class,
            () -> identificarCliente.executar(cpf),
            "Deveria lançar RecursoNaoEncontradoException para CPF inexistente");

        assertEquals("CPF não encontrado", exception.getMessage(), "A mensagem de exceção não está correta");
        
        verify(clienteGateway).buscarPorCpf(cpf);
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException quando CPF tem formato inválido")
    void t6() {
        // Arrange
        String cpf = "123"; // CPF inválido

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> identificarCliente.executar(cpf),
            "Deveria lançar IllegalArgumentException para CPF com formato inválido");

        assertEquals("CPF deve conter 11 dígitos numéricos", exception.getMessage(), "A mensagem de exceção não está correta");
        
        verify(clienteGateway, never()).buscarPorCpf(any());
    }
}