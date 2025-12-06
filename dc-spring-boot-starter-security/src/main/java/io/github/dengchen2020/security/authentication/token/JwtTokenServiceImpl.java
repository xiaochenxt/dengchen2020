package io.github.dengchen2020.security.authentication.token;

import io.fusionauth.jwt.InvalidJWTSignatureException;
import io.fusionauth.jwt.JWTExpiredException;
import io.fusionauth.jwt.domain.JWT;
import io.github.dengchen2020.core.security.principal.Authentication;
import io.github.dengchen2020.core.utils.JsonUtils;
import io.github.dengchen2020.security.exception.SessionTimeOutException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;

/**
 * 无状态Token认证实现
 * <p>对比有状态：</p>
 * <p>优势：不依赖第三方组件，节省内存，稳定性好，可维护性高</p>
 * <p>劣势：对Token的控制力弱，无法做到踢人下线，实时封禁等功能，因此请求Token的有效期不能太长，刷新Token的有效期可根据业务需求设置稍长一些
 *
 * @author xiaochen
 * @since 2024/4/28
 */
public class JwtTokenServiceImpl implements TokenService {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenServiceImpl.class);

    private final AuthenticationConvert authenticationConvert;

    private final JwtHelper jwtHelper;

    private final long expireSeconds;

    private final long refreshExpireSeconds;

    public JwtTokenServiceImpl(String secret, AuthenticationConvert authenticationConvert, long expireSeconds, long refreshExpireSeconds) {
        this.jwtHelper = new JwtHelper(secret);
        this.authenticationConvert = authenticationConvert;
        this.expireSeconds = expireSeconds;
        this.refreshExpireSeconds = refreshExpireSeconds;
    }

    @Override
    public String getToken(HttpServletRequest request) {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization != null && authorization.length() > 7) {
            return authorization.substring(7);
        }
        String token = request.getHeader(TokenConstant.TOKEN_NAME);
        if (token != null) return token;
        return request.getParameter(TokenConstant.TOKEN_NAME);
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
        String token = jwtHelper.createToken(authentication, expiresIn);
        long refreshTokenExpiresIn = System.currentTimeMillis() + refreshExpireSeconds * 1000;
        String refreshToken = jwtHelper.refreshToken(token, refreshTokenExpiresIn);
        storeRefreshToken(authentication, refreshToken, refreshExpireSeconds);
        return new TokenInfo(token, expiresIn, refreshToken, refreshTokenExpiresIn);
    }

    /**
     * 存储刷新token
     * @param authentication 认证信息对象
     * @param refreshToken 刷新token
     * @param expiresIn 有效期（秒）
     */
    protected void storeRefreshToken(Authentication authentication, String refreshToken, long expiresIn) {

    }

    public TokenInfo refreshToken(String refreshToken) {
        try {
            JWT jwt = jwtHelper.parseToken(refreshToken);
            if (jwtHelper.isRefreshToken(jwt) && checkRefreshToken(refreshToken)) return createToken(readJwt(jwt));
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                switch (e) {
                    case JWTExpiredException jwtExpiredException -> log.debug("刷新token已过期，{}，异常信息：{}", refreshToken, jwtExpiredException.toString());
                    case InvalidJWTSignatureException invalidJWTSignatureException ->
                            log.debug("刷新token无效，{}，异常信息：{}", refreshToken, invalidJWTSignatureException.toString());
                    default -> log.debug(e.toString());
                }
            }
            throw new SessionTimeOutException();
        }
        throw new SessionTimeOutException("不是一个刷新token，" + refreshToken);
    }

    /**
     * 检查刷新token是否有效
     * @param refreshToken 刷新token
     * @return
     */
    protected boolean checkRefreshToken(String refreshToken) {
        return true;
    }

    @Override
    public Authentication readToken(String token) {
        JWT jwt = jwtHelper.parseToken(token);
        if (jwtHelper.isRefreshToken(jwt)) throw new SessionTimeOutException("不是一个请求token，" + token);
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
