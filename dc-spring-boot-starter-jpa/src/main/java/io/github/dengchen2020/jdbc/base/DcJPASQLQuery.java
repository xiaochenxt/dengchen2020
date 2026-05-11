package io.github.dengchen2020.jdbc.base;

import com.querydsl.core.QueryResults;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.FactoryExpression;
import com.querydsl.jpa.impl.JPAUtil;
import com.querydsl.jpa.sql.JPASQLQuery;
import com.querydsl.sql.SQLSerializer;
import com.querydsl.sql.SQLTemplates;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import java.util.*;

final class DcJPASQLQuery<T> extends JPASQLQuery<T> {

    private final EntityManager entityManager;

    public DcJPASQLQuery(EntityManager entityManager, SQLTemplates sqlTemplates) {
        super(entityManager, sqlTemplates);
        this.entityManager = entityManager;
    }

    @Override
    protected SQLSerializer createSerializer() {
        return new DcNativeSQLSerializer(configuration, queryHandler.wrapEntityProjections());
    }

    public Query createQuery() {
        return createQuery(false);
    }

    private Query createQuery(boolean forCount) {
        var serializer = (DcNativeSQLSerializer) serialize(forCount);
        var queryString = serializer.toString();
        logQuery(queryString);
        Expression<?> projection = queryMixin.getMetadata().getProjection();
        Query query;

        if (!FactoryExpression.class.isAssignableFrom(projection.getClass())
                && isEntityExpression(projection)) {
            if (queryHandler.createNativeQueryTyped()) {
                query = entityManager.createNativeQuery(queryString, projection.getType());
            } else {
                query = entityManager.createNativeQuery(queryString);
            }

        } else {
            query = entityManager.createNativeQuery(queryString);
        }
        if (!forCount) {
            var aliases = serializer.getAliases();
            Set<String> used = new HashSet<>();
            if (projection instanceof FactoryExpression) {
                for (Expression<?> expr : ((FactoryExpression<?>) projection).getArgs()) {
                    if (isEntityExpression(expr)) {
                        queryHandler.addEntity(query, extractEntityExpression(expr).toString(), expr.getType());
                    } else if (aliases.containsKey(expr)) {
                        for (String scalar : aliases.get(expr)) {
                            if (!used.contains(scalar)) {
                                queryHandler.addScalar(query, scalar, expr.getType());
                                used.add(scalar);
                                break;
                            }
                        }
                    }
                }
            } else if (isEntityExpression(projection)) {
                queryHandler.addEntity(
                        query, extractEntityExpression(projection).toString(), projection.getType());
            } else if (aliases.containsKey(projection)) {
                for (String scalar : aliases.get(projection)) {
                    if (!used.contains(scalar)) {
                        queryHandler.addScalar(query, scalar, projection.getType());
                        used.add(scalar);
                        break;
                    }
                }
            }
        }

        if (lockMode != null) {
            query.setLockMode(lockMode);
        }
        if (flushMode != null) {
            query.setFlushMode(flushMode);
        }

        for (Map.Entry<String, Object> entry : hints.entrySet()) {
            query.setHint(entry.getKey(), entry.getValue());
        }

        // set constants
        JPAUtil.setConstants(query, serializer.getConstants(), queryMixin.getMetadata().getParams());
        this.projection = null; // necessary when query is reused

        if (!forCount && projection instanceof FactoryExpression) {
            if (!queryHandler.transform(query, (FactoryExpression<?>) projection)) {
                this.projection = (FactoryExpression<?>) projection;
            }
        }

        return query;
    }

    @Override
    public QueryResults<T> fetchResults() {
        // TODO : handle entity projections as well
        try {
            var countQuery = createQuery(true);
            var total = ((Number) countQuery.getSingleResult()).longValue();
            if (total > 0) {
                var modifiers = queryMixin.getMetadata().getModifiers();
                var query = createQuery(false);
                @SuppressWarnings("unchecked")
                var list = (List<T>) getResultList(query);
                return new QueryResults<>(list, modifiers, total);
            } else {
                return QueryResults.emptyResults();
            }
        } finally {
            reset();
        }
    }

    /**
     * Transforms results using FactoryExpression if ResultTransformer can't be used
     *
     * @param query query
     * @return results
     */
    private List<?> getResultList(Query query) {
        // TODO : use lazy fetch here?
        if (projection != null) {
            List<?> results = query.getResultList();
            List<Object> rv = new ArrayList<>(results.size());
            for (Object o : results) {
                if (o != null) {
                    Object[] arr;
                    if (!o.getClass().isArray()) {
                        arr = new Object[] {o};
                    } else {
                        arr = (Object[]) o;
                    }
                    if (projection.getArgs().size() < arr.length) {
                        var shortened = new Object[projection.getArgs().size()];
                        System.arraycopy(arr, 0, shortened, 0, shortened.length);
                        arr = shortened;
                    }
                    rv.add(projection.newInstance(arr));
                } else {
                    rv.add(null);
                }
            }
            return rv;
        } else {
            return query.getResultList();
        }
    }

}
