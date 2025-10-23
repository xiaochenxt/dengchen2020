package io.github.dengchen2020.security.authentication.token;

import io.fusionauth.jwt.Signer;
import io.fusionauth.jwt.Verifier;
import io.fusionauth.jwt.domain.JWT;
import io.fusionauth.jwt.hmac.HMACSigner;
import io.fusionauth.jwt.hmac.HMACVerifier;
import io.github.dengchen2020.core.security.principal.Authentication;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * @author xiaochen
 * @since 2023/3/30
 */
public class JwtHelper {

    private final Signer signer;

    private final Verifier verifier;

    public JwtHelper(String secret) {
        this.signer = HMACSigner.newSHA256Signer(secret);
        this.verifier = HMACVerifier.newVerifier(secret);
    }

    public JwtHelper(Signer signer, Verifier verifier) {
        this.signer = signer;
        this.verifier = verifier;
    }


    /**
     * 创建Token
     *
     * @param authentication 认证信息
     * @return Token字符串
     */
    public String createToken(Authentication authentication, long expiresIn) {
        JWT jwt = new JWT();
        jwt.setExpiration(Instant.ofEpochMilli(expiresIn).atZone(ZoneId.systemDefault()));
        jwt.addClaim(TokenConstant.PAYLOAD, authentication);
        return JWT.getEncoder().encode(jwt, signer);
    }

    /**
     * 创建Token
     *
     * @param authentication 认证信息
     * @return Token字符串
     */
    public String createToken(Authentication authentication, long expiresIn, Map<String, Object> claims) {
        JWT jwt = new JWT();
        jwt.setExpiration(Instant.ofEpochMilli(expiresIn).atZone(ZoneId.systemDefault()));
        jwt.addClaim(TokenConstant.PAYLOAD, authentication);
        if(claims != null) claims.forEach(jwt::addClaim);
        return JWT.getEncoder().encode(jwt, signer);
    }

    /**
     * 创建Token
     *
     * @param claims 令牌中包含的信息
     * @return Token字符串
     */
    public String createToken(long expiresIn, Map<String, Object> claims) {
        JWT jwt = new JWT();
        jwt.setExpiration(Instant.ofEpochMilli(expiresIn).atZone(ZoneId.systemDefault()));
        if(claims != null) claims.forEach(jwt::addClaim);
        return JWT.getEncoder().encode(jwt, signer);
    }

    /**
     * 解析Token获取令牌中的信息
     *
     * @param token Token字符串
     * @return 令牌中包含的信息
     */
    public JWT parseToken(String token) {
        return JWT.getDecoder().decode(token, verifier);
    }

    /**
     * 判断Token是否过期
     *
     * @param token Token字符串
     * @return true表示Token已过期，false表示Token未过期
     */
    public boolean isTokenExpired(String token) {
        JWT jwt = parseToken(token);
        return jwt.isExpired(ZonedDateTime.now());
    }

    /**
     * 刷新Token
     *
     * @param token          原Token字符串
     * @param expiresIn 多少秒后过期
     * @return 新Token字符串
     */
    public String refreshToken(String token, long expiresIn) {
        return refreshToken(token, expiresIn, UUID.randomUUID().toString().replace("-", ""));
    }

    /**
     * 刷新Token
     *
     * @param token          原Token字符串
     * @param expiresIn 多少秒后过期
     * @return 新Token字符串
     */
    public String refreshToken(String token, long expiresIn, String ati) {
        try {
            JWT jwt = parseToken(token);
            jwt.setExpiration(Instant.ofEpochMilli(expiresIn).atZone(ZoneId.systemDefault()));
            jwt.addClaim(TokenConstant.ATI, ati);
            return JWT.getEncoder().encode(jwt, signer);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 是否是刷新token
     */
    public boolean isRefreshToken(JWT jwt) {
        return jwt.getOtherClaims().containsKey(TokenConstant.ATI);
    }

}
