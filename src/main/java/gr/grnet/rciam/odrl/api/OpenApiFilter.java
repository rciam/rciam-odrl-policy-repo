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
        if (openAPI == null) {
            return;
        }

        Components components = openAPI.getComponents();
        if (components == null) {
            components = OASFactory.createObject(Components.class);
            openAPI.setComponents(components);
        }

        // --- FIX: Create a MUTABLE copy of the schemes map ---
        Map<String, SecurityScheme> mutableSchemes = new LinkedHashMap<>();
        if (components.getSecuritySchemes() != null) {
            mutableSchemes.putAll(components.getSecuritySchemes());
        }

        // 1. Patch the 'oauth2' scheme from openapi.yaml with the real token URL
        SecurityScheme oauth2 = mutableSchemes.get("oauth2");
        if (oauth2 != null
                && oauth2.getFlows() != null
                && oauth2.getFlows().getClientCredentials() != null
                && tokenUrl != null
                && !tokenUrl.isBlank()) {
            oauth2.getFlows().getClientCredentials().setTokenUrl(tokenUrl);
        }

        // 2. Add 'BearerAuth' as a manual paste alternative
        mutableSchemes.putIfAbsent("BearerAuth",
            OASFactory.createObject(SecurityScheme.class)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Manual: Paste access token with required policies:* scope here"));

        // Write the mutable map back to the components
        components.setSecuritySchemes(mutableSchemes);

        // --- FIX: Create a MUTABLE copy of the security requirements list ---
        List<SecurityRequirement> mutableSecurity = new ArrayList<>();
        if (openAPI.getSecurity() != null) {
            mutableSecurity.addAll(openAPI.getSecurity());
        }

        // 3. Register 'BearerAuth' as a top-level security requirement
        if (mutableSecurity.stream().noneMatch(req -> req.hasScheme("BearerAuth"))) {
            mutableSecurity.add(OASFactory.createObject(SecurityRequirement.class)
                .addScheme("BearerAuth"));
        }

        // Write the mutable list back to the openAPI object
        openAPI.setSecurity(mutableSecurity);
    }
}
