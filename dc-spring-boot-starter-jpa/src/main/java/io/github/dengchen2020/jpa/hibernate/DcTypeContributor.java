package io.github.dengchen2020.jpa.hibernate;

import org.hibernate.boot.model.TypeContributions;
import org.hibernate.boot.model.TypeContributor;
import org.hibernate.engine.config.spi.ConfigurationService;
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
        if (contributed) return; // 因为hibernate的bug，详见：https://hibernate.atlassian.net/browse/HHH-20070，这里会被执行两次，简单去重
        contributed = true;
        var configService = serviceRegistry.getService(ConfigurationService.class);
        if (configService == null) return;
        var settings = configService.getSettings();
        boolean textLengthLimitEnabled = (boolean) settings.getOrDefault("dc.hibernate.postgres.text-length-limit.enabled", true);
        if (!textLengthLimitEnabled) typeContributions.contributeJavaType(DcStringJavaType.INSTANCE);
    }
}
