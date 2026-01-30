package gr.grnet.rciam.odrl.api;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.Optional;
import java.util.LinkedHashMap;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.OASFilter;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.Components;
import org.eclipse.microprofile.openapi.models.security.OAuthFlow;
import org.eclipse.microprofile.openapi.models.security.OAuthFlows;
import org.eclipse.microprofile.openapi.models.security.OAuthScope;
import org.eclipse.microprofile.openapi.models.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.models.security.SecurityScheme;
import org.eclipse.microprofile.openapi.models.security.SecurityScheme.Type;

@ApplicationScoped
public class OpenApiFilter implements OASFilter {

    @ConfigProperty(name = "openapi.oidc.token-url")
    Optional<String> tokenUrl;

    @Override
    public void filterOpenAPI(OpenAPI openAPI) {
        Components components = openAPI.getComponents();
        if (components == null) {
            components = new Components();
            openAPI.setComponents(components);
        }

        if (components.getSecuritySchemes() == null) {
            components.setSecuritySchemes(new LinkedHashMap<>());
        }

        // 1. Add "Paste Token" Scheme (BearerAuth)
        components.getSecuritySchemes().put("BearerAuth",
            new SecurityScheme()
                .type(Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Paste your Access Token here directly.")
        );

        // 2. Configure OAuth2 Scheme (OIDC)
        SecurityScheme oauth2 = components.getSecuritySchemes().get("oauth2");
        if (oauth2 == null) {
            oauth2 = new SecurityScheme().type(Type.OAUTH2);
            components.getSecuritySchemes().put("oauth2", oauth2);
        } else if (oauth2.getType() != null && oauth2.getType() != Type.OAUTH2) {
            // Defensive: Abort if 'oauth2' exists but is the wrong type
            return;
        }

        if (tokenUrl.isPresent() && !tokenUrl.get().isBlank()) {
            OAuthFlow clientCreds = new OAuthFlow();
            clientCreds.setTokenUrl(tokenUrl.get());

            OAuthScope scopes = new OAuthScope();
            scopes.addScope("policies:read", "Read policies and validate");
            scopes.addScope("policies:write", "Create, update and delete policies");

            OAuthFlows flows = new OAuthFlows();
            flows.setClientCredentials(clientCreds);

            oauth2.setFlows(flows);
        }

        // 3. Make "Paste Token" Available Globally
        // This ensures the BearerAuth box appears on endpoints defined in your YAML,
        // giving you a choice between OIDC (Login) or Bearer (Paste).
        openAPI.addSecurityRequirement(
            new SecurityRequirement().addScheme("BearerAuth")
        );
    }
}
