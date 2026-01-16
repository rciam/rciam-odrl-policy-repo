package gr.grnet.rciam.odrl.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import gr.grnet.rciam.odrl.domain.PolicyEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@ApplicationScoped
public class PolicyRepository implements PanacheRepositoryBase<PolicyEntity, UUID> {

    public List<PolicyEntity> search(String status, String type, 
                                     String target, String assigner, String assignee, 
                                     String q, int page, int size) {
        
        StringBuilder sql = new StringBuilder("SELECT * FROM policies p WHERE 1=1");
        Map<String, Object> params = new HashMap<>();

        if (status != null) {
            sql.append(" AND p.status = :status");
            params.put("status", status);
        }
        if (type != null) {
            sql.append(" AND p.policy_type = :type");
            params.put("type", type);
        }
        if (q != null) {
            sql.append(" AND (LOWER(p.name) LIKE :q OR LOWER(p.description) LIKE :q)");
            params.put("q", "%" + q.toLowerCase() + "%");
        }

        if (target != null) {
            sql.append(" AND (jsonb_path_exists(p.odrl_policy, '$.**.target ? (@ == $target)', jsonb_build_object('target', :target)))");
            params.put("target", target);
        }

        if (assignee != null) {
            sql.append(" AND (jsonb_path_exists(p.odrl_policy, '$.**.assignee ? (@ == $assignee)', jsonb_build_object('assignee', :assignee)))");
            params.put("assignee", assignee);
        }

        if (assigner != null) {
            sql.append(" AND (jsonb_path_exists(p.odrl_policy, '$.**.assigner ? (@ == $assigner)', jsonb_build_object('assigner', :assigner)))");
            params.put("assigner", assigner);
        }

        sql.append(" ORDER BY p.created_at DESC");

        Query query = getEntityManager().createNativeQuery(sql.toString(), PolicyEntity.class);
        params.forEach(query::setParameter);

        query.setFirstResult(page * size);
        query.setMaxResults(size);

        return query.getResultList();
    }
}
