package gr.grnet.rciam.odrl.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import gr.grnet.rciam.odrl.domain.PolicyEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import java.util.List;

@QuarkusTest
public class PolicyRepositoryTest {

    @Inject PolicyRepository repository;
    @Inject ObjectMapper mapper;

    @Test
    @Transactional
    public void testNativeJsonSearch() throws Exception {
        // 1. Setup Data
        PolicyEntity p1 = new PolicyEntity();
        p1.setName("Policy A");
        p1.setPolicyType(PolicyEntity.PolicyType.set);
        p1.setStatus(PolicyEntity.PolicyStatus.published);
        p1.setOdrlPolicy(mapper.readTree("{\"uid\": \"1\", \"permission\": [{\"target\": \"urn:asset:A\"}]}"));
        repository.persist(p1);

        // 2. Execute Search
        List<PolicyEntity> results = repository.search(null, null, "urn:asset:A", null, null, null, 0, 10);
        
        // 3. Verify
        Assertions.assertEquals(1, results.size(), "Should find exactly one policy matching target urn:asset:A");
        Assertions.assertEquals("Policy A", results.get(0).getName());
    }
}
