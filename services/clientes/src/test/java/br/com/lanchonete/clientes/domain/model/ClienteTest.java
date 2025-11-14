package br.com.lanchonete.clientes.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ClienteTest {

    @Test
    @DisplayName("Deve criar cliente com sucesso quando os dados são válidos")
    void t1() {
        Cliente cliente = Cliente.criar("João Silva", "joao@email.com", "12345678901");

        assertEquals("João Silva", cliente.getNome());
        assertEquals("joao@email.com", cliente.getEmail().getValor());
        assertEquals("12345678901", cliente.getCpf().getValor());
        assertNull(cliente.getId());
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar cliente com nome nulo")
    void t2() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Cliente.criar(null, "joao@email.com", "12345678901")
        );

        assertEquals("Nome é obrigatório", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar cliente com nome vazio")
    void t3() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Cliente.criar("", "joao@email.com", "12345678901")
        );

        assertEquals("Nome é obrigatório", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar cliente com nome em branco")
    void t4() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Cliente.criar("   ", "joao@email.com", "12345678901")
        );

        assertEquals("Nome é obrigatório", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar cliente com email inválido")
    void t5() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Cliente.criar("João Silva", "email-invalido", "12345678901")
        );

        assertEquals("Email inválido", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar cliente com CPF inválido")
    void t6() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Cliente.criar("João Silva", "joao@email.com", "123")
        );

        assertEquals("CPF deve conter 11 dígitos numéricos", exception.getMessage());
    }

    @Test
    @DisplayName("Deve reconstituir cliente com ID a partir de dados do banco")
    void t7() {
        Cliente cliente = Cliente.reconstituir(1L, "Maria Santos", "maria@email.com", "98765432100");

        assertEquals(1L, cliente.getId());
        assertEquals("Maria Santos", cliente.getNome());
        assertEquals("maria@email.com", cliente.getEmail().getValor());
        assertEquals("98765432100", cliente.getCpf().getValor());
    }

    @Test
    @DisplayName("Deve ser igual quando clientes têm os mesmos dados")
    void t8() {
        Cliente cliente1 = Cliente.reconstituir(1L, "João Silva", "joao@email.com", "12345678901");
        Cliente cliente2 = Cliente.reconstituir(1L, "João Silva", "joao@email.com", "12345678901");

        assertEquals(cliente1, cliente2);
        assertEquals(cliente1.hashCode(), cliente2.hashCode());
    }

    @Test
    @DisplayName("Deve ser diferente quando clientes têm dados diferentes")
    void t9() {
        Cliente cliente1 = Cliente.reconstituir(1L, "João Silva", "joao@email.com", "12345678901");
        Cliente cliente2 = Cliente.reconstituir(2L, "Maria Santos", "maria@email.com", "98765432100");

        assertNotEquals(cliente1, cliente2);
    }

    @Test
    @DisplayName("Deve ter toString bem formatado")
    void t10() {
        Cliente cliente = Cliente.reconstituir(1L, "João Silva", "joao@email.com", "12345678901");

        String toString = cliente.toString();

        assertTrue(toString.contains("Cliente{"));
        assertTrue(toString.contains("id=1"));
        assertTrue(toString.contains("nome='João Silva'"));
        assertTrue(toString.contains("cpf="));
        assertTrue(toString.contains("email="));
    }

    @Test
    @DisplayName("Cliente é completamente imutável")
    void t11() {
        Cliente cliente = Cliente.criar("João Silva", "joao@email.com", "12345678901");

        assertEquals("João Silva", cliente.getNome());
        assertEquals("joao@email.com", cliente.getEmail().getValor());
        assertEquals("12345678901", cliente.getCpf().getValor());
        assertNull(cliente.getId());
    }
}
