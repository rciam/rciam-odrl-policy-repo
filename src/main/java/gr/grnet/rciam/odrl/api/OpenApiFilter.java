package gr.grnet.rciam.odrl.api;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.LinkedHashMap;
import java.util.Optional;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.OASFactory;
import org.eclipse.microprofile.openapi.OASFilter;
import org.eclipse.microprofile.openapi.models.Components;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.security.OAuthFlow;
import org.eclipse.microprofile.openapi.models.security.OAuthFlows;
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

        // 1. Define the "Paste Token" Scheme
        components.getSecuritySchemes().putIfAbsent(
            "BearerAuth",
            OASFactory.createObject(SecurityScheme.class)
                .type(Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Paste your Access Token here directly.")
        );

        // 2. Update OIDC Token URL (Preserving Scopes from YAML)
        SecurityScheme oauth2 = components.getSecuritySchemes().get("oauth2");

        // Only proceed if YAML defined 'oauth2' and we have a dynamic URL to inject
        if (oauth2 != null && tokenUrl.isPresent() && !tokenUrl.get().isBlank()) {
             // Ensure we are working with OAuth2 type
            if (oauth2.getType() == Type.OAUTH2) {
                OAuthFlows flows = oauth2.getFlows();
                if (flows == null) {
                    flows = OASFactory.createObject(OAuthFlows.class);
                    oauth2.setFlows(flows);
                }

                OAuthFlow cc = flows.getClientCredentials();
                if (cc == null) {
                    cc = OASFactory.createObject(OAuthFlow.class);
                    flows.setClientCredentials(cc);
                }

                cc.setTokenUrl(tokenUrl.get());
            }
        }

        // 3. Apply "Paste Token" Globally
        // This ensures the BearerAuth option actually works on all endpoints
        openAPI.addSecurityRequirement(
            OASFactory.createObject(SecurityRequirement.class).addScheme("BearerAuth")
        );
    }
}
