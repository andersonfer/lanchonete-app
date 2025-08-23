package br.com.lanchonete.produtos.adaptadores.mappers;

public interface Mapper<DOMAIN, DTO> {
    DTO paraDTO(DOMAIN domain);
    DOMAIN paraDominio(DTO dto);
}