package io.github.dengchen2020.jpa.hibernate;

import org.hibernate.boot.model.FunctionContributions;
import org.hibernate.boot.model.FunctionContributor;
import org.hibernate.dialect.*;
import org.hibernate.metamodel.model.domain.ReturnableType;
import org.hibernate.query.sqm.function.AbstractSqmSelfRenderingFunctionDescriptor;
import org.hibernate.query.sqm.function.FunctionKind;
import org.hibernate.query.sqm.function.SqmFunctionDescriptor;
import org.hibernate.query.sqm.function.SqmFunctionRegistry;
import org.hibernate.query.sqm.produce.function.StandardArgumentsValidators;
import org.hibernate.query.sqm.produce.function.StandardFunctionArgumentTypeResolvers;
import org.hibernate.query.sqm.produce.function.StandardFunctionReturnTypeResolvers;
import org.hibernate.sql.ast.SqlAstTranslator;
import org.hibernate.sql.ast.spi.SqlAppender;
import org.hibernate.sql.ast.tree.SqlAstNode;
import org.hibernate.sql.ast.tree.expression.Literal;
import org.hibernate.sql.ast.tree.expression.QueryLiteral;
import org.hibernate.type.BasicType;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.spi.TypeConfiguration;

import java.util.List;

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
        var booleanBasicType = configuration.getBasicTypeRegistry().resolve(StandardBasicTypes.BOOLEAN);
        var dialect = functionContributions.getDialect();
        if (dialect instanceof PostgreSQLDialect) {
            registry.registerPattern("jsonb_get", "?1 -> ?2", stringBasicType);
            registry.registerPattern("jsonb_get_str", "?1 ->> ?2", stringBasicType);
            registry.registerPattern("jsonb_get_by_patharr", "?1 #> ?2", stringBasicType);
            registry.registerPattern("jsonb_get_str_by_patharr", "?1 #>> ?2", stringBasicType);
            registry.registerPattern("jsonb_exists", "jsonb_exists(?1,?2)", booleanBasicType);
            registry.registerPattern("json_contains","?1 @> ?2::jsonb", booleanBasicType);
        } else if (dialect instanceof MySQLDialect) {
            registry.registerPattern("random", "rand()", doubleBasicType);
            registry.registerPattern("json_contains","json_contains(?1,?2)", booleanBasicType);
            if (registry.findFunctionDescriptor("json_exists") != null) registry.registerPattern("json_exists", "json_contains_path(?1,'one',?2)", booleanBasicType);
            registry.registerPattern("json_value","?1 ->> ?2", stringBasicType);
            registry.registerPattern("json_query","?1 -> ?2", stringBasicType);
        } else if (dialect instanceof AbstractTransactSQLDialect) {
            registry.registerPattern("random", "rand()", doubleBasicType);
            if (registry.findFunctionDescriptor("json_exists") != null) registry.registerPattern("json_exists", "json_value(?1,?2) is not null", booleanBasicType);
        } else if (dialect instanceof OracleDialect) {
            registry.registerPattern("random", "DBMS_RANDOM.VALUE()", doubleBasicType);
        } else if (dialect instanceof H2Dialect) {
            registry.registerPattern("random", "rand()", doubleBasicType);
        }
        if (registry.findFunctionDescriptor("json_exists") == null) registry.registerPattern("json_exists","json_exists(?1,?2)", booleanBasicType);
        if (registry.findFunctionDescriptor("json_value") == null) registry.registerPattern("json_value","json_value(?1,?2)", stringBasicType);
        if (registry.findFunctionDescriptor("json_query") == null) registry.registerPattern("json_query","json_query(?1,?2)", stringBasicType);

       // registerCustomFunction(registry, configuration, stringBasicType, dialect);
    }

    /**
     * 自定义函数代码示例，用于高级查询
     * @param registry 函数注册器
     * @param typeConfig 类型配置
     * @param returnType 返回类型
     * @param dialect 数据库方言
     */
    private void registerCustomFunction(SqmFunctionRegistry registry, TypeConfiguration typeConfig, BasicType<String> returnType, Dialect dialect) {
        SqmFunctionDescriptor jsonbGetFunction = new AbstractSqmSelfRenderingFunctionDescriptor(
                "jsonb_get", // 用于日志调试的名字
                FunctionKind.NORMAL,
                StandardArgumentsValidators.exactly(2),
                StandardFunctionReturnTypeResolvers.invariant(returnType),
                StandardFunctionArgumentTypeResolvers.invariant(typeConfig, org.hibernate.query.sqm.produce.function.FunctionParameterType.STRING, org.hibernate.query.sqm.produce.function.FunctionParameterType.STRING)) {
            @Override
            public void render(SqlAppender appender, List<? extends SqlAstNode> args, ReturnableType<?> returnableType, SqlAstTranslator<?> translator) {
                var arg1 = args.getFirst();
                arg1.accept(translator); // 渲染第一个参数
                appender.appendSql(" -> "); // 代码示例
                var arg2 = args.get(1);
                if (arg2 instanceof Literal literal) { // 如果是静态参数，第二个参数
                    String path = literal.getLiteralValue().toString().trim();
                    if (dialect instanceof PostgreSQLDialect) {
                        // 这里可以改变path
                        new QueryLiteral<>(path, returnType).accept(translator);
                    }
                } else {
                    var arg = args.get(1); // 如果是动态参数，第二个参数
                    arg.accept(translator); // 渲染第二个参数
                }
            }

            @Override
            public boolean alwaysIncludesParentheses() {
                return false; // 如果不需要()则为false，例如random()需要为true，-> 则为false
            }
        };
        registry.register("jsonb_get", jsonbGetFunction);
    }
}
