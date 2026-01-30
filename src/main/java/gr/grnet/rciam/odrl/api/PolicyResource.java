package gr.grnet.rciam.odrl.api;

import com.fasterxml.jackson.databind.JsonNode;
import gr.grnet.rciam.odrl.domain.PolicyEntity;
import gr.grnet.rciam.odrl.dto.PolicyInput;
import gr.grnet.rciam.odrl.service.PolicyService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.net.URI;
import java.util.Map;
import java.util.UUID;

@Path("/policies")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Policies", description = "Operations related to ODRL Policies")
public class PolicyResource {

    @Inject
    PolicyService service;

    @GET
    @RolesAllowed("policies:read")
    @APIResponse(
        responseCode = "200",
        description = "Paginated list of policies",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = @Schema(
                type = SchemaType.OBJECT,
                example = "{\"items\": [], \"total\": 0, \"limit\": 20, \"offset\": 0}"
            )
        )
    )
    public Response list(
            @Parameter(description = "Filter by status") @QueryParam("status") String status,
            @Parameter(description = "Filter by policy type") @QueryParam("policyType") String policyType,
            @Parameter(description = "Filter by target") @QueryParam("target") String target,
            @Parameter(description = "Filter by assigner") @QueryParam("assigner") String assigner,
            @Parameter(description = "Filter by assignee") @QueryParam("assignee") String assignee,
            @Parameter(description = "Search query") @QueryParam("q") String q,
            @Parameter(description = "Max items per page") @QueryParam("limit") @DefaultValue("20") int limit,
            @Parameter(description = "Offset for pagination") @QueryParam("offset") @DefaultValue("0") int offset
    ) {
        var items = service.list(status, policyType, target, assigner, assignee, q, offset, limit);
        long total = service.count(status, policyType, target, assigner, assignee, q);

        return Response.ok(Map.of(
                "items", items,
                "total", total,
                "limit", limit,
                "offset", offset
        )).build();
    }

    @POST
    @RolesAllowed("policies:write")
    @APIResponse(responseCode = "201", description = "Policy created")
    public Response create(@Valid PolicyInput input, @Context UriInfo uriInfo) {
        PolicyEntity created = service.create(input);
        URI location = uriInfo.getAbsolutePathBuilder()
                .path(created.getId().toString())
                .build();
        return Response.created(location).entity(created).build();
    }

    @GET
    @Path("/{policyId}")
    @RolesAllowed("policies:read")
    @APIResponse(responseCode = "200", description = "Policy details")
    @APIResponse(responseCode = "404", description = "Policy not found")
    public Response get(@PathParam("policyId") UUID policyId) {
        return Response.ok(service.findById(policyId)).build();
    }

    @PUT
    @Path("/{policyId}")
    @RolesAllowed("policies:write")
    public Response update(@PathParam("policyId") UUID policyId, @Valid PolicyInput input) {
        return Response.ok(service.update(policyId, input)).build();
    }

    @PATCH
    @Path("/{policyId}")
    @Consumes("application/merge-patch+json")
    @RolesAllowed("policies:write")
    public Response patch(@PathParam("policyId") UUID policyId, JsonNode patch) {
        return Response.ok(service.patch(policyId, patch)).build();
    }

    @DELETE
    @Path("/{policyId}")
    @RolesAllowed("policies:write")
    @APIResponse(responseCode = "204", description = "Policy deleted")
    public Response delete(@PathParam("policyId") UUID policyId) {
        service.delete(policyId);
        return Response.noContent().build();
    }

    @POST
    @Path("/{policyId}/validate")
    @RolesAllowed("policies:read")
    @APIResponse(responseCode = "200", description = "Validation result")
    public Response validate(@PathParam("policyId") UUID policyId, PolicyInput candidate) {
        return Response.ok(service.validate(policyId, candidate)).build();
    }
}
