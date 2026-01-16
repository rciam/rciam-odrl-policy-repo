package gr.grnet.rciam.odrl.domain;

import com.fasterxml.jackson.databind.JsonNode;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "policies")
@Data
@EqualsAndHashCode(callSuper = false)
public class PolicyEntity extends PanacheEntityBase {

    @Id
    @GeneratedValue
    public UUID id;

    @Column(nullable = false)
    public String name;

    @Column(columnDefinition = "TEXT")
    public String description;

    public String version;

    @Enumerated(EnumType.STRING)
    @Column(name = "policy_type", nullable = false)
    public PolicyType policyType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public PolicyStatus status;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "policy_labels", joinColumns = @JoinColumn(name = "policy_id"))
    @Column(name = "label")
    public List<String> labels;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "odrl_policy", columnDefinition = "jsonb")
    public JsonNode odrlPolicy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    public Instant createdAt;

    @Column(name = "created_by")
    public String createdBy;

    @UpdateTimestamp
    @Column(name = "updated_at")
    public Instant updatedAt;

    public enum PolicyType {
        set, offer, agreement, request, unknown
    }

    public enum PolicyStatus {
        draft, published, deprecated
    }
}
