package io.github.dengchen2020.jpa.base;

import io.github.dengchen2020.core.security.context.SecurityContextHolder;
import io.github.dengchen2020.core.security.principal.TenantInfo;
import org.jspecify.annotations.NonNull;
import org.springframework.data.spel.spi.EvaluationContextExtension;

import java.util.HashMap;
import java.util.Map;

public class DcEvaluationContextExtension implements EvaluationContextExtension {
    @Override
    public @NonNull String getExtensionId() {
        return "dc";
    }

    @Override
    public @NonNull Map<String, Object> getProperties() {
        Map<String, Object> properties = HashMap.newHashMap(2);
        var authentication = SecurityContextHolder.getAuthentication();
        if (authentication == null) {
            properties.put("userId", null);
        } else {
            properties.put("userId", authentication.userId());
        }
        if (!(authentication instanceof TenantInfo tenantInfo)) {
            properties.put("tenantId", null);
        } else {
            properties.put("tenantId", tenantInfo.tenantId());
        }
        return properties;
    }

}
