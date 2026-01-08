package io.github.dengchen2020.jpa.hibernate;

import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.PostgreSQLDialect;
import org.hibernate.type.descriptor.java.StringJavaType;
import org.hibernate.type.descriptor.jdbc.JdbcType;

/**
 * 在hibernate映射String时做一些自定义处理
 * @author xiaochen
 * @since 2026/1/8
 */
public class DcStringJavaType extends StringJavaType {

    public static final DcStringJavaType INSTANCE = new DcStringJavaType();

    @Override
    public long getDefaultSqlLength(Dialect dialect, JdbcType jdbcType) {
        if (dialect instanceof PostgreSQLDialect) return 0; // 当数据库为PostgreSQL时，String的默认长度为0
        return super.getDefaultSqlLength(dialect, jdbcType);
    }

}
