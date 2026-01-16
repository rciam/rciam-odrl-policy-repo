package gr.grnet.rciam.odrl.service;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.enterprise.context.ApplicationScoped;
import gr.grnet.rciam.odrl.dto.ValidationResult;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class OdrValidator {
    public ValidationResult validate(JsonNode policy) {
        List<ValidationResult.ValidationError> errors = new ArrayList<>();
        if (!policy.has("uid")) {
            errors.add(new ValidationResult.ValidationError("ODRL-001", "Policy MUST have a uid", "/", "model"));
        }
        boolean hasRules = policy.has("permission") || policy.has("prohibition") || policy.has("obligation");
        if (!hasRules) {
             errors.add(new ValidationResult.ValidationError("ODRL-002", "Policy MUST have at least one permission, prohibition or obligation", "/", "model"));
        }
        return new ValidationResult(errors.isEmpty(), errors);
    }
}
