package br.com.lanchonete.autoatendimento.frameworks.config;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {SwaggerConfig.class})
class SwaggerConfigTest {

    @Autowired
    private OpenAPI customOpenAPI;

    @Test
    @DisplayName("Deve criar o bean customOpenAPI")
    void t1() {
        assertNotNull(customOpenAPI);
    }
}