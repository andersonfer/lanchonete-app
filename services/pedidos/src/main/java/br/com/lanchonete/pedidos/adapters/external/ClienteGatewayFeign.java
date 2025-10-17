package br.com.lanchonete.pedidos.adapters.external;

import br.com.lanchonete.pedidos.application.gateways.ClienteGateway;
import feign.FeignException;
import org.springframework.stereotype.Component;

@Component
public class ClienteGatewayFeign implements ClienteGateway {

    private final ClienteFeignClient feignClient;

    public ClienteGatewayFeign(ClienteFeignClient feignClient) {
        this.feignClient = feignClient;
    }

    @Override
    public boolean existeCliente(String cpf) {
        try {
            feignClient.buscarPorCpf(cpf);
            return true;
        } catch (FeignException.NotFound e) {
            return false;
        } catch (FeignException e) {
            throw new RuntimeException("Erro ao validar cliente: " + e.getMessage(), e);
        }
    }

    @Override
    public String buscarNomeCliente(String cpf) {
        try {
            ClienteResponse response = feignClient.buscarPorCpf(cpf);
            return response.getNome();
        } catch (FeignException e) {
            return null;
        }
    }
}
