package org.jumpmind.pos.server.config;

import static org.jumpmind.pos.util.RestApiSupport.REST_API_TOKEN_HEADER_NAME;

import static io.swagger.v3.oas.models.security.SecurityScheme.In.HEADER;
import static io.swagger.v3.oas.models.security.SecurityScheme.Type.APIKEY;

import static java.util.Collections.singletonList;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {
    private static final String SECURITY_SCHEME_NAME = "APIToken";

    @Bean
    public OpenAPI api() {
        return new OpenAPI()
                .info(getInfo())
                .schemaRequirement(SECURITY_SCHEME_NAME, getSecurityScheme())
                .security(getSecurityRequirements());
    }

    private Info getInfo() {
        return new Info()
                .title("JumpMind Commerce API")
                .version("4.0.0");
    }

    private List<SecurityRequirement> getSecurityRequirements() {
        SecurityRequirement securityRequirement = new SecurityRequirement();
        securityRequirement.addList(SECURITY_SCHEME_NAME);

        return singletonList(securityRequirement);
    }

    private SecurityScheme getSecurityScheme() {
        SecurityScheme securityScheme = new SecurityScheme();

        securityScheme.type(APIKEY);
        securityScheme.in(HEADER);
        securityScheme.name(REST_API_TOKEN_HEADER_NAME);

        return securityScheme;
    }
}
