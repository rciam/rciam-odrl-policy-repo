package gr.grnet.rciam.odrl.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.Query;
import gr.grnet.rciam.odrl.domain.PolicyEntity;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class PolicyRepository implements PanacheRepositoryBase<PolicyEntity, UUID> {

    public List<PolicyEntity> search(String status, String type,
                                     String target, String assigner, String assignee,
                                     String q, int offset, int limit) {

        String sql = buildSearchQuery("SELECT * FROM policies p WHERE 1=1", status, type, q, target, assignee, assigner);

        Query query = getEntityManager().createNativeQuery(sql, PolicyEntity.class);
        bindParameters(query, status, type, q, target, assignee, assigner);

        query.setFirstResult(offset);
        query.setMaxResults(limit);

        return query.getResultList();
    }

    public long count(String status, String type,
                      String target, String assigner, String assignee,
                      String q) {

        String sql = buildSearchQuery("SELECT count(*) FROM policies p WHERE 1=1", status, type, q, target, assignee, assigner);

        Query query = getEntityManager().createNativeQuery(sql);
        bindParameters(query, status, type, q, target, assignee, assigner);

        return ((Number) query.getSingleResult()).longValue();
    }

    private String buildSearchQuery(String baseSql, String status, String type, String q, String target, String assignee, String assigner) {
        StringBuilder sql = new StringBuilder(baseSql);
        if (status != null) sql.append(" AND p.status = :status");
        if (type != null) sql.append(" AND p.policy_type = :type");
        if (q != null) sql.append(" AND (LOWER(p.name) LIKE :q OR LOWER(p.description) LIKE :q)");
        if (target != null) sql.append(" AND (jsonb_path_exists(p.odrl_policy, '$.**.target ? (@ == $target)', jsonb_build_object('target', :target)))");
        if (assignee != null) sql.append(" AND (jsonb_path_exists(p.odrl_policy, '$.**.assignee ? (@ == $assignee)', jsonb_build_object('assignee', :assignee)))");
        if (assigner != null) sql.append(" AND (jsonb_path_exists(p.odrl_policy, '$.**.assigner ? (@ == $assigner)', jsonb_build_object('assigner', :assigner)))");

        if (!baseSql.contains("count")) {
            sql.append(" ORDER BY p.created_at DESC");
        }
        return sql.toString();
    }

    private void bindParameters(Query query, String status, String type, String q, String target, String assignee, String assigner) {
        if (status != null) query.setParameter("status", status);
        if (type != null) query.setParameter("type", type);
        if (q != null) query.setParameter("q", "%" + q.toLowerCase() + "%");
        if (target != null) query.setParameter("target", target);
        if (assignee != null) query.setParameter("assignee", assignee);
        if (assigner != null) query.setParameter("assigner", assigner);
    }
}
