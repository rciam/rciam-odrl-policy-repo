package gr.grnet.rciam.odrl.api;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.LinkedHashMap;
import java.util.Optional;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.OASFactory;
import org.eclipse.microprofile.openapi.OASFilter;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.Components;
import org.eclipse.microprofile.openapi.models.security.OAuthFlow;
import org.eclipse.microprofile.openapi.models.security.OAuthFlows;
import org.eclipse.microprofile.openapi.models.security.Scopes;
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
            components = OASFactory.createObject(Components.class);
            openAPI.setComponents(components);
        }

        if (components.getSecuritySchemes() == null) {
            components.setSecuritySchemes(new LinkedHashMap<>());
        }

        // 1. Add "Paste Token" Scheme (BearerAuth)
        SecurityScheme bearerScheme = OASFactory.createObject(SecurityScheme.class)
                .type(Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Paste your Access Token here directly.");

        components.getSecuritySchemes().put("BearerAuth", bearerScheme);

        // 2. Configure OAuth2 Scheme (OIDC)
        SecurityScheme oauth2 = components.getSecuritySchemes().get("oauth2");
        if (oauth2 == null) {
            oauth2 = OASFactory.createObject(SecurityScheme.class).type(Type.OAUTH2);
            components.getSecuritySchemes().put("oauth2", oauth2);
        } else if (oauth2.getType() != null && oauth2.getType() != Type.OAUTH2) {
            return;
        }

        if (tokenUrl.isPresent() && !tokenUrl.get().isBlank()) {
            OAuthFlow clientCreds = OASFactory.createObject(OAuthFlow.class);
            clientCreds.setTokenUrl(tokenUrl.get());

            // Scopes is an Interface in MP OpenAPI 3.1
            Scopes scopes = OASFactory.createObject(Scopes.class);
            scopes.addScope("policies:read", "Read policies and validate");
            scopes.addScope("policies:write", "Create, update and delete policies");

            clientCreds.setScopes(scopes);

            OAuthFlows flows = OASFactory.createObject(OAuthFlows.class);
            flows.setClientCredentials(clientCreds);

            oauth2.setFlows(flows);
        }

        // 3. Make "Paste Token" Available Globally
        SecurityRequirement req = OASFactory.createObject(SecurityRequirement.class)
                .addScheme("BearerAuth");

        openAPI.addSecurityRequirement(req);
    }
}
