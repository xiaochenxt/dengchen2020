package io.github.dengchen2020.jpa.hibernate;

import org.hibernate.boot.ResourceStreamLocator;
import org.hibernate.boot.spi.AdditionalMappingContributions;
import org.hibernate.boot.spi.AdditionalMappingContributor;
import org.hibernate.boot.spi.InFlightMetadataCollector;
import org.hibernate.boot.spi.MetadataBuildingContext;
import org.hibernate.engine.config.spi.ConfigurationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 在hibernate中做一些额外配置
 * @author xiaochen
 * @since 2026/4/17
 */
public class DcMetadataContributor implements AdditionalMappingContributor {

    private static final Logger log = LoggerFactory.getLogger(DcMetadataContributor.class);

    @Override
    public void contribute(AdditionalMappingContributions contributions, InFlightMetadataCollector metadata, ResourceStreamLocator resourceStreamLocator, MetadataBuildingContext buildingContext) {
        var serviceRegistry = buildingContext.getBootstrapContext().getServiceRegistry();
        var configService = serviceRegistry.getService(ConfigurationService.class);
        if (configService == null) return;
        var settings = configService.getSettings();
        boolean dynamicInsert = (boolean) settings.getOrDefault("dc.hibernate.dynamic_insert", false);
        boolean dynamicUpdate = (boolean) settings.getOrDefault("dc.hibernate.dynamic_update", false);
        if (dynamicInsert || dynamicUpdate) {
            for (var entity : metadata.getEntityBindings()) {
                if (dynamicInsert) entity.setDynamicInsert(true);
                if (dynamicUpdate) entity.setDynamicUpdate(true);
            }
            if (dynamicInsert && log.isDebugEnabled()) log.debug("hibernate所有JPA实体启用动态生成新增sql（@DynamicInsert）");
            if (dynamicUpdate) log.info("hibernate所有JPA实体启用动态生成更新sql（@DynamicUpdate）");
        }
    }

}
