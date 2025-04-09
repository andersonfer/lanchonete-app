package br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CadastrarClienteDTO {
    private String nome;
    private String cpf;
    private String email;
}
