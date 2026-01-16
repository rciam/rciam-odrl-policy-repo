package gr.grnet.rciam.odrl.dto;

import com.fasterxml.jackson.databind.JsonNode;
import gr.grnet.rciam.odrl.domain.PolicyEntity;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record PolicyInput(
    @NotNull String name,
    String description,
    String version,
    @NotNull PolicyEntity.PolicyType policyType,
    @NotNull PolicyEntity.PolicyStatus status,
    List<String> labels,
    @NotNull JsonNode odrlPolicy
) {}
