package io.github.dengchen2020.ip.config;

import io.github.dengchen2020.ip.service.IpService;
import io.github.dengchen2020.ip.service.impl.xdb.IpXdbServiceImpl;
import io.github.dengchen2020.ip.service.impl.xdb.IpXdbV2ServiceImpl;
import org.lionsoul.ip2region.service.InvalidConfigException;
import org.lionsoul.ip2region.xdb.XdbException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.io.IOException;

/**
 * ip查询配置
 * @author xiaochen
 * @since 2023/5/6
 */
@Configuration(proxyBeanMethods = false)
public final class IpAutoConfiguration {

    @ConditionalOnMissingBean
    @Bean
    IpService ipService(Environment environment) throws IOException, XdbException, InterruptedException, InvalidConfigException {
        var ipv4Location = environment.getProperty("dc.ip.v4.location","ip.xdb");
        var ipv6Location = environment.getProperty("dc.ip.v6.location","ipv6.xdb");
        var verify = environment.getProperty("dc.ip.verify", boolean.class, false);
        var useV2 = environment.getProperty("dc.ip.use-v2", boolean.class, false);
        if (!useV2) return new IpXdbServiceImpl(ipv4Location, ipv6Location, verify);
        var bufferCache = environment.getProperty("dc.ip.buffer-cache.enabled", boolean.class, true);
        var ipv4SearchCount = environment.getProperty("dc.ip.v4.searcher-count", int.class, 20);
        var ipv6SearchCount = environment.getProperty("dc.ip.v6.searcher-count", int.class, 20);
        if (bufferCache) return new IpXdbV2ServiceImpl(ipv4Location, ipv6Location, verify);
        return new IpXdbV2ServiceImpl(ipv4Location, ipv6Location, ipv4SearchCount, ipv6SearchCount, verify);
    }

}
