package gr.grnet.rciam.odrl.service;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import gr.grnet.rciam.odrl.domain.PolicyEntity;
import gr.grnet.rciam.odrl.dto.PolicyInput;
import gr.grnet.rciam.odrl.dto.ValidationResult;
import gr.grnet.rciam.odrl.repository.PolicyRepository;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class PolicyService {

    @Inject PolicyRepository repository;
    @Inject OdrValidator validator;

    public List<PolicyEntity> list(String status, String type, String target, String assigner, String assignee, String q, int offset, int limit) {
        return repository.search(status, type, target, assigner, assignee, q, offset, limit);
    }

    public long count(String status, String type, String target, String assigner, String assignee, String q) {
        return repository.count(status, type, target, assigner, assignee, q);
    }

    @Transactional
    public PolicyEntity create(PolicyInput input) {
        PolicyEntity entity = new PolicyEntity();
        mapInputToEntity(input, entity);
        repository.persist(entity);
        return entity;
    }

    public PolicyEntity findById(UUID id) {
        return repository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Policy not found"));
    }

    @Transactional
    public PolicyEntity update(UUID id, PolicyInput input) {
        PolicyEntity entity = findById(id);
        mapInputToEntity(input, entity);
        return entity;
    }

    @Transactional
    public PolicyEntity patch(UUID id, JsonNode patch) {
        PolicyEntity entity = findById(id);

        if (patch.has("name")) {
            entity.setName(patch.get("name").isNull() ? null : patch.get("name").asText());
        }
        if (patch.has("description")) {
            entity.setDescription(patch.get("description").isNull() ? null : patch.get("description").asText());
        }
        if (patch.has("version")) {
            entity.setVersion(patch.get("version").isNull() ? null : patch.get("version").asText());
        }
        if (patch.has("status")) {
            entity.setStatus(patch.get("status").isNull() ? null :
                            PolicyEntity.PolicyStatus.valueOf(patch.get("status").asText()));
        }
        if (patch.has("policyType")) {
            entity.setPolicyType(patch.get("policyType").isNull() ? null :
                                PolicyEntity.PolicyType.valueOf(patch.get("policyType").asText()));
        }
        if (patch.has("odrlPolicy")) {
            entity.setOdrlPolicy(patch.get("odrlPolicy").isNull() ? null : patch.get("odrlPolicy"));
        }
        return entity;
    }

    @Transactional
    public void delete(UUID id) {
        PolicyEntity entity = findById(id);
        entity.setStatus(PolicyEntity.PolicyStatus.deprecated);
    }

    public ValidationResult validate(UUID id, PolicyInput candidate) {
        JsonNode policyToValidate = (candidate != null) ?
                candidate.odrlPolicy() : findById(id).getOdrlPolicy();
        return validator.validate(policyToValidate);
    }

    private void mapInputToEntity(PolicyInput input, PolicyEntity entity) {
        entity.setName(input.name());
        entity.setDescription(input.description());
        entity.setVersion(input.version());
        entity.setPolicyType(input.policyType());
        entity.setStatus(input.status());
        entity.setLabels(input.labels());
        entity.setOdrlPolicy(input.odrlPolicy());
    }
}
