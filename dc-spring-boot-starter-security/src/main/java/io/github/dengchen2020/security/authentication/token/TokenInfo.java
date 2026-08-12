package io.github.dengchen2020.security.authentication.token;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * token信息
 *
 * @param token                 token
 * @param expiresIn             有效期（秒）
 * @param refreshToken          刷新token
 * @param refreshTokenExpiresIn 刷新token有效期
 * @author xiaochen
 * @since 2024/4/16
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record TokenInfo(String token, long expiresIn, String refreshToken, Long refreshTokenExpiresIn) {

    public TokenInfo(String token,long expiresIn){
        this(token, expiresIn, null, null);
    }

}
