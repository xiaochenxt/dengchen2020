package io.github.dengchen2020.jpa.hibernate;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

import java.util.Locale;

import static java.lang.Character.*;
import static java.lang.Character.isDigit;
import static java.lang.Character.isLowerCase;

/**
 * 从Hibernate7.0.0开始，修复了字段名映射错误的bug，该bug曾将字段h5Html映射为h5html，正确应为h5_html。 </br>
 * 据我个人的使用经验，从未命中过该bug，一方面是我几乎从未在数据库字段中使用过阿拉伯数字，另一方面是我很早就发现了这个bug并刻意的避开了。这里同步7.0.0的变更可以避免未来潜在的bug同时保持与其他框架的一致性。
 * Converts {@code camelCase} or {@code MixedCase} logical names to {@code snake_case}.
 *
 * @author Phillip Webb
 * @author Madhura Bhave
 */
// Originally copied from Spring's SpringPhysicalNamingStrategy as this strategy is popular there.
public class PhysicalNamingStrategySnakeCaseImpl implements PhysicalNamingStrategy {

    @Override
    public Identifier toPhysicalCatalogName(Identifier logicalName, JdbcEnvironment jdbcEnvironment) {
        return apply( logicalName );
    }

    @Override
    public Identifier toPhysicalSchemaName(Identifier logicalName, JdbcEnvironment jdbcEnvironment) {
        return apply( logicalName );
    }

    @Override
    public Identifier toPhysicalTableName(Identifier logicalName, JdbcEnvironment jdbcEnvironment) {
        return apply( logicalName );
    }

    @Override
    public Identifier toPhysicalSequenceName(Identifier logicalName, JdbcEnvironment jdbcEnvironment) {
        return apply( logicalName );
    }

    @Override
    public Identifier toPhysicalColumnName(Identifier logicalName, JdbcEnvironment jdbcEnvironment) {
        return apply( logicalName );
    }

    private Identifier apply(final Identifier name) {
        if ( name == null ) {
            return null;
        }
        else if ( name.isQuoted() ) {
            return quotedIdentifier( name );
        }
        else {
            return unquotedIdentifier( name );
        }
    }

    private String camelCaseToSnakeCase(String name) {
        final StringBuilder builder = new StringBuilder( name.replace( '.', '_' ) );
        for ( int i = 1; i < builder.length() - 1; i++ ) {
            if ( isUnderscoreRequired( builder.charAt( i - 1 ), builder.charAt( i ), builder.charAt( i + 1 ) ) ) {
                builder.insert( i++, '_' );
            }
        }
        return builder.toString();
    }

    protected Identifier unquotedIdentifier(Identifier name) {
        return new Identifier( camelCaseToSnakeCase( name.getText() ).toLowerCase( Locale.ROOT ), false);
    }

    protected Identifier quotedIdentifier(Identifier quotedName) {
        return quotedName;
    }

    private boolean isUnderscoreRequired(final char before, final char current, final char after) {
        return ( isLowerCase( before ) || isDigit( before ) )
                && isUpperCase( current )
                && ( isLowerCase( after ) || isDigit( after ) );
    }
}
