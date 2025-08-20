package br.com.lanchonete.auth.application.services;

import br.com.lanchonete.auth.application.usecases.IdentificarCliente;
import br.com.lanchonete.auth.adapters.jwt.JwtService;
import br.com.lanchonete.auth.domain.entities.Cliente;
import br.com.lanchonete.auth.domain.exceptions.RecursoNaoEncontradoException;
import br.com.lanchonete.auth.domain.exceptions.ValidacaoException;
import br.com.lanchonete.auth.dto.AuthCpfRequest;
import br.com.lanchonete.auth.dto.AuthCpfResponse;
import br.com.lanchonete.auth.dto.ClienteResponse;

public class AuthService {
    private final IdentificarCliente identificarCliente;
    private final JwtService jwtService;
    
    public AuthService(IdentificarCliente identificarCliente, JwtService jwtService) {
        this.identificarCliente = identificarCliente;
        this.jwtService = jwtService;
    }
    
    public AuthCpfResponse autenticar(AuthCpfRequest request) {
        try {
            // 1. Validar se request tem campo 'cpf'
            if (!request.hasCpfField()) {
                throw new ValidacaoException("Campo 'cpf' é obrigatório");
            }
            
            // 2. Use Case decide: anônimo ou CPF
            Cliente cliente = identificarCliente.executar(request.getCpf());
            
            // 3. Gerar JWT
            String token = jwtService.generateToken(cliente);
            
            // 4. Converter para DTO de resposta
            return new AuthCpfResponse(
                true,
                ClienteResponse.from(cliente),
                token,
                3600
            );
            
        } catch (ValidacaoException | RecursoNaoEncontradoException | IllegalArgumentException e) {
            return new AuthCpfResponse(false, e.getMessage());
        }
    }
}