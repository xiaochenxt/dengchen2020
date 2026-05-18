package io.github.dengchen2020.core.security.principal;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户认证信息
 *
 * @author xiaochen
 * @since 2024/4/28
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SimpleUserAuthentication implements Authentication, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    private final String userId;
    @JsonProperty
    private final Map<String, Object> attributes;

    public SimpleUserAuthentication(String userId) {
        this.userId = userId;
        this.attributes = new HashMap<>();
    }

    @JsonProperty
    @Override
    public String userId() {
        return userId;
    }

    @Override
    public void setAttribute(String name, Object value) {
        attributes.put(name, value);
    }

    @Override
    public Object getAttribute(String name) {
        return attributes.get(name);
    }
}
