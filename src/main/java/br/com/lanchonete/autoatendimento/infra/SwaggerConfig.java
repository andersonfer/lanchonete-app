package br.com.lanchonete.autoatendimento.infra;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API de Autoatendimento Para Lanchonetes")
                        .description("API REST para gerenciamento de produtos e pedidos.")
                        .version("v1.0.0"));
    }
}
