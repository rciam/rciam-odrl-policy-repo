package gr.grnet.rciam.odrl.dto;

import java.util.List;

public record ValidationResult(boolean valid, List<ValidationError> errors) {
    public record ValidationError(String code, String message, String path, String category) {}
}
