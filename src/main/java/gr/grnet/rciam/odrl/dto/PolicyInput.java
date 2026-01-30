package gr.grnet.rciam.odrl.dto;

import com.fasterxml.jackson.databind.JsonNode;
import gr.grnet.rciam.odrl.domain.PolicyEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record PolicyInput(
    @NotBlank(message = "Policy Name is required")
    String name,

    @NotBlank(message = "Policy Description is required")
    String description,

    String version,

    @NotNull(message = "Policy Type is required")
    PolicyEntity.PolicyType policyType,

    @NotNull(message = "Policy Status is required")
    PolicyEntity.PolicyStatus status,

    List<String> labels,

    @NotNull(message = "ODRL Policy content is required")
    JsonNode odrlPolicy
) {}
