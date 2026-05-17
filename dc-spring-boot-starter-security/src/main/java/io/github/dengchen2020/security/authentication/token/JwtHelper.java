package io.github.dengchen2020.security.authentication.token;

import io.fusionauth.jwt.JWTExpiredException;
import io.fusionauth.jwt.Signer;
import io.fusionauth.jwt.Verifier;
import io.fusionauth.jwt.domain.JWT;
import io.fusionauth.jwt.hmac.HMACSigner;
import io.fusionauth.jwt.hmac.HMACVerifier;

import java.time.Instant;
import java.time.ZoneId;

/**
 * JWT工具
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
     * 编码JWT
     *
     * @param jwt {@link JWT}
     * @return JWT字符串
     */
    public String encode(JWT jwt) {
        return JWT.getEncoder().encode(jwt, signer);
    }

    /**
     * 解码JWT，如果过期则抛出{@link JWTExpiredException}异常
     *
     * @param token JWT字符串
     * @return {@link JWT}
     */
    public JWT decode(String token) {
        return JWT.getDecoder().decode(token, verifier);
    }

    /**
     * 创建Token
     * @param expiresIn 过期时间（时间戳）
     * @return {@link JWT}
     */
    public JWT create(long expiresIn) {
        return create(expiresIn, null);
    }

    /**
     * 创建Token
     *
     * @param expiresIn 过期时间（时间戳）
     * @return {@link JWT}
     */
    public JWT create(long expiresIn, String jti) {
        return create(expiresIn, jti, null);
    }

    /**
     * 创建Token
     *
     * @param expiresIn 过期时间（时间戳）
     * @param jti JWT唯一ID，可用于防止重复使用
     * @param sub 用户ID，可用于刷新令牌
     * @return {@link JWT}
     */
    public JWT create(long expiresIn, String jti, String sub) {
        JWT jwt = new JWT();
        jwt.setExpiration(Instant.ofEpochMilli(expiresIn).atZone(ZoneId.systemDefault()));
        if (jti != null) jwt.setUniqueId(jti);
        if (sub != null) jwt.setSubject(sub);
        return jwt;
    }

}
