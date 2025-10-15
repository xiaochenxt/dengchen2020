package io.github.dengchen2020.ip.config;

import io.github.dengchen2020.ip.service.IpService;
import io.github.dengchen2020.ip.service.impl.xdb.IpXdbServiceImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * ip查询配置
 * @author xiaochen
 * @since 2023/5/6
 */
@Configuration(proxyBeanMethods = false)
public class IpAutoConfiguration {

    @ConditionalOnMissingBean
    @Bean
    public IpService ipService(Environment environment){
        return new IpXdbServiceImpl(environment.getProperty("dc.ip.v4.location","ip.xdb"), environment.getProperty("dc.ip.v6.location","ipv6.xdb"), environment.getProperty("dc.ip.verify", boolean.class, true));
    }

}
