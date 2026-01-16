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
    public Response list(
            @QueryParam("status") String status,
            @QueryParam("policyType") String policyType,
            @QueryParam("target") String target,
            @QueryParam("assigner") String assigner,
            @QueryParam("assignee") String assignee,
            @QueryParam("q") String q,
            @QueryParam("limit") @DefaultValue("20") int limit,
            @QueryParam("offset") @DefaultValue("0") int offset) {
        
        int pageIndex = offset / limit;
        var items = service.list(status, policyType, target, assigner, assignee, q, pageIndex, limit);
        return Response.ok(Map.of("items", items, "limit", limit, "offset", offset)).build();
    }

    @POST
    public Response create(@Valid PolicyInput input) {
        PolicyEntity created = service.create(input);
        return Response.created(URI.create("/policies/" + created.getId())).entity(created).build();
    }

    @GET
    @Path("/{policyId}")
    public Response get(@PathParam("policyId") UUID policyId) {
        return Response.ok(service.findById(policyId)).build();
    }

    @PUT
    @Path("/{policyId}")
    public Response update(@PathParam("policyId") UUID policyId, @Valid PolicyInput input) {
        return Response.ok(service.update(policyId, input)).build();
    }

    @POST
    @Path("/{policyId}/validate")
    public Response validate(@PathParam("policyId") UUID policyId, PolicyInput input) {
        return Response.ok(service.validate(policyId, input)).build();
    }
}
