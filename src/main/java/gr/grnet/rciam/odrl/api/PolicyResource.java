package gr.grnet.rciam.odrl.api;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import gr.grnet.rciam.odrl.domain.PolicyEntity;
import gr.grnet.rciam.odrl.dto.PolicyInput;
import gr.grnet.rciam.odrl.service.PolicyService;

import java.net.URI;
import java.util.Map;
import java.util.UUID;

@Path("/policies")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PolicyResource {

    @Inject PolicyService service;

    @GET
    @RolesAllowed("policies:read")
    public Response list(
            @QueryParam("status") String status,
            @QueryParam("policyType") String policyType,
            @QueryParam("target") String target,
            @QueryParam("assigner") String assigner,
            @QueryParam("assignee") String assignee,
            @QueryParam("q") String q,
            @QueryParam("limit") @DefaultValue("20") int limit,
            @QueryParam("offset") @DefaultValue("0") int offset) {

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
    public Response create(@Valid PolicyInput input) {
        PolicyEntity created = service.create(input);

        URI location = uriInfo.getAbsolutePathBuilder()
                              .path(created.getId().toString())
                              .build();

        return Response.created(location)
                .entity(created)
                .build();
    }

    @GET
    @Path("/{policyId}")
    @RolesAllowed("policies:read")
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
    @RolesAllowed("policies:write")
    @Consumes("application/merge-patch+json")
    public Response patch(@PathParam("policyId") UUID policyId, JsonNode patch) {
        return Response.ok(service.patch(policyId, patch)).build();
    }

    @DELETE
    @Path("/{policyId}")
    @RolesAllowed("policies:write")
    public Response delete(@PathParam("policyId") UUID policyId) {
        service.delete(policyId);
        return Response.noContent().build();
    }

    @POST
    @Path("/{policyId}/validate")
    @RolesAllowed("policies:read")
    public Response validate(@PathParam("policyId") UUID policyId, PolicyInput input) {
        return Response.ok(service.validate(policyId, input)).build();
    }
}
