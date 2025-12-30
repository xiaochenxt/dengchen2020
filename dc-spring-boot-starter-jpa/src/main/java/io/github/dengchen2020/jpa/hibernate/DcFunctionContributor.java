package io.github.dengchen2020.jpa.hibernate;

import org.hibernate.boot.model.FunctionContributions;
import org.hibernate.boot.model.FunctionContributor;
import org.hibernate.dialect.*;
import org.hibernate.type.StandardBasicTypes;

/**
 * 在hibernate中注册一些hibernate自身没有的数据库的专有函数
 * @author xiaochen
 * @since 2025/12/30
 */
public class DcFunctionContributor implements FunctionContributor {
    @Override
    public void contributeFunctions(FunctionContributions functionContributions) {
        var registry = functionContributions.getFunctionRegistry();
        var configuration = functionContributions.getTypeConfiguration();
        var stringBasicType = configuration.getBasicTypeRegistry().resolve(StandardBasicTypes.STRING);
        var doubleBasicType = configuration.getBasicTypeRegistry().resolve(StandardBasicTypes.DOUBLE);
        var dialect = functionContributions.getDialect();
        if (dialect instanceof PostgreSQLDialect) {
            registry.registerPattern("jsonb_get_str", "?1 ->> ?2", stringBasicType);
            registry.registerPattern("jsonb_get_str_by_patharr", "?1 #>> ?2", stringBasicType);
        } else if (dialect instanceof MySQLDialect) {
            registry.registerPattern("random", "rand()", doubleBasicType);
        } else if (dialect instanceof AbstractTransactSQLDialect) {
            registry.registerPattern("random", "rand()", doubleBasicType);
        } else if (dialect instanceof OracleDialect) {
            registry.registerPattern("random", "DBMS_RANDOM.VALUE()", doubleBasicType);
        } else if (dialect instanceof H2Dialect) {
            registry.registerPattern("random", "rand()", doubleBasicType);
        }
    }
}
