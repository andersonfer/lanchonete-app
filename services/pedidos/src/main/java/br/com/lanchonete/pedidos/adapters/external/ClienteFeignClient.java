package br.com.lanchonete.pedidos.adapters.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "clientes-service", url = "${clientes.service.url}")
public interface ClienteFeignClient {

    @GetMapping("/clientes/cpf/{cpf}")
    ClienteResponse buscarPorCpf(@PathVariable String cpf);
}
