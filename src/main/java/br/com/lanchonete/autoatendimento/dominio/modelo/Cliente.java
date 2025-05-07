package br.com.lanchonete.autoatendimento.dominio.modelo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Cliente {
    //TODO: verificar como as validações podem ser feitas aqui
    private Long id;
    private String nome;
    private String cpf;
    private String email;

}
