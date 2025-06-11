package br.com.lanchonete.autoatendimento.entidades.cliente;

import br.com.lanchonete.autoatendimento.entidades.shared.Cpf;
import br.com.lanchonete.autoatendimento.entidades.shared.Email;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ClienteTest {

    @Test
    @DisplayName("Deve criar cliente com sucesso quando os dados são válidos")
    void t1() {
        // Criar cliente com dados válidos
        Cliente cliente = Cliente.criar("João Silva", "joao@email.com", "12345678901");

        // Verificações
        assertEquals("João Silva", cliente.getNome(), "Nome deve estar correto");
        assertEquals("joao@email.com", cliente.getEmail().getValor(), "Email deve estar correto");
        assertEquals("12345678901", cliente.getCpf().getValor(), "CPF deve estar correto");
        assertNull(cliente.getId(), "ID deve ser nulo para cliente novo");
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar cliente com nome nulo")
    void t2() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Cliente.criar(null, "joao@email.com", "12345678901"),
                "Deve lançar exceção para nome nulo"
        );

        assertEquals("Nome é obrigatório", exception.getMessage(),
                "Mensagem de erro incorreta");
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar cliente com nome vazio")
    void t3() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Cliente.criar("", "joao@email.com", "12345678901"),
                "Deve lançar exceção para nome vazio"
        );

        assertEquals("Nome é obrigatório", exception.getMessage(),
                "Mensagem de erro incorreta");
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar cliente com nome em branco")
    void t4() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Cliente.criar("   ", "joao@email.com", "12345678901"),
                "Deve lançar exceção para nome em branco"
        );

        assertEquals("Nome é obrigatório", exception.getMessage(),
                "Mensagem de erro incorreta");
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar cliente com email inválido")
    void t5() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Cliente.criar("João Silva", "email-invalido", "12345678901"),
                "Deve lançar exceção para email inválido"
        );

        assertEquals("Email inválido", exception.getMessage(),
                "Mensagem de erro incorreta");
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar cliente com CPF inválido")
    void t6() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Cliente.criar("João Silva", "joao@email.com", "123"),
                "Deve lançar exceção para CPF inválido"
        );

        assertEquals("CPF deve conter 11 dígitos numéricos", exception.getMessage(),
                "Mensagem de erro incorreta");
    }

    @Test
    @DisplayName("Deve criar cliente sem validação com ID")
    void t7() {
        // Criar cliente sem validação
        Cliente cliente = Cliente.criarSemValidacao(1L, "Maria Santos", "maria@email.com", "98765432100");

        // Verificações
        assertEquals(1L, cliente.getId(), "ID deve estar correto");
        assertEquals("Maria Santos", cliente.getNome(), "Nome deve estar correto");
        assertEquals("maria@email.com", cliente.getEmail().getValor(), "Email deve estar correto");
        assertEquals("98765432100", cliente.getCpf().getValor(), "CPF deve estar correto");
    }

    @Test
    @DisplayName("Deve lançar exceção ao definir nome nulo")
    void t8() {
        Cliente cliente = Cliente.criar("João Silva", "joao@email.com", "12345678901");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> cliente.setNome(null),
                "Deve lançar exceção ao definir nome nulo"
        );

        assertEquals("Nome é obrigatório", exception.getMessage(),
                "Mensagem de erro incorreta");
    }

    @Test
    @DisplayName("Deve lançar exceção ao definir CPF nulo")
    void t9() {
        Cliente cliente = Cliente.criar("João Silva", "joao@email.com", "12345678901");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> cliente.setCpf(null),
                "Deve lançar exceção ao definir CPF nulo"
        );

        assertEquals("CPF é obrigatório", exception.getMessage(),
                "Mensagem de erro incorreta");
    }

    @Test
    @DisplayName("Deve lançar exceção ao definir email nulo")
    void t10() {
        Cliente cliente = Cliente.criar("João Silva", "joao@email.com", "12345678901");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> cliente.setEmail(null),
                "Deve lançar exceção ao definir email nulo"
        );

        assertEquals("Email é obrigatório", exception.getMessage(),
                "Mensagem de erro incorreta");
    }

    @Test
    @DisplayName("Deve ser igual quando clientes têm os mesmos dados")
    void t11() {
        Cliente cliente1 = Cliente.criarSemValidacao(1L, "João Silva", "joao@email.com", "12345678901");
        Cliente cliente2 = Cliente.criarSemValidacao(1L, "João Silva", "joao@email.com", "12345678901");

        assertEquals(cliente1, cliente2, "Clientes com mesmos dados devem ser iguais");
        assertEquals(cliente1.hashCode(), cliente2.hashCode(), "HashCode deve ser igual");
    }

    @Test
    @DisplayName("Deve ser diferente quando clientes têm dados diferentes")
    void t12() {
        Cliente cliente1 = Cliente.criarSemValidacao(1L, "João Silva", "joao@email.com", "12345678901");
        Cliente cliente2 = Cliente.criarSemValidacao(2L, "Maria Santos", "maria@email.com", "98765432100");

        assertNotEquals(cliente1, cliente2, "Clientes com dados diferentes devem ser diferentes");
    }

    @Test
    @DisplayName("Deve ter toString bem formatado")
    void t13() {
        Cliente cliente = Cliente.criarSemValidacao(1L, "João Silva", "joao@email.com", "12345678901");

        String toString = cliente.toString();

        assertTrue(toString.contains("Cliente{"), "ToString deve começar com Cliente{");
        assertTrue(toString.contains("id=1"), "ToString deve conter o ID");
        assertTrue(toString.contains("nome='João Silva'"), "ToString deve conter o nome");
        assertTrue(toString.contains("cpf="), "ToString deve conter o CPF");
        assertTrue(toString.contains("email="), "ToString deve conter o email");
    }

    @Test
    @DisplayName("Deve permitir alterar nome válido")
    void t14() {
        Cliente cliente = Cliente.criar("João Silva", "joao@email.com", "12345678901");

        cliente.setNome("João Santos");

        assertEquals("João Santos", cliente.getNome(), "Nome deve ter sido alterado");
    }

    @Test
    @DisplayName("Deve permitir alterar CPF válido")
    void t15() {
        Cliente cliente = Cliente.criar("João Silva", "joao@email.com", "12345678901");

        cliente.setCpf(new Cpf("98765432100"));

        assertEquals("98765432100", cliente.getCpf().getValor(), "CPF deve ter sido alterado");
    }

    @Test
    @DisplayName("Deve permitir alterar email válido")
    void t16() {
        Cliente cliente = Cliente.criar("João Silva", "joao@email.com", "12345678901");

        cliente.setEmail(new Email("joao.silva@email.com"));

        assertEquals("joao.silva@email.com", cliente.getEmail().getValor(), "Email deve ter sido alterado");
    }
}