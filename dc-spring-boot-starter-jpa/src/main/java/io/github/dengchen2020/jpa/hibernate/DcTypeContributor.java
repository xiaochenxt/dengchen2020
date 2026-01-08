package io.github.dengchen2020.jpa.hibernate;

import org.hibernate.boot.model.TypeContributions;
import org.hibernate.boot.model.TypeContributor;
import org.hibernate.service.ServiceRegistry;

/**
 * 在hibernate中注册一些类型
 * @author xiaochen
 * @since 2025/1/8
 */
public class DcTypeContributor implements TypeContributor {

    private volatile boolean contributed = false;

    @Override
    public void contribute(TypeContributions typeContributions, ServiceRegistry serviceRegistry) {
        if (contributed) return; // 因为hibernate的bug，这里会被执行两次，简单去重
        typeContributions.contributeJavaType(DcStringJavaType.INSTANCE);
        contributed = true;
    }
}
