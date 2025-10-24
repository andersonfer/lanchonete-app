package br.com.lanchonete.cozinha.infrastructure.config;

import br.com.lanchonete.cozinha.application.usecases.IniciarPreparoPedidoUseCase;
import br.com.lanchonete.cozinha.application.usecases.ListarPedidosCozinhaUseCase;
import br.com.lanchonete.cozinha.application.usecases.MarcarPedidoComoProntoUseCase;
import br.com.lanchonete.cozinha.domain.repository.PedidoCozinhaRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    @Bean
    public ListarPedidosCozinhaUseCase listarPedidosCozinhaUseCase(PedidoCozinhaRepository repository) {
        return new ListarPedidosCozinhaUseCase(repository);
    }

    @Bean
    public IniciarPreparoPedidoUseCase iniciarPreparoPedidoUseCase(PedidoCozinhaRepository repository) {
        return new IniciarPreparoPedidoUseCase(repository);
    }

    @Bean
    public MarcarPedidoComoProntoUseCase marcarPedidoComoProntoUseCase(PedidoCozinhaRepository repository) {
        return new MarcarPedidoComoProntoUseCase(repository);
    }
}
