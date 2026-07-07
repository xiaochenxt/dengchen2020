package io.github.dengchen2020.security.authentication.token;

import io.github.dengchen2020.core.security.principal.Authentication;
import io.github.dengchen2020.core.utils.Base64Utils;
import io.github.dengchen2020.core.utils.StrUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;

/**
 * 有状态Token实现基类
 * @author xiaochen
 * @since 2026/5/16
 */
abstract class AbstractStateTokenService implements TokenService, InitializingBean {

    static final String TOKEN_COMMON_PREFIX = "dc:security:token:";
    static final String TOKEN_INFO_KEY = "dc:security:token:info:";

    protected AuthenticationConvert authenticationConvert;
    protected StringRedisTemplate stringRedisTemplate;

    @Autowired
    public void setAuthenticationConvert(AuthenticationConvert authenticationConvert) {
        this.authenticationConvert = authenticationConvert;
    }

    @Autowired
    public void setStringRedisTemplate(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public void afterPropertiesSet() {
        if (authenticationConvert == null) throw new IllegalStateException("authenticationConvert must be set");
        if (stringRedisTemplate == null) throw new IllegalStateException("stringRedisTemplate must be set");
    }

    protected final long expireSeconds;
    protected final boolean autorenewal;
    protected final long autorenewalSeconds;
    protected final String tokenName;

    public AbstractStateTokenService(long expireSeconds, boolean autorenewal, long autorenewalSeconds, String tokenName) {
        this.expireSeconds = expireSeconds;
        if (autorenewal) {
            if (autorenewalSeconds <= 0) throw new IllegalArgumentException("autorenewalSeconds must be greater than 0");
            if (autorenewalSeconds > expireSeconds / 2) throw new IllegalArgumentException("autorenewalSeconds must be less than expireSeconds / 2");
        }
        this.autorenewal = autorenewal;
        this.autorenewalSeconds = autorenewalSeconds;
        this.tokenName = tokenName;
    }

    @Override
    public String tokenName() {
        return tokenName;
    }

    protected String generateTokenStr(Authentication authentication) {
        return StrUtils.uuidSimplified() + encodeUserId(authentication.userId());
    }

    public String getUserId(String token) {
        return decodeUserId(token.substring(32));
    }

    protected String encodeUserId(String userId) {
        return Base64Utils.encodeUrlToString(userId);
    }

    protected String decodeUserId(String userId) {
        return Base64Utils.decodeToString(userId);
    }

    protected TokenInfo generateTokenInfo(String token, long expiresIn) {
        return new TokenInfo(token, expiresIn);
    }

    // ======== Redis Key 辅助方法 ========

    protected abstract String tokenKeyPrefix();

    protected String tokenKey(String userId) {
        return tokenKeyPrefix() + "{" + userId + "}";
    }

    protected String infoKey(String userId) {
        return TOKEN_INFO_KEY + "{" + userId + "}";
    }

    protected List<String> keys(String userId) {
        return List.of(tokenKey(userId), infoKey(userId));
    }

    // ========= 通用方法 =========

    /**
     * 刷新redis中的认证信息，相当于更新用户session中的信息
     * @param authentication 认证信息
     * @return 如果用户在线则刷新返回true，否则失败返回false
     */
    public boolean refreshAuthentication(@NonNull Authentication authentication) {
        try {
            return stringRedisTemplate.opsForValue().setIfPresent(infoKey(authentication.userId()), authenticationConvert.serialize(authentication));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * 使用户的所有token失效，即强制下线
     * @Note: 虽然这里只会清除该用户当前设备的所有token，但实际效果会使该用户在所有设备下线
     * @param userId
     */
    public void offline(String userId) {
        stringRedisTemplate.unlink(keys(userId));
    }

}
