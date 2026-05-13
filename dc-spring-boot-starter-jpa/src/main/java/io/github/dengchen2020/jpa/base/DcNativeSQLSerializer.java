package io.github.dengchen2020.jpa.base;

import com.querydsl.core.JoinExpression;
import com.querydsl.core.QueryMetadata;
import com.querydsl.core.types.*;
import com.querydsl.sql.ColumnMetadata;
import com.querydsl.sql.Configuration;
import com.querydsl.sql.SQLOps;
import com.querydsl.sql.SQLSerializer;
import jakarta.persistence.Column;
import jakarta.persistence.Table;

import java.util.*;

import static java.lang.Character.*;

/**
 * {@code DcNativeSQLSerializer} extends {@link SQLSerializer} to extract referenced entity paths and
 * change some serialization formats
 *
 * @author tiwe
 */
final class DcNativeSQLSerializer extends SQLSerializer {

    private final Map<Expression<?>, List<String>> aliases = new HashMap<>();

    public DcNativeSQLSerializer(Configuration configuration) {
        super(configuration);
    }

    @Override
    protected void appendAsColumnName(Path<?> path, boolean precededByDot) {
        if (path.getAnnotatedElement().isAnnotationPresent(Column.class)) {
            var column = path.getAnnotatedElement().getAnnotation(Column.class);
            if (!column.name().isEmpty()) {
                append(getTemplates().quoteIdentifier(column.name(), precededByDot));
            } else {
                append(getTemplates().quoteIdentifier(camelCaseToSnakeCase(ColumnMetadata.getName(path)), precededByDot));
            }
        } else {
            append(getTemplates().quoteIdentifier(camelCaseToSnakeCase(ColumnMetadata.getName(path)), precededByDot));
        }
    }

    @Override
    protected void handleJoinTarget(JoinExpression je) {
        var templates = getTemplates();
        Class<?> type = je.getTarget().getType();
        if (type.isAnnotationPresent(Table.class) && templates.isSupportsAlias()) {
            var table = type.getAnnotation(Table.class);
            boolean precededByDot;
            if (!table.schema().isEmpty() && templates.isPrintSchema()) {
                appendSchemaName(table.schema());
                append(".");
                precededByDot = true;
            } else {
                precededByDot = false;
            }
            appendTableName(table.name(), precededByDot);
            append(templates.getTableAlias());
        }
        super.handleJoinTarget(je);
    }

    private boolean isAlias(Expression<?> expr) {
        return expr instanceof Operation && ((Operation<?>) expr).getOperator() == Ops.ALIAS;
    }

    public Map<Expression<?>, List<String>> getAliases() {
        return aliases;
    }

    private boolean isAllExpression(Expression<?> expr) {
        if (expr instanceof Operation) {
            return ((Operation<?>) expr).getOperator() == SQLOps.ALL;
        } else if (expr instanceof TemplateExpression) {
            return ((TemplateExpression<?>) expr).getTemplate().toString().equals("*");
        } else {
            return false;
        }
    }

    @Override
    public void serialize(QueryMetadata metadata, boolean forCountRow) {
        // TODO get rid of this wrapping when Hibernate doesn't require unique aliases anymore
        var modified = false;
        Set<String> used = new HashSet<>();
        Expression<?> projection = metadata.getProjection();
        if (projection instanceof Path) {
            Path<?> path = (Path<?>) projection;
            if (!used.add(path.getMetadata().getName())) {
                var alias = "col_1";
                aliases.computeIfAbsent(projection, DcNativeSQLSerializer::createArrayList).add(alias);
                projection = ExpressionUtils.as(projection, alias);
                modified = true;
            } else if (path.getAnnotatedElement().isAnnotationPresent(Column.class)) {
                var column = path.getAnnotatedElement().getAnnotation(Column.class);
                if (!column.name().isEmpty()) {
                    aliases
                            .computeIfAbsent(projection, DcNativeSQLSerializer::createArrayList)
                            .add(column.name());
                } else {
                    aliases
                            .computeIfAbsent(projection, DcNativeSQLSerializer::createArrayList)
                            .add(camelCaseToSnakeCase(ColumnMetadata.getName(path)));
                }
            } else {
                aliases
                        .computeIfAbsent(projection, DcNativeSQLSerializer::createArrayList)
                        .add(camelCaseToSnakeCase(ColumnMetadata.getName(path)));
            }
        } else if (projection instanceof FactoryExpression) {
            FactoryExpression<?> factoryExpr = (FactoryExpression<?>) projection;
            List<Expression<?>> fargs = new ArrayList<>(factoryExpr.getArgs());
            for (var j = 0; j < fargs.size(); j++) {
                if (fargs.get(j) instanceof Path) {
                    Path<?> path = (Path<?>) fargs.get(j);
                    String columnName;
                    if (path.getAnnotatedElement().isAnnotationPresent(Column.class)) {
                        var column = path.getAnnotatedElement().getAnnotation(Column.class);
                        if (!column.name().isEmpty()) {
                            columnName = column.name();
                        } else {
                            columnName = camelCaseToSnakeCase(ColumnMetadata.getName(path));
                        }
                    } else {
                        columnName = camelCaseToSnakeCase(ColumnMetadata.getName(path));
                    }
                    if (!used.add(columnName)) {
                        var alias = "col_" + (j + 1);
                        aliases.computeIfAbsent(path, DcNativeSQLSerializer::createArrayList).add(alias);
                        fargs.set(j, ExpressionUtils.as(fargs.get(j), alias));
                    } else {
                        aliases.computeIfAbsent(path, DcNativeSQLSerializer::createArrayList).add(columnName);
                    }
                } else if (isAlias(fargs.get(j))) {
                    Operation<?> operation = (Operation<?>) fargs.get(j);
                    aliases
                            .computeIfAbsent(operation, DcNativeSQLSerializer::createArrayList)
                            .add(operation.getArg(1).toString());
                } else if (!isAllExpression(fargs.get(j))) {
                    var alias = "col_" + (j + 1);
                    aliases.computeIfAbsent(fargs.get(j), DcNativeSQLSerializer::createArrayList).add(alias);
                    fargs.set(j, ExpressionUtils.as(fargs.get(j), alias));
                }
            }
            projection = Projections.tuple(new ArrayList<>(fargs));
            modified = true;
        } else if (isAlias(projection)) {
            Operation<?> operation = (Operation<?>) projection;
            aliases
                    .computeIfAbsent(operation, DcNativeSQLSerializer::createArrayList)
                    .add(operation.getArg(1).toString());
        } else {
            // https://github.com/querydsl/querydsl/issues/80
            if (!isAllExpression(projection)) {
                var alias = "col_1";
                aliases.computeIfAbsent(projection, DcNativeSQLSerializer::createArrayList).add(alias);
                projection = ExpressionUtils.as(projection, alias);
                modified = true;
            }
        }
        if (modified) {
            metadata = metadata.clone();
            metadata.setProjection(projection);
        }
        super.serialize(metadata, forCountRow);
    }

    private static <T> ArrayList<T> createArrayList(Object key) {
        return new ArrayList<>();
    }

    @Override
    public void visitConstant(Object constant) {
        if (constant instanceof Collection<?>) {
            append("(");
            var first = true;
            for (Object element : ((Collection<?>) constant)) {
                if (!first) {
                    append(", ");
                }
                visitConstant(element);
                first = false;
            }
            append(")");
        } else {
            super.visitConstant(constant);
        }
    }

    @Override
    protected void serializeConstant(int parameterIndex, String constantLabel) {
        append("?");
        append(Integer.toString(parameterIndex));
    }

    private String camelCaseToSnakeCase(String name) {
        final StringBuilder builder = new StringBuilder( name.replace( '.', '_' ) );
        for ( int i = 1; i < builder.length() - 1; i++ ) {
            if ( isUnderscoreRequired( builder.charAt( i - 1 ), builder.charAt( i ), builder.charAt( i + 1 ) ) ) {
                builder.insert( i++, '_' );
            }
        }
        return builder.toString().toLowerCase(Locale.ROOT);
    }

    private boolean isUnderscoreRequired(final char before, final char current, final char after) {
        return ( isLowerCase( before ) || isDigit( before ) )
                && isUpperCase( current )
                && ( isLowerCase( after ) || isDigit( after ) );
    }

}
