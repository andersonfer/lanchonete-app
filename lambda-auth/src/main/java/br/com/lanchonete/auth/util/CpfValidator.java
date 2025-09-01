package br.com.lanchonete.auth.util;

public class CpfValidator {
    
    public boolean isValido(String cpf) {
        if (cpf == null || cpf.trim().isEmpty()) {
            return false;
        }
        
        String cpfLimpo = cpf.replaceAll("[^0-9]", "");
        
        return cpfLimpo.length() == 11;
    }
}