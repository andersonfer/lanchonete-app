package br.com.lanchonete.auth.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CpfValidatorTest {

    private CpfValidator cpfValidator;

    @BeforeEach
    void configurar() {
        cpfValidator = new CpfValidator();
    }

    @Test
    @DisplayName("Deve validar CPF válido como true")
    void t1() {
        String cpfValido = "12345678901";
        
        boolean resultado = cpfValidator.isValido(cpfValido);
        
        assertTrue(resultado, "CPF válido deve retornar true");
    }

    @Test
    @DisplayName("Deve validar CPF formatado como true")
    void t2() {
        String cpfFormatado = "123.456.789-01";
        
        boolean resultado = cpfValidator.isValido(cpfFormatado);
        
        assertTrue(resultado, "CPF formatado deve retornar true");
    }

    @Test
    @DisplayName("Deve invalidar CPF com menos de 11 dígitos")
    void t3() {
        String cpfCurto = "123456789";
        
        boolean resultado = cpfValidator.isValido(cpfCurto);
        
        assertFalse(resultado, "CPF com menos de 11 dígitos deve ser inválido");
    }

    @Test
    @DisplayName("Deve invalidar CPF com mais de 11 dígitos")
    void t4() {
        String cpfLongo = "123456789012";
        
        boolean resultado = cpfValidator.isValido(cpfLongo);
        
        assertFalse(resultado, "CPF com mais de 11 dígitos deve ser inválido");
    }

    @Test
    @DisplayName("Deve invalidar CPF nulo")
    void t5() {
        boolean resultado = cpfValidator.isValido(null);
        
        assertFalse(resultado, "CPF nulo deve ser inválido");
    }

    @Test
    @DisplayName("Deve invalidar CPF vazio")
    void t6() {
        boolean resultado = cpfValidator.isValido("");
        
        assertFalse(resultado, "CPF vazio deve ser inválido");
    }

    @Test
    @DisplayName("Deve invalidar CPF com caracteres não numéricos")
    void t7() {
        String cpfComLetras = "1234567890a";
        
        boolean resultado = cpfValidator.isValido(cpfComLetras);
        
        assertFalse(resultado, "CPF com letras deve ser inválido");
    }

    @Test
    @DisplayName("Deve validar CPF com todos dígitos iguais como válido")
    void t8() {
        String cpfTodosIguais = "11111111111";
        
        boolean resultado = cpfValidator.isValido(cpfTodosIguais);
        
        assertTrue(resultado, "CPF com 11 dígitos deve ser válido");
    }

    @Test
    @DisplayName("Deve validar CPF com espaços como válido após remoção")
    void t9() {
        String cpfComEspacos = "123 456 789 01";
        
        boolean resultado = cpfValidator.isValido(cpfComEspacos);
        
        assertTrue(resultado, "CPF com espaços deve ser válido após remoção");
    }
}