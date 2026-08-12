package io.github.dengchen2020.security.aot;

import io.fusionauth.jwt.domain.Header;
import io.fusionauth.jwt.domain.JWT;
import io.fusionauth.jwt.json.ZonedDateTimeDeserializer;
import io.fusionauth.jwt.json.ZonedDateTimeSerializer;
import io.github.dengchen2020.aot.utils.AotUtils;
import io.github.dengchen2020.security.authentication.token.TokenInfo;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

/**
 * @author xiaochen
 * @since 2025/5/23
 */
public class SecurityRuntimeHintsRegistrar implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        AotUtils aotUtils = new AotUtils(hints, classLoader);
        aotUtils.registerReflection(JWT.class, Header.class, ZonedDateTimeSerializer.class, ZonedDateTimeDeserializer.class, TokenInfo.class);
    }

}
