package br.com.lanchonete.clientes.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API de Clientes - Lanchonete")
                        .version("1.0.0")
                        .description("API para gestão de clientes do sistema de lanchonete")
                        .contact(new Contact()
                                .name("Equipe Lanchonete")
                                .email("contato@lanchonete.com.br")));
    }
}
