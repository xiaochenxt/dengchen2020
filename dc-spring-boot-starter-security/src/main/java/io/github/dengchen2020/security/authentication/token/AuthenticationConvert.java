package io.github.dengchen2020.security.authentication.token;

import io.github.dengchen2020.core.utils.JsonUtils;
import io.github.dengchen2020.core.security.principal.Authentication;

/**
 * Token认证信息转换器
 *
 * @author xiaochen
 * @since 2024/4/28
 */
public interface AuthenticationConvert {

    Class<? extends Authentication> convertType();

    default String serialize(Authentication authentication) {
        return JsonUtils.toJson(authentication);
    }

    default Authentication deserialize(String tokenInfo) {
        return JsonUtils.fromJson(tokenInfo, convertType());
    }

}
