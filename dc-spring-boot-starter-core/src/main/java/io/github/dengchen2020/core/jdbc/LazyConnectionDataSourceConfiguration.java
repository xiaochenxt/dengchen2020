/*
 * Copyright 2012-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.dengchen2020.core.jdbc;

import javax.sql.DataSource;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.springframework.core.Ordered;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;
import org.springframework.jmx.export.MBeanExportException;
import org.springframework.jmx.export.MBeanExporter;
import org.springframework.jmx.export.naming.MetadataNamingStrategy;
import org.springframework.jmx.support.JmxUtils;

/**
 * <p>SpringBoot4.1.x推出的新功能（Spring早已支持，但是SpringBoot没有自动配置）
 * 实现数据库连接的懒获取，可以显著优化连接池争用问题并可能减少不必要的连接获取以提高性能，这里将其移植到SpringBoot3</p>
 * Replace the auto-configured {@link DataSource} by a
 * {@linkplain LazyConnectionDataSourceProxy lazy proxy} that fetches the underlying JDBC
 * connection as late as possible. Also make sure to register the target
 * {@link DataSource} in the JMX domain if necessary.
 *
 * @author Stephane Nicoll
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(DataSourceAutoConfiguration.class)
@ConditionalOnBean(DataSource.class)
@ConditionalOnProperty(name = "spring.datasource.connection-fetch", havingValue = "lazy")
final class LazyConnectionDataSourceConfiguration {

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    static LazyConnectionDataSourceBeanPostProcessor lazyConnectionDataSourceBeanPostProcessor(
            ObjectProvider<MBeanExporter> mbeanExporter, ObjectProvider<MetadataNamingStrategy> namingStrategy) {
        return new LazyConnectionDataSourceBeanPostProcessor(mbeanExporter, namingStrategy.getIfAvailable());
    }

    static class LazyConnectionDataSourceBeanPostProcessor implements BeanPostProcessor, Ordered {

        private final ObjectProvider<MBeanExporter> mbeanExporter;
        private final MetadataNamingStrategy namingStrategy;

        LazyConnectionDataSourceBeanPostProcessor(ObjectProvider<MBeanExporter> mbeanExporter,
                                                  MetadataNamingStrategy namingStrategy) {
            this.mbeanExporter = mbeanExporter;
            this.namingStrategy = namingStrategy;
        }

        @Override
        public @Nullable Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
            if (beanName.equals("dataSource") && bean instanceof DataSource dataSource) {
                this.mbeanExporter.ifAvailable((exporter) -> {
                    if (JmxUtils.isMBean(dataSource.getClass())) {
                        try {
                            exporter.registerManagedResource(dataSource, namingStrategy.getObjectName(dataSource, beanName));
                        } catch (Throwable e) {
                            throw new MBeanExportException("Unable to generate ObjectName for MBean [" + dataSource + "]", e);
                        }
                    }
                });
                return new LazyConnectionDataSourceProxy(dataSource);
            }
            return bean;
        }

        @Override
        public int getOrder() {
            return 0;
        }

    }

}
