package com.example.userservice.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "User Service API",
        version = "1.0.0",
        description = "Pure CRUD operations for User management in MSA Architecture"
    ),
    servers = {
        @Server(url = "http://localhost:8082", description = "User Service"),
        @Server(url = "http://localhost:8081/api/users", description = "Via Gateway")
    }
)
public class OpenApiConfig {
}