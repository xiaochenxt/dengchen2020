package io.github.dengchen2020.core.web.mvc;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * 静态资源Servlet自动配置
 *
 * @author xiaochen
 * @since 2025/6/23
 */
@ConditionalOnProperty(value = "dc.static.servlet.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@Import(StaticServletBeanRegistrar.class)
@Configuration(proxyBeanMethods = false)
public class StaticServletAutoConfiguration {



}
