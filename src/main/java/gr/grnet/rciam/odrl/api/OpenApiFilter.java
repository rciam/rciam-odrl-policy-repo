package gr.grnet.rciam.odrl.api;

import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.openapi.OASFactory;
import org.eclipse.microprofile.openapi.OASFilter;
import org.eclipse.microprofile.openapi.models.Components;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.models.security.SecurityScheme;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class OpenApiFilter implements OASFilter {

    private final String tokenUrl;

    public OpenApiFilter() {
        this.tokenUrl = ConfigProvider.getConfig()
            .getOptionalValue("openapi.oidc.token-url", String.class)
            .orElse(null);
    }

    @Override
    public void filterOpenAPI(OpenAPI openAPI) {
        if (openAPI == null) return;

        Components components = openAPI.getComponents();
        if (components == null) {
            components = OASFactory.createObject(Components.class);
            openAPI.setComponents(components);
        }

        Map<String, SecurityScheme> schemes = components.getSecuritySchemes();
        if (schemes == null) {
            schemes = new LinkedHashMap<>();
            components.setSecuritySchemes(schemes);
        }

        // 1. Patch the 'oauth2' scheme from openapi.yaml with the real token URL
        SecurityScheme oauth2 = schemes.get("oauth2");
        if (oauth2 != null
                && oauth2.getFlows() != null
                && oauth2.getFlows().getClientCredentials() != null
                && tokenUrl != null
                && !tokenUrl.isBlank()) {
            oauth2.getFlows().getClientCredentials().setTokenUrl(tokenUrl);
        }

        // 2. Add 'BearerAuth' as a manual paste alternative
        schemes.putIfAbsent("BearerAuth",
            OASFactory.createObject(SecurityScheme.class)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Manual: Paste an access token here"));

        // 3. Register 'BearerAuth' as a top-level security requirement
        List<SecurityRequirement> security = openAPI.getSecurity();
        if (security == null) {
            security = new ArrayList<>();
            openAPI.setSecurity(security);
        }

        if (security.stream().noneMatch(req -> req.hasScheme("BearerAuth"))) {
            security.add(OASFactory.createObject(SecurityRequirement.class)
                .addScheme("BearerAuth"));
        }
    }
}
