package io.github.haroldbaes17.minecraftfacts.config;

import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI OpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Minecraft Facts API")
                        .version("v1")
                        .description("API REST de datos curiosos sobre Minecraft")
                        .contact(new Contact()
                                .name("Harold Barrantes Estrada")
                                .email("haroldbaes17@gmail.com")))
                .servers(List.of(
                        new Server().url("/v1/api").description("Servidor local")
                ));

    }
}
