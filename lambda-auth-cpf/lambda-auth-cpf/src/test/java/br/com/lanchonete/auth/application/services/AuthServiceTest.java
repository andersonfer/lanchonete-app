package br.com.lanchonete.auth.application.services;

import br.com.lanchonete.auth.adapters.jwt.JwtService;
import br.com.lanchonete.auth.application.usecases.IdentificarCliente;
import br.com.lanchonete.auth.domain.entities.Cliente;
import br.com.lanchonete.auth.domain.exceptions.RecursoNaoEncontradoException;
import br.com.lanchonete.auth.dto.AuthCpfRequest;
import br.com.lanchonete.auth.dto.AuthCpfResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private IdentificarCliente identificarCliente;
    
    @Mock
    private JwtService jwtService;
    
    private AuthService authService;
    
    @BeforeEach
    void setUp() {
        authService = new AuthService(identificarCliente, jwtService);
    }

    @Test
    @DisplayName("Deve retornar erro quando request não tem campo CPF")
    void t1() {
        // Arrange
        AuthCpfRequest request = new AuthCpfRequest();
        // Não chamar setCpf() para simular request sem campo cpf

        // Act
        AuthCpfResponse response = authService.autenticar(request);

        // Assert
        assertNotNull(response, "A resposta não deveria ser nula");
        assertFalse(response.isSuccess(), "A autenticação deveria falhar");
        assertEquals("Campo 'cpf' é obrigatório", response.getError(), "A mensagem de erro não está correta");
        assertNull(response.getCliente(), "O cliente deveria ser null em caso de erro");
        assertNull(response.getToken(), "O token deveria ser null em caso de erro");
        
        verify(identificarCliente, never()).executar(any());
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    @DisplayName("Deve autenticar cliente anônimo quando CPF é vazio")
    void t2() {
        // Arrange
        AuthCpfRequest request = new AuthCpfRequest();
        request.setCpf(""); // CPF vazio
        
        Cliente clienteAnonimo = Cliente.criarAnonimo();
        String tokenEsperado = "token-anonimo-jwt";
        
        when(identificarCliente.executar("")).thenReturn(clienteAnonimo);
        when(jwtService.generateToken(clienteAnonimo)).thenReturn(tokenEsperado);

        // Act
        AuthCpfResponse response = authService.autenticar(request);

        // Assert
        assertNotNull(response, "A resposta não deveria ser nula");
        assertTrue(response.isSuccess(), "A autenticação deveria ser bem-sucedida");
        assertNull(response.getError(), "Não deveria haver erro");
        
        assertNotNull(response.getCliente(), "O cliente não deveria ser nulo");
        assertNull(response.getCliente().getId(), "O ID do cliente anônimo deveria ser null");
        assertNull(response.getCliente().getCpf(), "O CPF do cliente anônimo deveria ser null");
        assertEquals("Cliente Anônimo", response.getCliente().getNome(), "O nome do cliente anônimo não está correto");
        assertNull(response.getCliente().getEmail(), "O email do cliente anônimo deveria ser null");
        
        assertEquals(tokenEsperado, response.getToken(), "O token não está correto");
        assertEquals(3600, response.getExpiresIn(), "O tempo de expiração não está correto");
        
        verify(identificarCliente).executar("");
        verify(jwtService).generateToken(clienteAnonimo);
    }

    @Test
    @DisplayName("Deve autenticar cliente com sucesso quando CPF é válido e existe")
    void t3() {
        // Arrange
        AuthCpfRequest request = new AuthCpfRequest();
        request.setCpf("12345678901");
        
        Cliente clienteIdentificado = Cliente.reconstituir(1L, "João Silva", "joao@email.com", "12345678901");
        String tokenEsperado = "token-cliente-jwt";
        
        when(identificarCliente.executar("12345678901")).thenReturn(clienteIdentificado);
        when(jwtService.generateToken(clienteIdentificado)).thenReturn(tokenEsperado);

        // Act
        AuthCpfResponse response = authService.autenticar(request);

        // Assert
        assertNotNull(response, "A resposta não deveria ser nula");
        assertTrue(response.isSuccess(), "A autenticação deveria ser bem-sucedida");
        assertNull(response.getError(), "Não deveria haver erro");
        
        assertNotNull(response.getCliente(), "O cliente não deveria ser nulo");
        assertEquals(1L, response.getCliente().getId(), "O ID do cliente não está correto");
        assertEquals("12345678901", response.getCliente().getCpf(), "O CPF do cliente não está correto");
        assertEquals("João Silva", response.getCliente().getNome(), "O nome do cliente não está correto");
        assertEquals("joao@email.com", response.getCliente().getEmail(), "O email do cliente não está correto");
        
        assertEquals(tokenEsperado, response.getToken(), "O token não está correto");
        assertEquals(3600, response.getExpiresIn(), "O tempo de expiração não está correto");
        
        verify(identificarCliente).executar("12345678901");
        verify(jwtService).generateToken(clienteIdentificado);
    }

    @Test
    @DisplayName("Deve retornar erro quando CPF não é encontrado")
    void t4() {
        // Arrange
        AuthCpfRequest request = new AuthCpfRequest();
        request.setCpf("99999999999");
        
        when(identificarCliente.executar("99999999999"))
            .thenThrow(new RecursoNaoEncontradoException("CPF não encontrado"));

        // Act
        AuthCpfResponse response = authService.autenticar(request);

        // Assert
        assertNotNull(response, "A resposta não deveria ser nula");
        assertFalse(response.isSuccess(), "A autenticação deveria falhar");
        assertEquals("CPF não encontrado", response.getError(), "A mensagem de erro não está correta");
        assertNull(response.getCliente(), "O cliente deveria ser null em caso de erro");
        assertNull(response.getToken(), "O token deveria ser null em caso de erro");
        
        verify(identificarCliente).executar("99999999999");
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    @DisplayName("Deve retornar erro quando CPF tem formato inválido")
    void t5() {
        // Arrange
        AuthCpfRequest request = new AuthCpfRequest();
        request.setCpf("123");
        
        when(identificarCliente.executar("123"))
            .thenThrow(new IllegalArgumentException("CPF deve conter 11 dígitos numéricos"));

        // Act
        AuthCpfResponse response = authService.autenticar(request);

        // Assert
        assertNotNull(response, "A resposta não deveria ser nula");
        assertFalse(response.isSuccess(), "A autenticação deveria falhar");
        assertEquals("CPF deve conter 11 dígitos numéricos", response.getError(), "A mensagem de erro não está correta");
        assertNull(response.getCliente(), "O cliente deveria ser null em caso de erro");
        assertNull(response.getToken(), "O token deveria ser null em caso de erro");
        
        verify(identificarCliente).executar("123");
        verify(jwtService, never()).generateToken(any());
    }
}