package br.com.lanchonete.autoatendimento.adaptadores.rest.mappers;

public interface Mapper<DOMAIN, DTO> {
    DTO paraDTO(DOMAIN domain);
    DOMAIN paraDominio(DTO dto);
}