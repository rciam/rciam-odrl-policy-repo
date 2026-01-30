package gr.grnet.rciam.odrl.api;

import java.util.LinkedHashMap;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.openapi.OASFactory;
import org.eclipse.microprofile.openapi.OASFilter;
import org.eclipse.microprofile.openapi.models.Components;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.security.OAuthFlow;
import org.eclipse.microprofile.openapi.models.security.OAuthFlows;
import org.eclipse.microprofile.openapi.models.security.SecurityScheme;
import org.eclipse.microprofile.openapi.models.security.SecurityScheme.Type;

public class OpenApiFilter implements OASFilter {

    private final String tokenUrl;

    public OpenApiFilter() {
        Config cfg = ConfigProvider.getConfig();
        this.tokenUrl = cfg.getOptionalValue("openapi.oidc.token-url", String.class).orElse(null);
    }

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

        components.getSecuritySchemes().putIfAbsent(
            "BearerAuth",
            OASFactory.createObject(SecurityScheme.class)
                .type(Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
        );

        if (tokenUrl == null || tokenUrl.isBlank()) {
            return;
        }

        SecurityScheme oauth2 = components.getSecuritySchemes().get("oauth2");
        if (oauth2 == null) {
            return;
        }
        if (oauth2.getType() != null && oauth2.getType() != Type.OAUTH2) {
            return;
        }

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

        cc.setTokenUrl(tokenUrl);
    }
}
