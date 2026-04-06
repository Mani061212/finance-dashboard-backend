package com.finance.dashboard.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.*;
import io.swagger.v3.oas.annotations.info.*;
import io.swagger.v3.oas.annotations.security.*;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title   = "Finance Dashboard API",
                version = "1.0",
                description = "Role-based financial records management. " +
                              "Roles: VIEWER (recent only) | ANALYST (own transactions + analytics) " +
                              "| ADMIN (full access).",
                contact = @Contact(name = "Finance Team")
        ),
        security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
        name         = "bearerAuth",
        type         = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme       = "bearer",
        in           = SecuritySchemeIn.HEADER,
        description  = "Paste the access token from POST /api/auth/login"
)
public class OpenApiConfig {}
