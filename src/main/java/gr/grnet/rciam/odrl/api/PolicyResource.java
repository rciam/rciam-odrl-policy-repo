package gr.grnet.rciam.odrl.api;

import java.net.URI;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import gr.grnet.rciam.odrl.domain.PolicyEntity;
import gr.grnet.rciam.odrl.dto.PolicyInput;
import gr.grnet.rciam.odrl.service.PolicyService;

@Path("/policies")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Policies", description = "Operations related to ODRL Policies")
@RolesAllowed({"policies:read", "policies:write"})
public class PolicyResource {

    @Inject
    PolicyService policyService;

    @GET
    @RolesAllowed("policies:read")
    @APIResponse(
        responseCode = "200",
        description = "List of policies",
        content = @Content(mediaType = MediaType.APPLICATION_JSON,
        schema = @Schema(type = SchemaType.ARRAY, implementation = PolicyEntity.class))
    )
    public Response getAllPolicies(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("10") int size) {
        return Response.ok(policyService.findAll(page, size)).build();
    }

    @GET
    @Path("/{id}")
    @RolesAllowed("policies:read")
    public Response getPolicy(@PathParam("id") String id) {
        return policyService.findById(id)
                .map(policy -> Response.ok(policy).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @POST
    @RolesAllowed("policies:write")
    public Response createPolicy(@Valid PolicyInput input, @Context UriInfo uriInfo) {
        PolicyEntity created = policyService.create(input);

        URI location = uriInfo.getAbsolutePathBuilder().path(created.id).build();

        return Response.created(location).entity(created).build();
    }

    @PATCH
    @Path("/{id}")
    @RolesAllowed("policies:write")
    public Response updatePolicy(@PathParam("id") String id, @Valid PolicyInput input) {
        return policyService.update(id, input)
                .map(policy -> Response.ok(policy).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed("policies:write")
    public Response deletePolicy(@PathParam("id") String id) {
        boolean deleted = policyService.delete(id);
        if (deleted) {
            return Response.noContent().build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }
}
