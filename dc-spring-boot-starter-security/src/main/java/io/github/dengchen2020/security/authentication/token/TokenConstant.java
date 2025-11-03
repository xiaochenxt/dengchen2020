package io.github.dengchen2020.security.authentication.token;

/**
 * token相关的常量
 * @author xiaochen
 * @since 2024/5/21
 */
public final class TokenConstant {

    private TokenConstant() {}

    public static final String TOKEN_NAME = "dc-token";

    public static final String PAYLOAD = "payload";

    public static final String ATI = "ati";

    /**
     * token生成时userId和随机字符串之间的分隔符
     */
    public static final String SEPARATOR = "-";

}
