package io.github.dengchen2020.security.authentication.token;

import io.fusionauth.jwt.Signer;
import io.fusionauth.jwt.Verifier;
import io.fusionauth.jwt.domain.JWT;
import io.fusionauth.jwt.hmac.HMACSigner;
import io.fusionauth.jwt.hmac.HMACVerifier;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

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
     * 编码JWT
     *
     * @param jwt JWT
     * @return JWT字符串
     */
    public String encode(JWT jwt) {
        return JWT.getEncoder().encode(jwt, signer);
    }

    /**
     * 解码JWT
     *
     * @param token JWT字符串
     * @return {@link JWT}
     */
    public JWT decode(String token) {
        return JWT.getDecoder().decode(token, verifier);
    }

    /**
     * 创建Token
     *
     * @return {@link JWT}
     */
    public JWT create(long expiresIn) {
        return create(expiresIn, null);
    }

    /**
     * 创建Token
     *
     * @return {@link JWT}
     */
    public JWT create(long expiresIn, String jti) {
        return create(expiresIn, jti, null);
    }

    /**
     * 创建Token
     *
     * @return {@link JWT}
     */
    public JWT create(long expiresIn, String jti, String sub) {
        JWT jwt = new JWT();
        jwt.setExpiration(Instant.ofEpochMilli(expiresIn).atZone(ZoneId.systemDefault()));
        if (jti != null) jwt.setUniqueId(jti);
        if (sub != null) jwt.setSubject(sub);
        return jwt;
    }

    /**
     * 判断是否过期
     *
     * @param jwt {@link JWT}
     * @return true表示Token已过期，false表示Token未过期
     */
    public boolean isExpired(JWT jwt) {
        return jwt.isExpired(ZonedDateTime.now());
    }

}
