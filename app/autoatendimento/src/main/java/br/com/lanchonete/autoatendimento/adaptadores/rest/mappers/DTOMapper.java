package br.com.lanchonete.autoatendimento.adaptadores.rest.mappers;

public interface DTOMapper<DOMAIN> {
    Object paraDTO(DOMAIN domain);
}