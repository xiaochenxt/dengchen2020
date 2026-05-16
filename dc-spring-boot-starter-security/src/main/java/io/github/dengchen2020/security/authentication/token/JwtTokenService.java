package io.github.dengchen2020.security.authentication.token;

import io.fusionauth.jwt.InvalidJWTSignatureException;
import io.fusionauth.jwt.JWTExpiredException;
import io.fusionauth.jwt.domain.JWT;
import io.github.dengchen2020.core.security.principal.Authentication;
import io.github.dengchen2020.core.utils.JsonUtils;
import io.github.dengchen2020.core.utils.StrUtils;
import io.github.dengchen2020.security.exception.SessionTimeOutException;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 基于JWT实现无状态Token认证
 * <p>对比有状态：</p>
 * <p>优势：不依赖第三方组件，节省内存，稳定性好，可维护性高</p>
 * <p>劣势：对Token的控制力弱，无法做到踢人下线，实时封禁等功能，因此请求Token的有效期不能太长，刷新Token的有效期可根据业务需求设置稍长一些
 *
 * @author xiaochen
 * @since 2024/4/28
 */
public class JwtTokenService implements TokenService, InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenService.class);

    private AuthenticationConvert authenticationConvert;

    @Autowired
    public void setAuthenticationConvert(AuthenticationConvert authenticationConvert) {
        this.authenticationConvert = authenticationConvert;
    }

    @Override
    public void afterPropertiesSet() {
        if (authenticationConvert == null) throw new IllegalStateException("authenticationConvert must be set");
    }

    private final JwtHelper jwtHelper;

    private final long expireSeconds;

    private final long refreshExpireSeconds;
    private final String tokenName;

    public JwtTokenService(String secret, long expireSeconds, long refreshExpireSeconds, String tokenName) {
        this.jwtHelper = new JwtHelper(secret);
        this.expireSeconds = expireSeconds;
        this.refreshExpireSeconds = refreshExpireSeconds;
        this.tokenName = tokenName;
    }

    @Override
    public String tokenName() {
        return tokenName;
    }

    @Override
    public TokenInfo createToken(Authentication authentication) {
        return createToken(authentication, expireSeconds, refreshExpireSeconds);
    }

    /**
     * 创建token
     * @param authentication 认证信息对象
     * @param expireSeconds 过期秒数
     * @param refreshExpireSeconds 刷新token过期秒数
     * @return Token信息
     */
    public TokenInfo createToken(Authentication authentication, long expireSeconds, long refreshExpireSeconds) {
        long expiresIn = System.currentTimeMillis() + expireSeconds * 1000;
        String token = jwtHelper.encode(jwtHelper.create(expiresIn, StrUtils.uuidSimplified(), authentication.getName())
                .addClaim(TokenConstant.PAYLOAD, authentication));
        if (refreshExpireSeconds > 0) {
            long refreshTokenExpiresIn = System.currentTimeMillis() + refreshExpireSeconds * 1000;
            var refreshToken = createRefreshToken(authentication, refreshExpireSeconds);
            return new TokenInfo(token, expiresIn, refreshToken, refreshTokenExpiresIn);
        }
        return new TokenInfo(token, expiresIn);
    }

    /**
     * 创建刷新token
     * @param authentication 认证信息对象
     * @param expiresIn 有效期（秒）
     */
    public String createRefreshToken(Authentication authentication, long expiresIn) {
        var jti = StrUtils.uuidSimplified();
        var sub = authentication.getName();
        storeRefreshToken(expiresIn, jti, sub);
        return jwtHelper.encode(jwtHelper.create(expiresIn, jti, sub));
    }

    /**
     * 存储刷新token
     * @param expiresIn 有效期（秒）
     * @param jti 刷新token的唯一ID
     * @param sub 用户ID，唯一标识
     */
    protected void storeRefreshToken(long expiresIn, String jti, String sub) {

    }

    /**
     * 刷新token
     * @param refreshToken 刷新token
     * @return Token信息
     */
    public TokenInfo refreshToken(String refreshToken) {
        try {
            JWT jwt = jwtHelper.decode(refreshToken);
            return createToken(checkRefreshToken(jwt.getString("jti"), jwt.getString("sub")));
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                switch (e) {
                    case JWTExpiredException jwtExpiredException -> log.debug("刷新token已过期，{}，异常信息：{}", refreshToken, jwtExpiredException.toString());
                    case InvalidJWTSignatureException invalidJWTSignatureException ->
                            log.debug("刷新token无效，{}，异常信息：{}", refreshToken, invalidJWTSignatureException.toString());
                    default -> log.debug(e.toString());
                }
            }
            if (e instanceof SessionTimeOutException) throw e;
            throw new SessionTimeOutException();
        }
    }

    /**
     * 检查刷新token，如果有效需返回认证信息
     * @param jti 刷新token的唯一ID
     * @param sub 用户ID，唯一标识
     * @return {@link TokenInfo}
     */
    protected Authentication checkRefreshToken(String jti, String sub) {
        throw new SessionTimeOutException("未实现刷新token");
    }

    @Override
    public @Nullable Authentication readToken(String token) {
        JWT jwt = jwtHelper.decode(token);
        return readJwt(jwt);
    }

    /**
     * 读取jwt中的认证信息
     *
     * @param jwt jwt
     * @return {@link Authentication}
     */
    private Authentication readJwt(JWT jwt) {
        return JsonUtils.convertValue(jwt.getOtherClaims().get(TokenConstant.PAYLOAD), authenticationConvert.type());
    }

}
